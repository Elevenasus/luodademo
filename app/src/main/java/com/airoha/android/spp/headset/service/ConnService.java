package com.airoha.android.spp.headset.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.airoha.android.lib.fieldTest.OnAirohaAirDumpListener;
import com.airoha.android.lib.fieldTest.logger.AirDumpLog;
import com.airoha.android.lib.fieldTest.logger.AirDumpLogForDebug;
import com.airoha.android.lib.mmi.OnAirohaFollowerExistingListener;
import com.airoha.android.lib.mmi.OnAirohaMmiEventListener;
import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.OnAirohaOtaEventListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.TransportTarget;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.spp.headset.Intents.UiIntents;
import com.airoha.android.spp.headset.OTA.OtaActivity;
import com.airoha.android.spp.headset.R;
import com.airoha.android.spp.headset.activity.BluetoothActivity;
import com.airoha.android.spp.headset.bluetooth.AirohaEngine;
import com.airoha.android.spp.headset.phoneControl.IncomingCallPreProcessor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ConnService extends Service {
    public static final String ACTION_START_CONN = "ACTION_START_CONN";
    public static final String EXTRA_MAC_ADDR = "EXTRA_MAC_ADDR";

    public static final String ACTION_SEND = "ACTION_SEND";
    public static final String ACTION_SEND_QUEUED = "ACTION_SEND_QUEUED";
    public static final String EXTRA_CMD = "EXTRA_CMD";

    public static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";

    public static final String ACTION_CONN_SUCCESS = "ACTION_CONN_SUCCESS";
    public static final String ACTION_CONN_FAIL = "ACTION_CONN_FAIL";

	//2016.08.18 Daniel: Mantis#7882, on Nexus 5X, not sending ACL_DISCONNECTED to upper layer, this is a workaround
    public static final String ACTION_SPP_DISCONNECTED = "ACTION_SPP_DISCONNECTED";

    public static final String ACTION_FOLLOWER_DISCONNECTED = "ACTION_FOLLOWER_DISCONNECTED";

    public static final String ACTION_AT_TOGGLE = "ACTION_AT_TOGGLE";

    private static final String TAG = "ConnService";

    private ConnectThread mConnectThread;

    private String mBtAddr;

    // 2016.11.01 Daniel: should not be static
    private Context mCtx;

    // 2016.11.01 Daniel: should not be static
    private AirohaLink mAirohaLink = null;

    private IncomingCallPreProcessor mIncomingCallPreProcessor;

    private AirohaOtaFlowMgr otaMgr = null;

    private final Object mConnectionLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        mCtx = this;

        registerIntentFilters();
    }

    private void setForeground(){
        Intent resultIntent = new Intent(this, BluetoothActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        0//PendingIntent.FLAG_UPDATE_CURRENT
                );


        //Create the notification object through the builder
        Notification noti = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("running")
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_bluetooth_audio).build();

        startForeground(1234, noti);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        unregisterReceivers();
        stopForeground(true);
        super.onDestroy();
    }

    private void unregisterReceivers() {
        try {
            Log.d(TAG, "Service unregisterReceivers");
            unregisterReceiver(mBtTrafficReceiver);
            unregisterReceiver(mOtaReceiver);
//            unregisterReceiver(mAdpReceiver);
            mIncomingCallPreProcessor.unregisterPhoneListner();

        }catch (Exception e){
            return; // don't care
        }
    }

    private class ConnectThread extends Thread{
        final String mmBtaddr;
        //boolean mmIsRunning;

        public ConnectThread(String btaddr){
            mmBtaddr = btaddr;
        }

        @Override
        public void run() {
            Log.d(TAG, "start connectSpp thread");

            synchronized (mConnectionLock) {
                boolean isSppConnected = false;
                //mmIsRunning = true;
                mAirohaLink = new AirohaLink(ConnService.this);
                mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
                //mAirohaLink.setOnMmiEventListener(mMmiEventListener);
                mAirohaLink.registerOnMmiEventListener(TAG, mMmiEventListener);
                mAirohaLink.setOnAirohaAirDumpListener(mOnAirohaAirDumpListener);
                mAirohaLink.registerFollowerExistenceListener(TAG, mFollowerExistingListener);

                // 2016.08.23 Daniel: add retries
                // 2 times, interval 2000ms
                // Mantis#7868, System's reconnection process cause the async call to fail

                isSppConnected = mAirohaLink.connect(mmBtaddr);
                int retry = 0;
                while (!isSppConnected && retry <= 2) { //&& mmIsRunning){
                    SystemClock.sleep(2000);
                    // 2016.08.26 Daniel, Mantis#8032: Got cancelled while retrying
                    if (mAirohaLink == null) {
                        Log.d(TAG, "got cancelled by user");
                        return;
                    }
                    isSppConnected = mAirohaLink.connect(mmBtaddr);
                    retry++;
                }

                if (isSppConnected) {
                    final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
                            .getRemoteDevice(mmBtaddr);
                    AirohaEngine.setDeviceName(device.getName());

                    return;
                } else {
                    mAirohaLink = null;
                    sendBroadcast(new Intent(ACTION_CONN_FAIL));
                    stopSelf();
                    return;
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {

            String action = intent.getAction();

            if (action.equals(ACTION_START_CONN)) {
                mBtAddr = intent.getStringExtra(EXTRA_MAC_ADDR);

                // 2016.08.19 Daniel: Mantis#7906, don't let user cancel thread and start frequently
                if (mConnectThread == null) {
                    mConnectThread = new ConnectThread(mBtAddr);
                    mConnectThread.start();
//                DiaryLogger.logToFile("Service onStart", true);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startFragmentActivity(){
        Intent actintent = new Intent();
        actintent.setClass(this, BluetoothActivity.class);
        actintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); // 2016.09.30 Daniel, fixing bug

        startActivity(actintent);
    }

    private void registerIntentFilters(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SEND);
        intentFilter.addAction(ACTION_SEND_QUEUED);
        intentFilter.addAction(ACTION_DISCONNECT);
		// 2016.08.18 Daniel: Mantis#7882, on Nexus 5X, not sending ACL_DISCONNECTED to upper layer, this is a workaround
//        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //2016.08.19 Daniel: Mantis#7878, handle A2DP and Headset profiles disconnection
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_CONN_SUCCESS);
        intentFilter.addAction(ACTION_AT_TOGGLE);
        mCtx.registerReceiver(mBtTrafficReceiver, intentFilter);


        final IntentFilter otaIntentFilter = new IntentFilter();
        otaIntentFilter.addAction(OtaActivity.OTA_START);
        otaIntentFilter.addAction(OtaActivity.OTA_APPLY);
        otaIntentFilter.addAction(OtaActivity.OTA_CANCEL);
        otaIntentFilter.addAction(OtaActivity.OTA_ACCEPT);
        mCtx.registerReceiver(mOtaReceiver, otaIntentFilter);
    }


    private final BroadcastReceiver mBtTrafficReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String tag = "BtTraffic receiver";
            Log.d(tag, action);

            if(action.equals(ACTION_SEND)){
                byte[] extra = intent.getByteArrayExtra(EXTRA_CMD);
                Send(extra);
            }

            if(action.equals(ACTION_SEND_QUEUED)){
                byte[] extra = intent.getByteArrayExtra(EXTRA_CMD);
                SendToQueue(extra);
            }

            if(action.equals(ACTION_DISCONNECT)){
                closeConn("ACTION_DISCONNECT");
            }

            if(action.equals(ACTION_CONN_SUCCESS)){
                //mAirohaCallerNameManager = new AirohaCallerNameManager(mAirohaLink, mCtx);

                mIncomingCallPreProcessor = new IncomingCallPreProcessor(mAirohaLink, mCtx);
            }

            //2016.08.18 Daniel: Mantis#7882, on Nexus 5X, not sending ACL_DISCONNECTED to upper layer, this is a workaround
            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                // 2016.09.30 Daniel: finish the BT Activity first
                sendBroadcast(new Intent(ACTION_SPP_DISCONNECTED));

                // 2016.09.26 Daniel: check if A2DP/HFP is still connected
//                if(isA2DPnHFPStillAlive()){
//                    // 2016.09.26 Daniel: restart the connectSpp flow
////                    DiaryLogger.logToFile("try to recover", true);
//                    mConnectThread = new ConnectThread(mBtAddr);
//                    mConnectThread.start();
//                }else {
//                    closeConn();
//                }
            }

            //2016.08.19 Daniel: Mantis#7878, handle A2DP and Headset profiles disconnection
            if(action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                    || action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)){

                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                if(state == BluetoothProfile.STATE_DISCONNECTED){
                    sendBroadcast(new Intent(ACTION_SPP_DISCONNECTED));
                    closeConn("BluetoothProfile.STATE_DISCONNECTED");
                } else if(state == BluetoothProfile.STATE_CONNECTED){
                    synchronized (this) {
                        if (mConnectThread == null && (mAirohaLink == null || !mAirohaLink.isConnected())) {
                            mConnectThread = new ConnectThread(mBtAddr);
                            mConnectThread.start();
                        }
                    }
                }
            }

            if (action.equals(ACTION_AT_TOGGLE)) {
                toggleAT();
                return;
            }
        }
    };

