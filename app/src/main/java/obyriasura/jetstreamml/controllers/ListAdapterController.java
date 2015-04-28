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
    List<ViewWrapper> viewsOnDisplay = new ArrayList<>();

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
        ViewWrapper viewWrapper = null;
        if (convertView == null) {
            rowView = mInflater.inflate(mResource, parent, false);
        } else {
            rowView = convertView;
            if (hasRowView(convertView)) {
                viewWrapper = getMatchingView(convertView);
                viewWrapper.cancelTask();
            }
        }

        titleText = (TextView) rowView.findViewById(R.id.text1);
        descText = (TextView) rowView.findViewById(R.id.text2);
        iconView = (ImageView) rowView.findViewById(R.id.image_icon);

        try {
            RowViewModel rowViewItem = (RowViewModel) getItem(position);
            titleText.setText(rowViewItem.toString());
            descText.setText(rowViewItem.getDescriptionText());

            // lookup rowView has icon bitmap.
            boolean isIconSet = false;
            if (rowViewItem.getIconImage() != null) {
                iconView.setImageBitmap(rowViewItem.getIconImage());
                isIconSet = true;
            }

            // set default and try remote lookup icon.
            if (!isIconSet) {
                if (viewWrapper != null && viewWrapper.isTaskCancelled()) {
                    viewWrapper.resetBackgroundTask(iconView);
                } else if (viewWrapper == null) {
                    viewWrapper = new ViewWrapper(iconView, rowView);
                }

                if (rowViewItem.getType().equals(ItemTypeEnum.TYPE_DEVICE)) {
                    iconView.setImageResource(R.mipmap.ic_device);
                    viewWrapper.executeTask(rowViewItem);

                } else if (rowViewItem.getType() == ItemTypeEnum.TYPE_FOLDER) {
                    iconView.setImageResource(R.mipmap.ic_folder);
                    viewWrapper.executeTask(rowViewItem);

                } else {
                    // Movies/Videos have different icon.
                    if (((ItemModel) rowViewItem.getItemModel()).getItem().getFirstResource().getProtocolInfo().getContentFormat().contains("video")) {
                        iconView.setImageResource(R.mipmap.ic_movie);
                        viewWrapper.executeTask(rowViewItem);
                    } else {
                        iconView.setImageResource(R.mipmap.ic_unknown);
                    }
                    //todo add more icons music/images etc...
                }
            }

            if (!hasRowView(rowView)) viewsOnDisplay.add(viewWrapper);
        } catch (ClassCastException ex) {
            Log.e("ArrayAdapter", "Incompatible or unknown types within the array");
        }
        return rowView;
    }

    private boolean hasRowView(View view) {
        return view != null && getMatchingView(view) != null;
    }

    private ViewWrapper getMatchingView(View view) {
        try {
            if (view != null) {
                for (ViewWrapper viewMatch : viewsOnDisplay) {
                    if (viewMatch.viewRef.get() != null && view.equals(viewMatch.viewRef.get()))
                        return viewMatch;
                }
            }
        } catch (NullPointerException ex) {/*Do nothing*/}
        return null;
    }

    private class ViewWrapper {
        private WeakReference<View> viewRef;
        private RetrieveRemoteBitmapTask bitmapTask;
        private boolean taskCancelled = false;

        public ViewWrapper(ImageView imageView, View view) {
            if (imageView != null && view != null) {
                this.viewRef = new WeakReference<>(view);
                this.bitmapTask = new RetrieveRemoteBitmapTask(imageView);
                taskCancelled = false;
            }
        }

        public void executeTask(RowViewModel item) {
            this.bitmapTask.execute(item);
        }

        public boolean cancelTask() {
            taskCancelled = true;
            return bitmapTask.cancel(true);
        }

        public boolean isTaskCancelled() {
            return taskCancelled;
        }

        public void resetBackgroundTask(ImageView imageView) {
            if (imageView != null && taskCancelled) {
                bitmapTask = new RetrieveRemoteBitmapTask(imageView);
                taskCancelled = false;
            }
        }
    }
}