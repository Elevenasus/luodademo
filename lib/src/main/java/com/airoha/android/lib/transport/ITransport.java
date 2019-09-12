package com.airoha.android.lib.transport;

import android.content.Context;

/**
 * Created by MTK60279 on 2018/1/18.
 */

public interface ITransport {
    void OnPhysicalConnected(String type);
    void OnPhysicalDisconnected(String type);
    Context getContext();
    void handlePhysicalPacket(byte[] packet);
}
