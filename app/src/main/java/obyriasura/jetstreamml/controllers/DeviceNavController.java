package obyriasura.jetstreamml.controllers;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import obyriasura.jetstreamml.R;
import obyriasura.jetstreamml.helpers.Constants;
import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.item.DeviceModel;
import obyriasura.jetstreamml.models.item.FolderModel;
import obyriasura.jetstreamml.models.item.ItemModel;
import obyriasura.jetstreamml.models.service.ServiceController;

public class DeviceNavController extends Activity implements ListViewController.FragmentEventListener, ServiceController.ControlPointListener, FragmentManager.OnBackStackChangedListener {

    private ServiceController serviceController;

    /**
     * Animated spinning progress widget.
     */
    private ProgressBar mLoadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_fragment_container);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.spin_loader);
        mLoadingSpinner.setVisibility(View.VISIBLE);

        if (!startService()) return;

        ListViewController devList = null;
        // attach fragment to main view to show devices available on the network.
        if (savedInstanceState != null) {
            //devList = (ListViewController) this.getFragmentManager().findFragmentByTag("DeviceList");
            return;
        }

        //todo finish settings.
        //SharedPreferences settings = getPreferences(MODE_PRIVATE);

        // Create the first frag if not returning from saved state.
        if (devList == null) {
            devList = new ListViewController();
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        // add the root view to the stack.
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.add(R.id.fragment_holder, devList, Constants.DEVICE_LIST_TAG);
        trans.commit();
    }

    private boolean startService() {
        // Everything relies on the service
        try {
            serviceController = new ServiceController(this);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            makePopupWithMessage("Fatal Error: Service Failed to Start.");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_controller, menu);
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });
        item = menu.findItem(R.id.rescan_button);
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
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.rescan_button) {
            if (serviceController == null)
                return false;
            makePopupWithMessage(getString(R.string.scanning));
            serviceController.getControlPoint().getRegistry().removeAllRemoteDevices();
            serviceController.getControlPoint().search();
            return true;
        }

        if (id == R.id.toggle_service) {
            if (isChangingConfigurations()) return true;
            boolean flag;
            if (serviceController != null) {
                flag = serviceController.dispose();
                serviceController = null;
            } else {
                flag = startService();
            }
            return flag;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        Log.d(getString(R.string.app_name), "onDestroy");

        // unbind to stop leaking, from service stays active and is rebound onCreate
        serviceController.unBindService();
        // todo save instance of current fragment. for orientation change
        //Tear down upnp service completely when killing app.
        if (isFinishing())
            if (serviceController.dispose())
                serviceController = null;

        super.onDestroy();
    }

    // Fragment listener method, The fragment will call this to update the view.
    @Override
    public void onItemSelectListener(Object selectedItem) {
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
        }

        mLoadingSpinner.setVisibility(View.VISIBLE);

        if (selectedItem instanceof DeviceModel) {
            final DeviceModel deviceModel = (DeviceModel) selectedItem;
            if (deviceModel.getDevice().isFullyHydrated()) {
                if (!deviceModel.browseChildren(serviceController))
                    makePopupWithMessage(getString(R.string.cannot_browse));
            } else {
                makePopupWithMessage(getString(R.string.device_still_registering));
            }
        }

        if (selectedItem instanceof FolderModel) {
            final FolderModel folderModel = (FolderModel) selectedItem;
            if (folderModel.getFolder() != null) {
                if (!folderModel.browseChildren(serviceController))
                    makePopupWithMessage(getString(R.string.cannot_browse));
            }
        }
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
        ListViewController df = (ListViewController) this.getFragmentManager().findFragmentByTag("DeviceList");
        df.setAdapter(serviceController.getDevicesList());
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

    @Override
    public void browseComplete(final AbstractItemModel item) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createNewListFragment(item, null);
                mLoadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();
    }
    /* End of callback methods */
}
