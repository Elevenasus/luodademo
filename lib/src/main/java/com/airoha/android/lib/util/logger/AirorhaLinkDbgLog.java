package com.airoha.android.lib.util.logger;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Evonne.Hsieh on 2016/6/7.
 */
public class AirorhaLinkDbgLog {

    private String mDeviceName;

    private File mFile = null;

    private BlockingQueue<String> mLogStringQueue;

    private LogThread mLogThread;

    public AirorhaLinkDbgLog(String deviceName) {
        mDeviceName = deviceName;

        if(mDeviceName == null){
            return;
        }

        mFile = new File(Environment.getExternalStorageDirectory(), mDeviceName + "AirohaLink.txt");
        try {
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mLogStringQueue = new LinkedBlockingQueue<>();

        mLogThread = new LogThread();
        mLogThread.start();
    }

    public synchronized void addStringToQueue(String tag, String logContent) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

        String logString = timeStamp + "--" + tag + ":" + logContent + "\n";

        mLogStringQueue.add(logString);
    }

    class LogThread extends Thread {
        @Override
        public void run() {
            while(mLogStringQueue!=null){
                if(mLogStringQueue.size() > 0){
                    String logStr = mLogStringQueue.poll();

                    if(logStr != null){
                        logToFile(logStr);
                    }
                }
            }
        }
    }

    public synchronized void logToFile(String logString) {
        if(mFile == null){
            return;
        }

        FileOutputStream mFos = null;
        try {
            mFos = new FileOutputStream(mFile, true);

            mFos.write(logString.getBytes());
            mFos.flush();
            mFos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFos != null) {
                    mFos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public synchronized void logToFile(String tag, String logContent)
    {
        if(mFile == null){
            return;
        }

        FileOutputStream mFos = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            mFos = new FileOutputStream(mFile, true);
            mFos.write((timeStamp + "--").getBytes());
            mFos.write((tag+": ").getBytes());
            mFos.write(logContent.getBytes());
            mFos.write("\n".getBytes());
            mFos.flush();
            mFos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFos != null) {
                    mFos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        if(mLogStringQueue != null){
            mLogStringQueue.clear();

            mLogStringQueue = null;
        }
    }
}
