package com.airoha.android.lib.transport.connection;

/**
 * There is no Android's origin event for SPP state changes.
 * This is for listening SPP Socket I/O state changes of {@link com.airoha.android.lib.transport.AirohaLink}
 * @author Daniel.Lee
 */

public interface OnAirohaConnStateListener {
    /**
     * SPP Connected
     */
    void OnConnected(String type);

    /**
     * SPP Disconnected
     */
    void OnDisconnected();
}
