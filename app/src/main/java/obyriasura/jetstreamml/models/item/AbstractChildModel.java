/*
 * JetStream ML
 * AbstractChildModel.java
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

/**
 * A distinct abstract child class implementing a parent object, which define this as
 * a child-of, a device or folder.
 */
public abstract class AbstractChildModel extends AbstractItemModel {

    private AbstractItemModel parent;

    public AbstractChildModel(String id, Service contentDirectoryService, AbstractItemModel parent) {
        super(id);
        setContentDirectoryService(contentDirectoryService);
        this.parent = parent;
    }

    public AbstractItemModel getParent() {
        return parent;
    }

    public void setParent(AbstractItemModel parent) {
        this.parent = parent;
    }
}
