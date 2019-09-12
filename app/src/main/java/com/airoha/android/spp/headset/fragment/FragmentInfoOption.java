package com.airoha.android.spp.headset.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.airoha.android.lib.mmi.cmd.AirohaMMICmd;
import com.airoha.android.lib.mmi.cmd.KeyCode;
import com.airoha.android.lib.mmi.cmd.OCF;
import com.airoha.android.lib.mmi.cmd.OGF;
import com.airoha.android.lib.mmi.cmd.Resp;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.spp.headset.Intents.UiIntents;
import com.airoha.android.spp.headset.R;
import com.airoha.android.spp.headset.bluetooth.AirohaEngine;
import com.airoha.android.spp.headset.service.ConnService;

public class FragmentInfoOption extends BaseFragment {

    private static final String TAG = FragmentInfoOption.class.getSimpleName();
    private View mFragmentView;

    private ToggleButton mToggleVP, mToggleCaller, mToggleAT;
    private EditText mEditTextName;
    private TextView mTextFwVerMaster;
    private TextView mTextFwVerFollower;
    private TextView mTextBatteryMaster;
    private TextView mTextBatteryFollower;

    private Button mBtnGetFwVerMaster;
    private Button mBtnGetFwVerFollower;
    private Button mBtnGetBatteryMaster;
    private Button mBtnGetBatteryFollower;

    // Channel
    private Button mBtnGetChannel;
    private Button mBtnGetChannelFr;
    private TextView mTextGetChannelResult;
    private TextView mTextGetChannelFrResult;

    private Button mBtnRoleSwitch;

    private TextView mTextViewA2dp, mTextViewLineIn, mTextViewA2dpNum, mTextViewLineInNum;
    private TextView mTextViewLangIdx;
    private SeekBar mSeekBar, mSeekBarMasterAT, mSeekBarSlaveAT;
    private Button mBtnChangeLang;
    private Button mBtnPEQSwitch;
    private Button mBtnFindMyAceesory;

    private RelativeLayout mLayoutMasterAT, mLayoutSlaveAT;

    private Context mCtx;

    private String mdevicename;

    private final Handler infomHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD));
//                    sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD_FR));
                    sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD));
