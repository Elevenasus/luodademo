package com.airoha.android.lib.ota;

import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.flashDescriptor.FlashDescriptor;
import com.airoha.android.lib.flashDescriptor.OnFlashDescriptorListener;
import com.airoha.android.lib.flashDescriptor.SectorIndex;
import com.airoha.android.lib.flashDescriptor.SectorTable;
import com.airoha.android.lib.mmi.cmd.AirohaMMICmd;
import com.airoha.android.lib.ota.cmd.ACL_0_INQUIRY;
import com.airoha.android.lib.ota.cmd.ACL_1_READ_BOOTCODE;
import com.airoha.android.lib.ota.cmd.ACL_2_1_SET_CONFIG_REG;
import com.airoha.android.lib.ota.cmd.ACL_2_EXTERNAL_INIT;
import com.airoha.android.lib.ota.cmd.ACL_2_INTERNAL_INIT;
import com.airoha.android.lib.ota.cmd.ACL_3_UNLOCK;
import com.airoha.android.lib.ota.cmd.ACL_4_EXTERNAL_ERASE;
import com.airoha.android.lib.ota.cmd.ACL_4_INTERNAL_ERASE;
import com.airoha.android.lib.ota.cmd.ACL_4_INTERNAL_ERASE_RESUME;
import com.airoha.android.lib.ota.cmd.ACL_4_INTERNAL_ERASE_TINY;
import com.airoha.android.lib.ota.cmd.ACL_5_EXTERNAL_PROGRAM;
import com.airoha.android.lib.ota.cmd.ACL_5_EXTERNAL_PROGRAM_DEMOSOUND;
import com.airoha.android.lib.ota.cmd.ACL_5_INTERNAL_PROGRAM;
import com.airoha.android.lib.ota.cmd.ACL_5_INTERNAL_PROGRAM_RESUME;
import com.airoha.android.lib.ota.cmd.ACL_5_INTERNAL_PROGRAM_TINY;
import com.airoha.android.lib.ota.cmd.ACL_5_INTERNAL_PROGRAM_TINY_PEQ;
import com.airoha.android.lib.ota.cmd.ACL_6_APPLY;
import com.airoha.android.lib.ota.cmd.ACL_7_CANCEL;
import com.airoha.android.lib.ota.cmd.IAclHandleResp;
import com.airoha.android.lib.ota.cmdRaw.CmdGet4kCrc;
import com.airoha.android.lib.ota.cmdRaw.CmdInternalErase;
import com.airoha.android.lib.ota.cmdRaw.CmdInternalProgram;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;
import com.airoha.android.lib.ota.flash.FlashSize;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.TransportTarget;
import com.airoha.android.lib.util.ContentChecker;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.Crc16FwAlgo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * <p>{@link AirohaOtaFlowMgr} is the center of handling the OTA follow.</>
 * <p>The step of OTA related operation roughly like below:</>
 * <p>1. Implement {@link OnAirohaOtaEventListener}</>
 * <p>2. Init {@link AirohaOtaFlowMgr#AirohaOtaFlowMgr(AirohaLink, OnAirohaOtaEventListener)}</>
 * <p>3. {@link AirohaOtaFlowMgr#setBinFileName(String)}</>
 * <p>4. {@link AirohaOtaFlowMgr#setBootcodeFileName(String)}</>
 * <p>5. {@link AirohaOtaFlowMgr#startOTA()} Before start OTA, you must have {@link AirohaLink} connected.</>
 * <p>6. {@link AirohaOtaFlowMgr#applyOTA()}</>
 *
 * @author Evonne.Hsieh
 * @see AirohaLink#connect(String)
 * @see OnAirohaOtaEventListener
 */
public class AirohaOtaFlowMgr {
    public static final String DEVICE_BOOT_CODE_NAME = "device.bootcode";
    private static final String TAG = "AirohaOtaFlowMgr";
    private boolean mIsUsingLocalFile = false;
    public static int otaPageNum = 1;

    public static final String DEFAULT_BOOTCODE_FILE = "bootcode.bootcode";
    public static final String DEFAULT_BIN_FILE = "updatte.bin";

    private String mBootCodeFileName = "bootcode.bootcode";
    private String mBinFileName = "update.bin";
    private static String mExtFile = "demo.ext";
    private final Context mCtx;

    private ACL_0_INQUIRY mCmdInquiry;
    private ACL_1_READ_BOOTCODE mCmdReadBootCode;
    private ACL_2_INTERNAL_INIT mCmdInternalInit;
    private ACL_2_EXTERNAL_INIT mCmdExternalInit;
    private ACL_2_1_SET_CONFIG_REG mCmdSetCfgReg;
    private ACL_3_UNLOCK mCmdUnlock;
    private ACL_4_INTERNAL_ERASE mCmdInternalErase;
    private ACL_4_INTERNAL_ERASE_RESUME mCmdInternalEraseResume;
    private ACL_4_INTERNAL_ERASE_TINY mCmdInternalEraseTiny;
    private ACL_4_EXTERNAL_ERASE mCmdExternalErase;
    private ACL_5_INTERNAL_PROGRAM mCmdInternalProgram;
    private ACL_5_INTERNAL_PROGRAM_RESUME mCmdInternalProgramResume;
    private ACL_5_INTERNAL_PROGRAM_TINY mCmdInternalProgramTiny;
    private ACL_5_INTERNAL_PROGRAM_TINY_PEQ mCmdInternalProgramTinyPeq;
    private ACL_5_EXTERNAL_PROGRAM mCmdExternalProgram;
    private ACL_5_EXTERNAL_PROGRAM_DEMOSOUND mCmdExternalProgramDemosound;
    private ACL_6_APPLY mCmdApply;
    private ACL_7_CANCEL mCmdCancel;

    private AirohaLink mAirohaLink;
    private boolean mIsCheckFwSupported = false;

    private IAclHandleResp mCurrentAclCmd;

    private boolean mIsUpdatingDspParam = false;

    private boolean DEBUG_LOG_PRINT_RAW = true;

    private LinkedList<Crc16Per4kData> mListBinCrc16Per4kData = new LinkedList<>();
    private LinkedList<Crc16Per4kData> mListFwCrc16Per4kData = new LinkedList<>();
    private LinkedList<CmdGet4kCrc> mListCmdGetFwCrc16 = new LinkedList<>();
    private LinkedList<BinDataBufferPerPageShift> mListBinDataBufferPerPageShift = new LinkedList<>();
    private LinkedList<Bin4kInfoBuffer> mListBin4kInfoBuffer = new LinkedList<>();
    private LinkedList<CmdInternalErase> mListToDoCmdInternalErase = new LinkedList<>();
    private LinkedList<CmdInternalProgram> mListTodoCmdInternalProgram = new LinkedList<>();


    private LinkedList<Bin4kInfoBuffer> mListToBeSendBin4kInfoBuffer = new LinkedList<>();

    private OnAirohaOtaEventListener mOtaListener = new OnAirohaOtaEventListener() {
        @Override
        public void OnUpdateProgressbar(int value) {
            Log.d(TAG, "OnUpdateProgressbar" + value);
        }

        @Override
        public void OnOtaResult(boolean isPass, String status) {

        }

        @Override
        public void OnOtaStartApplyUI() {

        }


        @Override
        public void OnShowCurrentStage(String currentStage) {

        }

        @Override
        public void OnNotifyMessage(String msg) {

        }
    };

    private final OnAirohaAclEventListener mAclListener = new OnAirohaAclEventListener() {

        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            mCurrentAclCmd.handleResp(packet);

            mOtaListener.OnNotifyMessage(Converter.byte2HexStr(packet));

            if (mCurrentAclCmd.isCompleted()) {
                mOtaListener.OnOtaResult(true, mCurrentAclCmd.getStatus());

                IAclHandleResp next = mCurrentAclCmd.getNextCmd();

                if (next != null) {
                    mCurrentAclCmd = next;
                    mCurrentAclCmd.SendCmd();
                    mOtaListener.OnShowCurrentStage(mCurrentAclCmd.getClass().getSimpleName());
                } else {
                    // The phase of  OTA_PROGRAM_COMPLETE

                    // 2018.06.27 new flow - BTA1693 - start
                    mAirohaLink.filterMmiSendForOta(false);

                    mAirohaLink.setFotaStatus((byte) 0x00);
                    // 2018.06.27 new flow - BTA1693 - end


                    mOtaListener.OnOtaStartApplyUI();
                }
            }

            if (mCurrentAclCmd.isRetryFailed()) {
                mOtaListener.OnOtaResult(false, mCurrentAclCmd.getStatus());
            }
        }
    };

    private final OnAirohaAclEventListener mAclListenerForResume = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            mCurrentAclCmd.handleResp(packet);

            if (mCurrentAclCmd.isCompleted()) {
                mOtaListener.OnOtaResult(true, mCurrentAclCmd.getStatus());

                if(mCurrentAclCmd instanceof ACL_2_INTERNAL_INIT){
                    // stat check CRCs

                    mOtaListener.OnShowCurrentStage("Checking CRCs");
                    constructAll4kInfoBuffers();

                    return;
                }

                IAclHandleResp next = mCurrentAclCmd.getNextCmd();

                if (next != null) {
                    mCurrentAclCmd = next;
                    mCurrentAclCmd.SendCmd();
                    mOtaListener.OnShowCurrentStage(mCurrentAclCmd.getClass().getSimpleName());
                } else {
                    // The phase of  OTA_PROGRAM_COMPLETE

                    // 2018.06.27 new flow - BTA1693 - start
                    mAirohaLink.filterMmiSendForOta(false);

                    mAirohaLink.setFotaStatus((byte) 0x00);
                    // 2018.06.27 new flow - BTA1693 - end

                    mOtaListener.OnOtaStartApplyUI();
                }
            }

            if (mCurrentAclCmd.isRetryFailed()) {
                mOtaListener.OnOtaResult(false, mCurrentAclCmd.getStatus());
            }
        }
    };

    private byte mFlashType;
    private int mFlashSize;

    /**
     * Constructor
     *
     * @param airohaLink, initialed AirohaLink
     * @param listener,   Caller need to implement the {@link OnAirohaOtaEventListener} interface for communicating with UI
     */
    public AirohaOtaFlowMgr(AirohaLink airohaLink, OnAirohaOtaEventListener listener) {
        this(airohaLink);

        // replace with the callback implementation
        mOtaListener = listener;
    }


    public AirohaOtaFlowMgr(AirohaLink airohaLink){
        // reset the file names
        mBootCodeFileName = "bootcode.bootcode";
        mBinFileName = "update.bin";

        mAirohaLink = airohaLink;
        mCtx = mAirohaLink.getContext();

        initCmds();
    }

    private void initCmds() {
        mCmdInquiry = new ACL_0_INQUIRY(this);
        mCmdReadBootCode = new ACL_1_READ_BOOTCODE(this);
        mCmdInternalInit = new ACL_2_INTERNAL_INIT(this);
        mCmdExternalInit = new ACL_2_EXTERNAL_INIT(this);
        mCmdSetCfgReg = new ACL_2_1_SET_CONFIG_REG(this);
        mCmdUnlock = new ACL_3_UNLOCK(this);
        mCmdInternalErase = new ACL_4_INTERNAL_ERASE(this);
        mCmdInternalEraseResume = new ACL_4_INTERNAL_ERASE_RESUME(this);
        mCmdInternalEraseTiny = new ACL_4_INTERNAL_ERASE_TINY(this);
        mCmdExternalErase = new ACL_4_EXTERNAL_ERASE(this);
        mCmdInternalProgram = new ACL_5_INTERNAL_PROGRAM(this);
        mCmdInternalProgramResume = new ACL_5_INTERNAL_PROGRAM_RESUME(this);
        mCmdInternalProgramTiny = new ACL_5_INTERNAL_PROGRAM_TINY(this);
        mCmdInternalProgramTinyPeq = new ACL_5_INTERNAL_PROGRAM_TINY_PEQ(this);
        mCmdExternalProgram = new ACL_5_EXTERNAL_PROGRAM(this);
        mCmdExternalProgramDemosound = new ACL_5_EXTERNAL_PROGRAM_DEMOSOUND(this);
        mCmdApply = new ACL_6_APPLY(this);
        mCmdCancel = new ACL_7_CANCEL(this);
    }

    public static String getExtFileName() {
        return mExtFile;
    }

    public void setExtFileName(String mExtFile) {
        AirohaOtaFlowMgr.mExtFile = mExtFile;
    }

    public String getBootcodeFileName() {
        return mBootCodeFileName;
    }

    /**
     * Before start OTA, must setup bootcode and bin file for OTA
     * Set update bootcode file name, the file should be downloaded to the {@link Context#getFilesDir()}
     *
     * @param filename
     */
    public void setBootcodeFileName(String filename) {
        mBootCodeFileName = filename;
    }

    public String getBinFileName() {
        return mBinFileName;
    }


    private int getBinFileLength() {
        File binFile = new File(mBinFileName);

        return (int) binFile.length();
    }

    /**
     * Before start OTA, must setup bootcode and bin file for OTA
     * Set update bin file name, the file should be downloaded to the {@link Context#getFilesDir()}
     *
     * @param filename
     */
    public void setBinFileName(String filename) {
        mBinFileName = filename;
    }

    public void updateProgress() {
        mOtaListener.OnUpdateProgressbar(1);
    }


    /**
     * Start OTA process.
     * You need to have {@link AirohaLink } connected before startOTA
     *
     * @see AirohaLink#connect(String)
     */
    public void startOTA() {
        // 2018.06.27 new flow - BTA1693 - start
        mAirohaLink.setFotaStatus((byte) 0x01);
        // 2018.06.27 new flow - BTA1693 - end

        configAirohaLink(mAclListener);

        mCmdInquiry.setNextCmd1(mCmdReadBootCode);

        mCmdReadBootCode.setNextCmd1(mCmdInternalInit);
        mCmdReadBootCode.setNextCmd2(mCmdExternalInit);

        mCmdInternalInit.setNextCmd1(mCmdSetCfgReg);

        mCmdExternalInit.setNextCmd1(mCmdSetCfgReg);

        mCmdSetCfgReg.setNextCmd1(mCmdUnlock);

        mCmdUnlock.setNextCmd1(mCmdInternalErase);
        mCmdUnlock.setNextCmd2(mCmdExternalErase);

        mCmdInternalErase.setNextCmd1(mCmdInternalProgram);

        mCmdExternalErase.setNextCmd1(mCmdExternalProgram);

        mIsCheckFwSupported = isCheckFwVersionSupported();

        mCmdInternalProgram.setCheckFwSupported(mIsCheckFwSupported);
        mCmdExternalProgram.setCheckFwSupported(mIsCheckFwSupported);

        mCurrentAclCmd = mCmdInquiry;

        mCurrentAclCmd.SendCmd();
    }

    public void startResumeOTA(){
//        configAirohaLink(mAclListenerForResume);

        // 2018.06.27 new flow - BTA1693 - start
        mAirohaLink.setFotaStatus((byte) 0x01);
        // 2018.06.27 new flow - BTA1693 - end

        // 2018.3.12 Daniel: new flow for resend
        mAirohaLink.setFw4KCrc16Listener(mFw4kCrc16Listener);

        mAirohaLink.setAclEventListener(mAclListenerForResume);

        initCmds();

        mCmdInquiry.setNextCmd1(mCmdReadBootCode);

        mCmdReadBootCode.setNextCmd1(mCmdInternalInit);

        mCurrentAclCmd = mCmdInquiry;
        mCurrentAclCmd.SendCmd();
    }

    /**
     * for internal only
     */
    public void startOTALite() {
        // read flash descriptor first
        FlashDescriptor.getInstance().setDescriptorListener(mDescriptorListener);
        FlashDescriptor.getInstance().StartDescriptorParser(mAirohaLink);
    }

    private void startUpdateDspParam(int structStartAddress){
        configAirohaLink(mAclListener);
        mAirohaLink.sendCommand(AirohaMMICmd.SUSPEND_DSP);

        mIsUpdatingDspParam = true;

        mCmdInquiry.setNextCmd1(mCmdInternalInit);
        // No need to check boot code
        mCmdInternalInit.setNextCmd1(mCmdSetCfgReg);
        mCmdSetCfgReg.setNextCmd1(mCmdUnlock);

        mCmdUnlock.setNextCmd1(mCmdInternalEraseTiny);

        mCmdInternalEraseTiny.setEraseStartAddr(structStartAddress);
        mCmdInternalEraseTiny.setEraseLength(getBinFileLength());
        mCmdInternalEraseTiny.setNextCmd1(mCmdInternalProgramTiny);

        mCmdInternalProgramTiny.setProgramStartAddr(structStartAddress);

        mCurrentAclCmd = mCmdInquiry;
        mCurrentAclCmd.SendCmd();
    }

    private final OnFlashDescriptorListener mDescriptorListener = new OnFlashDescriptorListener() {

        @Override
        public void OnResult() {
            FlashDescriptor.SectorTableHeader boundaryTable = FlashDescriptor.getInstance()._stHeaderList.get(SectorTable.Boundary.ordinal());
            int sectorAddress = boundaryTable._sectorInfoList.get(SectorIndex.Boundary.DSP_PEQ_PARAMETER_STRU.ordinal()).flashAddress;

            Log.d(TAG, "DSP_PEQ_PARAMETER_STRU address:" + sectorAddress);

            startUpdateDspParam(sectorAddress);
        }
    };


    private void configAirohaLink(OnAirohaAclEventListener listener) {
        // 2017.04.14 Daniel, stop MMI cmd sending
        mAirohaLink.filterMmiSendForOta(true);

        // 2017.04.17 Danie, clear MMI cmd queue
        mAirohaLink.clearMmiQueue();

        mAirohaLink.setAclEventListener(listener);

        initCmds();
    }

    public void startOTADemoSound() {
        configAirohaLink(mAclListener);

        mCmdInquiry.setNextCmd1(mCmdExternalInit);
        // No need to check boot code for demo sound
        // mCmdReadBootCode.setNextCmd2(mCmdExternalInit);
        mCmdExternalInit.setNextCmd1(mCmdSetCfgReg);
        mCmdSetCfgReg.setNextCmd1(mCmdUnlock);

        mCmdExternalErase.setForDemoSound();

        // 2018.01.05 Daniel: don't care the FLASH_TYPE from mCmdInquiry passed - start
        mCmdUnlock.setNextCmd1(mCmdExternalErase); // Daniel, trick: cheat here, always use external for demo sound
        // 2018.01.05 Daniel: don't care the FLASH_TYPE from mCmdInquiry passed - end
        mCmdUnlock.setNextCmd2(mCmdExternalErase);
        mCmdExternalErase.setNextCmd1(mCmdExternalProgramDemosound);

        mCurrentAclCmd = mCmdInquiry;
        mCurrentAclCmd.SendCmd();
    }



    /**
     * Parsing from BIN file to see if check FW version supported
     *
     * @return
     */
    private boolean isCheckFwVersionSupported() {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[257];
            try {
                if (mIsUsingLocalFile)
                    ios = new FileInputStream(getBinFileName());
                else
                    ios = new FileInputStream(mCtx.getFilesDir() + "/" + DEFAULT_BIN_FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int read = 0;
            read = ios.read(buffer);
            ous = new ByteArrayOutputStream();
            ous.write(buffer, 0, read);
            ios.close();
            ous.close();
            byte[] pattern = new byte[6];
            System.arraycopy(buffer, 0, pattern, 0, pattern.length);
            String airoha = new String(pattern, "UTF-8");
            if (airoha.equals("AIROHA")) {
                return true;
            }

        } catch (IOException e) {
            AirohaOtaLog.LogToFile("Read Fw Version Fail" + "\n");
            return false;
        }
        return false;
    }

    public void setLocalFlag(boolean isLocal) {
        mIsUsingLocalFile = isLocal;
    }

    public boolean isUsingLocalFile(){
        return mIsUsingLocalFile;
    }

    /**
     * After OTA process is finished, must do Apply to trigger DUT reset immediately.
     */
    public void applyOTA() {
        if(mIsUpdatingDspParam){
            mAirohaLink.sendCommand(AirohaMMICmd.RESUME_DSP);
            return;
        }

        mCmdApply.SendCmd();

        mAirohaLink.setFwVerSyncListener(null);
    }

    /**
     * Cancel OTA process
     */
    public void cancelOTA() {
        if(mIsUpdatingDspParam){
            mAirohaLink.sendCommand(AirohaMMICmd.RESUME_DSP);
            return;
        }

        mCmdCancel.SendCmd();

        // TODO 2017.04.14 Daniel, turn it on again. SPP still stays alive.
        mAirohaLink.filterMmiSendForOta(false);

        mAirohaLink.setFwVerSyncListener(null);
    }

    public Context getContext() {
        return mCtx;
    }

    public AirohaLink getAirohaLink() {
        return mAirohaLink;
    }

    public void setFlashType(byte flashType) {
        this.mFlashType = flashType;

    }

    public byte getFlashType() {
        return mFlashType;
    }

    public void setFlashSize(int flashSize) {
        this.mFlashSize = flashSize;

        if(mFlashType == FLASH_TYPE.INTERNAL.ordinal()){
            if (mFlashSize == FlashSize.M16) {
                mFotaStartAddress = 0x100000;
            }
            if (mFlashSize == FlashSize.M32) {
                mFotaStartAddress = 0x200000;
            }
        }

        if(mFlashType == FLASH_TYPE.EXTERNAL.ordinal()) {

        }
    }

    public int getFlashSize() {
        return mFlashSize;
    }

    private int mFotaStartAddress = 0x100000;

    private void getBinDataBufferToList(){
        mListBinDataBufferPerPageShift.clear();

        InputStream ios = null;
        try {
            byte[] buffer = new byte[257];
            try {
                if (mIsUsingLocalFile)
                    ios = new FileInputStream(getBinFileName());
                else
                    ios = new FileInputStream(mCtx.getFilesDir() + "/" + DEFAULT_BIN_FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

//            mAirohaLink.logToFile(TAG, "Bin length:" + getBinFileLength());

            // 2017.10.20, Daniel: Force to read 1st 257 bytes(some info hidden here)
            byte[] skippbuffer = new byte[257];
            ios.read(skippbuffer);

            mAirohaLink.logToFile(TAG, "skipp the 1st 257 bytes");

            int addrShiftPerPageWrite = 0;
            while (ios.read(buffer) >0){
                BinDataBufferPerPageShift binDataBufferPerPageShift = new BinDataBufferPerPageShift(buffer);
                binDataBufferPerPageShift.setAddress(addrShiftPerPageWrite+mFotaStartAddress);

                mListBinDataBufferPerPageShift.add(binDataBufferPerPageShift);

                if(DEBUG_LOG_PRINT_RAW) {
                    mAirohaLink.logToFile(TAG, String.format("Addr shift  256:  %d ( 0x%04X)", addrShiftPerPageWrite, addrShiftPerPageWrite));
                    mAirohaLink.logToFile(TAG, "Bin data(1+256): " + Converter.byte2HexStr(buffer));
                }

                addrShiftPerPageWrite = addrShiftPerPageWrite + 256;
            }

            ios.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getBinCrc16Per4kToList(){
        mListBinCrc16Per4kData.clear();

        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096 + 16];
            try {
                if (mIsUsingLocalFile)
                    ios = new FileInputStream(getBinFileName());
                else
                    ios = new FileInputStream(mCtx.getFilesDir() + "/" + DEFAULT_BIN_FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

//            mAirohaLink.logToFile(TAG, "Bin length:" + getBinFileLength());

            // 2017.10.20, Daniel: Force to read 1st 257 bytes(some info hidden here)
            byte[] skippbuffer = new byte[257];
            ios.read(skippbuffer);

            mAirohaLink.logToFile(TAG, "skipp the 1st 257 bytes");

            int addrInBin = 0;
            while (ios.read(buffer) >0){

                byte[] bufferExtracted = new byte[4096];

                Arrays.fill(bufferExtracted, (byte) 0xFF);

                for (int i = 0; i< 16; i++){
                    System.arraycopy(buffer, (1+257*i), bufferExtracted, i*256, 256);
                }

                int addrOffseted = addrInBin+mFotaStartAddress;

                byte[] crc16 = Crc16FwAlgo.getCrc16ByteArrayInBigEndian(bufferExtracted);
                Crc16Per4kData crc16Per4KData = new Crc16Per4kData(crc16);
                crc16Per4KData.setAddress(addrOffseted);
                mListBinCrc16Per4kData.offer(crc16Per4KData);

                mOtaListener.OnNotifyMessage(String.format("Bin Addr(offset %d): %d (0x%04X),  CRC:  %02X, %02X", mFotaStartAddress, addrOffseted, addrOffseted, crc16[0], crc16[1]));
                if(DEBUG_LOG_PRINT_RAW) {
                    mAirohaLink.logToFile(TAG, String.format("Bin Addr(offset %d): %d (0x%04X),  CRC:  %02X, %02X", mFotaStartAddress, addrOffseted, addrOffseted, crc16[0], crc16[1]));
                    mAirohaLink.logToFile(TAG, "extracted:" + Converter.byte2HexStr(bufferExtracted));
                }
                addrInBin = addrInBin + 4096;
            }

            ios.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void constructBin4kInfoBuffers(){
        mOtaListener.OnNotifyMessage("parsing Bin to buffer");
        getBinDataBufferToList();

        mOtaListener.OnNotifyMessage("calculating Bin Crc");
        getBinCrc16Per4kToList();

        mAirohaLink.logToFile(TAG, "mListBinCrc16Per4kData.size(): " + mListBinCrc16Per4kData.size()); // 256
        mAirohaLink.logToFile(TAG, "mListBinDataBufferPerPageShift.size(): " + mListBinDataBufferPerPageShift.size()); //  4082

        // 1 BinCrc16Per4KData mapping to 16 BinDataBufferPerPageShift

        int remainder = mListBinDataBufferPerPageShift.size() % 16; // 4082%16
        int mappingMatched = mListBinDataBufferPerPageShift.size() / 16; // 4083/16 = 255

        for (int i = 0; i < mappingMatched; i++) {
            Bin4kInfoBuffer bin4kInfoBuffer = new Bin4kInfoBuffer();
            bin4kInfoBuffer.setCrc16Per4kData(mListBinCrc16Per4kData.get(i));

            for (int j = 0; j < 16; j++) {

                bin4kInfoBuffer.addToBinDataBufferPerPageShiftList(mListBinDataBufferPerPageShift.get(i * 16 + j));
                Log.d(TAG, "idx: " + (i*16 + j));
            }

            mListBin4kInfoBuffer.add(bin4kInfoBuffer);
        }

        if(remainder!=0){
            Bin4kInfoBuffer bin4kInfoBuffer = new Bin4kInfoBuffer();
            int lastIdx = mListBinCrc16Per4kData.size() - 1;
            bin4kInfoBuffer.setCrc16Per4kData(mListBinCrc16Per4kData.get(lastIdx));

            for (int j = 0; j < remainder; j++) {
                bin4kInfoBuffer.addToBinDataBufferPerPageShiftList(mListBinDataBufferPerPageShift.get(lastIdx * 16 + j));
//                Log.d(TAG, "idx: " + (lastIdx*16 + j));
            }

            for (int j = remainder; j < 16; j++) {
                bin4kInfoBuffer.addToBinDataBufferPerPageShiftList(new BinDataBufferPerPageShift());
            }

            mListBin4kInfoBuffer.add(bin4kInfoBuffer);
        }
    }

    public void constructAll4kInfoBuffers(){
        mListBinCrc16Per4kData.clear();
        mListBinDataBufferPerPageShift.clear();

        constructBin4kInfoBuffers();
        fillListCmdGetFwCrc16();
    }

    private void fillListCmdGetFwCrc16(){
        int startAddress = mFotaStartAddress;
        int endAddress = startAddress * 2;

        int int4K = 0x1000;

        while (startAddress < endAddress){
            CmdGet4kCrc cmd = new CmdGet4kCrc(startAddress);
            mListCmdGetFwCrc16.add(cmd);

            startAddress = startAddress + int4K;
        }

        CmdGet4kCrc cmd = mListCmdGetFwCrc16.poll();
        if (cmd != null) {
            mAirohaLink.sendOrEnqueue(cmd.getRaw());
        }
    }

    private OnAirohaFw4KCrc16Listener mFw4kCrc16Listener = new OnAirohaFw4KCrc16Listener() {
        @Override
        public void On4KCrc16Reported(int address, byte[] fwCrc16) {

            mAirohaLink.logToFile(TAG, String.format("FW address:%d (0x%04X), CRC: %02X, %02X",address, address, fwCrc16[0], fwCrc16[1]));

            mOtaListener.OnNotifyMessage(String.format("FW address:%d (0x%04X), CRC: %02X, %02X",address, address, fwCrc16[0], fwCrc16[1]));

            Crc16Per4kData crc16Per4kData = new Crc16Per4kData(fwCrc16);
            crc16Per4kData.setAddress(address);

            mListFwCrc16Per4kData.add(crc16Per4kData);

            if(mListCmdGetFwCrc16.size()!=0){
                CmdGet4kCrc cmd = mListCmdGetFwCrc16.poll();
                if(cmd!=null){
                    mAirohaLink.sendOrEnqueue(cmd.getRaw());
                }
            }else {
                if(mListFwCrc16Per4kData.size() == mListBinCrc16Per4kData.size()){
                    // TODO to check
                    mAirohaLink.logToFile(TAG, "ready to check CRCs");

                    checkCrcLists();

                    checkToBeErasedList();

                    checkToBeProgrammedList();

                    buildResumeChain();
                }else {
                    // TODO debug:
                    mAirohaLink.logToFile(TAG, "mListFwCrc16Per4kData.size():" + mListFwCrc16Per4kData.size());
                    mAirohaLink.logToFile(TAG, "mListBinCrc16Per4kData.size(): " + mListBinCrc16Per4kData.size() );
                }
            }
        }
    };

    private void checkCrcLists(){
        mListToBeSendBin4kInfoBuffer.clear();

        for(int i = 0; i< mListBin4kInfoBuffer.size(); i++) {
            Bin4kInfoBuffer bin4kInfoBuffer = mListBin4kInfoBuffer.get(i);

            Crc16Per4kData binCrc16Per4kData = bin4kInfoBuffer.getCrc16Per4KData();
            Crc16Per4kData fwCrc16Per4kData = mListFwCrc16Per4kData.get(i);

            int address = binCrc16Per4kData.getAddress();
            if(!binCrc16Per4kData.isCrcEqual(fwCrc16Per4kData)){
                mListToBeSendBin4kInfoBuffer.add(bin4kInfoBuffer);

                mAirohaLink.logToFile(TAG, String.format("%d th bin4kInfoBuffer need to resend, address: %d (0x%04X)", i, address, address));
            }else{
                mAirohaLink.logToFile(TAG, String.format("address :%d (0x%04X) check pass", address, address));
            }
        }

        mAirohaLink.logToFile(TAG, String.format("total %d bin4kInfoBuffer need to resend", mListToBeSendBin4kInfoBuffer.size()));

    }

    private void checkToBeErasedList(){
        for (Bin4kInfoBuffer bin4kInfoBuffer: mListToBeSendBin4kInfoBuffer){
            int address = bin4kInfoBuffer.getCrc16Per4KData().getAddress();

            mAirohaLink.logToFile(TAG, String.format("address offseted: %d (0x%04X) need to be erased", address, address));

            // TODO to be improved, can check 64K align - start
            CmdInternalErase cmdInternalErase = new CmdInternalErase(address, ACL_OCF.ACL_VCMD_FLASH_SECTOR_ERASE_4K);

            mListToDoCmdInternalErase.add(cmdInternalErase);
            // TODO to be improved, can check 64K align - end;
        }
    }

    private void checkToBeProgrammedList(){
        for (Bin4kInfoBuffer bin4kInfoBuffer: mListToBeSendBin4kInfoBuffer){
            int address = bin4kInfoBuffer.getCrc16Per4KData().getAddress();

            mAirohaLink.logToFile(TAG, String.format("4K address  : %d (0x%04X) need to be programmed", address, address));

            LinkedList<BinDataBufferPerPageShift> binDataBufferPerPageShiftLinkedList = bin4kInfoBuffer.getListBinDataBufferPerPageShifts();

            for(BinDataBufferPerPageShift binDataBufferPerPageShift: binDataBufferPerPageShiftLinkedList){
                byte[] data = binDataBufferPerPageShift.getRaw();
                if(!ContentChecker.isAllDummyHexFF(data)){
                    int addressPage = binDataBufferPerPageShift.getAddress();

                    mOtaListener.OnNotifyMessage(String.format("Page address  : %d (0x%04X) need to be programmed", addressPage, addressPage));

                    mAirohaLink.logToFile(TAG, String.format("Page address : %d (0x%04X) need to be programmed", addressPage, addressPage));
                    mAirohaLink.logToFile(TAG, "To be programmed: " + Converter.byte2HexStr(data));

                    mListTodoCmdInternalProgram.add(new CmdInternalProgram(addressPage, data));
                }
            }
        }
    }

    private void buildResumeChain(){
        mCmdInternalEraseResume.setCmdList(mListToDoCmdInternalErase);
        mCmdInternalProgramResume.setCmdList(mListTodoCmdInternalProgram);

        mCmdSetCfgReg.setNextCmd1(mCmdUnlock);

        mCmdUnlock.setNextCmd1(mCmdInternalEraseResume);

        mCmdInternalEraseResume.setNextCmd1(mCmdInternalProgramResume);

        mCurrentAclCmd = mCmdSetCfgReg;

        mCurrentAclCmd.SendCmd();
    }

    public void switchUpdateTarget(TransportTarget target){
        ACL_OGF.changeOGF(target);
    }

    public void notifyMessageToUser(String msg){
        mOtaListener.OnNotifyMessage(msg);
    }
}