//    static {
//        System.loadLibrary("dsp-jni");
//    }


    private final BroadcastReceiver mOtaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(OtaActivity.OTA_START)){
                otaMgr = new AirohaOtaFlowMgr(mAirohaLink, mOtaEventListener);
                otaMgr.setBootcodeFileName(intent.getStringExtra(OtaActivity.EXTRA_BOOT));
                otaMgr.setExtFileName(intent.getStringExtra(OtaActivity.EXTRA_EXT));
                otaMgr.setLocalFlag(intent.getBooleanExtra(OtaActivity.EXTRA_IS_LOCAL, false));

                int channel = intent.getIntExtra(OtaActivity.EXTRA_OTA_CHANNEL, OtaActivity.OTA_CHANNEL_MASTER);

                if(channel == OtaActivity.OTA_CHANNEL_FOLLOWER){
                    otaMgr.switchUpdateTarget(TransportTarget.Follower);
                }else {
                    otaMgr.switchUpdateTarget(TransportTarget.Master);
                }

                int otaType = intent.getIntExtra(OtaActivity.EXTRA_OTA_TYPE, OtaActivity.OTA_TYPE_NORMAL);

                switch (otaType){
                    case OtaActivity.OTA_TYPE_NORMAL:
                        otaMgr.setBinFileName(intent.getStringExtra(OtaActivity.EXTRA_BIN));
                        otaMgr.startOTA();
                        break;

                    case OtaActivity.OTA_TYPE_EXT_DEMOSOUND:
                        otaMgr.startOTADemoSound();
                        break;

                    case OtaActivity.OTA_TYPE_LIGHT:
                        otaMgr.setBinFileName(intent.getStringExtra(OtaActivity.EXTRA_DSP_PARAM));
                        otaMgr.startOTALite();
                        break;
                    case OtaActivity.OTA_TYPE_RESUME:
                        otaMgr.setBinFileName(intent.getStringExtra(OtaActivity.EXTRA_BIN));
                        otaMgr.startResumeOTA();
                }
            }

            if(action.equals(OtaActivity.OTA_APPLY)){
                otaMgr.applyOTA();
            }

            if(action.equals(OtaActivity.OTA_CANCEL)){
                otaMgr.cancelOTA();
            }

            if(action.equals(OtaActivity.OTA_ACCEPT)){

                boolean accept = intent.getBooleanExtra(OtaActivity.EXTRA_ACCEPT_ALERT, false);

                //otaMgr.acceptOlderVersionAlert(accept);
            }
        }
    };

    private final OnAirohaOtaEventListener mOtaEventListener = new OnAirohaOtaEventListener() {

        @Override
        public void OnUpdateProgressbar(int value) {
            Intent intent = new Intent(OtaActivity.OTA_UPDATE_PROGRESSBAR);
            intent.putExtra(OtaActivity.OTA_RESULT, value);
            sendBroadcast(intent);
        }

        @Override
        public void OnOtaResult(boolean isPass, String status) {
            Intent intent = new Intent(OtaActivity.OTA_IS_PASS);
            intent.putExtra(OtaActivity.OTA_RESULT, isPass);
            intent.putExtra(OtaActivity.OTA_STATUS, status);
            sendBroadcast(intent);
        }

        @Override
        public void OnOtaStartApplyUI() {
            Intent intent = new Intent(OtaActivity.OTA_START_APPLY_UI);
            sendBroadcast(intent);
        }

        @Override
        public void OnShowCurrentStage(String currentStage) {
            Intent intent = new Intent(OtaActivity.OTA_CURRENT_STAGE);
            intent.putExtra(OtaActivity.OTA_STATUS, currentStage);
            sendBroadcast(intent);
        }

        @Override
        public void OnNotifyMessage(String msg) {
            Intent intent = new Intent(OtaActivity.OTA_NOTIFY_MSG);
            intent.putExtra(OtaActivity.OTA_STATUS, msg);
            sendBroadcast(intent);
        }
    };

    private final OnAirohaAirDumpListener mOnAirohaAirDumpListener = new OnAirohaAirDumpListener() {
        @Override
        public void OnAirDumpDataInd(String hexStr) {
            AirDumpLog.logToFile(hexStr);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String deb = timeStamp + ":" + hexStr;
            AirDumpLogForDebug.logToFile(deb);
        }
    };

    private final OnAirohaMmiEventListener mMmiEventListener = new OnAirohaMmiEventListener() {

        @Override
        public void OnHexResp(byte[] resp) {

        }

        @Override
        public void OnGetChannelInfoResp(byte resp) {

        }

        @Override
        public void OnGetChannelInfoInd(byte bLeft_right) {
            Intent intent = new Intent(UiIntents.ACTION_GET_CHANNEL);
            intent.putExtra(UiIntents.EXTRA_CHANNEL_LR, bLeft_right);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetChannelInfoRespFollower(byte resp) {

        }

        @Override
        public void OnGetChannelInfoIndFollower(byte bLeft_right) {
            Intent intent = new Intent(UiIntents.ACTION_GET_CHANNEL_FR);
            intent.putExtra(UiIntents.EXTRA_CHANNEL_LR, bLeft_right);
            sendBroadcast(intent);
        }

        @Override
        public void OnRoleSwitchResp(byte resp) {

        }

        @Override
        public void OnSetDeviceNameResp(byte resp) {

        }

        @Override
        public void OnGetDeviceNameResp(byte resp) {

        }

        @Override
        public void OnGetDeviceNameInd(String deviceName) {

        }

        @Override
        public void OnGetBatteryResp(byte resp) {
            // don't care
        }

        @Override
        public void OnGetBatteryInd(byte batteryStatus) {
            Intent intent = new Intent(UiIntents.ACTION_GET_BATTERY_IND);
            intent.putExtra(UiIntents.EXTRA_BATTERY_LEVEL, batteryStatus);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetBatteryRespFollower(byte resp) {

        }

        @Override
        public void OnGetBatteryIndFollower(byte batteryStatus) {
            Intent intent = new Intent(UiIntents.ACTION_GET_BATTERY_FR_IND);
            intent.putExtra(UiIntents.EXTRA_BATTERY_LEVEL, batteryStatus);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetTwsSlaveBatteryResp(byte resp) {

        }

        @Override
        public void OnGetTwsSlaveBatteryInd(byte batteryStatus) {

        }

        @Override
        public void OnGetVolumeResp(byte resp) {

        }

        @Override
        public void OnGetVolumeInd(byte volume) {
            Intent intent = new Intent(UiIntents.ACTION_GET_VOLUME_IND);
            intent.putExtra(UiIntents.EXTRA_VOLUME, volume);
            sendBroadcast(intent);
        }

        @Override
        public void OnSetVolumeResp(byte resp) {

        }

        @Override
        public void OnEnableVoicePromptResp(byte resp) {

        }

        @Override
        public void OnDisableVoicePromptResp(byte resp) {

        }

        @Override
        public void OnEnableVoiceCommandResp(byte resp) {

        }

        @Override
        public void OnDisableVoiceCommandResp(byte resp) {

        }

        @Override
        public void OnPlayFindToneResp(byte resp) {
            // don't care
        }

        @Override
        public void OnCheckVoicePromptResp(byte resp) {
            // don't care
        }

        @Override
        public void OnCheckVoicePromptInd(byte isVPEnabled, byte vpLangIndex, byte vpLangTotal, byte[] vpEnums) {
            Intent intent = new Intent(UiIntents.ACTION_GET_VP_IND);
            intent.putExtra(UiIntents.EXTRA_VP_ENABLED, isVPEnabled);
            sendBroadcast(intent);
        }

        @Override
        public void OnNextVoicePromptLangResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_VP_NEXT_RESP);
            sendBroadcast(intent);
        }

        @Override
        public void OnReportVoicePromptStatus(byte vpStatus) {
            Intent intent = new Intent(UiIntents.ACTION_VP_ENABLE_RESP);
            intent.putExtra(UiIntents.EXTRA_VP_ENABLED, vpStatus);
            sendBroadcast(intent);
        }

        @Override
        public void OnReportVoicePromptLangChanged(byte vpLangIdx) {

        }

        @Override
        public void OnSetVoicePromptLangResp(byte resp) {

        }

        @Override
        public void OnCheckEqResp(byte resp) {

        }

        @Override
        public void OnCheckEqInd(final byte a2dpEqIdx, final byte auxEqIdx,
                                 final byte a2dpEqTotal, final byte auxEqTotal,
                                 final byte isDefaultEq,
                                 final byte a2dpSectorMode, final byte auxSectorMode) {
            Intent intent = new Intent(UiIntents.ACTION_GET_PEQ_IND);
            intent.putExtra(UiIntents.EXTRA_PEQ_A2DP_IDX, a2dpEqIdx);
            intent.putExtra(UiIntents.EXTRA_PEQ_AUX_IDX, auxEqIdx);
            intent.putExtra(UiIntents.EXTRA_PEQ_A2DP_COUNT, a2dpEqTotal);
            intent.putExtra(UiIntents.EXTRA_PEQ_AUX_COUNT, auxEqTotal);

            sendBroadcast(intent);
        }

        @Override
        public void OnChangeEqModeResp(byte resp) {
            // don't care
        }

        @Override
        public void OnSetA2dpEqResp(byte resp) {
            // don't care
        }

        @Override
        public void OnReportA2dpEqChanged(byte a2dpEqIdx) {
            Intent intent = new Intent(UiIntents.ACTION_REPORT_PEQ_A2DP_CHANGE);
            intent.putExtra(UiIntents.EXTRA_PEQ_A2DP_IDX, a2dpEqIdx);

            sendBroadcast(intent);
        }

        @Override
        public void OnSetAuxEqResp(byte resp) {
            // don't care
        }

        @Override
        public void OnReportAuxEqChanged(byte auxEqIdx) {
            Intent intent = new Intent(UiIntents.ACTION_REPORT_PEQ_AUX_CHANGE);
            intent.putExtra(UiIntents.EXTRA_PEQ_AUX_IDX, auxEqIdx);

            sendBroadcast(intent);
        }

        @Override
        public void OnGetFwVersionResp(byte resp) {

        }

        @Override
        public void OnGetFwVersionInd(String fwStr) {
            Intent intent = new Intent(UiIntents.ACTION_GET_FW_VERSION);
            intent.putExtra(UiIntents.EXTRA_FW_STRING, fwStr);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetFwVersionRespFollower(byte resp) {

        }

        @Override
        public void OnGetFwVersionIndFollower(String fwStr) {
            Intent intent = new Intent(UiIntents.ACTION_GET_FW_VERSION_FR);
            intent.putExtra(UiIntents.EXTRA_FW_STRING, fwStr);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetTwsSlaveFwVersionResp(byte resp) {

        }

        @Override
        public void OnGetTwsSlaveFwVersionInd(String fwStr) {

        }

        @Override
        public void OnGetChargeBatteryStatusResp(byte resp) {

        }

        @Override
        public void OnGetChargeBatteryStatusInd(ChargingStatus status) {

        }

        @Override
        public void OnGetChargeBatteryStatusFollowerResp(byte resp) {

        }

        @Override
        public void OnGetChargeBatteryStatusFollowerInd(ChargingStatus status) {

        }

        @Override
        public void OnGetVoiceCommandStatusInd(byte voiceCmdStatus) {

        }

        @Override
        public void OnGetCallerNameStatusInd(byte callerNameStatus) {

        }

        @Override
        public void OnSetCallerNameResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_SET_CALLERNAME);
            sendBroadcast(intent);
        }

        @Override
        public void OnPassThroughDataInd(byte[] data) {

        }

        @Override
        public void OnPasThroughDataResp(byte resp) {

        }

        @Override
        public void OnGetSectorInfoResp(byte resp) {

        }

        @Override
        public void OnGetSectorInfoInd(byte[] data) {

        }

        @Override
        public void OnGetSectorInfoRespV2(byte resp) {

        }

        @Override
        public void OnGetSectorInfoIndV2(byte[] data) {

        }


        @Override
        public void OnReportPeqSectorModeChanged(byte mode) {

        }

        @Override
        public void OnAudioTransparencyToggleResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_TOGGLE_AT_RESP);
            intent.putExtra(UiIntents.EXTRA_AT_STATUS, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnSetMasterATGainResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_SET_MASTER_AT_GAIN_RESP);
            intent.putExtra(UiIntents.EXTRA_SET_MASTER_AT_GAIN_RESULT, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnSetSlaveATGainResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_SET_SLAVE_AT_GAIN_RESP);
            intent.putExtra(UiIntents.EXTRA_SET_SLAVE_AT_GAIN_RESULT, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetMasterATGainResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_GET_MASTER_AT_GAIN_RESP);
            intent.putExtra(UiIntents.EXTRA_MASTER_AT_GAIN, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetSlaveATGainResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_GET_SLAVE_AT_GAIN_RESP);
            intent.putExtra(UiIntents.EXTRA_SLAVE_AT_GAIN, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetMasterATStatusResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_GET_MASTER_AT_STATUS_RESP);
            intent.putExtra(UiIntents.EXTRA_MASTER_AT_STATUS, resp);
            sendBroadcast(intent);
        }

        @Override
        public void OnGetSlaveATStatusResp(byte resp) {
            Intent intent = new Intent(UiIntents.ACTION_GET_SLAVE_AT_STATUS_RESP);
            intent.putExtra(UiIntents.EXTRA_SLAVE_AT_STATUS, resp);
            sendBroadcast(intent);
        }

    };

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {

        @Override
        public void OnConnected(String type) {

            mConnectThread = null;

            sendBroadcast(new Intent(ACTION_CONN_SUCCESS));
            setForeground();
            //startFragmentActivity();
        }

        @Override
        public void OnDisconnected() {
            closeConn("mSppStateListener OnDisconnected");
            sendBroadcast(new Intent(ACTION_SPP_DISCONNECTED));
        }
    };

