package com.airoha.android.lib.ota;

import java.util.LinkedList;

public class Bin4kInfoBuffer {
    private Crc16Per4kData mCrc16Per4KData;
//    private BinDataBufferPerPageShift[] mBinDataBufferPerPageShifts = new BinDataBufferPerPageShift[16]; // size should be 16

    private LinkedList<BinDataBufferPerPageShift> mListBinDataBufferPerPageShifts = new LinkedList<>();

    public Bin4kInfoBuffer(){

    }

    public void setCrc16Per4kData(Crc16Per4kData crc16Per4kData) {
        mCrc16Per4KData = crc16Per4kData;
    }

    public Crc16Per4kData getCrc16Per4KData() {
        return mCrc16Per4KData;
    }

    public void addToBinDataBufferPerPageShiftList(BinDataBufferPerPageShift item) {
        mListBinDataBufferPerPageShifts.add(item);
    }

    public LinkedList<BinDataBufferPerPageShift> getListBinDataBufferPerPageShifts() {
        return mListBinDataBufferPerPageShifts;
    }
}
