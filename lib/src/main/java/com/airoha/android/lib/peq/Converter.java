package com.airoha.android.lib.peq;

/**
 * Created by MTK60279 on 2017/11/22.
 */

public class Converter {

    public static byte[] convertDoubleToBytes(double[] fwparam){
        byte[] result = null;

        // convert to short first
        short[] sFwparam = new short[fwparam.length];

        result = new byte[sFwparam.length * 2];

        for(int i = 0; i < sFwparam.length; i++){
            sFwparam[i] = (short) fwparam[i];
            // 2017.11.24 Daniel: User Big-Endian to storage
            result[2*i+1] = (byte)(sFwparam[i] & 0x00FF);
            result[2*i] = (byte)((sFwparam[i] & 0xFF00) >> 8 );
        }

        return result;
    }

    public static float convert4BytesToFloat(byte[] input){
        int result = 0;

        for(int i = 0; i<4 ; i++){
            result |= ((input[i] & 0xFF)<<(8*i));
        }

        return (float) (result/1000.0);
    }

    public static float[] convert4BytesArrToFloatArr(byte[] input){
        float[] output = new float[input.length/4];

        for(int i = 0; i< output.length; i++){
            byte[] temp = {input[i*4], input[i*4 +1], input[i*4 +2], input[i*4 +3]};
            output[i] = convert4BytesToFloat(temp);
        }

        return output;
    }

    public static byte[] convertFloatTo4Bytes(float input) {
        byte[] fwParam = new byte[4];

        int multiOneThousand = (int)(input * 1000);

        for(int i = 0; i< 4; i++){
            fwParam[i] = (byte) ((multiOneThousand>>(8*i)) & (0xFF));
        }

        return fwParam;
    }

    public static float[] doubleArrToFloatArr(double[] doubleArray){
        float[] floatArray = new float[doubleArray.length];
        for (int i = 0 ; i < doubleArray.length; i++)
        {
            floatArray[i] = (float) doubleArray[i];
        }

        return floatArray;
    }


    public static byte[] convertFloatArrTo4BytesArr(float[] input){
        byte[] fwParam = new byte[4 * input.length];

        for(int i = 0; i< input.length; i++){
            byte[] converted = convertFloatTo4Bytes(input[i]);

            fwParam[i*4] = converted[0];
            fwParam[i*4 + 1] = converted[1];
            fwParam[i*4 + 2] = converted[2];
            fwParam[i*4 + 3] = converted[3];
        }

        return fwParam;
    }
}
