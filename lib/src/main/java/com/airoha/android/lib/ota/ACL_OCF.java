package com.airoha.android.lib.ota;

/**
 * Created by Daniel.Lee on 2016/5/11.
 */
public class ACL_OCF {

    //  Inquiry Update Source
    public static final byte ACL_VCMD_FLASH_INQUIRY_INTERNAL_EXTERNAL_UPDATE = (byte)0x17;

    // Flash Init
    public static final byte ACL_VCMD_FLASH_READ_MANUFACTURER_AND_MEMORYTYPE = (byte)0x01;
    public static final byte ACL_VCMD_SPIFLASH_READ_MANUFACTURER_AND_MEMORYTYPE = (byte)0x19;
    public static final byte ACL_VCMD_FLASH_SET_CONFIGURATION_REGISTER = (byte)0x02;

    // Read Flash and check boot area
    public static final byte ACL_VCMD_FLASH_READ = (byte)0x09;

    // Flash Unlock
    // for internal only
    // public static final byte ACL_VCMD_FLASH_SET_PROTECT_BIT = (byte) 0x03;
    // for all type
    public static final byte ACL_VCMD_FLASH_LOCK_ALL = (byte) 0x11;
    public static final byte ACL_VCMD_FLASH_UNLOCK_ALL = (byte) 0x12;
    public static final byte HCI_ACL_OCF_SPIFLASH_PAGE_READ = (byte) 0x1C;
    public static final byte HCI_ACL_OCF_SPIFLASH_LOCK_ALL = (byte) 0x1D;
    public static final byte HCI_ACL_OCF_SPIFLASH_UNLOCK_ALL = (byte) 0x1E;

    // Sector Erase
    public static final byte ACL_VCMD_FLASH_SECTOR_ERASE_4K = (byte) 0x05;
    public static final byte ACL_VCMD_FLASH_SECTOR_ERASE_32K = (byte) 0x06;
    public static final byte ACL_VCMD_FLASH_SECTOR_ERASE_64K = (byte) 0x07;
    public static final byte ACL_VCMD_SPIFLASH_SECTOR_ERASE = (byte) 0x1A;
    public static final byte ACL_VCMD_POLLING_FOR_ERASE_DONE = (byte) 0x20;

    //    After issue ACL_VCMD_SPIFLASH_SECTOR_ERASE and get response,
    //    we need to issue ACL_VCMD_SPIFLASH_RD_WIP_STATUS and polling write_operation.
    //    The erase is done when write_operation is 0, otherwise 1 instead.
    //public static final byte ACL_VCMD_SPIFLASH_RD_WIP_STATUS = (byte) 0x20;

    // Page Program
    public static final byte ACL_VCMD_FLASH_PAGE_PROGRAM = (byte) 0x08;
    public static final byte ACL_VCMD_SPIFLASH_PAGE_PROGRM = (byte) 0x1B;
    public static final byte ACL_VCMD_FLASH_BYTE_PROGRM = (byte) 0x0A;


    // Soft Reset (Apply Update)
    public static final byte ACL_VCMD_FLASH_APPLY_DFU_UPDATE = (byte) 0x18;
    //    After SoftReset, wait HCI_VEVENT_BOOT_COMPLETED which means loader update complete

    // Cancel Update
    public static final byte ACL_VCMD_FLASH_DFU_UPDATE_CANCEL = (byte) 0x16;
    public static final byte ACL_VCMD_SPIFLASH_DFU_UPDATE_CANCEL = (byte) 0x1F;

    // Read VC address
    public static final byte ACL_VCMD_FLASH_READ_VC_ADDRESS = (byte) 0x22;

//    public static final byte ACL_GET_CRC16 = (byte) 0x36;
}
