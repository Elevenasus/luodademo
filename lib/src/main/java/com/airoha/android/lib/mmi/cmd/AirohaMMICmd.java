package com.airoha.android.lib.mmi.cmd;

/**
 * Created by Daniel.Lee on 2016/3/22.
 */
public final class AirohaMMICmd {

    private static byte mSectorIdx = (byte) 0x00;

    public static byte getSectorBasePlusOffset() {
        return mSectorBasePlusOffset; // OCF
    }

    private static byte mSectorBasePlusOffset;

    public static byte getSectorGroup() {
        return mSectorGroup; // OGF
    }

    private static byte mSectorGroup;

    private static byte mSectorHeadOffsetL;
    private static byte mSectorHeadOffsetH;
    private static byte mSectorDataLenL;
    private static byte mSectorDataLenH;


    public static byte[] generateCmd(byte ocfAsOpcode, byte ogfRelayFollower) {
        switch (ocfAsOpcode) {
            case OCF.GET_BATTERY:
            case OCF.GET_FW_VER:
            case OCF.GET_CHANNEL_INFO:

                return new byte[]{UartCmdHeader.H0, UartCmdHeader.H1, UartCmdHeader.H2,
                        (byte) 0x02, ocfAsOpcode, ogfRelayFollower};

            default:
                return null;
        }
    }


