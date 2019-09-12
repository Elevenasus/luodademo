package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.flash.FlashSize;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/2.
 */
public class ACL_4_EXTERNAL_ERASE extends AclCmd implements IAclHandleResp {

    private final String TAG = "EXTERNAL_ERASE";
    private int eraseStartAddr = 0x00; // 2017.09.18, Daniel: all external flash starts from 0x00;
    private int eraseEndAddr = 0;
    private int eraseNowAddr = 0;
    private int retryCnt = 0;
    private int percent = 0;
    private int cmdCount = 0;
    private ACL_4_1_POLLING_ERASE polling;

    private boolean isCmdPass = false;

    private boolean mIsCompleted;
    private boolean mIsRetryFailed;

    private IAclHandleResp mNextCmd;

    private String mStatus;

    private boolean mIsForDemoSound = false;

    public void setForDemoSound(){
        mIsForDemoSound = true;
    }

    private ACL_4_1_POLLING_ERASE.OnPollingStateListener mOnPollingStateListener = new ACL_4_1_POLLING_ERASE.OnPollingStateListener() {
        @Override
        public void OnPollingComplete() {
            AirohaOtaLog.LogToFile("pollingHandler: OTA_POLLING_COMPLETE\n");
            eraseNowAddr += 0x1000;
            retryCnt = 0;
            cmdCount++;
            if (cmdCount == percent) {
                cmdCount = 0;
                mAirohaOtaFlowMgr.updateProgress();
            }

            if (eraseNowAddr < eraseEndAddr) {
                byte[] cmd = getCommand();
                isCmdPass = false;
                SendCmdToTarget(cmd);
            } else {
                // stop
                mIsCompleted = true;
            }
        }

        @Override
        public void OnPollingFail() {
            AirohaOtaLog.LogToFile("pollingHandler: OTA_POLLING_FAIL\n");
            mIsCompleted = false;
        }
    };


    public ACL_4_EXTERNAL_ERASE(AirohaOtaFlowMgr mgr) {
        super(mgr);
        polling = new ACL_4_1_POLLING_ERASE(mgr);
        polling.setListener(mOnPollingStateListener);
    }

    private static boolean isExternalEraseCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [1A] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return packet[7] == ACL_OCF.ACL_VCMD_SPIFLASH_SECTOR_ERASE && packet[8] == ACL_OGF.getAclVcmd();
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;
        byte[] addr = Converter.intToByteArray(eraseNowAddr);

        Log.d("erase addr: ", "" + eraseNowAddr);
        AirohaOtaLog.LogToFile("ERASE ADDR NOW: " + eraseNowAddr + "\n");

        cmd = new byte[]{0x02, 0x00, 0x0F, 0x05, 0x00,
                ACL_OCF.ACL_VCMD_SPIFLASH_SECTOR_ERASE,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
        };

        return cmd;
    }

    @Override
    public void SendCmd() {
        initStartAddress();
        CountProgressPercent();
        eraseNowAddr = eraseStartAddr;
        byte[] cmd = getCommand();
        SendCmdToTarget(cmd);
    }

    private void CountProgressPercent() {
        percent = (eraseEndAddr - eraseStartAddr) / 0x1000;
        percent = percent / 20;
    }

    private void initStartAddress() {
        int flashSize = mAirohaOtaFlowMgr.getFlashSize();

        if (flashSize == FlashSize.M8) {
            eraseStartAddr = 0x000000;
            eraseEndAddr = 0x0FFFFF;
        }
        if (flashSize == FlashSize.M16) {
            eraseStartAddr = 0x100000;
            eraseEndAddr = 0x1FFFFF;
        }
        if (flashSize == FlashSize.M32) {
            eraseStartAddr = 0x300000;
            eraseEndAddr = 0x3FFFFF;
        }
        if (flashSize == FlashSize.M64) {
            eraseStartAddr = 0x700000;
            eraseEndAddr = 0x7FFFFF;
        }
        if (flashSize == FlashSize.M128) {
            eraseStartAddr = 0xF00000;
            eraseEndAddr = 0xFFFFFF;
        }

        if (mIsForDemoSound) {
            eraseStartAddr = 0x00;
        }

        AirohaOtaLog.LogToFile("ERASE ADDR FROM: " + eraseStartAddr + "\n");
        AirohaOtaLog.LogToFile("ERASE ADDR TO: " + eraseEndAddr + "\n");
    }

    private void SendCmdToTarget(byte[] cmd) {
        mAirohaLink.sendCommand(cmd);
        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("ERASE SEND: " + log + "\n");
    }

    //    @Override
    public void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [1A] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        byte status = packet[9];

        Log.d(TAG, " status:" + status);

        isCmdPass = status == (byte) 0x00;

        Log.d(TAG, "cmd pass: " + isCmdPass);
        AirohaOtaLog.LogToFile("ERASE RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (ACL_4_1_POLLING_ERASE.isPollingCmd(packet)) {

            polling.handleResp(packet);

            return;
        }

        if (!isExternalEraseCmd(packet))
            return;

        ParsePacket(packet);

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("ERASE RECEIVE: " + log + "\n");

        if (isCmdPass) {
            polling.SendCmd();
            mStatus = "OTA_ERASE_PASS";
        } else {
            retryCnt += 1;
            if (retryCnt >= 5) {
                mStatus = "OTA_ERASE_FAIL";

                mIsRetryFailed = true;
            } else {
                if (eraseNowAddr < eraseEndAddr) {
                    mStatus = "OTA_ERASE_FAIL_RETRY";

                    byte[] cmd = getCommand();
                    isCmdPass = false;
                    SendCmdToTarget(cmd);
                }
            }
        }
        Log.d("retryCnt: ", "" + retryCnt);
        AirohaOtaLog.LogToFile("ERASE RETRY CNT: " + retryCnt + "\n");
    }

    @Override
    public IAclHandleResp getNextCmd() {
        return mNextCmd;
    }

    @Override
    public void setNextCmd1(IAclHandleResp cmd) {
//        mNextCmd.setFlashInfo(mFlashStru);
        mNextCmd = cmd;
    }

    @Override
    public void setNextCmd2(IAclHandleResp cmd) {

    }

    @Override
    public String getStatus() {
        return mStatus;
    }

    @Override
    public boolean isCompleted() {
        return mIsCompleted;
    }

    @Override
    public boolean isRetryFailed() {
        return mIsRetryFailed;
    }
}
