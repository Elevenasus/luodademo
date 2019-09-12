package com.airoha.android.lib.ota.cmdRaw;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;

public class CmdUnlock implements ICmdRaw {
    private byte[] raw;

    public CmdUnlock(byte flashType){
        if(flashType == FLASH_TYPE.INTERNAL.ordinal()){
            raw = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                    ACL_OCF.ACL_VCMD_FLASH_UNLOCK_ALL,
                    ACL_OGF.getAclVcmd(),
            };
        }

        if(flashType == FLASH_TYPE.EXTERNAL.ordinal()){
            raw = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                    ACL_OCF.HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL,
                    ACL_OGF.getAclVcmd(),
            };
        }
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }
}
