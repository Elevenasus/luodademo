package com.airoha.android.spp.headset.phoneControl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.airoha.android.lib.callerName.AirohaCallerNameManager;
import com.airoha.android.lib.transport.AirohaLink;

import java.io.File;
import java.util.Locale;

/**
 * Created by Daniel.Lee on 2017/5/4.
 * Handling incoming call
 * Check Caller's name from the phone book
 * This is an example. SDK user can customize what to do before set the wav to AirohaCallerNameManager
 */

public class IncomingCallPreProcessor {

    private static final String TAG = "InCallPreProcessor";

    private static final String DEFAULT_ENGINE = "com.google.android.tts";

    private String mCallerNameOrNumber;

    private TextToSpeech myTTS;

    private Context mCtx;

    private AirohaCallerNameManager mAirohaCallerNameManager;

    /**
     * This constructor can only be called within the UI thread since the PhoneStateListener depends on the Looper
     * @param airohaLink, you need to make the AirohaLink connected before you feed it to the constructor
     * @param ctx
     */
    public IncomingCallPreProcessor(AirohaLink airohaLink, Context ctx){
        mCtx = ctx;

        //register for phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        // 2016.08.15 Daniel: Mantis#7601, multiple incoming call events
        mPhoneListener = new AirohaPhoneListner();
        telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        mAirohaCallerNameManager = new AirohaCallerNameManager(airohaLink, ctx);
    }

    /**
     * You must call the unregisterPhoneListner for ending the life cycle of your Service
     */
    public void unregisterPhoneListner(){
        // 2016.08.15 Daniel: Mantis#7601, multiple incoming call events
        //unregister for phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    private UtteranceProgressListener mUtterListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Log.d(TAG, "TTS progress start: " + utteranceId);
        }

        @Override
        public void onDone(String utteranceId) {
            Log.d(TAG, "TTS progress done: "+ utteranceId);

            // if the last utterance id
            if(utteranceId.equals(SynthHelper.LastSythnUtterancdID())){

                Log.d(TAG, "TTS start to resample last id:" + SynthHelper.LastSythnUtterancdID());

                String synedFileName = (new File(mCtx.getFilesDir(), SynthHelper.LastSythnUtteranceFileName())).toString();

                mAirohaCallerNameManager.setWavNameToStart(synedFileName);
            }
        }

        @Override
        public void onError(String utteranceId) {
            Log.d(TAG, "TTS progress error: " + utteranceId);
        }
    };

    // 2016.08.15 Daniel: Mantis#7601, multiple incoming call events
    private AirohaPhoneListner mPhoneListener;// = new AirohaPhoneListner();

    private class AirohaPhoneListner extends PhoneStateListener {

        private boolean mmIsDoingCallerAnnouncement = false;
        private final String Tag = "AirohaPhoneListner";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.v(Tag, "state:"+state);
            switch (state) {

                case TelephonyManager.CALL_STATE_IDLE:

                    mmIsDoingCallerAnnouncement = false;
                    // 2016.10.03 Daniel: initTTS and safeShutdownTTS in PhoneState Listener
                    shutdownTTS();
                    break;

                case TelephonyManager.CALL_STATE_RINGING:

                    if(mmIsDoingCallerAnnouncement)
                        return;

                    Log.v(Tag, "incomingNumber:" + incomingNumber + " received");
                    mmIsDoingCallerAnnouncement = true;
                    String callerName = ContactLookup.getIncomingCallerName(mCtx, incomingNumber);

                    String callerNameOrNumber;
                    if(callerName == null || callerName.isEmpty()){
                        callerNameOrNumber = incomingNumber;
                    }else {
                        callerNameOrNumber = callerName;
                    }

                    mCallerNameOrNumber = callerNameOrNumber;

                    // 2016.10.03 Daniel: initTTS and safeShutdownTTS in PhoneState Listener
                    initTTSforCallerNameSythn();

                    break;

                default:
                    break;
            }
        }
    }

    private void shutdownTTS() {
        if(myTTS!=null){
            try{

                Log.d(TAG, "TTS shutdown");

                myTTS.shutdown();
            }catch (IllegalArgumentException e){
                // Google tts issue
            }
            myTTS = null;
        }
    }

    private void initTTSforCallerNameSythn(){
        // 2016.10.05 Daniel: force to release, TTS will disconnect after long running. Unknown reason.
        if(myTTS!=null) {
            myTTS = null;
        }

        myTTS = new TextToSpeech(mCtx, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    Log.d(TAG, "TTS init success");

                    myTTS.setLanguage(Locale.getDefault());
                    myTTS.setOnUtteranceProgressListener(mUtterListener);

                    SynthHelper.startSythnThread(mCtx, mCallerNameOrNumber, myTTS);
                }
            }
        }, DEFAULT_ENGINE);
    }
}
