/*
 * JetStream ML
 * ItemModel.java
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

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.helpers.Constants;
import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.service.ServiceManager;

/**
 * A Concrete class describing a media item,
 * or playable/viewable item.
 */
public class ItemModel extends AbstractChildModel {

    private Item item;

    public ItemModel(DIDLObject deviceItem, Service service, AbstractItemModel parent) {
        super(deviceItem.getId(), service, parent);
        if (deviceItem instanceof Item) {
            this.item = (Item) deviceItem;
        }
        setItemType(ItemTypeEnum.TYPE_ITEM);
        setIconUrl(null);
    }

    public Item getItem() {
        return item;
    }

    @Override
    public void setIconUrl(URL iconUrl) {
        if (iconUrl == null) {
            DIDLObject.Property iconArt = findProperty(ALBUM_ART_PROPERTY, item);
            if (iconArt != null) {
                try {
                    super.setIconUrl(new URL(iconArt.toString()));
                } catch (MalformedURLException e) { /* do nothing */}
            }
        } else {
            super.setIconUrl(iconUrl);
        }
    }

    @Override
    public boolean browseChildren(ServiceManager serviceManager) {
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
        if (descriptionString.length() > Constants.MAX_DESCRIPTION_LENGTH) {
            descriptionString.delete(Constants.MAX_DESCRIPTION_LENGTH, descriptionString.length());
        }
        descriptionString.append(" ...");
        return descriptionString.toString();
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
