package com.airoha.android.lib.ota.cmd;

/**
 * Created by MTK60279 on 2017/10/19.
 */

public class CmdItem {
    public byte[] cmd;
    public boolean isPass;
    public int retryCnt;
    public boolean isSend;

    public CmdItem(byte[] c, boolean pass, int retry, boolean send) {
        cmd = c;
        isPass = pass;
        retryCnt = retry;
        isSend = send;
    }
}
