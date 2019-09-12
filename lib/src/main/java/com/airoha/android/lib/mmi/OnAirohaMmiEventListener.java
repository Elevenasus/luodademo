package com.airoha.android.lib.mmi;

import com.airoha.android.lib.mmi.charging.ChargingStatus;
import com.airoha.android.lib.transport.AirohaLink;

/**
 * The callback of updating the resp./ind. of {@link AirohaLink} MMI actions
 * @author Daniel.Lee
 */

public interface OnAirohaMmiEventListener {
    /**
     * Resp. for {@link AirohaLink#getBattery()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetBatteryResp(final byte resp);
    /**
     * Ind. for {@link AirohaLink#getBattery()}
     * @param batteryStatus, 0~100
     */
    void OnGetBatteryInd(final byte batteryStatus);

    /**
     * Resp. for {@link AirohaLink#getBatteryFollower()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetBatteryRespFollower(final byte resp);
    /**
     * Ind. for {@link AirohaLink#getBatteryFollower()}
     * @param batteryStatus, 0~100
     */
    void OnGetBatteryIndFollower(final byte batteryStatus);



    /**
     * Resp. for {@link AirohaLink#getTwsSlaveBattery()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetTwsSlaveBatteryResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getTwsSlaveBattery()}
     * @param batteryStatus, 0~100
     */
    void OnGetTwsSlaveBatteryInd(final byte batteryStatus);

    /**
     * Resp. for {@link AirohaLink#getVolume()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetVolumeResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getVolume()}
     * @param volume, 0~127
     */
    void OnGetVolumeInd(final byte volume);

    /**
     * Resp. for  {@link AirohaLink#setVolume(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetVolumeResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#enableVoicePrompt()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnEnableVoicePromptResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#disableVoicePrompt()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnDisableVoicePromptResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#enableVoiceCommand()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnEnableVoiceCommandResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#disableVoiceCommand()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnDisableVoiceCommandResp(final byte resp);


    /**
     * Resp. for {@link AirohaLink#playFindTone()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnPlayFindToneResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#checkVoicePrompt()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnCheckVoicePromptResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#checkVoicePrompt()}
     * @param isVPEnabled, 0x01: On, 0x00: Off
     * @param vpLangIndex, Index of current Voice Prompt language
     * @param vpLangTotal,  Total of Voice Prompt languages, Max will be 6
     * @param vpEnums, An array for the enum values of each language
     */
    void OnCheckVoicePromptInd(final byte isVPEnabled, final byte vpLangIndex, final byte vpLangTotal, final byte[] vpEnums);

    /**
     * Resp. for {@link AirohaLink#nextVoicePromptLang()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnNextVoicePromptLangResp(final byte resp);

    /**
     * FW reports the voice prompt status
     * @param vpStatus, 0x01: On, 0x00: Off
     * {@link AirohaLink#enableVoicePrompt()}
     * {@link AirohaLink#disableVoicePrompt()}
     */
    void OnReportVoicePromptStatus(final byte vpStatus);

    /**
     * FW reports the index for current voice prompt language changed
     * @param vpLangIdx
     */
    void OnReportVoicePromptLangChanged(final byte vpLangIdx);

    /**
     * Resp. for {@link AirohaLink#setVoicePromptLang(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetVoicePromptLangResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#checkEQ()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnCheckEqResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#checkEQ()}
     * @param a2dpEqIdx, Index of current A2DP EQ mode
     * @param auxEqIdx, Index of current AUX EQ mode
     * @param a2dpEqTotal, Total of A2DP EQ modes
     * @param auxEqTotal, Total of AUX EQ modes
     * @param isDefaultEq, Index of default EQ mode, 0: Off
     * @param a2dpSectorMode using default peq or customer (0:default, 1:customer)
     * @param auxSectorMode using default peq or customer (0:default, 1:customer)
     */
    void OnCheckEqInd(final byte a2dpEqIdx, final byte auxEqIdx,
                      final byte a2dpEqTotal, final byte auxEqTotal,
                      final byte isDefaultEq,
                      final byte a2dpSectorMode, final byte auxSectorMode);

    /**
     * Resp. for {@link AirohaLink#changeEQMode()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnChangeEqModeResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#setA2dpEq(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetA2dpEqResp(final byte resp);

    /**
     * FW reports the A2DP EQ mode changed
     * @param a2dpEqIdx, Index of A2DP EQ mode
     */
    void OnReportA2dpEqChanged(final byte a2dpEqIdx);

