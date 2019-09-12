package com.airoha.android.lib.peq;

public class PeqUserInputStru {

    private static final int USER_INPUT_PARAM_LEN = 4 * 5; // 4 is the size of float

    private byte[] mUserInputFreqs;// = new byte[USER_INPUT_PARAM_LEN];
    private byte[] mUserInputBws;// = new byte[USER_INPUT_PARAM_LEN];
    private byte[] mUserInputGains;// = new byte[USER_INPUT_PARAM_LEN];

    private byte[] mUserInputExpFreqs;// = new byte[USER_INPUT_PARAM_LEN];
    private byte[] mUserInputExpBws;// = new byte[USER_INPUT_PARAM_LEN];
    private byte[] mUserInputExpGains;// = new byte[USER_INPUT_PARAM_LEN];


    private boolean mIsExpEnabled = false;

    public float getUserInputFreq(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputFreqs, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public float getUserInputBw(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputBws, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public float getUserInputGain(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputGains, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public float getUserInputExpFreq(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputExpFreqs, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public float getUserInputExpBw(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputExpBws, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public float getUserInputExpGain(int idx) {
        byte[] temp = new byte[4];

        System.arraycopy(mUserInputExpGains, idx * 4, temp, 0, 4);

        return Converter.convert4BytesToFloat(temp);
    }

    public void setUserInputFreqs(float[] userInputFreqs){
        mUserInputFreqs = Converter.convertFloatArrTo4BytesArr(userInputFreqs);
    }

    public void setUserInputBws(float[] userInputBws) {
        this.mUserInputBws = Converter.convertFloatArrTo4BytesArr(userInputBws);
    }

    public void setUserInputGains(float[] userInputGain) {
        this.mUserInputGains = Converter.convertFloatArrTo4BytesArr(userInputGain);
    }

    public void setUserInputExpFreqs(float[] userInputFreqExp) {
        if(userInputFreqExp == null){
            mUserInputExpFreqs = null;
            return;
        }

        this.mUserInputExpFreqs = Converter.convertFloatArrTo4BytesArr(userInputFreqExp);
    }

    public void setUserInputBwExp(float[] userInputBwExp) {
        if(userInputBwExp == null) {
            mUserInputExpBws = null;
            return;
        }
        this.mUserInputExpBws = Converter.convertFloatArrTo4BytesArr(userInputBwExp);
    }

    public void setUserInputGainExp(float[] userInputGainExp) {
        if(userInputGainExp == null){
            mUserInputExpGains = null;
            return;
        }
        this.mUserInputExpGains = Converter.convertFloatArrTo4BytesArr(userInputGainExp);
    }


    public PeqUserInputStru() {

    }


    public PeqUserInputStru(byte[] raw) {
        // check length

        if (raw.length < USER_INPUT_PARAM_LEN * 3) {
            // throw exception
            throw new IllegalArgumentException("Unable to parse");
        }

        mIsExpEnabled = false;

        mUserInputFreqs = new byte[USER_INPUT_PARAM_LEN];
        mUserInputBws = new byte[USER_INPUT_PARAM_LEN];
        mUserInputGains = new byte[USER_INPUT_PARAM_LEN];

        int idx = 0;
        System.arraycopy(raw, idx, mUserInputFreqs, 0, USER_INPUT_PARAM_LEN);

        idx += USER_INPUT_PARAM_LEN;
        System.arraycopy(raw, idx, mUserInputBws, 0, USER_INPUT_PARAM_LEN);

        idx += USER_INPUT_PARAM_LEN;
        System.arraycopy(raw, idx, mUserInputGains, 0, USER_INPUT_PARAM_LEN);

        if (raw.length == USER_INPUT_PARAM_LEN * 6) {
            mUserInputExpFreqs = new byte[USER_INPUT_PARAM_LEN];
            mUserInputExpBws = new byte[USER_INPUT_PARAM_LEN];
            mUserInputExpGains = new byte[USER_INPUT_PARAM_LEN];

            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(raw, idx, mUserInputExpFreqs, 0, USER_INPUT_PARAM_LEN);

            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(raw, idx, mUserInputExpBws, 0, USER_INPUT_PARAM_LEN);

            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(raw, idx, mUserInputExpGains, 0, USER_INPUT_PARAM_LEN);

            mIsExpEnabled = true;
        }
    }

    public byte[] getRaw() {
        if(mUserInputFreqs == null || mUserInputBws == null || mUserInputGains == null){
            throw new IllegalStateException("No User Input has been set");
        }


        byte[] raw = new byte[USER_INPUT_PARAM_LEN * 3];

        if (mUserInputExpFreqs != null && mUserInputExpBws != null && mUserInputExpGains != null){
            raw = new byte[USER_INPUT_PARAM_LEN * 6];
        }


        int idx = 0;
        System.arraycopy(mUserInputFreqs, 0, raw, idx, USER_INPUT_PARAM_LEN);

        idx += USER_INPUT_PARAM_LEN;
        System.arraycopy(mUserInputBws, 0, raw, idx, USER_INPUT_PARAM_LEN);

        idx += USER_INPUT_PARAM_LEN;
        System.arraycopy(mUserInputGains, 0, raw, idx, USER_INPUT_PARAM_LEN);

        if (mUserInputExpFreqs != null && mUserInputExpBws != null && mUserInputExpGains != null){
            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(mUserInputExpFreqs, 0, raw, idx, USER_INPUT_PARAM_LEN);

            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(mUserInputExpBws, 0, raw, idx, USER_INPUT_PARAM_LEN);

            idx += USER_INPUT_PARAM_LEN;
            System.arraycopy(mUserInputExpGains, 0, raw, idx, USER_INPUT_PARAM_LEN);
        }

        return raw;
    }

    public boolean isExpEnabled(){
        return mIsExpEnabled;
    }

    public boolean isDataEmpty(){
       byte[] raw = getRaw();

       if(isAllZero(raw)){
           return true;
       }

       // 2018.06.19 Daniel new added for FW change
       if(isAllFF(raw)){
           return true;
       }

       return false;
    }

    private boolean isAllZero(byte[] raw) {
        for (byte b: raw){
            if(b != (byte)0x00){
                return false;
            }
        }

        return true;
    }

    private boolean isAllFF(byte[] raw) {
        for (byte b: raw){
            if(b != (byte)0xFF){
                return false;
            }
        }

        return true;
    }
}
