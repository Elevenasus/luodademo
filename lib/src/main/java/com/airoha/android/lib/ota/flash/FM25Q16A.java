package com.airoha.android.lib.ota.flash;

//(Manufacture ID, Memory Type, Capacity)
//        FLASH_BH_BH25D16A: 0xA1 40 15
//        FLASH_FM_FM25Q16A:0x68 40 15

public class FM25Q16A implements IFlashInfo {
    @Override
    public byte MafID() {
        return (byte) 0xA1 ;
    }

    @Override
    public byte MemoryType() {
        return (byte) 0x40;
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
