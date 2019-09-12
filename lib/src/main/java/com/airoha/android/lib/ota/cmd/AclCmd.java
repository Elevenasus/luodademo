package com.airoha.android.lib.ota.cmd;

import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.transport.AirohaLink;

/**
 * Created by Daniel.Lee on 2017/8/8.
 */

public abstract class AclCmd implements IAclHandleResp{
    protected final AirohaOtaFlowMgr mAirohaOtaFlowMgr;
    protected final AirohaLink mAirohaLink;

    public AclCmd(AirohaOtaFlowMgr mgr){
        mAirohaOtaFlowMgr = mgr;
        mAirohaLink = mgr.getAirohaLink();
    }
}
