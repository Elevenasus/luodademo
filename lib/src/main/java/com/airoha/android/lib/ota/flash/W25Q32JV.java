package com.airoha.android.lib.ota.flash;

/**
 * Created by MTK60279 on 2017/12/28.
 */

public class W25Q32JV implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte)0xEF;
    }

    @Override
    public byte MemoryType() {
        return (byte)0x40;
    }

    @Override
    public byte MemoryDesity() {
        return (byte)0x16;
    }

    @Override
    public int Size() {
        return FlashSize.M32;
    }
}
