package com.airoha.android.spp.headset.OTA;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Evonne.Hsieh on 2016/6/16.
 */
public class Download_Ota_File {

    private final String TAG = "DL file";
    private boolean isOk = true;
    Handler _handler;

    private Context mCtx;

    Download_Ota_File(Handler handler, Context ctx)
    {
        _handler = handler;
        mCtx = ctx;
    }

    public void Download(String bootUrl, String fwUrl)
    {
        URL bootURL = null;
        URL updateURL = null;
        URL extURL = null;

        AirohaOtaLog.LogToFile("Boot path: "+bootUrl+"\n");
        AirohaOtaLog.LogToFile("FW path: "+fwUrl+"\n");
        try {
            bootURL = new URL(bootUrl);
            updateURL = new URL(fwUrl);
        } catch (Exception x) {
            x.printStackTrace();
            AirohaOtaLog.LogToFile("Open url fail\n");
            AirohaOtaLog.LogToFile("Exception: "+x+"\n");
            isOk = false;
            _handler.obtainMessage(OtaActivity.HTTP_DL_FAIL).sendToTarget();
        }

        new DownloadFilesTask().execute(bootURL, updateURL);
    }

    private void DownloadFile(URL url, File file)
    {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(response);
            fos.close();
        }  catch (Exception x) {
            x.printStackTrace();
            AirohaOtaLog.LogToFile("DownloadFile fail\n");
            AirohaOtaLog.LogToFile("Exception: "+x+"\n");
            isOk = false;
            _handler.obtainMessage(OtaActivity.HTTP_DL_FAIL).sendToTarget();
        }

    }

    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            File boot = new File(mCtx.getFilesDir(), AirohaOtaFlowMgr.DEFAULT_BOOTCODE_FILE);
            File update = new File(mCtx.getFilesDir(), AirohaOtaFlowMgr.DEFAULT_BIN_FILE);
            File ext = new File(mCtx.getFilesDir(), AirohaOtaFlowMgr.getExtFileName());
            ArrayList<File> fileLst = new ArrayList<File>();
            fileLst.add(boot);
            fileLst.add(update);
            fileLst.add(ext);
            int count = urls.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                //totalSize += Downloader.downloadFile(urls[i]);
               // publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
               // if (isCancelled()) break;
                DownloadFile(urls[i], fileLst.get(i));
                _handler.obtainMessage(OtaActivity.HTTP_UPDATE_PROGRESS).sendToTarget();
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            // showDialog("Downloaded " + result + " bytes");
            if(isOk) _handler.obtainMessage(OtaActivity.HTTP_DL_PASS).sendToTarget();
        }
    }
}