//                    sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD_FR));
                    sendCommandToService(AirohaMMICmd.GET_DEVICE_NAME);
                    sendCommandToService(AirohaMMICmd.GET_VOICE_PROMPT);
                    sendCommandToService(AirohaMMICmd.GET_VOLUME);
                    sendCommandToService(AirohaMMICmd.GET_PEQ);

                    if(mToggleCaller.isChecked()) {
                        sendCommandToService(AirohaMMICmd.CALLER_NAME_ON);
                    }else {
                        sendCommandToService(AirohaMMICmd.CALLER_NAME_OFF);
                    }

                    sendCommandToService(AirohaMMICmd.GET_MASTER_AT_STATUS);
                    sendCommandToService(AirohaMMICmd.GET_SLAVE_AT_STATUS);

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private final BroadcastReceiver mMMIPacketReceiever= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String tag = "mmi packet receiver";

            Log.d(tag, action);


            if(action.equals(UiIntents.ACTION_GET_BATTERY_IND) || action.equals(UiIntents.ACTION_REPORT_BATTERY_STATUS)) {
                byte status = intent.getByteExtra(UiIntents.EXTRA_BATTERY_LEVEL, (byte) 0);

                mTextBatteryMaster.setText(String.valueOf(status));
            }

            if(action.equals(UiIntents.ACTION_GET_BATTERY_FR_IND) || action.equals(UiIntents.ACTION_REPORT_BATTERY_FR_STATUS)) {
                byte status = intent.getByteExtra(UiIntents.EXTRA_BATTERY_LEVEL, (byte) 0);

                mTextBatteryFollower.setText(String.valueOf(status));
            }

            if(action.equals(UiIntents.ACTION_GET_VOLUME_IND)) {
                byte vol = intent.getByteExtra(UiIntents.EXTRA_VOLUME, (byte) 0);
                setVolBar(vol);
            }

            if(action.equals(UiIntents.ACTION_VP_ENABLE_RESP) || action.equals(UiIntents.ACTION_VP_DISABLE_RESP)) {
                unlockToggleVP();
            }

            if(action.equals(UiIntents.ACTION_VP_NEXT_RESP)) {
                unlockBtnVPNext();
            }


            if(action.equals(UiIntents.ACTION_REPORT_VP_STATUS)) {
                byte status = intent.getByteExtra(UiIntents.EXTRA_VP_STATUS, (byte)0);
                setVPtogglebutton(status);
            }

            if(action.equals(UiIntents.ACTION_REPORT_VP_LANG)) {
                byte lang = intent.getByteExtra(UiIntents.EXTRA_REPORT_VP_LANG, (byte)0);
                setVPLangIdx(String.valueOf(lang));
            }

            if(action.equals(UiIntents.ACTION_GET_DEVICENAME)) {
                String devicename = intent.getStringExtra(UiIntents.EXTRA_DEVICENAME);
                mEditTextName.setText(devicename);
            }

            if(action.equals(UiIntents.ACTION_REPORT_PEQ_A2DP_CHANGE)) {
                String a2dpidx = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_A2DP_IDX, Resp.Fail));
                setA2DPIdx(a2dpidx);
            }

            if(action.equals(UiIntents.ACTION_REPORT_PEQ_AUX_CHANGE)) {
                String auxidx = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_AUX_IDX, Resp.Fail));
                setAuxIdx(auxidx);
            }

            if(action.equals(UiIntents.ACTION_GET_VP_IND)) {
                byte vpEnabled = intent.getByteExtra(UiIntents.EXTRA_VP_ENABLED, Resp.Fail);
                setVPtogglebutton(vpEnabled);
            }

            if(action.equals(UiIntents.ACTION_GET_PEQ_IND)) {
                String a2dpIdx = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_A2DP_IDX, Resp.Fail));
                String auxIdx = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_AUX_IDX, Resp.Fail));
                String a2dpCount = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_A2DP_COUNT, Resp.Fail));
                String auxCount = String.valueOf(intent.getByteExtra(UiIntents.EXTRA_PEQ_AUX_COUNT, Resp.Fail));
                setA2DPIdx(a2dpIdx);
                setA2DPCount(a2dpCount);
                setAuxIdx(auxIdx);
                setAuxCount(auxCount);
            }

            if(action.equals(UiIntents.ACTION_GET_FW_VERSION)){
                String fwStr = intent.getStringExtra(UiIntents.EXTRA_FW_STRING);
                mTextFwVerMaster.setText(fwStr);
            }

            if(action.equals(UiIntents.ACTION_GET_FW_VERSION_FR)) {
                String fwStr = intent.getStringExtra(UiIntents.EXTRA_FW_STRING);
                mTextFwVerFollower.setText(fwStr);
            }

            if (action.equals(UiIntents.ACTION_TOGGLE_AT_RESP)) {
                sendCommandToService(AirohaMMICmd.GET_MASTER_AT_STATUS);
                sendCommandToService(AirohaMMICmd.GET_SLAVE_AT_STATUS);
            }

            if (action.equals(UiIntents.ACTION_GET_MASTER_AT_STATUS_RESP)) {
                byte status = intent.getByteExtra(UiIntents.EXTRA_MASTER_AT_STATUS, (byte) 0);
                if (status == 0) {
                    mToggleAT.setChecked(true);
                    sendCommandToService(AirohaMMICmd.GET_MASTER_AT_GAIN);
                    mLayoutMasterAT.setVisibility(View.VISIBLE);
                } else {
                    mToggleAT.setChecked(false);
                    mLayoutMasterAT.setEnabled(false);
                    mLayoutMasterAT.setVisibility(View.GONE);
                }
                mLayoutMasterAT.invalidate();
                unlockToggleAT();
            }

            if (action.equals(UiIntents.ACTION_GET_SLAVE_AT_STATUS_RESP)) {
                byte status = intent.getByteExtra(UiIntents.EXTRA_SLAVE_AT_STATUS, (byte) 0);
                if (status == 0) {
                    sendCommandToService(AirohaMMICmd.GET_SLAVE_AT_GAIN);
                    mLayoutSlaveAT.setVisibility(View.VISIBLE);
                } else {
                    mLayoutSlaveAT.setEnabled(false);
                    mLayoutSlaveAT.setVisibility(View.GONE);
                }
                mLayoutSlaveAT.invalidate();
            }

            if (action.equals(UiIntents.ACTION_SET_MASTER_AT_GAIN_RESP)) {
                byte result = intent.getByteExtra(UiIntents.EXTRA_SET_MASTER_AT_GAIN_RESULT, (byte) 0);
                if (result != 0) {
                    Toast.makeText(getActivity(), "Failed to set master AT gain!", Toast.LENGTH_LONG);
                }
            }

            if (action.equals(UiIntents.ACTION_SET_SLAVE_AT_GAIN_RESP)) {
                byte result = intent.getByteExtra(UiIntents.EXTRA_SET_SLAVE_AT_GAIN_RESULT, (byte) 0);
                if (result != 0) {
                    Toast.makeText(getActivity(), "Failed to set slave AT gain!", Toast.LENGTH_LONG);
                }
            }

            if (action.equals(UiIntents.ACTION_GET_MASTER_AT_GAIN_RESP)) {
                byte gain = intent.getByteExtra(UiIntents.EXTRA_MASTER_AT_GAIN, (byte) 0);
                setATGainBar(mSeekBarMasterAT, gain);
                mLayoutMasterAT.setEnabled(true);
            }

            if (action.equals(UiIntents.ACTION_GET_SLAVE_AT_GAIN_RESP)) {
                byte gain = intent.getByteExtra(UiIntents.EXTRA_SLAVE_AT_GAIN, (byte) 0);
                setATGainBar(mSeekBarSlaveAT, gain);
                mLayoutSlaveAT.setEnabled(true);
            }

            if(action.equals(UiIntents.ACTION_GET_CHANNEL)) {
                byte bLR = intent.getByteExtra(UiIntents.EXTRA_CHANNEL_LR, (byte) 0);
                mTextGetChannelResult.setText(String.valueOf(bLR));
            }

            if(action.equals(UiIntents.ACTION_GET_CHANNEL_FR)) {
                byte bLR = intent.getByteExtra(UiIntents.EXTRA_CHANNEL_LR, (byte) 0);
                mTextGetChannelFrResult.setText(String.valueOf(bLR));
            }

            if(action.equals(UiIntents.ACTION_SET_CALLERNAME)) {
                unlockToggleCaller();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        mFragmentView = inflater.inflate(R.layout.fragment_info, container, false);

        initUIComponents();
        initPreferences();
        sendDelayedCmdToStart();

        registerIntentFilters();

        this.getActivity();
        return mFragmentView;
    }

    private void registerIntentFilters(){

        Log.d(TAG, "register intent filters");

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UiIntents.ACTION_KEY_PEQ_MODE_CHANGE_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_BATTERY_IND);
        intentFilter.addAction(UiIntents.ACTION_GET_BATTERY_FR_IND);
        intentFilter.addAction(UiIntents.ACTION_GET_TWS_SLAVE_BATTERY_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_TWS_SLAVE_BATTERY_IND);
        intentFilter.addAction(UiIntents.ACTION_REPORT_BATTERY_STATUS);
        intentFilter.addAction(UiIntents.ACTION_REPORT_BATTERY_FR_STATUS);
        intentFilter.addAction(UiIntents.ACTION_GET_VOLUME_IND);
        intentFilter.addAction(UiIntents.ACTION_VP_ENABLE_RESP);
        intentFilter.addAction(UiIntents.ACTION_VP_DISABLE_RESP);
        intentFilter.addAction(UiIntents.ACTION_VP_NEXT_RESP);
        intentFilter.addAction(UiIntents.ACTION_REPORT_VP_STATUS);
        intentFilter.addAction(UiIntents.ACTION_REPORT_VP_LANG);
        intentFilter.addAction(UiIntents.ACTION_GET_DEVICENAME);
        intentFilter.addAction(UiIntents.ACTION_REPORT_PEQ_A2DP_CHANGE);
        intentFilter.addAction(UiIntents.ACTION_REPORT_PEQ_AUX_CHANGE);
        intentFilter.addAction(UiIntents.ACTION_GET_VP_IND);
        intentFilter.addAction(UiIntents.ACTION_GET_PEQ_IND);
        intentFilter.addAction(UiIntents.ACTION_GET_FW_VERSION);
        intentFilter.addAction(UiIntents.ACTION_GET_FW_VERSION_FR);

        intentFilter.addAction(UiIntents.ACTION_TOGGLE_AT_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_MASTER_AT_STATUS_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_SLAVE_AT_STATUS_RESP);
        intentFilter.addAction(UiIntents.ACTION_SET_MASTER_AT_GAIN_RESP);
        intentFilter.addAction(UiIntents.ACTION_SET_SLAVE_AT_GAIN_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_MASTER_AT_GAIN_RESP);
        intentFilter.addAction(UiIntents.ACTION_GET_SLAVE_AT_GAIN_RESP);

        intentFilter.addAction(UiIntents.ACTION_GET_CHANNEL);
        intentFilter.addAction(UiIntents.ACTION_GET_CHANNEL_FR);

        intentFilter.addAction(UiIntents.ACTION_SET_CALLERNAME);

        mCtx = this.getActivity();
        mCtx.registerReceiver(mMMIPacketReceiever, intentFilter);

        // 2016.11.11 Daniel: should be using AirohaLink.
    }

    public void sendDelayedCmdToStart() {

        Log.d("FragmentInfoOption", "sendDelyedCmdToStart");
        mdevicename = AirohaEngine.getDeviceName();
        mEditTextName.setText(mdevicename);

        Message message = new Message();
        message.what = 0;
        infomHandler.sendMessageDelayed(message, 2000);
    }


    private void initUIComponents() {

        Button changenamebutton = (Button) mFragmentView
                .findViewById(R.id.changenamebutton);
        changenamebutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeviceName();
            }
        });


        mBtnPEQSwitch = (Button) mFragmentView.findViewById(R.id.peqbutton);
        mBtnPEQSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPEQ();
            }
        });

        mBtnChangeLang = (Button) mFragmentView.findViewById(R.id.changelang);
        mBtnChangeLang.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToNextVPLang();
            }
        });

        mBtnFindMyAceesory = (Button) mFragmentView.findViewById(R.id.findmydevice);
        mBtnFindMyAceesory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findmyaccessory();
            }
        });


        setVPClickListener();
        setCallerClickLisener();
        setATClickListener();

        mEditTextName = (EditText) mFragmentView.findViewById(R.id.nameedit);

        mTextViewA2dp = (TextView) mFragmentView.findViewById(R.id.a2dptextview);
        mTextViewLineIn = (TextView) mFragmentView.findViewById(R.id.lineintextview);
        mTextViewA2dpNum = (TextView) mFragmentView.findViewById(R.id.a2dpNumtextview);
        mTextViewLineInNum = (TextView) mFragmentView.findViewById(R.id.lineinNumtextview);


        mTextFwVerMaster = (TextView) mFragmentView.findViewById(R.id.textViewGetFwVersionResult);
        mTextFwVerFollower = (TextView) mFragmentView.findViewById(R.id.textViewGetFwVersionFrResult);

        mTextBatteryMaster = (TextView) mFragmentView.findViewById(R.id.textViewGetBatResult);
        mTextBatteryFollower = (TextView) mFragmentView.findViewById(R.id.textViewGetBatFrResult);

        mTextGetChannelResult = (TextView) mFragmentView.findViewById(R.id.textViewGetChannelResult);
        mTextGetChannelFrResult = (TextView) mFragmentView.findViewById(R.id.textViewGetChannelFrResult);

        mTextViewLangIdx = (TextView) mFragmentView.findViewById(R.id.tvLangIdx);

        mSeekBar = (SeekBar) mFragmentView.findViewById(R.id.seekBar1);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setVolumebySeekbar(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }
        });


        mLayoutMasterAT = (RelativeLayout) mFragmentView.findViewById(R.id.layout_AT_master);
        mLayoutSlaveAT = (RelativeLayout) mFragmentView.findViewById(R.id.layout_AT_slave);

        mSeekBarMasterAT = (SeekBar) mFragmentView.findViewById(R.id.seekBar_AT_master);
        mSeekBarMasterAT.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setATGainbySeekbar(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }
        });

        mSeekBarSlaveAT = (SeekBar) mFragmentView.findViewById(R.id.seekBar_AT_slave);
        mSeekBarSlaveAT.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setATGainbySeekbar(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }
        });

        mBtnGetFwVerMaster = (Button) mFragmentView.findViewById(R.id.buttonGetFwVersion);
        mBtnGetFwVerMaster.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD));
            }
        });

        mBtnGetFwVerFollower = (Button) mFragmentView.findViewById(R.id.buttonGetFwVersionFr);
        mBtnGetFwVerFollower.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD_FR));
            }
        });

        mBtnGetBatteryMaster = (Button) mFragmentView.findViewById(R.id.buttonGetBattery);
        mBtnGetBatteryMaster.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD));
            }
        });

        mBtnGetBatteryFollower = (Button) mFragmentView.findViewById(R.id.buttonGetBatteryFr);
        mBtnGetBatteryFollower.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD_FR));
            }
        });

        mBtnRoleSwitch = (Button) mFragmentView.findViewById(R.id.buttonRoleSwitch);
        mBtnRoleSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.ROLE_SWITCH);
            }
        });

        mBtnGetChannel = (Button) mFragmentView.findViewById(R.id.buttonGetChannel);
        mBtnGetChannel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_CHANNEL_INFO, OGF.AIROHA_MMI_CMD));
            }
        });

        mBtnGetChannelFr = (Button) mFragmentView.findViewById(R.id.buttonGetChannelFr);
        mBtnGetChannelFr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(AirohaMMICmd.generateCmd(OCF.GET_CHANNEL_INFO, OGF.AIROHA_MMI_CMD_FR));
            }
        });
    }

    private void setVolumebySeekbar(SeekBar seekBar) {
        byte[] volPreCmd;
        volPreCmd = AirohaMMICmd.SET_VOLUME;

        int seekProgress = seekBar.getProgress();

        Log.d("Vol", "progress:" + seekProgress);

        byte[] vol = new byte[1];
        vol[0]= Converter.intToByte(seekBar.getProgress());

        sendCommandToService(AirohaMMICmd.combineComplexCmd(volPreCmd, vol));
    }

    /**
     * 語音切換中英
     *
     */
    private void switchToNextVPLang() {
        sendCommandToService(AirohaMMICmd.VOICE_PROMPT_LANG_CHANGE_NEXT);
        lockBtnVPNext();
    }

    private void lockBtnVPNext(){
        Log.d("VP Next", "lock");
        mBtnChangeLang.setEnabled(false);
    }

    private void unlockBtnVPNext(){
        Log.d("VP Next", "unlock");
        mBtnChangeLang.setEnabled(true);
    }

    /**
     * peq切替
     *
     */
    private void switchPEQ() {
        sendCommandToService(AirohaMMICmd.KEY_PEQ_MODE_CHANGE);
    }


    private void findmyaccessory() {
        sendCommandToService(AirohaMMICmd.FIND_MY_ACCESSORY);
    }


    private void setDeviceName() {
        hideKeyboard();

        String newname = mEditTextName.getText().toString();
        if (null == newname) {
            Toast.makeText(getActivity(), "Please enter new name ",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            byte[] pre_cmd;
            pre_cmd = AirohaMMICmd.WRITE_DEVICE_NAME;
            byte[] post_cmd_name = Converter.stringtoascii(newname);

            //            01 00 FC LL A2 48 NN NN NN NN NN NN
            //            LL: name length + 2
            //            NN: name
            pre_cmd[3] = Byte.parseByte(post_cmd_name.length + 2 + "");

            byte[] destination = new byte[pre_cmd.length + post_cmd_name.length];

            System.arraycopy(pre_cmd, 0, destination, 0, pre_cmd.length);
            System.arraycopy(post_cmd_name, 0, destination, pre_cmd.length,
                    post_cmd_name.length);

            dialoginitialRename(destination);
        }

    }

    private void setVPLangIdx(String str) {
        mTextViewLangIdx.setText(str + "/");
    }

    private void setVolBar(byte bVol) {
        int percent = (int) bVol;

        mSeekBar.setProgress(percent);
    }

    private void setA2DPIdx(String value) {
        mTextViewA2dp.setText(getString(R.string.a2dpMode) + value + "/");
    }

    private void setA2DPCount(String num) {
        mTextViewA2dpNum.setText(num);
    }

    private void setAuxIdx(String value) {
        mTextViewLineIn.setText(getString(R.string.lineinMode) + value + "/");
    }

    private void setAuxCount(String num) {
        mTextViewLineInNum.setText(num);
    }

    private void setVPtogglebutton(byte b) {
        if( b == 0x00) {
            mToggleVP.setChecked(false);
        } else if (b == 0x01) {
            mToggleVP.setChecked(true);
        }
    }

    private void setCallerClickLisener(){
        mToggleCaller = (ToggleButton) mFragmentView.findViewById(R.id.toggleCaller);

        mToggleCaller.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton toggleButton = (ToggleButton)v;
                boolean isChecked = toggleButton.isChecked();

                if(isChecked) {
                    sendCommandToService(AirohaMMICmd.CALLER_NAME_ON);
                    lockToggleCaller();
                }else {
                    sendCommandToService(AirohaMMICmd.CALLER_NAME_OFF);
                    lockToggleCaller();
                }
            }
        });

    }

    private void setVPClickListener() {
        mToggleVP = (ToggleButton) mFragmentView.findViewById(R.id.toggleVP);

        mToggleVP.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton tb = (ToggleButton) v;
                boolean isChecked = tb.isChecked();

                if (isChecked) {
                    sendCommandToService(AirohaMMICmd.VOICE_PROMPT_ENABLE);
                    // Daniel: block for FW memory leak
                    lockToggleVP();
                } else {
                    sendCommandToService(AirohaMMICmd.VOICE_PROMPT_DISABLE);
                    // Daniel: block for FW memory leak
                    lockToggleVP();
                }
            }
        });
    }

    private void setATClickListener() {
        mToggleAT = (ToggleButton) mFragmentView.findViewById(R.id.toggleAT);

        mToggleAT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mToggleAT.isChecked()) {
                    mLayoutMasterAT.setVisibility(View.GONE);
                    mLayoutSlaveAT.setVisibility(View.GONE);
                }
                Intent intent = new Intent(ConnService.ACTION_AT_TOGGLE);
                getActivity().sendBroadcast(intent);
                lockToggleAT();
            }
        });
    }

    /**
     *  Daniel: block for FW memory leak
     */
    private void lockToggleVP() {
        Log.d("VP on/off", "lock");
        mToggleVP.setEnabled(false);
    }

    private void unlockToggleVP(){
        Log.d("VP on/off", "unlock");
        mToggleVP.setEnabled(true);
    }

    private void lockToggleCaller() {
        Log.d("Caller on/off", "lock");
        mToggleCaller.setEnabled(false);
    }

    private void unlockToggleCaller(){
        Log.d("Caller on/off", "unlock");
        mToggleCaller.setEnabled(true);
    }

    private void initPreferences(){
        SharedPreferences preference = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        mToggleCaller.setChecked(preference.getBoolean(getString(R.string.PrefChkCallerName), false));
    }

    private void savePreferences(){
        SharedPreferences preference = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(getString(R.string.PrefChkCallerName), mToggleCaller.isChecked());
        editor.commit();
    }

    @Override
    public void onDestroy() {
        try {
            mCtx.unregisterReceiver(mMMIPacketReceiever);
            Log.d(TAG, "unregistered intent filter");
            savePreferences();
        }catch (Exception e){
            Log.d("FragmaengInfo", e.getMessage());
        }
        super.onDestroy();
    }


    /**
     * 詢問是否要改名字
     *
     * @param new_name
     */
    private void dialoginitialRename(final byte[] new_name) {
        AlertDialog ad = new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.deviceRename))
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Modify Device Name")
                .setNeutralButton(
                        getActivity().getResources()
                                .getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                sendCommandToService(new_name);
                            }
                        })
                .setPositiveButton(
                        getActivity().getResources().getString(R.string.cancel),
                        null).setCancelable(false).create();
        if (!ad.isShowing()) {
            ad.show();
        } else {
            ad.cancel();
        }
    }

    private void getBattery() {
        sendCommandToService(AirohaMMICmd.GET_BATTERY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


    private void lockToggleAT() {
        Log.d("AT on/off", "lock");
        mToggleAT.setEnabled(false);
        mToggleAT.invalidate();
    }

    private void unlockToggleAT() {
        Log.d("AT on/off", "unlock");
        mToggleAT.setEnabled(true);
        mToggleAT.invalidate();
    }

    private void setATGainBar(SeekBar seekBar, byte bGain) {
        int percent = (int) bGain;

        seekBar.setProgress(percent);
    }

    private void setATGainbySeekbar(SeekBar seekBar) {
        int seekProgress = seekBar.getProgress();
        byte[] gainPreCmd;
        if (seekBar.getId() == R.id.seekBar_AT_slave) {
            gainPreCmd = AirohaMMICmd.SET_SLAVE_AT_GAIN;
            Log.d("AT Slave Gain", "progress:" + seekProgress);
        } else {
            gainPreCmd = AirohaMMICmd.SET_MASTER_AT_GAIN;
            Log.d("AT Master Gain", "progress:" + seekProgress);
        }

        byte[] gain = new byte[1];
        gain[0] = Converter.intToByte(seekBar.getProgress());

        sendCommandToService(AirohaMMICmd.combineComplexCmd(gainPreCmd, gain));
    }
}