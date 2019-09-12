package com.airoha.android.spp.headset.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airoha.android.spp.headset.BlockModels.MarketNames;
import com.airoha.android.spp.headset.BuildConfig;
import com.airoha.android.spp.headset.R;
import com.airoha.android.spp.headset.service.ConnService;

import java.util.Locale;
import java.util.Set;

/**
 * 進入程式的主畫面<br>
 * 選擇裝置的列表<br>
 * 及接收連接裝置的receiver<br>
 *
 * @author easyapp_jim
 */
public class MainActivity extends FragmentActivity {

    private static final String REQUIRED_PACKAGE = "com.google.android.tts";
    private static final String REQUIRED_VERSION = "3816";
    private static final int REQUIRED_VERSION_CODE = 210308160; // guess from 3816, not sure about the correct value

    // need to be shut down after checking
    private TextToSpeech myTTS;

    //status check code
    private final int MY_DATA_CHECK_CODE = 2;
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothDevice mBDevice = null;

    /**
     * smart phone bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * Device listview
     */
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Set<BluetoothDevice> pairedDevices;
    // Stops scanning after 12 seconds.
    private static final long SCAN_PERIOD = 12000;

    // 2016.09.07 Daniel, Mantis#8142, block for Meizu M1 Note
    private static final long DELAY_CLICK = 2000;

    /**
     * Device info
     */
    private String address_extra;

    private boolean mScanning;
    private boolean mConnecting;
    private Handler mHandler;
    private AlertDialog mBTSettingDialog;
    private AlertDialog mTtsInstallDialog;
    private ListView mPairedListView;

    private void startService(String address){
        Intent intent = new Intent(this, ConnService.class);
        intent.setAction(ConnService.ACTION_START_CONN);
        intent.putExtra(ConnService.EXTRA_MAC_ADDR, address);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        getActionBar().setTitle(getString(R.string.title_select));
        setContentView(R.layout.activity_main);

        initDialogBTSettings();
        initDialogInstallTTSData();
        registerBTIntentFilters();
        checkPackage();
    }

    private void checkTTSLanguage(){
        initTTS();
    }


    private void checkPackage() {
        if(!isPackageExisted(REQUIRED_PACKAGE)){
            Log.d("Checking package", "not installed");

            showDialogInstaller();
            return;
        }else {
            checkTTSLanguage();
            checkTTS();
        }
    }

    private void checkTTS(){
        //check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    private void initTTS(){
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Locale defaultLocale = Locale.getDefault();

                    int languageStatus = myTTS.isLanguageAvailable(defaultLocale);

                    // 2016.09.07 Daniel, Mantis#8157, guide to install language pack
                    if(TextToSpeech.LANG_MISSING_DATA == languageStatus){
                        mTtsInstallDialog.show();
                    }
                } else if (status == TextToSpeech.ERROR) {

                }

