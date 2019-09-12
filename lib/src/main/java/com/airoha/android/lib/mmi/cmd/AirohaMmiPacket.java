package com.airoha.android.lib.mmi.cmd;

/**
 * Created by MTK60279 on 2018/3/22.
 */

public class AirohaMmiPacket {
    byte[] mRaw;

    public AirohaMmiPacket(byte ocf, byte ogf, byte[] payload){
        int payLoadLength = 0;

        if(payload!=null){
            payLoadLength = payload.length;
        }

        mRaw = new byte[4+2+payLoadLength];

        mRaw[0] = UartCmdHeader.H0;
        mRaw[1] = UartCmdHeader.H1;
        mRaw[2] = UartCmdHeader.H2;
        mRaw[3] = (byte)(2 + payLoadLength);
        mRaw[4] = ocf;
        mRaw[5] = ogf;

        if(payload != null){
            System.arraycopy(payload, 0, mRaw, 6, payload.length);
        }
    }

    public byte[] getRaw(){
        return mRaw;
    }
}
