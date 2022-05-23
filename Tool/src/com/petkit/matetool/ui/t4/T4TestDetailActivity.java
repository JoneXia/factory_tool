package com.petkit.matetool.ui.t4;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.service.AndroidBLEActionService;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DeviceModuleStateStruct;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.t4.mode.T4TestUnit;
import com.petkit.matetool.ui.t4.utils.T4Utils;
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

import static com.petkit.matetool.ui.t4.utils.T4Utils.T4TestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.t4.utils.T4Utils.T4TestModes.TEST_MODE_AUTO;
import static com.petkit.matetool.ui.t4.utils.T4Utils.T4TestModes.TEST_MODE_BALANCE_SET;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class T4TestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener, PrintResultCallback {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<T4TestUnit> mT4TestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String mAgeingResult = null;    //老化测试的结果
    private int mTempStep; //有些测试项中会细分成几步，红外测试时使用
    private String bleMac;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<T4TestUnit> mT4AutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private int mDeviceType;
    private boolean isNewSN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mT4TestUnits = (ArrayList<T4TestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
        } else {
            mT4TestUnits = (ArrayList<T4TestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
            mErrorDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
        }

        setContentView(R.layout.activity_feeder_test_detail);

        registerBoradcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);


        PrintUtils.setCallback(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CurrentTestStep", mCurTestStep);
        outState.putSerializable("TestUnits", mT4TestUnits);
        outState.putSerializable(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        outState.putSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE, mErrorDevice);
        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
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
        setTitle(mT4TestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mT4TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mDevice.getSn())) {
                    if (mT4TestUnits.get(mCurTestStep).getState() != 2 || (mErrorDevice != null && !mDevice.getSn().equals(mErrorDevice.getSn()))) {
                        mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                } else {
                    mDescTextView.setText("mac:" + mDevice.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("需要分别测试菜单按键和OK按键！");
                break;
            case TEST_MODE_DC:
                mPromptTextView.setText("正常电压范围（单位mV）：[11000, 13000]");
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试显示屏和蜂鸣器，观察是否正常！");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("观察老化数据，手动判断结果！");
                break;
            case TEST_MODE_MOTOR_2:
                mPromptTextView.setText("测试集便盒电机，需分别测试合上位置霍尔和打开位置霍尔！");
                break;
            case TEST_MODE_BALANCE:
                mPromptTextView.setText("观察秤数据，手动判断是否正常！");
                break;
            case TEST_MODE_BALANCE_SET:
            case TEST_MODE_BALANCE_SET_2:
                mPromptTextView.setText("秤校准，请按照提示操作！");
                break;
            case TEST_MODE_MOTOR:
                mPromptTextView.setText("需分别测试初始位置霍尔、排废位置霍尔和抚平位置霍尔！");
                break;
            case TEST_MODE_PROXIMITY:
                mPromptTextView.setText("测试接近传感器！");
                break;
            case TEST_MODE_COVER_HOLZER:
                mPromptTextView.setText("需分别测试上盖盖上与取下！");
                break;
            case TEST_MODE_BT:
                mPromptTextView.setText("需测试蓝牙正常工作！");
                break;
            case TEST_MODE_TIME:
                mPromptTextView.setText("测试设备时钟正常！");
                break;
            case TEST_MODE_AUTO:
                mPromptTextView.setText("自动测试项包括：电压、RTC、蓝牙，点击开始后程序自动完成检测。");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mT4TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_AGEINGRESULT:  // 人工判定结果
            case TEST_MODE_BALANCE:
            case TEST_MODE_LED:
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
                if (mT4TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mT4TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mT4TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                switch (mT4TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if (isPrinterConnected()) {
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
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MAC:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(162, params));
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
                switch (mT4TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_PROXIMITY:
                        isWriteEndCmd = true;
                        mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mT4TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_BALANCE:
                        params = new HashMap<>();
                        params.put("module", mT4TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));

                        mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mT4TestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                    case TEST_MODE_RESET_SN:
                    case TEST_MODE_AUTO:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_BALANCE:
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mT4TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));

                        mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    default:
                        isWriteEndCmd = true;
                        if (mT4TestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        params = new HashMap<>();
                        params.put("module", mT4TestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mT4TestUnits.get(mCurTestStep).getModule());
        switch (mT4TestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_TIME:
                params.put("state", mT4TestUnits.get(mCurTestStep).getState());
                params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                break;
            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                return;

            default:
                params.put("state", mT4TestUnits.get(mCurTestStep).getState());
                break;
        }

        mTempStep = 0;
        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));

        if (mT4TestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            return;
        }

        mT4AutoTestUnits = T4Utils.generateT4AutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mT4AutoTestUnits.size() > 0 && mAutoUnitStep < mT4AutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mT4AutoTestUnits.get(mAutoUnitStep).getName());

            HashMap<String, Object> params = new HashMap<>();
            params.put("module", mT4AutoTestUnits.get(mAutoUnitStep).getModule());
            params.put("state", mT4AutoTestUnits.get(mAutoUnitStep).getState());
            switch (mT4AutoTestUnits.get(mAutoUnitStep).getType()) {
                case TEST_MODE_TIME:
                    params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                    break;
            }

            PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(163, params));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (T4TestUnit unit : mT4AutoTestUnits) {
                if (unit.getType() != T4Utils.T4TestModes.TEST_MODE_SN &&
                        unit.getType() != T4Utils.T4TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }
            mDescTextView.append("\n------");
            mDescTextView.append("\n自动项测试已完成，结果：" + (result ? "成功" : "失败"));

            mT4TestUnits.get(mCurTestStep).setResult(result ? TEST_PASS : TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (mCurTestStep == mT4TestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mTempStep = 0;
            mCurTestStep++;
            refreshView();
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("TestUnits", mT4TestUnits);
        intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        setResult(RESULT_OK, intent);
        super.finish();
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

                                    switch (mT4TestUnits.get(mCurTestStep).getType()) {
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

                if ((mT4TestUnits.get(mCurTestStep).getType() == TEST_MODE_AUTO &&
                        mT4AutoTestUnits != null && mAutoUnitStep < mT4AutoTestUnits.size() &&
                        mT4AutoTestUnits.get(mAutoUnitStep).getModule() != moduleStateStruct.getModule())
                        || (mT4TestUnits.get(mCurTestStep).getType() != TEST_MODE_AUTO &&
                        moduleStateStruct.getModule() != mT4TestUnits.get(mCurTestStep).getModule())) {
                    LogcatStorageHelper.addLog("response和request的module不一致！放弃！");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        desc.append("\n").append("直流电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= 11000 && moduleStateStruct.getSub0() <= 13000;
                        break;
                    case 1:
                        if (moduleStateStruct.getState() == 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("屏幕和蜂鸣器已关闭");
                        } else if (moduleStateStruct.getState() == 1) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("屏幕和蜂鸣器已打开");
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 2:
                        desc.append("\n").append("mcu").append("-").append("通信").append("-").append(moduleStateStruct.getSub0() == 1 ? "正常" : "异常");
                        if (moduleStateStruct.getSub0() > 0) {
                            mTempResult = mTempResult | 0x1;
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("按键").append("-").append("菜单").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if (moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x100;
                            desc.append("\n").append("按键").append("-").append("OK键").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x111;
                        break;
                    case 3:
                        if ((moduleStateStruct.getSub0() & 0x1) == 1
                                        || (moduleStateStruct.getSub0() >> 1 & 0x1) == 1) {
                            desc.append("\n").append("电机").append("-").append(moduleStateStruct.getState() == 0 ? "异常" : "正常")
                                    .append((moduleStateStruct.getSub0() & 0x1) == 1 ? "，打开到位" : "，打开不到位")
                                    .append((moduleStateStruct.getSub0() >> 1 & 0x1) == 1 ? "，关闭到位" : "，关闭不到位");
                        }

                        if (moduleStateStruct.getState() > 0) {
                            if ((moduleStateStruct.getSub0() & 0x3) == 1) {
                                mTempResult = (mTempResult | 0x1);
                            }
                            if ((moduleStateStruct.getSub0() & 0x2) == 2) {
                                mTempResult = (mTempResult | 0x10);
                            }
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 4:
                        desc.append("\n").append("电机").append("-").append(moduleStateStruct.getState() == 1 ? "正常" : "异常")
                                .append("，电流").append("：").append((moduleStateStruct.getSub2()) == 1 ? "正常" : "异常").append("\n");

                        if (moduleStateStruct.getState() > 0) {
                            if ((moduleStateStruct.getSub0() & 0x1) == 1) {
                                mTempResult = (mTempResult | 0x1);
                            }
                            if ((moduleStateStruct.getSub0() >> 1 & 0x1) == 1) {
                                mTempResult = (mTempResult | 0x10);
                            }
                            if ((moduleStateStruct.getSub0() >> 2 & 0x1) == 1) {
                                mTempResult = (mTempResult | 0x100);
                            }
                            desc.append("初始位置霍尔: ").append((mTempResult & 0x1) == 1 ? "正常" : "测试中").append("\n")
                                    .append("排废位置霍尔: ").append((mTempResult & 0x10) == 0x10 ? "正常" : "测试中").append("\n")
                                    .append("抚平位置霍尔: ").append((mTempResult & 0x100) == 0x100 ? "正常" : "测试中").append("\n");
                        }
                        result = mTempResult == 0x111;
                        break;
                    case 5:
                        if (mT4TestUnits.get(mCurTestStep).getState() == 1) {
                            desc.append("\n").append("秤").append("-").append("校准模式").append("-");
                            switch (moduleStateStruct.getSub2()) {
                                case 0:
                                    desc.append("空桶");
                                    mTempResult = mTempResult | 0x1;
                                    break;
                                case 1:
                                    desc.append(mT4TestUnits.get(mCurTestStep).getType() == TEST_MODE_BALANCE_SET ? "10KG模式" : "4KG模式");
                                    mTempResult = mTempResult | 0x10;
                                    break;
                                case 2:
                                    desc.append(mT4TestUnits.get(mCurTestStep).getType() == TEST_MODE_BALANCE_SET ? "20KG模式" : "8KG模式");
                                    mTempResult = mTempResult | 0x100;
                                    break;
                                case 3:
                                    if (mTempResult == 0x111) {
                                        desc.append("校准完成");
                                        result = true;
                                    }
                                    break;
                            }
                        }
                        desc.append("\n").append("秤").append("-").append("读取数值").append("-").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("秤").append("-").append("实际克数").append("-").append(moduleStateStruct.getSub0()).append("克");
                        break;
                    case 6:
                        int leftState = moduleStateStruct.getState() & 0x1;
                        int rightState = (moduleStateStruct.getState() >> 1) & 0x1;
                        desc.append("\n接近传感器： ").append(" 左 - ").append(leftState == 1 ? "已靠近； " : "未靠近； ")
                                .append(" 右 - ").append(rightState == 1 ? "已靠近" : "未靠近");

                        desc.append("\n").append("左侧读取数值：").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("右侧读取数值：").append(moduleStateStruct.getSub2());

                        if (leftState > 0) {
                            mTempResult = mTempResult | 0x1;
                        } else {
                            mTempResult = mTempResult | 0x10;
                        }
                        if (rightState > 0) {
                            mTempResult = mTempResult | 0x100;
                        } else {
                            mTempResult = mTempResult | 0x1000;
                        }
                        result = mTempResult == 0x1111;
                        break;
                    case 7:
                        if (moduleStateStruct.getState() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("上盖：").append("已安装");
                        } else {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("上盖：").append("已打开");
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 8:
                        desc.append("\n").append("蓝牙").append(":").append("已打开");
                        //TODO: 连接蓝牙
                        startBleTest(moduleStateStruct.getBtMac());
                        break;
                    case 9:
                        if (!isEmpty(moduleStateStruct.getTime())) {
                            desc.append("\n").append(DateUtil.getFormatDateFromString(moduleStateStruct.getTime()));
                            result = true;
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
                        mT4AutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                        gotoNextAutoUnit();
                    }
                } else {
                    if (result) {
                        mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    refershBtnView();
                }
                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state")) {
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
                                mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                if (mT4TestUnits.get(mCurTestStep).getType() == TEST_MODE_AGEINGRESULT) {
                    mDescTextView.setText(mAgeingResult);
                } else {
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
                        if (isNewSN) {
                            isNewSN = false;
                            if (mDeviceType == Globals.T4_p) {
                                DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, mAgeingResult, 1);
                            } else {
                                DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, mAgeingResult);
                            }
                        }

                        mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                    } else {
                        mDescTextView.append("\n读取校验失败");
                    }
                } catch (JSONException e) {
                    mDescTextView.append("\n读取校验失败");
                }
                break;
        }
    }

    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mT4TestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (T4TestUnit unit : mT4TestUnits) {
                if (unit.getType() != T4Utils.T4TestModes.TEST_MODE_SN &&
                        unit.getType() != T4Utils.T4TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("还有未完成的测试项，不能写入SN！");
            } else {
//                startScanSN(mDeviceType);
                generateAndSendSN();
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mDevice.getMac());
            params.put("sn", mDevice.getSn());
            params.put("opt", 0);
            PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(161, params));
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
                if (!DeviceCommonUtils.checkSN(sn, Globals.T4)) {
                    showShortToast("无效的SN！");
                    return;
                }
                if (mDevice.getSn() == null || !mDevice.getSn().equals(sn)) {
                    isNewSN = true;
                }
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());
                LogcatStorageHelper.addLog("write SN: " + sn);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mDevice.getMac());
                payload.put("sn", sn);
                if (mDeviceType == Globals.T4_p) {
                    payload.put("withK3", 1);
                }
                if (mT4TestUnits.get(mCurTestStep).getState() == 2) {
                    payload.put("force", 100);
                }
                payload.put("opt", 0);
                PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(161, payload));
                break;
        }
    }


    private void generateAndSendSN() {
        String sn = DeviceCommonUtils.generateSNForTester(Globals.T4, mTester);
        if (sn == null) {
            showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
            return;
        }
        mDevice.setSn(sn);
        mDevice.setCreation(System.currentTimeMillis());

        isNewSN = true;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("mac", mDevice.getMac());
        payload.put("sn", sn);
        if (mDeviceType == Globals.T4_p) {
            payload.put("withK3", 1);
        }
        if (mT4TestUnits.get(mCurTestStep).getState() == 2) {
            payload.put("force", 100);
        }
        payload.put("opt", 0);
        PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(161, payload));
    }

    private Bundle getPrintParam() {
        Bundle param = new Bundle();

        param.putInt(IDzPrinter.PrintParamName.PRINT_DIRECTION, 0);
        param.putInt(IDzPrinter.PrintParamName.PRINT_COPIES, 2);
        param.putInt(IDzPrinter.PrintParamName.GAP_TYPE, 2);
        param.putInt(IDzPrinter.PrintParamName.PRINT_DENSITY, 14);
        param.putInt(IDzPrinter.PrintParamName.PRINT_SPEED, 2);
        return param;
    }

    private boolean printBarcode(String onedBarcde, String twodBarcde) {
        return PrintUtils.printText(onedBarcde, twodBarcde, mT4TestUnits.get(mCurTestStep).getState());
    }

    private String getKeyDescByState(int state) {
        String desc = null;
        switch (state) {
            case 1:
                desc = "按下";
                break;
            case 2:
                desc = "短按";
                break;
            case 3:
                desc = "短按释放";
                break;
            case 4:
                desc = "长按";
                break;
            case 5:
                desc = "长按释放";
                break;
            case 6:
                desc = "双击";
                break;
            case 7:
                desc = "短长按";
                break;
            case 8:
                desc = "双击释放";
                break;
        }

        return desc;
    }

    /********************************************************************************************************************************************/
    // DzPrinter连接打印功能相关
    /********************************************************************************************************************************************/



    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {

        if (mDevice == null || mDevice.getMac() == null) {
            showShortToast("无效的猫厕所");
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

//                T4Utils.storeTempDeviceInfo(mDevice);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                payload.put("opt", 0);
                payload.put("force", 100);
                if (mDeviceType == Globals.T4_p) {
                    payload.put("withK3", 1);
                }
                PetkitSocketInstance.getInstance().sendString(T4Utils.getRequestForKeyAndPayload(161, payload));
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


    @Override
    public void onPrintSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadDialog.dismissDialog();
                mDescTextView.append("\n" + getString(R.string.printsuccess));
                mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
    protected void onDestroy() {
        super.onDestroy();

        unregisterBroadcastReceiver();
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
//                                stopBleScan();
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
                        if(deviceInfo.getName() == null){
                            return;
                        }

                        if (deviceInfo.getAddress()!=null && deviceInfo.getAddress().equalsIgnoreCase(bleMac)){

                            mDescTextView.append("\n搜索到设备，信号为： " + deviceInfo.getRssi());

                            if (deviceInfo.getRssi() >= -60) {
                                mDescTextView.append("\n蓝牙测试完成");
                                if (isInAutoUnits) {
                                    mT4AutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                                } else {
                                    mT4TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                    refershBtnView();
                                }
                            } else {
                                mDescTextView.append("\n蓝牙信号弱，请检查！");
                                if (isInAutoUnits) {
                                    mT4AutoTestUnits.get(mAutoUnitStep).setResult(TEST_FAILED);
                                } else {
                                    mT4TestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                                    refershBtnView();
                                }
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
