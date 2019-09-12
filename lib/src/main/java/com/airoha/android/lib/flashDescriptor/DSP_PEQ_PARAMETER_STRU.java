package com.airoha.android.lib.flashDescriptor;

import android.util.Log;

/**
 * Created by Daniel.Lee on 2017/9/13.
 */

public class DSP_PEQ_PARAMETER_STRU {
    private static final String TAG = "DSP_PEQ_PARAMETER_STRU";

    // content:
    private static final int MAX_SET = 1;
    private static final int PEQ_PARAM_LEN = 222; // length 37*2*3
    private static final int LPF_PARAM_LEN = 15*2*3;
    private static final int USER_INPUT_PARAM_LEN = 4 * 5; // 10

    private static final int USER_INPUT_SECTOR_LENGTH = 512; // 2*256
    private static final int USER_INPUT_SECTOR_ADDR = 8192 - USER_INPUT_SECTOR_LENGTH;//8192-512;

    private byte num_of_a2dp_peq_config = 0;
    private byte num_of_linein_peq_config = 0;

    private byte[][] mA2dpPeq = new byte[MAX_SET][PEQ_PARAM_LEN];
    private byte[][] mLineInPeq;

    private byte peq_exp_enable = 1; // keep always 1

    private byte[][] mA2dpPeqExp = new byte[MAX_SET][PEQ_PARAM_LEN];
    private byte[][] mLineInPeqExp;

    private byte num_of_AudioTransparency_peq_config = 0; // keep

    private PEQ_100HZ_CUSTOM_CONFIG_SECTOR[] mA2dpLpf = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR[MAX_SET];

    private byte hpf_enable = 0x00; // force to be 0, not open for tuning

    private byte[][] mUserInputFreq = new byte[MAX_SET][USER_INPUT_PARAM_LEN];
    private byte[][] mUserInputBw = new byte[MAX_SET][USER_INPUT_PARAM_LEN];
    private byte[][] mUserInputGain = new byte[MAX_SET][USER_INPUT_PARAM_LEN];

    private byte[][] mUserInputFreqExp = new byte[MAX_SET][USER_INPUT_PARAM_LEN];
    private byte[][] mUserInputBwExp = new byte[MAX_SET][USER_INPUT_PARAM_LEN];
    private byte[][] mUserInputGainExp = new byte[MAX_SET][USER_INPUT_PARAM_LEN];

    private byte[] originalConfig;

    class PEQ_100HZ_CUSTOM_CONFIG_SECTOR{
        byte mEnable;
        byte[] mContent;

        public PEQ_100HZ_CUSTOM_CONFIG_SECTOR(byte enable){
            mEnable = enable;

            if(enable == 0x01){
                mContent = new byte[LPF_PARAM_LEN];
            }
        }
    }


    // init a all new one
    public DSP_PEQ_PARAMETER_STRU() {
        originalConfig = new byte[8192];
    }

