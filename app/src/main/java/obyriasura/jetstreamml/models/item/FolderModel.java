package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * Created by obyri on 4/4/15.
 */
public class FolderModel extends AbstractChildModel {

    private Container folder;

    public FolderModel(DIDLObject folder, Service service, AbstractItemModel parent) {
        super(folder.getId(), service, parent);
        if (folder instanceof Container) {
            this.folder = (Container) folder;
        } else {
            throw new IllegalArgumentException("Invalid DIDLObject type");
        }
        addIconUrl();
    }

    public Container getFolder() {
        return folder;
    }

    @Override
    public boolean browseChildren(ServiceController serviceController) {
        return serviceController != null && serviceController.createBrowseAction(this.getContentDirectoryService(), this.getId(), this);
    }

    @Override
    public String getDescription() {
        StringBuilder descriptionString = new StringBuilder();
        DIDLObject.Property longDesc = findProperty(LONG_DESCRIPTION_PROPERTY, folder);
        if (longDesc == null)
            return "";
        descriptionString.append(longDesc.toString());
        if (descriptionString.length() == 0) {
            return "";
        }
        if (descriptionString.length() > DESCRIPTION_LENGTH) {
            descriptionString.delete(DESCRIPTION_LENGTH, descriptionString.length());
        }
        descriptionString.append(" ...");
        return descriptionString.toString();
    }

    @Override
    protected void addIconUrl() {
        DIDLObject.Property iconArt = findProperty(ALBUM_ART_PROPERTY, folder);
        if (iconArt != null) {
            try {
                setIconUrl(new URL(iconArt.toString()));
            } catch (MalformedURLException e) { /* do nothing */}
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderModel that = (FolderModel) o;
        return folder.equals(that.folder);
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    @Override
    public String toString() {
        return folder.getTitle();

    }
}
