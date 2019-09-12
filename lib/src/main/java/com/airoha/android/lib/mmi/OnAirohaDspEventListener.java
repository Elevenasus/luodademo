package com.airoha.android.lib.mmi;

/**
 * Created by MTK60279 on 2017/11/27.
 */

public interface OnAirohaDspEventListener {
    void OnResumeResp(final byte resp, final byte ogf);
    void OnSuspendResp(final byte resp, final byte ogf);
}
