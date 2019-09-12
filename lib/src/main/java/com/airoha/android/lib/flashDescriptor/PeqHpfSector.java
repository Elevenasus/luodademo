package com.airoha.android.lib.flashDescriptor;

import android.util.Log;

import com.airoha.android.lib.flashDescriptor.cmd.ACL_READ;
import com.airoha.android.lib.ota.OnAirohaAclEventListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;
import com.airoha.android.lib.util.logger.AirorhaEngineerDbgLog;

/**
 * Created by Daniel.Lee on 2017/9/13.
 */

public class PeqHpfSector {
    public interface OnPeqHpfParsingListener {
        void OnResult(DSP_PEQ_PARAMETER_STRU dspPeqParamStru);
    }

    private static final String TAG = "PeqHpfSector";

    //private int PEQ_ADDRESS = 0xf6000; // start 0xf6000
    private int mEndAddress = 0xf8000;// end 0xf8000
    private final int PAGE = 0x100; // read shift
    private final int SECTOR_LENGTH = 8192; // 0x2000

    private int currentAddr = 0xf6000;
    private int mStartAddress = 0xf6000;

    private OnPeqHpfParsingListener mResultListener;

    private ACL_READ _read = null;
    private AirohaLink mAirohaLink;

    private byte[] mSectorContent;
    private int mCopyIndex = 0;

    public void StartDescriptorParser(AirohaLink airohaLink){
        mAirohaLink = airohaLink;
        // read flash前256 bytes來parse descriptor
        _read = new ACL_READ(mAirohaLink, currentAddr);
        mAirohaLink.setAclEventListener(mRomDescriptorListener);
        _read.SendCmd();
    }

    public void setStartSectorDesc(int startAddress){
        mStartAddress = startAddress;
        currentAddr = startAddress;
        mEndAddress = startAddress + SECTOR_LENGTH;

        mSectorContent = new byte[SECTOR_LENGTH];

        mCopyIndex = 0; // reset
    }

    public int getSectorStartAddress(){
        return mStartAddress;
    }


    public void setListener(OnPeqHpfParsingListener listener){
        mResultListener = listener;
    }

    private final OnAirohaAclEventListener mRomDescriptorListener = new OnAirohaAclEventListener() {
        @Override
        public void OnHandleCurrentCmd(byte[] packet) {
            _read.handleResp(packet);

            if(_read.isCmdPass){
                //mBytes.add(_read.getData());
                copyPagePerShift(_read.getData());
            }

            currentAddr = currentAddr + PAGE;

            if(currentAddr < mEndAddress){
                _read.setReadAddr(currentAddr);
                _read.SendCmd();
            }else { // reached
                if(mResultListener!=null){
                    mResultListener.OnResult(new DSP_PEQ_PARAMETER_STRU(mSectorContent));
                }
            }
        }
    };

    private void copyPagePerShift(byte[] packetData){

        Log.d(TAG, "copying sector data:" + Converter.byte2HexStr(packetData));

        AirorhaEngineerDbgLog.logToFile(TAG+".log", "copying sector data: " + Converter.byte2HexStr(packetData));

        System.arraycopy(packetData, 0, mSectorContent, mCopyIndex, PAGE);
        mCopyIndex += PAGE;
    }

}
