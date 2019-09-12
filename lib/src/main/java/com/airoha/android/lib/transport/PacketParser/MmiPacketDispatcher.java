package com.airoha.android.lib.transport.PacketParser;

import android.util.Log;

import com.airoha.android.lib.mmi.OnAirohaDspEventListener;
import com.airoha.android.lib.mmi.OnAirohaMmiEventListener;
import com.airoha.android.lib.mmi.OnAirohaFollowerExistingListener;
import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.mmi.cmd.AirohaMMICmd;
import com.airoha.android.lib.mmi.cmd.KeyCode;
import com.airoha.android.lib.mmi.cmd.OCF;
import com.airoha.android.lib.mmi.cmd.OGF;
import com.airoha.android.lib.mmi.cmd.SppPacketIndex;
import com.airoha.android.lib.ota.FwDesc.FwVersion;
import com.airoha.android.lib.ota.OnAirohaFw4KCrc16Listener;
import com.airoha.android.lib.ota.OnAirohaFwVerSyncListener;
import com.airoha.android.lib.peq.AirohaPeqMgr.OnSendRealTimeUpdatePeqRespListener;
import com.airoha.android.lib.peq.DrcMode.OnAirohaReportDrcModeListener;
import com.airoha.android.lib.peq.OnAirohaPeqControlListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static com.airoha.android.lib.util.Converter.byte2HexStr;

/**
 * Created by Daniel.Lee on 2016/6/1.
 */
public class MmiPacketDispatcher {

    private static final String TAG = "MmiPacketDispatcher";

//    private static final String TAG_RESP = "Airoha Resp";
//    private static final String TAG_SOLI_IND = "Airoha Soli Ind";
//    private static final String TAG_UNSOLI_IND = "Airoha UnSoli Ind";

    private OnAirohaFwVerSyncListener mFwVerSyncListener;

    private OnSendRealTimeUpdatePeqRespListener mRealTimeUpdatePeqRespListener;

    private ConcurrentHashMap<String, OnAirohaMmiEventListener> mMmiEventListenerMap = null;

    private ConcurrentHashMap<String, OnAirohaPeqControlListener> mPeqControlListenerMap = null;

    private OnAirohaDspEventListener mOnAirohaDspEventListener;

    private OnAirohaFw4KCrc16Listener mFwF4Crc16Listener;

    private OnAirohaReportDrcModeListener mReportDrcModeListener;

    private ConcurrentHashMap<String, OnAirohaFollowerExistingListener> mSlaveConnectionListenerMap;

    private AirohaLink mAirohaLink;

    public MmiPacketDispatcher(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;

        mMmiEventListenerMap = new ConcurrentHashMap<>();
        mPeqControlListenerMap = new ConcurrentHashMap<>();

        mSlaveConnectionListenerMap = new ConcurrentHashMap<>();
    }

    public void setDspEventListener(OnAirohaDspEventListener listener) {
        mOnAirohaDspEventListener = listener;
    }

    public void setFwF4Crc16Listener(OnAirohaFw4KCrc16Listener listener) {
        mFwF4Crc16Listener = listener;
    }

    public void setReportDrcModeListener(OnAirohaReportDrcModeListener listener) {
        mReportDrcModeListener = listener;
    }

    public void registerFollowerExistingListener(String key, OnAirohaFollowerExistingListener listener) {
        mSlaveConnectionListenerMap.put(key, listener);
    }

    public void registerMmiListener(String key, OnAirohaMmiEventListener listener) {
        mMmiEventListenerMap.put(key, listener);
    }


    public void registerPeqControlListener(String key, OnAirohaPeqControlListener listener) {
        mPeqControlListenerMap.put(key, listener);
    }

    public void unregisterListener(String key) {
        if (mMmiEventListenerMap == null) {
            return;
        }
        mMmiEventListenerMap.remove(key);
    }

    public void setFwVerSyncListener(OnAirohaFwVerSyncListener listener) {
        mFwVerSyncListener = listener;
    }

    public void setRealTimeUpdatePeqRespListener(OnSendRealTimeUpdatePeqRespListener listener) {
        mRealTimeUpdatePeqRespListener = listener;
    }

