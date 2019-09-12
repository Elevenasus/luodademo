package com.airoha.android.spp.headset.Intents;

/**
 * Created by Daniel.Lee on 2017/5/5.
 */

public class UiIntents {
    /**
     * Indication for AirohaLink.getBattery
     * contains the extra field {@link #EXTRA_BATTERY_LEVEL}.
     */
    public static final String ACTION_GET_BATTERY_IND = "ACTION_GET_BATTERY_IND";
    /**
     * Report BATTERY_STATUS from DUT
     * contains the extra field {@link #EXTRA_BATTERY_LEVEL}.
     */
    public static final String ACTION_REPORT_BATTERY_STATUS = "ACTION_REPORT_BATTERY_STATUS";

    public static final String ACTION_GET_BATTERY_FR_IND = "ACTION_GET_BATTERY_FR_IND";

    public static final String ACTION_REPORT_BATTERY_FR_STATUS = "ACTION_REPORT_BATTERY_FR_STATUS";

    /**
     * Extra for Battery level, use getByteExtra to retrieve: 0~100
     */
    public static final String EXTRA_BATTERY_LEVEL = "EXTRA_BATTERY_LEVEL";
    /**
     * Response for AirohaLink.getTwsSlaveBattery
     */
    public static final String ACTION_GET_TWS_SLAVE_BATTERY_RESP = "ACTION_GET_TWS_SLAVE_BATTERY_RESP";
    /**
     * Indication for AirohaLink.getTwsSlaveBattery
     * contains the extra field {@link #EXTRA_BATTERY_LEVEL}.
     */
    public static final String ACTION_GET_TWS_SLAVE_BATTERY_IND = "ACTION_GET_TWS_SLAVE_BATTERY_IND";
    /**
     * Response for AirohaLink.enableVoicePrompt
     */
    public static final String ACTION_VP_ENABLE_RESP = "ACTION_VP_ENABLE_RESP";
    /**
     * Response for AirohaLink.disableVoicePrompt
     */
    public static final String ACTION_VP_DISABLE_RESP = "ACTION_VP_DISABLE_RESP";
    /**
     * Response for AirohaLink.nextVoicePromptLang
     */
    public static final String ACTION_VP_NEXT_RESP = "ACTION_VP_NEXT_RESP";
    /**
     * Indication for AirohaLink.getVoicePrompt
     * contains the extra field
     * {@link #EXTRA_VP_ENABLED}.
     * {@link #EXTRA_VP_IDX}.
     *{@link #EXTRA_VP_COUNT}.
     * {@link #EXTRA_VP_NAME}.
     */
    public static final String ACTION_GET_VP_IND = "ACTION_GET_VP_IND";
    /**
     * Extra for VP Enabled,  use getByteExtra to retrieve. 00 for off, 01 for on
     */
    public static final String EXTRA_VP_ENABLED = "EXTRA_VP_ENABLED";
    /**
     * Extra for VP Idx,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_VP_IDX = "EXTRA_VP_IDX";
    /**
     * Extra for VP Count,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_VP_COUNT = "EXTRA_VP_COUNT";
    /**
     * Extra for VP Count,  use getStringExtra to retrieve.
     */
    public static final String EXTRA_VP_NAME = "EXTRA_VP_NAME";
    /**
     *  Report from VP_STATUS from DUT
     *  contains the extra field {@link #EXTRA_VP_STATUS}.
     */
    public static final String ACTION_REPORT_VP_STATUS = "ACTION_REPORT_VP_STATUS";
    /**
     *  Extra for Report from VP_STATUS from DUT, use getByteExtra to retrieve.
     */
    public static final String EXTRA_VP_STATUS = "EXTRA_VP_STATUS";
    /**
     *  Report from VP_LANG from DUT
     *  contains the extra field {@link #EXTRA_REPORT_VP_LANG}.
     */
    public static final String ACTION_REPORT_VP_LANG = "ACTION_REPORT_VP_LANG";
    /**
     *  Extra for Report from VP_LANG from DUT, use getByteExtra to retrieve.
     */
    public static final String EXTRA_REPORT_VP_LANG = "EXTRA_REPORT_VP_LANG";
    /**
     * Indication for AirohaLink.getVolume
     * contains the extra field {@link #EXTRA_VOLUME}.
     */
    public static final String ACTION_GET_VOLUME_IND = "ACTION_GET_VOLUME_IND";
    /**
     * Extra for AirohaLink.getVolume, use getByteExtra to retrieve. 0~127
     */
    public static final String EXTRA_VOLUME = "EXTRA_VOLUME";
    public static final String ACTION_GET_PEQ_IND = "ACTION_GET_PEQ_IND";
    /**
     * Extra for AirohaLink.checkEQ, use getByteExtra to retrieve
     */
    public static final String EXTRA_PEQ_AUX_IDX = "EXTRA_PEQ_AUX_IDX";
    /**
     * Extra for AirohaLink.checkEQ, use getByteExtra to retrieve
     */
    public static final String EXTRA_PEQ_A2DP_IDX = "EXTRA_PEQ_A2DP_IDX";
    /**
     * Extra for AirohaLink.checkEQ, use getByteExtra to retrieve
     */
    public static final String EXTRA_PEQ_A2DP_COUNT = "EXTRA_PEQ_A2DP_COUNT";
    /**
     * Extra for AirohaLink.checkEQ, use getByteExtra to retrieve
     */
    public static final String EXTRA_PEQ_AUX_COUNT = "EXTRA_PEQ_AUX_COUNT";
    /**
     * Response for AirohaLink.changeEQMode
     */
    public static final String ACTION_KEY_PEQ_MODE_CHANGE_RESP = "ACTION_KEY_PEQ_MODE_CHANGE_RESP";
    // PEQ A2DP change
    public static final String ACTION_REPORT_PEQ_A2DP_CHANGE = "ACTION_REPORT_PEQ_A2DP_CHANGE";
    // PEQ AUX change
    public static final String ACTION_REPORT_PEQ_AUX_CHANGE = "ACTION_REPORT_PEQ_AUX_CHANGE";
    // Device name
    public static final String ACTION_GET_DEVICENAME = "ACTION_GET_DEVICENAME";
    public static final String EXTRA_DEVICENAME = "EXTRA_DEVICENAME";
    // FW Ver.
    public static final String ACTION_GET_FW_VERSION = "ACTION_GET_FW_VERSION";
    public static final String ACTION_GET_FW_VERSION_FR = "ACTION_GET_FW_VERSION_FR";
    public static final String EXTRA_FW_STRING = "EXTRA_FW_STRING";

