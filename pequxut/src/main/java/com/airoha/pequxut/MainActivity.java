package com.airoha.pequxut;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airoha.android.lib.mmi.OnAirohaMmiEventListener;
import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.peq.AirohaPeqMgr;
import com.airoha.android.lib.peq.OnAirohaPeqControlListener;
import com.airoha.android.lib.peq.PeqUserInputStru;
import com.airoha.android.lib.peq.UserInputConstraint;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.TransportTarget;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PeqUxUt";

    @Bind(R.id.textViewVer)
    protected TextView mTextVer;
    @Bind(R.id.editTextBtAddr)
    protected EditText mEditSppAddr;
    @Bind(R.id.buttonConSpp)
    protected Button mBtnConSpp;
    @Bind(R.id.buttonConBle)
    protected Button mBtnConBle;
    @Bind(R.id.buttonDisCon)
    protected Button mBtnDisCon;
    @Bind(R.id.textViewConResult)
    protected TextView mTextConSppResult;
    @Bind(R.id.textViewConState)
    protected TextView mTextConState;

    @Bind(R.id.seekBar0)
    protected SeekBar mSeekBarGain0;
    @Bind(R.id.seekBar1)
    protected SeekBar mSeekBarGain1;
    @Bind(R.id.seekBar2)
    protected SeekBar mSeekBarGain2;
    @Bind(R.id.seekBar3)
    protected SeekBar mSeekBarGain3;
    @Bind(R.id.seekBar4)
    protected SeekBar mSeekBarGain4;
    @Bind(R.id.seekBar5)
    protected SeekBar mSeekBarGain5;
    @Bind(R.id.seekBar6)
    protected SeekBar mSeekBarGain6;
    @Bind(R.id.seekBar7)
    protected SeekBar mSeekBarGain7;
    @Bind(R.id.seekBar8)
    protected SeekBar mSeekBarGain8;
    @Bind(R.id.seekBar9)
    protected SeekBar mSeekBarGain9;
    @Bind(R.id.editTextGain0)
    protected EditText mEditTextGain0;
    @Bind(R.id.editTextGain1)
    protected EditText mEditTextGain1;
    @Bind(R.id.editTextGain2)
    protected EditText mEditTextGain2;
    @Bind(R.id.editTextGain3)
    protected EditText mEditTextGain3;
    @Bind(R.id.editTextGain4)
    protected EditText mEditTextGain4;
    @Bind(R.id.editTextGain5)
    protected EditText mEditTextGain5;
    @Bind(R.id.editTextGain6)
    protected EditText mEditTextGain6;
    @Bind(R.id.editTextGain7)
    protected EditText mEditTextGain7;
    @Bind(R.id.editTextGain8)
    protected EditText mEditTextGain8;
    @Bind(R.id.editTextGain9)
    protected EditText mEditTextGain9;
    @Bind(R.id.editTextFreq0)
    protected EditText mEditTextFreq0;
    @Bind(R.id.editTextFreq1)
    protected EditText mEditTextFreq1;
    @Bind(R.id.editTextFreq2)
    protected EditText mEditTextFreq2;
    @Bind(R.id.editTextFreq3)
    protected EditText mEditTextFreq3;
    @Bind(R.id.editTextFreq4)
    protected EditText mEditTextFreq4;
    @Bind(R.id.editTextFreq5)
    protected EditText mEditTextFreq5;
    @Bind(R.id.editTextFreq6)
    protected EditText mEditTextFreq6;
    @Bind(R.id.editTextFreq7)
    protected EditText mEditTextFreq7;
    @Bind(R.id.editTextFreq8)
    protected EditText mEditTextFreq8;
    @Bind(R.id.editTextFreq9)
    protected EditText mEditTextFreq9;

    @Bind(R.id.editTextBw0)
    protected EditText mEditTextBw0;
    @Bind(R.id.editTextBw1)
    protected EditText mEditTextBw1;
    @Bind(R.id.editTextBw2)
    protected EditText mEditTextBw2;
    @Bind(R.id.editTextBw3)
    protected EditText mEditTextBw3;
    @Bind(R.id.editTextBw4)
    protected EditText mEditTextBw4;
    @Bind(R.id.editTextBw5)
    protected EditText mEditTextBw5;
    @Bind(R.id.editTextBw6)
    protected EditText mEditTextBw6;
    @Bind(R.id.editTextBw7)
    protected EditText mEditTextBw7;
    @Bind(R.id.editTextBw8)
    protected EditText mEditTextBw8;
    @Bind(R.id.editTextBw9)
    protected EditText mEditTextBw9;

    @Bind(R.id.buttonUpdateRealtimePEQ)
    protected Button mBtnUpdateRealTimePEQ;

    @Bind(R.id.buttonUpdatePeqWith3SamplingRates)
    protected Button mBtnUpdatePeqWith3SamplingRates;

    @Bind(R.id.buttonUpdatePeqUiData)
    protected Button mBtnUpdatePeqUiData;

    @Bind(R.id.buttonLazyForTest)
    protected Button mBtnLazyForTest;

    @Bind(R.id.ckbEnableExp)
    protected CheckBox mCkbEnableExp;

    @Bind(R.id.btnGetMusicSampleRate)
    protected Button mBtnGetMusicSampleRate;

    @Bind(R.id.textViewMusicSampleRateEnum)
    protected TextView mTextMusicSampleRateEnum;

    @Bind(R.id.radioBtnMaster)
    protected RadioButton mRadioBtnMaster;

    @Bind(R.id.radioBtnFollower)
    protected RadioButton mRadioBtnFollower;

    @Bind(R.id.ckbEnableTwsSync)
    protected CheckBox mCkbEnableTwsSync;

    // check EQ
    @Bind(R.id.buttonCheckEq)
    protected Button mBtnCheckEq;

    @Bind(R.id.textViewCheckEqInd)
    protected TextView mTextCheckEqInd;

    // set Eq
    @Bind(R.id.buttonSetA2dpPeq)
    protected Button mBtnSetA2dpPeq;

    @Bind(R.id.editTextA2dpIdx)
    protected EditText mEditA2dpIdx;

    @Bind(R.id.editTextA2dpMode)
    protected EditText mEditA2dpMode;

    @Bind(R.id.textViewSetA2dpPeqResp)
    protected TextView mTextSetA2dpResp;

    private Context mCtx = this;

    private AirohaLink mAirohaLink = null;
    private AirohaPeqMgr mAirohaPeqMgr = null;

    private PeqUserInputStru mPeqUserInputStru = new PeqUserInputStru();

    private final double PROGRESS_STEP = 0.001;

    private EditText[] mEditFreqs;
    private SeekBar[] mSeekBars;
    private EditText[] mEditGains;
    private EditText[] mEditBws;
    private boolean mIsPeqExpEnabled = true;

    private TransportTarget mTransportTarget = TransportTarget.Master;

    private static final int CASE_ONE_SR_MASTER = 0;
    private static final int CASE_ONE_SR_FOLLOWER = 1;
