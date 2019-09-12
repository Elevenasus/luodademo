package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class MX25L8035E implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0xC2;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x20;
    }

    @Override
    public byte MemoryDesity() {
        return (byte) 0x14;
    }

    @Override
    public int Size() {
        return FlashSize.M8;
    }
}
