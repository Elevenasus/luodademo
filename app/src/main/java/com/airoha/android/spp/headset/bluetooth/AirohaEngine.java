package com.airoha.android.spp.headset.bluetooth;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Daniel.Lee on 2016/6/7.
 */
public class AirohaEngine {
    private static String devicename;

    public static void setDeviceName(String name) {
        devicename = name;
    }

    public static String getDeviceName() {
        return devicename;
    }
}
