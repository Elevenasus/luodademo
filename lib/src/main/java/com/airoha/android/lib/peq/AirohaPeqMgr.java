package com.airoha.android.lib.peq;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airoha.android.lib.mmi.OnAirohaFollowerExistingListener;
import com.airoha.android.lib.mmi.cmd.OGF;
import com.airoha.android.lib.peq.DrcMode.DrcModeEnum;
import com.airoha.android.lib.peq.DrcMode.OnAirohaReportDrcModeListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.TransportTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by MTK60279 on 2017/11/10.
 */

public class AirohaPeqMgr {

    private boolean mIsSimSuccess;


//    private PeqUserInputStru mPeqUserInputStru;
//    public void setUiData(PeqUserInputStru peqUserInputStru) {
//        mPeqUserInputStru = peqUserInputStru;
//    }

    public interface OnUiListener {
        void OnRealTimeUpdated(boolean success);

        void OnRelTimeInputRejected();

        void OnFollowerExisting(boolean existing);
    }

    public interface OnSendRealTimeUpdatePeqRespListener {
        void OnSendRealTimeUpdatePeqResp(byte resp, byte ogf);
    }

    private static final String TAG = "AirohaPeqMgr";

    private OnUiListener mUiListener;

    private AirohaLink mAirohaLink = null;

    private static final double DIV = 1000.0;

//    private static double[] mPeqFs = new double[]{44.1}; // as default if no sampling rate configed
//    private static double[] mLpfFs = new double[]{5.5125}; // as default if no sampling rate configed

    private double[] mPeqFsReport = new double[]{44.1}; // as default if no sampling rate configed
    private double[] mLpfFsReport = new double[]{5.5125}; // as default if no sampling rate configed
    private static final double[] ALL_PEQ_FS = new double[]{32.0, 44.1, 48.0};
    private static final double[] ALL_LPF_FS = new double[]{4.0, 5.5125, 6.0};

    private long mTime0;

    // control flag with User Inputs
    private boolean mIsPeqExpEnabled = false;
    private boolean mIsLpfEnabled = false;


    private boolean mIsSingleDrcAllowedForLowBand = true;

    private boolean mIsFollowerExisting = false;

    public static final byte MODE_ENUM_LOW = 0x00;
    public static final byte MODE_ENUM_MID = 0x01;
    public static final byte MODE_ENUM_HIGH = 0x02;
    public static final byte MODE_ENUM_ALL = 0x03;

    private static final int CASE_ONE_SR_MASTER = 0;
    private static final int CASE_ONE_SR_FOLLOWER = 1;
    private static final int CASE_THREE_SR_MASTER = 2;
    private static final int CASE_THREE_SR_FOLLOWER = 3;
    private static final int CASE_ONE_SR_TWS = 4;
    private static final int CASE_THREE_SR_TWS = 5;
    private static final int CASE_WRITE_UI_DATA = 6;

    private int mUxCase = 0;

    private double[] mPeqFreqs;
    private double[] mPeqExpFreqs;
    private double[] mPeqGains;
    private double[] mPeqExpGains;
    private double[] mPeqBws;
    private double[] mPeqExpBws;

    /**
     * Constructor
     *
     * @param airohaLink
     */
    public AirohaPeqMgr(AirohaLink airohaLink) {
        mAirohaLink = airohaLink;
        mAirohaLink.setOnPeqRealTimeUpdateListenr(mOnSendRealTimeUpdatePeqRespListener);
        // TODO 2018.05.08 Verifying
        mAirohaLink.setReportDrcModeListener(mReportDrModeListener);

        mAirohaLink.registerFollowerExistenceListener(TAG, mFollowerExistingListener);
    }

