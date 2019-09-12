package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdSetConfigReg;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/2.
 */
public class ACL_2_1_SET_CONFIG_REG extends AclCmd implements IAclHandleResp {

    private final String TAG = "SET_CONFIG_REG";
    private int retryCnt = 0;
    private boolean isCmdPass = false;
    private boolean mIsCompleted;

    private IAclHandleResp mNextCmd;
    private String mStatus;

    public ACL_2_1_SET_CONFIG_REG(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isConfigRegCmd(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [02] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return packet[7] == ACL_OCF.ACL_VCMD_FLASH_SET_CONFIGURATION_REGISTER && packet[8] == ACL_OGF.getAclVcmd();
    }

//    @Override
//    private byte[] getCommand() {
//        byte[] cmd = (new CmdSetConfigReg()).getRaw();
//
//        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
//        AirohaOtaLog.LogToFile("CONFIG REGISTER SEND: " + log + "\n");
//
//        return cmd;
//    }

    @Override
    public void SendCmd() {
        byte[] cmd = (new CmdSetConfigReg()).getRaw();

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("CONFIG REGISTER SEND: " + log + "\n");

        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [02] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]

        byte status = packet[9];

        Log.d(TAG, " status:" + status);

        isCmdPass = status == (byte) 0x00;

        Log.d(TAG, "cmd pass: " + isCmdPass);
        AirohaOtaLog.LogToFile("CONFIG REGISTER RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isConfigRegCmd(packet))
            return;

        ParsePacket(packet);
        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("CONFIG REGISTER RECEIVE: " + log + "\n");

        if (isCmdPass) {
            mStatus = "OTA_REG_PASS";

            mIsCompleted = true;

        } else {
            retryCnt += 1;
            SendCmd();
            if (retryCnt >= 5) {
                mStatus = "OTA_REG_FAIL";

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
