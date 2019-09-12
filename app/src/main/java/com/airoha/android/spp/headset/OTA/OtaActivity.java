package com.airoha.android.spp.headset.OTA;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.airoha.android.lib.fieldTest.logger.AirDumpLog;
import com.airoha.android.lib.fieldTest.logger.AirDumpLogForDebug;
import com.airoha.android.lib.ota.AirohaOtaFlowMgr;
import com.airoha.android.lib.ota.logger.AirohaOtaLog;
import com.airoha.android.spp.headset.R;
import com.airoha.android.spp.headset.service.ConnService;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OtaActivity extends Activity {
    public static final String OTA_START = "Ota_Start";
    public static final String OTA_IS_PASS = "Ota_Is_Pass";
    public static final String OTA_RESULT = "Ota_Result";
    public static final String OTA_STATUS = "Ota_Status";
    public static final String OTA_UPDATE_PROGRESSBAR = "Ota_Update_Progress_Bar";
    public static final String OTA_APPLY = "Ota_Apply";
    public static final String OTA_CANCEL = "Ota_Cancel";
    public static final String OTA_ACCEPT = "OTA_ACCEPT";
    public static final String OTA_START_APPLY_UI = "Ota_Start_Apply_Ui";
    public static final String OTA_START_ALERT_UI = "OTA_START_ALERT_UI";
    public static final String OTA_ALERT_INVALID_OEM = "OTA_ALERT_INVALID_OEM";
    public static final String OTA_CURRENT_STAGE = "OTA_CURRENT_STAGE";
    public static final String OTA_SWITCH_CHANNEL = "OTA_SWITCH_CHANNEL";
    public static final String OTA_NOTIFY_MSG = "OTA_NOTIFY_MSG";
    public static final int HTTP_DL_PASS = 0;
    public static final int HTTP_DL_FAIL = 1;
    public static final int HTTP_UPDATE_PROGRESS = 2;
    public static final int LOCAL_DL = 3;
    public static final String EXTRA_BOOT = "EXTRA_BOOT";
    public static final String EXTRA_BIN = "EXTRA_BIN";
    public static final String EXTRA_EXT = "EXTRA_EXT";
    public static final String EXTRA_DSP_PARAM = "EXTRA_DSP_PARAM";
    public static final String EXTRA_IS_LOCAL = "EXTRA_IS_LOCAL";
    public static final String EXTRA_OTA_TYPE = "EXTRA_OTA_TYPE";
    public static final String EXTRA_ACCEPT_ALERT = "EXTRA_ACCEPT_ALERT";
    public static final String EXTRA_OTA_CHANNEL = "EXTRA_OTA_CHANNEL";
    public static final int OTA_TYPE_NORMAL = 0;
    public static final int OTA_TYPE_EXT_DEMOSOUND = 1;
    public static final int OTA_TYPE_LIGHT = 2;
    public static final int OTA_TYPE_RESUME = 4;
    public static final int OTA_CHANNEL_MASTER = 0;
    public static final int OTA_CHANNEL_FOLLOWER = 1;
    private Download_Ota_File mHttpDownLoader;
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private final BroadcastReceiver mOtaUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action.equals(OTA_IS_PASS)) {
                boolean isPass = false;
                isPass = intent.getBooleanExtra(OTA_RESULT, false);
                String status = intent.getStringExtra(OTA_STATUS);
                if (isPass) {
                } else {
                    stopProgressbar(status);
                }
            }

            if (action.equals(OTA_UPDATE_PROGRESSBAR)) {
                int value = 0;
                value = intent.getIntExtra(OTA_RESULT, 0);
                updateProgressbar(value);
            }

            if (action.equals(OTA_START_APPLY_UI)) {
                dismissProgressbar();

                mAlertDialog = new AlertDialog.Builder(OtaActivity.this)
                        .setTitle("Airoha Air Update")
                        .setMessage("Confirm to upgrade?")
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                AirohaOtaLog.LogToFile("-----APPLY START-----" + "\n");
                                Intent intent = new Intent(OTA_APPLY);
                                sendBroadcast(intent);

                                finish();
                            }
                        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                AirohaOtaLog.LogToFile("-----CANCEL START-----" + "\n");
                                Intent intent = new Intent(OTA_CANCEL);
                                sendBroadcast(intent);

                                finish();
                            }
                        }).create();

                mAlertDialog.show();

            }

            if (action.equals(OTA_START_ALERT_UI)) {
                mAlertDialog = new AlertDialog.Builder(OtaActivity.this)
                        .setTitle("Downgrade to older version?")
                        .setMessage("Confirm to proceed")
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(OTA_ACCEPT);
                                intent.putExtra(EXTRA_ACCEPT_ALERT, true);
                                sendBroadcast(intent);
                            }
                        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(OTA_ACCEPT);
                                intent.putExtra(EXTRA_ACCEPT_ALERT, false);
                                sendBroadcast(intent);
                            }
                        }).create();

                if (!isFinishing()) {
                    mAlertDialog.show();
                }
            }

            if (action.equals(OTA_ALERT_INVALID_OEM)) {
                mAlertDialog = new AlertDialog.Builder(OtaActivity.this).create();
                mAlertDialog.setTitle("Invalid OEM");
                mAlertDialog.setMessage("Can't proceed ");
                mAlertDialog.setCancelable(true);

                if (!isFinishing()) {

                    mProgressDialog.dismiss();

                    mAlertDialog.show();
                }
            }

            if(action.equals(OTA_CURRENT_STAGE)){
                String stage = intent.getStringExtra(OTA_STATUS);
                mProgressDialog.setTitle(stage);
            }

            if(action.equals(OTA_NOTIFY_MSG)) {
                String msg = intent.getStringExtra(OTA_STATUS);
                mProgressDialog.setMessage(msg);
            }

            if(action.equals(ConnService.ACTION_SPP_DISCONNECTED)){

                mProgressDialog.setMessage("Got Disconnected. Not Completed");
                SystemClock.sleep(3000);

                if(!isFinishing()){

                    finish();
                }
            }

            if(action.equals(ConnService.ACTION_FOLLOWER_DISCONNECTED)){
                // if current is updating follower should alert, else ignore

                if(mOtaChannel == OTA_CHANNEL_FOLLOWER){
                    mProgressDialog.setMessage("Follower Got Disconnected. Not Completed");
                    SystemClock.sleep(3000);

                    if(!isFinishing()){

                        finish();
                    }
                }
            }

        }
    };
    private CheckBox mChkUseLocalFile;
    private EditText mEditFwBootPath;
    private EditText mEditFwBinPath;
    private EditText mEditExtPath;
    private EditText mEditDspParamPath;
    private FilePickerDialog mBootFilePickerDialog;
    private FilePickerDialog mFwFilePickerDialog;
    private FilePickerDialog mExtFilePickerDialog;
    private FilePickerDialog mDspParamFilePickerDialog;
    private Button mBtnFwBootFilePicker;
    private Button mBtnFwBinFilePicker;
    private Button mBtnExtFilePicker;
    private Button mBtnDspParamFilerPicker;
    private Button mBtnResetAirDumpLog;

    private int mOtaType = OTA_TYPE_NORMAL;
    private int mOtaChannel = OTA_CHANNEL_MASTER;


    private final Handler httpDLHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HTTP_DL_PASS:
                    Log.d("OtaFlow handler", "OTA_DL_PASS");
                    AirohaOtaLog.LogToFile("-----DL FW PASS-----" + "\n");
                    AirohaOtaLog.LogToFile("-----OTA INQUIRY START-----" + "\n");
                    Intent intent = new Intent(OTA_START);
