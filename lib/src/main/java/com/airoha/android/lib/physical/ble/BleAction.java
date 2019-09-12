package com.airoha.android.lib.physical.ble;

import android.bluetooth.BluetoothGatt;

/**
 * Created by MTK60279 on 2017/11/30.
 */
public class BleAction {
    protected BluetoothGatt mBluetoothGatt;
    protected IBleAction mBleAction;

    public BleAction(BluetoothGatt bluetoothGatt) {
        mBluetoothGatt = bluetoothGatt;
    }

    public void runOnThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }
}
