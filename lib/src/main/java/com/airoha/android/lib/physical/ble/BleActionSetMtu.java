package com.airoha.android.lib.physical.ble;

import android.bluetooth.BluetoothGatt;

/**
 * Created by MTK60279 on 2017/11/30.
 */
public class BleActionSetMtu extends BleAction {

    private static final int VERIFIED_MTU = 273;

    public BleActionSetMtu(BluetoothGatt bluetoothGatt) {
        super(bluetoothGatt);
        mBleAction = new IBleAction() {
            @Override
            public void exec() {
                mBluetoothGatt.requestMtu(VERIFIED_MTU);
            }
        };
    }

}
