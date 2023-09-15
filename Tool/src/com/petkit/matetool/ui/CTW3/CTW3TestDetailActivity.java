package com.petkit.matetool.ui.CTW3;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.petkit.android.ble.data.BaseDataUtils;
import com.petkit.android.ble.data.PetkitBleMsg;
import com.petkit.android.utils.ByteUtil;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.P3.mode.GsensorData;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.ui.CTW3.CTW3Utils.CTW3TestModes.TEST_MODE_KEY;
import static com.petkit.matetool.ui.CTW3.CTW3Utils.CTW3TestModes.TEST_MODE_PUMP_RESET;
import static com.petkit.matetool.ui.CTW3.CTW3Utils.CTW3TestModes.TEST_MODE_TIME;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class CTW3TestDetailActivity extends BaseActivity implements PrintResultCallback {

    private Tester mTester;
    private int mTestType;
    private int mCurTestStep;
    private int mDeviceType;
    private ArrayList<CTW3TestUnit> mTestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private GsensorData lastGsensorData;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<CTW3TestUnit> mAutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private boolean isNewSN = false;
    private long mTempTimestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTestUnits = (ArrayList<CTW3TestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTestType = savedInstanceState.getInt("TestType");
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            if (savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        } else {
            mTestUnits = (ArrayList<CTW3TestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mTestType = getIntent().getIntExtra("TestType", Globals.TYPE_TEST);
            mDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
            if (getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        }

        if(savedInstanceState != null) {
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
        }

        setContentView(R.layout.activity_feeder_test_detail);

        registerBoradcastReceiver();

        if (mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_SN &&
                mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PRINT &&
                mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PUMP_RESET &&
                mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_RESET_SN &&
                mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_RESET_ID) {
            startTestModule();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        PrintUtils.setCallback(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CurrentTestStep", mCurTestStep);
        outState.putSerializable("TestUnits", mTestUnits);
        outState.putSerializable(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putInt("TestType", mTestType);
        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        if (mErrorDevice != null) {
            outState.putSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE, mErrorDevice);
        }
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
        setTitle(mTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mDevice.getSn())) {
                    if (mTestUnits.get(mCurTestStep).getState() != 2 || (mErrorDevice != null && !mDevice.getSn().equals(mErrorDevice.getSn()))) {
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                } else {
                    mDescTextView.setText("mac:" + mDevice.getMac());
                }
                break;
            case TEST_MODE_DC:
            case TEST_MODE_AUTO:
                mPromptTextView.setText("正常电压范围（单位mV）：[4500, 5500]，BAT电压范围：【3500， 4300】");
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试指示灯《红红红蓝绿》依次闪烁，观察是否正常！");
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("按键测试，观察是否正常！");
                break;
            case TEST_MODE_PUMP:
                mPromptTextView.setText("测试水泵，先测试有水/没水状态，再判定水泵转动是否正常！");
                break;
            case TEST_MODE_PROXIMITY:
                mPromptTextView.setText("测试接近传感器，包括接近和非接近状态");
                break;
            case TEST_MODE_CHARGING:
                mPromptTextView.setText("测试充电状态");
                break;
            case TEST_MODE_TIME:
                mPromptTextView.setText("设置当前时间，确认显示的是否正确");
                break;
            case TEST_MODE_PUMP_RESET:
                mPromptTextView.setText("请将水泵从内筒取出，然后进行测试，电压应该处于[100, 220]mV");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LED:
            case TEST_MODE_PUMP:
            case TEST_MODE_PROXIMITY:
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
                if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case 0x199:
                String sn = data.getStringExtra(DeviceCommonUtils.EXTRA_DEVICE_SN);
                if (!DeviceCommonUtils.checkSN(sn, mDeviceType)) {
                    showShortToast("无效的SN！");
                    return;
                }
                if (mDevice.getSn() == null || !mDevice.getSn().equals(sn)) {
                    isNewSN = true;
                }
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());
                LogcatStorageHelper.addLog("write SN: " + sn);

                sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, sn.getBytes()));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_btn_1:
                switch (mTestUnits.get(mCurTestStep).getType()) {
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
                        startSetSn();
                        break;
                    case TEST_MODE_RESET_ID:
                        //TODO:
                        break;
                    case TEST_MODE_MAC:
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                        setResult(RESULT_OK);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PUMP_RESET:
                        showPUMPResetDialog();
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_PUMP:
                    case TEST_MODE_PROXIMITY:
                        isWriteEndCmd = true;
                        mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        refershBtnView();
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_PRINT:
                    case TEST_MODE_RESET_SN:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LED:
                    case TEST_MODE_PROXIMITY:
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PUMP:
//                        if (mTempResult != 0x11) {
//                            showShortToast("请先测试有水和没水状态，再观察水泵是否正常转动！");
//                            return;
//                        }
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    default:
                        isWriteEndCmd = true;
                        if (mTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        gotoNextTestModule();
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                break;
            case TEST_MODE_TIME:
                byte[] data = new byte[6];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();

                int sec = BLEConsts.getSeconds();
                data[1] = (byte) ((sec >> 24) & 0xFF);
                data[2] = (byte) ((sec >> 16) & 0xFF);
                data[3] = (byte) ((sec >> 8) & 0xFF);
                data[4] = (byte) ((sec >> 0) & 0xFF);

                data[5] = 20;

                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
            default:
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), mTestUnits.get(mCurTestStep).getState()));
                break;
        }

        if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            return;
        }

        mAutoTestUnits = CTW3Utils.generateAutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mAutoTestUnits.size() > 0 && mAutoUnitStep < mAutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mAutoTestUnits.get(mAutoUnitStep).getName());

            switch (mAutoTestUnits.get(mAutoUnitStep).getType()) {
                //if cmd needneed data, add it
            }

            sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule(), mAutoTestUnits.get(mAutoUnitStep).getState()));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (CTW3TestUnit unit : mAutoTestUnits) {
                if (unit.getType() != CTW3Utils.CTW3TestModes.TEST_MODE_SN &&
                        unit.getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }
            mDescTextView.append("\n------");
            mDescTextView.append("\n自动项测试已完成，结果：" + (result ? "成功" : "失败"));

            mTestUnits.get(mCurTestStep).setResult(result ? TEST_PASS : TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (isNewSN) {
            showQuitConfirmDialog();
            return;
        }

        if (mCurTestStep == mTestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mCurTestStep++;
            refreshView();

            if (mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_SN
                && mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_RESET_SN
                    && mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_RESET_ID
                    && mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PUMP_RESET
                    && mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PRINT) {
                startTestModule();
            }
        }
    }

    @Override
    public void finish() {
        if (isNewSN) {
            mDevice.setSn(null);
        }
        Intent intent = new Intent();
        intent.putExtra("TestUnits", mTestUnits);
        intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
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


    /**
     * @param key
     * @param data
     */
    public void processResponseData(int key, byte[] data) {
        boolean result = false;
        StringBuilder desc = new StringBuilder();

        switch (key) {
            case BLEConsts.OP_CODE_TEST_INFO:
                CTW3Utils.CTW3TestModes type = mTestUnits.get(mCurTestStep).getType();

                if (type == CTW3Utils.CTW3TestModes.TEST_MODE_AUTO && isInAutoUnits) {
                    type = mAutoTestUnits.get(mAutoUnitStep).getType();
                }
                switch (type) {
                    case TEST_MODE_DC:
                        int voltage = ByteUtil.byteToInt(data, 0, 2);
                        desc.append("\n电源电压：" + voltage);
                        result = voltage >= 4500 && voltage <= 5500;
                        break;
                    case TEST_MODE_BAT:
                        int batVoltage = ByteUtil.byteToInt(data, 0, 2);
                        desc.append("\n电池电压：" + batVoltage);
                        result = batVoltage >= 3500 && batVoltage <= 4300;
                        break;
                    case TEST_MODE_LED:
                        break;
                    case TEST_MODE_TIME:
                        int year = ByteUtil.byteToInt(data, 0, 2);
                        int mouth = ByteUtil.byteToInt(data, 2, 1);
                        int day = ByteUtil.byteToInt(data, 3, 1);
                        int hour = ByteUtil.byteToInt(data, 4, 1);
                        int minute = ByteUtil.byteToInt(data, 5, 1);
                        int second = ByteUtil.byteToInt(data, 6, 1);
                        int week = ByteUtil.byteToInt(data, 7, 1);
                        desc.append(String.format("\n %d月%d日 %d时%d分%d秒 周%d", mouth, day, hour, minute, second, week));
                        result = year >= 2023;
                        break;
                    case TEST_MODE_KEY:
                        desc.append("\n按键：" + (data[0] == 1 ? "按下" : "松开"));
                        if (data[0] == 1) {
                            mTempResult = (mTempResult | 0x1);
                        } else {
                            mTempResult = (mTempResult | 0x10);
                        }
                        result = mTempResult == 0x11;
                        break;
                    case TEST_MODE_PUMP_RESET:
                        desc.append("\n" + (data[0] == 1 ? "校准完成" : "校准中"));
                        desc.append("，线圈电压(mV)：" + ByteUtil.byteToInt(data, 1, 2));
                        result = data[0] == 1;
                        break;
                    case TEST_MODE_PUMP:
                        desc.append("\n水泵：" + (data[0] == 1 ? "开启" : "关闭"));
                        desc.append("，水位：" + (data[1] == 1 ? "有水" : "没水"));
                        if (data[1] == 1) {
                            mTempResult = (mTempResult | 0x1);
                        } else {
                            mTempResult = (mTempResult | 0x10);
                        }
                        break;
                    case TEST_MODE_PROXIMITY:
                        desc.append("\n接近数值：" + ByteUtil.byteToInt(data, 0, 2));
                        break;
                    case TEST_MODE_CHARGING:
                        desc.append("\n充电：" + (data[0] == 1 ? "连接" + (data[1] == 1 ? "，充电完成" : "，充电中") : "断开"));
                        if (data[0] == 1) {
                            mTempResult = (mTempResult | 0x1);
                        } else {
                            mTempResult = (mTempResult | 0x10);
                        }
                        result = mTempResult == 0x11;
                        break;
                }
                break;
            case BLEConsts.OP_CODE_WRITE_SN:
                if (mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_SN &&
                        mTestUnits.get(mCurTestStep).getType() != CTW3Utils.CTW3TestModes.TEST_MODE_RESET_SN) {
                    return;
                }
                if (data.length != 1) {
                    desc.append("\n数据错误，处理失败");
                } else if (data[0] != 1){
                    desc.append("\nSN写入失败");
                } else {
                    desc.append("\nSN写入成功，开始校验");
                    sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_GET_INFO), false);
                }
                break;
            case BLEConsts.OP_CODE_GET_INFO:
                if (data.length >= 22) {
                    byte[] snRaw = new byte[14];
                    System.arraycopy(data, 8, snRaw, 0, 14);

                    if (!"0000000000000000000000000000".equals(ByteUtil.byteArrayToHexStr(snRaw))) {
                        String sn = new String(snRaw);
                        if (mDevice.getSn().equalsIgnoreCase(sn)) {
                            desc.append("\nSN校验成功");
                            if (isNewSN) {
                                isNewSN = false;
                                DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, null);
                            }
                        }
                    } else {
                        desc.append("\nSN校验失败，未写入成功");
                    }
                } else {
                    desc.append("\nSN校验失败，未写入成功");
                }
                result = true;
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
                mDescTextView.append("\n测试结果正常");
                mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                gotoNextAutoUnit();
            } else {
                if (mTestUnits.get(mCurTestStep).getType() != TEST_MODE_KEY
                        && System.currentTimeMillis() - mTempTimestamp > 0.9 * 1000) {
                    mTempTimestamp = System.currentTimeMillis();
                    sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_TEST_INFO), false);
                }
            }
        } else {
            if (result) {
                mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mTestUnits.get(mCurTestStep).getType() != TEST_MODE_PUMP_RESET
                                && mTestUnits.get(mCurTestStep).getType() != TEST_MODE_KEY
                                && mTestUnits.get(mCurTestStep).getType() != TEST_MODE_TIME
                                && System.currentTimeMillis() - mTempTimestamp > 0.9 * 1000) {
                            mTempTimestamp = System.currentTimeMillis();
                            sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_TEST_INFO), false);
                        }
                    }
                }, 1000);
            }
            refershBtnView();
        }
    }


    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (CTW3TestUnit unit : mTestUnits) {
                if (unit.getType() != CTW3Utils.CTW3TestModes.TEST_MODE_SN &&
                        unit.getType() != CTW3Utils.CTW3TestModes.TEST_MODE_PRINT
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
                    startScanSN(mDeviceType);
                }
            }
        } else {
            sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, mDevice.getSn().getBytes()));
        }
    }


    private void generateAndSendSN() {
        String sn = DeviceCommonUtils.generateSNForTester(mDeviceType, mTester);
        if (sn == null) {
            showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
            return;
        }
        isNewSN = true;
        mDevice.setSn(sn);
        mDevice.setCreation(System.currentTimeMillis());

        sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, mDevice.getSn().getBytes()));
    }

    private void showPUMPResetDialog() {

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.Prompt)
                .setMessage("确认已将水泵从内筒中取出了吗？")
                .setNegativeButton(R.string.Cancel, null)
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener(){
                            public void onClick(
                                    DialogInterface dialog,
                                    int which){
                                startTestModule();
                            }
                        })
                .show();
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
        return PrintUtils.printText(onedBarcde, twodBarcde, mTestUnits.get(mCurTestStep).getState());
    }


    /********************************************************************************************************************************************/
    // DzPrinter连接打印功能相关
    /********************************************************************************************************************************************/



    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {

        if (mDevice == null || mDevice.getMac() == null) {
            showShortToast("无效的设备");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置SN");
        builder.setCancelable(false);
        builder.setView(initView(mDevice.getMac(), mDevice.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sn = et2.getText().toString();

                if (sn == null || sn.length() != 14) {
                    showShortToast("无效的SN");
                    return;
                }
                isNewSN = true;
                mDevice.setSn(sn);

                sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, sn.getBytes()));
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
        et1.setKeyListener(null);
        ((TextView) view.findViewById(R.id.tv_title2)).setText("SN:");
        et2 = (EditText) view.findViewById(R.id.et_value2);
        et2.setText(text2 == null ? "" : text2);
        et2.setSelection(et2.getText().toString().length());
        et2.requestFocus();
        return view;
    }


    @Override
    public void onPrintSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadDialog.dismissDialog();
                mDescTextView.append("\n" + getString(R.string.printsuccess));
                mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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

    private void sendBleData(byte[] rawData) {
        sendBleData(rawData, true);
    }

    /**
     * 蓝牙发送数据，返回值在广播里
     *
     * @param rawData
     */
    private void sendBleData(byte[] rawData, boolean showLog) {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_STEP_ENTRY);
        intent.putExtra(BLEConsts.EXTRA_DATA, rawData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (showLog) {
            mDescTextView.append("\n指令发送成功！");
        }
    }

    private void showDeviceDisconnectDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.Prompt)
                .setMessage("蓝牙异常断开，请重新连接！")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener(){
                            public void onClick(
                                    DialogInterface dialog,
                                    int which){
                                finish();
                            }
                        })
                .show();
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
                                LogcatStorageHelper.addLog("BLE disconnect, error code : " + progress);
                                showDeviceDisconnectDialog();
                                break;
                            case BLEConsts.PROGRESS_STEP_DATA:
                                ArrayList<PetkitBleMsg> msgs = (ArrayList<PetkitBleMsg>) arg1.getSerializableExtra(BLEConsts.EXTRA_DATA);
                                if (msgs != null) {
                                    for (PetkitBleMsg msg : msgs) {
                                        processResponseData(msg.getCmd(), msg.getData());
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        break;

                    case BLEConsts.BROADCAST_ERROR:
                        progress = arg1.getIntExtra(BLEConsts.EXTRA_DATA, 0);
                        LogcatStorageHelper.addLog("BLE disconnect, error code : " + progress);
                        showDeviceDisconnectDialog();
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
