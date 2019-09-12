package com.airoha.android.lib.ota.cmd;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.flash.FLASH_TYPE;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/8.
 */
public class ACL_7_CANCEL extends AclCmd implements IAclHandleResp {

    public ACL_7_CANCEL(AirohaOtaFlowMgr mgr) {
        super(mgr);
    }

//    @Override
    public byte[] getCommand() {
        byte[] cmd = null;
        if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.INTERNAL.ordinal()) {
            AirohaOtaLog.LogToFile("CANCEL SEND: ACL_VCMD_FLASH_DFU_UPDATE_CANCEL\n");
            cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                    ACL_OCF.ACL_VCMD_FLASH_DFU_UPDATE_CANCEL,
                    ACL_OGF.getAclVcmd(),
            };
        }
        if (mAirohaOtaFlowMgr.getFlashType() == FLASH_TYPE.EXTERNAL.ordinal()) {
            AirohaOtaLog.LogToFile("CANCEL SEND: ACL_VCMD_SPIFLASH_DFU_UPDATE_CANCEL\n");
            cmd = new byte[]{0x02, 0x00, 0x0F, 0x02, 0x00,
                    ACL_OCF.ACL_VCMD_SPIFLASH_DFU_UPDATE_CANCEL,
                    ACL_OGF.getAclVcmd(),
            };
        }
        return cmd;
    }

    @Override
    public void SendCmd() {
        byte[] cmd = getCommand();
        mAirohaLink.sendCommand(cmd);

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        AirohaOtaLog.LogToFile("CANCEL SEND: " + log + "\n");
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
