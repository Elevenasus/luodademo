package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class MX25U8033E implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0xC2;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x25;
    }

    @Override
    public byte MemoryDesity() {
        return (byte) 0x34;
    }

    @Override
    public int Size() {
        return FlashSize.M8;
    }
}
