/*
 * JetStream ML
 * DeviceNavController.java
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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.fourthline.cling.support.model.Res;

import java.util.ArrayList;

import obyriasura.jetstreamml.R;
import obyriasura.jetstreamml.helpers.Constants;
import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.item.DeviceModel;
import obyriasura.jetstreamml.models.item.FolderModel;
import obyriasura.jetstreamml.models.item.ItemModel;
import obyriasura.jetstreamml.models.service.ServiceManager;

/**
 * Main Activity controller class.
 */
public class DeviceNavController extends Activity implements ListViewController.FragmentEventListener, ServiceManager.ControlPointListener {

    private ServiceManager serviceManager;
    private Activity activity;
    /**
     * Animated spinning progress widget.
     */
    private ProgressBar mLoadingSpinner;
    private BroadcastReceiver connectivityChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean isLanConnected = false;

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiStatus = cm.getNetworkInfo(1);

            if ((wifiStatus != null && (wifiStatus.getState() == NetworkInfo.State.CONNECTED || wifiStatus.getState() == NetworkInfo.State.CONNECTING)))
                isLanConnected = true;

            // Check for ethernet connection.
            if (!isLanConnected) {
                NetworkInfo ethStatus = cm.getNetworkInfo(9);
                if (ethStatus != null && (ethStatus.getState() == NetworkInfo.State.CONNECTED || ethStatus.getState() == NetworkInfo.State.CONNECTING))
                    isLanConnected = true;
            }

