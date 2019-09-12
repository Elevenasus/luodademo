package com.airoha.android.lib.ota.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.CRC8;
import com.airoha.android.lib.util.Converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Evonne.Hsieh on 2016/6/3.
 */
public class ACL_5_INTERNAL_PROGRAM_TINY extends AclCmd implements IAclHandleResp {
    private final String TAG = "INTERNAL_PROGRAM";
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

    private final int   CRC_POLYNOM = 0;
    private final byte  CRC_INITIAL = 0;
    private CRC8 crc8;

    public ACL_5_INTERNAL_PROGRAM_TINY(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

    private static boolean isInternalProgramCmd(byte[] packet) {
        // [02] [00] [0F] [09] [00] [01] [00] [08] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b2]
        return packet[7] == ACL_OCF.ACL_VCMD_FLASH_PAGE_PROGRAM && packet[8] == ACL_OGF.getAclVcmd();
    }

    public void setProgramStartAddr(int programStartAddr) {
        this.mProgramStartAddr = programStartAddr;
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;
        byte[] addr = Converter.intToByteArray(mProgramStartAddr);
        Log.d("flash addr: ", "" + mProgramStartAddr);

        cmd = new byte[]{0x02, 0x00, 0x0F, 0x06, 0x01,
                ACL_OCF.ACL_VCMD_FLASH_PAGE_PROGRAM,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
        };

        return cmd;
    }

    @Override
    public void SendCmd() {
        AirohaOtaLog.LogToFile("PROGRAM START ADDR: " + mProgramStartAddr + "\n");

        PrepareCmdListForTinyBin();
        CountProgressPercent();

        SendCmdToTarget(cmdTableList.get(0).cmd);
        cmdTableList.get(0).isSend = true;

        SendCmdToTarget(cmdTableList.get(1).cmd);
        cmdTableList.get(1).isSend = true;

        SendCmdToTarget(cmdTableList.get(2).cmd);
        cmdTableList.get(2).isSend = true;
    }

    private void CountProgressPercent() {
        mPercent = cmdTableList.size() / 76;
    }

    /**
     * Copy from branch  SVN17437
     */
    private void PrepareCmdListForTinyBin(){
        AirohaOtaLog.LogToFile("PROGRAM SEND: ACL_VCMD_FLASH_PAGE_PROGRAM\n");

        InputStream ios = null;
        try {
            byte[] buffer = new byte[256]; // 256 data, 1 crc need to be calculated
            try {
                ios = new FileInputStream(mAirohaOtaFlowMgr.getBinFileName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                AirohaOtaLog.LogToFile(e.getMessage() + "\n");
            }

            int read = 0;

            while ((read = ios.read(buffer)) != -1) {
                byte[] data = new byte[256];
                Arrays.fill(data, (byte)0xFF);

                System.arraycopy(buffer, 0, data, 0, read);

                byte[] cmd = new byte[257+10];
                byte[] header = getCommand();
                System.arraycopy(header, 0, cmd, 0, header.length);

                // cmd = header + crc + data
                byte crc = 0;
                crc8 = new CRC8(CRC_INITIAL);
                crc8.update(data);
                crc = (byte)crc8.getValue();

                cmd[10] = crc;

                System.arraycopy(data, 0, cmd, 11, data.length);

                cmdTableList.add(new CmdItem(cmd, false, 0, false));
                mProgramStartAddr += 0x100;
            }
            ios.close();

        } catch (IOException e) {
            AirohaOtaLog.LogToFile("PROGRAM CMD TABLE FAIL" + "\n");
        }
    }

    private void SendCmdToTarget(byte[] cmd) {
        mAirohaLink.sendCommand(cmd);
        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("PROGRAM SEND: " + log + "\n");
    }

    private byte[] GetNextCmd() {
        byte[] cmd = null;
        for (int i = 0; i < cmdTableList.size(); i++) {
            if (!cmdTableList.get(i).isSend) {
                cmd = cmdTableList.get(i).cmd;
                cmdTableList.get(i).isSend = true;
                break;
            }
        }
        return cmd;
    }

    private byte[] GetRetryCmd() {

        byte[] cmd = null;
        for (int i = 0; i < cmdTableList.size(); i++) {
            if (cmdTableList.get(i).isSend && !cmdTableList.get(i).isPass) {
                if (cmdTableList.get(i).retryCnt >= 5) {

                    mIsRetryFailed = true;
                } else {
                    cmd = cmdTableList.get(i).cmd;
                    cmdTableList.get(i).retryCnt += 1;
                }

                Log.d("retryCnt: ", "" + cmdTableList.get(i).retryCnt);
                AirohaOtaLog.LogToFile("PROGRAM RETRY CNT: " + cmdTableList.get(i).retryCnt + "\n");
                break;
            }
        }
        return cmd;
    }

    private boolean IsContinue() {
        for (int i = 0; i < cmdTableList.size(); i++) {
            if (!cmdTableList.get(i).isPass) {
                return true;
            }
        }
        return false;
    }

    //    @Override
    public void ParsePacket(byte[] packet) {
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
    public void handleResp(byte[] packet) {
        if (!isInternalProgramCmd(packet))
            return;

        if (mIsRetryFailed)
            return;

        ParsePacket(packet);

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("PROGRAM RECEIVE: " + log + "\n");

        if (IsContinue()) {
            if (isCmdPass) {
                isCmdPass = false;
                mCmdCount++;
                if (mCmdCount == mPercent) {
                    mCmdCount = 0;
                    mAirohaOtaFlowMgr.updateProgress();
                }
                byte[] cmd = GetNextCmd();
                if (cmd != null)
                    SendCmdToTarget(cmd);
            } else {
                byte[] cmd = GetRetryCmd();
                if (cmd != null)
                    SendCmdToTarget(cmd);
            }
        } else {
            mIsCompeted = true;

            Log.d(TAG, "completed");
        }
    }

    @Override
    public IAclHandleResp getNextCmd() {
        return null;
    }

    @Override
    public void setNextCmd1(IAclHandleResp cmd) {

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
        Log.d(TAG, "completed: " + mIsCompeted);

        return mIsCompeted;
    }

    @Override
    public boolean isRetryFailed() {
        return mIsRetryFailed;
    }
}