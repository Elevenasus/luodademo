package com.airoha.android.spp.headset.phoneControl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Daniel.Lee on 2016/8/25.
 */
public class SynthHelper {

    private static final String TAG = "SynthHelper";

    private static String mLastSythnUtteranceID;

    public static String LastSythnUtterancdID(){
        return mLastSythnUtteranceID;
    }

    public static String LastSythnUtteranceFileName(){
        return mLastSythnUtteranceID + ".wav";
    }

    public static void startSythnThread(final Context ctx, final String originStr, final TextToSpeech myTTS){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 2016.09.07 Daniel, add protection
                if(originStr == null || originStr.isEmpty() || originStr.equals(""))
                    return;

                mLastSythnUtteranceID = String.format("airoha_tts_part_%d", 0);

                Log.d(TAG, "start to sythn");

                // Daniel, customer can decide the rate
                myTTS.setSpeechRate((float)1.7);

                File fileToMedia = new File(ctx.getFilesDir(), mLastSythnUtteranceID + ".wav");
                HashMap<String, String> myHashRender = new HashMap<String, String>();
                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mLastSythnUtteranceID); // airoha_tts_part_1 to be checked in OnDone
                myTTS.synthesizeToFile(originStr, myHashRender, fileToMedia.toString());
            }
        }).start();
    }
}
