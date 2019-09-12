package com.airoha.android.lib.peq.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Evonne.Hsieh on 2016/6/7.
 */
public class AirohaPeqLog {
    private static AirohaPeqLog ourInstance;
    private static FileOutputStream fos;
    private static File file;

    private static boolean DBG = true;

    public static void LogToFile(String log)
    {
        if(!DBG)
            return;

        try {
            file = new File("/sdcard/AirohaPeq.txt");
            fos = new FileOutputStream(file, true);
            if (!file.exists()) {
                file.createNewFile();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            fos.write((timeStamp + ": ").getBytes());
            fos.write((log).getBytes());
            fos.write(("\n").getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