    /**
     * Response for AirohaLink.audioTransparencyToggle
     */
    public static final String ACTION_TOGGLE_AT_RESP = "ACTION_TOGGLE_AT_RESP";
    /**
     * Extra for AT status,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_AT_STATUS = "EXTRA_AT_STATUS";
    /**
     * Response for AirohaLink.setMasterATGain
     */
    public static final String ACTION_SET_MASTER_AT_GAIN_RESP = "ACTION_SET_MASTER_AT_GAIN_RESP";
    /**
     * Extra for setMasterATGain result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_SET_MASTER_AT_GAIN_RESULT = "EXTRA_SET_MASTER_AT_GAIN_RESULT";
    /**
     * Response for AirohaLink.setSlaveATGain
     */
    public static final String ACTION_SET_SLAVE_AT_GAIN_RESP = "ACTION_SET_SLAVE_AT_GAIN_RESP";
    /**
     * Extra for setSlaveATGain result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_SET_SLAVE_AT_GAIN_RESULT = "EXTRA_SET_SLAVE_AT_GAIN_RESULT";
    /**
     * Response for AirohaLink.getMasterATGain
     */
    public static final String ACTION_GET_MASTER_AT_GAIN_RESP = "ACTION_GET_MASTER_AT_GAIN_RESP";
    /**
     * Extra for getMasterATGain result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_MASTER_AT_GAIN = "EXTRA_MASTER_AT_GAIN";
    /**
     * Response for AirohaLink.getSlaveATGain
     */
    public static final String ACTION_GET_SLAVE_AT_GAIN_RESP = "ACTION_GET_SLAVE_AT_GAIN_RESP";
    /**
     * Extra for getSlaveATGain result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_SLAVE_AT_GAIN = "EXTRA_SLAVE_AT_GAIN";
    /**
     * Response for AirohaLink.getMasterATStatus
     */
    public static final String ACTION_GET_MASTER_AT_STATUS_RESP = "ACTION_GET_MASTER_AT_STATUS_RESP";
    /**
     * Extra for getMasterATStatus result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_MASTER_AT_STATUS = "EXTRA_MASTER_AT_STATUS";
    /**
     * Response for AirohaLink.getSlaveATStatus
     */
    public static final String ACTION_GET_SLAVE_AT_STATUS_RESP = "ACTION_GET_SLAVE_AT_STATUS_RESP";
    /**
     * Extra for getSlaveATStatus result,  use getByteExtra to retrieve.
     */
    public static final String EXTRA_SLAVE_AT_STATUS = "EXTRA_SLAVE_AT_STATUS";


    public static final String ACTION_GET_CHANNEL = "ACTION_GET_CHANNEL";

    public static final String ACTION_GET_CHANNEL_FR = "ACTION_GET_CHANNEL_FR";

    public static final String EXTRA_CHANNEL_LR = "EXTRA_CHANNEL_LR";

    public static final String ACTION_SET_CALLERNAME = "ACTION_SET_CALLERNAME";
}
