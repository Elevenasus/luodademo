package com.airoha.android.lib.mmi;

/**
 * Created by Daniel.Lee on 2016/11/10.
 */

public interface OnAirohaCallerNameEventListener {
    void OnReportEnterIncomingCall();
    void OnReportExitIncomingCall();

    void OnReportStopResp();
    void OnReportFailResp(byte packetIdx);
    void OnReportSuccessResp(byte packetIdx);
}
