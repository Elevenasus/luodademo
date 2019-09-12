package com.airoha.android.lib.peq;

public class RealTimeEnableMode {

//    (3 sample rate) 
//    enableMode = 0x01 = PEQ >> Total 229 bytes (6+222+1) 
//    enableMode = 0x02 = HPF 
//    enableMode = 0x04 = EXP_PEQ >> Total 229 bytes 
//    enableMode = 0x08 = LB_PEQ >> Total 97 bytes 

    public static final byte THREE_SAMPLE_RATE_PEQ = (byte) 0x01; // Total 229 bytes (6+222+1) 
//    public static final byte THREE_SAMPLE_RATE_HPF = (byte) 0x02; // never used in app
    public static final byte THREE_SAMPLE_RATE_EXP = (byte) 0x04; // Total 229 bytes 
    public static final byte THREE_SAMPLE_RATE_LB = (byte) 0x08;
    //public static final byte THREE_SAMPLE_RATE_PEQ_PLUS_EXP_PLUS_LB = (byte) 0x0C;

    public static final byte ONE_SAMPLE_RATE_PEQ = (byte) 0x11; // 2018.06.14 BTA-1652
    public static final byte ONE_SAMPLE_RATE_PEQ_PLUS_EXP = (byte) 0x15; // 2018.06.14 BTA-1652
    public static final byte ONE_SAMPLE_RATE_PEQ_PLUS_LB = (byte) 0x19; // 2018.06.14 BTA-1652
    public static final byte ONE_SAMPLE_RATE_PEQ_PLUS_EXP_PLUS_LB = (byte) 0x1D; // 2018.06.14 BTA-1652
}
