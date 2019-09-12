package com.airoha.android.lib.ota.cmdRaw;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.cmdRaw.ICmdRaw;
import com.airoha.android.lib.util.Converter;

public class CmdInternalProgram implements ICmdRaw {

    private byte[] mRaw;

    public CmdInternalProgram(int address, byte[] payload){
        byte[] addr = Converter.intToByteArray(address);

        byte[] head = new byte[]{0x02, 0x00, 0x0F, 0x06, 0x01,
                ACL_OCF.ACL_VCMD_FLASH_PAGE_PROGRAM,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
        };

        mRaw = new byte[257 + 10];

        System.arraycopy(head, 0, mRaw, 0, head.length);

        // cmd = header(10) + crc(1) + data(256)
        System.arraycopy(payload, 0, mRaw, 10, payload.length);
    }


    @Override
    public byte[] getRaw() {
        return mRaw;
    }
}