    // copy
    public DSP_PEQ_PARAMETER_STRU(byte[] packet) {

        try{
            // array copy
            originalConfig = new byte[packet.length];
            System.arraycopy(packet, 0, originalConfig, 0, packet.length);

            int idx = 0;
            num_of_a2dp_peq_config = originalConfig[idx]; //0~5
            idx++; // 1

            num_of_linein_peq_config = originalConfig[idx]; // 0~5 don't care
            idx++; //2

            if (num_of_a2dp_peq_config != 0 && num_of_a2dp_peq_config != -1) {
                mA2dpPeq = new byte[num_of_a2dp_peq_config][PEQ_PARAM_LEN];

                for (int i = 0; i < num_of_a2dp_peq_config; i++) {
                    System.arraycopy(originalConfig, idx, mA2dpPeq[i], 0, PEQ_PARAM_LEN);
                    idx = idx + PEQ_PARAM_LEN; // 222*i
                }
            }

            if (num_of_linein_peq_config != 0 && num_of_linein_peq_config != -1) {
                mLineInPeq = new byte[num_of_linein_peq_config][PEQ_PARAM_LEN];

                for (int i = 0; i < num_of_linein_peq_config; i++) {
                    System.arraycopy(originalConfig, idx, mLineInPeq[i], 0, PEQ_PARAM_LEN);
                    idx = idx + PEQ_PARAM_LEN; // 222*i
                }
            }

            // idx = 2+222*5 = 1112
            peq_exp_enable = 1;//originalConfig[idx];//1
            idx++; // 1113


            if (num_of_a2dp_peq_config != 0 && num_of_a2dp_peq_config != -1) {
                mA2dpPeqExp = new byte[num_of_a2dp_peq_config][PEQ_PARAM_LEN];

                for (int i = 0; i < num_of_a2dp_peq_config; i++) {
                    System.arraycopy(originalConfig, idx, mA2dpPeqExp[i], 0, PEQ_PARAM_LEN);
                    idx = idx + PEQ_PARAM_LEN; // 222*i
                }
            }

            if (num_of_linein_peq_config != 0 && num_of_linein_peq_config != -1) {
                mLineInPeqExp = new byte[num_of_linein_peq_config][PEQ_PARAM_LEN];

                for (int i = 0; i < num_of_linein_peq_config; i++) {
                    System.arraycopy(originalConfig, idx, mLineInPeqExp[i], 0, PEQ_PARAM_LEN);
                    idx = idx + PEQ_PARAM_LEN; // 222*i
                }
            }

            // idx = 1113 + 1110 = 2223
            num_of_AudioTransparency_peq_config = originalConfig[idx];
            idx++; // 2224

            if (num_of_AudioTransparency_peq_config != 0 && num_of_AudioTransparency_peq_config != -1) {
                // Daniel: don't care about AT, not open for tuning
            }

            // LPF
            if(num_of_a2dp_peq_config != 0 && num_of_a2dp_peq_config != -1 ){
                mA2dpLpf = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR[num_of_a2dp_peq_config];

                for(int i = 0; i<num_of_a2dp_peq_config; i++){
                    mA2dpLpf[i] = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR(originalConfig[idx]);
                    idx++;
                    if(mA2dpLpf[i].mEnable == 0x01){
                        System.arraycopy(originalConfig, idx, mA2dpLpf[i].mContent, 0, LPF_PARAM_LEN);
                        idx = idx + LPF_PARAM_LEN;
                    }
                }
            }


            hpf_enable = originalConfig[idx];
            idx++;

            if (hpf_enable != 0) {
                // Daniel: don't care about hpf, not open for tuning
            }

            // -- start for user input --
            idx = USER_INPUT_SECTOR_ADDR;

            byte[] userInputSector = new byte[USER_INPUT_SECTOR_LENGTH];

            System.arraycopy(originalConfig, idx, userInputSector, 0, userInputSector.length);

            if(!isUserInputEmpty(userInputSector)) {

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputFreq[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputBw[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputGain[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputFreqExp[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputBwExp[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }

                for(int i = 0; i< num_of_a2dp_peq_config; i++){
                    System.arraycopy(originalConfig, idx, mUserInputGainExp[i], 0, USER_INPUT_PARAM_LEN);
                    idx = idx + USER_INPUT_PARAM_LEN;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void replacePeqParam(int idx, byte[] param) {
        // resize
        if(idx > mA2dpPeq.length -1){
            byte[][] tmp = new byte[idx+1][PEQ_PARAM_LEN];
            for(int i = 0; i< mA2dpPeq.length; i++){
                System.arraycopy(mA2dpPeq[i], 0, tmp[i], 0, PEQ_PARAM_LEN);
            }

            mA2dpPeq = tmp;
        }

        System.arraycopy(param, 0, mA2dpPeq[idx], 0, param.length);
    }

    public void replaceLpfParam(int idx, byte[] param){
        // resize
        if(idx > mA2dpLpf.length -1){
            PEQ_100HZ_CUSTOM_CONFIG_SECTOR[] tmp = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR[idx+1];

            for (int i = 0; i< mA2dpLpf.length; i++){
                tmp[i] = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR(mA2dpLpf[i].mEnable);
                System.arraycopy(mA2dpLpf[i].mContent, 0, tmp[i].mContent, 0, LPF_PARAM_LEN);
            }
            mA2dpLpf = tmp;
        }

        if(param == null){
            mA2dpLpf[idx] = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR((byte) 0x00);
        }else {
            mA2dpLpf[idx] = new PEQ_100HZ_CUSTOM_CONFIG_SECTOR((byte) 0x01);

            System.arraycopy(param, 0, mA2dpLpf[idx].mContent, 0, param.length);
        }
    }

    public void replacePeqExpParam(int idx, byte[] param) {
        // resize
        if(idx > mA2dpPeqExp.length -1){
            byte[][] tmp = new byte[idx+1][PEQ_PARAM_LEN];
            for(int i = 0; i< mA2dpPeqExp.length; i++){
                System.arraycopy(mA2dpPeqExp[i], 0, tmp[i], 0, PEQ_PARAM_LEN);
            }

            mA2dpPeqExp = tmp;
        }

        System.arraycopy(param, 0, mA2dpPeqExp[idx], 0, param.length);
    }

    public void replaceUserInputFreq(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputFreq[idx], 0, userInput.length);
    }

    public void replaceUserInputBw(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputBw[idx], 0, userInput.length);
    }

    public void replaceUserInputGain(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputGain[idx], 0, userInput.length); // length 10
    }

    public void replaceUserInputFreqExp(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputFreqExp[idx], 0, userInput.length);
    }

    public void replaceUserInputBwExp(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputBwExp[idx], 0, userInput.length);
    }

    public void replaceUserInputGainExp(int idx, byte[] userInput) {
        System.arraycopy(userInput, 0, mUserInputGainExp[idx], 0, userInput.length); // length 10
    }

    public byte[] getUserInputFreq(int idx) {
        return mUserInputFreq[idx];
    }

    public byte[] getUserInputBw(int idx) {
        return mUserInputBw[idx];
    }

    public byte[] getUserInputGain(int idx) {
        return mUserInputGain[idx];
    }

    public byte[] getUserInputFreqExp(int idx){
        return mUserInputFreqExp[idx];
    }

    public byte[] getUserInputBwExp(int idx){
        return mUserInputBwExp[idx];
    }

    public byte[] getUserInputGainExp(int idx) {
        return mUserInputGainExp[idx];
    }


    public byte[] getBytes() {

        // clean
        originalConfig = new byte[8192];

        int idx = 0;
        //num_of_a2dp_peq_config = 5;//originalConfig[idx]; //5
        originalConfig[idx] = num_of_a2dp_peq_config;

        idx++; // 1
        // num_of_linein_peq_config = originalConfig[idx]; // 0 or don't care
        originalConfig[idx] = num_of_linein_peq_config;

        idx++; //2
        for (int i = 0; i < num_of_a2dp_peq_config; i++) {
            //System.arraycopy(originalConfig, idx, mA2dpPeq[i], 0, PEQ_PARAM_LEN);
            System.arraycopy(mA2dpPeq[i], 0, originalConfig, idx, PEQ_PARAM_LEN);
            idx = idx + PEQ_PARAM_LEN; // 222*i
        }

        for (int i = 0; i < num_of_linein_peq_config; i++) {
            System.arraycopy(mLineInPeq[i], 0, originalConfig, idx, PEQ_PARAM_LEN);
            idx = idx + PEQ_PARAM_LEN;
        }

        // idx = 2+222*5 = 1112
        // peq_exp_enable = 1;//originalConfig[idx];//1
        originalConfig[idx] = peq_exp_enable; //1
        idx++; // 1113

        for (int i = 0; i < num_of_a2dp_peq_config; i++) {
            //System.arraycopy(originalConfig, idx, mA2dpPeqExp[i], 0, PEQ_PARAM_LEN);
            System.arraycopy(mA2dpPeqExp[i], 0, originalConfig, idx, PEQ_PARAM_LEN);
            idx = idx + PEQ_PARAM_LEN; //222*i;
        }

        for (int i = 0; i < num_of_linein_peq_config; i++) {
            System.arraycopy(mLineInPeqExp[i], 0, originalConfig, idx, PEQ_PARAM_LEN);
            idx = idx + PEQ_PARAM_LEN;
        }

        // idx = 1113 + 1110 = 2223
        //num_of_AudioTransparency_peq_config = originalConfig[idx];
        originalConfig[idx] = num_of_AudioTransparency_peq_config;
        idx++; // 2224

        for (int i = 0; i < num_of_AudioTransparency_peq_config; i++) {
            // Daniel: don't care about AT, not open for tuning
        }

        for(int i = 0; i < num_of_a2dp_peq_config; i++){
            originalConfig[idx] = mA2dpLpf[i].mEnable;
            idx++;
            if(mA2dpLpf[i].mEnable == 0x01){
                System.arraycopy(mA2dpLpf[i].mContent, 0, originalConfig, idx, LPF_PARAM_LEN);
                idx = idx + LPF_PARAM_LEN;
            }
        }

        originalConfig[idx] = hpf_enable;
        idx++;

        // -- start for user input --
        idx = USER_INPUT_SECTOR_ADDR;

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputFreq[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputBw[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputGain[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputFreqExp[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputBwExp[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        for(int i = 0; i< num_of_a2dp_peq_config; i++) {
            System.arraycopy(mUserInputGainExp[i], 0, originalConfig, idx, USER_INPUT_PARAM_LEN);
            idx = idx + USER_INPUT_PARAM_LEN;
        }

        return originalConfig;
    }

    private boolean isUserInputEmpty(byte[] lastPage){
        return isAllFF(lastPage);
    }

    public void setNumberOfSets(byte numSets) {
        Log.d(TAG, "setNumberOfSets: " + numSets);

        num_of_a2dp_peq_config = numSets;
    }

    public byte getNumberOfSets(){
        return num_of_a2dp_peq_config;
    }

    private boolean isAllFF(byte[] arr) {
        for (byte b : arr) {
            if (b != (byte) 0xFF) {
                return false;
            }
        }

        return true;
    }

}
