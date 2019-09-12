package com.airoha.android.lib.ota.cmd;

import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.ContentChecker;
import com.airoha.android.lib.util.Converter;

//import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Evonne.Hsieh on 2016/6/15.
 */
public class ACL_1_READ_BOOTCODE extends AclCmd implements IAclHandleResp {
    private final String TAG = "READ_BOOTCODE";
    private final Context mCtx;
    private final int endAddr = 0x1FFF;
    private int retryCnt = 0;
    private int percent = 0;
    private int cmdCount = 0;
    private int startAddr = 0;
    private boolean isCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;
    private IAclHandleResp mNextInternalCmd;
    private IAclHandleResp mNextExternalCmd;
    private IAclHandleResp mNextCmd;

    private String mStatus;

    public ACL_1_READ_BOOTCODE(AirohaOtaFlowMgr mgr) {
        super(mgr);
        mCtx = mgr.getContext();
    }

    private static boolean isReadCmd(byte[] packet) {
        // [02] [00] [0F] [06] [01] [09] [04] [data.....]
        // [   ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [b2] [b1] [b0]
        return packet[5] == ACL_OCF.ACL_VCMD_FLASH_READ && packet[6] == ACL_OGF.getAclVcmd();
    }

    private void CountProgressPercent() {
        percent = (endAddr - startAddr) / 0x100;
        percent = percent / 2;
    }

//    @Override
    private byte[] getCommand() {
        byte[] addr = Converter.intToByteArray(startAddr);
        byte[] len = Converter.ShortToBytes((short) 256);
        byte[] cmd = new byte[]{0x02, 0x00, 0x0F, 0x07, 0x00,
                ACL_OCF.ACL_VCMD_FLASH_READ,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
                len[1], len[0]
        };

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("READ SEND: " + log + "\n");

        return cmd;
    }

    @Override
    public void SendCmd() {
        CountProgressPercent();
        byte[] cmd = getCommand();
        mAirohaLink.sendCommand(cmd);
    }

    //    @Override
    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [06] [01] [09] [04] [data.....]
        // [   ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [b2] [b1] [b0]
        byte status = packet[7];

        Log.d(TAG, " status:" + status);

        isCmdPass = status == (byte) 0x00;

        Log.d(TAG, "cmd pass: " + isCmdPass);
        AirohaOtaLog.LogToFile("READ RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isReadCmd(packet))
            return;

        ParsePacket(packet);
        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("READ RECEIVE: " + log + "\n");

        if (isCmdPass) {
            startAddr += 0x100;
            // write file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mCtx.getFilesDir() + "/" + AirohaOtaFlowMgr.DEVICE_BOOT_CODE_NAME, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int i = 11; i < packet.length; i++) {
                try {
                    byte o = packet[i];
                    fos.write(o);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (cmdCount == percent) {
                cmdCount = 0;

                mAirohaOtaFlowMgr.updateProgress();
            }

            if (startAddr < endAddr) {
                isCmdPass = false;
                SendCmd();
            } else {
                // stop
                File f;
                File f2;

                f = new File(mCtx.getFilesDir() + "/" + AirohaOtaFlowMgr.DEVICE_BOOT_CODE_NAME);

                if (mAirohaOtaFlowMgr.isUsingLocalFile()) {
                    f2 = new File(mAirohaOtaFlowMgr.getBootcodeFileName());
                } else {
                    f2 = new File(mCtx.getFilesDir() + "/" + AirohaOtaFlowMgr.DEFAULT_BOOTCODE_FILE);
                }


                if (0 == ContentChecker.compareFileContent(f, f2)) {

                    if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.INTERNAL.ordinal()) {
                        mNextCmd = mNextInternalCmd;
                    } else if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.EXTERNAL.ordinal()) {
                        mNextCmd = mNextExternalCmd;
                    }

                    mStatus = "READ BOOTCODE PASS";
                    mIsCompleted = true;
                } else {

                    mStatus = "READ BOOTCODE FAIL";
                    mIsCompleted = false;
                }

                f.delete();
            }
        } else {
            // retry
            retryCnt += 1;
            Log.d("retryCnt: ", "" + retryCnt);
            AirohaOtaLog.LogToFile("READ RETRY CNT: " + retryCnt + "\n");
            if (retryCnt >= 5) {

                mIsRetryFailed = true;
                mStatus = "READ BOOTCODE RETRY FAIL";

                File f = new File(mCtx.getFilesDir() + "/" + AirohaOtaFlowMgr.DEVICE_BOOT_CODE_NAME);
                f.delete();
            } else {
                if (startAddr < endAddr) {
                    isCmdPass = false;
                    SendCmd();
                }
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