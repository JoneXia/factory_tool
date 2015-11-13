package com.petkit.matetool.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.service.DatagramProcessService;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

/**
 * 测试工具入口，设置测试mate类型和工位
 *
 * Created by Jone on 2015/10/23.
 */
public class StartActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private BroadcastReceiver mBroadcastReceiver;

    private EditText fixtureNumberEditText;
    private int testStyle = 0;
    private int workStation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        LogcatStorageHelper.getInstance(this, "http://www.baidu.com").start();

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerBoradcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterBoradcastReceiver();
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.activity_name);

        imb_titleleft.setVisibility(View.GONE);
        fixtureNumberEditText = (EditText) findViewById(R.id.fixture_number_edittxt);
        findViewById(R.id.start_test).setOnClickListener(this);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.mate_test_style);
        ((RadioButton)radioGroup.findViewById(R.id.mate_style)).setChecked(true);
        testStyle = Globals.MATE_STYLE;

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_test:
                String workStationString = fixtureNumberEditText.getText().toString().trim();
                if(isEmpty(workStationString)){
                    showShortToast(R.string.input_error);
                    return;
                }
                workStation = Integer.valueOf(workStationString);

                if (workStation <= 0 || workStation >= 10) {
                    showShortToast(R.string.input_error);
                    return;
                }

                if (parseTestStyle()) {
                    collapseSoftInputMethod(fixtureNumberEditText);
                    LoadDialog.show(this);

                    final Intent service = new Intent(this, DatagramProcessService.class);
                    service.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                    startService(service);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.mate_style:
                testStyle = Globals.MATE_STYLE;
                break;
            case R.id.mate_pro:
                testStyle = Globals.MATE_PRO;
                break;
            default:
                break;
        }
    }

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(isFinishing()) {
                    return;
                }

                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);

                    LoadDialog.dismissDialog();
                    switch (progress){
                        case DatagramConsts.DATAGRAM_START:
                            Bundle bundle = new Bundle();
                            bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
                            bundle.putInt(DatagramConsts.EXTRA_MATE_STYLE, testStyle);
                            startActivityWithData(SelectActivity.class, bundle, false);
                            break;
                        case DatagramConsts.DATAGRAM_DESTROY:
                            showShortToast(R.string.Test_canceled);
                            break;
                    }
                } else if(arg1.getAction().equals("reset")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!Utils.mIsReged) {
                                Utils.mIsReging =  false;
                            }
                        }
                    }, 5000);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBoradcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private boolean parseTestStyle() {
        return testStyle == Globals.MATE_PRO
                || testStyle == Globals.MATE_STYLE;
    }



}
