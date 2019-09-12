package com.airoha.android.lib.ota.stages;

import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.transport.AirohaLink;

public class AirohaFotaStage implements IAirohaFotaStage {

    private AirohaLink mAirohaLink;
    private AirohaOtaFlowMgr mAirohaOtaFlowMgr;


    public AirohaFotaStage(AirohaOtaFlowMgr airohaOtaFlowMgr){
        mAirohaOtaFlowMgr = airohaOtaFlowMgr;
        mAirohaLink = airohaOtaFlowMgr.getAirohaLink();
    }

    @Override
    public void start() {

    }

    @Override
    public void handleResp(byte[] packet) {

    }

    @Override
    public void notifyNext() {

    }
}
