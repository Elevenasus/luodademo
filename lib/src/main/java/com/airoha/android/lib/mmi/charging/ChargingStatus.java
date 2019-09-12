package com.airoha.android.lib.mmi.charging;

import com.airoha.android.lib.transport.AirohaLink;

/**
 * @see AirohaLink#getChargeBatteryStatus()
 * @see com.airoha.android.lib.mmi.OnAirohaMmiEventListener#OnGetChargeBatteryStatusInd(ChargingStatus)
 */
public enum ChargingStatus {
    // TODO add javadoc for each state
    BAT_WELL,
    BAT_LOW,
    BAT_CHARGER_IN,
    BAT_CHARGER_RECHARGE,
    BAT_CHARGING_FULL,
    BAT_INVALID;

    public static ChargingStatus getValue(byte ind){

        switch (ind){
            case 0x00:
                return BAT_WELL;
            case 0x01:
                return BAT_LOW;
            case 0x02:
                return BAT_CHARGER_IN;
            case 0x03:
                return BAT_CHARGER_RECHARGE;
            case 0x04:
                return BAT_CHARGING_FULL;
        }

        return BAT_INVALID;
    }
}
