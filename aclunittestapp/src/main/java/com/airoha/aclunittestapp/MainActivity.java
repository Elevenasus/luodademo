package com.airoha.aclunittestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airoha.android.lib.acl.AirohaAclMgr;
import com.airoha.android.lib.acl.OnAclExternalFlashRespListener;
import com.airoha.android.lib.transport.AirohaLink;
import com.airoha.android.lib.transport.connection.OnAirohaConnStateListener;
import com.airoha.android.lib.util.Converter;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Airoha_ACL_UT";

    private AirohaLink mAirohaLink = null;

    private AirohaAclMgr mAirohaAclMgr = null;


    // Connect
    @Bind(R.id.editTextSppAddr)
    protected EditText mEditSppAddr;
    @Bind(R.id.buttonConSpp)
    protected Button mBtnConSpp;
    //    @Bind(R.id.buttonConBle)
//    protected Button mBtnConBle;
    @Bind(R.id.buttonDisConSPP)
    protected Button mBtnDisConSpp;
    @Bind(R.id.textViewConSppResult)
    protected TextView mTextConSppResult;
    @Bind(R.id.textViewConSppState)
    protected TextView mTextConSppState;

    @Bind(R.id.textViewVer)
    protected TextView mTextVer;

    @Bind(R.id.btnUnlockExternalFlash)
    protected Button mBtnUnlockExternalFlash;

    @Bind(R.id.btnErasePageExternalFlash)
    protected Button mBtnErasePageExtraFlash;

    @Bind(R.id.btnWritePageDataExternalFlash)
    protected Button mBtnWritePageDataExternalFlash;

    @Bind(R.id.btnReadPageDataExternalFlash)
    protected Button mBtnReadPageDataExternalFlash;

    @Bind(R.id.btnLockExternalFlash)
    protected Button mBtnLockExternalFlash;

    @Bind(R.id.tvRespUnlockExternalFlash)
    protected TextView mTvRespUnlockExternalFlash;

    @Bind(R.id.tvRespErasePageExternalFlash)
    protected TextView mTvRespErasePageExternalFlash;

    @Bind(R.id.tvRespWritePageDataExternalFlash)
    protected TextView mTvRespWritePageDataExternalFlash;

    @Bind(R.id.tvAddrWritePageDataExternalFlash)
    protected TextView mTvAddrWritePageDataExternalFlash;

    @Bind(R.id.tvRespReadPageDataExternalFlash)
    protected TextView mTvRespReadPageDataExternalFlash;

    @Bind(R.id.tvAddrReadPageDataExternalFlash)
    protected TextView mTvAddrReadPageDataExternalFlash;

    @Bind(R.id.tvDataReadPageDataExternalFlash)
    protected TextView mTvDataReadPageDataExternalFlash;

    @Bind(R.id.tvRespLockExternalFlash)
    protected TextView mTvRespLockExternalFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAirohaLink = new AirohaLink(this);
        mTextVer.setText("SDK Ver:" + mAirohaLink.getSdkVer());
        mAirohaLink.registerOnConnStateListener(TAG, mSppStateListener);

        mAirohaAclMgr = new AirohaAclMgr(mAirohaLink);
        mAirohaAclMgr.registerClientListener(TAG, mAclExternalFlashRespListener);

        initUImember();
    }

    private final OnAirohaConnStateListener mSppStateListener = new OnAirohaConnStateListener() {
        @Override
        public void OnConnected(final String type) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("Conn. :" + type);
                }
            });
        }

        @Override
        public void OnDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "DisConnected", Toast.LENGTH_SHORT).show();
                    mTextConSppState.setText("DisConn.");
                }
            });
        }
    };

    private OnAclExternalFlashRespListener mAclExternalFlashRespListener = new OnAclExternalFlashRespListener() {
        @Override
        public void OnUnlockExternalFlashResp(final byte status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvRespUnlockExternalFlash.setText(String.valueOf(status));
                }
            });
        }

        @Override
        public void OnEraseExternalFlashResp(final byte status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvRespErasePageExternalFlash.setText(String.valueOf(status));
                }
            });
        }

        @Override
        public void OnWritePageExternalFlashResp(final byte status, final int address) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvRespWritePageDataExternalFlash.setText(String.valueOf(status));
                    mTvAddrWritePageDataExternalFlash.setText(String.valueOf(address));
                }
            });
        }

        @Override
        public void OnReadPageExternalFlashResp(final byte status, final int address, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvRespReadPageDataExternalFlash.setText(String.valueOf(status));
                    mTvAddrReadPageDataExternalFlash.setText(String.valueOf(address));
                    mTvDataReadPageDataExternalFlash.setText(Converter.byte2HexStr(data));
                }
            });
        }

        @Override
        public void OnLockPageExternalFlashResp(final byte status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvRespLockExternalFlash.setText(String.valueOf(status));
                }
            });
        }
    };

    void initUImember(){
        mBtnConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btaddr = mEditSppAddr.getText().toString();

                Boolean result = mAirohaLink.connect(btaddr);

                mTextConSppResult.setText(result.toString());
            }
        });

        mBtnDisConSpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaLink.disconnect();
            }
        });

        mBtnUnlockExternalFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaAclMgr.unlockExternalFlash();
            }
        });

        mBtnErasePageExtraFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaAclMgr.eraseExternalFlash(0);
            }
        });

        mBtnWritePageDataExternalFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] pageData = new byte[256];


                mAirohaAclMgr.writePageExternalFlash(0, pageData);
            }
        });

        mBtnReadPageDataExternalFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaAclMgr.readPageExternalFlash(0);
            }
        });

        mBtnLockExternalFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAirohaAclMgr.lockExternalFlash();
            }
        });

    }

}
