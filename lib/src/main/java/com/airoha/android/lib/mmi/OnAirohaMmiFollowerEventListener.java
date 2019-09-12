package com.airoha.android.lib.mmi;

import com.airoha.android.lib.transport.AirohaLink;

/**
 * Created by MTK60279 on 2017/11/27.
 */

public interface OnAirohaMmiFollowerEventListener {
    /**
     * Resp. for {@link AirohaLink#getBatteryFollower()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetBatteryResp(final byte resp);
    /**
     * Ind. for {@link AirohaLink#getBatteryFollower()}
     * @param batteryStatus, 0~100
     */
    void OnGetBatteryInd(final byte batteryStatus);


    /**
     * Resp. for {@link AirohaLink#getFwVersionFollower()}
     * @param resp, 0x00: Success, 0xFF: fail
     */
    void OnGetFwVersionResp(final byte resp);

    /**
     * Ind. for {@link AirohaLink#getFwVersionFollower()}
     * @param fwStr, FW version formatted
     */
    void OnGetFwVersionInd(final String fwStr);
}
