package com.airoha.android.lib.transport;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.BuildConfig;
import com.airoha.android.lib.fieldTest.OnAirohaAirDumpListener;
import com.airoha.android.lib.mmi.OnAirohaCallerNameEventListener;
import com.airoha.android.lib.mmi.OnAirohaDspEventListener;
import com.airoha.android.lib.mmi.OnAirohaMmiEventListener;
import com.airoha.android.lib.mmi.OnAirohaFollowerExistingListener;
import com.airoha.android.lib.mmi.OnAlexaLicenseKeyEventListener;
import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.mmi.cmd.AirohaMMICmd;
import com.airoha.android.lib.mmi.cmd.AirohaMmiPacket;
import com.airoha.android.lib.mmi.cmd.KeyCode;
import com.airoha.android.lib.mmi.cmd.OCF;
import com.airoha.android.lib.mmi.cmd.OGF;
import com.airoha.android.lib.mmi.cmd.UartCmdHeader;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.OnAirohaAclEventListener;
import com.airoha.android.lib.ota.OnAirohaFw4KCrc16Listener;
import com.airoha.android.lib.ota.OnAirohaFwVerSyncListener;
import com.airoha.android.lib.peq.AirohaPeqMgr;
import com.airoha.android.lib.peq.DrcMode.OnAirohaReportDrcModeListener;
import com.airoha.android.lib.peq.OnAirohaPeqControlListener;
import com.airoha.android.lib.physical.AirohaBleController;
import com.airoha.android.lib.physical.AirohaSppController;
import com.airoha.android.lib.physical.IPhysical;
import com.airoha.android.lib.physical.PhysicalType;
import com.airoha.android.lib.transport.Commander.QueuedCmdsCommander;
import com.airoha.android.lib.transport.PacketParser.AclPacketDispatcher;
import com.airoha.android.lib.transport.PacketParser.AirDumpPacketDispatcher;
import com.airoha.android.lib.transport.PacketParser.AlexaPacketDispatcher;
import com.airoha.android.lib.transport.PacketParser.CallerNamePacketDispatcher;
import com.airoha.android.lib.transport.PacketParser.MmiPacketDispatcher;
import com.airoha.android.lib.transport.PacketParser.PacketHeaderChecker;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaLinkDbgLog;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AirohaLink is the center of the Airoha Headset SDK on Android.
 * <p>
 * <p>
 * <p>For state change, you need to register {@link #registerOnConnStateListener(String, OnAirohaConnStateListener)}  and implement {@link OnAirohaConnStateListener}<p/>
 * <p>
 * <p>For Caller Name and OTA, you need to initial {@link AirohaLink} and call {@link #connect(String)} before using them.
 * </p>
 *
 * @author Daniel.Lee
 * @see AirohaOtaFlowMgr
 * @see com.airoha.android.lib.callerName.AirohaCallerNameManager
 */
public class AirohaLink implements ITransport{

    private static final String TAG = "AirohaLink";

    public static final UUID UUID_AIROHA_SPP = UUID.fromString("00000000-0000-0000-0099-aabbccddeeff");

    private final Context mCtx;
    private boolean mIsConnected = false;

    private BluetoothManager mBluetoothManager;
    /* Get Default Adapter */
    private BluetoothAdapter mBluetoothAdapter = null;

    private QueuedCmdsCommander mQueuedCmdsCommander;

    private ConcurrentHashMap<String, OnAirohaConnStateListener> mConnStateListenerMap = null;

    private boolean mIsFilterMmiSendForOta = false;

    private IPhysical mPhysical;

    private MmiPacketDispatcher mMmiPacketDispatcher;

    private AlexaPacketDispatcher mAlexaPacketDispater;

    private AirorhaLinkDbgLog mLogger;

    /**
     * Constructor
     *
     * @param ctx Application Context
     */
    public AirohaLink(Context ctx) {
        Log.d(TAG, "Ver:" + BuildConfig.VERSION_NAME);
        mCtx = ctx;

        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mCtx.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

        mQueuedCmdsCommander = new QueuedCmdsCommander();

        mMmiPacketDispatcher = new MmiPacketDispatcher(this);

        mAlexaPacketDispater = new AlexaPacketDispatcher(this);

        mConnStateListenerMap = new ConcurrentHashMap<>();

        mLogger = new AirorhaLinkDbgLog("NoConnectedDevice");
    }

    private static boolean isHciCmd(byte[] packet) {
        if (packet.length < 3)
            return false;
        return packet[0] == UartCmdHeader.H0 && packet[1] == UartCmdHeader.H1 && packet[2] == UartCmdHeader.H2;
    }

    /**
     * Standard way to connect Spp to SPP
     *
     * @param address BT Addr
     * @return true: Success, false: fail
     * @see OnAirohaConnStateListener
     */
    public boolean connectSpp(String address) throws IllegalArgumentException {
        Log.d(TAG, "connectSpp");

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }

        ACL_OGF.changeOGFforPhysical(PhysicalType.SPP);

        mQueuedCmdsCommander.clearQueue();

        mPhysical = new AirohaSppController(this);

        return mPhysical.connect(address);
    }


    public boolean connectBle(String address) {
        Log.d(TAG, "connectBle");

        ACL_OGF.changeOGFforPhysical(PhysicalType.BLE);

        mQueuedCmdsCommander.clearQueue();

        mPhysical = new AirohaBleController(this);

        return mPhysical.connect(address);
    }

   synchronized public boolean connect(String address) throws IllegalArgumentException {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        String deviceName = device.getName();

        mLogger = new AirorhaLinkDbgLog(deviceName);

        mQueuedCmdsCommander.clearQueue();

        // 2018.06.05 Daniel: refer BTA-1603 force to use SPP
//        int deviceType = device.getType();
//        if(deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC){
            ACL_OGF.changeOGFforPhysical(PhysicalType.SPP);
            mPhysical = new AirohaSppController(this);
//        }

//        if(deviceType == BluetoothDevice.DEVICE_TYPE_LE || deviceType == BluetoothDevice.DEVICE_TYPE_DUAL){
//            ACL_OGF.changeOGFforPhysical(PhysicalType.BLE);
//            mPhysical = new AirohaBleController(this);
//        }

        return mPhysical.connect(address);
    }

    /**
     * disconnect from SPP
     *
     * @see OnAirohaConnStateListener
     */
    synchronized public void disconnect() {
        if (mPhysical != null) {
            mPhysical.disconnect();
            mPhysical = null;
        }

        if (mQueuedCmdsCommander != null) {
            mQueuedCmdsCommander.clearQueue();
            mQueuedCmdsCommander.isResponded = true;
        }

        mIsConnected = false;

        mLogger.stop();
        mLogger = null;
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    /**
     * for extended feature
     *
     * @param cmd
     * @return
     */
    public boolean sendCommand(byte[] cmd) {
        Log.d(TAG, "sendCommnd:" + Converter.byte2HexStr(cmd));

        return mPhysical.write(cmd);
    }


    /**
     * MMI api should use this, Queue mechanism is invoked
     *
     * @param cmd {@link AirohaMMICmd}.
     */
    public synchronized void sendOrEnqueue(byte[] cmd) {
        // 2017.04.14 Daniel, Prevent User sending commands during OTA
        if (mIsFilterMmiSendForOta && isHciCmd(cmd)) {
            Log.d(TAG, "someone trying to bothering OTA, protected!!!");
            return;
        }

        if (mQueuedCmdsCommander.isQueueEmpty() && mQueuedCmdsCommander.isResponded) {
            Log.d(TAG, "soe: cmd send");
            sendCommand(cmd);
            mQueuedCmdsCommander.isResponded = false;
        } else {
            Log.d(TAG, "soe: cmd enqueue");
            mQueuedCmdsCommander.enqueneCmd(cmd);
        }
    }

    /**
     * check MMI API send messages
     */
    private synchronized void checkQueuedActions() {

        Log.d("zyyyyy", "checkQueuedActions set responded");
        mQueuedCmdsCommander.isResponded = true;

        byte[] nextCmd = mQueuedCmdsCommander.getNextCmd();

        if (nextCmd != null)
            sendCommand(nextCmd);
    }

    /**
     * get battery level
     *
     * @see OnAirohaMmiEventListener#OnGetBatteryResp
     * @see OnAirohaMmiEventListener#OnGetBatteryInd(byte)
     */
    public void getBattery() {
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD));
    }

    public void getBatteryFollower(){
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD_FR));
    }

    public void getChannelInfo() {
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_CHANNEL_INFO, OGF.AIROHA_MMI_CMD));
    }

    public void getChannelInfoFollower() {
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_CHANNEL_INFO, OGF.AIROHA_MMI_CMD_FR));
    }

    /**
     * get  TWS Slave battery level
     *
     * @see OnAirohaMmiEventListener#OnGetTwsSlaveBatteryResp(byte)
     * @see OnAirohaMmiEventListener#OnGetTwsSlaveBatteryInd(byte)
     */
    public void getTwsSlaveBattery() {
        sendOrEnqueue(AirohaMMICmd.GET_RIGHT_BATTERY);
    }

    /**
     * play a tone for finding BT device
     *
     * @see OnAirohaMmiEventListener#OnPlayFindToneResp(byte)
     */
    public void playFindTone() {
        sendOrEnqueue(AirohaMMICmd.FIND_MY_ACCESSORY);
    }


    /**
     * get volume
     *
     * @see OnAirohaMmiEventListener#OnGetVolumeResp(byte)
     * @see OnAirohaMmiEventListener#OnGetVolumeInd(byte)
     */
    public void getVolume() {
        sendOrEnqueue(AirohaMMICmd.GET_VOLUME);
    }

    /**
     * enable voice prompt
     *
     * @see OnAirohaMmiEventListener#OnEnableVoicePromptResp(byte)
     */
    public void enableVoicePrompt() {
        sendOrEnqueue(AirohaMMICmd.VOICE_PROMPT_ENABLE);
    }

    /**
     * disable voice prompt
     *
     * @see OnAirohaMmiEventListener#OnDisableVoicePromptResp(byte)
     */
    public void disableVoicePrompt() {
        sendOrEnqueue(AirohaMMICmd.VOICE_PROMPT_DISABLE);
    }

    /**
     * enable voice command
     *
     * @see OnAirohaMmiEventListener#OnEnableVoiceCommandResp(byte)
     */
    public void enableVoiceCommand() {
        sendOrEnqueue(AirohaMMICmd.VOICE_COMMAND_ENABLE);
    }

    /**
     * disable voice command
     *
     * @see OnAirohaMmiEventListener#OnDisableVoiceCommandResp(byte)
     */
    public void disableVoiceCommand() {
        sendOrEnqueue(AirohaMMICmd.VOICE_OMMAND_DISABLE);
    }


    /**
     * set volume, param should be 0~127
     *
     * @param vol, 0~127
     * @throws IOException for invalid input
     * @see OnAirohaMmiEventListener#OnSetVolumeResp(byte)
     */
    public void setVolume(byte vol) throws IOException {
        if (vol > (byte) 0x7F || vol < 0) {
            throw new IOException("Incorrect param, should be 0~127");
        }

        byte[] volb = new byte[]{vol};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_VOLUME, volb));
    }

    /**
     * set A2DP EQ to specific mode
     *
     * @param idx
     * @see OnAirohaMmiEventListener#OnSetA2dpEqResp(byte)
     */
    public void setA2dpEq(byte idx) {

        byte[] idxb = new byte[]{idx};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_PEQ_A2DP, idxb));
    }

    /**
     * st A2DP EQ to idx (1~5) under default(0)/user(1) mode
     * @param idx 1~5
     * @param sectorMode 0/1
     * @throws IllegalArgumentException
     */
    public void setA2dpEq(byte idx, byte sectorMode) throws IllegalArgumentException {
        Log.d(TAG, "setA2dpEq: " + idx + ", " + sectorMode);

        if(sectorMode != 0x00 && sectorMode != 0x01) {
            throw new IllegalArgumentException("mode not supported!");
        }

        if(idx >0x05 || idx < 0x00) {
            throw new IllegalArgumentException("index not supported!");
        }


        byte[] payload = new byte[]{idx, sectorMode};
//        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_PEQ_A2DP_W_MODE, payload));

        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.SET_PEQ_A2DP, OGF.AIROHA_MMI_CMD, payload);

        sendOrEnqueue(packet.getRaw());
    }

    /**
     * set A2DP EQ to specific mode
     *
     * @param idx
     * @see OnAirohaMmiEventListener#OnSetAuxEqResp(byte)
     */
    public void setAuxEq(byte idx) {

        byte[] idxb = new byte[]{idx};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_PEQ_AUX, idxb));
    }

    /**
     * check EQ info
     *
     * @see OnAirohaMmiEventListener#OnCheckEqResp(byte)
     */
    public void checkEQ() {
        sendOrEnqueue(AirohaMMICmd.GET_PEQ);
    }

    /**
     * change to next EQ
     *
     * @see OnAirohaMmiEventListener#OnChangeEqModeResp(byte)
     */
    public void changeEQMode() {
        sendOrEnqueue(AirohaMMICmd.KEY_PEQ_MODE_CHANGE);
    }

    /**
     * check voice prompt
     *
     * @see OnAirohaMmiEventListener#OnCheckVoicePromptResp(byte)} (byte)
     * @see OnAirohaMmiEventListener#OnCheckVoicePromptInd(byte, byte, byte, byte[])
     */
    public void checkVoicePrompt() {
        sendOrEnqueue(AirohaMMICmd.GET_VOICE_PROMPT);
    }

    /**
     * change to next voice prompt language
     *
     * @see OnAirohaMmiEventListener#OnNextVoicePromptLangResp(byte)
     */
    public void nextVoicePromptLang() {
        sendOrEnqueue(AirohaMMICmd.VOICE_PROMPT_LANG_CHANGE_NEXT);
    }

    /**
     * set voice prompt language by index
     *
     * @param idx
     * @see OnAirohaMmiEventListener#OnSetVoicePromptLangResp(byte)
     */
    public void setVoicePromptLang(byte idx) {
        byte[] idxb = new byte[]{idx};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_VP_LANG, idxb));
    }

    /**
     * set Caller Name function to be turned on,
     *
     * @see com.airoha.android.lib.callerName.AirohaCallerNameManager#AirohaCallerNameManager(AirohaLink, Context)
     * @see #getCallerNameStatus()
     * @see OnAirohaMmiEventListener#OnSetCallerNameResp(byte)
     */
    public void setCallerNameOn() {
        sendOrEnqueue(AirohaMMICmd.CALLER_NAME_ON);
    }

    /**
     * set Caller Name function to be turned off,
     *
     * @see #getCallerNameStatus()
     * @see OnAirohaMmiEventListener#OnSetCallerNameResp(byte)
     */
    public void setCallerNameOff() {
        sendOrEnqueue(AirohaMMICmd.CALLER_NAME_OFF);
    }

    /**
     * get the FW version
     *
     * @see OnAirohaMmiEventListener#OnGetFwVersionInd(String)
     */
    public void getFwVersion() {
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD));
    }

    public void getFwVersionFollower() {
        sendOrEnqueue(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD_FR));
    }

    /**
     * get the FW version of TWS Slave
     *
     * @see OnAirohaMmiEventListener#OnGetTwsSlaveFwVersionInd(String)
     */
    public void getTwsSlaveFwVersion() {
        sendOrEnqueue(AirohaMMICmd.GET_TWS_SLAVE_VERSION);
    }

    /**
     * get the charging status
     *
     * @see OnAirohaMmiEventListener#OnGetChargeBatteryStatusResp(byte)
     * @see OnAirohaMmiEventListener#OnGetChargeBatteryStatusInd(ChargingStatus)
     * @see ChargingStatus
     */
    public void getChargeBatteryStatus() {
        sendOrEnqueue(AirohaMMICmd.GET_CHG_BAT_STATUS);
    }

    public void getChageBatteryStatusFollower() {
        sendOrEnqueue(AirohaMMICmd.GET_CHG_BAT_STATUS_FOLLOWER);
    }

    /**
     * Need to set a listener implementing {@link OnAirohaMmiEventListener} interface for handing MMI events callback
     *
     * @param subscriberName arbitrary name for the subscriber
     * @param listener
     */
    public void registerOnMmiEventListener(String subscriberName, OnAirohaMmiEventListener listener){
        mMmiPacketDispatcher.registerMmiListener(subscriberName, listener);
    }

    public void registerOnAirohaSamplingRateListener(String subscriberName, OnAirohaPeqControlListener listener) {
        mMmiPacketDispatcher.registerPeqControlListener(subscriberName, listener);
    }
    public void clearOnMmiEventListener() {
        //mOnMiEventListener = null;
    }

    /**
     * Need to set a listener implementing  {@link OnAirohaConnStateListener} interface for handling SPP/BLE state changes
     * @param subscriberName arbitrary name for the subscriber
     * @param listener
     */
    public void registerOnConnStateListener(String subscriberName, OnAirohaConnStateListener listener) {
        mConnStateListenerMap.put(subscriberName, listener);
    }

    /**
     * Need to set a listener implementing {@link OnAirohaCallerNameEventListener} interface for handing Caller Name events callback
     *
     * @param listener
     */
    public void setOnCallerNameEventListener(OnAirohaCallerNameEventListener listener) {
        CallerNamePacketDispatcher.setOnAirohaCallerNameEventListener(listener);
    }

    /**
     * get the Voice Command feature status
     *
     * @see OnAirohaMmiEventListener#OnGetVoiceCommandStatusInd(byte)
     */
    public void getVoiceCommandStatus() {
        sendOrEnqueue(AirohaMMICmd.GET_VOICE_COMMAND_STATUS);
    }

    /**
     * get the Caller Name feature status
     *
     * @see OnAirohaMmiEventListener#OnGetCallerNameStatusInd(byte)
     */
    public void getCallerNameStatus() {
        sendOrEnqueue(AirohaMMICmd.GET_CALLER_NAME_STATUS);
    }

    /**
     * get SDK version name
     *
     * @return SDK version name
     */
    public String getSdkVer() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Prevent User sending commands during OTA
     *
     * @param isFilterMmiSendForOta
     */
    public void filterMmiSendForOta(boolean isFilterMmiSendForOta) {
        mIsFilterMmiSendForOta = isFilterMmiSendForOta;
        Log.d(TAG, "MMI filter:" + mIsFilterMmiSendForOta);
    }

    /**
     * Force to clear all pending MMI commands
     * For Airoha Engineers only
     */
    public synchronized void clearMmiQueue() {
        mQueuedCmdsCommander.isResponded = true;
        mQueuedCmdsCommander.clearQueue();

    }

    public void setAclEventListener(OnAirohaAclEventListener listener) {
        AclPacketDispatcher.setListener(listener);
    }

    public void clearAclEventListener() {
        //mAclEventListener = null;
    }

    public void setFwVerSyncListener(OnAirohaFwVerSyncListener listener) {
        mMmiPacketDispatcher.setFwVerSyncListener(listener);
    }

    /**
     * Share the context to other Airoha modules
     *
     * @return Context of AirohaLink is related
     */
    @Override
    public Context getContext() {
        return mCtx;
    }

    public void sendPassThroughData(byte[] data) {
        byte payloadLength = (byte) (data.length + 2); // 2: ocf+ogf

        byte[] cmd = new byte[4 + payloadLength];

        cmd[0] = UartCmdHeader.H0;
        cmd[1] = UartCmdHeader.H1;
        cmd[2] = UartCmdHeader.H2;
        cmd[3] = payloadLength;
        cmd[4] = OCF.PASS_THROUGH_CMD;
        cmd[5] = OGF.PASS_THROUGH_CMD;

        System.arraycopy(data, 0, cmd, 6, data.length);

        sendOrEnqueue(cmd);
    }

    public void getSectorInfo(byte sectorIdx, byte offset, byte dataLengthFromOffset) {
        sendOrEnqueue(AirohaMMICmd.GET_SECTOR_INFO(sectorIdx, offset, dataLengthFromOffset));
    }

    public void getSectorInfoV2(byte sectorBasePlusOffset, byte sectorGroup, byte headOffsetL, byte headOffsetH, byte dataLenL, byte dataLenH) {
        sendCommand(AirohaMMICmd.GET_SECTOR_INFO_V2(sectorBasePlusOffset, sectorGroup, headOffsetL, headOffsetH, dataLenL, dataLenH));
    }



    /**
     *
     * @param enableMode {@link com.airoha.android.lib.peq.RealTimeEnableMode}
     * @param param
     */
    public void sendRealTimeUpdatePEQ(byte enableMode, byte[] param, byte ogf){
        int paramLength = param.length;

        byte[] command = new byte[7 + paramLength];

        command[0] = UartCmdHeader.H0;
        command[1] = UartCmdHeader.H1;
        command[2] = UartCmdHeader.H2;
        command[3] = (byte) (paramLength + 3);
        command[4] = OCF.SEND_REAL_TIME_UPDATE_PEQ;
        command[5] = ogf;
        command[6] = enableMode;
        System.arraycopy(param, 0, command, 7, paramLength);

        sendOrEnqueue(command);
    }

    public void getUserDefinedPeqHpfSectorIdx(){
        sendOrEnqueue(AirohaMMICmd.GET_USER_DEFINED_PEQ_HPF_SECTOR_IDX);
    }

    /**
	 * switch the Audio Transparency to ON/OFF
	 * @see OnAirohaMmiEventListener#OnAudioTransparencyToggleResp(byte)
	 */
    public void audioTransparencyToggle(){
		sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.TRIGGER_KEY_EVENT, KeyCode.AUDIO_TRANSPARENCY));
	}
    /**
     * set the Audio Transparency Gain to Master device
     * @param gain, 0x00~0x0F
     * @see OnAirohaMmiEventListener#OnSetMasterATGainResp(byte)
     */
    public void setMasterATGain(byte gain){
        byte[] data = new byte[]{gain};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_MASTER_AT_GAIN, data));
    }
    /**
     * set the Audio Transparency Gain to Slave device
     * @param gain, 0x00~0x0F
     * @see OnAirohaMmiEventListener#OnSetSlaveATGainResp(byte)
     */
    public void setSlaveATGain(byte gain){
        byte[] data = new byte[]{gain};
        sendOrEnqueue(AirohaMMICmd.combineComplexCmd(AirohaMMICmd.SET_SLAVE_AT_GAIN, data));
    }
    /**
     * get the Audio Transparency Gain from Master device
     * @see OnAirohaMmiEventListener#OnGetMasterATGainResp(byte)
     */
    public void getMasterATGain(){
        sendOrEnqueue(AirohaMMICmd.GET_MASTER_AT_GAIN);
    }
    /**
     * get the Audio Transparency Gain from Slave device
     * @see OnAirohaMmiEventListener#OnGetSlaveATGainResp(byte)
     */
    public void getSlaveATGain(){
        sendOrEnqueue(AirohaMMICmd.GET_SLAVE_AT_GAIN);
    }
    /**
     * get the Audio Transparency status from Master device
     * @see OnAirohaMmiEventListener#OnGetMasterATStatusResp(byte)
     */
	public void getMasterATStatus(){
		sendOrEnqueue(AirohaMMICmd.GET_MASTER_AT_STATUS);
}
    /**
     * get the Audio Transparency status from Slave device
     * @see OnAirohaMmiEventListener#OnGetSlaveATStatusResp(byte)
     */
	public void getSlaveATStatus(){
		sendOrEnqueue(AirohaMMICmd.GET_SLAVE_AT_STATUS);
	}

	public void startRoleSwitch(){
	    sendOrEnqueue(AirohaMMICmd.ROLE_SWITCH);
    }

	public void setOnAirohaAirDumpListener(OnAirohaAirDumpListener listener){
        AirDumpPacketDispatcher.setOnAirohaAirDumpListener(listener);
    }

    public void setOnPeqRealTimeUpdateListenr(AirohaPeqMgr.OnSendRealTimeUpdatePeqRespListener listenr){
	    mMmiPacketDispatcher.setRealTimeUpdatePeqRespListener(listenr);
    }

    public void setDspEventListener(OnAirohaDspEventListener listener){
        mMmiPacketDispatcher.setDspEventListener(listener);
    }

    public void setFw4KCrc16Listener(OnAirohaFw4KCrc16Listener listener){
        mMmiPacketDispatcher.setFwF4Crc16Listener(listener);
    }

    public void setReportDrcModeListener(OnAirohaReportDrcModeListener listener) {
	    mMmiPacketDispatcher.setReportDrcModeListener(listener);
    }

    @Override
    public void handlePhysicalPacket(byte[] packet){

        // check for HCI
        if (PacketHeaderChecker.isHciEventPacketCollected(packet)) {
            // check for Air Dump first
            if (PacketHeaderChecker.isAirDumpCollected(packet)) {
                AirDumpPacketDispatcher.parseSend(packet);
                Log.d("zyyyyy", "handlePhysicalPacket :  " + Converter.byte2HexStr(packet));
                return;
            }

            // else
            mMmiPacketDispatcher.parseSend(packet);
            CallerNamePacketDispatcher.parseSendCallerNameInExit(packet);

            //音量
            if (MmiPacketDispatcher.isActiveResp(packet)) {
                checkQueuedActions();
                Log.d("zyyyyy", "checkQueuedActions:  " + Converter.byte2HexStr(packet));
            }
            return;
        }

        // check for ACL
        if (PacketHeaderChecker.isAclPacketCollected(packet)) {
            // check for Caller Name function first
            if(PacketHeaderChecker.isCallerReportPacketCollected(packet)) {
                CallerNamePacketDispatcher.parseSendCallerNameProgress(packet);
                Log.d("zyyyyy", "isAclPacketCollected:  " + Converter.byte2HexStr(packet));
                return;
            }

            // else
            AclPacketDispatcher.parseSend(packet);
            return;
        }

        if(PacketHeaderChecker.isAlexaEventCollected(packet)) {

            // 2018.07.24 call the dispatcher
            mAlexaPacketDispater.parseSend(packet);
            Log.d("zyyyyy", "mAlexaPacketDispater:  " + Converter.byte2HexStr(packet));
        }
    }

    @Override
    public void OnPhysicalConnected(String type) {
        mIsConnected = true;

        for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
            if(listener != null) {
                listener.OnConnected(type);
            }
        }
    }

    @Override
    public void OnPhysicalDisconnected(String type) {
        mIsConnected = false;

        for (OnAirohaConnStateListener listener : mConnStateListenerMap.values()) {
            if(listener != null) {
                listener.OnDisconnected();
            }
        }

    }


    public void logToFile(String tag, String content) {
        Log.d(tag, content);

        if(mLogger == null)
            return;

        mLogger.logToFile(tag, content);
    }


    /**
     * Debug use. App developer should not use it.
     */
    public void getMusicSampleRate(){
        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.GET_MUSIC_SAMPLE_RATE, OGF.AIROHA_MMI_CMD, null);
        sendOrEnqueue(packet.getRaw());
    }

    /**
     * get the UI data of Master's PEQ
     */
    public void getRealTimeUiData(){
        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.GET_REAL_TIME_UI_DATA, OGF.AIROHA_MMI_CMD, null);
        sendOrEnqueue(packet.getRaw());
    }

    /**
     * get the UI data of Follower's PEQ
     */
    public void getRealTimeUiDataFollower(){
        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.GET_REAL_TIME_UI_DATA, OGF.AIROHA_MMI_CMD_FR, null);
        sendOrEnqueue(packet.getRaw());
    }

    /**
     * Used by AirohaPeqMgr
     * Airoha engineer only. App developer should not use it.
     * @param data
     */
    public void setRealTimeUiData(byte[] data){
        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.SET_REAL_TIME_UI_DATA, OGF.AIROHA_MMI_CMD, data);
        sendOrEnqueue(packet.getRaw());
    }

    /**
     * Used by AirohaPeqMgr
     * Airoha engineer only. App developer should not use it.
     * @param data
     */
    public void setRealTimeUiDataFollower(byte[] data){
        AirohaMmiPacket packet = new AirohaMmiPacket(OCF.SET_REAL_TIME_UI_DATA, OGF.AIROHA_MMI_CMD_FR, data);
        sendOrEnqueue(packet.getRaw());
    }


    /**
     * Airoha engineer only. App developer should not use it.
     * @param observer
     * @param listener
     */
    public void registerFollowerExistenceListener(String observer, OnAirohaFollowerExistingListener listener) {
	    mMmiPacketDispatcher.registerFollowerExistingListener(observer, listener);
    }

    /**
     * Airoha engineer only. App developer should not use it.
     * @param onOff
     */
    public void setFotaStatus(byte onOff){
        // stop other MMI actions
        clearMmiQueue();

	    byte[] payload = new byte[]{onOff};

	    AirohaMmiPacket packet = new AirohaMmiPacket(OCF.SET_FOTA_STATUS, OGF.AIROHA_MMI_CMD, payload);
	    // send this right away to secure FW
	    sendCommand(packet.getRaw());
    }

    /**
     * Only special FW will report Alexa license key
     * @param observer
     * @param licenseKeyEventListener
     */
    public void registerAlexaLicenseKeyListener(String observer,
                                                OnAlexaLicenseKeyEventListener licenseKeyEventListener) {
	    mAlexaPacketDispater.registerListener(observer, licenseKeyEventListener);
    }
}
