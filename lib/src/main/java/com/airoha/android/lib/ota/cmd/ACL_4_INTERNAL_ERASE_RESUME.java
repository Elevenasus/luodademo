package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdInternalErase;
import com.airoha.android.lib.ota.flash.FlashSize;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;

import java.util.LinkedList;

public class ACL_4_INTERNAL_ERASE_RESUME extends AclCmd implements IAclHandleResp {

    private final String TAG = "INTERNAL_ERASE_RESUME";

    private LinkedList<CmdInternalErase> mListCmdInternalErase;

    private static final int INT_64K = 0x10000;
    private int mEraseStartAddr = 0;
    private int mEraseEndAddr = 0;

    private boolean mIsCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;

    private IAclHandleResp mNextCmd;

    private String mStatus = "";

    private int mSizeOfInitList = 0;
    private int mRespCounter = 0;

    public ACL_4_INTERNAL_ERASE_RESUME(AirohaOtaFlowMgr mgr){
        super(mgr);
    }

    public void setCmdList(LinkedList<CmdInternalErase> cmdList){
        mListCmdInternalErase = cmdList;

        mSizeOfInitList = cmdList.size();
    }

    @Override
    public void SendCmd() {
        //PrepareInitData();

        mAirohaLink.sendCommand(mListCmdInternalErase.poll().getRaw());
    }



    @Override
    public void handleResp(byte[] packet) {
        if (!isInternalEraseCmd(packet))
            return;

        ParsePacket(packet);
    }

    private void PrepareInitData() {
        //if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.INTERNAL.ordinal()) {
        if (mAirohaOtaFlowMgr.getFlashSize() == FlashSize.M16) {
            mEraseStartAddr = 0x100000;
            mEraseEndAddr = 0x1FFFFF;
        }
        if (mAirohaOtaFlowMgr.getFlashSize() == FlashSize.M32) {
            mEraseStartAddr = 0x200000;
            mEraseEndAddr = 0x3FFFFF;
        }
        //}

        AirohaOtaLog.LogToFile("ERASE ADDR FROM: " + mEraseStartAddr + "\n");
        AirohaOtaLog.LogToFile("ERASE ADDR TO: " + mEraseEndAddr + "\n");

        // gen data

        mListCmdInternalErase = new LinkedList<>();
        while (mEraseStartAddr < mEraseEndAddr){
            CmdInternalErase cmd = new CmdInternalErase(mEraseStartAddr, ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_64K);

            mListCmdInternalErase.add(cmd);

            mEraseStartAddr += INT_64K;
        }

    }

    private void ParsePacket(final byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [05] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        final byte status = packet[9];

        Log.d(TAG, " status: " + status);

        mIsCmdPass = status == (byte) 0x00;

        Log.d(TAG, "cmd pass: " + mIsCmdPass);
        AirohaOtaLog.LogToFile("ERASE RESULT: " + mIsCmdPass + "\n");


        if(mIsCmdPass){
            mRespCounter++;

            mAirohaOtaFlowMgr.notifyMessageToUser(String.format("Erasing: %d/%d", mRespCounter, mSizeOfInitList));

            if(mListCmdInternalErase.size() == 0){
                mIsCompleted = true;
            }else {
                mAirohaLink.sendCommand(mListCmdInternalErase.poll().getRaw());
            }
        }
    }

    private static boolean isInternalEraseCmd(final byte[] packet) {
        // [02] [00] [0F] [05] [00] [01] [00] [05] [04] [00]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
        return (packet[7] == ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_64K ||
                packet[7] == ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_4K)&& packet[8] == ACL_OGF.getAclVcmd();
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