    public void parseSend(byte[] packet) {

        if (isDspSuspendCmd(packet)) {
            logBothWay("isDspSuspendCmd resp.", Converter.byte2HexStr(packet));

            if (mOnAirohaDspEventListener != null) {
                mOnAirohaDspEventListener.OnSuspendResp(packet[5], packet[4]);
            }

            return;
        }

        if (isDspResumeCmd(packet)) {
            logBothWay("isDspResumeCmd resp.", Converter.byte2HexStr(packet));

            if (mOnAirohaDspEventListener != null) {
                mOnAirohaDspEventListener.OnResumeResp(packet[5], packet[4]);
            }

            return;
        }

        if (isKeyCmd(packet)) {
            doKeyCodeCmdHandler(packet);
            return;
        }

        handlePacketDispatch(packet);
    }


    private void handlePacketDispatch(byte[] packet) {
        byte bOCF = packet[SppPacketIndex.OCF];
        byte bOGF = packet[SppPacketIndex.OGF];

        // Report something
        if (OGF.isIndUnsolictited(bOGF)) {
            try {
                handleIndUnsolicited(bOCF, bOGF, packet);
            } catch (Exception e) {
                logBothWay(e.getMessage(), "");
            }
            return;
        }

        // Report from active commands
        if (OGF.isIndSolictited(bOGF)) {
            try {
                handleIndSolicited(bOCF, bOGF, packet);
            } catch (Exception e) {
                logBothWay(e.getMessage(), "");
            }
        }

        if (OGF.isResp(bOGF)) {
            byte bResp = packet[SppPacketIndex.RESP];
            try {
                handleResp(bOCF, bOGF, bResp);
            } catch (Exception e) {
                logBothWay(e.getMessage(), "");
            }
        }
    }

    public static boolean isActiveResp(byte[] packet) {
        byte bOGF = packet[SppPacketIndex.OGF];

        return OGF.isResp(bOGF);
    }

    private void handleIndSolicited(byte bOCF, byte bOGF, byte[] packet) {
        if (OCF.GET_FW_VER == bOCF) {


            byte[] version = new byte[6];
            System.arraycopy(packet, 5, version, 0, version.length);

            FwVersion mFwVer = new FwVersion(version, false);

            if (bOGF == OGF.AIROHA_MMI_IND_SOLICITED) {
                logBothWay("OnGetFwVersionInd", mFwVer.toString());

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetFwVersionInd(mFwVer.toString());
                    }
                }
            }


