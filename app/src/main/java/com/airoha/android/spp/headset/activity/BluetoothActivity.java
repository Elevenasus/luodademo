package com.airoha.android.spp.headset.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.airoha.android.spp.headset.R;
import com.airoha.android.spp.headset.fragment.BaseFragment;
import com.airoha.android.spp.headset.fragment.FragmentContainer;
import com.airoha.android.spp.headset.service.ConnService;


/**
 * Communication modes: byte-stream mode
 */
// unused
public final class BluetoothActivity extends FragmentActivity {

    private static final String TAG = BluetoothActivity.class.getSimpleName();
    private FragmentContainer mFragmentContainer;
    private ProgressDialog mDialogReconnect;

    /**
     * Page construction
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mFragmentContainer = new FragmentContainer();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.child_container, mFragmentContainer, "main").commitAllowingStateLoss();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_SPP_DISCONNECTED);
        intentFilter.addAction(ConnService.ACTION_CONN_SUCCESS);
        intentFilter.addAction(ConnService.ACTION_CONN_FAIL);
        this.registerReceiver(mBTStateReceiver, intentFilter);

        Log.d(TAG, "done onCreate");
    }


    @Override
    protected void onDestroy() {
        Log.d("BluetoothActivity", "onDestroy");

        try{
            unregisterReceiver(mBTStateReceiver);

        }catch (Exception e){
            // don't care, Android problem
        }

        super.onDestroy();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment f = (BaseFragment) getSupportFragmentManager().findFragmentByTag("main");
        f.onActivityResult(requestCode, resultCode, data);


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            showDialogWithDisconnect();
        }
    }

    /**
     * 詢問是否要斷開連接
     */
    public void showDialogWithDisconnect() {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.deviceDisconnect))
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Disconnect Device?")
                .setNeutralButton(
                        getResources()
                                .getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                finishBTActivity();
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

    private void finishBTActivity() {
        try{
            unregisterReceiver(mBTStateReceiver);

        }catch (Exception e){
            // don't care, Android problem
        }

        Intent intent = new Intent(ConnService.ACTION_DISCONNECT);
        sendBroadcast(intent);

        finish();
    }

    private final BroadcastReceiver mBTStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(ConnService.ACTION_SPP_DISCONNECTED)){
                synchronized (this) {
                    if(mDialogReconnect == null) {
                        showDialogForReconnecting();
                    }
                }
            } else if(action.equals(ConnService.ACTION_CONN_FAIL)){
                synchronized (this) {
                    finishBTActivity();
                }
            } else if(action.equals(ConnService.ACTION_CONN_SUCCESS)){
                synchronized (this) {
                    if (mDialogReconnect != null) {
                        mDialogReconnect.dismiss();
                        mDialogReconnect = null;
                        mFragmentContainer.refreshInfo();
                    }
                }
            }
        }
    };

    private void showDialogForReconnecting(){
        mDialogReconnect = new ProgressDialog(this);
        mDialogReconnect.setIcon(R.drawable.ic_launcher);
        mDialogReconnect.setTitle(getResources().getString(R.string.deviceDisconnected));
        mDialogReconnect.setMessage("Waiting for the auto-reconnection of device...");
        mDialogReconnect.setCancelable(false);
        mDialogReconnect.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishBTActivity();
                mDialogReconnect = null;
            }
        });
        if (!mDialogReconnect.isShowing()) {
            mDialogReconnect.show();
        }
    }
}