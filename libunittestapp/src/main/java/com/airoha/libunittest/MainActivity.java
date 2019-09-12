package com.airoha.libunittest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airoha.android.lib.flashDescriptor.FlashDescriptor;
import com.airoha.android.lib.flashDescriptor.OnFlashDescriptorListener;
import com.airoha.android.lib.flashDescriptor.SectorIndex;
import com.airoha.android.lib.flashDescriptor.SectorTable;
import com.airoha.android.lib.mmi.OnAirohaMmiEventListener;
import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.OnAirohaOtaEventListener;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.io.IOException;

import airoha.com.libunittestapp.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Airoha_UT";

    private AirohaLink mAirohaLink = null;

    private ProgressDialog pd;
    private AlertDialog.Builder dialog;
    private AirohaOtaFlowMgr otaMgr = null;


    // Connect
    @Bind(R.id.editTextSppAddr)
    protected EditText mEditSppAddr;
    @Bind(R.id.buttonConSpp)
    protected Button mBtnConSpp;
//    @Bind(R.id.buttonConBle)
//    protected Button mBtnConBle;
    @Bind(R.id.buttonDisConSPP)
    protected Button mBtnDisConSpp;
    @Bind(R.id.textViewConSppResult)
    protected TextView mTextConSppResult;
    @Bind(R.id.textViewConSppState)
    protected TextView mTextConSppState;

    // Channel
    @Bind(R.id.buttonGetChannel)
    protected Button mBtnGetChannel;
    @Bind(R.id.textViewGetChannelResp)
    protected TextView mTextGetChannelResp;
    @Bind(R.id.textViewGetChannelResult)
    protected TextView mTextGetChannelResult;

    @Bind(R.id.buttonGetChannelFr)
    protected Button mBtnGetChannelFr;
    @Bind(R.id.textViewGetChannelFrResp)
    protected TextView mTextGetChannelFrResp;
    @Bind(R.id.textViewGetChannelFrResult)
    protected TextView mTextGetChannelFrResult;


    // Battery
    @Bind(R.id.buttonGetBattery)
    protected Button mBtnGetBattery;
    @Bind(R.id.textViewGetBatResult)
    protected TextView mTextGetBatResult;
    @Bind(R.id.textViewGetBatResp)
    protected TextView mTextGetBatResp;

    @Bind(R.id.buttonGetBatteryFr)
    protected Button mBtnGetBatteryFr;
    @Bind(R.id.textViewGetBatFrResult)
    protected TextView mTextGetBatFrResult;
    @Bind(R.id.textViewGetBatFrResp)
    protected TextView mTextGetBatFrResp;

    // Fw Version
    @Bind(R.id.buttonGetFwVersion)
    protected  Button mBtnGetFwVersion;
    @Bind(R.id.textViewGetFwVersionResp)
    protected  TextView mTextGetFwVersionResp;
    @Bind(R.id.textViewGetFwVersionResult)
    protected  TextView mTextGetFwVersionResult;

    @Bind(R.id.buttonGetFwVersionFr)
    protected Button mBtnGetFwVersionFr;
    @Bind(R.id.textViewGetFwVersionFrResp)
    protected TextView mTextGetFwVersionFrResp;
    @Bind(R.id.textViewGetFwVersionFrResult)
    protected TextView mTextGetFwVersionFrResult;


    @Bind(R.id.buttonGetTwsSlaveFwVersion)
    protected  Button mBtnGetTwsSlaveFwVersion;
    @Bind(R.id.textViewGetTwsSlaveFwVersionResp)
    protected  TextView mTextGetTwsSlavbeFwVersionResp;
    @Bind(R.id.textViewGetTwsSlaveFwVersionResult)
    protected  TextView mTextGetTwsSlaveFwVersionResult;


    @Bind(R.id.buttonFindMyAccessory)
    protected Button mBtnFindMyAccessory;

    @Bind(R.id.textViewFindMyAsyResp)
    protected TextView mTextFindMyAsyResp;

    @Bind(R.id.buttonGetVolume)
    protected Button mBtnGetVolume;

    @Bind(R.id.textViewGetVolResult)
    protected TextView mTextVolResult;

    @Bind(R.id.textViewGetVolResp)
    protected TextView mTextGetVolResp;

    @Bind(R.id.buttonEnableVP)
    protected Button mBtnEnableVP;

    @Bind(R.id.textViewEnablVPResp)
    protected TextView mTextEnableVPResult;

    @Bind(R.id.buttonDisableVP)
    protected Button mBtnDisableVP;

    @Bind(R.id.textViewDisablVPResp)
    protected TextView mTextDisableVPResult;

    @Bind(R.id.buttonEnableCN)
    protected Button mBtnEnableCN;

    @Bind(R.id.textViewEnablCNResp)
    protected TextView mTextEnableCNResult;

    @Bind(R.id.buttonDisableCN)
    protected Button mBtnDisableCN;

    @Bind(R.id.textViewDisablCNResp)
    protected TextView mTextDisableCNResult;

    @Bind(R.id.buttonEnableVC)
    protected Button mBtnEnableVC;

    @Bind(R.id.textViewEnablVCResp)
    protected TextView mTextEnableVCResult;

    @Bind(R.id.buttonDisableVC)
    protected Button mBtnDisableVC;

    @Bind(R.id.textViewDisablVCResp)
    protected TextView mTextDisableVCResult;


    @Bind(R.id.textViewVPStatus)
    protected TextView mTextVPStatus;

    @Bind(R.id.buttonSetVolume)
    protected Button mBtnSetVolume;

    @Bind(R.id.editTextVol)
    protected EditText mEditVol;

    @Bind(R.id.textViewSetVolResp)
    protected TextView mTextSetVolResult;

    @Bind(R.id.buttonGetPEQ)
    protected Button mBtnGetPEQ;

    @Bind(R.id.textViewGetPEQResp)
    protected  TextView mTextGetPEQResp;

    @Bind(R.id.textViewPEQIndication)
    protected TextView mTextPEQInd;

    @Bind(R.id.buttonGetVoiePrompt)
    protected Button mBtnGetVoicePrompt;

    @Bind(R.id.buttonNextVPLang)
    protected Button mBtnNextVPLang;

    @Bind(R.id.textViewNextVPResp)
    protected TextView mTextNexVPresp;

    @Bind(R.id.textViewNextVPResult)
    protected TextView mTextNextVPResult;

    @Bind(R.id.textViewGetVoicePromptResp)
    protected TextView mTextGetVoicePromptResp;

    @Bind(R.id.textViewVPIndication)
    protected TextView mTextVPInd;

    @Bind(R.id.buttonChangePEQ)
    protected Button mBtnChangePEQ;

    @Bind(R.id.textViewChangePEQResp)
    protected TextView mTextChangePEQResp;

    @Bind(R.id.buttonSetVPLang)
    protected Button mBtnSetVPLang;

    @Bind(R.id.textViewSetVPLangResp)
    protected TextView mTextSetVPLangResp;

    @Bind(R.id.editTextVpLang)
    protected EditText mEditVPLang;

    @Bind(R.id.buttonSendVibation)
    protected Button mBtnSendVib;

    @Bind(R.id.textViewSendVibResp)
    protected TextView mTextSendVibResp;

    @Bind(R.id.buttonGetTwsSlaveBattery)
    protected Button mBtnGetTwsSlaveBattery;

    @Bind(R.id.textViewGetTwsSlaveBatResp)
    protected TextView mTextGetTwsSlaveBatResp;

    @Bind(R.id.textViewGetTwsSlaveBatResult)
    protected TextView mTextGetTwsSlaveBatResult;

    @Bind(R.id.buttonSetA2dpEq)
    protected Button mBtnSetA2dpEq;

    @Bind(R.id.textViewSetA2dpEqResp)
    protected TextView mTextSetA2DPResp;

    @Bind(R.id.editTextA2dpIdx)
    protected EditText mEditA2dpIdx;

    @Bind(R.id.buttonSetAuxEq)
    protected Button mBtnSetAuxEq;

    @Bind(R.id.textViewSetAuxEqResp)
    protected TextView mTextSetAuxResp;

    @Bind(R.id.editTextAuxIdx)
    protected EditText mEditAuxIdx;

    @Bind(R.id.buttonGetChgBatStatus)
    protected  Button mBtnGetChgBatStatus;

    @Bind(R.id.buttonStartOta)
    protected Button mBtnStartOta;

    @Bind(R.id.textViewGetChgBatStatusResp)
    protected  TextView mTextGetChgBatStatusResp;

    @Bind(R.id.textViewGetChgBatStatusResult)
    protected  TextView mTextGetChgBatStatusResult;

    @Bind(R.id.buttonGetChgBatStatusFollower)
    protected  Button mBtnGetChgBatStatusFollower;

    @Bind(R.id.textViewGetChgBatStatusFollowerResp)
    protected  TextView mTextGetChgBatStatusFollowerResp;

    @Bind(R.id.textViewGetChgBatStatusFollowerResult)
    protected  TextView mTextGetChgBatStatusFollowerResult;

    @Bind(R.id.buttonGetVoiceCmdStatus)
    protected  Button mBtnGetVoiceCmdStatus;

    @Bind(R.id.textViewGetVoiceCmdStatusResult)
    protected TextView mTextGetVoiceCmdStatusResult;

    @Bind(R.id.buttonGetCallerStatus)
    protected  Button mBtnGetCallerStatus;

    @Bind(R.id.textViewGetCallerNameStatusResult)
    protected TextView mTextGetCallerNameStatusResult;

    @Bind(R.id.buttonPassThrough)
    protected Button mBtnPassThrough;

    @Bind(R.id.editTextPassThrough)
    protected EditText mEditPassThrough;

    @Bind(R.id.textPassThrough)
    protected TextView mTextPassThroughData;

    @Bind(R.id.textPassThroughResp)
    protected TextView mTextPassThroughResp;

    @Bind(R.id.textViewVer)
    protected TextView mTextVer;

    @Bind(R.id.buttonGetSectorInfo)
    protected Button mBtnGetSectorInfo;

    @Bind(R.id.editTextSectorIdx)
    protected EditText mEditSectorIdx;

    @Bind(R.id.editTextSectorOffset)
    protected EditText mEditSectorOffset;

    @Bind(R.id.editTextSectorLength)
    protected EditText mEditSectorLength;

    @Bind(R.id.textGetSectorInfoResp)
    protected TextView mTextGetSectorInfoResp;

    @Bind(R.id.textGetSectorInfoInd)
    protected TextView mTextGetSectorInforInd;

    @Bind(R.id.buttonGetSectorInfoV2)
    protected TextView mBtnGetSectorInfoV2;

    @Bind(R.id.editTextSectorOCF)
    protected EditText mEditSectorOCF;

    @Bind(R.id.editTextSectorOGF)
    protected EditText mEditSectorOGF;

    @Bind(R.id.editTextSectorHeadoffset)
    protected EditText mEditSectorHeadoffset;

    @Bind(R.id.editTextSectorDataLen)
    protected EditText mEditSectorDataLen;

    @Bind(R.id.textGetSectorInfoV2Ind)
    protected TextView mTextGetSectorInfoV2Ind;

    @Bind(R.id.textGetSectorInfoV2Resp)
    protected TextView mTextGetSectorInfoV2Resp;

    @Bind(R.id.buttonGetPeqHpfUserIdx)
    protected Button mBtnGetPeqHpfUserIdx;

    @Bind(R.id.buttonToggleAT)
    protected  Button mBtnToggleAT;

    @Bind(R.id.textViewToggleATResp)
    protected TextView mTextToggleAT;

    @Bind(R.id.buttonSetMasterATGain)
    protected Button mBtnSetMasterATGain;

    @Bind(R.id.editTextSetMasterATGain)
    protected EditText mEditSetMasterATGain;

    @Bind(R.id.textViewSetMasterATGainResp)
    protected TextView mTextSetMasterATGainResult;

    @Bind(R.id.buttonGetMasterATGain)
    protected  Button mBtnGetMasterATGain;

    @Bind(R.id.textViewGetMasterATGainResp)
    protected TextView mTextGetMasterATGain;

    @Bind(R.id.buttonSetSlaveATGain)
    protected Button mBtnSetSlaveATGain;

    @Bind(R.id.editTextSetSlaveATGain)
    protected EditText mEditSetSlaveATGain;

    @Bind(R.id.textViewSetSlaveATGainResp)
    protected TextView mTextSetSlaveATGainResult;

    @Bind(R.id.buttonGetSlaveATGain)
    protected  Button mBtnGetSlaveATGain;

    @Bind(R.id.textViewGetSlaveATGainResp)
    protected TextView mTextGetSlaveATGain;

    @Bind(R.id.buttonGetMasterATStatus)
    protected  Button mBtnGetMasterATStatus;

    @Bind(R.id.textViewGetMasterATStatusResp)
    protected TextView mTextGetMasterATStatus;

    @Bind(R.id.buttonGetSlaveATStatus)
    protected  Button mBtnGetSlaveATStatus;

    @Bind(R.id.textViewGetSlaveATStatusResp)
    protected TextView mTextGetSlaveATStatus;

    @Bind(R.id.buttonSendHexCmd)
    protected  Button mBtnSendHexCmd;

    @Bind(R.id.editTextSendHexCmd)
    protected EditText mEditSendHexCmd;

    @Bind(R.id.textViewHexResp)
    protected TextView mTextHexResp;

    @Bind(R.id.buttonGetFlashStructAddr)
    protected Button mBtnGetFlashStuctAddr;

    @Bind(R.id.textViewFlashStructAddress)
    protected TextView mTextFlashStructAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAirohaLink = new AirohaLink(this);
        mTextVer.setText("SDK Ver:" + mAirohaLink.getSdkVer());
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);
        mAirohaLink.registerOnMmiEventListener(TAG, mOnAirohaMmiEventListener);
        initUImember();
    }

    void initUImember(){
        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mEditSppAddr.getText().toString();

                Boolean result = mAirohaLink.connect(btaddr);

                mTextConSppResult.setText(result.toString());
            }
        });

