package com.airoha.android.lib.ota.cmd;

/**
 * Created by Daniel.Lee on 2017/4/20.
 */

public interface IAclHandleResp {
    //byte[] getCommand();
    void SendCmd();
//    void ParsePacket(byte[] packet);
    void handleResp(final byte[] packet);
    IAclHandleResp getNextCmd();
    void setNextCmd1(IAclHandleResp cmd);
    void setNextCmd2(IAclHandleResp cmd);
    String getStatus();
    boolean isCompleted();
    boolean isRetryFailed();
}
