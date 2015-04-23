package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.seamless.util.MimeType;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * Created by obyri on 3/31/15.
 */
public class DeviceModel extends AbstractItemModel {
    private final Device device;
    private Icon deviceIcon;

    public DeviceModel(Device device) throws DeviceNotFitModelException {
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
            throw new DeviceNotFitModelException("Not the device we are looking for.");
        }
        this.device = device;

        // add the icon from the device.
        addIcon();
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

    public Device getDevice() {
        return device;
    }

    public Icon getDeviceIcon() {
        return deviceIcon;
    }

    @Override
    public boolean browseChildren(ServiceController serviceController) {
        return serviceController != null && serviceController.createBrowseAction(this.getContentDirectoryService(), "0", this);
    }

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
    public URL getIconUrl() {
        if (device instanceof RemoteDevice && deviceIcon != null) {
            return ((RemoteDevice) device).normalizeURI(deviceIcon.getUri());
        }

        try {
            return deviceIcon != null ? deviceIcon.getUri().toURL() : null;
        } catch (MalformedURLException ex) {
            return null;
        }
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

    public class DeviceNotFitModelException extends Exception {
        public DeviceNotFitModelException(String message) {
            super(message);
        }
    }
}
