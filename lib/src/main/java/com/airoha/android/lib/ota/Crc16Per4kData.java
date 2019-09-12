package com.airoha.android.lib.ota;

/**
 * Created by MTK60279 on 2018/3/12.
 */

public class Crc16Per4kData {
    private final byte[] raw = new byte[2];

    private int mAddress; // address in bin

    public Crc16Per4kData(byte[] crc16){
        raw[0] = crc16[0];
        raw[1] = crc16[1];
    }

    public void setAddress(int addr){
        mAddress = addr;
    }

    public int getAddress() {
        return mAddress;
    }

    public byte[] getRaw(){
        return raw;
    }

    public boolean isCrcEqual(Crc16Per4kData compare){
        byte[] compareData = compare.getRaw();

        for(int i = 0; i<raw.length; i++){
            if(raw[i] != compareData[i]){
                return false;
            }
        }

        return true;
    }

}
