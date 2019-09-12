package com.airoha.android.lib.ota.FwDesc;

import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2017/7/5.
 */

public class FwVersion {
    private int mMaj;
    private int mMin;
    private int mBuild;
    private int mRev;

    public int MajNum() {
        return mMaj;
    }

    public int MinNum() {
        return mMin;
    }

    public int BuildNum() {
        return mBuild;
    }

    public int RevNum() {
        return mRev;
    }

    public FwVersion(byte[] packet, boolean isBin){
        mMaj = (packet[0] & 0xFF);
        mMin = (packet[1] & 0xFF);

        // TODO Config. Tool and FW not consistent
        if(isBin){
            mBuild = Converter.BytesToU16(packet[3], packet[2]);
            mRev = Converter.BytesToU16(packet[5], packet[4]);
        }else {
            mBuild = Converter.BytesToU16(packet[2], packet[3]);
            mRev = Converter.BytesToU16(packet[4], packet[5]);
        }

    }

    public String toString(){
        return String.format("%d.%d.%d.%d", mMaj, mMin, mBuild, mRev);
    }

}
