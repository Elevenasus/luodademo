package com.airoha.android.lib.physical;

/**
 * Created by MTK60279 on 2017/11/30.
 */

public interface IPhysical {
    boolean connect(String address);
    void disconnect();
    boolean write(final byte[] cmd);
    void notifyConnected();
    void notifyDisconnected();
    String typeName();
}
