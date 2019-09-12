package com.airoha.android.lib.jniWrapper;

import android.content.Context;

import java.io.File;

/**
 * Created by Daniel.Lee on 2016/11/15.
 */

public class BasicDSP {
    public static final String JNI_RESULT_SUCCESS  = "AMR_COMPRESSED";

    private static final String libenc = "libenc.so";

    private Context mCtx;

    // 讀取函式庫
    static {
        System.loadLibrary("basicdsp-jni");
    }

    private String mLibPathOfCaller;

    public BasicDSP(Context ctx){
        mCtx = ctx;

        mLibPathOfCaller = "/data/data/" + ctx.getPackageName() + "/lib/";
    }

    private static native String compressFromJNI(String libname, String inputFileName, String outputFileName);

    public String compress(String inputFileName, String outFileName){
        String outputFileName = (new File(mCtx.getFilesDir(), outFileName)).toString();

        return compressFromJNI(mLibPathOfCaller+libenc, inputFileName, outputFileName);
    }
}