    /**
     * Need to call this before calling {@link #startRealTimeUpdate(double[], double[], double[], double[], double[], double[])}
     *
     * @param modeEnum From FW, sampleRate XX : 0 – 32Khz; 1 – 44.1Khz; 2 – 48Khz ; 0xFF - unknown
     *                 By this class use: 0x03 all sampling rate
     *                 {@link #MODE_ENUM_LOW}
     *                 {@link #MODE_ENUM_MID}
     *                 {@link #MODE_ENUM_HIGH}
     *                 {@link #MODE_ENUM_ALL}
     */
    public void configSamplingRateByCases(byte modeEnum) {
        switch (modeEnum) {
            case MODE_ENUM_LOW:
//                mPeqFs = new double[]{32.0};
                mPeqFsReport = new double[]{32.0};
//                mLpfFs = new double[]{4.0};
                mLpfFsReport = new double[]{4.0};
                break;

            case MODE_ENUM_MID:
//                mPeqFs = new double[]{44.1};
                mPeqFsReport = new double[]{44.1};
//                mLpfFs = new double[]{5.5125};
                mLpfFsReport = new double[]{5.5125};
                break;

            case MODE_ENUM_HIGH:
//                mPeqFs = new double[]{48.0};
                mPeqFsReport = new double[]{48.0};
//                mLpfFs = new double[]{6.0};
                mLpfFsReport = new double[]{5.5125};
                break;

            case MODE_ENUM_ALL:
//                mPeqFs = new double[]{32.0, 44.1, 48.0};
                mPeqFsReport = new double[]{32.0, 44.1, 48.0};
//                mLpfFs = new double[]{4.0, 5.5125, 6.0};
                mLpfFsReport = new double[]{4.0, 5.5125, 6.0};
                break;
        }
    }


    private final OnSendRealTimeUpdatePeqRespListener mOnSendRealTimeUpdatePeqRespListener = new OnSendRealTimeUpdatePeqRespListener() {
        @Override
        public void OnSendRealTimeUpdatePeqResp(byte resp, byte ogf) {
            if (resp == (byte) 0xFF) {
                mUiListener.OnRealTimeUpdated(false);
            } else {
                mUiListener.OnRealTimeUpdated(true);

                // 2018.08.17 Daniel: for Jaybird, the following operations will be controlled in different timings
//                switch (mUxCase){
//                    case CASE_ONE_SR_MASTER:
//                        mUxCase = CASE_THREE_SR_MASTER;

//                        try {
//                            startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
//                                    mPeqBws, mPeqExpBws, TransportTarget.Master, ALL_PEQ_FS, ALL_LPF_FS);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case  CASE_ONE_SR_FOLLOWER:
//                        mUxCase = CASE_THREE_SR_FOLLOWER;
//
//                        try {
//                            startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
//                                    mPeqBws, mPeqExpBws, TransportTarget.Follower, ALL_PEQ_FS, ALL_LPF_FS);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case CASE_ONE_SR_TWS:
//                        mUxCase = CASE_THREE_SR_TWS;
//
//                        try {
//                            startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
//                                    mPeqBws, mPeqExpBws, TransportTarget.Both, ALL_PEQ_FS, ALL_LPF_FS);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//
//                    case CASE_THREE_SR_MASTER:
//                        mUxCase = CASE_WRITE_UI_DATA;
//                        if(mPeqUserInputStru!=null){
//                            Log.d(TAG, "sending UI data");
//                            mAirohaLink.setRealTimeUiData(mPeqUserInputStru.getRaw());
//                        }
//                        mUiListener.OnRealTimeUpdated(true);
//                        break;
//
//                    case CASE_THREE_SR_FOLLOWER:
//                        mUxCase = CASE_WRITE_UI_DATA;
//                        if(mPeqUserInputStru!=null){
//                            mAirohaLink.setRealTimeUiDataFollower(mPeqUserInputStru.getRaw());
//                        }
//                        mUiListener.OnRealTimeUpdated(true);
//                        break;
//
//                    case CASE_THREE_SR_TWS:
//                        mUxCase = CASE_WRITE_UI_DATA;
//                        if(mPeqUserInputStru!=null){
//                            mAirohaLink.setRealTimeUiData(mPeqUserInputStru.getRaw());
//                            mAirohaLink.setRealTimeUiDataFollower(mPeqUserInputStru.getRaw());
//                        }
//                        mUiListener.OnRealTimeUpdated(true);
//                        break;
//                }
            }

            Log.d(TAG, "--- end, total time(with air IO) elapsed: " + (System.currentTimeMillis() - mTime0) + "---");
        }

    };

