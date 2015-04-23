package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * Created by obyri on 4/5/15.
 */
public abstract class AbstractItemModel {
    public static final String ALBUM_ART_PROPERTY = "albumArtURI";
    public static final String LONG_DESCRIPTION_PROPERTY = "longDescription";
    public static final String GENRE_PROPERTY = "genre";
    public final int DESCRIPTION_LENGTH = 320;

    private final String id;
    private Service contentDirectoryService;
    private ArrayList<AbstractItemModel> children = new ArrayList<>();
    private byte[] rawIconData;

    /**
     * Constructor.
     *
     * @param id upnp uid.
     */
    public AbstractItemModel(String id) {
        this.id = id;
    }

    public ArrayList<AbstractItemModel> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<AbstractItemModel> children) {
        this.children = children;
    }

    public Service getContentDirectoryService() {
        return contentDirectoryService;
    }

    public void setContentDirectoryService(Service contentDirectoryService) {
        this.contentDirectoryService = contentDirectoryService;
    }

    protected DIDLObject.Property findProperty(String searchTerm, DIDLObject item) {
        List<DIDLObject.Property> props = item.getProperties();
        for (DIDLObject.Property property : props) {
            if (property.getDescriptorName().toLowerCase().contains(searchTerm.toLowerCase())) {
                return property;
            }
        }
        return null;
    }


    public String getId() {
        return id;
    }

    public byte[] getRawIconData() {
        return rawIconData;
    }

    public void setRawIconData(byte[] rawIconData) {
        this.rawIconData = rawIconData;
    }

    public abstract boolean browseChildren(ServiceController serviceController);

    public abstract String getDescription();

    public abstract URL getIconUrl();

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public abstract String toString();

}
