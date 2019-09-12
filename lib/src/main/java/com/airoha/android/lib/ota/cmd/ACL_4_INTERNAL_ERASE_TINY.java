package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

public class ACL_4_INTERNAL_ERASE_TINY extends AclCmd implements IAclHandleResp {
    private static final int INT_4K = 0x1000;
    private final String TAG = "INTERNAL_ERASE_TINY";
    private int mEraseStartAddr = 0;
    private int mEraseLength = INT_4K; // at least 4K
    private int mEraseEndAddr = 0;
    private int mEraseNowAddr = 0;
    private int mRetryCnt = 0;
    private int mPercent = 0;
    private int mCmdCount = 0;
    private boolean mIsCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;
    private IAclHandleResp mNextCmd;
    private String mStatus;

    public ACL_4_INTERNAL_ERASE_TINY(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isInternalEraseCmd(final byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [05] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return packet[7] == ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_4K && packet[8] == ACL_OGF.getAclVcmd();
    }

    public void setEraseStartAddr(int eraseStartAddr) {
        this.mEraseStartAddr = eraseStartAddr;
    }

    public void setEraseLength(int eraseLength) {
        this.mEraseLength = eraseLength;
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;
        final byte[] addr = Converter.intToByteArray(mEraseNowAddr);

        Log.d(TAG, "ERASE ADDR NOW: " + mEraseNowAddr);
        AirohaOtaLog.LogToFile("ERASE ADDR NOW: " + mEraseNowAddr + "\n");

//        if (mFlashStru.flashType == FLASH_TYPE.INTERNAL.ordinal()) {
            AirohaOtaLog.LogToFile("ERASE ADDR NOW: ACL_VCMD_FLASH_SECTOR_ERASE_4K\n");
            cmd = new byte[]{0x02, 0x00, 0x0F, 0x05, 0x00,
                    ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_4K,
                    ACL_OGF.getAclVcmd(),
                    addr[2], addr[1], addr[0],
            };
//        }
        return cmd;
    }

    @Override
    public void SendCmd() {
        PrepareInitData();
        CountProgressPercent();
        mEraseNowAddr = mEraseStartAddr;
        final byte[] cmd = getCommand();
        SendCmdToTarget(cmd);
    }

    private void CountProgressPercent() {
        mPercent = (mEraseEndAddr - mEraseStartAddr) / INT_4K;
        mPercent = mPercent / 20;
    }

    private void PrepareInitData() {
//        if (mFlashStru.flashType == FLASH_TYPE.INTERNAL.ordinal()) {
//            if (mFlashStru.flashSize.equals(FlashSize.M_16)) {
//                mEraseStartAddr = 0x100000;
//                mEraseEndAddr = 0x1FFFFF;
//            }
//            if (mFlashStru.flashSize.equals(FlashSize.M_32)) {
//                mEraseStartAddr = 0x200000;
//                mEraseEndAddr = 0x3FFFFF;
//            }
//        }

        mEraseEndAddr = mEraseStartAddr + mEraseLength - 1;

        AirohaOtaLog.LogToFile("ERASE ADDR FROM: " + mEraseStartAddr + "\n");
        AirohaOtaLog.LogToFile("ERASE ADDR TO: " + mEraseEndAddr + "\n");
    }

    private void SendCmdToTarget(final byte[] cmd) {
        mAirohaLink.sendCommand(cmd);
        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("ERASE SEND: " + log + "\n");
    }

    //    @Override
    private void ParsePacket(final byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [05] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        final byte status = packet[9];

        Log.d(TAG, " status: " + status);

        mIsCmdPass = status == (byte) 0x00;

        Log.d(TAG, "cmd pass: " + mIsCmdPass);
        AirohaOtaLog.LogToFile("ERASE RESULT: " + mIsCmdPass + "\n");
    }

    @Override
    public void handleResp(final byte[] packet) {
        if (!isInternalEraseCmd(packet))
            return;

        ParsePacket(packet);

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("ERASE RECEIVE: " + log + "\n");

        if (mIsCmdPass) {
            mEraseNowAddr += INT_4K;
            mRetryCnt = 0;
            mCmdCount++;
            if (mCmdCount == mPercent) {
                mCmdCount = 0;
                mAirohaOtaFlowMgr.updateProgress();
            }

        } else {
            mRetryCnt += 1;
        }
        Log.d("mRetryCnt: ", "" + mRetryCnt);
        AirohaOtaLog.LogToFile("ERASE RETRY CNT: " + mRetryCnt + "\n");

        if (mRetryCnt >= 5) {

            mStatus = "OTA_ERASE_FAIL";

            mIsCompleted = false;

            mIsRetryFailed = true;

        } else {
            if (mEraseNowAddr < mEraseEndAddr) {
                final byte[] cmd = getCommand();
                mIsCmdPass = false;
                SendCmdToTarget(cmd);
            } else {
                // stop

                mStatus = "OTA_ERASE_PASS";

                mIsCompleted = true;
            }
        }
    }

    @Override
    public IAclHandleResp getNextCmd() {
        return mNextCmd;
    }

    @Override
    public void setNextCmd1(final IAclHandleResp cmd) {
        mNextCmd = cmd;
    }

    @Override
    public void setNextCmd2(final IAclHandleResp cmd) {

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
