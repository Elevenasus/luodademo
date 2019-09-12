package com.airoha.android.lib.util.logger;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Evonne.Hsieh on 2016/6/7.
 */
public class AirorhaEngineerDbgLog {

    public static void clearLogFile(String fileName){
        File mFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);

        if(mFile.exists()){
            try{
                mFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public static void logToFile(String fileName, String logContent)
    {
        File mFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
        try {
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream mFos = null;
        try {
            mFos = new FileOutputStream(mFile, true);
            mFos.write(logContent.getBytes());
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
