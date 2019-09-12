package com.airoha.android.lib.ota.flash;

/**
 * Created by Daniel.Lee on 2017/9/5.
 *
 * This is for decoding the last appending data returned from FW in ParsePacket of XXX_PROGRAM
 *
 */

public class ProgramErrorCode {
//    #define FLASH_SUCCESS                                            0
//            #define FLASH_WRITE_SOURCE_CRC_FAIL               2
//            #define FLASH_VERIFY_CRC_FAIL                              3
//            #define FLASH_VERIFY_DATA_FAIL                            4
//            #define FLASH_UNSUPPORTED_FUNCTION               5
//            #define FLASH_ADDR_NOT_AT_BOUNDARY             6
//            #define FLASH_DATA_ADDR_NOT_4BYTE_ALIGN     7
//            #define FLASH_INVALID_LENGTH                               8
//            #define FLASH_UNSUPPORTED_FLASH_IC                  9
//            #define FLASH_UNSUPPORTED_FLASH_SIZE              10
    public static final int FLASH_SUCCESS = 0;
    public static final int FLASH_WRITE_SOURCE_CRC_FAIL = 2;
    public static final int FLASH_VERIFY_CRC_FAIL = 3;
    public static final int FLASH_VERIFY_DATA_FAIL = 4;
    public static final int FLASH_UNSUPPORTED_FUNCTION = 5;
    public static final int FLASH_ADDR_NOT_AT_BOUNDARY = 6;
    public static final int FLASH_DATA_ADDR_NOT_4BYTE_ALIGN = 7;
    public static final int FLASH_INVALID_LENGTH = 8;
    public static final int FLASH_UNSUPPORTED_FLASH_IC = 9;
    public static final int FLASH_UNSUPPORTED_FLASH_SIZE = 10;
}
