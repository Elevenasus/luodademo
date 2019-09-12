package com.airoha.android.lib.transport.PacketParser;

import android.os.SystemClock;

import com.airoha.android.lib.mmi.OnAirohaFollowerExistingListener;
import com.airoha.android.lib.mmi.OnAlexaLicenseKeyEventListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.util.Converter;

import java.util.concurrent.ConcurrentHashMap;

public class AlexaPacketDispatcher {

    private static final String TAG = "AlexaPacketDispatcher";

    private ConcurrentHashMap<String, OnAlexaLicenseKeyEventListener> mAlexaKeyListenerMap;

    private AirohaLink mAirohaLink;

    public AlexaPacketDispatcher(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;

        mAlexaKeyListenerMap = new ConcurrentHashMap<>();
    }

    public void registerListener(String observer, OnAlexaLicenseKeyEventListener listener) {
        mAlexaKeyListenerMap.put(observer, listener);
    }

    public void parseSend(byte[] packet) {

        // 1 header + 8 key values
        if(packet.length == 9){
            byte[] key = new byte[8];

            System.arraycopy(packet, 1, key, 0, 8);

            mAirohaLink.logToFile(TAG, "OnLicenseKey: " + Converter.byte2HexStr(key));

            for(OnAlexaLicenseKeyEventListener listener : mAlexaKeyListenerMap.values()){
                if(listener != null){
                    listener.OnLicenseKey(key);
                }
            }
        }
    }
}
