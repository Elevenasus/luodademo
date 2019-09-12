package com.airoha.android.lib.transport.PacketParser;

import android.util.Log;

import com.airoha.android.lib.util.Converter;

/**
 * Created by MTK60279 on 2017/12/1.
 */

public class PacketHeaderChecker {

    private static final int MIN_SIZE = 6;
    private static final int HEADER_SIZE = 3;

    public static final byte HCI_EVENT_START = 0x04;
    public static final byte ACL_EVENT_START = 0x02;

    public static final byte ALEXA_EVENT_START = 0x0A;

    public static boolean isAirDumpCollected(byte[] packet){
        // ex: 04 FF 18 E8 4A length(1 byte) data(length bytes)
        if(!(packet[0] == HCI_EVENT_START))
            return false;

        return packet[3] == (byte) 0xE8 && packet[4] == (byte) 0x4A;
    }

    public static boolean isHciEventPacketCollected(byte[] packet){
        if(packet[0] != HCI_EVENT_START)
            return false;

        if (packet.length < MIN_SIZE)
            return false;

        // ex: 04 FF 03 F9 49 00
        //get the idx 2 for length
        int pLength = packet[2];

        // check equals MIN_SIZE + pLength
        if (packet.length == pLength + HEADER_SIZE) {
            Log.d("PacketCmr", "got full packet:" + Converter.byte2HexStr(packet));
            return true;
        } else
            return false;
    }

    public static boolean isCallerReportPacketCollected(byte[] packet){
        return packet[0] == ACL_EVENT_START && packet[6] == 0x07;
    }

    public static boolean isAclPacketCollected(byte[] packet){
        return packet[0] == ACL_EVENT_START;
    }

    public static boolean isAlexaEventCollected(byte[] packet) {
        return packet[0] == ALEXA_EVENT_START;
    }

}
