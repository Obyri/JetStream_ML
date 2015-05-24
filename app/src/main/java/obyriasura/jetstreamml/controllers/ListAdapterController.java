/*
 * JetStream ML
 * ButtonListAdapter.java
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

package obyriasura.jetstreamml.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import obyriasura.jetstreamml.R;
import obyriasura.jetstreamml.helpers.ItemTypeEnum;
import obyriasura.jetstreamml.models.item.ItemModel;
import obyriasura.jetstreamml.models.listAdapter.RowViewModel;

/**
 * Customised array adapter overriding getView to create a view with
 * an image on the left followed by two text fields, one above the other,
 * for Title and long description.
 *
 * @param <T>
 */
public class ListAdapterController<T> extends ArrayAdapter<T> {

    private final LayoutInflater mInflater;
    private final int mResource;

    TextView titleText;
    TextView descText;
    ImageView iconView;


    public ListAdapterController(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.mResource = resource;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        if (convertView == null) {
            rowView = mInflater.inflate(mResource, parent, false);
        } else {
            rowView = convertView;
        }

        titleText = (TextView) rowView.findViewById(R.id.text1);
        descText = (TextView) rowView.findViewById(R.id.text2);
        iconView = (ImageView) rowView.findViewById(R.id.image_icon);

        try {
            RowViewModel rowViewItem = (RowViewModel) getItem(position);
            titleText.setText(rowViewItem.toString());
            descText.setText(rowViewItem.getDescriptionText());

            // set default and try remote lookup icon.
                if (rowViewItem.getType().equals(ItemTypeEnum.TYPE_DEVICE)) {
                    //iconView.setImageResource(R.mipmap.ic_device);
                    loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_device), iconView);

                } else if (rowViewItem.getType() == ItemTypeEnum.TYPE_FOLDER) {
                    loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_folder), iconView);

                } else if (rowViewItem.getType() == ItemTypeEnum.TYPE_ITEM) {
                    // Movies/Videos etc have different icon.
                    if (((ItemModel) rowViewItem.getItemModel()).getItem().getFirstResource().getProtocolInfo().getContentFormat().contains("video")) {
                        loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_video), iconView);
                    } else if (((ItemModel) rowViewItem.getItemModel()).getItem().getFirstResource().getProtocolInfo().getContentFormat().contains("audio")) {
                        loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_music), iconView);
                    } else if (((ItemModel) rowViewItem.getItemModel()).getItem().getFirstResource().getProtocolInfo().getContentFormat().contains("image")) {
                        loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_image), iconView);
                    } else {
                        loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_unknown), iconView);
                    }
                } else {
                    loadBitmap(rowViewItem, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_unknown), iconView);
                }

        } catch (ClassCastException ex) {
            Log.e("ArrayAdapter", "Incompatible or unknown types within the array");
        }
        return rowView;
    }

    private void loadBitmap(RowViewModel rowViewModel,Bitmap placeholderBitmap, ImageView imageView) {
        if (rowViewModel == null) return;
        if (rowViewModel.getIconImage() != null) {
            imageView.setImageBitmap(rowViewModel.getIconImage());
            return;
        }

        if (cancelTask(rowViewModel, imageView)) {
            RetrieveRemoteBitmapTask task = new RetrieveRemoteBitmapTask(imageView);
            IconViewAsyncDrawable iconViewAsyncDrawable = new IconViewAsyncDrawable(getResources(), placeholderBitmap, task);
            imageView.setImageDrawable(iconViewAsyncDrawable);
            task.execute(rowViewModel);
        }
    }

    private boolean cancelTask(RowViewModel rowViewModel, ImageView imageView) {
        RetrieveRemoteBitmapTask task = getBitmapTaskFromImageView(imageView);
        if (task != null ) {
            RowViewModel taskRowViewModel = task.getRowViewModel();
            if (taskRowViewModel == null || taskRowViewModel != rowViewModel) {
                task.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static RetrieveRemoteBitmapTask getBitmapTaskFromImageView(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof IconViewAsyncDrawable) {
                IconViewAsyncDrawable iconViewAsyncDrawable =  (IconViewAsyncDrawable) drawable;
                return iconViewAsyncDrawable.getRemoteBitmapTask();
            }
        }
        return null;
    }

    private Resources getResources() {
        return getContext().getResources();
    }


    /**
     * class wrapper of the async task to retrieve the thumbnail bitmap for the source.
     * This class provides the reference to the task thread so it may be cancelled
     */
    private static class IconViewAsyncDrawable extends BitmapDrawable {
        private WeakReference<RetrieveRemoteBitmapTask> mBitmapTask;

        public IconViewAsyncDrawable(Resources resources, Bitmap bitmap, RetrieveRemoteBitmapTask bitmapTask) {
            super(resources, bitmap);
            mBitmapTask = new WeakReference<>(bitmapTask);
        }

        public RetrieveRemoteBitmapTask getRemoteBitmapTask() {
            return mBitmapTask.get();
        }
    }
}