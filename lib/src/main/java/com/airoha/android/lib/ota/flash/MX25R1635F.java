package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class MX25R1635F implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0xC2;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x28;
    }

    @Override
    public byte MemoryDesity() {
        return 0x15;
    }

    @Override
    public int Size() {
        return FlashSize.M16;
    }


}
