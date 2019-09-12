package com.airoha.android.lib.peq;

import com.airoha.android.lib.transport.AirohaLink;

public interface OnAirohaPeqControlListener {

    /**
     * @param sampleRateEnum U8 SampleRate XX : 0 – 32Khz; 1 – 44.1Khz; 2 – 48Khz ; 0xFF - unknown
     */
    void OnMusicSampleRateChanged(final byte sampleRateEnum);


    /**
     * @param resp
     * @see AirohaLink#getMusicSampleRate()
     */
    void OnGetMusicSampleRateResp(final byte resp);

    /**
     * @param resp
     * @see AirohaLink#getRealTimeUiData()
     */
    void OnGetRealTimeUiDataResp(final byte resp);

    /**
     * @param data
     * @see AirohaLink#getRealTimeUiData()
     */
    void OnGetRealTimeUiDataInd(final byte[] data);

    /**
     * @param data
     */
    void OnReportRealTimeUiDataInd(final byte[] data);

    /**
     * @param resp
     * @see AirohaLink#setRealTimeUiData(byte[])
     */
    void OnSetRealTimeUiDataResp(final byte resp);
}