//        mBtnConBle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String btaddr = mEditSppAddr.getText().toString();
//
//                Boolean result = mAirohaLink.connectBle(btaddr);
//
//                mTextConSppResult.setText(result.toString());
//            }
//        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.disconnect();
            }
        });

        mBtnGetChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getChannelInfo();
            }
        });

        mBtnGetChannelFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getChannelInfoFollower();
            }
        });

        mBtnGetBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getBattery();
            }
        });

        mBtnGetBatteryFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getBatteryFollower();
            }
        });

        mBtnGetFwVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAirohaLink.getFwVersion();
            }
        });

        mBtnGetFwVersionFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAirohaLink.getFwVersionFollower();
            }
        });

        mBtnFindMyAccessory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.playFindTone();
            }
        });

        mBtnGetVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getVolume();
            }
        });

        mBtnSetVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vol = mEditVol.getText().toString();

                int voli = Integer.valueOf(vol);

                byte volb = (byte)voli;

                try {
                    mAirohaLink.setVolume(volb);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mBtnEnableVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.enableVoicePrompt();
            }
        });

        mBtnDisableVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.disableVoicePrompt();
            }
        });

        mBtnGetPEQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.checkEQ();
            }
        });

        mBtnGetVoicePrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.checkVoicePrompt();
            }
        });

        mBtnNextVPLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.nextVoicePromptLang();
            }
        });

        mBtnChangePEQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.changeEQMode();
            }
        });

        mBtnSetVPLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = mEditVPLang.getText().toString();
                int idxi = Integer.valueOf(idx);
                byte idxb = (byte)idxi;

                mAirohaLink.setVoicePromptLang(idxb);
            }
        });

        mBtnGetTwsSlaveBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getTwsSlaveBattery();
            }
        });

        mBtnSetA2dpEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = mEditA2dpIdx.getText().toString();
                int idxi = Integer.valueOf(idx);
                byte idxb = (byte)idxi;

                mAirohaLink.setA2dpEq(idxb);
            }
        });

        mBtnSetAuxEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = mEditAuxIdx.getText().toString();
                int idxi = Integer.valueOf(idx);
                byte idxb = (byte)idxi;

                mAirohaLink.setAuxEq(idxb);
            }
        });



        mBtnStartOta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initProgressDialog(v);

                otaMgr = new AirohaOtaFlowMgr(mAirohaLink, mOtaEventListener);
                otaMgr.setBootcodeFileName("bootcode.bootcode");
                otaMgr.setBinFileName("update.bin");
                otaMgr.startOTA();
            }
        });


        mBtnGetChgBatStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getChargeBatteryStatus();
            }
        });

        mBtnGetChgBatStatusFollower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getChageBatteryStatusFollower();
            }
        });

        mBtnEnableVC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.enableVoiceCommand();
            }
        });

        mBtnDisableVC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.disableVoiceCommand();
            }
        });

        mBtnEnableCN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.setCallerNameOn();
            }
        });

        mBtnDisableCN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.setCallerNameOff();
            }
        });

        mBtnGetVoiceCmdStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.getVoiceCommandStatus();
            }
        });

        mBtnGetCallerStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.getCallerNameStatus();
            }
        });

        mBtnPassThrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = mEditPassThrough.getText().toString();

                byte[] hex =Converter.hexStringToByteArray(str);

                mAirohaLink.sendPassThroughData(hex);
            }
        });

        mBtnGetSectorInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = Integer.valueOf(mEditSectorIdx.getText().toString());
                int offset = Integer.valueOf(mEditSectorOffset.getText().toString());
                int length = Integer.valueOf(mEditSectorLength.getText().toString());

                mAirohaLink.getSectorInfo((byte) idx, (byte) offset, (byte)length);
            }
        });

        mBtnGetSectorInfoV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ocf = Integer.valueOf(mEditSectorOCF.getText().toString());
                int ogf = Integer.valueOf(mEditSectorOGF.getText().toString());
                int offset = Integer.valueOf(mEditSectorHeadoffset.getText().toString());
                int dataLen = Integer.valueOf(mEditSectorDataLen.getText().toString());

                byte offsetL =(byte)(offset & 0xFF);
                byte offsetH = (byte) ((offset>>8) & 0xFF);

                byte dataLenL =(byte)(dataLen & 0xFF);
                byte dataLenH = (byte) ((dataLen>>8) & 0xFF);

                mAirohaLink.getSectorInfoV2((byte)ocf, (byte)ogf, offsetL, offsetH, dataLenL, dataLenH);
            }
        });


        mBtnGetPeqHpfUserIdx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAirohaLink.getUserDefinedPeqHpfSectorIdx();

                otaMgr = new AirohaOtaFlowMgr(mAirohaLink, mOtaEventListener);
            }
        });


        mBtnToggleAT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.audioTransparencyToggle();
            }
        });

        mBtnSetMasterATGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = mEditSetMasterATGain.getText().toString();
                if(idx.isEmpty()) {
                    return;
                }
                int idxi = Integer.valueOf(idx);
                byte idxb = (byte)idxi;
                mAirohaLink.setMasterATGain(idxb);
            }
        });

        mBtnGetMasterATGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getMasterATGain();
            }
        });

        mBtnSetSlaveATGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idx = mEditSetSlaveATGain.getText().toString();
                if(idx.isEmpty()) {
                    return;
                }
                int idxi = Integer.valueOf(idx);
                byte idxb = (byte)idxi;
                mAirohaLink.setSlaveATGain(idxb);
            }
        });

        mBtnGetSlaveATGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getSlaveATGain();
            }
        });

        mBtnGetMasterATStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getMasterATStatus();
            }
        });

        mBtnGetSlaveATStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.getSlaveATStatus();
            }
        });

        mBtnSendHexCmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = mEditSendHexCmd.getText().toString().replace(" ", "");
                if(cmd.isEmpty()) {
                    return;
                }
                mAirohaLink.sendCommand(Converter.hexStringToByteArray(cmd));
            }
        });


        mBtnGetFlashStuctAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FlashDescriptor.getInstance().setDescriptorListener(mDescriptorListener);
                FlashDescriptor.getInstance().StartDescriptorParser(mAirohaLink);
            }
        });
    }

    private final OnFlashDescriptorListener mDescriptorListener = new OnFlashDescriptorListener() {

        @Override
        public void OnResult() {
            // Daniel, testing -start
            FlashDescriptor.SectorTableHeader boundaryTable = FlashDescriptor.getInstance()._stHeaderList.get(SectorTable.Boundary.ordinal());
            int sectorAddress = boundaryTable._sectorInfoList.get(SectorIndex.Boundary.DSP_PEQ_PARAMETER_STRU.ordinal()).flashAddress;

            Log.d(TAG, "DSP_PEQ_PARAMETER_STRU address:" + sectorAddress);

            // Daniel, testing - end
        }
    };

    void initProgressDialog(View v) {
        pd = new ProgressDialog(v.getContext());
        pd.setTitle("downloading....");
        pd.setMessage("Please wait for about 5 minutes");
        pd.setMax(100);
        pd.setCancelable(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
        pd.setProgress(0);

        dialog = new AlertDialog.Builder(v.getContext());
    }

    void stopProgressbar(String msg) {
        pd.setTitle("downloading....");
        pd.setMessage(msg);
        pd.setCancelable(true);
    }



    @Override
    protected void onDestroy() {
        mAirohaLink.disconnect();

        super.onDestroy();
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Conn. :" + type);
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "DisConnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("DisConn.");
                }
            });
        }
    };


    private final OnAirohaOtaEventListener mOtaEventListener = new OnAirohaOtaEventListener() {

        @Override
        public void OnUpdateProgressbar(int value) {
            pd.incrementProgressBy(value);
        }

        @Override
        public void OnOtaResult(boolean isPass, String status) {
            if(isPass)
            {
            }
            else
            {
                stopProgressbar(status);
            }
        }

        private int count = 0;
        @Override
        public void OnOtaStartApplyUI() {
            pd.dismiss();

            dialog.setTitle("Airoha Air Update");
            dialog.setMessage("Confirm to upgrade?");
            dialog.setCancelable(false);
            dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    AirohaOtaLog.LogToFile("-----APPLY START-----" + "\n");
                    otaMgr.applyOTA();
                }
            });
            dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    AirohaOtaLog.LogToFile("-----CANCEL START-----" + "\n");
                    otaMgr.cancelOTA();
                }
            });
            dialog.show();
        }

        @Override
        public void OnShowCurrentStage(String currentStage) {

        }

        @Override
        public void OnNotifyMessage(String msg) {

        }
    };

    private OnAirohaMmiEventListener mOnAirohaMmiEventListener = new OnAirohaMmiEventListener() {

        @Override
        public void OnHexResp(final byte[] resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextHexResp.setText(Converter.byte2HexStr(resp));
                }
            });
        }

        @Override
        public void OnGetChannelInfoResp(byte resp) {

        }

        @Override
        public void OnGetChannelInfoInd(final byte bLeft_right) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChannelResult.setText(String.valueOf(bLeft_right));
                }
            });
        }

        @Override
        public void OnGetChannelInfoRespFollower(byte resp) {

        }

        @Override
        public void OnGetChannelInfoIndFollower(final byte bLeft_right) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChannelFrResult.setText(String.valueOf(bLeft_right));
                }
            });
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
        public void OnGetBatteryResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetBatResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetBatteryInd(final byte batteryStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetBatResult.setText(String.valueOf(batteryStatus));
                }
            });
        }

        @Override
        public void OnGetBatteryRespFollower(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetBatFrResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetBatteryIndFollower(final byte batteryStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetBatFrResult.setText(String .valueOf(batteryStatus));
                }
            });
        }

        @Override
        public void OnGetTwsSlaveBatteryResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetTwsSlaveBatResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetTwsSlaveBatteryInd(final byte batteryStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetTwsSlaveBatResult.setText(String.valueOf(batteryStatus));
                }
            });
        }

        @Override
        public void OnGetVolumeResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetVolResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetVolumeInd(final byte volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextVolResult.setText(String.valueOf(volume));
                }
            });
        }

        @Override
        public void OnSetVolumeResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetVolResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnEnableVoicePromptResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextEnableVPResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnDisableVoicePromptResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextDisableVPResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnEnableVoiceCommandResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextEnableVCResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnDisableVoiceCommandResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextDisableVCResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnPlayFindToneResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextFindMyAsyResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnCheckVoicePromptResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetVoicePromptResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnCheckVoicePromptInd(final byte isVPEnabled, final byte vpLangIndex, final byte vpLangTotal, final byte[] vpEnums) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextVPInd.setText(" enabled:" + isVPEnabled + "\n"
                            +"idx/count: " + vpLangIndex + "/" + vpLangTotal + "\n"
                            + "enums:" + Converter.byte2HexStr(vpEnums, vpEnums.length).concat(" ") );
                }
            });

        }


        @Override
        public void OnCheckEqResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetPEQResp.setText(String.valueOf(resp));
                }
            });

        }


        @Override
        public void OnCheckEqInd(final byte PEQInA2DP, final byte PEQInAUX,
                                 final byte PEQNumInA2DP, final byte PEQNumInAUX,
                                 final byte isUseDefaultPEQ,
                                 final byte A2DPSectorMode, final byte AUXSectorMode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextPEQInd.setText("PEQInA2DP: " + PEQInA2DP +
                            ", PEQInAUX: " + PEQInAUX + ", PEQNumInA2DP:" + PEQNumInA2DP +
                            ", PEQNumInAUX: " + PEQNumInAUX + ", isUseDefaultPEQ: " + isUseDefaultPEQ +
                            ", A2DPSectorMode: " + A2DPSectorMode + ", AUXSectorMode: " + AUXSectorMode);
                }
            });
        }

        @Override
        public void OnChangeEqModeResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextChangePEQResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnSetA2dpEqResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetA2DPResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnReportA2dpEqChanged(final byte a2dpEqIdx) {

        }

        @Override
        public void OnSetAuxEqResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetAuxResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnReportAuxEqChanged(final byte auxEqIdx) {

        }

        @Override
        public void OnGetFwVersionResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetFwVersionResp.setText(String.valueOf(resp));
                }
            });
        }


        @Override
        public void OnGetFwVersionInd(final String fwStr) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetFwVersionResult.setText(fwStr);
                }
            });
        }

        @Override
        public void OnGetFwVersionRespFollower(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetFwVersionFrResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetFwVersionIndFollower(final String fwStr) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetFwVersionFrResult.setText(String.valueOf(fwStr));
                }
            });
        }

        @Override
        public void OnGetTwsSlaveFwVersionResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetTwsSlavbeFwVersionResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetTwsSlaveFwVersionInd(final String fwStr) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetTwsSlaveFwVersionResult.setText(fwStr);
                }
            });
        }

        @Override
        public void OnGetChargeBatteryStatusResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChgBatStatusResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetChargeBatteryStatusInd(final ChargingStatus status ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChgBatStatusResult.setText(status.toString());
                }
            });
        }

        @Override
        public void OnGetChargeBatteryStatusFollowerResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChgBatStatusFollowerResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetChargeBatteryStatusFollowerInd(final ChargingStatus status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetChgBatStatusFollowerResult.setText(status.toString());
                }
            });
        }

        @Override
        public void OnGetVoiceCommandStatusInd(final byte voiceCmdStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetVoiceCmdStatusResult.setText(String.valueOf(voiceCmdStatus));
                }
            });
        }

        @Override
        public void OnGetCallerNameStatusInd(final byte callerNameStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetCallerNameStatusResult.setText(String.valueOf(callerNameStatus));
                }
            });
        }

        @Override
        public void OnSetCallerNameResp(final byte resp) {

        }

        @Override
        public void OnPassThroughDataInd(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String hex = Converter.byte2HexStr(data);

                    mTextPassThroughData.setText(hex);
                }
            });
        }

        @Override
        public void OnPasThroughDataResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextPassThroughResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetSectorInfoResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSectorInfoResp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetSectorInfoInd(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSectorInforInd.setText(Converter.byte2HexStr(data));
                }
            });
        }

        @Override
        public void OnGetSectorInfoRespV2(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSectorInfoV2Resp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetSectorInfoIndV2(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSectorInfoV2Ind.setText(Converter.byte2HexStr(data));
                }
            });
        }

        @Override
        public void OnNextVoicePromptLangResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextNexVPresp.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnReportVoicePromptStatus(final byte vpStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextVPStatus.setText(String.valueOf(vpStatus));
                }
            });
        }

        @Override
        public void OnReportVoicePromptLangChanged(final byte vpLangIdx) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextNextVPResult.setText(String.valueOf(vpLangIdx));
                }
            });
        }

        @Override
        public void OnSetVoicePromptLangResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetVPLangResp.setText(String.valueOf(resp));
                }
            });
        }


        @Override
        public void OnReportPeqSectorModeChanged(byte mode) {

        }


        @Override
        public void OnAudioTransparencyToggleResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextToggleAT.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnSetMasterATGainResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetMasterATGainResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnSetSlaveATGainResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextSetSlaveATGainResult.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetMasterATGainResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetMasterATGain.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetSlaveATGainResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSlaveATGain.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetMasterATStatusResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetMasterATStatus.setText(String.valueOf(resp));
                }
            });
        }

        @Override
        public void OnGetSlaveATStatusResp(final byte resp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextGetSlaveATStatus.setText(String.valueOf(resp));
                }
            });
        }

    };
}
