package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.flash.FlashSize;
import com.airoha.android.lib.ota.flash.FlashSizeLookup;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class ACL_2_EXTERNAL_INIT extends AclCmd implements IAclHandleResp {
    private final String TAG = "EXTERNAL_INIT";
    private byte manufactureId;
    private byte memType;
    private byte density;
    private int retryCnt = 0;
    private boolean isCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;
    private IAclHandleResp mNextCmd;

    private String mStatus;

    public ACL_2_EXTERNAL_INIT(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isExternalInitCmd(byte[] packet) {
        // [02] [00] [0F] [07] [00] [01] [00] [19] [04] [00] [C2] [15]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [id] [density]
        return packet[7] == ACL_OCF.ACL_VCMD_SPIFLASH_READ_MANUFACTURER_AND_MEMORYTYPE && packet[8] == ACL_OGF.getAclVcmd();
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;

        AirohaOtaLog.LogToFile("INIT SEND: ACL_VCMD_SPIFLASH_READ_MANUFACTURER_AND_MEMORYTYPE\n");
        cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                ACL_OCF.ACL_VCMD_SPIFLASH_READ_MANUFACTURER_AND_MEMORYTYPE,
                ACL_OGF.getAclVcmd()
        };

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("INIT SEND: " + log + "\n");

        return cmd;
    }

    @Override
    public void SendCmd() {
        byte[] cmd = getCommand();

        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [07] [00] [01] [00] [19] [04] [00] [C2] [15]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [id] [density]
        byte status = packet[9];

        Log.d(TAG, " status:" + status);

        if (status == (byte) 0x00) {
            // cmd pass
            isCmdPass = true;
            manufactureId = packet[10];
            memType = packet[11];
            density = packet[12];
            Log.d(TAG, " id:" + manufactureId);
            Log.d(TAG, " memType:" + memType);
            Log.d(TAG, " density:" + density);
            AirohaOtaLog.LogToFile("INIT MANUFACTURED ID: " + manufactureId + "\n");
            AirohaOtaLog.LogToFile("MEMTYPE: " + memType + "\n");
            AirohaOtaLog.LogToFile("INIT DENSITY: " + density + "\n");

            int flashSize = FlashSizeLookup.Inst().GetFlashSize(manufactureId, density);
            mAirohaOtaFlowMgr.setFlashSize(flashSize);

            AirohaOtaLog.LogToFile("FLASH SIZE: " + flashSize + "\n");

            if(flashSize == FlashSize.UNKNOWN){
                isCmdPass = false;
            }
        } else {
            isCmdPass = false;
        }

        Log.d(TAG, "cmd pass: " + isCmdPass);
        AirohaOtaLog.LogToFile("INIT RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isExternalInitCmd(packet))
            return;

        ParsePacket(packet);
        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("INIT RECEIVE: " + log + "\n");

        if (isCmdPass) {

            mStatus = "OTA_INIT_PASS";

            mIsCompleted = true;

        } else {
            retryCnt += 1;
            byte[] cmd = getCommand();
            mAirohaLink.sendCommand(cmd);
            if (retryCnt >= 5) {

                mStatus = "OTA_INIT_FAIL";

                mIsCompleted = false;

                mIsRetryFailed = true;

                Log.d(TAG, "unregister otareceiever, retry >5");
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
        return mIsRetryFailed;
    }
}
