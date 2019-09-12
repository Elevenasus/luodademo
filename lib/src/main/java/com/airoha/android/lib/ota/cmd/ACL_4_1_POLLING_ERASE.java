package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/8.
 */
public class ACL_4_1_POLLING_ERASE extends AclCmd implements IAclHandleResp {

    private final String TAG = "POLLING_ERASE";
    private int retryCnt = 0;
    private boolean isCmdPass = false;

    private IAclHandleResp mNextCmd;

    private boolean mIsRetryFailed;

    private String mStatus;

    private OnPollingStateListener mListener;

    public ACL_4_1_POLLING_ERASE(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    static boolean isPollingCmd(byte[] packet) {
        // [02] [00] [0F] [06] [00] [01] [00] [20] [04] [00] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [operation]
        return packet[7] == ACL_OCF.ACL_VCMD_POLLING_FOR_ERASE_DONE && packet[8] == ACL_OGF.getAclVcmd();
    }

    public void setListener(OnPollingStateListener listener) {
        mListener = listener;
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                ACL_OCF.ACL_VCMD_POLLING_FOR_ERASE_DONE,
                ACL_OGF.getAclVcmd(),
        };

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("POLLING SEND: " + log + "\n");
        Log.d("POLLING SEND:  ", "" + log);

        return cmd;
    }

    @Override
    public void SendCmd() {
        byte[] cmd = getCommand();
        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [06] [00] [01] [00] [20] [04] [00] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [operation]

        byte status = packet[9];
        //The erase is done when write_operation is 0, otherwise 1 instead
        byte op = packet[10];
        Log.d(TAG + " status:", "" + status);

        isCmdPass = status == (byte) 0x00 && op == (byte) 0x00;

        Log.d("cmd pass: ", "" + isCmdPass);
        AirohaOtaLog.LogToFile("POLLING OPERATION: " + op + "\n");
        AirohaOtaLog.LogToFile("POLLING RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isPollingCmd(packet))
            return;

        ParsePacket(packet);

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("POLLING RECEIVE: " + log + "\n");

        if (isCmdPass) {
            retryCnt = 0;

            mListener.OnPollingComplete();

            mStatus = "OTA_POLLING_COMPLETE";
        } else {
            if (retryCnt < 10) { // TODO 2018.08.24 Daniel for AE temp testing
                retryCnt += 1;
                isCmdPass = false;
                SendCmd();
            } else {

                mListener.OnPollingFail();

                mStatus = "OTA_POLLING_FAIL";

                mIsRetryFailed = true;
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
        return isCmdPass;
    }

    @Override
    public boolean isRetryFailed() {
        return mIsRetryFailed;
    }

    public interface OnPollingStateListener {
        void OnPollingComplete();

        void OnPollingFail();
    }

}