                // no longer need it
                try{
                    myTTS.shutdown();
                }catch (Exception e){

                }

            }
        }, REQUIRED_PACKAGE);
    }

    private void installFromMarket() {
        Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("market://details?id=" + REQUIRED_PACKAGE));
        startActivity(goToMarket);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (null == mBluetoothAdapter) {
            Toast.makeText(this, "Bluetooth module not found",
                    Toast.LENGTH_LONG).show();
            this.finish();
        }

        if(mBTSettingDialog.isShowing()){
            mBTSettingDialog.dismiss();
        }

        showDilaogWhenNotConnected();

        initial();
        modifyConStateUI(false);

        requestReadContactsPhoneStatePermission();
        requestExternalStoragePermission();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ConnService.class);
        stopService(intent);
    }

    private void showDilaogWhenNotConnected() {
        if (isNeedUserGoToSettings()) {
            // 2016.08.23 Daniel, Mantis#7920, LG G5 need to enable by its bt settings
            // 2016.08.24 Daniel, let Android Phone take care of the profile connections
            mBTSettingDialog.show();
        }
    }

    private boolean isNeedUserGoToSettings() {
//        return !mBluetoothAdapter.isEnabled() ||
//                mBluetoothAdapter.getBondedDevices().size() == 0 ||
//                mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)!= BluetoothProfile.STATE_CONNECTED ||
//                mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) != BluetoothProfile.STATE_CONNECTED;
        return !mBluetoothAdapter.isEnabled() ||  mBluetoothAdapter.getBondedDevices().size() == 0;
    }

    private void initial() {

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        mPairedListView = (ListView) findViewById(R.id.list_devices);
        mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
        mPairedListView.setOnItemClickListener(mDeviceClickListener);
        updatePairedDeviceList();

        TextView textViewAppVer = (TextView)findViewById(R.id.tvAppVer);
        textViewAppVer.setText(BuildConfig.VERSION_NAME);
    }

    /**
     * Check device name here to see if to add to the list
     */
    private void updatePairedDeviceList() {
        // Remove all element from the list
        mPairedDevicesArrayAdapter.clear();
        // Get a set of currently paired devices
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String addr = device.getAddress();

                String name = mBluetoothAdapter.getRemoteDevice(addr).getName();

                // 2017.05.03, Daniel: Sony SBH56 is restricted to connectSpp to AIROHA -- start
                if(device.getName().equals(MarketNames.SBH56))
                    continue;
                // 2017.05.03, Daniel: Sony SBH56 is restricted to connectSpp to AIROHA -- end

                mPairedDevicesArrayAdapter.add(name + "\n"
                        +addr);
            }
        } else {
            String noDevices = getString(R.string.no_device);
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private void registerBTIntentFilters() {
        this.registerReceiver(mBTStateReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        this.registerReceiver(mBTStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        this.registerReceiver(mBTStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
        this.registerReceiver(mBTStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        this.registerReceiver(mBTStateReceiver, new IntentFilter(ConnService.ACTION_CONN_SUCCESS));
        this.registerReceiver(mBTStateReceiver, new IntentFilter(ConnService.ACTION_CONN_FAIL));
    }

    /**
     * Start device discover with the BluetoothAdapter
     **/
    private void doDiscovery() {
        updatePairedDeviceList();
        // Indicate scanning in the title
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.cancelDiscovery();
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
        //showRefreshAnimation();
        mScanning = true;
        invalidateOptionsMenu();
    }

    private void cancelDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
        mScanning = false;
        invalidateOptionsMenu();
    }

    // The on-click listener for all devices in the ListViews
    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // 2016.09.02 Daniel, Mantis#8139: 會與SPP建立連線，HFP+A2DP確無回連成功
            if(isNeedUserGoToSettings()){
                // 2016.08.24 Daniel, let Android Phone take care of the profile connections
                mBTSettingDialog.show();
                return;
            }

//            if(mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)!= BluetoothProfile.STATE_CONNECTED ||
//                mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) != BluetoothProfile.STATE_CONNECTED){
//                mBTSettingDialog.show();
//                return;
//            }

            // 2018.08.06 Daniel: don't check HFP
            if(mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)!= BluetoothProfile.STATE_CONNECTED) {
                mBTSettingDialog.show();
                return;
            }

            // 2016.08.26 Daniel, Mantis#8032, check if BT is enabled before doing stuff...
            if(mConnecting)
                return;

            // Cancel discovery because it's costly and we're about to connectSpp
            cancelDiscovery();

            String strNoFound = getString(R.string.no_device);
            if (!((TextView) v).getText().toString().equals(strNoFound)) {
                // Get the device MAC address, which is the last 17 chars in the
                Log.d("BT device item", "clicked");
                modifyConStateUI(true);
                String info = ((TextView) v).getText().toString();
                final String address = info.substring(info.length() - 17);
                address_extra = address;

                mBDevice = mBluetoothAdapter.getRemoteDevice(address);

                // 2016.09.07 Daniel, Mantis#8142, block for Meizu M1 Note
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService(address);
                    }
                }, DELAY_CLICK);

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuRefresh = menu.findItem(R.id.menu_refresh);

        if (!mScanning && !mConnecting) {
            menuRefresh.setActionView(null);
            return true;
        }

        if(!mScanning && mConnecting) {
            menuRefresh.setActionView(
                    R.layout.actionbar_indeterminate_progress);
            return true;
        }

        if(mScanning){
            menuRefresh.setActionView(
                    R.layout.actionbar_indeterminate_progress);
            return true;
        }

        return true;
    }


    private void modifyConStateUI(boolean isConnecting){
        mConnecting = isConnecting;
        invalidateOptionsMenu();

        // 2016.09.07 Daniel, prevent multiple clicks from user
        if(isConnecting){
            mPairedListView.setEnabled(false);
        }else {
            mPairedListView.setEnabled(true);
        }
    }



    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        // setup TTS
        if(requestCode == MY_DATA_CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final BroadcastReceiver mBTStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                updateFoundDeviceList(device);
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                // Do something if disconnected
                Toast.makeText(MainActivity.this, "BT CHECKED",
                        Toast.LENGTH_SHORT).show();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
            }

            if(action.equals(ConnService.ACTION_CONN_SUCCESS)){

                Toast.makeText(MainActivity.this, getString(R.string.connected), Toast.LENGTH_SHORT).show();

                startFragmentActivity();

                modifyConStateUI(false);
            }

            if(action.equals(ConnService.ACTION_CONN_FAIL)){
                Toast.makeText(MainActivity.this,
                        getString(R.string.actMain_msg_device_connect_fail),
                        Toast.LENGTH_SHORT).show();

                modifyConStateUI(false);
            }
        }
    };

    private void startFragmentActivity(){
        Intent actintent = new Intent();
        actintent.setClass(this, BluetoothActivity.class);
        //actintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); // 2016.09.30 Daniel, fixing bug

        startActivity(actintent);
    }

    /**
     * Check device name here to see if to add to the list
     * @param device
     */
    private void updateFoundDeviceList(BluetoothDevice device) {
        // If it's already paired, skip it, because it's been listed
        // already
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            String strNoFound = getIntent().getStringExtra(
                    "no_devices_found");
            if (strNoFound == null)
                strNoFound = getString(R.string.no_device);

            if (mPairedDevicesArrayAdapter.getItem(0)
                    .equals(strNoFound)) {
                mPairedDevicesArrayAdapter.remove(strNoFound);
            }

            // 2017.10.06, Daniel: Moto X bug
            if(device.getName() == null)
                return;

            // 2017.05.03, Daniel: Sony SBH56 is restricted to connectSpp to AIROHA -- start
            if(device.getName().equals(MarketNames.SBH56))
                return;
            // 2017.05.03, Daniel: Sony SBH56 is restricted to connectSpp to AIROHA -- end
            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }
    }

    @Override
    protected void onDestroy() {
        closeConn();
        try {
            unregisterReceiver(mBTStateReceiver);
        }catch (Exception e){
            // don't care, Android problem
        }

        super.onDestroy();
    }

    private void closeConn(){
        Intent intent = new Intent(ConnService.ACTION_DISCONNECT);
        sendBroadcast(intent);
    }

    private ProgressDialog mProgressDialog;

