/*
 * JetStream ML
 * RetrieveRemoteBitmap.java
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URLConnection;

import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.listAdapter.RowViewModel;

/**
 * Class description.
 */
public class RetrieveRemoteBitmapTask extends AsyncTask<RowViewModel, Void, Bitmap> {
    /**
     * Async Task to retrieve a remote bitmap file from a ItemModel.
     */
    private final WeakReference<ImageView> iconViewRef;

    RetrieveRemoteBitmapTask(ImageView iconView) {
        iconViewRef = new WeakReference<>(iconView);
    }

    @Override
    protected Bitmap doInBackground(RowViewModel... params) {
        InputStream is;
        try {
            AbstractItemModel itemModel = params[0].getItemModel();
            URLConnection urlConnection = itemModel.getIconUrl().openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            byte[] buff = new byte[16 * 1024];
            int readLen;
            while ((readLen = is.read(buff, 0, buff.length)) != -1) {
                byteStream.write(buff, 0, readLen);
            }
            byte[] rawByteData = byteStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTargetDensity = 20;
            if (isCancelled()) {
                is.close();
                return null;
            }
            Bitmap bm = getResizedBitmap(BitmapFactory.decodeByteArray(rawByteData, 0, rawByteData.length, options), 120, 120);
            params[0].setIconImage(bm);
            is.close();
            return bm;
        } catch (IOException e) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        if (scaleHeight >= 1 || scaleWidth >= 1) return bm;
        // preserve aspect with same smallest scale factor for both X&Y.
        float scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // No point continuing here.
        if (isCancelled()) return;
        if (iconViewRef.get() != null && bitmap != null) {
            final ImageView iconViewActual = iconViewRef.get();
            if (iconViewActual != null) {
                iconViewActual.setImageBitmap(bitmap);
            }
        }
    }
}
