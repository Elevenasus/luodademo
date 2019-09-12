package com.airoha.android.lib.ota;

/**
 * This is the callback for reporting the status of {@link AirohaOtaFlowMgr}. In the implementation, you need to update the UI in the UI thread.
 * @author Evonne.Hsieh
 */
public interface OnAirohaOtaEventListener {
    /**
     * Update UI progressbar about the  progress
     * @param value, the progress
     */
    void OnUpdateProgressbar(int value);

    /**
     * Update the result of every OTA stage
     * @param isPass, return status
     * @param status, status that show on UI message
     */
    void OnOtaResult(boolean isPass, String status);

    /**
     * Popup a dialog to ask if User wants to apply updating the FW programmed
     * Suggest to implement a {@link android.app.AlertDialog} to ask User, then call {@link AirohaOtaFlowMgr#applyOTA()} or {@link AirohaOtaFlowMgr#cancelOTA()}
     */
    void OnOtaStartApplyUI();

//    void OnOtaApplyResult(boolean status);

//    void OnOtaOlderVersionAlert();
//
//    void OnOtaInvalidOEM();

    void OnShowCurrentStage(final String currentStage);

    void OnNotifyMessage(final String msg);
}
