package com.petkit.matetool.ui.R2;

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

import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class R2TestDetailActivity extends BaseActivity implements PrintResultCallback {

    private Tester mTester;
    private int mTestType;
    private int mCurTestStep;
    private int mDeviceType;
    private ArrayList<R2TestUnit> mTestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private GsensorData lastGsensorData;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<R2TestUnit> mAutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTestUnits = (ArrayList<R2TestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTestType = savedInstanceState.getInt("TestType");
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            if (savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE) != null) {
                mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            }
        } else {
            mTestUnits = (ArrayList<R2TestUnit>) getIntent().getSerializableExtra("TestUnits");
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

        if (mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_SN &&
                mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_PRINT &&
                mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_RESET_SN &&
                mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_RESET_ID) {
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
                mPromptTextView.setText("正常电压范围（单位mV）：[11000, 13000]");
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试指示灯蓝绿交替闪烁，观察是否正常！");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("测试温度，检测的温度和室温温差需在2度以内！");
                break;
            case TEST_MODE_WATER:
                mPromptTextView.setText("测试水位，需分别测试有水和没水！");
                break;
            case TEST_MODE_HEAT:
                mPromptTextView.setText("加热测试，自动测试加热和停止的过程，请等待测试结束！");
                break;
            case TEST_MODE_HEAT_PROTECT:
                mPromptTextView.setText("加热保护测试，需要一定的时间，请等待测试结束！");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LED:
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
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_TEMP:
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
                    case TEST_MODE_TEMP:
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
            case TEST_MODE_HEAT:
            case TEST_MODE_HEAT_PROTECT:
                mDescTextView.append("\n开始测试加热电流");
                byte[] param1 = ByteUtil.short2Bytes((short) (40*10));
                byte[] lightMode = new byte[1];
                lightMode[0]=ByteUtil.intToByte(1);
                byte[] param2 = ByteUtil.mergeBytes(param1,lightMode);
                byte[] param3 = ByteUtil.mergeBytes(param2,ByteUtil.short2Bytes(Short.valueOf(String.valueOf(0))));
                byte[] param4 = ByteUtil.mergeBytes(param3,ByteUtil.short2Bytes(Short.valueOf(String.valueOf(0))));

                sendBleData(BaseDataUtils.buildOpCodeBuffer(221, param4));
                break;
            default:
                sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule()));
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

        mAutoTestUnits = R2Utils.generateAutoTestUnits();
        isInAutoUnits = true;
        mAutoUnitStep = -1;

        gotoNextAutoUnit();

    }

    private void gotoNextAutoUnit() {
        mAutoUnitStep++;

        if (mAutoTestUnits.size() > 0 && mAutoUnitStep < mAutoTestUnits.size()) {
            mDescTextView.append("\n------");
            mDescTextView.append("\n开始进行：" + mAutoTestUnits.get(mAutoUnitStep).getName());

            HashMap<String, Object> params = new HashMap<>();
            params.put("module", mAutoTestUnits.get(mAutoUnitStep).getModule());
            params.put("state", mAutoTestUnits.get(mAutoUnitStep).getState());
            byte[] data = null;
            switch (mAutoTestUnits.get(mAutoUnitStep).getType()) {
                //if cmd need data, add it
            }

            sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule(), data));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (R2TestUnit unit : mAutoTestUnits) {
                if (unit.getType() != R2Utils.R2TestModes.TEST_MODE_SN &&
                        unit.getType() != R2Utils.R2TestModes.TEST_MODE_PRINT
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
        if (mCurTestStep == mTestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mCurTestStep++;
            refreshView();

            if (mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_SN
                && mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_RESET_SN
                    && mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_RESET_ID
                    && mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_PRINT) {
                startTestModule();
            }
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("TestUnits", mTestUnits);
        intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        setResult(RESULT_OK, intent);
        super.finish();
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
                if (mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_DC) {
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
            case BLEConsts.OP_CODE_WRITE_SN:
                if (mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_SN &&
                        mTestUnits.get(mCurTestStep).getType() != R2Utils.R2TestModes.TEST_MODE_RESET_SN) {
                    return;
                }
                if (data.length != 1) {
                    mDescTextView.append("\n数据错误，处理失败");
                } else if (data[0] != 1){
                    mDescTextView.append("\nSN写入失败");
                } else {
                    mDescTextView.append("\nSN写入成功");
                    result = true;
                }
                DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, null);
                break;
            case 210:
                int power = ByteUtil.toInt(data[0]);
                byte[] temp1 = new byte[4];
                temp1[0]=0;
                temp1[1]=0;
                temp1[2]=0;
                temp1[3]=0;
                byte[] times1 = new byte[4];
                System.arraycopy(data,1,times1,0,4);
                Long heatStatusTime = ByteUtil.bytes2Long(ByteUtil.mergeBytes(temp1, times1));
                int status = ByteUtil.toInt(data[5]);
                byte[] times2 = new byte[4];
                System.arraycopy(data,6,times2,0,4);
                Long tempTime = ByteUtil.bytes2Long(ByteUtil.mergeBytes(temp1, times2));
                byte[] temp = new byte[4];
                temp[0]=0;
                temp[1]=0;
                System.arraycopy(data, 10, temp, 2, 2);
                byte[] leftTime = new byte[4];
                leftTime[0]=0;
                leftTime[1]=0;
                System.arraycopy(data, 12, leftTime, 2, 2);
                byte[] current = new byte[4];
                current[0]=0;
                current[1]=0;
                System.arraycopy(data, 14, current, 2, 2);
                byte[] voltage = new byte[4];
                voltage[0]=0;
                voltage[1]=0;
                System.arraycopy(data, 16, voltage, 2, 2);
                int lackWarning = ByteUtil.toInt(data[18]);
                int currentWarning = ByteUtil.toInt(data[19]);
                int voltageWarning = ByteUtil.toInt(data[20]);
                int highWarning = ByteUtil.toInt(data[21]);

                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_AUTO:
                        int v = ByteUtil.bytes2Int(voltage);
                        mDescTextView.append("\n电压： " + v);
                        result = v <= 13000 && v >= 11000;
                        break;
                    case TEST_MODE_LED:
                        break;
                    case TEST_MODE_TEMP:
                        mDescTextView.append("\n温度：" + ByteUtil.bytes2Int(temp)/10f);
                        break;
                    case TEST_MODE_WATER:
                        mDescTextView.append("\n水位：" + (lackWarning == 0 ? "有水" : "没水"));
                        if (lackWarning == 0) {
                            mTempResult = mTempResult | 0x1;
                        }
                        if (lackWarning != 0) {
                            mTempResult = mTempResult | 0x10;
                        }
                        result = mTempResult == 0x11;
                        break;
                    case TEST_MODE_HEAT:
                        int c = ByteUtil.bytes2Int(current);
                        mDescTextView.append("\n" + getDescByStatus(status));
                        if (status == 1 && (c >= 1200 && c <= 2000)) {
                            mDescTextView.append(", 电流：" + c);
                            mTempResult = mTempResult | 0x1;

                            //加热过程测试完成，测试冷却过程，设置目标温度是0
                            mDescTextView.append("\n开始测试冷却电流");
                            byte[] param1 = ByteUtil.short2Bytes((short) (0));
                            byte[] lightMode = new byte[1];
                            lightMode[0]=ByteUtil.intToByte(1);
                            byte[] param2 = ByteUtil.mergeBytes(param1,lightMode);
                            byte[] param3 = ByteUtil.mergeBytes(param2,ByteUtil.short2Bytes(Short.valueOf(String.valueOf(0))));
                            byte[] param4 = ByteUtil.mergeBytes(param3,ByteUtil.short2Bytes(Short.valueOf(String.valueOf(0))));

                            sendBleData(BaseDataUtils.buildOpCodeBuffer(221, param4));
                        } else {
                            mDescTextView.append(", 电流：" + c + ", 电量不符合标准【1200-2000】");
                        }
                        if (mTempResult == 0x1 && status > 2 && (c >= 0 && c <= 50)) {
                            mTempResult = mTempResult | 0x10;
                        }
                        result = mTempResult == 0x11;
                        break;
                    case TEST_MODE_HEAT_PROTECT:
                        c = ByteUtil.bytes2Int(current);
                        mDescTextView.append("\n" + getDescByStatus(status));
                        mDescTextView.append(", 电流：" + c);
                        if (status == 1 && c == 0) {
                            mTempResult++;
                        }
                        result = mTempResult >= 10;
                        break;
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
                mDescTextView.append("\n测试结果正常");
                mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                gotoNextAutoUnit();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendBleData(BaseDataUtils.buildOpCodeBuffer(mAutoTestUnits.get(mAutoUnitStep).getModule()), false);
                    }
                }, 100);
            }
        } else {
            if (result) {
                mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
            }
            if (mTestUnits.get(mCurTestStep).getType() == R2Utils.R2TestModes.TEST_MODE_TEMP ||
                    mTestUnits.get(mCurTestStep).getType() == R2Utils.R2TestModes.TEST_MODE_WATER ||
                    mTestUnits.get(mCurTestStep).getType() == R2Utils.R2TestModes.TEST_MODE_HEAT ||
                    mTestUnits.get(mCurTestStep).getType() == R2Utils.R2TestModes.TEST_MODE_HEAT_PROTECT) {
                        new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendBleData(BaseDataUtils.buildOpCodeBuffer(mTestUnits.get(mCurTestStep).getModule()), false);
                                }
                            }, 1000);
            }
            refershBtnView();
        }
    }

    private String getDescByStatus(int status) {
        switch (status) {
            case 1:
                return "升温中";
            case 2:
                return "恒温中";
            case 3:
                return "暂停";
            default:
                return "故障/缺水";
        }
    }

    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (R2TestUnit unit : mTestUnits) {
                if (unit.getType() != R2Utils.R2TestModes.TEST_MODE_SN &&
                        unit.getType() != R2Utils.R2TestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("还有未完成的测试项，不能写入SN！");
            } else {
                String sn = DeviceCommonUtils.generateSNForTester(mDeviceType, mTester);
                if (sn == null) {
                    showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
                    return;
                }
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());

                sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, sn.getBytes()));
            }
        } else {
            sendBleData(BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_WRITE_SN, mDevice.getSn().getBytes()));
        }
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
