/*
 * JetStream ML
 * DeviceModel.java
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

package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.seamless.util.MimeType;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * A Concrete model describing a media device on the network.
 */
public class DeviceModel extends AbstractItemModel {
    private final Device device;
    private Icon deviceIcon;

    public DeviceModel(Device device) throws DeviceMismatchException {
        super(device.getIdentity().toString());

        // Add the device if it is browseable
        Service[] services = device.findServices();
        boolean browseable = false;
        for (Service s : services) {
            if (s.getServiceType().getType().equals("ContentDirectory")) {
                browseable = true;
                setContentDirectoryService(s);
            }
        }
        if (!browseable) {
            throw new DeviceMismatchException("Not the device we are looking for.");
        }
        this.device = device;
        setItemType(ItemTypeEnum.TYPE_DEVICE);

        // add the icon from the device.
        addIcon();
        setIconUrl(null);
    }

    public Device getDevice() {
        return device;
    }

    /**
     * Searches the Devices icon array for a compatible Icon.
     */
    private void addIcon() {
        Icon[] icons = this.device.getIcons();
        for (Icon ic : icons) {
            MimeType mt = ic.getMimeType();
            if (mt.getType().equals("image")) {
                if (mt.getSubtype().contains("png") || mt.getSubtype().contains("jpeg") || mt.getSubtype().contains("bmp")) {
                    if (ic.getHeight() > 64 && ic.getWidth() > 64) {
                        this.deviceIcon = ic;
                    }
                }
            }
        }
        // if still null then use first available if any.
        if (this.deviceIcon == null && icons.length > 0) {
            this.deviceIcon = icons[0];
        }
    }

    @Override
    public void setIconUrl(URL iconUrl) {
        if (iconUrl == null) {
            if (device instanceof RemoteDevice && deviceIcon != null) {
                super.setIconUrl(((RemoteDevice) device).normalizeURI(deviceIcon.getUri()));
            }

            try {
                super.setIconUrl(deviceIcon != null ? deviceIcon.getUri().toURL() : null);
            } catch (MalformedURLException ex) {/* do nothing */} catch (IllegalArgumentException ex) {
                ex.getMessage();
            }
        } else {
            super.setIconUrl(iconUrl);
        }
    }

    @Override
    public boolean browseChildren(ServiceController serviceController) {
        return serviceController != null && serviceController.createBrowseAction(this.getContentDirectoryService(), "0", this);
    }

    @Override
    public String getDescription() {
        StringBuilder descriptionString = new StringBuilder();
        try {
            descriptionString.append(device.getDetails().getModelDetails().getModelDescription()).append("\n");
            descriptionString.append(device.getDetails().getManufacturerDetails().getManufacturer());
        } catch (NullPointerException ex) {
            return "";
        }
        return descriptionString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceModel that = (DeviceModel) o;
        return device.equals(that.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public String toString() {
        String name = device.getDetails().getFriendlyName();
        return device.isFullyHydrated() ? name : name + " *";

    }

    public class DeviceMismatchException extends Exception {
        public DeviceMismatchException(String message) {
            super(message);
        }
    }
}
