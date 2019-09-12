package com.airoha.android.lib.fieldTest.logger;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Evonne.Hsieh on 2016/6/7.
 */
public class AirDumpLogForDebug {
    private static FileOutputStream mFos;
    private static File mFile;

    private static String LOG_FILE = "AirohaAirDumpForDebug.log";

    public static void setLogFileNameWithTimeStamp(String timeStamp){
        LOG_FILE = timeStamp + "AirohaAirDumpForDebug.log";
    }

    private static void createFile(String name) {
        mFile = new File(Environment.getExternalStorageDirectory(), name);
        try {
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearLogFile(){
        mFile = new File(Environment.getExternalStorageDirectory() + "/" + LOG_FILE);

        if(mFile.exists()){
            try{
                mFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public static void logToFile(String log)
    {
        createFile(LOG_FILE);

        try {
            mFos = new FileOutputStream(mFile, true);
            mFos.write(log.getBytes());
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
}
