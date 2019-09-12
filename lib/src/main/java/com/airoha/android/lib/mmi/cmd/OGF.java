package com.airoha.android.lib.mmi.cmd;

/**
 * Created by Daniel.Lee on 2016/3/16.
 */
public class OGF {
    public static final byte AIROHA_MMI_CMD_FR = (byte) 0x44;
    public static final byte AIROHA_MMI_RESP_FR = (byte) 0x45;
    public static final byte AIROHA_MMI_IND_UNSOLICITED_FR = (byte) 0x46;
    public static final byte AIROHA_MMI_IND_SOLICITED_FR = (byte) 0x47;

    public static final byte AIROHA_MMI_CMD = (byte) 0x48;
    public static final byte AIROHA_MMI_RESP = (byte)0x49;
    public static final byte AIROHA_MMI_IND_UNSOLICITED = (byte)0x4A;
    public static final byte AIROHA_MMI_IND_SOLICITED = (byte) 0x4B;


    public static final byte PASS_THROUGH_CMD = (byte)0x52;
    public static final byte PASS_THROUGH_IND = (byte)0x53; // MCU pass something through

//    public static final byte AIROHA_MMI_CMD_FR_EX = (byte) 0x54;
//    private static final byte AIROHA_MMI_RESP_FR_EX = (byte) 0x55;
//    private static final byte AIROHA_MMI_IND_UNSOLICITED_FR_EX = (byte) 0x56;
//    private static final byte AIROHA_MMI_IND_SOLICITED_FR_EX = (byte) 0x57;


    public static byte getParsingSectorGroup() {
        return mParsingSectorGroup;
    }


    public static byte mParsingSectorGroup = (byte)0x62;

    public static boolean isIndUnsolictited(byte b) {
        return AIROHA_MMI_IND_UNSOLICITED == b ||
                PASS_THROUGH_IND ==b ||
                AIROHA_MMI_IND_UNSOLICITED_FR == b;
    }

    public static boolean isIndSolictited(byte b) {
        return AIROHA_MMI_IND_SOLICITED == b ||
                mParsingSectorGroup == b ||
                AirohaMMICmd.getSectorBasePlusOffset() == b ||
                AIROHA_MMI_IND_SOLICITED_FR == b;
    }

    public static boolean isResp(byte b){
        return AIROHA_MMI_RESP == b || (byte) 0x4F == b ||
                mParsingSectorGroup == b || AirohaMMICmd.getSectorGroup() == b ||
                PASS_THROUGH_IND == b ||
                AIROHA_MMI_RESP_FR == b;
    }

}
