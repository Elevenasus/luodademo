package com.airoha.android.lib.ota;

import com.airoha.android.lib.ota.FwDesc.FwVersion;

/**
 * Created by Daniel.Lee on 2017/7/5.
 */

public interface OnAirohaFwVerSyncListener {
    void OnFwReported(FwVersion fwVersion);
}
