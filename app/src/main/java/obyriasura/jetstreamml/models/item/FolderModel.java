/*
 * JetStream ML
 * FolderModel.java
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
import org.fourthline.cling.support.model.container.Container;

import java.net.MalformedURLException;
import java.net.URL;

import obyriasura.jetstreamml.helpers.Constants;
import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.service.ServiceController;

/**
 * A Concrete model describing a folder item,
 * or an item which has children and is not a device.
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
        setItemType(ItemTypeEnum.TYPE_FOLDER);
        setIconUrl(null);
    }

    public Container getFolder() {
        return folder;
    }

    @Override
    public void setIconUrl(URL iconUrl) {
        if (iconUrl == null) {
            DIDLObject.Property iconArt = findProperty(ALBUM_ART_PROPERTY, folder);
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