//    private static final int CASE_THREE_SR_MASTER = 2;
//    private static final int CASE_THREE_SR_FOLLOWER = 3;
    private static final int CASE_ONE_SR_TWS = 4;
//    private static final int CASE_THREE_SR_TWS = 5;

    private int mUpdateCase;

    // paired list
    private ListView mPairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAirohaLink = new AirohaLink(this);
        mTextVer.setText("SDK Ver:" + mAirohaLink.getSdkVer());
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnMmiEventListener(TAG, mOnAirohaMmiEventListener);
        mAirohaLink.registerOnAirohaSamplingRateListener(TAG, mOnSamplingRateListener);

        mAirohaPeqMgr = new AirohaPeqMgr(mAirohaLink);
        mAirohaPeqMgr.setUserDefinedPeqParsingListener(mAirohaPeqUiListener);

        initUImember();

        requestExternalStoragePermission();
        
        initialButtonStates();

        updatePairedList();

        connectProfile();
    }
    
    void initialButtonStates(){
        mBtnDisCon.setEnabled(false);
        mBtnUpdateRealTimePEQ.setEnabled(false);
        mBtnConSpp.setEnabled(true);
        mBtnGetMusicSampleRate.setEnabled(false);
        mBtnUpdatePeqWith3SamplingRates.setEnabled(false);
        mBtnUpdatePeqUiData.setEnabled(false);
    }
    
    void workingButtonStates(){
        mBtnDisCon.setEnabled(true);
        mBtnUpdateRealTimePEQ.setEnabled(true);
        mBtnConSpp.setEnabled(false);
        mBtnGetMusicSampleRate.setEnabled(true);
        mBtnUpdatePeqWith3SamplingRates.setEnabled(true);
        mBtnUpdatePeqUiData.setEnabled(true);
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConState.setText("Connected:" + type);

                    workingButtonStates();

                    mRadioBtnMaster.setEnabled(true);
                    mRadioBtnMaster.setChecked(true);
                }
            });

            mAirohaLink.getRealTimeUiData();
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "DisConnected", Toast.LENGTH_SHORT).show();
                    mTextConState.setText("DisConnected");

                    initialButtonStates();
                }
            });
        }
    };

    private OnAirohaPeqControlListener mOnSamplingRateListener = new OnAirohaPeqControlListener() {
        @Override
        public void OnMusicSampleRateChanged(final byte sampleRateEnum) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextMusicSampleRateEnum.setText(String.valueOf(sampleRateEnum));
                }
            });


            mAirohaPeqMgr.configSamplingRateByCases(sampleRateEnum);

            // TODO User can interact with the UI and Update
        }

        @Override
        public void OnGetMusicSampleRateResp(byte resp) {

        }

        @Override
        public void OnGetRealTimeUiDataResp(byte resp) {

        }

        @Override
        public void OnGetRealTimeUiDataInd(byte[] data) {
           setUiDisplay(data);
        }

        @Override
        public void OnReportRealTimeUiDataInd(byte[] data) {
            setUiDisplay(data);
        }

        @Override
        public void OnSetRealTimeUiDataResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(resp == 0x00){
                        Toast.makeText(mCtx, "Peq Ui Data updated to FW", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private void setUiDisplay(byte[] data){
        mPeqUserInputStru = new PeqUserInputStru(data);

        if(mPeqUserInputStru.isDataEmpty()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCtx, "First init. User has not defined PEQ yet", Toast.LENGTH_LONG).show();

                    // 2018.06.27 BTA-1714: clear UI for prevent misleading UI
                    //
                    for(int i = 0; i<10; i++){
                        mEditFreqs[i].setText("0");
                        mEditGains[i].setText("0");
                        mEditBws[i].setText("0");
//                        mSeekBars[i].setProgress(0);
                    }
                }
            });

            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{

                    for(int i = 0; i< 5; i++){
                        mEditFreqs[i].setText(String.valueOf(mPeqUserInputStru.getUserInputFreq(i)));
                        mEditBws[i].setText(String.valueOf(mPeqUserInputStru.getUserInputBw(i)));
                        mEditGains[i].setText(String.valueOf(mPeqUserInputStru.getUserInputGain(i)));
                    }

                    mSeekBarGain0.setProgress(covertToProgress(mPeqUserInputStru.getUserInputGain(0)));
                    if(Double.valueOf(mEditTextFreq0.getText().toString()) < UserInputConstraint.FREQ_6DB_GAIN_MAX){
                        mSeekBarGain0.setProgress(convertToProgressLpf(mPeqUserInputStru.getUserInputGain(0)));
                    }
                    mSeekBarGain1.setProgress(covertToProgress(mPeqUserInputStru.getUserInputGain(1)));
                    if(Double.valueOf(mEditTextFreq0.getText().toString()) < UserInputConstraint.FREQ_6DB_GAIN_MAX){
                        mSeekBarGain1.setProgress(convertToProgressLpf(mPeqUserInputStru.getUserInputGain(1)));
                    }

                    mSeekBarGain2.setProgress(covertToProgress(mPeqUserInputStru.getUserInputGain(2)));
                    mSeekBarGain3.setProgress(covertToProgress(mPeqUserInputStru.getUserInputGain(3)));
                    mSeekBarGain4.setProgress(covertToProgress(mPeqUserInputStru.getUserInputGain(4)));

                    if(mPeqUserInputStru.isExpEnabled()) {
                        mCkbEnableExp.setChecked(true);

                        for (int i = 0; i < 5; i++) {
                            mEditFreqs[5 + i].setText(String.valueOf(mPeqUserInputStru.getUserInputExpFreq(i)));
                            mEditBws[5 + i].setText(String.valueOf(mPeqUserInputStru.getUserInputExpBw(i)));
                            mEditGains[5 + i].setText(String.valueOf(mPeqUserInputStru.getUserInputExpGain(i)));

                            mSeekBars[5+i].setProgress(covertToProgress(mPeqUserInputStru.getUserInputExpGain(i)));
                        }
                    }else {
                        mCkbEnableExp.setChecked(false);
                    }



                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    private OnAirohaMmiEventListener mOnAirohaMmiEventListener = new OnAirohaMmiEventListener() {

        @Override
        public void OnHexResp(final byte[] resp) {
        }

        @Override
        public void OnGetChannelInfoResp(byte resp) {

        }

        @Override
        public void OnGetChannelInfoInd(byte bLeft_right) {

        }

        @Override
        public void OnGetChannelInfoRespFollower(byte resp) {

        }

        @Override
        public void OnGetChannelInfoIndFollower(byte bLeft_right) {

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

        }

        @Override
        public void OnGetBatteryInd(byte batteryStatus) {

        }

        @Override
        public void OnGetBatteryRespFollower(byte resp) {

        }

        @Override
        public void OnGetBatteryIndFollower(byte batteryStatus) {

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

        }

        @Override
        public void OnCheckVoicePromptResp(byte resp) {

        }

        @Override
        public void OnCheckVoicePromptInd(byte isVPEnabled, byte vpLangIndex, byte vpLangTotal, byte[] vpEnums) {

        }

        @Override
        public void OnNextVoicePromptLangResp(byte resp) {

        }

        @Override
        public void OnReportVoicePromptStatus(byte vpStatus) {

        }

        @Override
        public void OnReportVoicePromptLangChanged(byte vpLangIdx) {

        }

        @Override
        public void OnSetVoicePromptLangResp(byte resp) {

        }

        @Override
        public void OnCheckEqResp(final byte resp) {

        }

        @Override
        public void OnCheckEqInd(final byte PEQInA2DP, final byte PEQInAUX,
                                 final byte PEQNumInA2DP, final byte PEQNumInAUX,
                                 final byte isUseDefaultPEQ,
                                 final byte A2DPSectorMode, final byte AUXSectorMode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextCheckEqInd.setText("PEQInA2DP: " + PEQInA2DP +
                            ", PEQInAUX: " + PEQInAUX + ", PEQNumInA2DP:" + PEQNumInA2DP +
                            ", PEQNumInAUX: " + PEQNumInAUX + ", isUseDefaultPEQ: " + isUseDefaultPEQ +
                            ", A2DPSectorMode: " + A2DPSectorMode + ", AUXSectorMode: " + AUXSectorMode);
                }
            });

        }

        @Override
        public void OnChangeEqModeResp(byte resp) {

        }

        @Override
        public void OnSetA2dpEqResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetA2dpResp.setText(String.format("%02X", resp));
                }
            });

        }

        @Override
        public void OnReportA2dpEqChanged(byte a2dpEqIdx) {

        }

        @Override
        public void OnSetAuxEqResp(byte resp) {

        }

        @Override
        public void OnReportAuxEqChanged(byte auxEqIdx) {

        }

        @Override
        public void OnGetFwVersionResp(byte resp) {

        }

        @Override
        public void OnGetFwVersionInd(String fwStr) {

        }

        @Override
        public void OnGetFwVersionRespFollower(byte resp) {

        }

        @Override
        public void OnGetFwVersionIndFollower(String fwStr) {

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
        public void OnReportPeqSectorModeChanged(final byte mode) {

        }

        @Override
        public void OnAudioTransparencyToggleResp(byte resp) {

        }

        @Override
        public void OnSetMasterATGainResp(byte resp) {

        }

        @Override
        public void OnSetSlaveATGainResp(byte resp) {

        }

        @Override
        public void OnGetMasterATGainResp(byte resp) {

        }

        @Override
        public void OnGetSlaveATGainResp(byte resp) {

        }

        @Override
        public void OnGetMasterATStatusResp(byte resp) {

        }

        @Override
        public void OnGetSlaveATStatusResp(byte resp) {

        }
    };

    void initUImember() {
        mEditFreqs = new EditText[] {
                mEditTextFreq0, mEditTextFreq1, mEditTextFreq2, mEditTextFreq3, mEditTextFreq4,
                mEditTextFreq5, mEditTextFreq6, mEditTextFreq7, mEditTextFreq8, mEditTextFreq9
        };

        mSeekBars = new SeekBar[] {
                mSeekBarGain0, mSeekBarGain1, mSeekBarGain2, mSeekBarGain3, mSeekBarGain4,
                mSeekBarGain5, mSeekBarGain6, mSeekBarGain7, mSeekBarGain8, mSeekBarGain9
        };

        mEditGains = new EditText[] {
                mEditTextGain0, mEditTextGain1, mEditTextGain2, mEditTextGain3, mEditTextGain4,
                mEditTextGain5, mEditTextGain6, mEditTextGain7, mEditTextGain8, mEditTextGain9
        };

        mEditBws = new EditText[] {
                mEditTextBw0, mEditTextBw1, mEditTextBw2, mEditTextBw3, mEditTextBw4,
                mEditTextBw5, mEditTextBw6, mEditTextBw7, mEditTextBw8, mEditTextBw9
        };

        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mEditSppAddr.getText().toString();

                Boolean result;

                try {
                    result = mAirohaLink.connect(btaddr);
                }catch (IllegalArgumentException e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    result = false;
                }

                mTextConSppResult.setText(result.toString());
            }
        });

        mBtnDisCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.disconnect();
            }
        });


        for(int i = 0; i < mSeekBars.length; i++){

            final EditText editGain = mEditGains[i];
            final EditText editFreq = mEditFreqs[i];

            mSeekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    double value = convertProgressBar(progress);

                    if(Double.valueOf(editFreq.getText().toString())<= UserInputConstraint.FREQ_6DB_GAIN_MAX){
                        value = convertProgressBarLpf(progress); // 2018.09.07 Daniel for freq<=0.25K, -6<=gain<=6
                    }

                    editGain.setText(String.format("%2.2f", value));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        mBtnUpdateRealTimePEQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnUpdateRealTimePEQ.setEnabled(false);

                try{
                    setRealTimeUiData();

                    if (mIsPeqExpEnabled) {
                        mAirohaPeqMgr.startRealTimeUpdate(
                                convertFreqEditText(), convertFreqExpEditText(),
                                convertGainEditText(), convertGainExpEditText(),
                                convertBwEdiText(), convertBwExpEditText()
                        );
                    } else {
                        mAirohaPeqMgr.startRealTimeUpdate(
                                convertFreqEditText(), null,
                                convertGainEditText(), null,
                                convertBwEdiText(), null
                        );
                    }

//                    if(mCkbEnableTwsSync.isChecked()){
//                        if (mIsPeqExpEnabled) {
//                            mAirohaPeqMgr.startRealTimeUpdateTws(
//                                    convertFreqEditText(), convertFreqExpEditText(),
//                                    convertGainEditText(), convertGainExpEditText(),
//                                    convertBwEdiText(), convertBwExpEditText()
//                            );
//                        } else {
//                            mAirohaPeqMgr.startRealTimeUpdateTws(
//                                    convertFreqEditText(), null,
//                                    convertGainEditText(), null,
//                                    convertBwEdiText(), null
//                            );
//                        }
//                    }else {
//                        switch (mUpdateCase) {
//                            case CASE_ONE_SR_MASTER:
//                                if (mIsPeqExpEnabled) {
//                                    mAirohaPeqMgr.startRealTimeUpdate(
//                                            convertFreqEditText(), convertFreqExpEditText(),
//                                            convertGainEditText(), convertGainExpEditText(),
//                                            convertBwEdiText(), convertBwExpEditText()
//                                    );
//                                } else {
//                                    mAirohaPeqMgr.startRealTimeUpdate(
//                                            convertFreqEditText(), null,
//                                            convertGainEditText(), null,
//                                            convertBwEdiText(), null
//                                    );
//                                }
//
//                                break;
//
//                            case CASE_ONE_SR_FOLLOWER:
//                                if (mIsPeqExpEnabled) {
//                                    mAirohaPeqMgr.startRealTimeUpdateFollower(
//                                            convertFreqEditText(), convertFreqExpEditText(),
//                                            convertGainEditText(), convertGainExpEditText(),
//                                            convertBwEdiText(), convertBwExpEditText()
//                                    );
//                                } else {
//                                    mAirohaPeqMgr.startRealTimeUpdateFollower(
//                                            convertFreqEditText(), null,
//                                            convertGainEditText(), null,
//                                            convertBwEdiText(), null
//                                    );
//                                }
//
//                                break;
//
//                        }
//                    }

                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "input exception: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    mBtnUpdateRealTimePEQ.setEnabled(true);
                }
            }
        });


        mBtnLazyForTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextFreq0.setText("0.026");
                mEditTextFreq1.setText("0.05");
                mEditTextFreq2.setText("0.11");
                mEditTextFreq3.setText("0.2");
                mEditTextFreq4.setText("0.4");
                mEditTextFreq5.setText("0.8");
                mEditTextFreq6.setText("1.6");
                mEditTextFreq7.setText("3.2");
                mEditTextFreq8.setText("6.4");
                mEditTextFreq9.setText("12.8");

                // this data can test getFgainCheckGscalValue flow
