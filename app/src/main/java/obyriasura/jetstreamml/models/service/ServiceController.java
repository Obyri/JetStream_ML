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
 * A facade controller to the android upnp service providing an exposed API to;
 * start/stop the service.
 * update device lists.
 * and browse into devices/containers/folders.
 */
public class ServiceController implements UpnpServiceWrapper.UpnpServiceListener {

    private UpnpServiceWrapper upnpServiceWrapper;
    private ArrayList<AbstractItemModel> devicesList = new ArrayList<>();
    private ControlPointListener controlPointListener;

    public ServiceController(Activity androidActivity) throws IllegalArgumentException {
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

    /* Listener interface implementation. -------------- */
    @Override
    public void upnpDeviceAdd(Device device) {
        // add to list adapter, refresh if exists.
        DeviceModel deviceModel;
        try {
            deviceModel = new DeviceModel(device);
        } catch (DeviceModel.DeviceNotFitModelException e) {
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
        } catch (DeviceModel.DeviceNotFitModelException ex) {
            // log stack trace only.
            ex.printStackTrace();
        }
    }
    /* ------------------------------------------------- */

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

    /**
     * Simple method to stop the service and cleanup resources.
     *
     * @return true on success, false otherwise.
     */
    public boolean dispose() {
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
            getControlPoint().execute(new BrowseActionCallback(contentDirectoryService, id, itemModel));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Listener interface that notifies the listening class about updates to the model.
     */
    public interface ControlPointListener {
        public void devicesChanged();

        public void browseComplete(AbstractItemModel item);
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