//                    intent.putExtra(EXTRA_BIN, AirohaOtaFlowMgr.getBinFileName());
//                    intent.putExtra(EXTRA_BOOT, AirohaOtaFlowMgr.getBootcodeFileName());
                    intent.putExtra(EXTRA_EXT, AirohaOtaFlowMgr.getExtFileName());
                    intent.putExtra(EXTRA_IS_LOCAL, false);
                    intent.putExtra(EXTRA_OTA_TYPE, mOtaType);
                    intent.putExtra(EXTRA_OTA_CHANNEL, mOtaChannel);
                    sendBroadcast(intent);
                    break;
                case HTTP_DL_FAIL:
                    Log.d("OtaFlow handler", "OTA_DL_FAIL");
                    AirohaOtaLog.LogToFile("-----DL FW FAIL-----" + "\n");
                    mProgressDialog.setTitle("downloading....");
                    mProgressDialog.setMessage("FW DL FAIL, PLEASE CHECK INTERNET");
                    mProgressDialog.setCancelable(true);
                    break;
                case HTTP_UPDATE_PROGRESS:
                    updateProgressbar(1);
                    break;
                case LOCAL_DL:
                    Log.d("OtaFlow handler", "OTA_DL_PASS");
                    AirohaOtaLog.LogToFile("-----DL FW PASS-----" + "\n");
                    AirohaOtaLog.LogToFile("-----OTA INQUIRY START-----" + "\n");
                    Intent i = new Intent(OTA_START);
                    i.putExtra(EXTRA_BIN, mEditFwBinPath.getText().toString().trim());
                    i.putExtra(EXTRA_BOOT, mEditFwBootPath.getText().toString().trim());
                    i.putExtra(EXTRA_EXT, mEditExtPath.getText().toString().trim());
                    i.putExtra(EXTRA_DSP_PARAM, mEditDspParamPath.getText().toString().trim());
                    i.putExtra(EXTRA_IS_LOCAL, true);
                    i.putExtra(EXTRA_OTA_TYPE, mOtaType);
                    i.putExtra(EXTRA_OTA_CHANNEL, mOtaChannel);
                    sendBroadcast(i);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);

        registerIntentFilters();

        mEditFwBootPath = (EditText) findViewById(R.id.editFwBootcodePath);
        mEditFwBinPath = (EditText) findViewById(R.id.editFwBinPath);
        mEditExtPath = (EditText) findViewById(R.id.editDemoSound);
        mEditDspParamPath = (EditText) findViewById(R.id.editDspParam);

        initBootFileDialog();
        initFwFileDialog();
        initExtFileDialog();
        initDspParamFileDialog();

        Button btnUpdateFw = (Button) this.findViewById(R.id.btnUpdateFw);
        btnUpdateFw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOtaType = OTA_TYPE_NORMAL;
                setProgressView();
                onOtaClick();
            }
        });

        Button btnResumeUpdateFw = (Button) findViewById(R.id.btnResumeUpdateFw);
        btnResumeUpdateFw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOtaType = OTA_TYPE_RESUME;
                setProgressView();
                onOtaClick();
            }
        });

        Button btnUpdateDemoSound = (Button) this.findViewById(R.id.btnUpdateDemoSound);
        btnUpdateDemoSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOtaType = OTA_TYPE_EXT_DEMOSOUND;
                setProgressView(100);
                onOtaClick();
            }
        });

        Button btnUpdateDspParam = (Button) findViewById(R.id.btnUpdateDspParam);
        btnUpdateDspParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOtaType = OTA_TYPE_LIGHT;
                setProgressView(3);
                onOtaClick();
            }
        });

        mChkUseLocalFile = (CheckBox) findViewById(R.id.chkLocalPath);
        mChkUseLocalFile.setChecked(false); // set to for Local
        mChkUseLocalFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBtnFwBootFilePicker.setVisibility(View.VISIBLE);
                    mBtnFwBinFilePicker.setVisibility(View.VISIBLE);
                    mBtnExtFilePicker.setVisibility(View.VISIBLE);
                    mBtnDspParamFilerPicker.setVisibility(View.VISIBLE);
                } else {
                    mBtnFwBootFilePicker.setVisibility(View.GONE);
                    mBtnFwBinFilePicker.setVisibility(View.GONE);
                    mBtnExtFilePicker.setVisibility(View.GONE);
                    mBtnDspParamFilerPicker.setVisibility(View.GONE);
                }
            }

        });

        CheckBox chkSwitchToUpdateFollower = (CheckBox) findViewById(R.id.chkSwitchChannel);
        chkSwitchToUpdateFollower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mOtaChannel = OTA_CHANNEL_FOLLOWER;
                }else {
                    mOtaChannel = OTA_CHANNEL_MASTER;
                }
            }
        });


        mBtnFwBootFilePicker = (Button) this.findViewById(R.id.btnBootFilePicker);
        mBtnFwBootFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBootFilePickerDialog.show();
            }
        });

        mBtnFwBinFilePicker = (Button) this.findViewById(R.id.btnFwFilePicker);
        mBtnFwBinFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFwFilePickerDialog.show();
            }
        });


        mBtnExtFilePicker = (Button) findViewById(R.id.btnExtFilePicker);
        mBtnExtFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtFilePickerDialog.show();
            }
        });

        mBtnDspParamFilerPicker = (Button) findViewById(R.id.btnDspParamFilePicker);
        mBtnDspParamFilerPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDspParamFilePickerDialog.show();
            }
        });

        mBtnResetAirDumpLog = (Button) findViewById(R.id.btnResetAirDumpLog);
        mBtnResetAirDumpLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AirDumpLog.clearLogFile();
