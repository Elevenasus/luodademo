package com.airoha.android.lib.ota;

import java.sql.Array;
import java.util.Arrays;

public class BinDataBufferPerPageShift {
    private byte[] raw = new byte[257]; // 1 crx + 256;

    private int mAddress; // address shift in 256

    private boolean mIsNeedToResume;

    public BinDataBufferPerPageShift(byte[] binCrcAndData){
        //raw = binCrcAndData;
        raw = new byte[257];

        System.arraycopy(binCrcAndData, 0, raw, 0, 257);
    }

    public BinDataBufferPerPageShift(){
        raw = new byte[257];
        Arrays.fill(raw, (byte)0xFF);
    }
//
//    public void setBinCrcAndData(byte[] crcAndData){
//        raw = crcAndData;
//    }

    public void setAddress(int addr){
        mAddress = addr;
    }

    public int getAddress() {
        return mAddress;
    }

    public void setNeedResume(boolean isNeedToResume){
        mIsNeedToResume = isNeedToResume;
    }

    public boolean isNeedToResume(){
        return  mIsNeedToResume;
    }

    public byte[] getRaw(){
        return raw;
    }
}
