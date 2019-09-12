package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class MD25D80 implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0x51;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x40;
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