//    private void saveConnectedBTAddr() {
//        // store the name to the SharedPreference
//        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
//
//        SharedPreferences.Editor editor = preference.edit();
//        editor.putString("bt_addr", address_extra);
//        editor.apply();
//    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);

            if(info.versionCode <REQUIRED_VERSION_CODE){
                return false;
            }

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void showDialogInstaller() {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setMessage("Install or Update from default App Market")
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Install required package")
                .setNeutralButton(
                        getResources()
                                .getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                installFromMarket();
                                finish();
                            }
                        })
                .setPositiveButton(
                        getResources().getString(R.string.cancel),
                        null).setCancelable(false).create();
        if (!ad.isShowing()) {
            ad.show();
        } else {
            ad.cancel();
        }
    }

    private void initDialogBTSettings() {
        mBTSettingDialog = new AlertDialog.Builder(this)
                .setMessage("Please enable BT/pair/connectSpp  via System Setting")
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Enable BT")
                .setNeutralButton(
                        getResources()
                                .getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent intentOpenBluetoothSettings = new Intent();
                                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                                startActivity(intentOpenBluetoothSettings);
                            }
                        })
                .setPositiveButton(
                        getResources().getString(R.string.cancel),
                        null).setCancelable(false).create();
    }

    // 2016.09.07 Daniel, Mantis#8157, guide to install language pack
    private void initDialogInstallTTSData(){
        mTtsInstallDialog = new AlertDialog.Builder(this)
                .setMessage("Confirm to go to TTS Lang. Pack Setting to download the language pack of your current locale")
                .setIcon(R.drawable.ic_launcher)
                .setTitle("TTS Language Pack Not Installed")
                .setNeutralButton(
                        getResources()
                                .getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //no data - install it now
                                Intent installTTSIntent = new Intent();
                                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installTTSIntent);
                            }
                        })
                .setPositiveButton(
                        getResources().getString(R.string.cancel),
                        null).setCancelable(false).create();
    }


    private void requestReadContactsPhoneStatePermission(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_CONTACTS},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_PHONE_STATE},
                    0
            );
        }
    }

    private void requestExternalStoragePermission(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }
    }

}