    /**
     * Resp. for {@link AirohaLink#setAuxEq(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetAuxEqResp(final byte resp);

    /**
     * FW reports the AUX EQ mode changed
     * @param auxEqIdx, Index of AUX EQ mode
     */
    void OnReportAuxEqChanged(final byte auxEqIdx);

    /**
     * Resp. for {@link AirohaLink#getFwVersion()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetFwVersionResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getFwVersion()}
     * @param fwStr, FW version formatted
     */
    void OnGetFwVersionInd(final String fwStr);

    /**
     * Resp. for {@link AirohaLink#getFwVersionFollower()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetFwVersionRespFollower(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getFwVersionFollower()}
     * @param fwStr, FW version formatted
     */
    void OnGetFwVersionIndFollower(final String fwStr);

    /**
     * Resp. for {@link AirohaLink#getTwsSlaveFwVersion()} ()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetTwsSlaveFwVersionResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getTwsSlaveFwVersion()}
     * @param fwStr, FW version formatted
     */
    void OnGetTwsSlaveFwVersionInd(final String fwStr);

    void OnGetChargeBatteryStatusResp(final byte resp);

    /**
     * FW reports charging status
     * @see ChargingStatus
     * @param status
     */
    void OnGetChargeBatteryStatusInd(final ChargingStatus status);

    void OnGetChargeBatteryStatusFollowerResp(final byte resp);
    void OnGetChargeBatteryStatusFollowerInd(final ChargingStatus status);

    /**
     * Ind. for {@link AirohaLink#getVoiceCommandStatus()}
     * @param voiceCmdStatus, 0x01: on, 0x00: off
     */
    void OnGetVoiceCommandStatusInd(final byte voiceCmdStatus);

    /**
     * Ind. for {@link AirohaLink#getCallerNameStatus()}
     * @param callerNameStatus, 0x01: on, 0x00: off
     */
    void OnGetCallerNameStatusInd(final byte callerNameStatus);

    /**
     * Resp. for {@link AirohaLink#setCallerNameOn()}
     * Resp. for {@link AirohaLink#setCallerNameOff()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetCallerNameResp(final byte resp);


    /**
     * FW reports MCU pass through data
     * @param data
     */
    void OnPassThroughDataInd(final byte[] data);

    /**
     * Resp. for {@link AirohaLink#sendPassThroughData(byte[])}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnPasThroughDataResp(final byte resp);

    void OnGetSectorInfoResp(final byte resp);

    void OnGetSectorInfoInd(final byte[] data);


    void OnGetSectorInfoRespV2(byte resp);

    void OnGetSectorInfoIndV2(final byte[] data);


    void OnReportPeqSectorModeChanged(final byte mode);

    /**
     * Resp. for {@link AirohaLink#audioTransparencyToggle()}
     * @param resp, 0x00: Disable, 0x01: Enable
     */
    void OnAudioTransparencyToggleResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#setMasterATGain(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetMasterATGainResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#setSlaveATGain(byte)}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnSetSlaveATGainResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#getMasterATGain()}
     * @param resp, 0x00~0x0F: gain value, 0xFF: fail
     */
    void OnGetMasterATGainResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#getSlaveATGain()}
     * @param resp, 0x00~0x0F: gain value, 0xFF: fail
     */
    void OnGetSlaveATGainResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#getMasterATStatus()}
     * @param resp, 0x00: enable, otherwise: disable
     */
    void OnGetMasterATStatusResp(final byte resp);

    /**
     * Resp. for {@link AirohaLink#getSlaveATStatus()}
     * @param resp, 0x00: enable, otherwise: disable
     */
    void OnGetSlaveATStatusResp(final byte resp);

    /**
     * Resp. for All DUT response
     */
    void OnHexResp(final byte[] resp);


    void OnGetChannelInfoResp(final byte resp);
    void OnGetChannelInfoInd(final byte bLeft_right);

    void OnGetChannelInfoRespFollower(final byte resp);
    void OnGetChannelInfoIndFollower(final byte bLeft_right);

    void OnRoleSwitchResp(final byte resp);

    void OnSetDeviceNameResp(final byte resp);
    void OnGetDeviceNameResp(final byte resp);
    void OnGetDeviceNameInd(final String deviceName);
}
