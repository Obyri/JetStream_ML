/*
 * JetStream ML
 * ServiceController.java
 *     Copyright (C) 2015  Reice Robinson
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package obyriasura.jetstreamml.models.service;

import android.app.Activity;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;

import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.item.DeviceModel;
import obyriasura.jetstreamml.models.item.FolderModel;
import obyriasura.jetstreamml.models.item.ItemModel;

/**
 * Service controller class.
 * A facade class to the android upnp service providing an exposed API to;
 * start/stop the service.
 * update device lists.
 * and browse into devices/containers/folders.
 */
public class ServiceManager implements UpnpServiceWrapper.UpnpServiceListener {

    private static ServiceManager instance;
    private UpnpServiceWrapper upnpServiceWrapper;
    private ArrayList<AbstractItemModel> devicesList = new ArrayList<>();
    private ControlPointListener controlPointListener;

    private ServiceManager(Activity androidActivity) throws IllegalArgumentException {
        // Setup UPNP Service after their is an adapter to receive input (ASYNC).
        upnpServiceWrapper = new UpnpServiceWrapper(androidActivity);

        // explicit start and bind to service
        if (upnpServiceWrapper.startService()) {
            if (!upnpServiceWrapper.bindService()) {
                this.upnpServiceWrapper = null;
                throw new IllegalArgumentException("Service Failed to bind");
            }
        }

        // We made it this far should be good to go.
        upnpServiceWrapper.setUpnpServiceListener(this);
        if (androidActivity instanceof ControlPointListener)
            controlPointListener = (ControlPointListener) androidActivity;

    }

    public static ServiceManager startUpnpService(Activity activity) {
        // Everything relies on the service
        try {
            instance = new ServiceManager(activity);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            instance = null;
        } finally {
            return instance;
        }
    }

    public static ServiceManager getInstance() {
        return instance;
    }

    public ArrayList<AbstractItemModel> getDevicesList() {
        return devicesList;
    }

    public UpnpServiceWrapper getUpnpServiceWrapper() {
        return upnpServiceWrapper;
    }

    public boolean unBindService() {
        try {
            return upnpServiceWrapper.unBindService();
        } catch (Exception ex) {
            return false;
        }
    }

    public void scanForNewServices() {
        this.getControlPoint().getRegistry().removeAllRemoteDevices();
        this.getControlPoint().search();
    }

    /**
     * Simple method to stop the service and cleanup resources.
     *
     * @return true on success, false otherwise.
     */
    public boolean stopService() {
        try {
            controlPointListener = null;
            upnpServiceWrapper.unBindService();
            upnpServiceWrapper.stopService();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the Android Upnp Service Control Point
     *
     * @return Control Point.
     */
    public org.fourthline.cling.controlpoint.ControlPoint getControlPoint() {
        return upnpServiceWrapper.getControlPoint();
    }

    public boolean createBrowseAction(Service contentDirectoryService, String id, AbstractItemModel itemModel) {
        try {
            //todo .execute return a future obj, use to trigger cancel etc...
            getControlPoint().execute(new BrowseActionCallback(contentDirectoryService, id, itemModel));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /* Listener interface implementation. -------------- */
    @Override
    public void upnpDeviceAdd(Device device) {
        // add to list adapter, refresh if exists.
        DeviceModel deviceModel;
        try {
            deviceModel = new DeviceModel(device);
        } catch (DeviceModel.DeviceMismatchException e) {
            e.printStackTrace();
            return;
        }

        int pos = devicesList.indexOf(deviceModel);

        if (pos >= 0) {
            //device is in the list
            devicesList.remove(deviceModel);
            devicesList.add(pos, deviceModel);
        } else {
            devicesList.add(deviceModel);
        }
        // send notification that the model has changed
        controlPointListener.devicesChanged();
    }

    @Override
    public void upnpDeviceRemove(Device device) {
        try {
            DeviceModel deviceModel = new DeviceModel(device);
            // remove from list adapter
            if (devicesList.indexOf(deviceModel) > 0) devicesList.remove(deviceModel);
            // send notification that the model has changed
            controlPointListener.devicesChanged();
        } catch (DeviceModel.DeviceMismatchException ex) {
            // log stack trace only.
            ex.printStackTrace();
        }
    }
    /* ------------------------------------------------- */

    /**
     * Listener interface that notifies the listening class about updates to the model.
     */
    public interface ControlPointListener {
        void devicesChanged();

        void browseComplete(AbstractItemModel item);
    }

    class BrowseActionCallback extends Browse {

        Service contentDirectoryService;
        AbstractItemModel parentItem;

        public BrowseActionCallback(Service s, String id, AbstractItemModel itemModel) {
            super(s, id, BrowseFlag.DIRECT_CHILDREN, CAPS_WILDCARD, 0, null, new SortCriterion(true, "dc:title"));
            contentDirectoryService = s;
            parentItem = itemModel;
        }

        @Override
        public void received(ActionInvocation actionInvocation, DIDLContent didl) {
            List<Container> containers = didl.getContainers();
            List<Item> items = didl.getItems();
            ArrayList<AbstractItemModel> children = new ArrayList<>();
            try {
                for (Container folder : containers) {
                    children.add(new FolderModel(folder, contentDirectoryService, parentItem));
                }
                for (Item item : items) {
                    children.add(new ItemModel(item, contentDirectoryService, parentItem));
                }
            } catch (IllegalArgumentException ex) {
                failure(actionInvocation, null, ex.getMessage());
            }
            parentItem.setChildren(children);
            controlPointListener.browseComplete(parentItem);

        }

        @Override
        public void updateStatus(Status status) {
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

        }
    }

}
