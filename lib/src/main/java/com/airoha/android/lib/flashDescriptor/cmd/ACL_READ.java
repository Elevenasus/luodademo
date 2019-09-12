package com.airoha.android.lib.flashDescriptor.cmd;

import android.util.Log;

import com.airoha.android.lib.ota.ACL_OCF;
import com.airoha.android.lib.ota.ACL_OGF;
import com.airoha.android.lib.ota.cmd.IAclHandleResp;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Evonne.Hsieh on 2016/6/15.
 */
public class ACL_READ implements IAclHandleResp {
    private final String TAG = "ACL_READ";
    private int readAddr = 0;

    public boolean isCmdPass = false;
    private boolean mIsCompleted = false;
    private byte[] _data = new byte[256];
    private AirohaLink mAirohaLink;

    public ACL_READ(AirohaLink airoLink, int addr)
    {
        mAirohaLink = airoLink;
        readAddr = addr;
    }

    public void setReadAddr(int addr){
        Log.d(TAG, "setReadAddr: " + addr);

        readAddr = addr;
    }

//    @Override
    private byte[] getCommand() {
        byte[] addr = Converter.intToByteArray(readAddr);
        byte[] len = Converter.ShortToBytes((short) 256);

        return new byte[]{0x02, 0x00, 0x0F, 0x07, 0x00,
                ACL_OCF.ACL_VCMD_FLASH_READ,
                ACL_OGF.getAclVcmd(),
                addr[2], addr[1], addr[0],
                len[1], len[0]
        };
    }

    @Override
    public void SendCmd() {
        byte[] cmd = getCommand();
        mAirohaLink.sendCommand(cmd);

        String log = Converter.byte2HexStr(cmd, cmd.length).concat(" ");
        Log.d(TAG + " cmd:", "" + log);
    }

    @Override
    public void handleResp(byte[] packet) {
        if (!isReadCmd(packet))
            return;

        ParsePacket(packet);

        if(isCmdPass)
        {
            System.arraycopy(packet, 11, _data, 0, 256);
            mIsCompleted = true;
        } else {
            mIsCompleted = false;
        }
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
        return mIsCompleted;
    }

    @Override
    public boolean isRetryFailed() {
        return false;
    }

    public void ParsePacket(byte[] packet) {
        // [02] [00] [0F] [06] [01] [09] [04] [data.....]
        // [   ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [b2] [b1] [b0]

        String log = Converter.byte2HexStr(packet, packet.length).concat(" ");

        byte status = packet[7];

        Log.d(TAG + " status:", "" + status);

        isCmdPass = status == (byte) 0x00;

        Log.d(TAG + "cmd pass: ", "" + isCmdPass);
    }

    public byte[] getData()
    {
        return _data;
    }

    private static boolean isReadCmd(byte[] packet) {
        // [02] [00] [0F] [06] [01] [09] [04] [data.....]
        // [   ] [    ] [    ] [    ] [    ] [OCF] [OGF] [status] [b2] [b1] [b0]
        return packet[5] == ACL_OCF.ACL_VCMD_FLASH_READ && packet[6] == ACL_OGF.getAclVcmd();
    }

}