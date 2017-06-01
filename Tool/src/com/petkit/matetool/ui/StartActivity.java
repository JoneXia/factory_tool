package com.petkit.matetool.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
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
import com.petkit.matetool.ui.feeder.FeederTestPrepareActivity;
import com.petkit.matetool.ui.mate.SelectActivity;
import com.petkit.matetool.ui.permission.PermissionDialogActivity;
import com.petkit.matetool.ui.catlitter.CatLitterMainActivity;
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
        if(!checkSelfPermissionComplete(this)) {
            startActivity(PermissionDialogActivity.class);
        } else {
            LogcatStorageHelper.getInstance(this, "http://www.baidu.com").start();
        }

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
        setTitle(R.string.app_name);

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

                switch (testStyle) {
                    case Globals.MATE_PRO:
                    case Globals.MATE_STYLE:
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
                        LoadDialog.show(this);

                        final Intent service = new Intent(this, DatagramProcessService.class);
                        service.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                        startService(service);
                        break;
                    case Globals.FEEDER:
                        Bundle bundle = new Bundle();
                        bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
                        startActivityWithData(FeederTestPrepareActivity.class, bundle, false);
                        break;
                    case Globals.CAT_LITTER:
                        bundle = new Bundle();
                        bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
                        startActivityWithData(CatLitterMainActivity.class, bundle, false);
                        break;
                }
                collapseSoftInputMethod(fixtureNumberEditText);
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
            case R.id.feeder:
                testStyle = Globals.FEEDER;
                break;
            case R.id.cat_litter:
                testStyle = Globals.CAT_LITTER;
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
                } else if(arg1.getAction().equals(Globals.BROADCAST_PERMISSION_FINISHED)) {
                    boolean result = arg1.getBooleanExtra("result", false);
                    if(result){
                        LogcatStorageHelper.getInstance(StartActivity.this, "http://www.baidu.com").start();
                    } else {
                        finish();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        filter.addAction(Globals.BROADCAST_PERMISSION_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBoradcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private boolean checkSelfPermissionComplete(Context context){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED);
    }

}
