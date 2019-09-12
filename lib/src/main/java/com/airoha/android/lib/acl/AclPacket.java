package com.airoha.android.lib.acl;

import android.support.annotation.Nullable;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;

/**
 * Created by MTK60279 on 2018/3/23.
 */

public class AclPacket {
    private byte[] mRaw;

    public AclPacket(byte ocf, @Nullable byte[] payload){

        int payloadLength = 0;
        if(payload != null){
            payloadLength = payload.length;
        }

        mRaw = new byte[5+ 2 + payloadLength];

        mRaw[0] = (byte)0x02;
        mRaw[1] = (byte)0x00;
        mRaw[2] = (byte)0x0F;

        int cmdLength = 2 + payloadLength;

        mRaw[3] = (byte) (cmdLength & 0xFF);
        mRaw[4] = (byte) ((cmdLength>>8) & 0xFF);

        mRaw[5] = ocf;

        mRaw[6] = ACL_OGF.getAclVcmd();

        if(payload != null){
            System.arraycopy(payload, 0, mRaw, 7, payload.length);
        }
    }

    public byte[] getRaw(){
        return mRaw;
    }
}
