package com.petkit.matetool.ui.K2;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.service.AndroidBLEActionService;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DeviceModuleStateStruct;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.mode.K2TestUnit;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.DateUtil;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.petkit.matetool.ui.K2.utils.K2Utils.DC_RANGE;
import static com.petkit.matetool.ui.K2.utils.K2Utils.K2TestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.K2.utils.K2Utils.K2TestModes.TEST_MODE_AUTO;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;
import static com.petkit.matetool.utils.Globals.TYPE_TEST;

/**
 * Created by Jone on 17/4/24.
 */
public class K2TestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<K2TestUnit> mK2TestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String bleMac;
    private String mAgeingResult = "null";

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<K2TestUnit> mK2AutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private boolean isNewSN = false;
    private int mTestType;

    //正在写入SN时，过滤重复的点击
    private boolean isWritingSN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mK2TestUnits = (ArrayList<K2TestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(K2Utils.EXTRA_K2);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(K2Utils.EXTRA_K2_TESTER);
            mTestType = savedInstanceState.getInt("TestType");
            mErrorDevice = (Device) savedInstanceState.getSerializable(K2Utils.EXTRA_ERROR_K2);
        } else {
            mK2TestUnits = (ArrayList<K2TestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mDevice = (Device) getIntent().getSerializableExtra(K2Utils.EXTRA_K2);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(K2Utils.EXTRA_K2_TESTER);
            mTestType = getIntent().getIntExtra("TestType", TYPE_TEST);
            mErrorDevice = (Device) getIntent().getSerializableExtra(K2Utils.EXTRA_ERROR_K2);
        }

        setContentView(R.layout.activity_feeder_test_detail);

        registerBoradcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CurrentTestStep", mCurTestStep);
        outState.putSerializable("TestUnits", mK2TestUnits);
        outState.putSerializable(K2Utils.EXTRA_K2, mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
        outState.putInt("TestType", mTestType);
        outState.putSerializable(K2Utils.EXTRA_ERROR_K2, mErrorDevice);
    }

    @Override
    protected void setupViews() {
        findViewById(R.id.test_btn_1).setOnClickListener(this);
        findViewById(R.id.test_btn_2).setOnClickListener(this);
        findViewById(R.id.test_btn_3).setOnClickListener(this);

        mDescTextView = (TextView) findViewById(R.id.test_detail);
        mPromptTextView = (TextView) findViewById(R.id.test_prompt);
        mBtn1 = (Button) findViewById(R.id.test_btn_1);
        mBtn2 = (Button) findViewById(R.id.test_btn_2);
        mBtn3 = (Button) findViewById(R.id.test_btn_3);
        mDescScrollView = (ScrollView) findViewById(R.id.test_scrllview);

        refreshView();
    }

    private void refreshView() {
        setTitle(mK2TestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mK2TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mDevice.getSn())) {
                    if (mErrorDevice != null && !mDevice.getSn().equals(mErrorDevice.getSn())) {
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                } else {
                    mDescTextView.setText("mac:" + mDevice.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("请先校准，注意保持按键表面清洁，无干扰，再分别测试功能键和童锁键！");
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试数码管和蜂鸣器，观察是否正常！");
                break;
            case TEST_MODE_LED_2:
                mPromptTextView.setText("测试LED灯，观察是否正常！");
                break;
            case TEST_MODE_HOLZER:
                mPromptTextView.setText("需分别测试正常和缺液状态！");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("测试温湿度传感器，观察是否正常！");
                break;
            case TEST_MODE_AUTO:
                mPromptTextView.setText("自动测试项包括：电压、RTC、蓝牙和风扇，点击开始后程序自动完成检测。");
                break;
            case TEST_MODE_DC:
                mPromptTextView.setText("正常电压范围（单位mV）：[" + DC_RANGE[0] + ", " + DC_RANGE[1] + "]");
                break;
            case TEST_MODE_BT:
                mPromptTextView.setText("需测试蓝牙正常工作！");
                break;
            case TEST_MODE_TIME:
                mPromptTextView.setText("测试设备时钟正常！");
                break;
            case TEST_MODE_FAN:
                mPromptTextView.setText("测试风扇，会自动切换转速！");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("观察老化数据，手动判断结果！");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mK2TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_TEMP:
            case TEST_MODE_LED:
            case TEST_MODE_LED_2:
            case TEST_MODE_AGEINGRESULT:
                mBtn1.setText(R.string.Start);
                mBtn2.setText(R.string.Failure);
                mBtn2.setBackgroundResource(R.drawable.selector_red);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn3.setText(R.string.Succeed);
                mBtn3.setBackgroundResource(R.drawable.selector_blue);
                break;
            case TEST_MODE_PRINT:
                mBtn1.setText(R.string.Print);
                mBtn2.setText(R.string.Set_print);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn2.setBackgroundResource(R.drawable.selector_gray);
                if (mK2TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    mBtn3.setText(R.string.Failure);
                    mBtn3.setBackgroundResource(R.drawable.selector_red);
                }
                break;
            case TEST_MODE_SN:
                mBtn1.setText(R.string.Write);
                mBtn2.setVisibility(View.INVISIBLE);
                if (mK2TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    mBtn3.setText(R.string.Failure);
                    mBtn3.setBackgroundResource(R.drawable.selector_red);
                }
                break;
            case TEST_MODE_KEY:
                mBtn1.setText("校准");
                mBtn2.setText("按键");
                mBtn2.setVisibility(View.VISIBLE);
                mBtn2.setBackgroundResource(R.drawable.selector_gray);
                if (mK2TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    mBtn3.setText(R.string.Failure);
                    mBtn3.setBackgroundResource(R.drawable.selector_red);
                }
                break;
            default:
                mBtn1.setText(R.string.Start);
                mBtn2.setVisibility(View.INVISIBLE);
                if (mK2TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    mBtn3.setText(R.string.Failure);
                    mBtn3.setBackgroundResource(R.drawable.selector_red);
                }
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_btn_1:
                switch (mK2TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        boolean result = true;
                        for (K2TestUnit unit : mK2TestUnits) {
                            if (unit.getType() != K2Utils.K2TestModes.TEST_MODE_PRINT
                                    && unit.getResult() != TEST_PASS) {
                                result = false;
                                break;
                            }
                        }

                        if (!result) {
                            showShortToast("还有未完成的测试项，不能写入SN！");
                            return;
                        }

                        if (PrintUtils.isPrinterConnected()) {
                            if (isEmpty(mDevice.getSn())) {
                                showShortToast("SN还未写入，不能打印！");
                            } else if (isEmpty(mDevice.getMac())) {
                                showShortToast("MAC为空，不能打印！");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mDevice.getSn());
                                params.put("MAC", mDevice.getMac());
                                String oneBarCode = "SN:" + mDevice.getSn();
                                printBarcode(oneBarCode, new Gson().toJson(params));
                            }
                        } else {
                            showShortToast("请先连接打印机！");
                        }
                        break;
                    case TEST_MODE_RESET_SN:
                        showSNSetDialog();
                        break;
                    case TEST_MODE_SN:
                        if (!isWritingSN) {
                            if (TextUtils.isEmpty(mAgeingResult)) {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("mac", mDevice.getMac());
                                PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                            } else {
                                startSetSn();
                            }
                        } else {
                            showShortToast("正在写入SN，请稍后！");
                        }
                        break;
                    case TEST_MODE_MAC:
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(162, params));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mK2TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_LED_2:
                    case TEST_MODE_TEMP:
                        isWriteEndCmd = true;
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mK2TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_KEY:
                        params = new HashMap<>();
                        params.put("module", mK2TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 1);
                        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mK2TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                    case TEST_MODE_RESET_SN:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_TEMP:
                    case TEST_MODE_LED:
                    case TEST_MODE_LED_2:
                    case TEST_MODE_AGEINGRESULT:
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    default:
                        isWriteEndCmd = true;
                        if (mK2TestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mK2TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mK2TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mK2TestUnits.get(mCurTestStep).getModule());
        switch (mK2TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_TIME:
                params.put("state", mK2TestUnits.get(mCurTestStep).getState());
                params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                break;
            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                return;

            default:
                params.put("state", mK2TestUnits.get(mCurTestStep).getState());
                break;
        }

        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(163, params));

        if (mK2TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mK2TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            return;
        }

        mK2AutoTestUnits = K2Utils.generateK2AutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mK2AutoTestUnits.size() > 0 && mAutoUnitStep < mK2AutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mK2AutoTestUnits.get(mAutoUnitStep).getName());

            HashMap<String, Object> params = new HashMap<>();
            params.put("module", mK2AutoTestUnits.get(mAutoUnitStep).getModule());
            params.put("state", mK2AutoTestUnits.get(mAutoUnitStep).getState());
            switch (mK2AutoTestUnits.get(mAutoUnitStep).getType()) {
                case TEST_MODE_TIME:
                    params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                    break;
            }

            PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(163, params));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (K2TestUnit unit : mK2AutoTestUnits) {
                if (unit.getType() != K2Utils.K2TestModes.TEST_MODE_SN &&
                        unit.getType() != K2Utils.K2TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }
            mDescTextView.append("\n------");
            mDescTextView.append("\n自动项测试已完成，结果：" + (result ? "成功" : "失败"));

            mK2TestUnits.get(mCurTestStep).setResult(result ? TEST_PASS : TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (isNewSN) {
            showQuitConfirmDialog();
            return;
        }

        if (mCurTestStep == mK2TestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mCurTestStep++;
            refreshView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopBleScan();
        unregisterBroadcastReceiver();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("TestUnits", mK2TestUnits);
        intent.putExtra(K2Utils.EXTRA_K2, mDevice);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (isNewSN) {
            showQuitConfirmDialog();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        showShortToast("与设备断开连接！");
        finish();
    }

    @Override
    public void onResponse(int key, String data) {
        switch (key) {
            case 163:
                JSONObject jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 1:
                                if (isWriteEndCmd) {
                                    isWriteEndCmd = false;
                                    gotoNextTestModule();
                                } else {
                                    mDescTextView.append("\n指令发送成功！");

                                    switch (mK2TestUnits.get(mCurTestStep).getType()) {
                                        //TODO: 测试项正在测试时，增加相应的提示
                                    }
                                }
                                break;
                            default:
                                mDescTextView.append("\n指令处理失败！");
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 164:
                DeviceModuleStateStruct moduleStateStruct = new Gson().fromJson(data, DeviceModuleStateStruct.class);
                boolean result = false;
                StringBuilder desc = new StringBuilder();

                if ((mK2TestUnits.get(mCurTestStep).getType() == TEST_MODE_AUTO &&
                        mK2AutoTestUnits != null && mAutoUnitStep < mK2AutoTestUnits.size() &&
                        mK2AutoTestUnits.get(mAutoUnitStep).getModule() != moduleStateStruct.getModule())
                    || (mK2TestUnits.get(mCurTestStep).getType() != TEST_MODE_AUTO &&
                        moduleStateStruct.getModule() != mK2TestUnits.get(mCurTestStep).getModule())) {
                    LogcatStorageHelper.addLog("response和request的module不一致！放弃！");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        desc.append("\n").append("直流电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= DC_RANGE[0] && moduleStateStruct.getSub0() <= DC_RANGE[1];
                        break;
                    case 1:
                        if (moduleStateStruct.getState() == 1) {
                            desc.append("\n").append("数码管和蜂鸣器已打开，请观察是否正常。");
                        }
                        break;
                    case 3:
                        if (moduleStateStruct.getState() == 1) {
                            desc.append("\n").append("LED已打开，请观察是否正常。");
                        }
                        break;
                    case 2:
                        if (moduleStateStruct.getState() == -1) {
                            desc.append("\n").append("按键还未校准，请先进行校准！");
                        } else {
                            desc.append("\n").append("按键状态").append("-").append(moduleStateStruct.getState() == 1 ? "正常" : "异常");
                            if (moduleStateStruct.getSub1() > 0 && moduleStateStruct.getSub1() != 4) {
                                mTempResult = mTempResult | 0x1;
                                desc.append("\n").append("按键").append("-").append("功能键").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                            }
                            if (moduleStateStruct.getSub2() > 0 && moduleStateStruct.getSub2() != 4) {
                                mTempResult = mTempResult | 0x10;
                                desc.append("\n").append("按键").append("-").append("童锁键").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                            }
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 7:
                        if (moduleStateStruct.getState() == -1) {
                            desc.append("\n").append("风扇异常！");
                        } else {
                            desc.append("\n").append("风扇正常，目标转速：").append(moduleStateStruct.getSub0()).append("，实际转速：").append(moduleStateStruct.getSub1());
                            result = moduleStateStruct.getState() == 1;
                        }
                        break;
                    case 8:
                        if (moduleStateStruct.getState() == 0) {
                            desc.append("\n").append("环境传感器异常！");
                        } else {
                            desc.append("\n").append("环境").append("：温度").append(getTempFormat(moduleStateStruct.getSub0())).append("，湿度").append(Math.round(moduleStateStruct.getSub1() / 10f)).append("%");
                        }
                        break;
                    case 9:
                        if (moduleStateStruct.getState() != 1) {
                            desc.append("\n").append("液位霍尔异常！");
                        } else {
                            if (moduleStateStruct.getSub0() > 0) {
                                mTempResult = mTempResult | 0x1;
                                desc.append("\n").append("液位霍尔：").append("正常");
                            } else {
                                mTempResult = mTempResult | 0x10;
                                desc.append("\n").append("液位霍尔：").append("缺液");
                            }
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 10:
                        desc.append("\n").append("设备蓝牙").append("已打开");
                        //TODO: 连接蓝牙
                        startBleTest(moduleStateStruct.getBtMac());
                        break;
                    case 11:
                        if (moduleStateStruct.getState() != 1) {
                            desc.append("\n").append("RTC异常！");
                        } else {
                            if (!isEmpty(moduleStateStruct.getTime())) {
                                desc.append("\n").append(DateUtil.getFormatDateFromString(moduleStateStruct.getTime()));
                                try {
                                    if (System.currentTimeMillis() - DateUtil.parseISO8601Date(moduleStateStruct.getTime()).getTime() < 60 * 1000) {
                                        result = true;
                                    }
                                } catch (Exception e) {
                                    desc.append("\n").append("时间解析错误！");
                                    e.printStackTrace();
                                }
                            } else {
                                desc.append("\n").append("时间为空！");
                            }
                        }
                        break;
                }
                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                if (isInAutoUnits) {
                    if (result) {
                        mK2AutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                        gotoNextAutoUnit();
                    }
                } else {
                    if (result) {
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    refershBtnView();
                }

                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state") && !jsonObject.isNull("opt")) {
                    try {
                        int opt = jsonObject.getInt("opt");
                        switch (jsonObject.getInt("state")) {
                            case 0:
                                mDescTextView.append("\n写入命令失败");
                                break;
                            case 1:
                                if (opt == 0) {
                                    mDescTextView.append("\n确认写入状态");
                                    HashMap<String, Object> payload = new HashMap<>();
                                    payload.put("mac", mDevice.getMac());
                                    payload.put("sn", mDevice.getSn());
                                    payload.put("opt", 1);
                                    PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
                                } else if (opt == 1) {
                                    mDescTextView.append("\n进行读取校验");

                                    PetkitSocketInstance.getInstance().sendString(K2Utils.getDefaultRequestForKey(110));
                                } else {
                                    mDescTextView.append("\n opt参数错误！值为：" + opt);
                                }
                                break;
                            case 2:
                                mDescTextView.append("\n写入SN失败");
                                break;
                            case 3:
                                mDescTextView.append("\nSN存在不允许写入");
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 165:
                jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 1:
                                mDescTextView.append("\n指令发送成功");
                                mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                refershBtnView();
                                break;
                            default:
                                mDescTextView.append("\n指令发送失败");
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 162:
                mDescTextView.append("\n指令发送成功");
                mDescTextView.append("\n请重启设备，确认ID是否擦除！");
                mDescTextView.append("\n擦除ID后需要重新测试！");
                break;
            case 167:
                mAgeingResult = data;
                if (TEST_MODE_AGEINGRESULT == mK2TestUnits.get(mCurTestStep).getType()) {
                    mDescTextView.setText(data);
                } else if (!isWritingSN){
                    startSetSn();
                }
                break;
            case 110:
                try {
                    jsonObject = JSONUtils.getJSONObject(data);
                    String mac = null, sn = null;
                    if (!jsonObject.isNull("mac")) {
                        mac = jsonObject.getString("mac");
                    }
                    if (!jsonObject.isNull("sn")) {
                        sn = jsonObject.getString("sn");
                    }

                    if (mDevice.getMac() != null && mDevice.getMac().equalsIgnoreCase(mac) &&
                            mDevice.getSn() != null && mDevice.getSn().equalsIgnoreCase(sn)) {
                        mDescTextView.append("\n写入SN成功");
//                        K2Utils.removeTempDeviceInfo(mDevice);
                        if (isNewSN) {
                            isNewSN = false;
                            K2Utils.storeSucceedDeviceInfo(mDevice, mAgeingResult);
                        }

                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                    } else {
                        mDescTextView.append("\n读取校验失败");
                    }
                } catch (JSONException e) {
                    mDescTextView.append("\n读取校验失败");
                }

                isWritingSN = false;
                break;
        }
    }

    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mK2TestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (K2TestUnit unit : mK2TestUnits) {
                if (unit.getType() != K2Utils.K2TestModes.TEST_MODE_SN &&
                        unit.getType() != K2Utils.K2TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("还有未完成的测试项，不能写入SN！");
            } else {
                if (mTestType == Globals.TYPE_AFTERMARKET || mTestType == Globals.TYPE_DUPLICATE_SN) {
                    generateAndSendSN();
                } else {
                    startScanSN(Globals.K2);
                }
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mDevice.getMac());
            params.put("sn", mDevice.getSn());
            params.put("opt", 0);
            PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, params));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case 0x199:
                String sn = data.getStringExtra(DeviceCommonUtils.EXTRA_DEVICE_SN);
                if (!DeviceCommonUtils.checkSN(sn, Globals.K2)) {
                    showShortToast("无效的SN！");
                    return;
                }
                if (mDevice.getSn() == null || !mDevice.getSn().equals(sn)) {
                    isNewSN = true;
                }

                isWritingSN = true;
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());
                LogcatStorageHelper.addLog("write SN: " + sn);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mDevice.getMac());
                payload.put("sn", sn);
                payload.put("opt", 0);
                PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
                break;
        }
    }


    private void generateAndSendSN() {
        String sn = K2Utils.generateSNForTester(mTester);
        if (sn == null) {
            showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
            return;
        }
        mDevice.setSn(sn);
        mDevice.setCreation(System.currentTimeMillis());

        isNewSN = true;
        isWritingSN = true;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("mac", mDevice.getMac());
        payload.put("sn", sn);
        payload.put("opt", 0);
        PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
    }

    private boolean printBarcode(String onedBarcde, String twodBarcde) {
        return PrintUtils.printText(onedBarcde, twodBarcde, mK2TestUnits.get(mCurTestStep).getState(), new PrintResultCallback() {
            @Override
            public void onPrintSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDescTextView.append("\n" + getString(R.string.printsuccess));
                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                    }
                });
            }

            @Override
            public void onPrintFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDescTextView.append(getString(R.string.printfailed));
                    }
                });
            }

            @Override
            public void onConnected() {

            }
        });
    }


    private String getTempFormat(int temp) {
        return String.format("%.1f℃", temp / 10f);
    }

    private String getKeyDescByState(int state) {
        String desc = null;
        switch (state) {
            case 1:
                desc = "按下";
                break;
            case 2:
                desc = "松开";
                break;
            case 3:
                desc = "单击";
                break;
            case 4:
                desc = "空";
                break;
            case 5:
                desc = "长按";
                break;
            case 6:
                desc = "双击";
                break;
            case 7:
                desc = "半长按";
                break;
            case 0:
                desc = "按键异常";
                break;
        }

        return desc;
    }

    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {

        if (mDevice == null || mDevice.getMac() == null) {
            showShortToast("无效的K2");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置SN");
        builder.setCancelable(false);
        builder.setView(initView(mDevice.getMac(), mDevice.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();

                if (sn == null || sn.length() != 14) {
                    showShortToast("无效的SN");
                    return;
                }
                mDevice.setSn(sn);
                isNewSN =true;
                isWritingSN = true;

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                payload.put("opt", 0);
                PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
            }
        });
        builder.setNegativeButton(R.string.Cancel, null);
        builder.show();
    }

    private boolean isShowing = false;

    // 初始化并获得设置打印数据的界面（两项数据）
    private View initView(String text1, String text2) {
        View view = View.inflate(this, R.layout.setvalue_item, null);
        ((LinearLayout) view.findViewById(R.id.ll_2)).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.tv_title1)).setText("MAC:");
        et1 = (EditText) view.findViewById(R.id.et_value1);
        et1.setText(text1 == null ? "" : text1);
        et1.setSelection(et1.getText().length());
        ((TextView) view.findViewById(R.id.tv_title2)).setText("SN:");
        et2 = (EditText) view.findViewById(R.id.et_value2);
        et2.setText(text2 == null ? "" : text2);
        et2.setSelection(et2.getText().toString().length());
        return view;
    }

    private void startBleTest(String mac) {

        if (mac == null || mac.length() != 12) {
            mDescTextView.append("\n蓝牙MAC异常，测试失败");
        } else {
            StringBuffer stringBuffer = new StringBuffer(mac);
//            stringBuffer.append(mac, 0, 2).append(":").append(mac, 2, 2).append(":")
//                    .append(mac, 4, 2).append(":").append(mac, 6, 2).append(":")
//                    .append(mac, 8, 2).append(":").append(mac, 10, 2);
            stringBuffer.insert(10, ':');
            stringBuffer.insert(8, ':');
            stringBuffer.insert(6, ':');
            stringBuffer.insert(4, ':');
            stringBuffer.insert(2, ':');
            mac = stringBuffer.toString();
        }

        bleMac = mac;

        if(CommonUtils.getAndroidSDKVersion() >= 18){
            if (!getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_BLUETOOTH_LE)) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_SCAN);
            final Intent service = new Intent(this, AndroidBLEActionService.class);
            service.putExtras(bundle);
            startService(service);
        }else {
            CommonUtils.showShortToast(this, "你的手机蓝牙不支持");
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
                            case BLEConsts.PROGRESS_SCANING_TIMEOUT:
                            case BLEConsts.PROGRESS_SCANING_FAILED:
                            case BLEConsts.ERROR_DEVICE_DISCONNECTED:
                            case BLEConsts.PROGRESS_DISCONNECTING:
                            case BLEConsts.ERROR_SYNC_INIT_FAIL:
                            case BLEConsts.ERROR_DEVICE_ID_NULL:
                                mDescTextView.append("\n蓝牙搜索结束");
                                if (isInAutoUnits) {
                                    gotoNextAutoUnit();
                                }
                                break;
                            default:
                                break;
                        }
                        break;

                    case BLEConsts.BROADCAST_ERROR:
                        progress = arg1.getIntExtra(BLEConsts.EXTRA_DATA, 0);
                        break;
                    case BLEConsts.BROADCAST_SCANED_DEVICE:
                        DeviceInfo deviceInfo = (DeviceInfo) arg1.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);

                        PetkitLog.d("onLeScan, deviceInfo k2: " + deviceInfo.toString());

                        if(deviceInfo.getName() == null){
                            return;
                        }

                        if (deviceInfo.getAddress()!=null && deviceInfo.getAddress().equalsIgnoreCase(bleMac)){
                            mDescTextView.append("\n搜索到设备，信号为： " + deviceInfo.getRssi());
                            mDescTextView.append("\n蓝牙测试完成");
                            if (isInAutoUnits) {
                                mK2AutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                            } else {
                                mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                refershBtnView();
                            }

                            stopBleScan();
                        }
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEConsts.BROADCAST_PROGRESS);
        filter.addAction(BLEConsts.BROADCAST_ERROR);
        filter.addAction(BLEConsts.BROADCAST_LOG);
        filter.addAction(BLEConsts.BROADCAST_SCANED_DEVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private void stopBleScan () {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
