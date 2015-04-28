/*
 * JetStream ML
 * UpnpServiceWrapper.java
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

/**
 * An android centric upnp Service wrapper providing instantiation, binding and unbinding
 * with a listener interface for callback on device changes.
 */
public class UpnpServiceWrapper {

    private AndroidUpnpService mUpnpService;
    private Activity mActivity; // Main context
    private BrowseRegistryListener mRegistryListener = new BrowseRegistryListener();
    /*
        service connection implementation
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mUpnpService = (AndroidUpnpService) service;

            // Get ready for future device advertisements
            mUpnpService.getRegistry().addListener(mRegistryListener);

            // Now add all devices to the list we already know about
            for (Device device : mUpnpService.getRegistry().getDevices()) {
                mRegistryListener.deviceAdded(device);
            }

            // Search asynchronously for all devices, they will respond soon
            mUpnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            mUpnpService = null;
        }
    };

    private UpnpServiceListener mUpnpServiceListener; // who's listening for updates from service.

    /**
     * Construct the service wrapper with service listener, which could be an activity or a fragment
     * class which implement the listener interface. The activity is the base activity to bind the
     * service to.
     *
     * @param activity reference to the activity starting, stopping and binding to the service.
     */
    public UpnpServiceWrapper(Activity activity) {
        if (activity == null)
            throw new IllegalArgumentException("No associated activity");
        mActivity = activity;
    }

    /**
     * Starts the service.
     *
     * @return the service.
     */
    public boolean startService() {
        ComponentName componentName = mActivity.startService(new Intent(mActivity, AndroidUpnpServiceImpl.class));
        return componentName != null;
    }

    /**
     * Explicitly Stops the service.
     *
     * @return boolean flag.
     */
    public boolean stopService() {
        boolean flag = mActivity.stopService(new Intent(mActivity, AndroidUpnpServiceImpl.class));
        mActivity = null;
        return flag;
    }

    /**
     * Binds the service to an application context.
     *
     * @return boolean flag.
     */
    public boolean bindService() {
        // This will start the UPnP service if it wasn't already started
        return mActivity != null && mActivity.bindService(new Intent(mActivity, AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * UnBind's the service from an application context, cleaning up references.
     *
     * @return boolean flag.
     */
    public boolean unBindService() {
        if (mUpnpService != null)
            mUpnpService.getRegistry().removeListener(mRegistryListener);

        if (mActivity == null) {
            serviceConnection = null;
            return stopService();
        }
        try {
            mActivity.unbindService(serviceConnection);
            serviceConnection = null;
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (NullPointerException ex) {
            // no serviceConnection.
            return true;
        }
    }

    /**
     * Return the actual service control point.
     *
     * @return ControlPoint.
     */
    public ControlPoint getControlPoint() {
        return mUpnpService.getControlPoint();
    }

    /**
     * Set the listener object when devices are added found and added to the registry.
     *
     * @param upnpServiceListener object implementing the listener interface.
     */
    public void setUpnpServiceListener(UpnpServiceListener upnpServiceListener) {
        this.mUpnpServiceListener = upnpServiceListener;
    }

    /**
     * Returns the actual service object.
     *
     * @return AndroidUpnpService implementation.
     */
    public AndroidUpnpService getAndroidUpnpService() {
        return mUpnpService;
    }

    /**
     * Optional
     * Public interface provides callbacks to the implementing class when a device is added or removed.
     */
    public interface UpnpServiceListener {
        void upnpDeviceAdd(Device device);

        void upnpDeviceRemove(Device device);
    }

    /*
        Callback methods from service when devices are registered on the LAN.
     */
    protected class BrowseRegistryListener extends DefaultRegistryListener {
        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            deviceRemoved(device);
        }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        // Async add to list
        public void deviceAdded(final Device device) {
            if (mUpnpServiceListener != null)  // tell someone the devices updated.
                mUpnpServiceListener.upnpDeviceAdd(device);
        }

        // Async remove from list
        public void deviceRemoved(final Device device) {
            if (mUpnpServiceListener != null)  // tell someone the devices updated.
                mUpnpServiceListener.upnpDeviceRemove(device);
        }

    }

}
