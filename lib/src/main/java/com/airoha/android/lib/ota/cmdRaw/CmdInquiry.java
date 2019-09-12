package com.airoha.android.lib.ota.cmdRaw;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;

public class CmdInquiry implements ICmdRaw {
    byte[] raw;

    public CmdInquiry(){
        raw = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                ACL_OCF.ACL_VCMD_FLASH_INQUIRY_INTERNAL_EXTERNAL_UPDATE,
                ACL_OGF.getAclVcmd()};
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }
}
