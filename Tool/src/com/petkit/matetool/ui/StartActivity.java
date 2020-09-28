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

import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.service.DatagramProcessService;
import com.petkit.matetool.ui.D3.D3TestPrepareActivity;
import com.petkit.matetool.ui.K2.K2TestPrepareActivity;
import com.petkit.matetool.ui.aq.AQTestMainActivity;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.cozy.CozyTestPrepareActivity;
import com.petkit.matetool.ui.feeder.FeederTestPrepareActivity;
import com.petkit.matetool.ui.feederMini.FeederMiniTestPrepareActivity;
import com.petkit.matetool.ui.go.GoTestMainActivity;
import com.petkit.matetool.ui.mate.SelectActivity;
import com.petkit.matetool.ui.permission.PermissionDialogActivity;
import com.petkit.matetool.ui.permission.mode.PermissionBean;
import com.petkit.matetool.ui.t3.T3TestPrepareActivity;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Versions.TOOL_AQ_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_COZY;
import static com.petkit.matetool.utils.Versions.TOOL_D3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_FEEDER_MINI_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_FEEDER_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_GO_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_K2_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_MATE_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_T3_VERSION;

/**
 * 测试工具入口，设置测试设备类型和工位
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
            LogcatStorageHelper.getInstance(this, "", "").start();
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
    protected void onDestroy() {
        super.onDestroy();

        PrintUtils.quit();
    }

    @Override
    protected void setupViews() {
        setTitle(getString(R.string.app_name) + " v" + CommonUtils.getAppVersionName(this));

        imb_titleleft.setVisibility(View.GONE);
        fixtureNumberEditText = (EditText) findViewById(R.id.fixture_number_edittxt);
        findViewById(R.id.start_test).setOnClickListener(this);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.mate_test_style);
        ((RadioButton)radioGroup.findViewById(R.id.mate_style)).setChecked(true);
        testStyle = Globals.MATE_STYLE;

        setDeviceToolVersion();

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_test:
                if(!checkPermissions()) {
                    return;
                }

                Bundle bundle;
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
                        startActivity(FeederTestPrepareActivity.class);
                        break;
                    case Globals.COZY:
                        startActivity(CozyTestPrepareActivity.class);
                        break;
                    case Globals.FEEDER_MINI:
                        startActivity(FeederMiniTestPrepareActivity.class);
                        break;
                    case Globals.GO:
                        startActivity(GoTestMainActivity.class);
                        break;
                    case Globals.T3:
                        startActivity(T3TestPrepareActivity.class);
                        break;
                    case Globals.K2:
                        startActivity(K2TestPrepareActivity.class);
                        break;
                    case Globals.AQ:
                        startActivity(AQTestMainActivity.class);
                        break;
                    case Globals.D3:
                        startActivity(D3TestPrepareActivity.class);
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
            case R.id.go:
                testStyle = Globals.GO;
                break;
            case R.id.feeder:
                testStyle = Globals.FEEDER;
                break;
            case R.id.cozy:
                testStyle = Globals.COZY;
                break;
            case R.id.feeder_mini:
                testStyle = Globals.FEEDER_MINI;
                break;
            case R.id.toilet:
                testStyle = Globals.T3;
                break;
            case R.id.k2:
                testStyle = Globals.K2;
                break;
            case R.id.d3:
                testStyle = Globals.D3;
                break;
            case R.id.aq:
                testStyle = Globals.AQ;
                break;
            default:
                break;
        }
    }

    private void setDeviceToolVersion() {
        RadioButton tempRadioButton;

        tempRadioButton = (RadioButton) findViewById(R.id.mate_style);
        tempRadioButton.setText("Mate Style" + " v" + TOOL_MATE_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.mate_pro);
        tempRadioButton.setText("Mate Pro" + " v" + TOOL_MATE_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.go);
        tempRadioButton.setText("Go抽检" + " v" + TOOL_GO_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.feeder);
        tempRadioButton.setText("喂食器（D1）" + " v" + TOOL_FEEDER_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.cozy);
        tempRadioButton.setText("宠物窝new（Z1s）" + " v" + TOOL_COZY);

        tempRadioButton = (RadioButton) findViewById(R.id.feeder_mini);
        tempRadioButton.setText("喂食器Mini（D2）" + " v" + TOOL_FEEDER_MINI_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.k2);
        tempRadioButton.setText("净味器（K2）" + " v" + TOOL_K2_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.toilet);
        tempRadioButton.setText("自动猫厕所（T3）" + " v" + TOOL_T3_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.aq);
        tempRadioButton.setText("智能鱼缸（AQ）" + " v" + TOOL_AQ_VERSION);

        tempRadioButton = (RadioButton) findViewById(R.id.d3);
        tempRadioButton.setText("行星喂食器（D3）" + " v" + TOOL_D3_VERSION);
    }

    private boolean checkPermissions() {
        if(!Globals.checkPermission(this, Manifest.permission.ACCESS_WIFI_STATE) ||
                !Globals.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                !Globals.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Bundle bundle = new Bundle();
            ArrayList<PermissionBean> permissionBeens = new ArrayList<>();
            permissionBeens.add(new PermissionBean(Manifest.permission.ACCESS_WIFI_STATE, R.string.Permission_phone_state, R.drawable.permission_read_phone));
            permissionBeens.add(new PermissionBean(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.Permission_location, R.drawable.permission_location));
            permissionBeens.add(new PermissionBean(Manifest.permission.ACCESS_FINE_LOCATION, R.string.Permission_location, R.drawable.permission_location));

            bundle.putSerializable(Globals.EXTRA_PERMISSION_CONTENT, permissionBeens);
            startActivityWithData(PermissionDialogActivity.class, bundle, false);
            return false;
        }

        return true;
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
                        LogcatStorageHelper.getInstance(StartActivity.this, "", "").start();
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
