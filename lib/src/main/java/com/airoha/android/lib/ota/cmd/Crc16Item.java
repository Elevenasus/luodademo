package com.airoha.android.lib.ota.cmd;

import com.airoha.android.lib.util.Crc16;

public class Crc16Item {
    private byte[] item;

    public Crc16Item(byte[] value){
        item = value;
    }

    public byte[] getRaw() {
        return item;
    }
}