//                AirDumpLogForDebug.clearLogFile();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

                AirDumpLog.setLogFileNameWithTimeStamp(timeStamp);
                AirDumpLogForDebug.setLogFileNameWithTimeStamp(timeStamp);
            }
        });
    }

    private void onOtaClick() {
        // delete log file
        File file = new File("/sdcard/AirohaOta.log");
        file.delete();

        AirohaOtaLog.LogToFile("*************************************\n");
        AirohaOtaLog.LogToFile("OTA start\n");
        if (mChkUseLocalFile.isChecked()) // update from local file
        {
            httpDLHandler.obtainMessage(OtaActivity.LOCAL_DL).sendToTarget();
        } else // update form server
        {
            AirohaOtaLog.LogToFile("-----DOWNLOAD FW FROM SERVER START-----" + "\n");
            mHttpDownLoader = new Download_Ota_File(httpDLHandler, OtaActivity.this);
            mHttpDownLoader.Download(mEditFwBootPath.getText().toString().trim(), mEditFwBinPath.getText().toString().trim());
        }
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(mOtaUIReceiver);

        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }


        super.onDestroy();
    }

    private void registerIntentFilters() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OTA_IS_PASS);
        intentFilter.addAction(OTA_UPDATE_PROGRESSBAR);
        intentFilter.addAction(OTA_START_APPLY_UI);
        intentFilter.addAction(OTA_START_ALERT_UI);
        intentFilter.addAction(OTA_ALERT_INVALID_OEM);
        intentFilter.addAction(OTA_CURRENT_STAGE);
        intentFilter.addAction(OTA_NOTIFY_MSG);

        // 2017.11.28 Daniel: handle OtaActivity should also listen to ConnService

        intentFilter.addAction(ConnService.ACTION_SPP_DISCONNECTED);

        intentFilter.addAction(ConnService.ACTION_FOLLOWER_DISCONNECTED);

        registerReceiver(mOtaUIReceiver, intentFilter);
    }

    private void setProgressView(int maxProgress) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("downloading....");
        mProgressDialog.setMessage("Please wait ..");
        mProgressDialog.setMax(maxProgress);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(0);

        if(!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void setProgressView() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("downloading....");
        mProgressDialog.setMessage("Please wait ..");
//        mProgressDialog.setMax(maxProgress);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mProgressDialog.setProgress(0);

        if(!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void initBootFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "editBootcode"};
        mBootFilePickerDialog = new FilePickerDialog(this, properties);
        mBootFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {
                    mEditFwBootPath.setText(files[0].toString());
                }
            }
        });
    }

    private void initFwFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"bin", "BIN", "ext"};
        mFwFilePickerDialog = new FilePickerDialog(this, properties);
        mFwFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {
                    mEditFwBinPath.setText(files[0].toString());
                }
            }
        });
    }

    private void initExtFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"ext"};
        mExtFilePickerDialog = new FilePickerDialog(this, properties);
        mExtFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {
                    mEditExtPath.setText(files[0].toString());
                }
            }
        });
    }

    private void initDspParamFileDialog(){
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"dsp"};
        mDspParamFilePickerDialog = new FilePickerDialog(this, properties);
        mDspParamFilePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files != null && files.length > 0) {
                    mEditDspParamPath.setText(files[0].toString());
                }
            }
        });
    }

    private void updateProgressbar(int value) {
        mProgressDialog.incrementProgressBy(value);
    }

    private void stopProgressbar(String msg) {
        mProgressDialog.setTitle("downloading....");
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
    }

    private void dismissProgressbar() {
        mProgressDialog.dismiss();
    }
}
