package com.airoha.android.lib.ota.cmd;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.cmdRaw.CmdApply;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/3.
 */
public class ACL_6_APPLY extends AclCmd implements IAclHandleResp {

    public ACL_6_APPLY(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

//    @Override
//    private byte[] getCommand() {
//        return new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
//                ACL_OCF.ACL_VCMD_FLASH_APPLY_DFU_UPDATE,
//                ACL_OGF.getAclVcmd(),
//        };
//    }

    @Override
    public void SendCmd() {
        byte[] cmd = (new CmdApply()).getRaw();
        mAirohaLink.sendCommand(cmd);

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("APPLY SEND: " + log + "\n");
    }


    @Override
    public void handleResp(byte[] packet) {

    }

    @Override
    public IAclHandleResp getNextCmd() {
        return null;
    }

    @Override
    public void setNextCmd1(IAclHandleResp cmd) {

    }

    @Override
    public void setNextCmd2(IAclHandleResp cmd) {

    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public boolean isRetryFailed() {
        return false;
    }
}
