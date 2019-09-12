package com.airoha.android.lib.ota.cmdRaw;

import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.util.Converter;

public class CmdInternalErase implements ICmdRaw {
    byte[] raw;


    public CmdInternalErase(int address, byte ocf){

        final byte[] addr = Converter.intToByteArray(address);

        raw = new byte[]{0x02, 0x00, 0x0F, 0x05, 0x00,
                ocf,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
        };
    }

    @Override
    public byte[] getRaw(){
        return raw;
    }
}
