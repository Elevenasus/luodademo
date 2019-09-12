package com.airoha.android.lib.ota.cmd;

import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.CRC8;
import com.airoha.android.lib.util.ContentChecker;
import com.airoha.android.lib.util.Converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evonne.Hsieh on 2016/6/3.
 */
public class ACL_5_EXTERNAL_PROGRAM_DEMOSOUND extends AclCmd implements IAclHandleResp {

    private final String TAG = "PROGRAM_DEMOSOUND";
    private final byte CRC_INITIAL = 0;
    private final byte[] CMD_HEADER = new byte[]{0x02, 0x00, 0x0F, 0x07, 0x01,
            ACL_OCF.ACL_VCMD_SPIFLASH_PAGE_PROGRM,
            ACL_OGF.getAclVcmd()};
    private Context mCtx;
    private int startAddr = 0x00; // 2017.09.18, Daniel: all external flash starts from 0x00;
    private int responseIdx = 0;
    private int percent = 0;
    private int cmdCount = 0;
    private List<CmdItem> cmdTableList = new ArrayList<>();
    private boolean isCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;
    private IAclHandleResp mNextCmd;
    private String mStatus;
    private CRC8 crc8;

    public ACL_5_EXTERNAL_PROGRAM_DEMOSOUND(AirohaOtaFlowMgr mgr) {
        super(mgr);
        mCtx = mgr.getContext();
    }

    private static boolean isExternalProgramCmd(byte[] packet) {
        // [02] [00] [0F] [09] [00] [01] [00] [1B] [04] [00] [   ] [   ] [   ] [   ]
        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [addr_b2] [addr_b1] [addr_b0]
        return packet[7] == ACL_OCF.ACL_VCMD_SPIFLASH_PAGE_PROGRM && packet[8] == ACL_OGF.getAclVcmd();
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;
        byte[] addr = Converter.intToByteArray(startAddr);
        Log.d("flash addr: ", "" + startAddr);

        cmd = new byte[]{0x02, 0x00, 0x0F, 0x07, 0x01,
                ACL_OCF.ACL_VCMD_SPIFLASH_PAGE_PROGRM,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
        };

        return cmd;
    }

    @Override
    public void SendCmd() {
//        initStartAddr();
        PrepareCmdList();
        CountProgressPercent();

        SendCmdToTarget(cmdTableList.get(0).cmd);
        cmdTableList.get(0).isSend = true;

        SendCmdToTarget(cmdTableList.get(1).cmd);
        cmdTableList.get(1).isSend = true;

        SendCmdToTarget(cmdTableList.get(2).cmd);
        cmdTableList.get(2).isSend = true;
    }

    private void CountProgressPercent() {
        percent = cmdTableList.size() / 76;
    }

//    private void initStartAddr() {
//        if (mFlashStru.flashSize.equals(FlashSize.M_8)) {
//            startAddr = 0x000000;
//        }
//        if (mFlashStru.flashSize.equals(FlashSize.M_16)) {
//            startAddr = 0x100000;
//        }
//        if (mFlashStru.flashSize.equals(FlashSize.M_32)) {
//            startAddr = 0x300000;
//        }
//        if (mFlashStru.flashSize.equals(FlashSize.M_64)) {
//            startAddr = 0x700000;
//        }
//        if (mFlashStru.flashSize.equals(FlashSize.M_128)) {
//            startAddr = 0xF00000;
//        }
//        AirohaOtaLog.logToFile("PROGRAM START ADDR: " + startAddr + "\n");
//    }

    private void PrepareCmdList() {
        AirohaOtaLog.LogToFile("PROGRAM SEND: ACL_VCMD_SPIFLASH_PAGE_PROGRM\n");

        InputStream fis = null;
        try {
            if (mAirohaOtaFlowMgr.isUsingLocalFile())
                fis = new FileInputStream(AirohaOtaFlowMgr.getExtFileName());
            else
                fis = new FileInputStream(mCtx.getFilesDir() + "/" + AirohaOtaFlowMgr.getExtFileName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            AirohaOtaLog.LogToFile(e.getMessage() + "\n");
        }


        try {
            byte[] binDataBuffer = new byte[256];//new byte[257];
            while (fis.read(binDataBuffer) != -1) {
                // 檢查是否全部都為0xFF，若是則不寫入
                if(!ContentChecker.isAllDummyHexFF(binDataBuffer)) {

                    byte[] cmd = new byte[256 + 10 + 1 + 1]; // 1byte for crc, 1 byte for extra 0

                    // copy header
                    System.arraycopy(CMD_HEADER, 0, cmd, 0, CMD_HEADER.length);

                    // copy crc
                    byte[] crc = new byte[2];
                    crc8 = new CRC8(CRC_INITIAL);
                    crc8.update(binDataBuffer);
                    crc[0] = (byte) crc8.getValue();
                    // initialed crs[1] = 0
                    System.arraycopy(crc, 0, cmd, 7, crc.length);

                    // copy address
                    byte[] addr = Converter.intToByteArray(startAddr);
                    // addr[2], addr[1], addr[0]
                    cmd[9] = addr[2];
                    cmd[10] = addr[1];
                    cmd[11] = addr[0];

                    // copy data
                    System.arraycopy(binDataBuffer, 0, cmd, 12, binDataBuffer.length);

                    cmdTableList.add(new CmdItem(cmd, false, 0, false));
                }

                startAddr += 0x100;
            }

            fis.close();
        } catch (IOException e) {
            AirohaOtaLog.LogToFile("PROGRAM CMD TABLE FAIL" + "\n");

            mIsRetryFailed = true;
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
//                    _handler.obtainMessage(ProgressState.OTA_PROGRAM_FAIL).sendToTarget();

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
            if (cmdTableList.get(i).cmd[9] == b2 && cmdTableList.get(i).cmd[10] == b1 && cmdTableList.get(i).cmd[11] == b0) {
                responseIdx = i;
                break;
            }
        }

        byte[] cmd = cmdTableList.get(responseIdx).cmd;


        Log.d(TAG, " status: " + status);
        Log.d(TAG, " addr: " + b2 + " " + b1 + " " + b0);

        if (status == (byte) 0x00 &&
                b2 == cmd[9] &&
                b1 == cmd[10] &&
                b0 == cmd[11]) {
            isCmdPass = true;
            cmdTableList.get(responseIdx).isPass = true;
        } else isCmdPass = false;

        Log.d("cmd pass: ", "" + isCmdPass);
        AirohaOtaLog.LogToFile("PROGRAM RESULT: " + isCmdPass + "\n");
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isExternalProgramCmd(packet))
            return;

        ParsePacket(packet);

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");
        AirohaOtaLog.LogToFile("PROGRAM RECEIVE: " + log + "\n");

        if (IsContinue()) {
            if (isCmdPass) {
                isCmdPass = false;
                cmdCount++;
                if (cmdCount == percent) {
                    cmdCount = 0;
//                    _handler.obtainMessage(ProgressState.OTA_PROGRESS_COUNT).sendToTarget();
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
//            _handler.obtainMessage(ProgressState.OTA_PROGRAM_COMPLETE).sendToTarget();

            mIsCompleted = true;
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
        return null;
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