package com.airoha.android.lib.peq;

import android.util.Log;

import com.airoha.lpflib.LpfJniWrapper;

import java.util.concurrent.Callable;

/**
 * Created by MTK60279 on 2017/11/7.
 */

public class LpfUtilCallable implements Callable {
    private static final String TAG = "LPF_UT";

    private static double[] mAllSamplingRates = {4.0, 5.5125, 6.0};
    private static final int MAX_SAMPLE_RATE_LEN = 3;
    private static final int FW_PARAM_LEN = 15;

    private static double[][] mFsXFwParam = new double[MAX_SAMPLE_RATE_LEN][FW_PARAM_LEN];

    public static void setSampleRates(double[] rates) {
        mAllSamplingRates = rates;
        mFsXFwParam = new double[mAllSamplingRates.length][FW_PARAM_LEN];
    }

    public LpfUtilCallable(int threadId, double samplingRate, double[] freqs, double[] gains, double[] bws) {
        this.mThreadId = threadId;
        this.mSamplingRate = samplingRate;
        this.mFreqs = freqs;
        this.mGains = gains;
        this.mBWs = bws;
    }

    private int mThreadId;
    private double mSamplingRate;
    private double[] mFreqs;
    private double[] mGains;
    private double[] mBWs;

    private static boolean runSimulateBySR(final int threadId, final double samplingRate, final double[] freqs, final double[] gains, final double[] bws) {
        int order_sel = 0;
        //int eq_band = 2;
        int para_limit_en = 1;
        int bound_step = 0;
        int gadj_mode = 0;
        double gscal_sug = 1.0;
        int band_num = 1;//2; //Set number of band .Support 1 or 2 bands

        band_num = freqs.length; // could be 1 or 2

        LpfJniWrapper.setParam(threadId, samplingRate, order_sel, para_limit_en, bound_step, gadj_mode, gscal_sug, band_num);

        for (int i = 0; i < freqs.length; i++) {
            LpfJniWrapper.setPeqPoint(threadId, i, freqs[i], gains[i], bws[i]);
        }

        int resultExec = LpfJniWrapper.exec(threadId);

        Log.d(TAG, "resultExec:" + resultExec);

        if (resultExec == -1){
            //return false;
            // 2018.03.21 Daniel: this flow will always make the simulation available. Ported from 152X config tool - start
            double gscalVlaue = 0;
            int peqBound = 0;
            // Won't be infinite loop
            while (resultExec == -1){
                gscalVlaue = LpfJniWrapper.getFgainCheckGscalValue(threadId);
                if (gscalVlaue == 0) {
                    gscalVlaue = 1.0;
                }

                peqBound += 2;

                LpfJniWrapper.setParam(threadId, samplingRate, order_sel, para_limit_en, peqBound, gadj_mode, gscalVlaue, band_num);

                for (int j = 0; j < freqs.length; j++) {
                    LpfJniWrapper.setPeqPoint(threadId, j, freqs[j], gains[j], bws[j]);
                }

                resultExec = LpfJniWrapper.exec(threadId);
            }

            // 2018.03.21 Daniel: this flow will always make the simulation available. Ported from 152X config tool - end
        }

        double gscalVlaue = LpfJniWrapper.getFgainCheckGscalValue(threadId);

        if(gscalVlaue != 0){
            LpfJniWrapper.setParam(threadId, samplingRate, 0, 1, 0, 0, gscalVlaue, band_num);

            for (int j = 0; j < freqs.length; j++) {
                LpfJniWrapper.setPeqPoint(threadId, j, freqs[j], gains[j], bws[j]);
            }

            LpfJniWrapper.exec(threadId);
        }


        double[] tmp;
        tmp = LpfJniWrapper.getFwParam(threadId);
        System.arraycopy(tmp, 0, mFsXFwParam[threadId], 0, mFsXFwParam[threadId].length);

        LpfJniWrapper.destroy(threadId);

        return true;
    }

    @Override
    public Boolean call() throws Exception {
        return runSimulateBySR(mThreadId, mSamplingRate, mFreqs, mGains, mBWs);
    }

    public static byte[] getCombinedResultToSend() {
        byte[] ret = null;

        ret = new byte[mAllSamplingRates.length * FW_PARAM_LEN * 2];

        for (int i = 0; i < mAllSamplingRates.length; i++) {
            byte[] tmp = Converter.convertDoubleToBytes(mFsXFwParam[i]);
            System.arraycopy(tmp, 0, ret, i * tmp.length, tmp.length);
        }

        return ret;
    }
}
