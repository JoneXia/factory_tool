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
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.DateUtil;
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
    private int mAutoUnitStep; //????????????????????????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mK2TestUnits = (ArrayList<K2TestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable(K2Utils.EXTRA_K2);
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(K2Utils.EXTRA_K2_TESTER);
            mErrorDevice = (Device) savedInstanceState.getSerializable(K2Utils.EXTRA_ERROR_K2);
        } else {
            mK2TestUnits = (ArrayList<K2TestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mDevice = (Device) getIntent().getSerializableExtra(K2Utils.EXTRA_K2);
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(K2Utils.EXTRA_K2_TESTER);
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
                mPromptTextView.setText("???????????????????????????????????????????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_LED:
                mPromptTextView.setText("???????????????????????????????????????????????????");
                break;
            case TEST_MODE_LED_2:
                mPromptTextView.setText("??????LED???????????????????????????");
                break;
            case TEST_MODE_HOLZER:
                mPromptTextView.setText("???????????????????????????????????????");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("????????????????????????????????????????????????");
                break;
            case TEST_MODE_AUTO:
                mPromptTextView.setText("?????????????????????????????????RTC???????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_DC:
                mPromptTextView.setText("???????????????????????????mV??????[" + DC_RANGE[0] + ", " + DC_RANGE[1] + "]");
                break;
            case TEST_MODE_BT:
                mPromptTextView.setText("??????????????????????????????");
                break;
            case TEST_MODE_TIME:
                mPromptTextView.setText("???????????????????????????");
                break;
            case TEST_MODE_FAN:
                mPromptTextView.setText("???????????????????????????????????????");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("??????????????????????????????????????????");
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
                mBtn1.setText("??????");
                mBtn2.setText("??????");
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
                            showShortToast("??????????????????????????????????????????SN???");
                            return;
                        }

                        if (PrintUtils.isPrinterConnected()) {
                            if (isEmpty(mDevice.getSn())) {
                                showShortToast("SN??????????????????????????????");
                            } else if (isEmpty(mDevice.getMac())) {
                                showShortToast("MAC????????????????????????");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mDevice.getSn());
                                params.put("MAC", mDevice.getMac());
                                String oneBarCode = "SN:" + mDevice.getSn();
                                printBarcode(oneBarCode, new Gson().toJson(params));
                            }
                        } else {
                            showShortToast("????????????????????????");
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
            mDescTextView.append("\n???????????????" + mK2AutoTestUnits.get(mAutoUnitStep).getName());

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
            mDescTextView.append("\n????????????????????????????????????" + (result ? "??????" : "??????"));

            mK2TestUnits.get(mCurTestStep).setResult(result ? TEST_PASS : TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
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
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        showShortToast("????????????????????????");
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
                                    mDescTextView.append("\n?????????????????????");

                                    switch (mK2TestUnits.get(mCurTestStep).getType()) {
                                        //TODO: ????????????????????????????????????????????????
                                    }
                                }
                                break;
                            default:
                                mDescTextView.append("\n?????????????????????");
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
                    LogcatStorageHelper.addLog("response???request???module?????????????????????");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        desc.append("\n").append("????????????").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= DC_RANGE[0] && moduleStateStruct.getSub0() <= DC_RANGE[1];
                        break;
                    case 1:
                        if (moduleStateStruct.getState() == 1) {
                            desc.append("\n").append("?????????????????????????????????????????????????????????");
                        }
                        break;
                    case 3:
                        if (moduleStateStruct.getState() == 1) {
                            desc.append("\n").append("LED????????????????????????????????????");
                        }
                        break;
                    case 2:
                        if (moduleStateStruct.getState() == -1) {
                            desc.append("\n").append("??????????????????????????????????????????");
                        } else {
                            desc.append("\n").append("????????????").append("-").append(moduleStateStruct.getState() == 1 ? "??????" : "??????");
                            if (moduleStateStruct.getSub1() > 0 && moduleStateStruct.getSub1() != 4) {
                                mTempResult = mTempResult | 0x1;
                                desc.append("\n").append("??????").append("-").append("?????????").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                            }
                            if (moduleStateStruct.getSub2() > 0 && moduleStateStruct.getSub2() != 4) {
                                mTempResult = mTempResult | 0x10;
                                desc.append("\n").append("??????").append("-").append("?????????").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                            }
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 7:
                        if (moduleStateStruct.getState() == -1) {
                            desc.append("\n").append("???????????????");
                        } else {
                            desc.append("\n").append("??????????????????????????????").append(moduleStateStruct.getSub0()).append("??????????????????").append(moduleStateStruct.getSub1());
                            result = moduleStateStruct.getState() == 1;
                        }
                        break;
                    case 8:
                        if (moduleStateStruct.getState() == 0) {
                            desc.append("\n").append("????????????????????????");
                        } else {
                            desc.append("\n").append("??????").append("?????????").append(getTempFormat(moduleStateStruct.getSub0())).append("?????????").append(Math.round(moduleStateStruct.getSub1() / 10f)).append("%");
                        }
                        break;
                    case 9:
                        if (moduleStateStruct.getState() != 1) {
                            desc.append("\n").append("?????????????????????");
                        } else {
                            if (moduleStateStruct.getSub0() > 0) {
                                mTempResult = mTempResult | 0x1;
                                desc.append("\n").append("???????????????").append("??????");
                            } else {
                                mTempResult = mTempResult | 0x10;
                                desc.append("\n").append("???????????????").append("??????");
                            }
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 10:
                        desc.append("\n").append("????????????").append("?????????");
                        //TODO: ????????????
                        startBleTest(moduleStateStruct.getBtMac());
                        break;
                    case 11:
                        if (moduleStateStruct.getState() != 1) {
                            desc.append("\n").append("RTC?????????");
                        } else {
                            if (!isEmpty(moduleStateStruct.getTime())) {
                                desc.append("\n").append(DateUtil.getFormatDateFromString(moduleStateStruct.getTime()));
                                try {
                                    if (System.currentTimeMillis() - DateUtil.parseISO8601Date(moduleStateStruct.getTime()).getTime() < 60 * 1000) {
                                        result = true;
                                    }
                                } catch (Exception e) {
                                    desc.append("\n").append("?????????????????????");
                                    e.printStackTrace();
                                }
                            } else {
                                desc.append("\n").append("???????????????");
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
                                mDescTextView.append("\n??????????????????");
                                break;
                            case 1:
                                if (opt == 0) {
                                    mDescTextView.append("\n??????????????????");
                                    HashMap<String, Object> payload = new HashMap<>();
                                    payload.put("mac", mDevice.getMac());
                                    payload.put("sn", mDevice.getSn());
                                    payload.put("opt", 1);
                                    PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
                                } else if (opt == 1) {
                                    mDescTextView.append("\n??????????????????");

                                    PetkitSocketInstance.getInstance().sendString(K2Utils.getDefaultRequestForKey(110));
                                } else {
                                    mDescTextView.append("\n opt????????????????????????" + opt);
                                }
                                break;
                            case 2:
                                mDescTextView.append("\n??????SN??????");
                                break;
                            case 3:
                                mDescTextView.append("\nSN?????????????????????");
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
                                mDescTextView.append("\n??????????????????");
                                mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                refershBtnView();
                                break;
                            default:
                                mDescTextView.append("\n??????????????????");
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 162:
                mDescTextView.append("\n??????????????????");
                mDescTextView.append("\n????????????????????????ID???????????????");
                mDescTextView.append("\n??????ID????????????????????????");
                break;
            case 167:
                mAgeingResult = data;
                if (TEST_MODE_AGEINGRESULT == mK2TestUnits.get(mCurTestStep).getType()) {
                    mDescTextView.setText(data);
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
                        mDescTextView.append("\n??????SN??????");
//                        K2Utils.removeTempDeviceInfo(mDevice);
                        K2Utils.storeSucceedDeviceInfo(mDevice, mAgeingResult);

                        mK2TestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        refershBtnView();
                    } else {
                        mDescTextView.append("\n??????????????????");
                    }
                } catch (JSONException e) {
                    mDescTextView.append("\n??????????????????");
                }
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
                showShortToast("??????????????????????????????????????????SN???");
            } else {
                String sn = K2Utils.generateSNForTester(mTester);
                if (sn == null) {
                    showShortToast("???????????????SN???????????????????????????SN??????????????????????????????????????????");
                    return;
                }
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mDevice.getMac());
                payload.put("sn", sn);
                payload.put("opt", 0);
                PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, payload));
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mDevice.getMac());
            params.put("sn", mDevice.getSn());
            params.put("opt", 0);
            PetkitSocketInstance.getInstance().sendString(K2Utils.getRequestForKeyAndPayload(161, params));
        }
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
        return String.format("%.1f???", temp / 10f);
    }

    private String getKeyDescByState(int state) {
        String desc = null;
        switch (state) {
            case 1:
                desc = "??????";
                break;
            case 2:
                desc = "??????";
                break;
            case 3:
                desc = "??????";
                break;
            case 4:
                desc = "???";
                break;
            case 5:
                desc = "??????";
                break;
            case 6:
                desc = "??????";
                break;
            case 7:
                desc = "?????????";
                break;
            case 0:
                desc = "????????????";
                break;
        }

        return desc;
    }

    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {

        if (mDevice == null || mDevice.getMac() == null) {
            showShortToast("?????????K2");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????SN");
        builder.setCancelable(false);
        builder.setView(initView(mDevice.getMac(), mDevice.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();

                if (sn == null || sn.length() != 14) {
                    showShortToast("?????????SN");
                    return;
                }
                mDevice.setSn(sn);

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

    // ???????????????????????????????????????????????????????????????
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
            mDescTextView.append("\n??????MAC?????????????????????");
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
            CommonUtils.showShortToast(this, "???????????????????????????");
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
                                mDescTextView.append("\n??????????????????");
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
                            mDescTextView.append("\n?????????????????????????????? " + deviceInfo.getRssi());
                            mDescTextView.append("\n??????????????????");
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
