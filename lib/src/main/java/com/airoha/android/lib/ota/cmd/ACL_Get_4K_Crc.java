package com.airoha.android.lib.ota.cmd;

import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.flash.FlashSize;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;

public class ACL_Get_4K_Crc extends AclCmd implements IAclHandleResp {
    private final String TAG = "ACL_Get_4K_Crc";

    private static final int INT_4K = 0x1000;

    private int mEraseStartAddr = 0;
    private int mEraseEndAddr = 0;

    private int mStartAddress = 0;
    private int mRetryCnt = 0;
    private int mPercent = 0;
    private int mCmdCount = 0;

    private boolean mIsCmdPass = false;
    private boolean mIsCompleted;
    private boolean mIsRetryFailed;

    private void configStartAddressByFlashSize() {
        if (mAirohaOtaFlowMgr.getFlashSize() == FlashSize.M16) {
            mEraseStartAddr = 0x100000;
            mEraseEndAddr = 0x1FFFFF;
        }
        if (mAirohaOtaFlowMgr.getFlashSize() == FlashSize.M32) {
            mEraseStartAddr = 0x200000;
            mEraseEndAddr = 0x3FFFFF;
        }

        AirohaOtaLog.LogToFile("GET_CRC16 START ADDR: " + mEraseStartAddr + "\n");
    }


    //    Get CRC 16 Cmd :
    //            [Send] 01 00 FC 0A 36 48 XX XX XX XX OO OO OO OO
    //    XX: Address
    //    OO: Length
    //
    //    [Resp] 04 ff 0c 36 4b XX XX XX XX OO OO OO OO CC CC
    //    CC: CRC16
    //
    //    Ex:
    //    Send : 01 00 FC 0A 36 48 00 10 00 00 00 00 10 00
    //    Resp: 04 ff 0c 36 4b 00 10 00 00 00 00 10 00 0f e1

//    private static boolean isGetCrc16Cmd(final byte[] packet) {
//        // [02] [00] [0F] [05] [00] [01] [00] [05] [04] [00]
//        // [   ] [    ] [    ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status]
//        return packet[7] == ACL_OCF.ACL_GET_CRC16 && packet[8] == ACL_OGF.getAclVcmd();
//    }


    public ACL_Get_4K_Crc(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

//    @Override
    private byte[] getCommand() {
        byte[] cmd = null;

        return cmd;
    }

    @Override
    public void SendCmd() {
        configStartAddressByFlashSize();

    }

    @Override
    public void handleResp(byte[] packet) {
//        if(!isGetCrc16Cmd(packet))
//            return;

        // parsing
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
        return false;
    }

    @Override
    public boolean isRetryFailed() {
        return false;
    }

//    public void getBinCrc16Per4kToList(){
//        mListBinCrc16.clear();
//
//        InputStream ios = null;
//        try {
//            byte[] buffer = new byte[4096];
//            try {
//                if (mIsUsingLocalFile)
//                    ios = new FileInputStream(getBinFileName());
//                else
//                    ios = new FileInputStream(mCtx.getFilesDir() + "/" + DEFAULT_BIN_FILE);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            while (ios.read(buffer) >0){
//                byte[] crc16 = Crc16.get2BytesCRC(buffer);
//
//                Crc16Per4kData crc16Buffer = new Crc16Per4kData(crc16);
//
//                mListBinCrc16.offer(crc16Buffer);
//
//                AirohaOtaLog.LogToFile("Bin CRC:" + crc16[0] + " " + crc16[1]);
//            }
//
//            ios.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
