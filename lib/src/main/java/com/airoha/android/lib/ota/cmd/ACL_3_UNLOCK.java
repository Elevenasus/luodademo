package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdUnlock;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class ACL_3_UNLOCK extends AclCmd implements IAclHandleResp {

    private final String TAG = "UNLOCK";
    private int retryCnt = 0;
    private boolean isCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;

    private IAclHandleResp mNextInternalCmd;
    private IAclHandleResp mNextExternalCmd;

    private IAclHandleResp mNextCmd;

    private String mStatus;

    public ACL_3_UNLOCK(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isUnlockCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [12] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return (packet[7] == ACL_OCF.ACL_VCMD_FLASH_UNLOCK_ALL && packet[8] == ACL_OGF.getAclVcmd()) ||
                (packet[7] == ACL_OCF.HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL && packet[8] == ACL_OGF.getAclVcmd());
    }

//    @Override
//    private byte[] getCommand() {
//        byte[] cmd = null;
//
//        cmd = (new CmdUnlock(mAirohaOtaFlowMgr.getFlashType())).getRaw();

//        if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.INTERNAL.ordinal()) {
//            AirohaOtaLog.LogToFile("UNLOCK SEND: ACL_VCMD_FLASH_UNLOCK_ALL\n");
//            cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
//                    ACL_OCF.ACL_VCMD_FLASH_UNLOCK_ALL,
//                    ACL_OGF.getAclVcmd(),
//            };
//        }
//        if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.EXTERNAL.ordinal()) {
//            AirohaOtaLog.LogToFile("UNLOCK SEND: HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL\n");
//            cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
//                    ACL_OCF.HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL,
//                    ACL_OGF.getAclVcmd(),
//            };
//        }

//        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
//        AirohaOtaLog.LogToFile("UNLOCK SEND: " + log + "\n");
//
//        return cmd;
//    }

    @Override
    public void SendCmd() {
        byte[] cmd = null;

        cmd = (new CmdUnlock(mAirohaOtaFlowMgr.getFlashType())).getRaw();

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("UNLOCK SEND: " + log + "\n");

        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [12] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]

        byte status = packet[9];

        Log.d(TAG, " status:" + status);

        if (status == (byte) 0x00) {
            isCmdPass = true;

            if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.INTERNAL.ordinal()) {
                mNextCmd = mNextInternalCmd;
            } else if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.EXTERNAL.ordinal()) {
                mNextCmd = mNextExternalCmd;
            }
        } else {
            isCmdPass = false;
        }

        Log.d("cmd pass: ", "" + isCmdPass);
        AirohaOtaLog.LogToFile("UNLOCK RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isUnlockCmd(packet))
            return;
        ParsePacket(packet);
        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("UNLOCK RECEIVE: " + log + "\n");

        if (isCmdPass) {
            mStatus = "OTA_UNLOCK_PASS";

            mIsCompleted = true;
        } else {
            retryCnt += 1;
            SendCmd();
            if (retryCnt >= 5) {
                mStatus = "OTA_UNLOCK_FAIL";

                mIsCompleted = false;
            }
        }
    }

    @Override
    public IAclHandleResp getNextCmd() {
        return mNextCmd;
    }

    @Override
    public void setNextCmd1(IAclHandleResp cmd) {
        mNextInternalCmd = cmd;
    }

    @Override
    public void setNextCmd2(IAclHandleResp cmd) {
        mNextExternalCmd = cmd;
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
