package com.petkit.matetool.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.http.ApiTools;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.service.DatagramProcessService;
import com.petkit.matetool.ui.D3.D3TestPrepareActivity;
import com.petkit.matetool.ui.D4.D4TestPrepareActivity;
import com.petkit.matetool.ui.K2.K2TestPrepareActivity;
import com.petkit.matetool.ui.W5.W5TestPrepareActivity;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.aq.AQTestMainActivity;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.TestPrepareActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
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
import com.petkit.matetool.utils.SpannableStringUtils;
import com.petkit.matetool.utils.TesterManagerUtils;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Versions.TOOL_AQ1S_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_AQH1_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_AQR_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_AQ_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_COZY;
import static com.petkit.matetool.utils.Versions.TOOL_CTW2_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_CTW3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_D3_1_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_D3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_D4S_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_D4_1_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_D4_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_FEEDER_MINI_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_FEEDER_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_GO_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_HG_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_K2_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_K3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_MATE_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_P3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_R2_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_T3_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_T4_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_W5N_VERSION;
import static com.petkit.matetool.utils.Versions.TOOL_W5_VERSION;

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

        DeviceCommonUtils.initDeviceConfig();
        TesterManagerUtils.initTesterTempList();

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

        setDeviceToolVersion();
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
        setTitle(getString(R.string.app_name) + " v" + CommonUtils.getAppVersionName(this)
                        + (ApiTools.MODEL.equalsIgnoreCase("TEST") ? " debug" : ""));

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
                    case Globals.D3_1:
                        bundle = new Bundle();
                        bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, testStyle);
                        startActivityWithData(D3TestPrepareActivity.class, bundle, false);
                        break;
                    case Globals.D4:
                    case Globals.D4_1:
                        bundle = new Bundle();
                        bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, testStyle);
                        startActivityWithData(D4TestPrepareActivity.class, bundle, false);
                        break;
                    case Globals.W5:
                    case Globals.W5C:
                        bundle = new Bundle();
                        bundle.putInt(W5Utils.EXTRA_W5_TYPE, testStyle == Globals.W5C ? W5Utils.W5_TYPE_MINI : W5Utils.W5_TYPE_NORMAL);
                        startActivityWithData(W5TestPrepareActivity.class, bundle, false);
                        break;
                    default:
                        if (testStyle == Globals.AQH1_500 || testStyle == Globals.AQH1_1000) {
                            showSelectRegion();
                            return;
                        }

                        bundle = new Bundle();
                        bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, testStyle);
                        startActivityWithData(TestPrepareActivity.class, bundle, false);
                        break;
                }
                collapseSoftInputMethod(fixtureNumberEditText);
                break;
        }
    }

    private void showSelectRegion() {

        final String[] items = { "中国", "海外"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle("请选择使用地：");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) {
                    switch (testStyle) {
                        case Globals.AQH1_500:
                            testStyle = Globals.AQH1_500_A;
                            break;
                        case Globals.AQH1_1000:
                            testStyle = Globals.AQH1_1000_A;
                            break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, testStyle);
                startActivityWithData(TestPrepareActivity.class, bundle, false);
            }
        });
        listDialog.show();

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
            case R.id.d4:
                testStyle = Globals.D4;
                break;
            case R.id.aq:
                testStyle = Globals.AQ;
                break;
            case R.id.w5:
                testStyle = Globals.W5;
                break;
            case R.id.w5c:
                testStyle = Globals.W5C;
                break;
            case R.id.p3c:
                testStyle = Globals.P3C;
                break;
            case R.id.p3d:
                testStyle = Globals.P3D;
                break;
            case R.id.t4:
                testStyle = Globals.T4;
                break;
            case R.id.t4_p:
                testStyle = Globals.T4_p;
                break;
            case R.id.k3:
                testStyle = Globals.K3;
                break;
            case R.id.aqr:
                testStyle = Globals.AQR;
                break;
            case R.id.aq1s:
                testStyle = Globals.AQ1S;
                break;
            case R.id.r2:
                testStyle = Globals.R2;
                break;
            case R.id.w5n:
                testStyle = Globals.W5N;
                break;
            case R.id.w4x:
                testStyle = Globals.W4X;
                break;
            case R.id.aqh1_500:
                testStyle = Globals.AQH1_500;
                break;
            case R.id.aqh1_1000:
                testStyle = Globals.AQH1_1000;
                break;
            case R.id.ctw2:
                testStyle = Globals.CTW2;
                break;
            case R.id.d3_1:
                testStyle = Globals.D3_1;
                break;
            case R.id.d4_1:
                testStyle = Globals.D4_1;
                break;
            case R.id.d4s:
                testStyle = Globals.D4S;
                break;
            case R.id.hg:
                testStyle = Globals.HG;
                break;
            case R.id.hg_100v:
                testStyle = Globals.HG_110V;
                break;
            case R.id.ctw3:
                testStyle = Globals.CTW3;
                break;
            //TODO: 新增设备需对应添加
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
        tempRadioButton.setText(getTextDetail(Globals.GO, "Go抽检" + " v" + TOOL_GO_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.feeder);
        tempRadioButton.setText(getTextDetail(Globals.FEEDER, "喂食器（D1）" + " v" + TOOL_FEEDER_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.cozy);
        tempRadioButton.setText(getTextDetail(Globals.COZY, "宠物窝new（Z1s）" + " v" + TOOL_COZY));

        tempRadioButton = (RadioButton) findViewById(R.id.feeder_mini);
        tempRadioButton.setText(getTextDetail(Globals.FEEDER_MINI, "喂食器Mini（D2）" + " v" + TOOL_FEEDER_MINI_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.k2);
        tempRadioButton.setText(getTextDetail(Globals.K2, "净味器（K2）" + " v" + TOOL_K2_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.toilet);
        tempRadioButton.setText(getTextDetail(Globals.T3, "自动猫厕所（T3）" + " v" + TOOL_T3_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.aq);
        tempRadioButton.setText(getTextDetail(Globals.AQ, "智能鱼缸（AQ）" + " v" + TOOL_AQ_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.d3);
        tempRadioButton.setText(getTextDetail(Globals.D3, "行星喂食器（D3）" + " v" + TOOL_D3_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.d4);
        tempRadioButton.setText(getTextDetail(Globals.D4, "喂食器SOLO（D4）" + " v" + TOOL_D4_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.w5);
        tempRadioButton.setText(getTextDetail(Globals.W5, "智能饮水机（W5）" + " v" + TOOL_W5_VERSION));
        tempRadioButton.setVisibility(View.GONE);   //W5项目暂停，修改了整个方案，和W5C不一致了。

        tempRadioButton = (RadioButton) findViewById(R.id.w5c);
        tempRadioButton.setText(getTextDetail(Globals.W5C, "智能饮水机MINI（W5C）" + " v" + TOOL_W5_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.t4);
        tempRadioButton.setText(getTextDetail(Globals.T4, "智能猫厕所MAX（T4）" + " v" + TOOL_T4_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.t4_p);
        tempRadioButton.setText(getTextDetail(Globals.T4_p, "智能猫厕所MAX（T4标配K3）" + " v" + TOOL_T4_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.p3c);
        tempRadioButton.setText(getTextDetail(Globals.P3C, "智能猫牌（P3C）" + " v" + TOOL_P3_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.p3d);
        tempRadioButton.setText(getTextDetail(Globals.P3D, "智能狗牌（P3D）" + " v" + TOOL_P3_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.k3);
        tempRadioButton.setText(getTextDetail(Globals.K3, "智能净味器（K3）" + " v" + TOOL_K3_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.aqr);
        tempRadioButton.setText(getTextDetail(Globals.AQR, "智能鱼缸（AQR）" + " v" + TOOL_AQR_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.aq1s);
        tempRadioButton.setText(getTextDetail(Globals.AQ1S, "智能鱼缸（AQ1S）" + " v" + TOOL_AQ1S_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.r2);
        tempRadioButton.setText(getTextDetail(Globals.R2, "智能加热棒（R2）" + " v" + TOOL_R2_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.w5n);
        tempRadioButton.setText(getTextDetail(Globals.W5N, "无线智能饮水机-陶瓷（W5）" + " v" + TOOL_W5N_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.w4x);
        tempRadioButton.setText(getTextDetail(Globals.W4X, "无线智能饮水机-不锈钢（W4X）" + " v" + TOOL_W5N_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.aqh1_500);
        tempRadioButton.setText(getTextDetail(Globals.AQH1_500, "鱼缸加热棒-500W（AQ-H1）" + " v" + TOOL_AQH1_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.aqh1_1000);
        tempRadioButton.setText(getTextDetail(Globals.AQH1_1000, "鱼缸加热棒-1000W（AQ-H1）" + " v" + TOOL_AQH1_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.ctw2);
        tempRadioButton.setText(getTextDetail(Globals.CTW2, "智能饮水机SOLO（CTW2）" + " v" + TOOL_CTW2_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.d4_1);
        tempRadioButton.setText(getTextDetail(Globals.D4_1, "喂食器SOLO NEW（D4-1）" + " v" + TOOL_D4_1_VERSION));
        tempRadioButton.setVisibility(View.GONE);

        tempRadioButton = (RadioButton) findViewById(R.id.d3_1);
        tempRadioButton.setText(getTextDetail(Globals.D3_1, "行星喂食器NEW（D3-1）" + " v" + TOOL_D3_1_VERSION));
        tempRadioButton.setVisibility(View.GONE);

        tempRadioButton = (RadioButton) findViewById(R.id.d4s);
        tempRadioButton.setText(getTextDetail(Globals.D4S, "双子星喂食器（D4S）" + " v" + TOOL_D4S_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.hg);
        tempRadioButton.setText(getTextDetail(Globals.HG, "烘干箱220V（HG）" + " v" + TOOL_HG_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.hg_100v);
        tempRadioButton.setText(getTextDetail(Globals.HG_110V, "烘干箱110V（HG）" + " v" + TOOL_HG_VERSION));

        tempRadioButton = (RadioButton) findViewById(R.id.ctw3);
        tempRadioButton.setText(getTextDetail(Globals.CTW3, "饮水机PRO（CTW3）" + " v" + TOOL_CTW3_VERSION));

        //TODO: 新增设备需对应添加
    }

    private SpannableStringBuilder getTextDetail(int type, String text) {
        SpannableStringUtils.SpanText text1 = new SpannableStringUtils.SpanText(text, CommonUtils.getColorById(R.color.black), 1.0f);

        if (TesterManagerUtils.getCurrentTesterForType(type) != null) {
            SpannableStringUtils.SpanText text2 = new SpannableStringUtils.SpanText(" 已登录", CommonUtils.getColorById(R.color.yellow), 0.8f);
            return SpannableStringUtils.makeSpannableString(text1, text2);
        } else {
            return SpannableStringUtils.makeSpannableString(text1);
        }
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
