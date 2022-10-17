package com.petkit.matetool.ui.HG;

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

import static com.petkit.matetool.ui.HG.HGUtils.HGTestModes.TEST_MODE_KEY;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class HGTestDetailActivity extends BaseActivity implements PrintResultCallback {

    private Tester mTester;
    private int mTestType;
    private int mCurTestStep;
    private int mDeviceType;
    private ArrayList<HGTestUnit> mTestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private GsensorData lastGsensorData;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<HGTestUnit> mAutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private boolean isNewSN = false;
    private short offset1, offset2, offset3;
    private byte[] mTempData;
    private int mStep, mTempNumber;
    private long mPTCStartTime, mTempTimestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTestUnits = (ArrayList<HGTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTestType = savedInstanceState.getInt("TestType");
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            if (savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        } else {
            mTestUnits = (ArrayList<HGTestUnit>) getIntent().getSerializableExtra("TestUnits");
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

        if (mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_SN &&
                mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_PRINT &&
                mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_RESET_SN &&
                mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_RESET_ID) {
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
                if (mTestType == Globals.TYPE_TEST_PARTIALLY) {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4500, 5500]");
                } else {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4500, 5500]");
                }
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试5个8字灯，6个状态指示灯，观察是否正常！");
                break;
            case TEST_MODE_FAN:
                mPromptTextView.setText("测试风扇，观察转速和电流否正常！");
                break;
            case TEST_MODE_TEMP_ANT:
                mPromptTextView.setText("测试温度检测，3路温湿度传感器，观察是否正常！");
                break;
            case TEST_MODE_TEMP_SET:
                mPromptTextView.setText("温度校准，校准后温度应该与正确温度一致！");
                break;
            case TEST_MODE_PTC:
                mPromptTextView.setText("测试NTC加热片，观察是否正常！");
                break;
            case TEST_MODE_ANION:
                mPromptTextView.setText("测试负离子开关，观察是否正常！");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("测试照明灯，观察是否正常！");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("温度读取测试，观察三个温度传感器数据是否正常！");
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LED:
            case TEST_MODE_LIGHT:
            case TEST_MODE_TEMP_ANT:
            case TEST_MODE_TEMP_SET:
            case TEST_MODE_ANION:
            case TEST_MODE_TEMP:
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
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_FAN:
                    case TEST_MODE_ANION:
                        mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        isWriteEndCmd = true;

                        byte[] data = new byte[2];
                        data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                        data[1] = (byte) 0;
                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    default:
                        mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        refershBtnView();
                        gotoNextTestModule();
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
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_FAN:
                    case TEST_MODE_ANION:
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        isWriteEndCmd = true;

                        byte[] data = new byte[2];
                        data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                        data[1] = (byte) 0;
                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                        break;
                    case TEST_MODE_PTC:
                        isWriteEndCmd = true;
                        if (mTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }
                        data = new byte[3];
                        data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                        data[1] = (byte) 0;
                        data[2] = (byte) 0;
                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                        break;
                    case  TEST_MODE_TEMP:
                        int a, b, c;
                        a = byteToInt(mTempData, 2, 2);
                        b = byteToInt(mTempData, 6, 2);
                        c = byteToInt(mTempData, 10, 2);
                        if (Math.abs(b-c) > 5 || Math.abs(a-b) > 10 || Math.abs(a-c) > 10) {
                            showLongToast("左右温差不超过0.5℃，NTC温差不超过1℃，请确认！");
                            break;
                        }
                    case TEST_MODE_TEMP_SET:
                    case TEST_MODE_LED:
                    case TEST_MODE_TEMP_ANT:
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    default:
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
        byte[] data = null;
        switch (mTestUnits.get(mCurTestStep).getType()) {

            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                break;
            //if cmd need data, add it
            case TEST_MODE_TEMP_ANT:
                data = new byte[4];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                data[1] = data[2] = data[3] = 0;
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
            case TEST_MODE_FAN:
                mDescTextView.append(mStep == 0 ? "\n转速设为：50%" : "\n转速设为：100%");
                data = new byte[2];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                data[1] = (byte) (mStep == 0 ? 50 : 100);
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
            case TEST_MODE_LED:
                data = new byte[2];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
            case TEST_MODE_LIGHT:
            case TEST_MODE_ANION:
                data = new byte[2];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                data[1] = (byte) (mStep%2 == 0 ? 100 : 0);
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                mStep++;
                break;
            case TEST_MODE_PTC:
                mPTCStartTime = System.currentTimeMillis();
                data = new byte[3];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                data[1] = 1;
                data[2] = 100;
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
            default:
                data = new byte[1];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
        }

        if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            byte[] data = new byte[1];
            data[0] = (byte) mAutoTestUnits.get(mAutoUnitStep).getState();

            sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule(), data));
            return;
        }

        mAutoTestUnits = HGUtils.generateAutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mAutoTestUnits.size() > 0 && mAutoUnitStep < mAutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mAutoTestUnits.get(mAutoUnitStep).getName());

            byte[] data = new byte[1];
            data[0] = (byte) mAutoTestUnits.get(mAutoUnitStep).getState();

            sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule(), data));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (HGTestUnit unit : mAutoTestUnits) {
                if (unit.getType() != HGUtils.HGTestModes.TEST_MODE_SN &&
                        unit.getType() != HGUtils.HGTestModes.TEST_MODE_PRINT
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
            switch (mTestUnits.get(mCurTestStep).getType()) {
                //TODO: 测试项正在测试时，增加相应的提示
                case TEST_MODE_TEMP_SET:
                    changeSetViewState(View.GONE);
                    break;
            }
            mTempResult = 0;
            mTempData = null;
            mStep = 0;
            mCurTestStep++;
            mPTCStartTime = 0;
            mTempTimestamp = 0;
            mTempNumber = 0;
            refreshView();

            if (mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_SN
                && mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_RESET_SN
                    && mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_RESET_ID
                    && mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_PRINT) {
                startTestModule();
            }
        }
    }

    @Override
    public void finish() {
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PTC:
            case TEST_MODE_LIGHT:
            case TEST_MODE_FAN:
            case TEST_MODE_ANION:
                byte[] data = new byte[2];
                data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
                data[1] = (byte) 0;
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
                break;
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
        PetkitLog.d("processResponseData key: " + key + ", data: " + data);

        switch (key) {
            case BLEConsts.OP_CODE_TEST_STEP:
                if (isWriteEndCmd) {
                    isWriteEndCmd = false;
                    gotoNextTestModule();
                } else if (data.length > 0 && data[0] == 1) {
                    sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_TEST_INFO), false);
                }
                break;
            case BLEConsts.OP_CODE_TEST_INFO:
                HGUtils.HGTestModes type = mTestUnits.get(mCurTestStep).getType();
                if (type == HGUtils.HGTestModes.TEST_MODE_AUTO) {
                    type = mAutoTestUnits.get(mAutoUnitStep).getType();
                }
                switch (type) {
                    case TEST_MODE_SIGNAL:
                        desc.append("\n通信").append(data[0] == 1 ? "正常" : "异常");
                        result = data[0] == 1;
                        break;
                    case TEST_MODE_KEY:
                        if (data[1] > 0 && data[1] < 7) {
                            int index = data[0];
                            if (index > 6) {
                                index -= 3;
                            }
                            if ((mTempResult & (0x1 << index)) == 0) {
                                desc.append("\n按键：").append(getKeyNameByIndex(data[0])).append("， 检测完成");
                                mTempResult = mTempResult | (0x1 << index);
                            }
                        }
                        result = mTempResult == 0x1ff;
                        break;
                    case TEST_MODE_DC:
                        desc.append("\n电压：").append(byteToInt(data, 0, 2));
                        result = byteToInt(data, 0, 2) >= 4500 && byteToInt(data, 0, 2) <= 5500;
                        break;
                    case TEST_MODE_FAN:
                        desc.append("\n电流：").append(byteToInt(data, 0, 2)).append("， 转速： ").append(byteToInt(data, 2, 2));
                        if (mStep == 0) {
                            if (byteToInt(data, 0, 2) >= 260 && byteToInt(data, 0, 2) <= 480) {
                                mTempNumber++;
                            } else {
                                mTempNumber = 0;
                            }

                            if (mTempNumber >= 5) {
                                mStep = 1;
                                mTempNumber = 0;
                                mTempResult = (mTempResult | 0x1);
                                startTestModule();
                            }
                        } else {
                            if (byteToInt(data, 0, 2) >= 1600 && byteToInt(data, 0, 2) <= 3000) {
                                mTempNumber++;
                            } else {
                                mTempNumber = 0;
                            }

                            if (mTempNumber >= 5) {
                                mStep = 2;
                                mTempResult = (mTempResult | 0x10);
                            }
                        }
                        result = mTempResult == 0x11;
                        break;
                    case TEST_MODE_TEMP_SET:
                        mTempData = data;
                        desc.append("\nNTC-校准前：").append(byteToInt(data, 0, 2)).append("， 校准后： ").append(byteToInt(data, 2, 2));

                        offset1 = (short) (byteToInt(data, 2, 2) - byteToInt(data, 0, 2));
                        offset2 = (short) (byteToInt(data, 6, 2) - byteToInt(data, 4, 2));
                        offset3 = (short) (byteToInt(data, 10, 2) - byteToInt(data, 8, 2));
                        LogcatStorageHelper.addLog("接收数据：" + desc.toString());
                        LogcatStorageHelper.addLog("offset1: " + offset1 + "，offset2: " + offset2 + "，offset3: " + offset3);
                        break;
                    case TEST_MODE_TEMP:
                        mTempData = data;
                        int a, b, c;
                        a = byteToInt(data, 2, 2);
                        b = byteToInt(data, 6, 2);
                        c = byteToInt(data, 10, 2);
                        desc.append("\nNTC：").append(a)
                                .append("，出风左：").append(b).append("，右：").append(c);
                        break;
                    case TEST_MODE_TEMP_ANT:
                        desc.append("\nAHT-温度：").append(byteToInt(data, 12, 2)).append("， 湿度： ").append(data[14]);
                        offset1 = (short) (byteToInt(data, 2, 2) - byteToInt(data, 0, 2));
                        offset2 = (short) (byteToInt(data, 6, 2) - byteToInt(data, 4, 2));
                        offset3 = (short) (byteToInt(data, 10, 2) - byteToInt(data, 8, 2));
                        LogcatStorageHelper.addLog("接收数据：" + desc.toString());
                        LogcatStorageHelper.addLog("offset1: " + offset1 + "offset2: " + offset2 + "offset3: " + offset3);
                        break;
                    case TEST_MODE_PTC:
                        desc.append("\nNTC：").append(byteToInt(data, 0, 2));
                        if (mTempNumber == 0) {
                            mTempNumber = byteToInt(data, 0, 2);
                            if (mTempNumber > 350) {
                                mTempNumber = -1;
                                desc.append("\n起始温度大于35摄氏度，测试无效，请冷却后重新测试");
                            }
                        } else if (mTempNumber != -1){
                            if (byteToInt(data, 0, 2) - mTempNumber > 100
                                    && System.currentTimeMillis() - mPTCStartTime <= 90 * 1000) {
                                result = true;
                                desc.append("\n加热测试已完成");
                            } else if (System.currentTimeMillis() - mPTCStartTime > 90 * 1000) {
                                desc.append("\n加热测试失败，未能在指定时间完成");
                            }
                        }
                        break;
                    case TEST_MODE_ANION:
                        desc.append("\n负离子").append(data[0] == 1 ? "已打开" : "已关闭");
                        if (data[0] == 1) {
                            mTempResult = (mTempResult | 0x1);
                        } else {
                            mTempResult = (mTempResult | 0x10);
                        }
                        result = mTempResult == 0x11;
                        break;
                    case TEST_MODE_LIGHT:
                        desc.append("\n照明灯").append(data[0] == 1 ? "已打开" : "已关闭");
                        if (data[0] == 1) {
                            mTempResult = (mTempResult | 0x1);
                        } else {
                            mTempResult = (mTempResult | 0x10);
                        }
                        result = mTempResult == 0x11;
                        break;
                }
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
                if (mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_SN &&
                        mTestUnits.get(mCurTestStep).getType() != HGUtils.HGTestModes.TEST_MODE_RESET_SN) {
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
                return;
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
                                DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, null);
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
                mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                gotoNextAutoUnit();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule()), false);
                    }
                }, 10);
            }
        } else {
            if (result) {
                mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
            } else {
                new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          if (mTestUnits.get(mCurTestStep).getType() != TEST_MODE_KEY
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

    private String getKeyNameByIndex(int index) {
        String desc = null;
        switch (index) {
            case 0:
                desc = "START";
                break;
            case 1:
                desc = "LIGHT";
                break;
            case 2:
                desc = "LOCK";
                break;
            case 11:
                desc = "温度-加";
                break;
            case 10:
                desc = "温度-减";
                break;
            case 5:
                desc = "风速-加";
                break;
            case 6:
                desc = "风速-减";
                break;
            case 3:
                desc = "时间-加";
                break;
            case 4:
                desc = "时间-减";
                break;
        }

        return desc;
    }

    private String getKeyDescByState(int state) {
        String desc = null;
        switch (state) {
            case 0:
                desc = "空";
                break;
            case 1:
                desc = "单击";
                break;
            case 2:
                desc = "长按";
                break;
            case 3:
                desc = "双击";
                break;
            case 4:
                desc = "按下";
                break;
            case 5:
                desc = "松开";
                break;
            case 6:
                desc = "长长按";
                break;
            case 7:
                desc = "按键结束";
                break;
        }

        return desc;
    }

    private void setNewTemp(short offset) {

        int a, b, c;
        a = byteToInt(mTempData, 0, 2);
        b = byteToInt(mTempData, 4, 2);
        c = byteToInt(mTempData, 8, 2);

        if (Math.abs(a-b) > 3 || Math.abs(b-c) > 3 || Math.abs(c-a) > 3) {
            mDescTextView.append(String.format("\n三个温度传感器温差不在0.3℃以内（ntc：%d，左：%d, 右：%d，请确认！", a, b, c));
            return;
        }

        offset1 += offset;
        int targetTemp = byteToInt(mTempData, 0, 2) + offset1;
        offset2 = (short) (targetTemp - byteToInt(mTempData, 4, 2));
        offset3 = (short) (targetTemp - byteToInt(mTempData, 8, 2));

        PetkitLog.d("发送数据: offset1: " + offset1 + "offset2: " + offset2 + "offset3: " + offset3);
        LogcatStorageHelper.addLog("发送数据: offset1: " + offset1 + "offset2: " + offset2 + "offset3: " + offset3);

        if (Math.abs(offset1) > 30 || Math.abs(offset2) > 30 || Math.abs(offset3) > 30) {
            mDescTextView.append("校准温度不能超过3℃");
        } else {
            byte[] data = new byte[4];
            data[0] = (byte) mTestUnits.get(mCurTestStep).getState();
            data[1] = (byte) (100 + offset1);
            data[2] = (byte) (100 + offset2);
            data[3] = (byte) (100 + offset3);
            sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule(), data));
        }
    }

    private void changeSetViewState(int state) {
        if (state == View.VISIBLE) {
            findViewById(R.id.set_view).setVisibility(View.VISIBLE);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.set_btn_1:
                            setNewTemp((short) -1);
                            break;
                        case R.id.set_btn_2:
                            setNewTemp((short) 1);
                            break;
                    }
                }
            };

            Button btn1 = (Button) findViewById(R.id.set_view).findViewById(R.id.set_btn_1);
            btn1.setText("-0.1℃");
            btn1.setOnClickListener(listener);
            Button btn2 = (Button) findViewById(R.id.set_view).findViewById(R.id.set_btn_2);
            btn2.setText("+0.1℃");
            btn2.setOnClickListener(listener);
        } else {
            findViewById(R.id.set_view).setVisibility(View.GONE);
        }
    }

    private int byteToInt(byte[] data, int srcPos, int len) {
        if (data.length < srcPos + len) {
            return 0;
        }
        byte[] tempByte = new byte[len];
        System.arraycopy(data, srcPos, tempByte, 0, len);
        return ByteUtil.bytes2Short(ByteUtil.reverseBytes(tempByte));
    }

    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
//            for (HGTestUnit unit : mTestUnits) {
//                if (unit.getType() != HGUtils.HGTestModes.TEST_MODE_SN &&
//                        unit.getType() != HGUtils.HGTestModes.TEST_MODE_PRINT
//                        && unit.getResult() != TEST_PASS) {
//                    result = false;
//                    break;
//                }
//            }

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

        switch (mTestUnits.get(mCurTestStep).getType()) {
            //TODO: 测试项正在测试时，增加相应的提示
            case TEST_MODE_TEMP_SET:
                changeSetViewState(View.VISIBLE);
                break;
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
