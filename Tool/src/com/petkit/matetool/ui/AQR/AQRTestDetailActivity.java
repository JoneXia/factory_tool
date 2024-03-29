package com.petkit.matetool.ui.AQR;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.data.AQRDataUtils;
import com.petkit.android.ble.data.BaseDataUtils;
import com.petkit.android.ble.data.PetkitBleMsg;
import com.petkit.android.utils.ByteUtil;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.AQR.mode.AQRTestUnit;
import com.petkit.matetool.ui.AQR.utils.AQRUtils;
import com.petkit.matetool.ui.P3.mode.GsensorData;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class AQRTestDetailActivity extends BaseActivity implements PrintResultCallback {

    private Tester mTester;
    private int mTestType;
    private int mCurTestStep;
    private ArrayList<AQRTestUnit> mAQRTestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private GsensorData lastGsensorData;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<AQRTestUnit> mAQRAutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private int mDeviceType;
    private boolean isNewSN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mAQRTestUnits = (ArrayList<AQRTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTestType = savedInstanceState.getInt("TestType");
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
            if (savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        } else {
            mAQRTestUnits = (ArrayList<AQRTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mTestType = getIntent().getIntExtra("TestType", Globals.TYPE_TEST);
            mDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
            if (getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        }

        setContentView(R.layout.activity_feeder_test_detail);

        registerBoradcastReceiver();

        if (mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_SN &&
                mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_PRINT &&
                mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_MAC &&
                mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_RESET_SN &&
                mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_RESET_ID) {
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
        outState.putSerializable("TestUnits", mAQRTestUnits);
        outState.putSerializable(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putInt("TestType", mTestType);
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
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
        setTitle(mAQRTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mAQRTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mDevice.getSn())) {
                    if (mAQRTestUnits.get(mCurTestStep).getState() != 2 || (mErrorDevice != null && !mDevice.getSn().equals(mErrorDevice.getSn()))) {
                        mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                } else {
                    mDescTextView.setText("mac:" + mDevice.getMac());
                }
                break;
            case TEST_MODE_DC:
                if (mTestType == Globals.TYPE_TEST_PARTIALLY) {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4500, 5500]");
                } else {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4500, 5500]");
                }
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试指示灯蓝绿交替闪烁，观察是否正常！");
                break;
            case TEST_MODE_PUMP:
                mPromptTextView.setText("测试水泵，先测试有水/没水状态，再判定水泵转动是否正常！");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mAQRTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LED:
            case TEST_MODE_PUMP:
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
                if (mAQRTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mAQRTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mAQRTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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

                sendBleData(AQRDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, sn.getBytes()));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_btn_1:
                switch (mAQRTestUnits.get(mCurTestStep).getType()) {
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
                        startScanSN(mDeviceType);
                        break;
                    case TEST_MODE_SN:
                        startSetSn();
                        break;
                    case TEST_MODE_RESET_ID:
                        //TODO:
                        break;
                    case TEST_MODE_MAC:
                        mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                        setResult(RESULT_OK);
                        gotoNextTestModule();
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mAQRTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_PUMP:
                        isWriteEndCmd = true;
                        mAQRTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        refershBtnView();
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mAQRTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_PRINT:
                    case TEST_MODE_RESET_SN:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LED:
                        mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PUMP:
                        if (mTempResult != 0x11) {
                            showShortToast("请先测试有水和没水状态，再观察水泵是否正常转动！");
                            return;
                        }
                        mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    default:
                        isWriteEndCmd = true;
                        if (mAQRTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mAQRTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        gotoNextTestModule();
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        switch (mAQRTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                break;
            default:
                sendBleData(AQRDataUtils.buildOpCodeBuffer(mAQRTestUnits.get(mCurTestStep).getModule(), mAQRTestUnits.get(mCurTestStep).getState()));
                break;
        }

        if (mAQRTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mAQRTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            return;
        }

        mAQRAutoTestUnits = AQRUtils.generateAQRAutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mAQRAutoTestUnits.size() > 0 && mAutoUnitStep < mAQRAutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mAQRAutoTestUnits.get(mAutoUnitStep).getName());

            HashMap<String, Object> params = new HashMap<>();
            params.put("module", mAQRAutoTestUnits.get(mAutoUnitStep).getModule());
            params.put("state", mAQRAutoTestUnits.get(mAutoUnitStep).getState());
            byte[] data = null;
            switch (mAQRAutoTestUnits.get(mAutoUnitStep).getType()) {
                //if cmd need data, add it
            }

            sendBleData(AQRDataUtils.buildOpCodeBuffer(mAQRAutoTestUnits.get(mAutoUnitStep).getModule(), data));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (AQRTestUnit unit : mAQRAutoTestUnits) {
                if (unit.getType() != AQRUtils.AQRTestModes.TEST_MODE_SN &&
                        unit.getType() != AQRUtils.AQRTestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }
            mDescTextView.append("\n------");
            mDescTextView.append("\n自动项测试已完成，结果：" + (result ? "成功" : "失败"));

            mAQRTestUnits.get(mCurTestStep).setResult(result ? TEST_PASS : TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (isNewSN) {
            showQuitConfirmDialog();
            return;
        }

        if (mCurTestStep == mAQRTestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mCurTestStep++;
            refreshView();

            if (mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_SN
                && mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_RESET_SN
                    && mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_MAC
                    && mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_RESET_ID
                    && mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_PRINT) {
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
        intent.putExtra("TestUnits", mAQRTestUnits);
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
            case BLEConsts.OP_CODE_BATTERY_KEY:
                if (mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_DC) {
                    return;
                }

                if (data.length < 3) {
                    mDescTextView.append("\n数据错误");
                } else {
                    byte[] voltageByte = new byte[2];
                    System.arraycopy(data, 0, voltageByte, 0, 2);
                    short voltage = ByteUtil.bytes2Short(voltageByte);
//                    int battery = ByteUtil.toInt(data[2]);
                    mDescTextView.append("\n电压：" + voltage);
//                    mDescTextView.append("，电量：" + battery);

                    if (mTestType == Globals.TYPE_TEST_PARTIALLY) {
//                        result = voltage >= 2800 && voltage <= 3200;
                        result = voltage >= 4500 && voltage <= 5500;
                    } else {
                        result = voltage >= 4500 && voltage <= 5500;
                    }

                    if (PERMISSION_ERASE) {
                        result = true;
                    }
                }
                break;
//            case BLEConsts.OP_CODE_AQR_PUMP_DATA:
//                if (mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_PUMP) {
//                    return;
//                }
//
//                if (data.length < 1) {
//                    mDescTextView.append("\n数据错误");
//                } else if (data[0] == 0){
//                    mDescTextView.append("\n没水");
//                    mTempResult = mTempResult | 0x1;
//                } else {
//                    mDescTextView.append("\n有水");
//                    mTempResult = mTempResult | 0x10;
//                }
//                break;
            case BLEConsts.OP_CODE_TEST_STEP:
//                if (data.length < 1) {
//                    mDescTextView.append("\n数据错误");
//                } else if (data[0] == 0){
//                    mDescTextView.append("\n写入失败");
//                } else {
//                    mDescTextView.append("\n写入成功");
//                }
                break;
            case BLEConsts.OP_CODE_TEST_RESULT:
//                if (data.length != 1) {
//                    mDescTextView.append("\n数据错误，处理失败");
//                } else if (data[0] != 1){
//                    mDescTextView.append("\n数据写入失败");
//                } else {
//                    mDescTextView.append("\n数据写入成功");
//                    result = true;
//                }
                break;
            case BLEConsts.OP_CODE_WRITE_SN:
                if (mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_SN &&
                        mAQRTestUnits.get(mCurTestStep).getType() != AQRUtils.AQRTestModes.TEST_MODE_RESET_SN) {
                    return;
                }
                if (data.length != 1) {
                    mDescTextView.append("\n数据错误，处理失败");
                } else if (data[0] != 1){
                    mDescTextView.append("\nSN写入失败");
                } else {
                    mDescTextView.append("\nSN写入成功，开始校验");
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
                            mDescTextView.append("\nSN校验成功");
                            if (isNewSN) {
                                isNewSN = false;
                                DeviceCommonUtils.storeSucceedDeviceInfo(Globals.AQR, mDevice, null);
                            }
                        }
                    } else {
                        mDescTextView.append("\nSN校验失败，未写入成功");
                    }
                } else {
                    mDescTextView.append("\nSN校验失败，未写入成功");
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
                mAQRAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                gotoNextAutoUnit();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mAQRAutoTestUnits.get(mAutoUnitStep).getModule()), false);
                    }
                }, 10);
            }
        } else {
            if (result) {
                mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
            } else {
                new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
//                          if (mAQRTestUnits.get(mCurTestStep).getType() == AQRUtils.AQRTestModes.TEST_MODE_PUMP) {
//                              sendBleData(AQRDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_AQR_PUMP_DATA), false);
//                          }
                      }
                  }, 1000);
            }
            refershBtnView();
        }
    }


    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mAQRTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (AQRTestUnit unit : mAQRTestUnits) {
                if (unit.getType() != AQRUtils.AQRTestModes.TEST_MODE_SN &&
                        unit.getType() != AQRUtils.AQRTestModes.TEST_MODE_PRINT
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
            sendBleData(AQRDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, mDevice.getSn().getBytes()));
        }
    }

    private void generateAndSendSN() {
        String sn = DeviceCommonUtils.generateSNForTester(mDeviceType, mTester);
        if (sn == null) {
            showShortToast("今天生成的SN已经达到上限，上传git coSN再更换账号才可以继续测试哦！");
            return;
        }
        isNewSN = true;
        mDevice.setSn(sn);
        mDevice.setCreation(System.currentTimeMillis());
        sendBleData(AQRDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, mDevice.getSn().getBytes()));
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
        return PrintUtils.printText(onedBarcde, twodBarcde, mAQRTestUnits.get(mCurTestStep).getState());
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
                mDevice.setSn(sn);
                isNewSN = true;

                sendBleData(AQRDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, sn.getBytes()));
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
        et1.setVisibility(View.GONE);
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
                mAQRTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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

//        AQRUtils.stopBle(this);
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