    /**
     * Get  FW version
     * payload:
     */
    public static final byte[] GET_FW_VERSION = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_FW_VER, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_TWS_SLAVE_VERSION = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_TWS_SLAVE_FW_VER, OGF.AIROHA_MMI_CMD};
    /**
     * Get Battery level
     * payload:
     * BatteryStatus -  0% - 100%, (0 – 100)
     */
    public static final byte[] GET_BATTERY = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_BATTERY, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_RIGHT_BATTERY = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_RIGHT_BATTERY, OGF.AIROHA_MMI_CMD};
    /**
     * Get device name
     * payload:
     * LL NN NN NN NN NN
     * LL: name length
     * NN: name
     */
    public static final byte[] GET_DEVICE_NAME = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_DEVICE_NAME, OGF.AIROHA_MMI_CMD};
    /**
     * Write device name
     */

    public static final byte[] WRITE_DEVICE_NAME = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.WRITE_DEVICE_NAME, OGF.AIROHA_MMI_CMD};
    /**
     * Get voice prompt info
     * XX XX XX XX XX XX XX
     * payload:
     * U8 isVPEnabled – is voice prompt on
     * U8 VPLangIndex– currentVPLangIndex
     * U8 VPLangCount – supported language count   supported language count, 0 for not supporting VP
     * U8 VPLangName[6] - Language enum for each voice prompt
     */
    public static final byte[] GET_VOICE_PROMPT = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_VOICE_PROMPT, OGF.AIROHA_MMI_CMD};
    /**
     * Turn VP on
     */
    public static final byte[] VOICE_PROMPT_ENABLE = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.VOICE_PROMPT_ENABLE, OGF.AIROHA_MMI_CMD};
    /**
     * Turn VP off
     */
    public static final byte[] VOICE_PROMPT_DISABLE = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.VOICE_PROMPT_DISABLE, OGF.AIROHA_MMI_CMD};
    /**
     * Change vp to next language
     */
    public static final byte[] VOICE_PROMPT_LANG_CHANGE_NEXT = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.VOICE_PROMPT_LANG_CHANGE_NEXT, OGF.AIROHA_MMI_CMD};
    /**
     * Sent from app to set specific language
     */
    public static final byte[] SET_VP_LANG = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_VP_LANG, OGF.AIROHA_MMI_CMD};
    /**
     * Get PEQ info
     * payload:
     U8 PEQInA2DP – current using PEQ mode in A2DP (0:off)
     U8 PEQInAUX – current using PEQ mode in AUX (0:off)
     U8 PEQNumInA2DP; - total PEQ mode in A2DP (if 0,do not support A2DP PEQ switch)
     U8 PEQNumInAUX; - total PEQ mode in AUX(if 0, do not support AUX PEQ switch)
     U8 isUseDefaultPEQ; - using default peq (if it has value, PEQ switch need skip mode 0:off)
     U8 A2DPSectorMode; - using default peq or customer (0:default, 1:customer)
     U8 AUXSectorMode; - using default peq or customer (0:default, 1:customer) //not support customer in current FW
     */
    public static final byte[] GET_PEQ = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_PEQ, OGF.AIROHA_MMI_CMD};
    /**
     * Set PEQ mode to next
     */

    public static final byte[] KEY_PEQ_MODE_CHANGE = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.KEY_PEQ_MODE_CHANGE, OGF.AIROHA_MMI_CMD};
    /**
     * change to specific PEQ index of A2DP
     */
    public static final byte[] SET_PEQ_A2DP = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_PEQ_A2DP, OGF.AIROHA_MMI_CMD};

    public static final byte[] SET_PEQ_A2DP_W_MODE = {UartCmdHeader.H0, UartCmdHeader.H1,
    UartCmdHeader.H2, (byte)0x04, OCF.SET_PEQ_A2DP, OGF.AIROHA_MMI_CMD};


    /**
     * change to specific PEQ index of AUX
     */
    public static final byte[] SET_PEQ_AUX = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_PEQ_AUX, OGF.AIROHA_MMI_CMD};
    /**
     * Play a specific voice prompt
     */
    public static final byte[] FIND_MY_ACCESSORY = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.FIND_MY_ACCESSORY_1520, OGF.AIROHA_MMI_CMD};
    /**
     * Get Volume
     */
    public static final byte[] GET_VOLUME = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_VOLUME, OGF.AIROHA_MMI_CMD};
    /**
     * Set Volume
     */
    public static final byte[] SET_VOLUME = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_VOL, OGF.AIROHA_MMI_CMD};
    // 2016.5.4 Daniel, new cmds
    public static final byte[] CALLER_NAME_ON = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_CALLER_NAME, OGF.AIROHA_MMI_CMD, (byte) 0x01};
    public static final byte[] CALLER_NAME_OFF = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_CALLER_NAME, OGF.AIROHA_MMI_CMD, (byte) 0x00};
    public static final byte[] CALLER_NAME_PACKET_COMPLETE = {0x02, 0x00, 0x00, 0x02, 0x00, 0x02, 0x07};
    public static final byte[] GET_CHG_BAT_STATUS = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_CHG_BAT_STATUS, OGF.AIROHA_MMI_CMD}; // 01 00 FC 02 B9 48
    public static final byte[] GET_CHG_BAT_STATUS_FOLLOWER = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_CHG_BAT_STATUS_FOLLOWER, OGF.AIROHA_MMI_CMD}; // 01 00 FC 02 BA 48
    public static final byte[] VOICE_COMMAND_ENABLE = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.VOICE_COMMAND_ENABLE, OGF.AIROHA_MMI_CMD};
    public static final byte[] VOICE_OMMAND_DISABLE = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.VOICE_COMMAND_DISABLE, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_VOICE_COMMAND_STATUS = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_VOICE_CMD_STATUS, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_CALLER_NAME_STATUS = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_CALLER_NAME_STATUS, OGF.AIROHA_MMI_CMD};


    //
    public static byte[] GET_SECTOR_INFO(byte sectorIdx, byte offset, byte dataLengthFromOffset) {
        // record what the caller request
        mSectorIdx = sectorIdx;
        return new byte[]{UartCmdHeader.H0, UartCmdHeader.H1, UartCmdHeader.H2, (byte) 0x04, sectorIdx, OGF.getParsingSectorGroup(), offset, dataLengthFromOffset};
    }

    public static byte getRequstedSectorIdx() {
        return mSectorIdx;
    }

    public static byte[] GET_SECTOR_INFO_V2(byte sectorBasePlusOffset, byte sectorGroup, byte headOffsetL, byte headOffsetH, byte dataLenL, byte dataLenH) {

        AirohaMMICmd.mSectorBasePlusOffset = sectorBasePlusOffset;
        AirohaMMICmd.mSectorGroup = sectorGroup;
        AirohaMMICmd.mSectorHeadOffsetL = headOffsetL;
        AirohaMMICmd.mSectorHeadOffsetH = headOffsetH;
        AirohaMMICmd.mSectorDataLenL = dataLenL;
        AirohaMMICmd.mSectorDataLenH = dataLenH;

        return new byte[] {UartCmdHeader.H0, UartCmdHeader.H1, UartCmdHeader.H2, (byte)0x06,
                AirohaMMICmd.mSectorBasePlusOffset, // OCF
                AirohaMMICmd.mSectorGroup, // OGF
                AirohaMMICmd.mSectorHeadOffsetL, AirohaMMICmd.mSectorHeadOffsetH,
                AirohaMMICmd.mSectorDataLenL, AirohaMMICmd.mSectorDataLenH};
    }

    public static final byte[] SUSPEND_DSP = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.SUSPEND_DSP, OGF.AIROHA_MMI_CMD};

    public static final byte[] SUSPEND_DSP_FR = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.SUSPEND_DSP, OGF.AIROHA_MMI_CMD_FR};

    public static final byte[] RESUME_DSP = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.RESUME_DSP, OGF.AIROHA_MMI_CMD};

    public static final byte[] RESUME_DSP_FR = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.RESUME_DSP, OGF.AIROHA_MMI_CMD_FR};

    public static final byte[] GET_USER_DEFINED_PEQ_HPF_SECTOR_IDX = {
            UartCmdHeader.H0, UartCmdHeader.H1, UartCmdHeader.H2, 0x02,
            OCF.GET_PEQ_HPF_USER_SECTOR, OGF.AIROHA_MMI_CMD
    };

    // Audio Transparency
    public static final byte[] TRIGGER_KEY_EVENT = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x04, OCF.PASS_THROUGH_CMD, OGF.AIROHA_MMI_CMD};
    public static final byte[] SET_MASTER_AT_GAIN = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_MASTER_AT_GAIN, OGF.AIROHA_MMI_CMD};
    public static final byte[] SET_SLAVE_AT_GAIN = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x03, OCF.SET_SLAVE_AT_GAIN, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_MASTER_AT_GAIN = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_MASTER_AT_GAIN, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_SLAVE_AT_GAIN = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_SLAVE_AT_GAIN, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_MASTER_AT_STATUS = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_MASTER_AT_STATUS, OGF.AIROHA_MMI_CMD};
    public static final byte[] GET_SLAVE_AT_STATUS = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x02, OCF.GET_SLAVE_AT_STATUS, OGF.AIROHA_MMI_CMD};

    public static final byte[] ROLE_SWITCH = {UartCmdHeader.H0, UartCmdHeader.H1,
            UartCmdHeader.H2, (byte) 0x04, OCF.PASS_THROUGH_CMD, OGF.AIROHA_MMI_CMD, (byte) 0x01, (byte) 0x0F};

    public static byte[] combineComplexCmd(final byte[] bytesPreCmd, final byte[] bytesPostCmd) {
        byte[] combinedCmd = new byte[bytesPreCmd.length + bytesPostCmd.length];

        System.arraycopy(bytesPreCmd, 0, combinedCmd, 0, bytesPreCmd.length);
        System.arraycopy(bytesPostCmd, 0, combinedCmd, bytesPreCmd.length, bytesPostCmd.length);
        return combinedCmd;
    }


}
