package com.airoha.android.lib.ota;

/**
 * Created by MTK60279 on 2018/3/12.
 */

public interface OnAirohaFw4KCrc16Listener {
    void On4KCrc16Reported(int address, byte[] crc16);
}
