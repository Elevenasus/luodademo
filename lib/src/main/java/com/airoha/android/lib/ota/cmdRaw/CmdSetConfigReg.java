package com.airoha.android.lib.ota.cmdRaw;


import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.cmdRaw.ICmdRaw;

public class CmdSetConfigReg implements ICmdRaw {

    private byte[] raw;

    public CmdSetConfigReg(){
        raw = new byte[]{0x02, 0x00, 0x0F, 0x03, 0x00,
                ACL_OCF.ACL_VCMD_FLASH_SET_CONFIGURATION_REGISTER,
                ACL_OGF.getAclVcmd(),
                (byte) 0xBF,
        };
    }


    @Override
    public byte[] getRaw() {
        return raw;
    }
}
