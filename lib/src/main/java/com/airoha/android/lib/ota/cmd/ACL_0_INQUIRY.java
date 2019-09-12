package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdInquiry;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class ACL_0_INQUIRY extends AclCmd implements IAclHandleResp {

    private final String TAG = "INQUIRY";
    private int retryCnt = 0;
    private boolean isCmdPass = false;
    private boolean mIsCompleted;
    private IAclHandleResp mNextCmd;
    private String mStatus;

    public ACL_0_INQUIRY(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isInquiryCmd(byte[] packet) {
        // [02] [00] [0F] [06] [00] [01] [00] [17] [04] [00] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [type]
        return packet[7] == ACL_OCF.ACL_VCMD_FLASH_INQUIRY_INTERNAL_EXTERNAL_UPDATE && packet[8] == ACL_OGF.getAclVcmd();
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = (new CmdInquiry()).getRaw();

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("INQUIRY SEND: " + log + "\n");

        return cmd;
    }

    @Override
    public void SendCmd() {
        byte[] cmd = getCommand();

        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [06] [00] [01] [00] [17] [04] [00] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [type]
        // type: 0->internal 1->external
        byte status = packet[9];

        Log.d(TAG, " status: " + status);

        if (status == (byte) 0x00) {
            // cmd pass
            mAirohaOtaFlowMgr.setFlashType(packet[10]);

            isCmdPass = true;
            Log.d(TAG, " type: " + packet[10]);
            AirohaOtaLog.LogToFile("INQUIRY FLASH TYPE: " + packet[10] + "\n");

            mStatus = "INQUIRY PASS";
        } else {
            isCmdPass = false;

            mStatus = "INQUIRY PASS";
        }

        Log.d(TAG, "cmd pass: " + isCmdPass);
        AirohaOtaLog.LogToFile("INQUIRY RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isInquiryCmd(packet))
            return;

        ParsePacket(packet);
        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("INQUIRY RECEIVE: " + log + "\n");

        if (isCmdPass) {
            mIsCompleted = true;
        } else {
            retryCnt += 1;
            byte[] cmd = getCommand();
            mAirohaLink.sendCommand(cmd);
            if (retryCnt >= 5) {
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
        return false;
    }
}
