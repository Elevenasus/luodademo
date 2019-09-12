package com.airoha.android.lib.util;

public class Crc16FwAlgo {
//      - (uint16_t) crc16_C: (UInt8*)pData length:(int) length {
//                        uint8_t i;
//                        uint16_t wCrc = 0xffff;
//                        while (length--) {
//                                wCrc ^= *(unsigned char *)pData++ << 8;
//                                for (i=0; i < 8; i++)
//                                        wCrc = wCrc & 0x8000 ? (wCrc << 1) ^ 0x1021 : wCrc << 1;
//                                }
//                        return wCrc & 0xffff;
//         }
    public static final int evalCRC16(byte[] data) {
        int crc = 0xFFFF;
        for (int i = 0; i < data.length; i++) {
            crc =  crc^((data[i]&0xFF) << 8);
            for (int j = 0; j < 8; ++j)
                if ((crc & 0x8000) != 0)
                    crc = (crc << 1) ^ 0x1021;
                else
                    crc <<= 1;
        }

        return (crc ) & 0xFFFF;
    }

    public static byte[] getCrc16ByteArrayInBigEndian(byte[] data) {
        int crcShort = evalCRC16(data);

        byte[] result = new byte[2];

        result[0] = (byte) ((crcShort>>8) & 0xFF);
        result[1] = (byte) (crcShort & 0xFF);

        return result;
    }

}
