package obyriasura.jetstreamml.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import java.util.List;

import obyriasura.jetstreamml.R;
import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.item.DeviceModel;
import obyriasura.jetstreamml.models.item.FolderModel;
import obyriasura.jetstreamml.models.item.ItemModel;

/**
 * Customised array adapter overriding getView to create a view with
 * an image on the left followed by two text fields, one above the other,
 * for Title and long description.
 *
 * @param <T>
 */
public class ButtonListAdapter<T> extends ArrayAdapter<T> {

    //private List<T> mObjects;
    private final LayoutInflater mInflater;
    private final int mResource;

    TextView titleText;
    TextView descText;
    ImageView iconView;


    public ButtonListAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        //this.mObjects = objects;
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

        // NO objects means not items, do empty rowView
        if (getCount() <= 0) {
            titleText.setText(R.string.no_items);
            iconView.setImageResource(R.mipmap.ic_unknown);
        } else {
            try {
                if (getItem(position) instanceof DeviceModel) {
                    final DeviceModel device = (DeviceModel) getItem(position);
                    titleText.setText(device.toString());
                    descText.setText(device.getDescription());

                    Bitmap bm;
                    if ((bm = fetchItemIconData(device)) != null) {
                        iconView.setImageBitmap(bm);
                    } else {
                        // try to fetch from server.
                        iconView.setImageResource(R.mipmap.ic_device);
                        fetchIconBitmapFromServer(device);
                    }

                } else if (getItem(position) instanceof FolderModel) {
                    FolderModel folder = (FolderModel) getItem(position);
                    titleText.setText(folder.toString());
                    descText.setText(folder.getDescription());
                    iconView.setImageResource(R.mipmap.ic_folder);
                    fetchIconBitmapFromServer(folder);

                } else {
                    ItemModel item = (ItemModel) getItem(position);
                    titleText.setText(item.toString());
                    descText.setText(item.getDescription());

                    // Movies/Videos have different icon.
                    if (item.getItem().getFirstResource().getProtocolInfo().getContentFormat().contains("video")) {
                        iconView.setImageResource(R.mipmap.ic_movie);
                        fetchIconBitmapFromServer(item);
                    } else {
                        iconView.setImageResource(R.mipmap.ic_unknown);
                    }
                    //todo add more icons music/images etc...

                }
            } catch (ClassCastException ex) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            }
        }
        return rowView;
    }

    private Bitmap fetchItemIconData(AbstractItemModel item) {
        if (item.getRawIconData() != null && item.getRawIconData().length != 0) {
            // raw icon data exists. try decode to bitmap.
            return BitmapFactory.decodeByteArray(item.getRawIconData(), 0, item.getRawIconData().length);
        }
        return null;
    }

    private void fetchIconBitmapFromServer(AbstractItemModel item) {
        // try retrieve the remote device icon bitmap.
        new RetrieveRemoteBitmapTask(iconView).execute(item);
    }

    /**
     * Async Task to retrieve a remote bitmap file from a ItemModel.
     * Calls getRemoteBitmap method.
     */
    private class RetrieveRemoteBitmapTask extends AsyncTask<AbstractItemModel, Void, Bitmap> {
        private final WeakReference<ImageView> iconViewRef;

        RetrieveRemoteBitmapTask(ImageView iconView) {
            iconViewRef = new WeakReference<>(iconView);
        }

        @Override
        protected Bitmap doInBackground(AbstractItemModel... params) {
            return getRemoteBitmap(params[0]);
        }

        private Bitmap getRemoteBitmap(AbstractItemModel item) {
            InputStream is;
            try {
                URLConnection urlConnection = item.getIconUrl().openConnection();
                is = new BufferedInputStream(urlConnection.getInputStream());
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                int readOff;
                byte[] buff = new byte[16 * 1024];

                while ((readOff = is.read(buff, 0, buff.length)) != -1) {
                    byteStream.write(buff);
                }
                byte[] rawByteData = byteStream.toByteArray();
                item.setRawIconData(rawByteData);
                Bitmap bm = BitmapFactory.decodeByteArray(rawByteData, 0, rawByteData.length);
                //Bitmap bm = BitmapFactory.decodeStream(is);
                is.close();
                return bm;
            } catch (IOException e) {
                return null;
            } catch (NullPointerException ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (iconViewRef != null && bitmap != null) {
                final ImageView iconViewActual = iconViewRef.get();
                if (iconViewActual != null) {
                    iconViewActual.setImageBitmap(bitmap);
                }
            }
        }


    }
}