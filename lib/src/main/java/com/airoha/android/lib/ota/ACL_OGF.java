package com.airoha.android.lib.ota;

import com.airoha.android.lib.physical.PhysicalType;
import com.airoha.android.lib.transport.TransportTarget;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class ACL_OGF {

    //public static final byte ACL_VEVT = (byte) 0x00;
    private static byte ACL_VCMD = (byte) 0x04; // Change ACL_OGF from 0x01 to 0x04 when using APP interface

    public static final byte SPP_MASTER = (byte) 0x04;
    public static final byte SPP_FOLLOWER = (byte) 0x05;

    public static byte getAclVcmd() {
        return ACL_VCMD;
    }

    public static void changeOGFforPhysical(PhysicalType type){
        if(type == PhysicalType.SPP) {
            ACL_VCMD = (byte) 0x04;
        }

        if(type == PhysicalType.BLE) {
            ACL_VCMD = (byte) 0x08;
        }
    }

    public static void changeOGF(TransportTarget target){
        if (target == TransportTarget.Master) {
            ACL_VCMD = SPP_MASTER;
        }

        if (target == TransportTarget.Follower) {
            ACL_VCMD = SPP_FOLLOWER;
        }
    }
}
