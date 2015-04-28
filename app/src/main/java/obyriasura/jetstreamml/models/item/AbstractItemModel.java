/*
 * JetStream ML
 * AbstractItemModel.java
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * A generic custom abstract class that wraps a DIDL object from the cling library.
 */
public abstract class AbstractItemModel {
    public static final String ALBUM_ART_PROPERTY = "albumArtURI";
    public static final String LONG_DESCRIPTION_PROPERTY = "longDescription";
    public static final String GENRE_PROPERTY = "genre"; //future use.

    private final String id;
    private Service mContentDirectoryService;
    private ArrayList<AbstractItemModel> mChildren = new ArrayList<>();
    private ItemTypeEnum mItemType;
    private URL mIconUrl;

    /**
     * Constructor.
     *
     * @param id upnp uid.
     */
    public AbstractItemModel(String id) {
        this.id = id;
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

    public Service getContentDirectoryService() {
        return mContentDirectoryService;
    }

    public void setContentDirectoryService(Service mContentDirectoryService) {
        this.mContentDirectoryService = mContentDirectoryService;
    }

    public ArrayList<AbstractItemModel> getChildren() {
        return mChildren;
    }

    public void setChildren(ArrayList<AbstractItemModel> mChildren) {
        this.mChildren = mChildren;
    }

    public ItemTypeEnum getItemType() {
        return this.mItemType;
    }

    protected void setItemType(ItemTypeEnum mItemType) {
        this.mItemType = mItemType;
    }

    public URL getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(URL iconUrl) {
        this.mIconUrl = iconUrl;
    }

    // Abstract Methods
    public abstract boolean browseChildren(ServiceController serviceController);

    public abstract String getDescription();

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public abstract String toString();

}
