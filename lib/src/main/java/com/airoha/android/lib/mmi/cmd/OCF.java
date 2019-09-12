package com.airoha.android.lib.mmi.cmd;

/**
 * Created by Daniel.Lee on 2016/3/16.
 */
public class OCF {

    //0
    public static final byte PASS_THROUGH_CMD = (byte) 0x01;
    public static final byte REPORT_PASS_THROUGH = (byte) 0x03;
    //public static final byte SET_VOICE_ASSISTANT = (byte)0x04;

    //1
    public static final byte SUSPEND_DSP = (byte) 0x10;
    public static final byte RESUME_DSP = (byte) 0x11;

    public static final byte SET_FOTA_STATUS = (byte) 0x12;

    //2
    public static final byte VOICE_PROMPT_ENABLE = (byte) 0x21;
    public static final byte VOICE_PROMPT_DISABLE = (byte) 0x22;
    public static final byte VOICE_PROMPT_LANG_CHANGE_NEXT = (byte) 0x23;

    //3
    public static final byte SET_MASTER_AT_GAIN = (byte) 0x30;
    public static final byte SET_SLAVE_AT_GAIN = (byte) 0x31;
    public static final byte GET_MASTER_AT_GAIN = (byte) 0x32;
    public static final byte GET_SLAVE_AT_GAIN = (byte) 0x33;
    public static final byte GET_MASTER_AT_STATUS = (byte) 0x34;
    public static final byte GET_SLAVE_AT_STATUS = (byte) 0x35;
    public static final byte GET_4K_SECTOR_CRC16 = (byte) 0x36;

    //6
    public static final byte KEY_PEQ_MODE_CHANGE = (byte) 0x62;

    public static final byte GET_MUSIC_SAMPLE_RATE = (byte) 0x6B;
    public static final byte GET_REAL_TIME_UI_DATA = (byte) 0x6C;
    public static final byte SET_REAL_TIME_UI_DATA = (byte) 0x6D;
    //A
    public static final byte WRITE_DEVICE_NAME = (byte) 0xA2;
    public static final byte SET_VOL = (byte) 0xAB;

    //B
    public static final byte FIND_MY_ACCESSORY_1520 = (byte) 0xB7;
    public static final byte GET_RIGHT_BATTERY = (byte) 0xB8;
    public static final byte GET_CHG_BAT_STATUS = (byte) 0xB9;
    public static final byte GET_CHG_BAT_STATUS_FOLLOWER = (byte) 0xBA;
    public static final byte SEND_REAL_TIME_UPDATE_PEQ = (byte) 0xBB;
    public static final byte GET_PEQ_HPF_USER_SECTOR = (byte) 0xBC;
    public static final byte GET_CHANNEL_INFO = (byte) 0xBD;

    //C

    //D
    public static final byte GET_CALLER_NAME_STATUS = (byte) 0xD2;
    public static final byte VOICE_COMMAND_ENABLE = (byte) 0xD3;
    public static final byte VOICE_COMMAND_DISABLE = (byte) 0xD4;
    public static final byte GET_VOICE_CMD_STATUS = (byte) 0xD5;

    //E
    public static final byte REPORT_BAT_STATUS = (byte) 0xE0;
    public static final byte REPORT_VP_STATUS = (byte) 0xE1;
    public static final byte REPORT_VP_LANG = (byte) 0xE2;
    public static final byte REPORT_PEQ_A2DP_CHANGE = (byte) 0xE3;
    public static final byte REPORT_PEQ_AUX_CHANGE = (byte) 0xE4;
    public static final byte REPORT_VOL_CHANGE = (byte) 0xE5;
    // 2016.08.26 Daniel: Mantis#7752
    public static final byte REPORT_INCOMINGCALL = (byte) 0xE7;

    public static final byte REPORT_PEQ_SECTOR_MODE_CHANGE = (byte) 0xE9;
    public static final byte REPORT_MUSIC_SAMPLE_RATE = (byte) 0xEA;
    public static final byte REPORT_PEQ_REAL_TIME_UI_DATA = (byte) 0xEB;

    // 2018.05.08 Daniel: BTA-1325
    public static final byte REPORT_DRC_MODE = (byte) 0xEC;
    // 2018.06.14 Daniel: BTA-1362
    public static final byte REPORT_SLAVE_STATUS = (byte) 0xED;

    //F
    public static final byte GET_FW_VER = (byte) 0xF0;
    public static final byte GET_TWS_SLAVE_FW_VER = (byte) 0xF1;

    public static final byte GET_DEVICE_NAME = (byte) 0xF5;
    public static final byte SET_VP_LANG = (byte) 0xF6;
    public static final byte SET_PEQ_A2DP = (byte) 0xF7;
    public static final byte SET_PEQ_AUX = (byte) 0xF8;
    public static final byte GET_BATTERY = (byte) 0xF9;
    public static final byte GET_PEQ = (byte) 0xFA;
    public static final byte GET_VOICE_PROMPT = (byte) 0xFB;
    public static final byte GET_VOLUME = (byte) 0xFC;

    public static final byte SET_CALLER_NAME = (byte) 0xFE;
}