            if (isLanConnected) {
                if (serviceManager == null) {
                    if ((serviceManager = ServiceManager.startUpnpService(getActivity())) != null) {
                        makePopupWithMessage(getString(R.string.scanning));
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        return;
                    }
                    makePopupWithMessage("Service Failed to Start.");
                    finish();
                    return;
                } else {
                    serviceManager.scanForNewServices();
                    return;
                }
            } else {
                if (serviceManager != null) {
                    serviceManager.stopService();
                    serviceManager = null;
                    FragmentManager fm = getFragmentManager();
                    // empty the fragment list
                    ((ListViewController) fm.findFragmentByTag(Constants.DEVICE_LIST_TAG)).setAdapter(new ArrayList<AbstractItemModel>());
                    fm.popBackStack(Constants.DEVICE_LIST_TAG, 0);
                }
                Toast.makeText(context, "This App Requires Local Network Connectivity", Toast.LENGTH_LONG).show();
            }
        }
    };
    private boolean isBrowseInProgress = false;

    public Activity getActivity() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        setContentView(R.layout.root_fragment_container);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.spin_loader);
        mLoadingSpinner.setVisibility(View.VISIBLE);

        if ((serviceManager = ServiceManager.startUpnpService(this)) == null) return;

        // Listen for connectivity changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChange, filter);

        ListViewController devList = null;
        // attach fragment to main view to show devices available on the network.
        if (savedInstanceState != null) {
            return;
        }

        //todo finish settings.
        //SharedPreferences settings = getPreferences(MODE_PRIVATE);

        // Create the first frag if not returning from saved state.

        devList = new ListViewController();
        // add the root view to the stack.
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.add(R.id.fragment_holder, devList, Constants.DEVICE_LIST_TAG);
        trans.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_view, menu);
        /*MenuItem item = menu.findItem(R.id.action_settings);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });*/
        MenuItem item = menu.findItem(R.id.rescan_button);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        item = menu.findItem(R.id.toggle_service);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == R.id.rescan_button) {
            if (serviceManager == null)
                return false;
            makePopupWithMessage(getString(R.string.scanning));
            serviceManager.scanForNewServices();
            return true;
        }

        if (id == R.id.toggle_service) {
            if (isChangingConfigurations()) return true;
            boolean flag;
            if (serviceManager != null) {
                flag = serviceManager.stopService();
                serviceManager = null;
            } else {
                flag = (serviceManager = ServiceManager.startUpnpService(this)) != null;
            }
            return flag;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        Log.d(getString(R.string.app_name), "onDestroy");

        // unbind to stop leaking, service stays active and is rebound onCreate
        serviceManager.unBindService();
        unregisterReceiver(connectivityChange);

        //Tear down upnp service completely when killing app.
        if (isFinishing())
            if (serviceManager.stopService())
                serviceManager = null;

        super.onDestroy();
    }

    // Fragment listener method, The fragment will call this to update the view.
    @Override
    public void onItemSelectListener(Object selectedItem) {
        if (!isBrowseInProgress) {// todo get reference to the browse thread and cancel it.
            // Play/show the file with and app on the device
            if (selectedItem instanceof ItemModel) {
                // create intent to system to handle file uri
                try {
                    ItemModel item = (ItemModel) selectedItem;
                    Res res = item.getItem().getFirstResource();
                    if (res == null)
                        return;
                    Uri uri = Uri.parse(res.getValue());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    if (type == null) {
                        type = res.getProtocolInfo().getContentFormat();
                    }
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, type);
                    startActivity(intent);
                } catch (NullPointerException ex) {
                    makePopupWithMessage(getString(R.string.info_play_failed));
                } catch (ActivityNotFoundException ex) {
                    makePopupWithMessage(getString(R.string.info_handler_not_available));
                }
                return;
            } else {

                mLoadingSpinner.setVisibility(View.VISIBLE);
                isBrowseInProgress = true;

                if (selectedItem instanceof DeviceModel) {
                    final DeviceModel deviceModel = (DeviceModel) selectedItem;
                    if (deviceModel.getDevice().isFullyHydrated()) {
                        if (!deviceModel.browseChildren(serviceManager)) {
                            makePopupWithMessage(getString(R.string.cannot_browse));
                            isBrowseInProgress = false;
                        }
                    } else {
                        makePopupWithMessage(getString(R.string.device_still_registering));
                    }
                } else if (selectedItem instanceof FolderModel) {
                    final FolderModel folderModel = (FolderModel) selectedItem;
                    if (folderModel.getFolder() != null) {
                        if (!folderModel.browseChildren(serviceManager)) {
                            makePopupWithMessage(getString(R.string.cannot_browse));
                            isBrowseInProgress = false;
                        }
                    }
                } else {
                    mLoadingSpinner.setVisibility(View.GONE);
                    isBrowseInProgress = false;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // block any action while a browse is in progress.
        // todo prolly can cancel browse action with thread ref
        if (!isBrowseInProgress)
            super.onBackPressed();
    }

    /**
     * Simple Toast message wrapper.
     *
     * @param string the string to display
     */
    public void makePopupWithMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    /**
     * Create and replace the current view fragment with this one,
     * putting the current onto the backstack.
     *
     * @param parentItem AbstractItemModel parent of each item in list.
     * @param tag        optional string to tag the fragment.
     */
    private void createNewListFragment(AbstractItemModel parentItem, String tag) {
        ListViewController df = new ListViewController();
        df.setParentItem(parentItem);
        df.setAdapter(parentItem.getChildren());
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_holder, df, tag);
        trans.addToBackStack(null);
        trans.commit();
    }

    /**
     * Update the device list fragment. This fragment is generally the starting point of the activity's view.
     */
    private void updateDeviceListFragment() {
        // Update the device list fragment.
        ListViewController df = (ListViewController) this.getFragmentManager().findFragmentByTag(Constants.DEVICE_LIST_TAG);
        df.setAdapter(serviceManager.getDevicesList());
    }

    /* Start of callback methods -- notifications are coming from different threads
     * so any view changes must be run on ui thread. */
    @Override
    public void devicesChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDeviceListFragment();
                mLoadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    /* End of callback methods */

    @Override
    public void browseComplete(final AbstractItemModel item) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isBrowseInProgress = false;
                createNewListFragment(item, null);
                mLoadingSpinner.setVisibility(View.GONE);
            }
        });
    }
}
