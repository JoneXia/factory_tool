package com.petkit.matetool.ui.aq;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;

public class AQColorActivity extends Activity implements View.OnClickListener {

    private DeviceInfo mCurDevice;

    private EditText rEdit, gEdit, bEdit;
    private TextView tTextView;
    private SeekBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mCurDevice = (DeviceInfo) savedInstanceState.getSerializable(AQUtils.EXTRA_AQ);
        } else {
            mCurDevice = (DeviceInfo) getIntent().getSerializableExtra(AQUtils.EXTRA_AQ);
        }
        setContentView(R.layout.activity_aq_color);

        findViewById(R.id.color_picker).setOnClickListener(this);
        findViewById(R.id.color_input).setOnClickListener(this);

        rEdit = (EditText) findViewById(R.id.color_r);
        gEdit = (EditText) findViewById(R.id.color_g);
        bEdit = (EditText) findViewById(R.id.color_b);
        tTextView = (TextView) findViewById(R.id.color_a);
        mBar = (SeekBar) findViewById(R.id.color_tp);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AQUtils.EXTRA_AQ, mCurDevice);

    }

//    @Override
//    protected void setupViews() {
//        setTitle("智能鱼缸灯光测试");
//
//        findViewById(R.id.color_picker).setOnClickListener(this);
//        findViewById(R.id.color_input).setOnClickListener(this);
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        unregisterBroadcastReceiver();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_picker:
                break;
            case R.id.color_input:
                writeColor();
                break;
        }
    }

    private void writeColor() {

        int r, g, b, t;
        t = mBar.getProgress();
        try {
            r = Integer.valueOf(rEdit.getEditableText().toString());
            g = Integer.valueOf(gEdit.getEditableText().toString());
            b = Integer.valueOf(bEdit.getEditableText().toString());

            int color = Color.argb(t, r, g, b);
        } catch (NumberFormatException e) {

        }


    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                PetkitLog.d("" + arg1.getAction());

                switch (arg1.getAction()) {
                    case BLEConsts.BROADCAST_PROGRESS:
                        int progress = arg1.getIntExtra(BLEConsts.EXTRA_PROGRESS, 0);

                        switch (progress) {
                            case BLEConsts.ERROR_DEVICE_DISCONNECTED:
                            case BLEConsts.PROGRESS_DISCONNECTING:
                                CommonUtils.showShortToast(AQColorActivity.this, "连接已断开");
                                finish();
                                break;
                            default:
                                break;
                        }
                        break;

                    case BLEConsts.BROADCAST_ERROR:
                        progress = arg1.getIntExtra(BLEConsts.EXTRA_DATA, 0);

                        switch (progress) {
                            case BLEConsts.ERROR_ABORTED:
                            case BLEConsts.ERROR_INVALID_PARAMETERS:
                            case BLEConsts.ERROR_INVALID_RESPONSE:
                            case BLEConsts.ERROR_SYNC_TIMEOUT:
                            default:
                                CommonUtils.showShortToast(AQColorActivity.this, "连接已断开");
                                finish();
                                break;
                        }
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEConsts.BROADCAST_PROGRESS);
        filter.addAction(BLEConsts.BROADCAST_ERROR);
        filter.addAction(BLEConsts.BROADCAST_LOG);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


}
