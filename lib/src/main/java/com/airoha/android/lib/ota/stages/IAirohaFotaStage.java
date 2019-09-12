package com.airoha.android.lib.ota.stages;

public interface IAirohaFotaStage {
    void start();
    void handleResp(byte[] packet);
    void notifyNext();
}