//                mEditTextFreq0.setText("0.25");
//                mEditTextFreq1.setText("0.28");
//                mEditTextFreq2.setText("0.4");
//                mEditTextFreq3.setText("5");
//                mEditTextFreq4.setText("15");
//
//                mEditTextBw0.setText("0.015");
//                mEditTextBw1.setText("0.015");
//                mEditTextBw2.setText("0.06");
//                mEditTextBw3.setText("6.5");
//                mEditTextBw4.setText("10");

                for(int i = 0; i< mEditGains.length; i++){
                    mEditGains[i].setText("0.01");
                }

                // this data can test getFgainCheckGscalValue flow
//                mEditTextGain0.setText("0.00");
//                mEditTextGain1.setText("0.00");
//                mEditTextGain2.setText("0.00");
//                mEditTextGain3.setText("-10");
//                mEditTextGain4.setText("-12");
            }
        });

        mCkbEnableExp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mIsPeqExpEnabled = isChecked;

                mEditTextFreq5.setEnabled(isChecked);
                mEditTextFreq6.setEnabled(isChecked);
                mEditTextFreq7.setEnabled(isChecked);
                mEditTextFreq8.setEnabled(isChecked);
                mEditTextFreq9.setEnabled(isChecked);
            }
        });

        for(int i = 0; i< mEditFreqs.length; i++){
            final SeekBar seekBar = mSeekBars[i];
            final EditText editBw = mEditBws[i];
            mEditFreqs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int v, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int v, int i1, int i2) {
                    try{
                        double val = Double.valueOf(charSequence.toString());
                        editBw.setText(String.format("%1.2f", val/2));
                    }catch (NumberFormatException e){
                        // don't care
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try{
                        double val = Double.valueOf(editable.toString());
                        int progressMax = getGainProgressMax(val);
                        seekBar.setMax(progressMax);
                        seekBar.setProgress(progressMax/2);
                    }catch (Exception e){

                    }
                }
            });
        }

        mBtnGetMusicSampleRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getMusicSampleRate();
            }
        });

        mCkbEnableTwsSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                }else {

                }
            }
        });

        mBtnCheckEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.checkEQ();
            }
        });

        mBtnSetA2dpPeq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    byte idx = Byte.valueOf(mEditA2dpIdx.getText().toString());
                    byte mode = Byte.valueOf(mEditA2dpMode.getText().toString());

                    mAirohaLink.setA2dpEq(idx, mode);

                }catch (Exception e){
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnUpdatePeqWith3SamplingRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    // 2018.8.17 Daniel: call this can reduce redundant calls
//                    mAirohaPeqMgr.startRealTimeUpdate3SamplingRate();

                    // 2018.8.17 Daniel: just for demo
                    if (mIsPeqExpEnabled) {
                        mAirohaPeqMgr.startRealTimeUpdate3SamplingRate(
                                convertFreqEditText(), convertFreqExpEditText(),
                                convertGainEditText(), convertGainExpEditText(),
                                convertBwEdiText(), convertBwExpEditText()
                        );
                    } else {
                        mAirohaPeqMgr.startRealTimeUpdate3SamplingRate(
                                convertFreqEditText(), null,
                                convertGainEditText(), null,
                                convertBwEdiText(), null
                        );
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mBtnUpdatePeqUiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAirohaPeqMgr.startUpdateUiData(mPeqUserInputStru);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void setRealTimeUiData() {
        if(mIsPeqExpEnabled){
            mPeqUserInputStru.setUserInputExpFreqs(convertFreqExpEditTextToFloats());
            mPeqUserInputStru.setUserInputBwExp(convertBwExpEditTextToFloats());
            mPeqUserInputStru.setUserInputGainExp(convertGainExpEditTextToFloats());
        }else {
            mPeqUserInputStru.setUserInputExpFreqs(null);
            mPeqUserInputStru.setUserInputBwExp(null);
            mPeqUserInputStru.setUserInputGainExp(null);
        }

        mPeqUserInputStru.setUserInputFreqs(convertFreqEditTextToFloats());
        mPeqUserInputStru.setUserInputBws(convertBwEdiTextToFloats());
        mPeqUserInputStru.setUserInputGains(convertGainEditTextToFloats());


//        mAirohaLink.setRealTimeUiData(mPeqUserInputStru.getRaw());

//        mAirohaPeqMgr.setUiData(mPeqUserInputStru);
    }

    private double[] convertFreqEditText(){
        return new double[]{
                Double.valueOf(mEditTextFreq0.getText().toString()),
                Double.valueOf(mEditTextFreq1.getText().toString()),
                Double.valueOf(mEditTextFreq2.getText().toString()),
                Double.valueOf(mEditTextFreq3.getText().toString()),
                Double.valueOf(mEditTextFreq4.getText().toString()),
        };
    }

    private float[] convertFreqEditTextToFloats(){
        return new float[]{
                Float.valueOf(mEditTextFreq0.getText().toString()),
                Float.valueOf(mEditTextFreq1.getText().toString()),
                Float.valueOf(mEditTextFreq2.getText().toString()),
                Float.valueOf(mEditTextFreq3.getText().toString()),
                Float.valueOf(mEditTextFreq4.getText().toString()),
        };
    }

    private double[] convertFreqExpEditText(){
        return new double[]{
                Double.valueOf(mEditTextFreq5.getText().toString()),
                Double.valueOf(mEditTextFreq6.getText().toString()),
                Double.valueOf(mEditTextFreq7.getText().toString()),
                Double.valueOf(mEditTextFreq8.getText().toString()),
                Double.valueOf(mEditTextFreq9.getText().toString()),
        };
    }

    private float[] convertFreqExpEditTextToFloats(){
        return new float[]{
                Float.valueOf(mEditTextFreq5.getText().toString()),
                Float.valueOf(mEditTextFreq6.getText().toString()),
                Float.valueOf(mEditTextFreq7.getText().toString()),
                Float.valueOf(mEditTextFreq8.getText().toString()),
                Float.valueOf(mEditTextFreq9.getText().toString()),
        };
    }

    private double[] convertGainEditText() {
        return new double[]{
                Double.valueOf(mEditTextGain0.getText().toString()),
                Double.valueOf(mEditTextGain1.getText().toString()),
                Double.valueOf(mEditTextGain2.getText().toString()),
                Double.valueOf(mEditTextGain3.getText().toString()),
                Double.valueOf(mEditTextGain4.getText().toString()),
        };
    }

    private float[] convertGainEditTextToFloats() {
        return new float[]{
                Float.valueOf(mEditTextGain0.getText().toString()),
                Float.valueOf(mEditTextGain1.getText().toString()),
                Float.valueOf(mEditTextGain2.getText().toString()),
                Float.valueOf(mEditTextGain3.getText().toString()),
                Float.valueOf(mEditTextGain4.getText().toString()),
        };
    }

    private double[] convertGainExpEditText() {
        return new double[]{
                Double.valueOf(mEditTextGain5.getText().toString()),
                Double.valueOf(mEditTextGain6.getText().toString()),
                Double.valueOf(mEditTextGain7.getText().toString()),
                Double.valueOf(mEditTextGain8.getText().toString()),
                Double.valueOf(mEditTextGain9.getText().toString()),
        };
    }

    private float[] convertGainExpEditTextToFloats() {
        return new float[]{
                Float.valueOf(mEditTextGain5.getText().toString()),
                Float.valueOf(mEditTextGain6.getText().toString()),
                Float.valueOf(mEditTextGain7.getText().toString()),
                Float.valueOf(mEditTextGain8.getText().toString()),
                Float.valueOf(mEditTextGain9.getText().toString()),
        };
    }
    
    private double[] convertBwEdiText(){
        return new double[]{
                Double.valueOf(mEditTextBw0.getText().toString()),
                Double.valueOf(mEditTextBw1.getText().toString()),
                Double.valueOf(mEditTextBw2.getText().toString()),
                Double.valueOf(mEditTextBw3.getText().toString()),
                Double.valueOf(mEditTextBw4.getText().toString()),
        };
    }

    private float[] convertBwEdiTextToFloats(){
        return new float[]{
                Float.valueOf(mEditTextBw0.getText().toString()),
                Float.valueOf(mEditTextBw1.getText().toString()),
                Float.valueOf(mEditTextBw2.getText().toString()),
                Float.valueOf(mEditTextBw3.getText().toString()),
                Float.valueOf(mEditTextBw4.getText().toString()),
        };
    }
    
    private double[] convertBwExpEditText(){
        return new double[]{
                Double.valueOf(mEditTextBw5.getText().toString()),
                Double.valueOf(mEditTextBw6.getText().toString()),
                Double.valueOf(mEditTextBw7.getText().toString()),
                Double.valueOf(mEditTextBw8.getText().toString()),
                Double.valueOf(mEditTextBw9.getText().toString()),
        };
    }

    private float[] convertBwExpEditTextToFloats(){
        return new float[]{
                Float.valueOf(mEditTextBw5.getText().toString()),
                Float.valueOf(mEditTextBw6.getText().toString()),
                Float.valueOf(mEditTextBw7.getText().toString()),
                Float.valueOf(mEditTextBw8.getText().toString()),
                Float.valueOf(mEditTextBw9.getText().toString()),
        };
    }

    private double convertProgressBar(int progress){
        return UserInputConstraint.GAIN_MIN + progress * PROGRESS_STEP;
    }

    private double convertProgressBarLpf(int progress) {
        return UserInputConstraint.LPF_GAIN_MIN + progress * PROGRESS_STEP;
    }

    private final AirohaPeqMgr.OnUiListener mAirohaPeqUiListener = new AirohaPeqMgr.OnUiListener() {
        @Override
        public void OnRealTimeUpdated(final boolean success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(success){
                        Toast.makeText(MainActivity.this, "Real time PEQ updated", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Real time PEQ Failed", Toast.LENGTH_SHORT).show();
                    }
                    mBtnUpdateRealTimePEQ.setEnabled(true);
                }
            });

//            if(success){
//                setRealTimeUiData();
//            }
        }

        @Override
        public void OnRelTimeInputRejected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Input not accepted by the algo.", Toast.LENGTH_SHORT).show();
                    mBtnUpdateRealTimePEQ.setEnabled(true);
                }
            });
        }

        @Override
        public void OnFollowerExisting(final boolean existing) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRadioBtnFollower.setEnabled(existing);
                    mCkbEnableTwsSync.setEnabled(existing);