    private final OnAirohaFollowerExistingListener mFollowerExistingListener = new OnAirohaFollowerExistingListener() {
        @Override
        public void OnSlaveConnected(boolean connected) {
            setFollowerExisting(connected);
        }
    };

    private void setFollowerExisting(boolean existing) {
        Log.d(TAG, "Follower existing: " + existing);
        mIsFollowerExisting = existing;
        mUiListener.OnFollowerExisting(existing);
    }

    private OnAirohaReportDrcModeListener mReportDrModeListener = new OnAirohaReportDrcModeListener() {
        @Override
        public void OnReportDrcMode(byte mode) {
            if (mode == DrcModeEnum.SINGLE) {
                mIsSingleDrcAllowedForLowBand = true;
                return;
            }

            if (mode == DrcModeEnum.DUAL) {
                mIsSingleDrcAllowedForLowBand = false;
                return;
            }

        }
    };


    /**
     * Callback to the UI layer for the response from AirohaPeqMgr's actions
     *
     * @param listener
     */
    public void setUserDefinedPeqParsingListener(OnUiListener listener) {
        mUiListener = listener;
    }

    private void setAlgoParams(@NonNull double[] peqFreqs, @Nullable double[] peqExpFreqs, @NonNull double[] peqGains, @Nullable double[] peqExpGains, @NonNull double[] peqBws, @Nullable double[] peqExpBws) {
        mPeqFreqs = peqFreqs;
        mPeqExpFreqs = peqExpFreqs;
        mPeqGains = peqGains;
        mPeqExpGains = peqExpGains;
        mPeqBws = peqBws;
        mPeqExpBws = peqExpBws;
    }

