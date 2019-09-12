package com.airoha.android.lib.transport.Commander;

import android.util.Log;

import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel.Lee on 2016/3/28.
 */
public class RespIndPacketBuffer {
    private final List<Byte> packetList;

    public RespIndPacketBuffer() {
        packetList = new ArrayList<Byte>();
    }

    public void addArrayToPacket(byte[] b, int len){
        for(int i = 0; i < len; i++)
            packetList.add(b[i]);
    }

    public void resetPacket(){
        packetList.clear();
    }

    public byte[] getPacket(){
        byte[] byteArray = new byte[packetList.size()];
        for (int index = 0; index < packetList.size(); index++) {
            byteArray[index] = packetList.get(index);
        }
        return byteArray;
    }
}