//                    mRadioBtnOneSamplingRateFollower.setEnabled(existing);
//                    mRadioBtnThreeSamplingRateFollower.setEnabled(existing);
//                    mRadioBtnOneSamplingRateTws.setEnabled(existing);
//                    mRadioBtnThreeSamplingRateTws.setEnabled(existing);
                }
            });
        }
    };


    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();
        disconnectProfile();

        super.onDestroy();
    }

    private void requestExternalStoragePermission(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    0
            );
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }
    }

    private int covertToProgress(double d){
        return (int) ((d - UserInputConstraint.GAIN_MIN)/ PROGRESS_STEP);
    }

    private int convertToProgressLpf(double d) {
        return (int) ((d- UserInputConstraint.LPF_GAIN_MIN) / PROGRESS_STEP);
    }

    private int getGainProgressMax(double inputFreq){

        int max = UserInputConstraint.getGainDbRangeMax(inputFreq);
        int min = UserInputConstraint.getGainDbRangeMin(inputFreq);

        return (int)( (max-min)/PROGRESS_STEP);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioBtnMaster:
                if (checked) {
                    mTransportTarget = TransportTarget.Master;

                    Log.d(TAG, "operating with:" + mTransportTarget.toString());

                    mAirohaLink.getRealTimeUiData();

                    mUpdateCase = CASE_ONE_SR_MASTER;
                }
                break;
            case R.id.radioBtnFollower:
                if (checked) {
                    mTransportTarget = TransportTarget.Follower;
                    Log.d(TAG, "operating with:" + mTransportTarget.toString());

                    mAirohaLink.getRealTimeUiDataFollower();

                    mUpdateCase = CASE_ONE_SR_FOLLOWER;
                }
                break;
        }
    }

    private void updatePairedList() {
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        mPairedListView = (ListView) findViewById(R.id.list_devices);
        mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
        mPairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView) view).getText().toString();
                Log.d(TAG, "clicked:" + info);
                String addr = info.split("\n")[1];
                Log.d(TAG, addr);

                mEditSppAddr.setText(addr);
            }
        });
        // Remove all element from the list
        mPairedDevicesArrayAdapter.clear();
        // Get a set of currently paired devices
        BluetoothAdapter mBlurAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Log.e("DeviceActivity ",
                    "Device not founds");
            mPairedDevicesArrayAdapter.add("No Device");
            return;
        }

        for (BluetoothDevice device : pairedDevices) {
            Log.d("DeviceActivity", "Device : address : " + device.getAddress() + " name :"
                    + device.getName());

            mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    + device.getAddress());
        }
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothA2dp mA2dpProfileProxy;

    private void connectProfile(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothAdapter.getProfileProxy(mCtx, mServiceListener, BluetoothProfile.A2DP);
    }

    private void disconnectProfile(){
        if(mA2dpProfileProxy != null){
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dpProfileProxy);
        }
    }

    private BluetoothProfile.ServiceListener mServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if(profile == BluetoothProfile.A2DP){
                mA2dpProfileProxy = (BluetoothA2dp) proxy;

                try {
                    final BluetoothDevice connectedDevice = mA2dpProfileProxy.getConnectedDevices().get(0);

                    Log.d(TAG, "a2dp connected device: " + connectedDevice.getName() + connectedDevice.getUuids().toString());
                    mEditSppAddr.setText(connectedDevice.getAddress());

                    ParcelUuid[] parcelUuids = connectedDevice.getUuids();

                    for (ParcelUuid parcelUuid : parcelUuids) {
                        Log.d(TAG, parcelUuid.toString());

                        if (parcelUuid.getUuid().compareTo(AirohaLink.UUID_AIROHA_SPP) == 0) {
                            Log.d(TAG, "found Airoha device");

                            Toast.makeText(mCtx, "Found Airoha Device:" + connectedDevice.getName(), Toast.LENGTH_LONG).show();


                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mAirohaLink.connect(connectedDevice.getAddress());
                                }
                            }).start();
                            ;
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };
}