    /**
     * give the params to start real-time PEQ update
     *
     * @param peqFreqs
     * @param peqExpFreqs can be null if disable Exp bands
     * @param peqGains
     * @param peqExpGains can be null if disable Exp bands
     * @param peqBws
     * @param peqExpBws   can be null if disable Exp bands
     * @throws Exception for alerting the constraints
     */
    public void startRealTimeUpdate(@NonNull final double[] peqFreqs,
                                    @Nullable final double[] peqExpFreqs,
                                    @NonNull final double[] peqGains,
                                    @Nullable final double[] peqExpGains,
                                    @NonNull final double[] peqBws,
                                    @Nullable final double[] peqExpBws) throws Exception {

        mUxCase = CASE_ONE_SR_MASTER;

        mPeqFreqs = peqFreqs;
        mPeqExpFreqs = peqExpFreqs;
        mPeqGains = peqGains;
        mPeqExpGains = peqExpGains;
        mPeqBws = peqBws;
        mPeqExpBws = peqExpBws;

        startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
                mPeqBws, mPeqExpBws, TransportTarget.Master, mPeqFsReport, mLpfFsReport);
    }

    /**
     * Giving 3-sampling rate coeffs will trigger FW to write data to flash
     * Use the filters's specs from previous startRealTime
     * for new filters' specs call {@link #startRealTimeUpdate3SamplingRate(double[], double[], double[], double[], double[], double[])}
     * @throws Exception
     */
    public void startRealTimeUpdate3SamplingRate() throws Exception {
        startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
                mPeqBws, mPeqExpBws, TransportTarget.Master, ALL_PEQ_FS, ALL_LPF_FS);
    }

    /**
     * @see #startRealTimeUpdate3SamplingRate()
     * @param peqFreqs
     * @param peqExpFreqs
     * @param peqGains
     * @param peqExpGains
     * @param peqBws
     * @param peqExpBws
     * @throws Exception
     */
    public void startRealTimeUpdate3SamplingRate(@NonNull final double[] peqFreqs,
                                                     @Nullable final double[] peqExpFreqs,
                                                     @NonNull final double[] peqGains,
                                                     @Nullable final double[] peqExpGains,
                                                     @NonNull final double[] peqBws,
                                                     @Nullable final double[] peqExpBws) throws Exception {

        mPeqFreqs = peqFreqs;
        mPeqExpFreqs = peqExpFreqs;
        mPeqGains = peqGains;
        mPeqExpGains = peqExpGains;
        mPeqBws = peqBws;
        mPeqExpBws = peqExpBws;

        startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
                mPeqBws, mPeqExpBws, TransportTarget.Master, ALL_PEQ_FS, ALL_LPF_FS);
    }

    /**
     * Feed the wrapped {@link PeqUserInputStru} to send to FW for storing
     * @see AirohaLink#setRealTimeUiData(byte[])
     * @see OnAirohaPeqControlListener#OnSetRealTimeUiDataResp
     * @param peqUserInputStru
     */
    public void startUpdateUiData(@NonNull PeqUserInputStru peqUserInputStru) throws Exception{
//        mPeqUserInputStru = peqUserInputStru;
        if(peqUserInputStru == null){
            throw new IllegalArgumentException("Can't be null");
        }

        mAirohaLink.setRealTimeUiData(peqUserInputStru.getRaw());
    }



    /**
     * give the params to start real-time PEQ update to Follower
     *
     * @param peqFreqs
     * @param peqExpFreqs can be null if disable Exp bands
     * @param peqGains
     * @param peqExpGains can be null if disable Exp bands
     * @param peqBws
     * @param peqExpBws   can be null if disable Exp bands
     * @throws Exception for alerting the constraints
     */
    public void startRealTimeUpdateFollower(@NonNull final double[] peqFreqs,
                                            @Nullable final double[] peqExpFreqs,
                                            @NonNull final double[] peqGains,
                                            @Nullable final double[] peqExpGains,
                                            @NonNull final double[] peqBws,
                                            @Nullable final double[] peqExpBws) throws Exception {
        mUxCase = CASE_ONE_SR_FOLLOWER;


        mPeqFreqs = peqFreqs;
        mPeqExpFreqs = peqExpFreqs;
        mPeqGains = peqGains;
        mPeqExpGains = peqExpGains;
        mPeqBws = peqBws;
        mPeqExpBws = peqExpBws;

        startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
                mPeqBws, mPeqExpBws, TransportTarget.Follower, mPeqFsReport, mLpfFsReport);
    }

    public void startRealTimeUpdateTws(@NonNull final double[] peqFreqs,
                                       @Nullable final double[] peqExpFreqs,
                                       @NonNull final double[] peqGains,
                                       @Nullable final double[] peqExpGains,
                                       @NonNull final double[] peqBws,
                                       @Nullable final double[] peqExpBws) throws Exception {

        mUxCase = CASE_ONE_SR_TWS;

        mPeqFreqs = peqFreqs;
        mPeqExpFreqs = peqExpFreqs;
        mPeqGains = peqGains;
        mPeqExpGains = peqExpGains;
        mPeqBws = peqBws;
        mPeqExpBws = peqExpBws;


        startRealTimeUpdateByChannel(mPeqFreqs, mPeqExpFreqs, mPeqGains, mPeqExpGains,
                mPeqBws, mPeqExpBws, TransportTarget.Both, mPeqFsReport, mLpfFsReport);
    }

    private void startRealTimeUpdateByChannel(@NonNull final double[] peqFreqs,
                                              @Nullable final double[] peqExpFreqs,
                                              @NonNull final double[] peqGains,
                                              @Nullable final double[] peqExpGains,
                                              @NonNull final double[] peqBws,
                                              @Nullable final double[] peqExpBws,
                                              final TransportTarget target,
                                              final double[] peqSamplingRates,
                                              final double[] lpfSamplingRates) throws Exception {

        if (peqExpFreqs != null && peqExpGains != null && peqExpBws != null) {
            mIsPeqExpEnabled = true;

            // limit the freq
            checkPeqExpConstraint(peqExpFreqs);

            // limit the bandwidth
            for (double samplingRate : peqSamplingRates) {
                checkBandWidthConstraint(peqExpFreqs, peqExpBws, samplingRate);
            }

        } else {
            mIsPeqExpEnabled = false;
        }

        // limit the freq
        checkFreqConstraint(peqFreqs);

        // limit the bandwidth
        for (double samplingRate : peqSamplingRates) {
            checkBandWidthConstraint(peqFreqs, peqBws, samplingRate);
        }


        mTime0 = System.currentTimeMillis();

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {

                PeqUtilCallable.setSampleRates(peqSamplingRates);
                LpfUtilCallable.setSampleRates(lpfSamplingRates);

                long startTime = System.currentTimeMillis();
                mIsSimSuccess = checkSimulatorResult(peqFreqs, peqExpFreqs, peqGains, peqExpGains,
                        peqBws, peqExpBws, peqSamplingRates, lpfSamplingRates);

                Log.d(TAG, "total sim time:" + (System.currentTimeMillis() - startTime));

                if (mIsSimSuccess) {
                    actionAfterSimSuccess(target, peqSamplingRates.length);
                } else {
                    mUiListener.OnRelTimeInputRejected();
                }

//            }
//        });

//        thread.setPriority(Thread.MAX_PRIORITY);
//        thread.start();
    }


    /**
     * Check the notes in BTA-1719
     * @param peqFreqs
     * @throws Exception
     */
    private void checkFreqConstraint(double[] peqFreqs) throws Exception {
        // 2017.12.28 Daniel: check peqFreqs -- start
        if (peqFreqs[0] <= UserInputConstraint.FREQ_LPF_MIN || peqFreqs[1] <= UserInputConstraint.FREQ_LPF_MIN) {
            throw new Exception(" the first two bands need to higher than 0.025k");
        }

        if (peqFreqs[2] <= UserInputConstraint.FREQ_LPF_MAX || peqFreqs[3] <= UserInputConstraint.FREQ_LPF_MAX || peqFreqs[4] <= UserInputConstraint.FREQ_LPF_MAX) {
            throw new Exception("only the first two bands can be lower than 0.1k");
        }

        if (peqFreqs[0] <= UserInputConstraint.FREQ_LPF_MAX || peqFreqs[1] <= UserInputConstraint.FREQ_LPF_MAX) {
            if (!mIsSingleDrcAllowedForLowBand) {
                throw new Exception("not allowed for tuning low band filter as FW reporting under Dual DRC mode");
            }
        }

        // 2017.12.28 Daniel: check peqFreqs -- end
    }

    private void checkPeqExpConstraint(double[] peqExpFreqs) throws Exception {
        for (double d : peqExpFreqs) {
            if (d <= UserInputConstraint.FREQ_LPF_MAX) {
                throw new Exception("expanded bands can't be lower than 0.1k");
            }
        }
    }


    private void checkBandWidthConstraint(double[] freqs, double[] bandWidths, double samplingRate) throws Exception {
        int endIdx = freqs.length - 1;

        int i = 0;

        if ((freqs[i] - (bandWidths[i] / 2)) < 0) {
            throw new Exception("Freq.1 - (BW1 / 2) should be ≧ 0. Please check.");
        }

        for (; i < endIdx; i++) {
            if ((freqs[i] + bandWidths[i] / 2) > (freqs[i + 1] - bandWidths[i + 1] / 2)) {
                throw new Exception("Freq." + (i + 2) + " - (BW" + (i + 2) + " / 2) should be ≧ Freq." + (i + 1) + " + (BW" + (i + 1) + " / 2). Please check.");
            }

            if ((freqs[i] + bandWidths[i] / 2) > samplingRate) {
                throw new Exception("Freq." + (i + 1) + " + (BW" + (i + 1) + " / 2) should be ≦ " + samplingRate + ". Please check.");
            }
        }
    }

    private void actionAfterSimSuccess(TransportTarget target, int samplingRatesLength) {
        byte mode = RealTimeEnableMode.ONE_SAMPLE_RATE_PEQ;

        byte[] peqSimResults = PeqUtilCallable.getPeqCombinedResultToSend(); // always have to have this
        byte[] peqExpSimResults = null;
        byte[] lpfSimResults = null;

        if(samplingRatesLength == 3){
            mode = RealTimeEnableMode.THREE_SAMPLE_RATE_PEQ;

            Log.d(TAG, "need to send 3 sampling rate coef one by one");

            sendByModeAndTarget(target,mode, peqSimResults);
            Log.d(TAG, " 3 sampling rate PEQ send");

            if (mIsPeqExpEnabled) {
                peqExpSimResults = PeqUtilCallable.getPeqExpCombinedResultToSend();

                mode = RealTimeEnableMode.THREE_SAMPLE_RATE_EXP;
                sendByModeAndTarget(target, mode, peqExpSimResults);
                Log.d(TAG, " 3 sampling rate EXP send");
            }

            if (mIsLpfEnabled) {
                lpfSimResults = LpfUtilCallable.getCombinedResultToSend();

                mode = RealTimeEnableMode.THREE_SAMPLE_RATE_LB;
                sendByModeAndTarget(target, mode, lpfSimResults);
                Log.d(TAG, " 3 sampling rate LPF send");
            }


            return;
        }

        // below is for 1 sampling rate update
        // coef. can be combined in one packet

        byte[] allParam = null;

        allParam = peqSimResults;

        if (mIsPeqExpEnabled) {
            peqExpSimResults = PeqUtilCallable.getPeqExpCombinedResultToSend();

            mode = RealTimeEnableMode.ONE_SAMPLE_RATE_PEQ_PLUS_EXP;

            allParam = new byte[peqSimResults.length + peqExpSimResults.length];
            System.arraycopy(peqSimResults, 0, allParam, 0, peqSimResults.length);
            System.arraycopy(peqExpSimResults, 0, allParam, peqSimResults.length, peqExpSimResults.length);
        }

        if (mIsLpfEnabled) {
            lpfSimResults = LpfUtilCallable.getCombinedResultToSend();

            mode = RealTimeEnableMode.ONE_SAMPLE_RATE_PEQ_PLUS_LB;

            allParam = new byte[peqSimResults.length + lpfSimResults.length];
            System.arraycopy(peqSimResults, 0, allParam, 0, peqSimResults.length);
            System.arraycopy(lpfSimResults, 0, allParam, peqSimResults.length, lpfSimResults.length);
        }

        if (mIsPeqExpEnabled & mIsLpfEnabled) {
            mode = RealTimeEnableMode.ONE_SAMPLE_RATE_PEQ_PLUS_EXP_PLUS_LB;

            allParam = new byte[peqSimResults.length + peqExpSimResults.length + lpfSimResults.length];
            System.arraycopy(peqSimResults, 0, allParam, 0, peqSimResults.length);
            System.arraycopy(peqExpSimResults, 0, allParam, peqSimResults.length, peqExpSimResults.length);
            System.arraycopy(lpfSimResults, 0, allParam, peqSimResults.length + peqExpSimResults.length, lpfSimResults.length);
        }


        sendByModeAndTarget(target, mode, allParam);
    }

    private void sendByModeAndTarget(TransportTarget target, byte mode, byte[] allParam) {
        if (target == TransportTarget.Master) {
            mAirohaLink.sendRealTimeUpdatePEQ(mode, allParam, OGF.AIROHA_MMI_CMD);
        } else if (target == TransportTarget.Follower) {
            mAirohaLink.sendRealTimeUpdatePEQ(mode, allParam, OGF.AIROHA_MMI_CMD_FR);
        } else {
            mAirohaLink.sendRealTimeUpdatePEQ(mode, allParam, OGF.AIROHA_MMI_CMD);
            mAirohaLink.sendRealTimeUpdatePEQ(mode, allParam, OGF.AIROHA_MMI_CMD_FR);
        }
    }

    synchronized private boolean checkSimulatorResult(double[] peqFreqs, double[] peqExpFreqs,
                                         double[] peqGains, double[] peqExpGains,
                                         double[] peqBws, double[] peqExpBws,
                                         double[] peqSamplingRates, double[] lpfSamplingRates) {

        mIsLpfEnabled = false;

        int maxThreads = 8;

        double[] lpBandWidths = null;
        double[] lpfFreqs = null;
        double[] lpfGains = null;

        ArrayList<Double> listLpfFreq = new ArrayList<>();
        ArrayList<Double> listLpfGain = new ArrayList<>();
        ArrayList<Double> listLpfBw = new ArrayList<>();

        ExecutorService threadPoll = Executors.newFixedThreadPool(maxThreads);
        List<Future<Boolean>> resultLists = new ArrayList<>();


        // 2018.08.21 Daniel, PEQ LB Library
        // Max input 2 point
        // dynamically check
        for(int i = 0; i < 2; i++){
            if(peqFreqs[i] <= UserInputConstraint.FREQ_LPF_MAX && peqFreqs[i] > UserInputConstraint.FREQ_LPF_MIN) {
                listLpfFreq.add(peqFreqs[i]);
                listLpfGain.add(peqGains[i]);
                listLpfBw.add(peqBws[i]);

                peqGains[i] = 0; // force to be 0
            }
        }

        lpfFreqs = getDoubleArr(listLpfFreq);
        lpfGains = getDoubleArr(listLpfGain);
        lpBandWidths = getDoubleArr(listLpfBw);

        // 2018.08.15 Daniel, if all of the lpfGains are 0, don't execute the algo.
        if(lpfGains != null) {
            for(double lpfgain : lpfGains){
                if(lpfgain != 0.0){
                    mIsLpfEnabled = true;
                }
            }
        }

        // lpf
        if (mIsLpfEnabled) {
            for (int i = 0; i < lpfSamplingRates.length; i++) {
                LpfUtilCallable lpfUtilCallable = new LpfUtilCallable(i, lpfSamplingRates[i], lpfFreqs, lpfGains, lpBandWidths);
                Future<Boolean> result = threadPoll.submit(lpfUtilCallable);
                resultLists.add(result);
            }
        }

        // exp 5 freq point
        if (mIsPeqExpEnabled) {
            for (int i = 0; i < peqSamplingRates.length; i++) {
                PeqUtilCallable peqUtilCallable = new PeqUtilCallable(i, peqSamplingRates[i], peqExpFreqs, peqExpGains, peqExpBws);
                Future<Boolean> result = threadPoll.submit(peqUtilCallable);
                resultLists.add(result);
            }
        }

        // first 5 freq point
        for (int i = 0; i < peqSamplingRates.length; i++) {
            // shifting thread id
            PeqUtilCallable peqUtilCallable = new PeqUtilCallable(i + peqSamplingRates.length, peqSamplingRates[i], peqFreqs, peqGains, peqBws);
            Future<Boolean> result = threadPoll.submit(peqUtilCallable);
            resultLists.add(result);
        }

        for (Future<Boolean> res : resultLists) {
            try {
                if (res.get() == false) {
                    return false;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public double[] getDoubleArr(ArrayList<Double> doubleArrayList){
        if(doubleArrayList.size() == 0)
            return null;

        double[] result = new double[doubleArrayList.size()];
        for (int index = 0; index < doubleArrayList.size(); index++) {
            result[index] = doubleArrayList.get(index);
        }
        return result;
    }

}



