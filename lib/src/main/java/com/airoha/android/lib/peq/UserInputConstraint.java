package com.airoha.android.lib.peq;

/**
 * Created by MTK60279 on 2018/3/8.
 */

/**
 * This is for checking the user input values before running Airoha's DSP algo.
 * Input Freq. and get the range for Gain min/max
 */
public class UserInputConstraint {
    public static final int GAIN_MIN = -12; // dB
    public static final int GAIN_MAX = 12; // dB

    public static final double FREQ_6DB_GAIN_MAX = 0.25; // kHz
    public static final double FREQ_LPF_MAX = 0.1; // kHZ
    public static final double FREQ_LPF_MIN = 0.025; // kHz

    public static final int LPF_GAIN_MAX = 6; // dB
    public static final int LPF_GAIN_MIN = -6; // dB
    //static final double FREQ_100 = 0.1; // kHz

    /**
     *
     * @param inputFreq kHz
     * @return
     */
    public static int getGainDbRangeMax(double inputFreq) {
        if(inputFreq <= FREQ_6DB_GAIN_MAX){
            return LPF_GAIN_MAX;
        }else {
            return GAIN_MAX;
        }
    }

    /**
     *
     * @param inputFreq kHz
     * @return
     */
    public static int getGainDbRangeMin(double inputFreq) {
        if(inputFreq<= FREQ_6DB_GAIN_MAX){
            return LPF_GAIN_MIN;
        }else {
            return GAIN_MIN;
        }
    }


}


