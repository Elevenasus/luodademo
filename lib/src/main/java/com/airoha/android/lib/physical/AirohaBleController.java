package com.airoha.android.lib.physical;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.ITransport;
import com.airoha.android.lib.util.Converter;

import java.util.UUID;

/**
 * Created by MTK60279 on 2017/11/30.
 */

public class AirohaBleController implements IPhysical{
    private static final String TAG = "AirohaBleController";


    private final static UUID UUID_AIROHA_LE_MMI_SERVICE = UUID.fromString("5052494D-2DAB-0341-6972-6F6861424C45");
    private final static UUID UUID_AIROHA_LE_MMI_READ_CHARC = UUID.fromString("43484152-2DAB-3141-6972-6F6861424C45");
    private final static UUID UUID_AIROHA_LE_MMI_WRITE_CHARC = UUID.fromString("43484152-2DAB-3241-6972-6F6861424C45");

    private static final int VERIFIED_MTU = 273; // 2017.11.28 Daniel: tested with 1526 V27

    private BluetoothManager mBluetoothManager;
    /* Get Default Adapter */
    private BluetoothAdapter mBluetoothAdapter = null;

    private final Context mCtx;

    private ITransport mAirohaLink;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mGattNotifyCharc;
    private BluetoothGattCharacteristic mGattWriteCharc;


    public AirohaBleController(AirohaLink airohaLink){
        mAirohaLink = airohaLink;

        mCtx = mAirohaLink.getContext();

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mCtx.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }


    @Override
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connectSpp.");
            return false;
        }
        // We want to directly connectSpp to the device, so we are setting the autoConnect
        // parameter to false.

        if(mBluetoothGatt!=null){
            // return false, disconnect not called
            return false;
        }

        mBluetoothGatt = device.connectGatt(mCtx, false, mGattCallback);

        //refreshDeviceCache(mBluetoothGatt);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.d(TAG, "GATT Connected");

                mBluetoothGatt.requestMtu(VERIFIED_MTU);
            }

            if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG, "GATT Disconnected");

                if(mBluetoothGatt!=null) {
                    synchronized (mBluetoothGatt) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                }

                notifyDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattService service = mBluetoothGatt.getService(UUID_AIROHA_LE_MMI_SERVICE);

                if(service == null){
                    Log.d(TAG, "Can't find Airoha MMI Service");
                    // disconnect
                }else {

                    //mBluetoothGatt.requestMtu(VERIFIED_MTU);

                    mGattNotifyCharc = service.getCharacteristic(UUID_AIROHA_LE_MMI_READ_CHARC);

                    mGattWriteCharc = service.getCharacteristic(UUID_AIROHA_LE_MMI_WRITE_CHARC);

                    if(mGattNotifyCharc == null ||  mGattWriteCharc == null){
                        // disconnect
                        Log.d(TAG, "Can't find Airoha MMI Charcs");
                    }else {
                        Log.d(TAG, "Found Airoha MMI Charcs");
                        Log.d(TAG, "read charc. property: " + mGattNotifyCharc.getProperties());
                        Log.d(TAG, "write charc. property: " + mGattWriteCharc.getProperties());

                        mBluetoothGatt.setCharacteristicNotification(mGattNotifyCharc, true);

                        BluetoothGattDescriptor desc = mGattNotifyCharc.getDescriptors().get(0);
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        mBluetoothGatt.writeDescriptor(desc);

                        notifyConnected();
                    }
                }
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d(TAG, "onCharacteristicWrite, status: " + status + ", value: " + Converter.byte2HexStr(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            byte[] packet = characteristic.getValue();

            Log.d(TAG, "onCharacteristicChanged :  " + Converter.byte2HexStr(packet));

            mAirohaLink.handlePhysicalPacket(packet);

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.d(TAG, "onDescriptorRead :  " + Converter.byte2HexStr(descriptor.getValue()));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            Log.d(TAG, "onMtuChanged, mtu: " + mtu + ", status: " + status);

            // Attempts to discover services after successful connection.
            boolean result = mBluetoothGatt.discoverServices();
        }
    };

    @Override
    public void disconnect() {
        if (mBluetoothGatt != null) {
            synchronized (mBluetoothGatt) {
                Log.d(TAG, "active disconnect LE");
                mBluetoothGatt.disconnect();
            }
        }
    }

    @Override
    public boolean write(byte[] cmd) {

        // try to use LE
        if(mGattWriteCharc!= null) {
            mGattWriteCharc.setValue(cmd);
        }else {
            return false;
        }

        if(mBluetoothGatt != null) {
            return mBluetoothGatt.writeCharacteristic(mGattWriteCharc);
        }

        return false;
    }

    @Override
    public void notifyConnected() {
        mAirohaLink.OnPhysicalConnected(typeName());
    }

    @Override
    public void notifyDisconnected() {
        mAirohaLink.OnPhysicalDisconnected(typeName());
    }

    @Override
    public String typeName() {
        return PhysicalType.BLE.toString();
    }
}
