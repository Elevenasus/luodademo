package com.airoha.android.lib.ota.flash;

/**
 * Created by Evonne.Hsieh on 2016/11/16.
 */
public class GD25Q32C implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0xC8;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x40;
    }

    @Override
    public byte MemoryDesity() {
        return (byte) 0x16;
    }

    @Override
    public int Size(){
        return FlashSize.M32;
    }
}
