package com.airoha.android.lib.transport.PacketParser;

import android.util.Log;

import com.airoha.android.lib.ota.OnAirohaAclEventListener;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2017/4/20.
 */

public class AclPacketDispatcher {

    private static final String TAG = "AclPacketDispatcher";

    private static OnAirohaAclEventListener mListener;

    public static void setListener(OnAirohaAclEventListener listener) {
        mListener = listener;
    }

    public static void parseSend(final byte[] packet){
        if(mListener == null){
            Log.d(TAG, "no acl event listener, exit parser");
            return;
        }

        String packetRawHex = Converter.byte2HexStr(packet, packet.length);
        Log.d(TAG,"raw data:" + packetRawHex);

        mListener.OnHandleCurrentCmd(packet);
    }
}
