package com.airoha.android.lib.util;

/**
 * Created by Daniel.Lee on 2016/6/28.
 */


public class Crc16 {
    /** CRC initialization value */
    public static short INIT_VALUE = (short) 0xffff;

    /**
     * Calculate next CRC value.
     * Based on algorithm from http://www.ccsinfo.com/forum/viewtopic.php?t=24977
     * @param crcValue current CRC value
     * @param data data value to add to CRC
     * @return next CRC value
     */
    public static short calculate(short crcValue, byte data) {
        short x = (short) (((crcValue >>> 8) ^ data) & 0xff);
        x ^= (x >>> 4);
        return (short) ((crcValue << 8) ^ (x << 12) ^ (x << 5) ^ x);
    }

    /**
     * Calculate CRC value of part of data from byte array.
     * @param data byte array
     * @param offset data offset to calculate CRC value
     * @param length data length to calculate CRC value
     * @return calculated CRC value
     */
    public static short calculate(byte[] data, int offset, int length) {
        short crcValue = INIT_VALUE;
        int counter = length;
        int index = offset;
        while (counter-- > 0) {
            crcValue = calculate(crcValue, data[index++]);
        }
        return crcValue;
    }


    /**
     * Calculate CRC value for byte array.
     * @param data byte array to calculate CRC value
     * @return calculated CRC value
     */
    public static short calculate(byte[] data) {
        return calculate(data, 0, data.length);
    }

    public static byte[] int16ToBytes(short value){
        return new byte[] {
                (byte)(value >>> 8),
                (byte)value};
    }

    public static byte[] get2BytesCRC(byte bytes[]){
        return int16ToBytes(calculate(bytes));
    }

}
