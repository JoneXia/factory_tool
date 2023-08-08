package com.petkit.matetool.ui.D4SH;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
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
import com.petkit.matetool.model.UDPDevice;
import com.petkit.matetool.player.BasePetkitPlayerListener;
import com.petkit.matetool.player.BasePetkitPlayerPortraitViewClickListener;
import com.petkit.matetool.ui.D4S.mode.D4STestUnit;
import com.petkit.matetool.ui.D4S.utils.D4SUtils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.DateUtil;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.JSONUtils;
import com.petkit.matetool.widget.PetkitPlayer;
import com.petkit.matetool.widget.PetkitPlayerPortraitView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.SwitchMode.SWITCH_FULL_OR_NORMAL;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_AUTO;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_MIC;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_PRINT;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_SN;
import static com.petkit.matetool.ui.D4SH.D4SHUtils.D4SHTestModes.TEST_MODE_VIDEO;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;
import static com.petkit.matetool.utils.Globals.TYPE_TEST;

/**
 * Created by Jone on 17/4/24.
 */
public class D4SHTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener,
        PrintResultCallback, BasePetkitPlayerListener, BasePetkitPlayerPortraitViewClickListener {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<D4SHTestUnit> mTestUnits;
    private long mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String mAgeingResult = null;    //老化测试的结果
    private int mTempStep; //有些测试项中会细分成几步，红外测试时使用
    private String bleMac;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    private ArrayList<D4STestUnit> mAutoTestUnits;
    private boolean isInAutoUnits = false;
    private int mAutoUnitStep; //有些测试项中会细分成几步
    private boolean isNewSN = false;
    private int mDeviceType;
    private int mTestType;
    private int leftLow, leftTop, rightLow, rightTop;

    private UDPDevice mUDPDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTestUnits = (ArrayList<D4SHTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_DEVICE);
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            mTestType = savedInstanceState.getInt("TestType");
            mErrorDevice = (Device) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
            mUDPDevice = (UDPDevice) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_UDPDEVICE);
        } else {
            mTestUnits = (ArrayList<D4SHTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_DEVICE);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
            mTestType = getIntent().getIntExtra("TestType", TYPE_TEST);
            mErrorDevice = (Device) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE);
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
            mUDPDevice = (UDPDevice) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_UDPDEVICE);
        }

        setContentView(R.layout.activity_video_test_detail);

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
        outState.putSerializable("TestUnits", mTestUnits);
        outState.putSerializable(DeviceCommonUtils.EXTRA_DEVICE, mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        outState.putSerializable(DeviceCommonUtils.EXTRA_ERROR_DEVICE, mErrorDevice);
        outState.putInt("TestType", mTestType);
        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        outState.putSerializable(DeviceCommonUtils.EXTRA_UDPDEVICE, mUDPDevice);
    }

    @Override
    protected void setupViews() {
        findViewById(R.id.test_btn_1).setOnClickListener(this);
        findViewById(R.id.test_btn_2).setOnClickListener(this);
        findViewById(R.id.test_btn_3).setOnClickListener(this);

        mDescTextView = findViewById(R.id.test_detail);
        mPromptTextView = findViewById(R.id.test_prompt);
        mBtn1 = findViewById(R.id.test_btn_1);
        mBtn2 = findViewById(R.id.test_btn_2);
        mBtn3 = findViewById(R.id.test_btn_3);
        mDescScrollView = findViewById(R.id.test_scrllview);

        initPlayer();
        refreshView();
    }


    /**

     */
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
            case TEST_MODE_KEY:
                mPromptTextView.setText("需要分别测试手动喂食键和WiFi设置键！");
                break;
            case TEST_MODE_DC:

                if (mTestType == Globals.TYPE_TEST_BOARD) {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4750, 6250]");
                } else {
                    mPromptTextView.setText("正常电压范围（单位mV）：[4500, 7000]");
                }
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("测试指示灯和蜂鸣器，观察是否正常！");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("观察老化数据，手动判断结果！");
                break;
            case TEST_MODE_IR:
                mPromptTextView.setText("测试红外对射，需要分别测试遮挡和不遮挡！");
                break;
            case TEST_MODE_MOTOR:
                mPromptTextView.setText("测试马达，需要分别测正转和反转，门位置都需要能关闭！");
                break;
            case TEST_MODE_BAT:
                mPromptTextView.setText("测试电池功能，观察电池电压（单位mV）：[5750, 6250]");
                break;
            case TEST_MODE_BT:
                mPromptTextView.setText("测试蓝牙功能，检测蓝牙信号强度，需大于-70！");
                break;
            case TEST_MODE_TIME:
                mPromptTextView.setText("测试设备时钟！");
                break;
            case TEST_MODE_VIDEO:
                mPromptTextView.setText("观察镜头对焦、色彩、清晰度、反光光斑等是否正常！");
                break;
            case TEST_MODE_IR_cut:
                mPromptTextView.setText("切换白片、红片观察视频效果！");
                break;
            case TEST_MODE_IR_light:
                mPromptTextView.setText("将摄像头置于暗箱，切换补光灯开关观察视频效果！");
                break;
            case TEST_MODE_SPEAK:
                mPromptTextView.setText("根据摄像头语音播报效果，判定是否正常！");
                break;
            case TEST_MODE_MIC:
                mPromptTextView.setText("按住录音后，根据摄像头播放判定是否正常！");
                break;
            case TEST_MODE_AUTO:
                mPromptTextView.setText("自动测试项包括：电压、串口、时钟、蓝牙，点击开始后程序自动完成检测。");
                break;
            default:
                break;
        }

        player.setVisibility(mTestUnits.get(mCurTestStep).isContainVideo() ? View.VISIBLE : View.GONE);
        if (mTestUnits.get(mCurTestStep).isContainVideo()) {
            initPlayer();
            startPlay();
        }

        refershBtnView();
    }

    private void refershBtnView() {

        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_AGEINGRESULT:  // 人工判定结果
            case TEST_MODE_LED:
            case TEST_MODE_VIDEO:
            case TEST_MODE_IR_cut:
            case TEST_MODE_IR_light:
            case TEST_MODE_SPEAK:
                mBtn1.setText(R.string.Start);
                mBtn2.setText(R.string.Failure);
                mBtn2.setBackgroundResource(R.drawable.selector_red);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn3.setText(R.string.Succeed);
                mBtn3.setBackgroundResource(R.drawable.selector_blue);
                break;
            case TEST_MODE_MIC:
                mBtn1.setText("按住说话");
                mBtn2.setText(R.string.Failure);
                mBtn2.setBackgroundResource(R.drawable.selector_red);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn3.setText(R.string.Succeed);
                mBtn3.setBackgroundResource(R.drawable.selector_blue);

                mBtn1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (mTestUnits.get(mCurTestStep).getType() == TEST_MODE_MIC) {
                            switch (event.getAction()) {
                                case ACTION_DOWN:
                                    HashMap<String, Object> params = new HashMap<>();
                                    params.put("module", mTestUnits.get(mCurTestStep).getModule());
                                    params.put("state", 1);
                                    PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
                                    break;
                                case ACTION_UP:
                                    params = new HashMap<>();
                                    params.put("module", mTestUnits.get(mCurTestStep).getModule());
                                    params.put("state", 0);
                                    PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
                                    break;
                            }
                        }
                        return false;
                    }
                });
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
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MAC:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(162, params));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MIC:
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LED:
                    case TEST_MODE_VIDEO:
                    case TEST_MODE_IR_cut:
                    case TEST_MODE_IR_light:
                    case TEST_MODE_SPEAK:
                    case TEST_MODE_MIC:
                        isWriteEndCmd = true;
                        mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                    case TEST_MODE_RESET_SN:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_VIDEO:
                    case TEST_MODE_IR_cut:
                    case TEST_MODE_IR_light:
                    case TEST_MODE_SPEAK:
                    case TEST_MODE_MIC:
                        isWriteEndCmd = true;
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    default:
                        isWriteEndCmd = true;
                        if (mTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        params = new HashMap<>();
                        params.put("module", mTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mTestUnits.get(mCurTestStep).getModule());
        switch (mTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_TIME:
                params.put("state", mTestUnits.get(mCurTestStep).getState());
                params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                break;
            case TEST_MODE_AUTO:
                startAutoUnitsTest();
                return;
            case TEST_MODE_VIDEO:
                if (mTestType == Globals.TYPE_TEST_BOARD && player != null && player.isMute()) {
                    player.switchMuteVolume();
                }

                params.put("state", mTempStep %2 == 0 ? 1 : 0);
                mTempStep++;
                break;
            case TEST_MODE_SPEAK:
            case TEST_MODE_MIC:
                if (mTestType == Globals.TYPE_TEST_BOARD && player != null && !player.isMute()) {
                    player.switchMuteVolume();
                }

                params.put("state", mTempStep %2 == 0 ? 1 : 0);
                mTempStep++;
                break;
            case TEST_MODE_IR_cut:
                mDescTextView.append(mTempStep %2 == 0 ? "\nIRcut-已打开" : "\nIRcut-已关闭");
                params.put("work", mTempStep %2 == 0 ? 1 : 0);
                params.put("state", 1);
                mTempStep++;
                break;
            case TEST_MODE_IR_light:
                mDescTextView.append(mTempStep %2 == 0 ? "\n红外灯-已打开" : "\n红外灯-已关闭");
                params.put("work", mTempStep %2 == 0 ? 1 : 0);
                params.put("state", 1);
                mTempStep++;
                break;

            default:
                params.put("state", mTestUnits.get(mCurTestStep).getState());
                break;
        }

        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));

        if (mTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void startAutoUnitsTest() {
        if (isInAutoUnits) {
            return;
        }

        mAutoTestUnits = D4SUtils.generateD4AutoTestUnits();
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
            switch (mAutoTestUnits.get(mAutoUnitStep).getType()) {
                case TEST_MODE_TIME:
                    params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                    break;
            }

            PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(163, params));
        } else {
            isInAutoUnits = false;

            boolean result = true;
            for (D4STestUnit unit : mAutoTestUnits) {
                if (unit.getType() != D4SUtils.D4STestModes.TEST_MODE_SN &&
                        unit.getType() != D4SUtils.D4STestModes.TEST_MODE_PRINT
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
            mTempStep = 0;
            mCurTestStep++;
            refreshView();
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

                                    switch (mTestUnits.get(mCurTestStep).getType()) {
                                        //TODO: 测试项正在测试时，增加相应的提示
                                    }
                                }
                                break;
                            default:
                                mDescTextView.append("\n指令发送成功！");
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

                if ((mTestUnits.get(mCurTestStep).getType() == TEST_MODE_AUTO &&
                        mAutoTestUnits != null && mAutoUnitStep < mAutoTestUnits.size() &&
                        mAutoTestUnits.get(mAutoUnitStep).getModule() != moduleStateStruct.getModule())
                        || (mTestUnits.get(mCurTestStep).getType() != TEST_MODE_AUTO &&
                        moduleStateStruct.getModule() != mTestUnits.get(mCurTestStep).getModule())) {
                    LogcatStorageHelper.addLog("response和request的module不一致！放弃！");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        desc.append("\n").append("直流电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        if (mTestType == Globals.TYPE_TEST_BOARD) {
                            result = moduleStateStruct.getSub0() >= 4750 && moduleStateStruct.getSub0() <= 6250;
                        } else {
                            result = moduleStateStruct.getSub0() >= 4500 && moduleStateStruct.getSub0() <= 7000;
                        }
                        break;
                    case 1:
                        if (moduleStateStruct.getState() == 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("LED灯和蜂鸣器已关闭");
                        } else if (moduleStateStruct.getState() == 1) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("LED灯和蜂鸣器已打开");
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 2:
                        if (moduleStateStruct.getSub0() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("按键").append("-").append("喂食键1").append("-").append(getKeyDescByState(moduleStateStruct.getSub0()));
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("按键").append("-").append("WiFi设置键").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if (moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x100;
                            desc.append("\n").append("按键").append("-").append("喂食键2").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x111;
                        break;
                    case 3:
                        desc.append("\n").append("粮道");
                        if (moduleStateStruct.getSub0() == 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("-未遮挡！");
                        } else if (moduleStateStruct.getSub0() == 1) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("-遮挡！");
                        } else {
                            desc.append("-异常！");
                        }

//                        desc.append("\n").append("桶内左");
//                        if (moduleStateStruct.getSub1() == 0) {
//                            mTempResult = mTempResult | 0x100;
//                            desc.append("-未遮挡！");
//                        } else if (moduleStateStruct.getSub1() == 1) {
//                            mTempResult = mTempResult | 0x1000;
//                            desc.append("-遮挡！");
//                        } else {
//                            desc.append("-异常！");
//                        }
//
//                        desc.append("\n").append("桶内右");
//                        if (moduleStateStruct.getSub2() == 0) {
//                            mTempResult = mTempResult | 0x10000;
//                            desc.append("-未遮挡！");
//                        } else if (moduleStateStruct.getSub2() == 1) {
//                            mTempResult = mTempResult | 0x100000;
//                            desc.append("-遮挡！");
//                        } else {
//                            desc.append("-异常！");
//                        }

                        result = mTempResult == 0x11;
                        break;
                    case 4:
                        if ((moduleStateStruct.getSub0() & 0x1) == 1) {
                            mTempResult = (mTempResult | 0x1);
                            desc.append("\n").append("关门：1");
                        } else {
                            mTempResult = (mTempResult | 0x2);
                            desc.append("\n").append("关门：0");
                        }

                        if ((moduleStateStruct.getSub0() & 0x2) == 2) {
                            mTempResult = (mTempResult | 0x4);
                            desc.append("，").append("左：1");
                        } else {
                            mTempResult = (mTempResult | 0x8);
                            desc.append("，").append("左：0");
                        }

                        if ((moduleStateStruct.getSub0() & 0x4) == 4) {
                            mTempResult = (mTempResult | 0x10);
                            desc.append("，").append("右：1");
                        } else {
                            mTempResult = (mTempResult | 0x20);
                            desc.append("，").append("右：0");
                        }

                        if ((moduleStateStruct.getSub0() & 0x8) == 8) {
                            mTempResult = (mTempResult | 0x40);
                            desc.append("，").append("辅助：1");
                        } else {
                            mTempResult = (mTempResult | 0x80);
                            desc.append("，").append("辅助：0");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            mTempResult = (mTempResult | 0x100);
                        } else if(moduleStateStruct.getSub1() < 0) {
                            mTempResult = (mTempResult | 0x200);
                        }
                        desc.append("，步数：").append(""+moduleStateStruct.getSub1())
                                .append("电流："+moduleStateStruct.getSub2());
                        result = mTempResult == 0x3ff;
                        break;
                    case 5:
                        desc.append("\n").append("电池电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        if (moduleStateStruct.getSub0() >= 5500) {
                            mTempResult = (mTempResult | 0x1);
                        }
                        if (moduleStateStruct.getSub0() < 6500) {
                            mTempResult = (mTempResult | 0x10);
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 6:
                        desc.append("\n").append("蓝牙").append(":").append("已打开");
                        startBleTest(moduleStateStruct.getBtMac());
                        break;
                    case 7:
                        if (!isEmpty(moduleStateStruct.getTime())) {
                            desc.append("\n").append(DateUtil.getFormatDateFromString(moduleStateStruct.getTime()));
                            result = true;
                        }
                        break;
                    case 8:
                        desc.append("\n").append("串口通信：").append(moduleStateStruct.getState() == 1 ? "正常" : "异常");

                        result = moduleStateStruct.getState() == 1;
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
                        mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                        gotoNextAutoUnit();
                    }
                } else {
                    if (result) {
                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                                    PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(161, payload));
                                } else if (opt == 1) {
                                    mDescTextView.append("\n进行读取校验");

                                    PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getDefaultRequestForKey(110));
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
                                mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                if (mTestUnits.get(mCurTestStep).getType() == TEST_MODE_AGEINGRESULT) {
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
                            DeviceCommonUtils.storeSucceedDeviceInfo(mDeviceType, mDevice, mAgeingResult);
                        }

                        mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
        if (isEmpty(mDevice.getSn()) || (mTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (D4SHTestUnit unit : mTestUnits) {
                if (unit.getType() != TEST_MODE_SN &&
                        unit.getType() != TEST_MODE_PRINT
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
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mDevice.getMac());
            params.put("sn", mDevice.getSn());
            params.put("opt", 0);
            PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(161, params));
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

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mDevice.getMac());
                payload.put("sn", sn);
                if (mTestUnits.get(mCurTestStep).getState() == 2) {
                    payload.put("force", 100);
                }
                payload.put("opt", 0);
                PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(161, payload));
                break;
        }
    }


    private void generateAndSendSN() {
        String sn = DeviceCommonUtils.generateSNForTester(mDeviceType, mTester);
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
        if (mTestUnits.get(mCurTestStep).getState() == 2) {
            payload.put("force", 100);
        }
        payload.put("opt", 0);
        PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(161, payload));
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
                desc = "单机";
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
            default:
                desc = "空";
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
            showShortToast("无效的喂食器");
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

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                payload.put("opt", 0);
                payload.put("force", 100);
                PetkitSocketInstance.getInstance().sendString(DeviceCommonUtils.getRequestForKeyAndPayload(161, payload));
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

                            if (deviceInfo.getRssi() >= -70) {
                                mDescTextView.append("\n蓝牙测试完成");
                                if (isInAutoUnits) {
                                    mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_PASS);
                                } else {
                                    mTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                    refershBtnView();
                                }
                            } else {
                                mDescTextView.append("\n蓝牙信号弱，请检查！");
                                if (isInAutoUnits) {
                                    mAutoTestUnits.get(mAutoUnitStep).setResult(TEST_FAILED);
                                } else {
                                    mTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
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




    private PetkitPlayer player;
    private boolean isPlayerInited = false;
    private boolean isWaitingInit  = false;
    private PetkitPlayerPortraitView playerPortraitView;

    private void initPlayer() {
        player = findViewById(R.id.d4sh_player);
        player.setPlayerListener(this);
        player.post(() -> {
            int videoPlayerHeight = Math.round(player.getWidth()* 10f / 16);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) player.getLayoutParams();
            layoutParams.height = videoPlayerHeight;
            player.setLayoutParams(layoutParams);
        });

    }


    @Override
    public void onConfigurationChanged(@androidx.annotation.NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setNoTitle();
            mPromptTextView.setVisibility(View.GONE);
        } else {
            setHasTitle();
            mPromptTextView.setVisibility(View.VISIBLE);
        }

        if (player != null) {
            player.setConfiguration(newConfig);
        }
    }

    private void startPlay() {
        if (isPlayerInited) {
            player.startVideo(String.format(mTestType == Globals.TYPE_TEST_BOARD ?
                    "http://%s/main.ts?audio=1" : "http://%s/main.flv", mUDPDevice.getIp()));
            isWaitingInit = false;
        } else {
            isWaitingInit = true;
        }
    }


    @Override
    public void onFastBackwardResult(boolean switchVideo) {

    }

    @Override
    public void onFastForwardResult(boolean switchVideo) {

    }

    @Override
    public void onStartPlay() {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onInitSuccess() {
        isPlayerInited = true;
        if (isWaitingInit) {
            startPlay();
        }
    }

    @Override
    public void playing(String videoTime, long position) {

    }

    @Override
    public void onVideoClick() {

    }

    @Override
    public void onVideoTouch(boolean isZoon) {
        if (!isZoon) {
            playerPortraitView.hideOneself();
        } else {
            playerPortraitView.showOneself(isZoon);
        }
    }

    @Override
    public void onSeekCompleted() {

    }

    @Override
    public void preparedVideo(String videoTime, int start, int max) {
        if (playerPortraitView == null) {
            playerPortraitView = new PetkitPlayerPortraitView(this);
            playerPortraitView.setViewClickListener(this);
            playerPortraitView.setBowlImage(mTestType == Globals.TYPE_TEST_BOARD ? R.drawable.video_circle : R.drawable.bowl_d4sh);
            player.addPortraitView(playerPortraitView);
        }

        if (mTestType == Globals.TYPE_TEST_BOARD) {
            if (mTestUnits.get(mCurTestStep).getType() == TEST_MODE_VIDEO) {
                if (player != null && player.isMute()) {
                    player.switchMuteVolume();
                }
            } else {
                if (player != null && !player.isMute()) {
                    player.switchMuteVolume();
                }
            }
        }
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPlayerRestart() {

    }

    @Override
    public void onFullScreenBtnClick() {

    }

    @Override
    public void onQualityOrTimeSpeedBtnClick() {

    }

    @Override
    public void onPlayBtnClick() {

    }

    @Override
    public void onPlayerPortraitViewClick() {
        player.switchFullOrWindowMode(SWITCH_FULL_OR_NORMAL, false);
    }

    @Override
    public void onVolumeBtnClick() {

    }

    @Override
    public void onPrivacyModePlayBtnClick() {

    }
}
