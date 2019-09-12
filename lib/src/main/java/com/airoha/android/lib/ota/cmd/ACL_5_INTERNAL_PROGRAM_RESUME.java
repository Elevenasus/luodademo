package com.airoha.android.lib.ota.cmd;

import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdInternalProgram;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ACL_5_INTERNAL_PROGRAM_RESUME extends AclCmd implements IAclHandleResp {

    private final String TAG = "INTERNAL_PROGRAM";
    private Context mCtx;
    private int mProgramStartAddr;
    private int mResponseIdx = 0;
    private int mPercent = 0;
    private int mCmdCount = 0;
    private List<CmdItem> cmdTableList = new ArrayList<>();

    private boolean isCmdPass = false;

    private boolean mIsCompeted;
    private boolean mIsRetryFailed;

    private IAclHandleResp mNextCmd;

    private String mStatus;

    private boolean mIsCheckFwSupported = false;

    private LinkedList<CmdInternalProgram> mListCmdInternalProgram;

    private int mSizeOfInitList = 0;
    private int mRespCounter = 0;

    public void setCmdList(LinkedList<CmdInternalProgram> listCmdInternalProgram) {
        this.mListCmdInternalProgram = listCmdInternalProgram;

        mSizeOfInitList = listCmdInternalProgram.size();
    }

    public ACL_5_INTERNAL_PROGRAM_RESUME(AirohaOtaFlowMgr mgr) {
        super(mgr);
        mCtx = mgr.getContext();
    }

    @Override
    public void SendCmd() {

        if(mListCmdInternalProgram.size() >=3){
            for (int i = 0; i < 3; i++){
                mAirohaLink.sendCommand(mListCmdInternalProgram.poll().getRaw());
            }
        }else {
            mAirohaLink.sendCommand(mListCmdInternalProgram.poll().getRaw());
        }

    }

    private static boolean isInternalProgramCmd(byte[] packet) {
        // [02] [00] [0F] [09] [00] [01] [00] [08] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b2]
        return packet[7] == ACL_OCF.ACL_VCMD_FLASH_PAGE_PROGRAM && packet[8] == ACL_OGF.getAclVcmd();
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isInternalProgramCmd(packet))
            return;

        mRespCounter++;

        // [02] [00] [0F] [09] [00] [01] [00] [08] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b1] [addr_b0]
        byte status = packet[9];

        if(status == 0x00){

            mAirohaOtaFlowMgr.notifyMessageToUser(
                    String.format("Addr: 0x %02X %02X %02X programmed (progress %d/%d)",
                            packet[10], packet[11], packet[12], mRespCounter, mSizeOfInitList));

            if(mListCmdInternalProgram.size() == 0) {
                mIsCompeted = true;
            }else {
                mAirohaLink.sendCommand(mListCmdInternalProgram.poll().getRaw());
            }
        }
    }

    private void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [09] [00] [01] [00] [08] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b1] [addr_b0]
        byte status = packet[9];
        byte b2 = packet[10];
        byte b1 = packet[11];
        byte b0 = packet[12];

        for (int i = 0; i < cmdTableList.size(); i++) {
            if (cmdTableList.get(i).cmd[7] == b2 && cmdTableList.get(i).cmd[8] == b1 && cmdTableList.get(i).cmd[9] == b0) {
                mResponseIdx = i;
                break;
            }
        }

        byte[] cmd = cmdTableList.get(mResponseIdx).cmd;

        Log.d(TAG, " status:" + status);
        Log.d(TAG, " addr:" + "" + b2 + " " + b1 + " " + b0);

        if (status == (byte) 0x00 &&
                b2 == cmd[7] &&
                b1 == cmd[8] &&
                b0 == cmd[9]) {
            isCmdPass = true;
            cmdTableList.get(mResponseIdx).isPass = true;
        } else isCmdPass = false;

        Log.d("cmd pass: ", "" + isCmdPass);
        AirohaOtaLog.LogToFile("PROGRAM RESULT: " + isCmdPass + "\n");
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
        return null;
    }

    @Override
    public boolean isCompleted() {
        return mIsCompeted;
    }

    @Override
    public boolean isRetryFailed() {
        return false;
    }
}
