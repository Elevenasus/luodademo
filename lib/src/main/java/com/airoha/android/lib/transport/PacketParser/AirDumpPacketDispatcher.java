package com.airoha.android.lib.transport.PacketParser;

import android.util.Log;

import com.airoha.android.lib.fieldTest.OnAirohaAirDumpListener;
import com.airoha.android.lib.util.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MTK60279 on 2017/10/23.
 */

public class AirDumpPacketDispatcher {

    private static final String TAG = "AirDumpPacketDispatcher";

    private static OnAirohaAirDumpListener mOnAirohaAirDumpListener;

    public static void setOnAirohaAirDumpListener(OnAirohaAirDumpListener listener){
        mOnAirohaAirDumpListener = listener;
    }

    public static void parseSend(byte[] packet){
        if(mOnAirohaAirDumpListener!=null){

            Log.d(TAG, "raw:" + Converter.byte2HexStr(packet));

            //mOnAirohaAirDumpListener.OnAirDumpDataInd("raw:" + Converter.byte2HexStr(packet) + "\n");

            byte[] dataArr = new byte[packet.length - 9]; // 2017.10.23, Daniel: skip 1st 2 bytes of data
            System.arraycopy(packet, 9, dataArr, 0, dataArr.length);

            List<Short> dataInWord = new ArrayList<Short>(); // signed!
            //dataInWord.clear();
            for(int i = 0; i < dataArr.length; i+=2)
            {
                short tmp = Converter.BytesToShort(dataArr[i], dataArr[i+1]);
                dataInWord.add(tmp);
            }
            Short[] logArr = dataInWord.toArray(new Short[dataInWord.size()]);
            String log = Converter.short2Str(logArr).concat(" ");

            Log.d(TAG, "decoded:" + log);

            mOnAirohaAirDumpListener.OnAirDumpDataInd(log + "\n");
        }
    }

}