//    private static  boolean isA2DPnHFPStillAlive(){
//        BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
//
//        return btadapter.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED &&
//              btadapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED;
//    }

    private final OnAirohaFollowerExistingListener mFollowerExistingListener = new OnAirohaFollowerExistingListener() {
        @Override
        public void OnSlaveConnected(boolean connected) {
            if(connected){

            }else {
                sendBroadcast(new Intent(ACTION_FOLLOWER_DISCONNECTED));
            }
        }
    };


    private boolean isConnected(){
        if(null == mAirohaLink){
            Log.d("mAirohaLink", "null!!");
            return false;
        }

        if(!mAirohaLink.isConnected()){
            Log.d("mAirohaLink", "!isConnected!!");
            return false;
        }

        return true;
    }

    private synchronized void Send(byte[] command){
        if(isConnected())
            mAirohaLink.sendCommand(command);
        else
            return;
    }

    private synchronized void SendToQueue(byte[] command){
        if(isConnected()){
            mAirohaLink.sendOrEnqueue(command);
        }
    }

    /*
     * cancel connectSpp
     */
    private void closeConn(String closeReason) {
        Log.d(TAG, "closeConn:" + closeReason);

        synchronized (mConnectionLock) {
            if (null != mAirohaLink) {
                mAirohaLink.disconnect();
                mAirohaLink = null;
            }
        }
//        stopSelf();
    }

    private void toggleAT(){
        if (null != mAirohaLink) {
            mAirohaLink.audioTransparencyToggle();
        }
    }
}
