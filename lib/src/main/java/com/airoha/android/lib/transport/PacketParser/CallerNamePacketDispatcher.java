package com.airoha.android.lib.transport.PacketParser;

import android.util.Log;

import com.airoha.android.lib.mmi.cmd.OCF;
import com.airoha.android.lib.mmi.OnAirohaCallerNameEventListener;
import com.airoha.android.lib.mmi.cmd.SppPacketIndex;
import com.airoha.android.lib.util.Converter;

/**
 * Created by Daniel.Lee on 2016/11/10.
 */

public class CallerNamePacketDispatcher {

    private static final String TAG = "CallerNamePacket";

    private static OnAirohaCallerNameEventListener mOnAirohaCallerNameEventListener;

    public static void setOnAirohaCallerNameEventListener(OnAirohaCallerNameEventListener listener){
        mOnAirohaCallerNameEventListener = listener;
    }

    public static void parseSendCallerNameInExit(byte[] packet){

        if(mOnAirohaCallerNameEventListener == null)
            return;

        byte bOpcode = packet[SppPacketIndex.OCF];

        if(OCF.REPORT_INCOMINGCALL == bOpcode) {
            Log.d(TAG, "REPORT_INCOMINGCALL");
            byte state = packet[5];

            if(state == (byte)0x00){
                mOnAirohaCallerNameEventListener.OnReportExitIncomingCall();
            }

            if(state == (byte)0x01){
                mOnAirohaCallerNameEventListener.OnReportEnterIncomingCall();
            }
        }
    }

    public static void parseSendCallerNameProgress(byte[] packet){
        if(mOnAirohaCallerNameEventListener == null)
            return;

        String progress = Converter.byte2HexStr(packet, packet.length).concat(" ");
        Log.d("caller packet: ", progress);

        // decoding
        /*
                FW收到將回 02 HH HH LL LH 01 07 XX ID
                LL: length Low byte
                LH: length High byte
                XX: 0 = OK, FF = Fail(可能是寫失敗), EE = Stop(本次傳送不播放, 所以請tool停止傳送)
                ID: 收到並已處理完的封包
                */
        byte status = packet[7];
        byte id = packet[8];

        if(status == (byte) 0xEE){
            // broadcast STOP
            mOnAirohaCallerNameEventListener.OnReportStopResp();
        }

        if(status == (byte) 0xFF){
            // brodcast FAIL
            mOnAirohaCallerNameEventListener.OnReportFailResp(id);
        }

        if(status == (byte) 0x00){
            // brodcst Success
            mOnAirohaCallerNameEventListener.OnReportSuccessResp(id);
        }
    }
}
