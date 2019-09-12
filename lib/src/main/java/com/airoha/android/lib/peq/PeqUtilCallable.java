package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.peqlib.PeqJniWrapper;

import java.util.concurrent.Callable;

/**
 * Created by Daniel.Lee on 2017/9/7.
 */

public class PeqUtilCallable implements Callable {
    private static final String TAG = "PEQ_UT";

    private static double[] mAllSamplingRates = {32.0, 44.1, 48.0};

    private static final int FW_PARAM_LEN = 37;
    private static final int MAX_SAMPLE_RATE_LEN = 3;

    // 0~2 for peq exp, 3~5, for peq
    private static double[][] mFsXFwParam = new double[MAX_SAMPLE_RATE_LEN][FW_PARAM_LEN];

    public static void setSampleRates(double[] rates) {
        mAllSamplingRates = rates;
        mFsXFwParam = new double[mAllSamplingRates.length * 2][FW_PARAM_LEN];
    }

    private int mThreadId;
    private double mSamplingRate;

    public PeqUtilCallable(int threadId, double samplingRate, double[] freqs, double[] gains, double[] bws) {
        this.mThreadId = threadId;
        this.mSamplingRate = samplingRate;
        this.mFreqs = freqs;
        this.mGains = gains;
        this.mBWs = bws;
    }

    private double[] mFreqs;
    private double[] mGains;
    private double[] mBWs;

    private static boolean runSimulateBySR(final int threadId, final double samplingRate, final double[] freqs, final double[] gains, final double[] bws) {
        PeqJniWrapper.setParam(threadId, samplingRate, 0, 1, 0, 0, 1);

        for (int j = 0; j < freqs.length; j++) {
            PeqJniWrapper.setPeqPoint(threadId, j, freqs[j], gains[j], bws[j]);
        }

        int resultExec = PeqJniWrapper.exec(threadId);

        Log.d(TAG, "resultExec:" + resultExec);

        if (resultExec == -1){
            //return false;
            // 2018.03.21 Daniel: this flow will always make the simulation available. Ported from 152X config tool - start
            double gscalVlaue = 0;
            int peqBound = 0;
            // Won't be infinite loop
            while (resultExec == -1){
                gscalVlaue = PeqJniWrapper.getFgainCheckGscalValue(threadId);
                if (gscalVlaue == 0) {
                    gscalVlaue = 1.0;
                }

                peqBound += 2;

                PeqJniWrapper.setParam(threadId, samplingRate, 0, 1, peqBound, 0, gscalVlaue);

                for (int j = 0; j < freqs.length; j++) {
                    PeqJniWrapper.setPeqPoint(threadId, j, freqs[j], gains[j], bws[j]);
                }

                resultExec = PeqJniWrapper.exec(threadId);
            }

            // 2018.03.21 Daniel: this flow will always make the simulation available. Ported from 152X config tool - end
        }

        double gscalVlaue = PeqJniWrapper.getFgainCheckGscalValue(threadId);
        if(gscalVlaue !=0 ){
            PeqJniWrapper.setParam(threadId, samplingRate, 0, 1, 0, 0, gscalVlaue);

            for (int j = 0; j < freqs.length; j++) {
                PeqJniWrapper.setPeqPoint(threadId, j, freqs[j], gains[j], bws[j]);
            }

            PeqJniWrapper.exec(threadId);
        }


        double[] tmp;
        tmp = PeqJniWrapper.getFwParam(threadId);
        System.arraycopy(tmp, 0, mFsXFwParam[threadId], 0, mFsXFwParam[threadId].length);

        PeqJniWrapper.destroy(threadId);

        return true;
    }

    public static byte[] getPeqExpCombinedResultToSend() {
        byte[] ret = null;

        ret = new byte[mAllSamplingRates.length * FW_PARAM_LEN * 2];

        for (int i = 0; i < mAllSamplingRates.length; i++) {
            byte[] tmp = Converter.convertDoubleToBytes(mFsXFwParam[i]); // 0~2 for peq exp, 3~5, for peq
            System.arraycopy(tmp, 0, ret, i * tmp.length, tmp.length);
        }

        return ret;
    }

    public static byte[] getPeqCombinedResultToSend() {
        byte[] ret = null;

        ret = new byte[mAllSamplingRates.length * FW_PARAM_LEN * 2];


//        public static void replaceLastDoubleWithFormula(double[] rescaleVal){
//            for(int i = 0; i< rescaleVal.length;i++){
//                double d = Math.pow(10, (rescaleVal[i]*-1.0)/20);
//                fsXFwParam[i][36] = fsXFwParam[i][36] * d;
//            }
//
//        }

        // 2018.07.25 Daniel: replace with rescale formula
        // const value
        final double constD = Math.pow(10, -12.0/20);
        for(int i =0; i< mAllSamplingRates.length; i++) {
            double lastD = mFsXFwParam[i+mAllSamplingRates.length][36];
            // replace lastD
            mFsXFwParam[i+mAllSamplingRates.length][36] = lastD * constD;
        }

        for (int i = 0; i < mAllSamplingRates.length; i++) {
            byte[] tmp = Converter.convertDoubleToBytes(mFsXFwParam[i + mAllSamplingRates.length]); // 0~2 for peq exp, 3~5, for peq
            System.arraycopy(tmp, 0, ret, i * tmp.length, tmp.length);
        }

        return ret;
    }

    @Override
    public Boolean call() throws Exception {
        return runSimulateBySR(mThreadId, mSamplingRate, mFreqs, mGains, mBWs);
    }
}
