package com.airoha.android.lib.physical;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.Commander.RespIndPacketBuffer;
import com.airoha.android.lib.transport.ITransport;
import com.airoha.android.lib.transport.PacketParser.PacketHeaderChecker;
import com.airoha.android.lib.util.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by MTK60279 on 2017/11/30.
 */

public class AirohaSppController implements IPhysical{
    private static final String TAG = "AirohaSppController";

    private final static byte[] UUID_AIROHA1520 = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
            (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}; //00000000-0000-0000-0099-aabbccddeeff

//    private static final byte SPP_EVENT_START = 0x04;
//    private static final byte ACL_VCMD_START = 0x02;
    private static final int HEADER_SIZE = 3;

    private BluetoothManager mBluetoothManager;
    /* Get Default Adapter */
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket mbsSocket = null;

    private InputStream mInStream = null;
    private OutputStream mOutStream = null;

    private Context mCtx;
    private boolean mIsConnected = false;

    private ConnectedThread mConnectedThread;


    private ITransport mAirohaLink;

    public AirohaSppController(AirohaLink airohaLink){

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
        Log.d(TAG, "createConn");

        if (!mBluetoothAdapter.isEnabled())
            return false;

        if (mIsConnected)
            this.disconnect();

        try {

            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
                    .getRemoteDevice(address);

            mbsSocket = createRfcomm(device);

            this.mbsSocket.connect();
            this.mOutStream = this.mbsSocket.getOutputStream();
            this.mInStream = this.mbsSocket.getInputStream();
            this.mIsConnected = true;

            Log.d(TAG, "mIsConnectOK true");


            startConnectedThread();

        } catch (IOException e) {
            Log.d(TAG, "createConn, exception:" + e.getMessage());
            this.disconnect();
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (this.mIsConnected) {
            try {
                // 2016.5.3 Daniel commented
                stopConnectedThread();

                if (null != this.mInStream)
                    this.mInStream.close();
                if (null != this.mOutStream)
                    this.mOutStream.close();
                if (null != this.mbsSocket)
                    this.mbsSocket.close();
                this.mIsConnected = false;
                Log.d(TAG, "mIsConnectOK false, normal");
            } catch (IOException e) {

                this.mInStream = null;
                this.mOutStream = null;
                this.mbsSocket = null;
                this.mIsConnected = false;
                Log.d(TAG, "mIsConnectOK false, exception");
            }
        }
    }

    @Override
    public boolean write(byte[] cmd) {
        if (this.mIsConnected) {
            try {
                Log.d("zyyyyy", "write:" + Converter.byte2HexStr(cmd));
                mOutStream.write(cmd);
                mOutStream.flush();
                return true;
            } catch (IOException e) {
                this.disconnect();
                return false;
            }
        } else
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
        return PhysicalType.SPP.toString();
    }


    private static UUID getUuidFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    private BluetoothSocket createRfcomm(BluetoothDevice device) {
        final UUID uuid1520 = getUuidFromByteArray(UUID_AIROHA1520); // 2016.1.14 Daniel, this makes the app only for 1520

        BluetoothSocket bluetoothSocket = null;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid1520);
        } catch (IOException e) {
            return null;
        }
        return bluetoothSocket;
    }

    /**
     * Thread to connectSpp the Bluetooth socket and start the thread that reads from the socket.
     */
    private class ConnectedThread extends Thread {

        private final RespIndPacketBuffer mmRespIndCmr;

        private final Context mCtx;

        private boolean mmIsRunning = false;

        public ConnectedThread(Context ctx) {
            mCtx = ctx;
            mmRespIndCmr = new RespIndPacketBuffer();
            mmIsRunning = true;
        }

        public void run() {
            Log.d(TAG, "ConnectedThread running");
            notifyConnected();

            while (mmIsRunning) {
                try {
                    // read byte
                    byte[] completePacket = new byte[512];
                    byte[] bAryTmp = new byte[512];

                    int len = mInStream.available();
                    Log.d("zyyyyy", "input stream available: " + len);

                    byte b = (byte) mInStream.read(); // get command header type 0x02 or 0x04
                    Log.d(TAG, "input stream starting: " + String.format("%02X", b));
                    int completePacketLength = 0;
                    if (b == PacketHeaderChecker.HCI_EVENT_START) {
                        completePacket[0] = b;
                        b = (byte) mInStream.read();
                        completePacket[1] = b;
                        b = (byte) mInStream.read(); // length
                        completePacket[2] = b;
                        completePacketLength = mInStream.read(bAryTmp, 0, (int) b);

                        System.arraycopy(bAryTmp, 0, completePacket, HEADER_SIZE, completePacketLength + HEADER_SIZE);
                        completePacketLength += HEADER_SIZE;
                    }
                    if (b == PacketHeaderChecker.ACL_EVENT_START) {
                        completePacket[0] = b; // type
                        b = (byte) mInStream.read();
                        completePacket[1] = b; // header 1
                        b = (byte) mInStream.read();
                        completePacket[2] = b; // header 2
                        // 2 bytes length
                        b = (byte) mInStream.read();
                        completePacket[3] = b;
                        b = (byte) mInStream.read();
                        completePacket[4] = b;
                        int leng = Converter.BytesToU16(completePacket[4], completePacket[3]);
                        completePacketLength = mInStream.read(bAryTmp, 0, leng);

                        System.arraycopy(bAryTmp, 0, completePacket, HEADER_SIZE + 2, completePacketLength + HEADER_SIZE + 2);
                        completePacketLength += HEADER_SIZE + 2;
                    }

                    if(b == PacketHeaderChecker.ALEXA_EVENT_START) {
                        completePacket[0] = b;

                        // followed by 8 bytes for license key
                        for(int i = 1; i<=8; i++){
                            b = (byte) mInStream.read();

                            completePacket[i] = b;
                        }

                        completePacketLength = 9;
                    }

                    mmRespIndCmr.addArrayToPacket(completePacket, completePacketLength);

                    byte[] packet = mmRespIndCmr.getPacket();
                    mmRespIndCmr.resetPacket();

                    mAirohaLink.handlePhysicalPacket(packet);


                } catch (IOException ioe) {
                    if (mmIsRunning) {
                        Log.d(TAG, "ConnectedT io exec: " + ioe.getMessage());
                        // 2016.08.18 Daniel: Mantis#7882, on Nexus 5X, not sending ACL_DISCONNECTED to upper layer, this is a workaround
                        // 2017.04.07 Daniel: remove above, use notifyDisconnected
                    } else {
                        Log.d(TAG, "ConnectedT io exec: " + ioe.getMessage() + "--by user");
                    }

                    notifyDisconnected();

                    return;
                } catch (IndexOutOfBoundsException ioobe) {
                    Log.d(TAG, "Connected thread ioobe");
                } catch (Exception e) {
                    Log.d(TAG, "Connected thread Except: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            mmIsRunning = false;

            Log.d(TAG, "ConnectedThread cancel");
        }
    }

    private synchronized void startConnectedThread() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(mCtx);
        mConnectedThread.start();
    }

    private void stopConnectedThread() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
}
