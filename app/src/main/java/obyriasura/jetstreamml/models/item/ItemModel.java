package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * Created by obyri on 4/4/15.
 */
public class ItemModel extends AbstractChildModel {

    private Item item;

    public ItemModel(DIDLObject deviceItem, Service service, AbstractItemModel parent) {
        super(deviceItem.getId(), service, parent);
        if (deviceItem instanceof Item) {
            this.item = (Item) deviceItem;
        }
        addIconUrl();
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean browseChildren(ServiceController serviceController) {
        return false;
    }

    @Override
    public String getDescription() {
        StringBuilder descriptionString = new StringBuilder();
        DIDLObject.Property longDesc = findProperty(LONG_DESCRIPTION_PROPERTY, item);
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
        DIDLObject.Property iconArt = findProperty(ALBUM_ART_PROPERTY, item);
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

        ItemModel that = (ItemModel) o;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public String toString() {
        return item.getTitle();
    }
}