            if (bOGF == OGF.AIROHA_MMI_IND_SOLICITED_FR) {
                logBothWay("OnGetFwVersionIndFollower", mFwVer.toString());

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetFwVersionIndFollower(mFwVer.toString());
                    }
                }
            }

            if (mFwVerSyncListener != null) {
                mFwVerSyncListener.OnFwReported(mFwVer);
            }

            return;
        }

        if (OCF.GET_CHANNEL_INFO == bOCF) {
            //            Event:
            //            relayer: 0x04 0xFF 0x03 0xBD 0x4B 0xZZ
            //            follower: 0x04 0xFF 0x03 0xBD 0x47 0xZZ

            byte bLeft_Right = packet[5];

            if (OGF.AIROHA_MMI_IND_SOLICITED == bOGF) {
                logBothWay("OnGetChannelInfoInd", String.valueOf(bLeft_Right));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetChannelInfoInd(bLeft_Right);
                    }
                }

                return;
            }


            if (OGF.AIROHA_MMI_IND_SOLICITED_FR == bOGF) {
                logBothWay("OnGetChannelInfoIndFollower", String.valueOf(bLeft_Right));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetChannelInfoIndFollower(bLeft_Right);
                    }
                }

                return;
            }
        }


        if (OCF.GET_TWS_SLAVE_FW_VER == bOCF) {


            byte[] version = new byte[6];
            System.arraycopy(packet, 5, version, 0, version.length);

            FwVersion mSlaveFwVer = new FwVersion(version, false);

            logBothWay("OnGetTwsSlaveFwVersionInd", mSlaveFwVer.toString());

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetTwsSlaveFwVersionInd(mSlaveFwVer.toString());
                }
            }

            return;
        }

        if (OCF.GET_PEQ == bOCF) {
            //   Sent from app
            //  04 FF 09 FA 4B XX XX XX XX XX XX XX
            //  U8 PEQInA2DP – current using PEQ mode in A2DP (0:off)
            //  U8 PEQInAUX – current using PEQ mode in AUX (0:off)
            //  U8 PEQNumInA2DP; - total PEQ mode in A2DP (if 0,do not support A2DP PEQ switch)
            //  U8 PEQNumInAUX; - total PEQ mode in AUX(if 0, do not support AUX PEQ switch)
            //  U8 isUseDefaultPEQ; - using default peq (if it has value, PEQ switch need skip mode 0:off)
            //  U8 A2DPSectorMode; - using default peq or customer (0:default, 1:customer)
            //  U8 AUXSectorMode; - using default peq or customer (0:default, 1:customer)

            byte PEQInA2DP = packet[5];
            byte PEQInAUX = packet[6];

            byte PEQNumInA2DP = packet[7];
            byte PEQNumInAUX = packet[8];

            byte isUseDefaultPEQ = packet[9];

            byte A2DPSectorMode = packet[10];
            byte AUXSectorMode = packet[11];



            logBothWay("OnCheckEqInd", "PEQInA2DP: " + PEQInA2DP +
                    ", PEQInAUX: " + PEQInAUX + ", PEQNumInA2DP:" + PEQNumInA2DP +
                    ", PEQNumInAUX: " + PEQNumInAUX + ", isUseDefaultPEQ: " + isUseDefaultPEQ +
                    ", A2DPSectorMode: " + A2DPSectorMode + ", AUXSectorMode" + AUXSectorMode);

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnCheckEqInd(PEQInA2DP, PEQInAUX,
                            PEQNumInA2DP, PEQNumInAUX,
                            isUseDefaultPEQ,
                            A2DPSectorMode, AUXSectorMode);
                }
            }

            return;
        }

        //         Sent from app
        //        04 FF 03 F9 4B XX
        //        U8 BatteryStatus -  0% - 100%, (0 – 100)
        if (OCF.GET_BATTERY == bOCF) {
            byte batStatus = packet[SppPacketIndex.BAT_STATUS];


            if (OGF.AIROHA_MMI_IND_SOLICITED == bOGF) {
                logBothWay("OnGetBatteryInd", String.valueOf(batStatus));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryInd(batStatus);
                    }
                }

                return;
            }

            if (OGF.AIROHA_MMI_IND_SOLICITED_FR == bOGF) {
                logBothWay("OnGetBatteryIndFollower", String.valueOf(batStatus));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryIndFollower(batStatus);
                    }
                }
            }
        }

        //         Sent from app
        //        04 FF 03 B8 4B XX
        //        U8 BatteryStatus -  0% - 100%, (0 – 100)
        if (OCF.GET_RIGHT_BATTERY == bOCF) {
            byte batStatus = packet[SppPacketIndex.BAT_STATUS];

            logBothWay("OnGetTwsSlaveBatteryInd", String.valueOf(batStatus));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetTwsSlaveBatteryInd(batStatus);
                }
            }

            return;
        }

        //        Sent from app
        //        04 FF 03 F0 4B XX
        //        U8 currentVol – 0% - 100%, (0-127)
        if (OCF.GET_VOLUME == bOCF) {
            byte vol = packet[SppPacketIndex.VOL];

            logBothWay("OnGetVolumeInd", String.valueOf(vol));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetVolumeInd(vol);
                }
            }
            return;
        }

        if (OCF.GET_CHG_BAT_STATUS == bOCF) {
            byte status = packet[SppPacketIndex.BAT_STATUS];

            logBothWay("OnGetChargeBatteryStatusInd", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetChargeBatteryStatusInd(ChargingStatus.getValue(status));
                }
            }

            return;
        }

        if (OCF.GET_CHG_BAT_STATUS_FOLLOWER == bOCF) {
            byte status = packet[SppPacketIndex.BAT_STATUS];

            logBothWay("OnGetChargeBatteryStatusFollowerInd", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetChargeBatteryStatusFollowerInd(ChargingStatus.getValue(status));
                }
            }

            return;
        }

        if (OCF.GET_DEVICE_NAME == bOCF) {
            return;
        }

        //        Sent from app
        if (OCF.GET_VOICE_PROMPT == bOCF) {
            logBothWay("OnCheckVoicePromptInd", Converter.byte2HexStr(packet));

            byte isVPEnabled = packet[SppPacketIndex.VP_STATUS];
            byte VPLangIndex = packet[SppPacketIndex.VP_STATUS + 1];
            byte VPLangTotal = packet[SppPacketIndex.VP_STATUS + 2]; // Max = 6

            byte[] VPEnums = new byte[VPLangTotal];
            // copy
            for (int i = 0; i < VPLangTotal; i++) {
                VPEnums[i] = packet[SppPacketIndex.VP_STATUS + 3 + i];
            }

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnCheckVoicePromptInd(isVPEnabled, VPLangIndex, VPLangTotal, VPEnums);
                }
            }
            return;
        }

        if (OCF.GET_VOICE_CMD_STATUS == bOCF) {
            byte status = packet[SppPacketIndex.VOICE_CMD_STATUS];

            logBothWay("OnGetVoiceCommandStatusInd ", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetVoiceCommandStatusInd(status);
                }
            }
            return;
        }

        if (OCF.GET_CALLER_NAME_STATUS == bOCF) {
            byte status = packet[SppPacketIndex.CALLER_NAME_STATUS];

            logBothWay("OnGetCallerNameStatusInd ", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetCallerNameStatusInd(status);
                }
            }

            return;
        }

        if (AirohaMMICmd.getRequstedSectorIdx() == bOCF) {
            // 0x04 0xFF 0xXX 0xA3 0x62 0xZZ 0xSS.....
            //XX: length
            //ZZ: start of data
            //ex: 0x04 0xFF 0x04 0xA3 0x62 0x11 0x22
            int dataLength = packet[2] - 2;
            byte[] data = new byte[dataLength];

            System.arraycopy(packet, 5, data, 0, dataLength);

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSectorInfoInd(data);
                }
            }

            return;
        }

        if (AirohaMMICmd.getSectorBasePlusOffset() == bOCF) {
            // 0x04 0xFF 0xXX 0xA3 0x62 0xZZ 0xSS.....
            //XX: length
            //ZZ: start of data
            //ex: 0x04 0xFF 0x04 0xA3 0x62 0x11 0x22
            int dataLength = packet[2] - 2;
            byte[] data = new byte[dataLength];

            System.arraycopy(packet, 5, data, 0, dataLength);

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSectorInfoIndV2(data);
                }
            }
            return;
        }

        if (OCF.GET_4K_SECTOR_CRC16 == bOCF) {
            //            [Resp] 04 ff 0c 36 4b XX XX XX XX OO OO OO OO CC CC
            //            XX: Address
            //            OO: Length
            //            CC: CRC16

            //            addr[3], addr[2], addr[1], addr[0],
            //             LEN[3], LEN[2], LEN[1], LEN[0]
            if (mFwF4Crc16Listener != null) {
                byte[] crc16 = new byte[2];
                crc16[0] = packet[13];
                crc16[1] = packet[14];

                byte[] bAddress = new byte[4];

                System.arraycopy(packet, 5, bAddress, 0, 4);

                int iAddress = Converter.bytesBigEndianToInt(bAddress);

                mFwF4Crc16Listener.On4KCrc16Reported(iAddress, crc16);
            }

            return;
        }

        if (OCF.GET_MUSIC_SAMPLE_RATE == bOCF) {
            byte sampleRateEnum = packet[5];
            logBothWay("OnMusicSampleRateChanged", "sampleRateEnum: " + sampleRateEnum);

            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnMusicSampleRateChanged(sampleRateEnum);
                }
            }
            return;
        }

        if (OCF.GET_REAL_TIME_UI_DATA == bOCF) {
            int dataLength = packet[2] - 2;
            byte[] data = new byte[dataLength];
            System.arraycopy(packet, 5, data, 0, data.length);

            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetRealTimeUiDataInd(data);
                }
            }
            return;
        }
    }

    private void handleResp(byte ocf, byte ogf, byte resp) {

        if (OCF.GET_CHANNEL_INFO == ocf) {
            logBothWay("OnGetChannelInfoResp", String.valueOf(resp));

            if (ogf == OGF.AIROHA_MMI_RESP) {
                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetChannelInfoResp(resp);
                    }
                }

                return;
            }

            if (ogf == OGF.AIROHA_MMI_RESP_FR) {
                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetChannelInfoRespFollower(resp);
                    }
                }
                return;
            }
            return;
        }


        if (OCF.GET_FW_VER == ocf) {
            logBothWay("OnGetFwVersionResp", String.valueOf(resp));

            if (ogf == OGF.AIROHA_MMI_RESP) {
                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    listener.OnGetFwVersionResp(resp);
                }
                return;
            }

            if (ogf == OGF.AIROHA_MMI_RESP_FR) {
                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    listener.OnGetFwVersionRespFollower(resp);
                }
                return;
            }
            return;
        }

        if (OCF.GET_TWS_SLAVE_FW_VER == ocf) {
            logBothWay("OnGetTwsSlaveFwVersionResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetTwsSlaveFwVersionResp(resp);
                }
            }
            return;
        }


        if (OCF.GET_BATTERY == ocf) {
            if (ogf == OGF.AIROHA_MMI_RESP) {
                logBothWay("OnGetBatteryResp", String.valueOf(resp));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryResp(resp);
                    }
                }
            }

            if (ogf == OGF.AIROHA_MMI_RESP_FR) {
                logBothWay("OnGetBatteryRespFollower", String.valueOf(resp));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryRespFollower(resp);
                    }
                }
            }

            return;
        }

        if (OCF.GET_RIGHT_BATTERY == ocf) {
            logBothWay("OnGetTwsSlaveBatteryResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetTwsSlaveBatteryResp(resp);
                }
            }
            return;
        }

        if (OCF.GET_VOLUME == ocf) {
            logBothWay("OnGetVolumeResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetVolumeResp(resp);
                }
            }
            return;
        }

        if (OCF.GET_VOICE_PROMPT == ocf) {
            logBothWay("OnCheckVoicePromptResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnCheckVoicePromptResp(resp);
                }
            }
            return;
        }

        if (OCF.GET_PEQ == ocf) {
            logBothWay("OnCheckEqResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnCheckEqResp(resp);
                }
            }
            return;
        }

        if (OCF.SET_VOL == ocf) {
            logBothWay("OnSetVolumeResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetVolumeResp(resp);
                }
            }
            return;
        }

        if (OCF.VOICE_PROMPT_ENABLE == ocf) {
            logBothWay("OnEnableVoicePromptResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnEnableVoicePromptResp(resp);
                }
            }
            return;
        }

        if (OCF.VOICE_PROMPT_DISABLE == ocf) {
            logBothWay("OnDisableVoicePromptResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnDisableVoicePromptResp(resp);
                }
            }

            return;
        }

        if (OCF.VOICE_COMMAND_ENABLE == ocf) {
            logBothWay("OnEnableVoiceCommandResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnEnableVoiceCommandResp(resp);
                }
            }
            return;
        }

        if (OCF.VOICE_COMMAND_DISABLE == ocf) {
            logBothWay("OnDisableVoiceCommandResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnDisableVoiceCommandResp(resp);
                }
            }
            return;
        }

        if (OCF.VOICE_PROMPT_LANG_CHANGE_NEXT == ocf) {
            logBothWay("OnNextVoicePromptLangResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnNextVoicePromptLangResp(resp);
                }
            }
            return;
        }
        if (OCF.FIND_MY_ACCESSORY_1520 == ocf) {
            logBothWay("OnPlayFindToneResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnPlayFindToneResp(resp);
                }
            }
            return;
        }

        if (OCF.WRITE_DEVICE_NAME == ocf) {
            logBothWay("OnSetDeviceNameResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetDeviceNameResp(resp);
                }
            }
            return;
        }


        if (OCF.KEY_PEQ_MODE_CHANGE == ocf) {
            logBothWay("OnChangeEqModeResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnChangeEqModeResp(resp);
                }
            }
            return;
        }

        if (OCF.SET_CALLER_NAME == ocf) {
            logBothWay("OnSetCallerNameResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetCallerNameResp(resp);
                }
            }
            return;
        }

        if (OCF.SET_VP_LANG == ocf) {
            logBothWay("OnSetVoicePromptLangResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetVoicePromptLangResp(resp);
                }
            }
            return;
        }

        if (OCF.SET_PEQ_A2DP == ocf) {
            logBothWay("OnSetA2dpEqResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetA2dpEqResp(resp);
                }
            }
            return;
        }

        if (OCF.SET_PEQ_AUX == ocf) {
            logBothWay("OnSetAuxEqResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetAuxEqResp(resp);
                }
            }
            return;
        }

        if (OCF.GET_CHG_BAT_STATUS == ocf) {
            logBothWay("OnGetChargeBatteryStatusResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetChargeBatteryStatusResp(resp);
                }
            }
            return;
        }

        if (OCF.GET_CHG_BAT_STATUS_FOLLOWER == ocf) {
            logBothWay("OnGetChargeBatteryStatusFollowerResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetChargeBatteryStatusFollowerResp(resp);
                }
            }
            return;
        }

        if (AirohaMMICmd.getRequstedSectorIdx() == ocf) {
            logBothWay("OnGetSectorInfoResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSectorInfoResp(resp);
                }
            }
            return;
        }

        if (AirohaMMICmd.getSectorBasePlusOffset() == ocf) {
            logBothWay("OnGetSectorInfoRespV2", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSectorInfoRespV2(resp);
                }
            }
            return;
        }

        if (OCF.SEND_REAL_TIME_UPDATE_PEQ == ocf) {
            logBothWay("OnSendRealTimeUpdatePeqResp", String.format("resp: %02X, ogf: %02X", resp, ogf));

            if (mRealTimeUpdatePeqRespListener != null) {
                mRealTimeUpdatePeqRespListener.OnSendRealTimeUpdatePeqResp(resp, ogf);
            }

            return;
        }

        if (OCF.SET_MASTER_AT_GAIN == ocf) {
            logBothWay("OnSetMasterATGainResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetMasterATGainResp(resp);
                }
            }
            return;
        }
        if (OCF.SET_SLAVE_AT_GAIN == ocf) {
            logBothWay("OnSetSlaveATGainResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetSlaveATGainResp(resp);
                }
            }
            return;
        }
        if (OCF.GET_MASTER_AT_GAIN == ocf) {
            logBothWay("OnGetMasterATGainResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetMasterATGainResp(resp);
                }
            }
            return;
        }
        if (OCF.GET_SLAVE_AT_GAIN == ocf) {
            logBothWay("OnGetSlaveATGainResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSlaveATGainResp(resp);
                }
            }
            return;
        }
        if (OCF.GET_MASTER_AT_STATUS == ocf) {
            logBothWay("OnGetMasterATStatusResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetMasterATStatusResp(resp);
                }
            }
            return;
        }
        if (OCF.GET_SLAVE_AT_STATUS == ocf) {
            logBothWay("OnGetSlaveATStatusResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetSlaveATStatusResp(resp);
                }
            }
            return;
        }
        if (OCF.GET_MUSIC_SAMPLE_RATE == ocf) {
            logBothWay("OnGetMusicSampleRateResp", String.valueOf(resp));

            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetMusicSampleRateResp(resp);
                }
            }
        }

        if (OCF.GET_REAL_TIME_UI_DATA == ocf) {
            logBothWay("OnGetRealTimeUiDataResp", String.valueOf(resp));
            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetRealTimeUiDataResp(resp);
                }
            }
        }

        if (OCF.SET_REAL_TIME_UI_DATA == ocf) {
            logBothWay("OnSetRealTimeUiDataResp", String.valueOf(resp));
            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnSetRealTimeUiDataResp(resp);
                }
            }
        }
    }

    private void handleIndUnsolicited(byte bOCF, byte bOGF, byte[] packet) {
        // Resp Success - start
        //         Sent from BT when battery level is changed
        //        04 FF 03 E0 4A XX
        //        XX : Battery level: 0% - 100% (0-100)
        if (OCF.REPORT_BAT_STATUS == bOCF) {
            byte batStatus = packet[SppPacketIndex.BAT_STATUS];

            if (OGF.AIROHA_MMI_IND_UNSOLICITED == bOGF) {
                logBothWay("OnGetBatteryInd", String.valueOf(batStatus));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryInd(batStatus);
                    }
                }
                return;
            }

            if (OGF.AIROHA_MMI_IND_SOLICITED_FR == bOGF) {
                logBothWay("OnGetBatteryIndFollower", String.valueOf(batStatus));

                for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                    if (listener != null) {
                        listener.OnGetBatteryIndFollower(batStatus);
                    }
                }
                return;
            }

        }
        //        Sent from BT when voice prompt on/off is changed
        //        04 FF 03 E1 4A XX
        //        XX – Voice prompt On: 1; Voice prompt Off: 0
        if (OCF.REPORT_VP_STATUS == bOCF) {
            byte vpStatus = packet[SppPacketIndex.VP_STATUS];

            logBothWay("OnReportVoicePromptStatus", String.valueOf(vpStatus));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportVoicePromptStatus(vpStatus);
                }
            }
            return;
        }
        //        Sent from BT when voice prompt language is changed
        //        04 FF 03 E2 4A XX
        //        XX – Voice prompt language index
        if (OCF.REPORT_VP_LANG == bOCF) {
            byte vpLang = packet[SppPacketIndex.VP_LANG];

            logBothWay("OnReportVoicePromptLangChanged", String.valueOf(vpLang));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportVoicePromptLangChanged(vpLang);
                }
            }
            return;
        }
        //        Sent from BT when peq A2DP is changed
        //        04 FF 03 E3 4A XX
        //        XX – PEQ A2DP index
        if (OCF.REPORT_PEQ_A2DP_CHANGE == bOCF) {
            byte a2dpEqIdx = packet[5];

            logBothWay("OnReportA2dpEqChanged", String.valueOf(a2dpEqIdx));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportA2dpEqChanged(a2dpEqIdx);
                }
            }
            return;
        }
        //        Sent from BT when peq AUX is changed
        //        04 FF 03 E4 4A XX
        //        XX – PEQ AUX index
        if (OCF.REPORT_PEQ_AUX_CHANGE == bOCF) {
            byte auxEqIdx = packet[5];

            logBothWay("OnReportAuxEqChanged", String.valueOf(auxEqIdx));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportAuxEqChanged(auxEqIdx);
                }
            }

            return;
        }
        //        Sent from BT when volume is changed
        //        Indication:
        //        04 FF 03 E5 4A XX
        //        XX : volume 0% - 100%, (0-127)
        if (OCF.REPORT_VOL_CHANGE == bOCF) {
            byte vol = packet[SppPacketIndex.VOL];
            logBothWay("OnGetVolumeInd", String.valueOf(vol));
            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnGetVolumeInd(vol);
                }
            }

            return;
        }

        // Sent from MCU pass through
        // 04 FF length 03 53 data
        if (OCF.REPORT_PASS_THROUGH == bOCF) {
            logBothWay("OnPassThroughDataInd", Converter.byte2HexStr(packet));

            int dataLength = packet[2] - 2; // -2: ocf, ogf

            byte[] data = new byte[dataLength];

            System.arraycopy(packet, 5, data, 0, dataLength);

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnPassThroughDataInd(data);
                }
            }

            return;
        }

        // 04 FF 03 01 53 Resp
        if (OCF.PASS_THROUGH_CMD == bOCF) {
            byte resp = packet[5];

            logBothWay("OnPasThroughDataResp", String.valueOf(resp));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnPasThroughDataResp(resp);
                }
            }

            return;
        }

        if (OCF.REPORT_PEQ_SECTOR_MODE_CHANGE == bOCF) {

            byte mode = packet[5];
            logBothWay("OnReportPeqSectorModeChanged", String.valueOf(mode));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportPeqSectorModeChanged(mode);
                }
            }

            return;
        }


        if (OCF.REPORT_MUSIC_SAMPLE_RATE == bOCF) {
            byte sampleRateEnum = packet[5];

            logBothWay("OnMusicSampleRateChanged", String.valueOf(sampleRateEnum));

            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnMusicSampleRateChanged(sampleRateEnum);
                }
            }
        }

        if (OCF.REPORT_PEQ_REAL_TIME_UI_DATA == bOCF) {
            int dataLength = packet[2] - 2;

            byte[] data = new byte[dataLength];

            System.arraycopy(packet, 5, data, 0, data.length);

            for (OnAirohaPeqControlListener listener : mPeqControlListenerMap.values()) {
                if (listener != null) {
                    listener.OnReportRealTimeUiDataInd(data);
                }
            }
        }

        if (OCF.REPORT_DRC_MODE == bOCF) {
            byte mode = packet[5];
            logBothWay("OnReportDrcMode", String.valueOf(mode));

            if (mReportDrcModeListener != null) {
                mReportDrcModeListener.OnReportDrcMode(mode);
            }


            return;
        }

        if (OCF.REPORT_SLAVE_STATUS == bOCF) {
            byte connected = packet[5];
            logBothWay("OnReportSlaveStatus", String.valueOf(connected));

            for (OnAirohaFollowerExistingListener listener : mSlaveConnectionListenerMap.values()) {
                if (listener != null) {
                    //                    Sent from BT when APP connected and slave connected/disconnected
                    //                    Indication:
                    //                    04 FF 03 ED 4A XX
                    //                    XX : Slave connected or not - 0: disconnect 1:Connect
                    if (connected == (byte) 0x01) {
                        listener.OnSlaveConnected(true);
                    } else {
                        listener.OnSlaveConnected(false);
                    }
                }
            }
        }
    }

    private static boolean isDspSuspendCmd(byte[] packet) {
        // [04] [FF] [03] [10] [49] [00]
        // [   ]  [   ]  [   ]  [   ]  [   ]  [status]
        return packet[3] == OCF.SUSPEND_DSP;
    }

    private static boolean isDspResumeCmd(byte[] packet) {
        // [04] [FF] [03] [11] [49] [00]
        // [   ]  [   ]  [   ]  [   ]  [   ]  [status]
        return packet[3] == OCF.RESUME_DSP;
    }

    private static boolean isKeyCmd(byte[] packet) {
        // [04] [FF] [05] [01] [49] [100E] [00]
        // [   ]  [   ]  [   ]  [   ]  [   ]  [status]
        return packet[2] == 0x05;
    }

    private void doKeyCodeCmdHandler(byte[] packet) {
        byte[] keyCodeArray = Arrays.copyOfRange(packet, SppPacketIndex.KEY_CODE_BEGIN, SppPacketIndex.KEY_CODE_END);
        byte status = packet[SppPacketIndex.AUDIO_TRANSPARENCY_TOGGLE_STATUS];


        if (keyCodeArray[0] == KeyCode.AUDIO_TRANSPARENCY[0] && keyCodeArray[1] == KeyCode.AUDIO_TRANSPARENCY[1]) {
            logBothWay("OnAudioTransparencyToggleResp", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnAudioTransparencyToggleResp(status);
                }
            }

            return;
        }

        if (keyCodeArray[0] == KeyCode.ROLE_SWITCH[0] && keyCodeArray[1] == KeyCode.ROLE_SWITCH[1]) {
            logBothWay("OnRoleSwitchResp", String.valueOf(status));

            for (OnAirohaMmiEventListener listener : mMmiEventListenerMap.values()) {
                if (listener != null) {
                    listener.OnRoleSwitchResp(status);
                }
            }

            return;
        }
    }

    private void logBothWay(String event, String arg) {
        String formated = event + ": " + arg;
        Log.d(TAG, formated);
        mAirohaLink.logToFile(TAG, formated);
    }
}
