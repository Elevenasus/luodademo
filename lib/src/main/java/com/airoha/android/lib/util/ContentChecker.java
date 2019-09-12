package com.airoha.android.lib.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by MTK60279 on 2017/10/19.
 */

public class ContentChecker {
    public static boolean isAllDummyHexFF(byte[] dataBuffer){
        for(int i=0; i<dataBuffer.length; i++){
            if (dataBuffer[i] != (byte) 0xFF){
                return false;
            }
        }

        return true;
    }

    private static int bufferSize(long fileLength) {
        int multiple = (int) (fileLength / 1024);
        if (multiple <= 1) {
            return 1024;
        } else if (multiple <= 8) {
            return 1024 * 2;
        } else if (multiple <= 16) {
            return 1024 * 4;
        } else if (multiple <= 32) {
            return 1024 * 8;
        } else if (multiple <= 64) {
            return 1024 * 16;
        } else {
            return 1024 * 64;
        }
    }

    public static int compareFileContent(File f1, File f2) {
        final int BUFFER_SIZE = bufferSize(f1.length());

        try {
            BufferedInputStream is1 = new BufferedInputStream(new FileInputStream(f1), BUFFER_SIZE);
            BufferedInputStream is2 = new BufferedInputStream(new FileInputStream(f2), BUFFER_SIZE);

            byte[] b1 = new byte[BUFFER_SIZE];
            byte[] b2 = new byte[BUFFER_SIZE];

            int read1 = -1;
            int read2 = -1;
            int read = -1;

            do {
                read1 = is1.read(b1);
                read2 = is2.read(b2);

                if (read1 < read2) {
                    return -1;
                } else if (read1 > read2) {
                    return 1;
                } else {
                    // read1 is equals to read2
                    read = read1;
                }

                if (read >= 0) {
                    if (read != BUFFER_SIZE) {
                        // clear the buffer not filled from the read
                        Arrays.fill(b1, read, BUFFER_SIZE, (byte) 0);
                        Arrays.fill(b2, read, BUFFER_SIZE, (byte) 0);
                    }
                    // compare the content of the two buffers
                    if (!Arrays.equals(b1, b2)) {
                        return new String(b1).compareTo(new String(b2));
                    }
                }
            } while (read >= 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
