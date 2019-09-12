package com.airoha.android.lib.ota.cmdRaw;

import com.airoha.android.lib.mmi.cmd.OCF;
import com.airoha.android.lib.mmi.cmd.OGF;
import com.airoha.android.lib.mmi.cmd.UartCmdHeader;
import com.airoha.android.lib.util.Converter;

/**
 * Created by MTK60279 on 2018/3/6.
 */

public class CmdGet4kCrc {
    byte[] raw;

    private final int INT_4K = 0x1000;

    private final byte[] LEN = {0x00, 0x10, 0x00, 0x00};

    public CmdGet4kCrc(int address){
        final byte[] addr = Converter.intToByteArray(address);

        raw = new byte[]{UartCmdHeader.H0, UartCmdHeader.H1, UartCmdHeader.H2, (byte)0x0A,
                OCF.GET_4K_SECTOR_CRC16, OGF.AIROHA_MMI_CMD,
                addr[3], addr[2], addr[1], addr[0],
                LEN[3], LEN[2], LEN[1], LEN[0]
        };
    }

    public byte[] getRaw(){
        return raw;
    }
}
