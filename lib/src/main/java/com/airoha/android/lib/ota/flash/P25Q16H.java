package com.airoha.android.lib.ota.flash;

public class P25Q16H implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0x85;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x60;
    }

    @Override
    public byte MemoryDesity() {
        return (byte) 0x15;
    }

    @Override
    public int Size() {
        return FlashSize.M16;
    }
}
