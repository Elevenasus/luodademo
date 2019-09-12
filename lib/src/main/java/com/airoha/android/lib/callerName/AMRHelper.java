package com.airoha.android.lib.callerName;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel.Lee on 2016/5/5.
 */
//
//    tool將資料以下列形式傳送
//    02 HH HH LL LH 00 07 ID ...................
//    HH HH 為connection handle: FW端不看此欄位
//    LL: length Low byte
//    LH: length High byte
//    total length = 3 + data 長度
//    ID: 目前傳送封包的號碼, 由0開始
//    .......: voice prompt需要的資料內容(長度欄位加上資料, 與voice prompt sector放的格是相同)
//    每包封包請傳 256*2 = 2 pages的資料量, 不足請補0

public class AMRHelper {
    private static final int PACKET_SIZE = 520; // 512+8
    private static final int DADA_SECTOR_SIZE = 512;

    private File mAMRfile;
    private InputStream mInputStream;
    private int mOriginFileLengthPlusHeader;
    private int mTotalCompletePackets;
    private int mTotalFragPacketBytes;
    private byte[] mOriginAmrPlusHeaderBytes;
    private List<byte[]> mListPackets;

    public AMRHelper(File file){
        mAMRfile = file;

        if(mAMRfile.exists()){
            // how many packets needed?

            int tmpNoHeaderFileLength = (int)mAMRfile.length();

            Log.d("AMRHelper", "compressed file size: " + tmpNoHeaderFileLength);

            // +2-byte header,
            mOriginFileLengthPlusHeader = tmpNoHeaderFileLength + 2;

            mTotalCompletePackets = mOriginFileLengthPlusHeader /DADA_SECTOR_SIZE;
            mTotalFragPacketBytes = mOriginFileLengthPlusHeader %DADA_SECTOR_SIZE;

            // read to inputstream
            try {
                mInputStream = new BufferedInputStream(new FileInputStream(mAMRfile));

            } catch (FileNotFoundException e) {

            }

            // read to byte array
            mOriginAmrPlusHeaderBytes = new byte[mOriginFileLengthPlusHeader];
//            b15: 0:normal narrow band, 1:high quality narrow band
//            b14: 0:not used, 1:wide band
//            b13~b0: actual length of voice data, it is allowed with value zero

//            0x44 0xe0 = 01000100  11100000
//
//            01代表WB VP
//            10011100000 = pattern size 1248 bytes

            byte b3 = (byte) tmpNoHeaderFileLength;
            int i2 = tmpNoHeaderFileLength >> 8;
            byte b2 = (byte)i2;

            mOriginAmrPlusHeaderBytes[0] = (byte) (0x40 + b2); // 0x40 = 01 00 00 00
            mOriginAmrPlusHeaderBytes[1] =  b3; //


            byte[] tmpInsBytes = new byte[tmpNoHeaderFileLength];

            try {
                mInputStream.read(tmpInsBytes, 0, tmpNoHeaderFileLength);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.arraycopy(tmpInsBytes, 0, mOriginAmrPlusHeaderBytes, 2, tmpNoHeaderFileLength);


            //  store gened packtes
            mListPackets = new ArrayList<>();

            // add complete packets
            for (int i = 0; i < mTotalCompletePackets; i++){
                byte[] tmp = new byte[DADA_SECTOR_SIZE];

                System.arraycopy(mOriginAmrPlusHeaderBytes, i*DADA_SECTOR_SIZE, tmp, 0, DADA_SECTOR_SIZE);

                byte[] tmp2 = genNewPakcet(i, tmp);

                mListPackets.add(tmp2);
            }

            // add frag packets
            if(mTotalFragPacketBytes!=0){

                byte[] tmp = new byte[DADA_SECTOR_SIZE];

                System.arraycopy(mOriginAmrPlusHeaderBytes, mTotalCompletePackets *DADA_SECTOR_SIZE, tmp, 0, mTotalFragPacketBytes);

                byte[] tmp2 = genNewPakcet((mTotalCompletePackets), tmp);

                mListPackets.add(tmp2);
            }

            writePacketsToLogFile();
        }
    }

    public List<byte[]> getPacketsList(){
        return mListPackets;
    }

    private void writePacketsToLogFile(){
//        List<byte[]> listBytes = mListPackets;
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream("/sdcard/airoha_wb_amr_packets.txt");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i< listBytes.size(); i++){
//            try  {
//                fos.write(listBytes.get(i));
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
//        }
    }


    //        前面header占掉8 byte, 所以data只剩512 (0x0200)
    //
    //        Total length: 512+3 = 515 (0x0203)
    //
    //        所以每下一次send的封包應該會長這樣:
    //
    //                02 00 00 03 02 00 07 ID + 512data

    private byte[] genNewPakcet(int packtIdx, byte[] filePer512){
        byte[] ret = new byte[PACKET_SIZE];

        // header
        ret[0] = 0x02;
        ret[1] = 0x00; // don't care
        ret[2] = 0x00; // don't care

        // length: 515 = 0x0203
        ret[3] = 0x03;// size, Low byte
        ret[4] = 0x02;// size, High byte

        //  fw format
        ret[5] = 0x00;
        ret[6] = 0x07;

        // packet ID
        ret[7] = (byte)packtIdx;

        // copy the data
        System.arraycopy(filePer512, 0, ret, 8, filePer512.length);
        return ret;
    }


}