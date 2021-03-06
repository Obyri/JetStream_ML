/*
 * JetStream ML
 * RowViewModel.java
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

package obyriasura.jetstreamml.models.listAdapter;

import android.graphics.Bitmap;

import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.item.AbstractItemModel;

/**
 * Class description.
 */
public class RowViewModel {

    private String mTitleText;
    private String mDescriptionText;
    private Bitmap mIconImage;
    private ItemTypeEnum mType;
    private AbstractItemModel mItemModel;

    public RowViewModel(AbstractItemModel itemModel) {
        this(null, null, null, itemModel, ItemTypeEnum.TYPE_UNKNOWN);
    }

    public RowViewModel(String title, String desc, AbstractItemModel itemModel) {
        this(title, desc, null, itemModel, ItemTypeEnum.TYPE_UNKNOWN);
    }

    public RowViewModel(String title, String desc, Bitmap bitmap, AbstractItemModel itemModel) {
        this(title, desc, bitmap, itemModel, ItemTypeEnum.TYPE_UNKNOWN);
    }

    public RowViewModel(String title, String desc, AbstractItemModel itemModel, ItemTypeEnum type) {
        this(title, desc, null, itemModel, type);
    }

    public RowViewModel(String title, String desc, Bitmap bitmap, AbstractItemModel itemModel, ItemTypeEnum type) {
        this.mTitleText = title != null ? title : "";
        this.mDescriptionText = desc != null ? desc : "";
        this.mIconImage = bitmap != null ? bitmap : null;
        this.mType = type;
        this.mItemModel = itemModel;
    }

    public String getDescriptionText() {
        return mDescriptionText;
    }

    public Bitmap getIconImage() {
        return mIconImage;
    }

    public void setIconImage(Bitmap iconImage) {
        this.mIconImage = iconImage;
    }

    public ItemTypeEnum getType() {
        return mType;
    }

    public AbstractItemModel getItemModel() {
        return mItemModel;
    }

    @Override
    public String toString() {
        return mTitleText;
    }
}
