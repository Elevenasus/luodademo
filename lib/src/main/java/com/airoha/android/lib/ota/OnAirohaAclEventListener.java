package com.airoha.android.lib.ota;

/**
 * Created by Daniel.Lee on 2017/4/20.
 */

public interface OnAirohaAclEventListener {
    void OnHandleCurrentCmd(final byte[] packet);
}
