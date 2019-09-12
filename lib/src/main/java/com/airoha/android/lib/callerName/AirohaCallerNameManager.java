package com.airoha.android.lib.callerName;

import android.content.Context;
import android.util.Log;

import com.airoha.android.lib.jniWrapper.BasicDSP;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.mmi.cmd.AirohaMMICmd;
import com.airoha.android.lib.mmi.OnAirohaCallerNameEventListener;

import java.io.File;
import java.util.List;

/**
 * This class does the synthesized wav file processing/transmitting job after App's Service has done TTS synthesizeToFile after
 * handling the incoming call.
 * @see AirohaCallerNameManager#setWavNameToStart(String)
 * @see AirohaLink#setCallerNameOn()
 * @see AirohaLink#setCallerNameOff()
 *  @author Daniel.Lee
 */
public class AirohaCallerNameManager {

    private static final String AIROHA_CALLER_NAME_AMR = "airoha_wb_amr.out";
    private static final String TAG = "AirohaCallerNameManager";

    private List<byte[]> mAmrPacketsList;

    private final BasicDSP mBasicDSP;

    private AirohaLink mAirohaLink;

    private final Context mCtx;

    private String mWavFileName;

    private final Object mWaitLock = new Object();

    /**
     * @param airohaLink, you need to make the AirohaLink connected before you feed it to the constructor
     * @param ctx
     */
    public AirohaCallerNameManager(AirohaLink airohaLink, Context ctx){
        mBasicDSP = new BasicDSP(ctx);

        mCtx = ctx;

        try{
            mAirohaLink = airohaLink;

            mAirohaLink.setOnCallerNameEventListener(mCallerNameEventListener);
        }catch (NullPointerException e){
            Log.d(TAG, "not a valid AirohaLink");
        }
    }

    /**
     * After App/Service done synthesizing, call this to start the audio data transmission
     * @param fileName, file name with full path
     */
    public void setWavNameToStart(String fileName){
        Log.d(TAG, "set wav name: " + fileName);
        mWavFileName = fileName;

        synchronized (mWaitLock){
            mWaitLock.notify();
        }
    }

    private void processWavToFw(String fileName){
        File fileToMedia = new File(fileName);

        if(fileToMedia.exists()) {
            Log.d(TAG, "syned file size(byte):" + fileToMedia.length());

            new ProcessWavToFwThread(fileName).start();
        }
    }

    private class ProcessWavToFwThread extends Thread{

        private final String mmFileName;
        public ProcessWavToFwThread(String filename){
            mmFileName = filename;
        }

        @Override
        public void run(){
            Log.d(TAG, "compressing wav");

            String jniStr =  mBasicDSP.compress(mmFileName, AIROHA_CALLER_NAME_AMR);

            Log.d(TAG, "compressed result: " + jniStr);

            // start a thread
            if(jniStr.equals(BasicDSP.JNI_RESULT_SUCCESS)){
                AMRHelper amrHelper = new AMRHelper(new File(mCtx.getFilesDir(), AIROHA_CALLER_NAME_AMR));

                mAmrPacketsList = amrHelper.getPacketsList();

                if(mAmrPacketsList != null && mAmrPacketsList.size()>0){
                    Log.d(TAG, " AmrProcesThread done, total packets:" + mAmrPacketsList.size());
                    // start to send 1st packet
                    //int wroteBytes = Send(mAmrPacketsList.get(0));
                    Send(mAmrPacketsList.get(0));
                    //Log.d(TAG, "AmrProcesThread 1st packted wrote: " + wroteBytes);
                }
            }
        }
    }

    private final OnAirohaCallerNameEventListener mCallerNameEventListener = new OnAirohaCallerNameEventListener() {

        @Override
        public void OnReportEnterIncomingCall() {
            if(mWavFileName!=null){
                Log.d(TAG, "no need to wait");
                processWavToFw(mWavFileName);
            }else{
                synchronized (mWaitLock){
                    try {
                        mWaitLock.wait();
                        Log.d(TAG, "unlocked");
                        processWavToFw(mWavFileName);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void OnReportExitIncomingCall() {
            mWavFileName = null;
        }

        @Override
        public void OnReportStopResp() {

        }

        @Override
        public void OnReportFailResp(byte id) {
            if(mAmrPacketsList == null)
                return;

            if(id<mAmrPacketsList.size()){
                new SendCmdThread(mAmrPacketsList.get(id)).start();
            }else {
                Log.d(TAG, "FW error, the idx is not valid:" + id);
            }
        }

        @Override
        public void OnReportSuccessResp(byte id) {

            Log.d(TAG, "success, pkt id:" + id);

            if(mAmrPacketsList == null)
                return;

            if(id == mAmrPacketsList.size() -1){
                // send complete command
                new SendCmdThread((AirohaMMICmd.CALLER_NAME_PACKET_COMPLETE)).start();

                Log.d(TAG, "complete caller packet send" );
                return;
            }
            // send next
            int next = id +1;
            if(next < mAmrPacketsList.size()) {
                Log.d(TAG, "send next, pkt id" + next);

                new SendCmdThread(mAmrPacketsList.get(next)).start();
            } else {
                Log.d(TAG, "no more packtes to send");
            }
        }
    };


    class SendCmdThread extends Thread{

        final byte[] mBytes;

        public SendCmdThread(byte[] bytes){
            mBytes = bytes;
        }

        @Override
        public void run() {
            Send(mBytes);
            //Log.d(TAG, "SendCmdThread wrote:" + wroteByte);
        }
    }

    private synchronized boolean Send(byte[] command){
        if(isConnect())
            return mAirohaLink.sendCommand(command);
        else
            return false;
    }

    private boolean isConnect(){
        if(null == mAirohaLink){
            Log.d(TAG, "mAirohaLink null!!");
            return false;
        }

        if(!mAirohaLink.isConnected()){
            Log.d(TAG, "mAirohaLink !isConnect!!");
            return false;
        }

        return true;
    }
}
