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

import android.content.Context;
import android.graphics.Bitmap;

import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.item.AbstractItemModel;

/**
 * Class description.
 */
public class RowViewModel {

    private String mTitleText = new String();
    private String mDescriptionText = new String();
    private Bitmap mIconImage;
    private Context mContext;
    private ItemTypeEnum mType;
    private AbstractItemModel mItemModel;

    public RowViewModel(Context appContext, AbstractItemModel itemModel) {
        this(null, null, null, appContext, itemModel, ItemTypeEnum.TYPE_UNKNOWN);
    }

    public RowViewModel(String title, String desc, Context appContext, AbstractItemModel itemModel) {
        this(title, desc, null, appContext, itemModel, ItemTypeEnum.TYPE_UNKNOWN);
    }

    public RowViewModel(String title, String desc, Context appContext, AbstractItemModel itemModel, ItemTypeEnum type) {
        this(title, desc, null, appContext, itemModel, type);
    }

    public RowViewModel(String title, String desc, Bitmap bitmap, Context appContext, AbstractItemModel itemModel, ItemTypeEnum type) {
        this.mTitleText = title != null ? title : new String();
        this.mDescriptionText = desc != null ? desc : new String();
        this.mIconImage = bitmap != null ? bitmap : null;
        this.mContext = appContext;
        this.mType = type;
        this.mItemModel = itemModel;
    }

    public String getTitleText() {
        return mTitleText;
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
