package com.petkit.matetool.ui.cozy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DeviceModuleStateStruct;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.cozy.mode.CozyState;
import com.petkit.matetool.ui.cozy.mode.CozyTestUnit;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.utils.PrintResultCallback;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.ui.utils.WifiAdminSimple;
import com.petkit.matetool.utils.DateUtil;
import com.petkit.matetool.utils.JSONUtils;
import com.vilyever.socketclient.util.StringValidation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getDateEN;
import static com.petkit.matetool.ui.cozy.utils.CozyUtils.CozyTestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.cozy.utils.CozyUtils.CozyTestModes.TEST_MODE_TEST;
import static com.petkit.matetool.ui.utils.PrintUtils.KeyGapType;
import static com.petkit.matetool.ui.utils.PrintUtils.KeyPrintDensity;
import static com.petkit.matetool.ui.utils.PrintUtils.KeyPrintSpeed;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

;

/**
 * Created by Jone on 17/4/24.
 */
public class CozyTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener, PrintResultCallback {

    private Tester mTester;
    private int mCurTestStep;

    //??????????????????????????????4????????????????????????????????????
//    private CozyTestZLPTempSimple mZLPTempSimple;


    private ArrayList<CozyTestUnit> mDeviceTestUnits;
    private int mTempResult;
    private Device mDevice, mErrorDevice;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3, mBtn4;
    private ScrollView mDescScrollView;

    private String mCacheFileName;
    private String mAgeingResult = null;    //?????????????????????
    private boolean mTempSensorResult = true;    //??????????????????????????????????????????????????????????????????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mDeviceTestUnits = (ArrayList<CozyTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mDevice = (Device) savedInstanceState.getSerializable("Device");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY_TESTER);
            mErrorDevice = (Device) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY);
        } else {
            mDeviceTestUnits = (ArrayList<CozyTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mDevice = (Device) getIntent().getSerializableExtra("Device");
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY_TESTER);
            mErrorDevice = (Device) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY);
        }

        setContentView(R.layout.activity_feeder_test_detail);

//        mZLPTempSimple = new CozyTestZLPTempSimple();
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
        outState.putSerializable("TestUnits", mDeviceTestUnits);
        outState.putSerializable("Device", mDevice);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
        outState.putSerializable(CozyUtils.EXTRA_COZY, mErrorDevice);
    }

    @Override
    protected void setupViews() {
        findViewById(R.id.test_btn_1).setOnClickListener(this);
        findViewById(R.id.test_btn_2).setOnClickListener(this);
        findViewById(R.id.test_btn_3).setOnClickListener(this);
        findViewById(R.id.test_btn_4).setOnClickListener(this);

        mDescTextView = (TextView) findViewById(R.id.test_detail);
        mPromptTextView = (TextView) findViewById(R.id.test_prompt);
        mBtn1 = (Button) findViewById(R.id.test_btn_1);
        mBtn2 = (Button) findViewById(R.id.test_btn_2);
        mBtn3 = (Button) findViewById(R.id.test_btn_3);
        mBtn4 = (Button) findViewById(R.id.test_btn_4);
        mDescScrollView = (ScrollView) findViewById(R.id.test_scrllview);

        refreshView();
    }

    private void refreshView() {
        setTitle(mDeviceTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mDeviceTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mDevice.getSn())) {
                    if (mDeviceTestUnits.get(mCurTestStep).getState() != 2 || (mErrorDevice != null && !mDevice.getSn().equals(mErrorDevice.getSn()))) {
                        mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mDevice.getMac() + "\n" + "sn:" + mDevice.getSn());
                } else {
                    mDescTextView.setText("mac:" + mDevice.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("????????????????????????????????????");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("?????????????????????????????????????????????????????????wifi?????????????????????????????????");
                break;
            case TEST_MODE_COOL:
            case TEST_MODE_HOT:
                mPromptTextView.setText("?????????????????????????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_VOLTAGE:
                mPromptTextView.setText("?????????????????????5v ~ 7v??????????????????");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("???????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_FAN:
                mPromptTextView.setText("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_TEST:
                mCacheFileName = CommonUtils.getAppDirPath() + DateUtil.formatISO8601Date(new Date()) + ".txt";
                mPromptTextView.setText("WK??????  TS??????  AIR??????  ??????  ??????  ??????");
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
        switch (mDeviceTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LIGHT:
            case TEST_MODE_TEMP:
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
                if (mDeviceTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mDeviceTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    mBtn3.setText(R.string.Failure);
                    mBtn3.setBackgroundResource(R.drawable.selector_red);
                }
                break;
            case TEST_MODE_TEST:
                mBtn1.setText("??????");
                mBtn2.setText("??????");
                mBtn2.setBackgroundResource(R.drawable.selector_gray);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn3.setText("??????");
                mBtn3.setBackgroundResource(R.drawable.selector_gray);
                mBtn4.setText("??????");
                mBtn4.setBackgroundResource(R.drawable.selector_red);
                mBtn4.setVisibility(View.VISIBLE);
                break;
            case TEST_MODE_RESET_SN:
                showSNSetDialog();
                break;
            default:
                mBtn1.setText(R.string.Start);
                mBtn2.setVisibility(View.INVISIBLE);
                if (mDeviceTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                switch (mDeviceTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if (isPrinterConnected()) {
                            if (isEmpty(mDevice.getSn())) {
                                showShortToast("SN??????????????????????????????");
                            } else if (isEmpty(mDevice.getMac())) {
                                showShortToast("MAC????????????????????????");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mDevice.getSn());
                                params.put("MAC", mDevice.getMac());
                                String oneBarCode = "SN:" + mDevice.getSn();
//                                String twoBarCode = "SN:" + mDevice.getSn() + ";MAC:" + mDevice.getMac();
                                printBarcode(oneBarCode, new Gson().toJson(params));
                            }
                        } else {
                            showShortToast("????????????????????????");
                        }
                        break;
                    case TEST_MODE_SN:
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MAC:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mDevice.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(162, params));
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
                switch (mDeviceTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_TEMP:
                        isWriteEndCmd = true;
                        mDeviceTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_TEST:
                        params = new HashMap<>();
                        params.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 2);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mDeviceTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mDeviceTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_TEST:
                        HashMap<String, Object> params2 = new HashMap<>();
                        params2.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
                        params2.put("state", 3);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params2));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_TEMP:
                        if (!mTempSensorResult) {
                            showShortToast("??????????????????????????????");
                            return;
                        }
                    case TEST_MODE_LIGHT:
                        mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    default:
                        isWriteEndCmd = true;
                        if (mDeviceTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mDeviceTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
            case R.id.test_btn_4:
                isWriteEndCmd = true;
                HashMap<String, Object> params = new HashMap<>();
                params.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
                params.put("state", 0);
                PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mDeviceTestUnits.get(mCurTestStep).getModule());
        params.put("state", mDeviceTestUnits.get(mCurTestStep).getState());
        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));

        mTempResult = 0;
        mTempSensorResult = true;
        if (mDeviceTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mDeviceTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (mCurTestStep == mDeviceTestUnits.size() - 1 || !isAutoTest) {
            finish();
        } else {
            mTempResult = 0;
            mCurTestStep++;
            refreshView();
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("TestUnits", mDeviceTestUnits);
        intent.putExtra("Device", mDevice);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        if (mDeviceTestUnits.get(mCurTestStep).getType() == TEST_MODE_TEST) {
            showShortToast("????????????????????????");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startReconnect();
                }
            }, 5000);
        } else {
            showShortToast("????????????????????????");
            finish();
        }
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

                if (mDeviceTestUnits.get(mCurTestStep).getModule() != 10
                        && moduleStateStruct.getModule() != mDeviceTestUnits.get(mCurTestStep).getModule()) {
                    LogcatStorageHelper.addLog("response???request???module?????????????????????");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        if (moduleStateStruct.getSub0() > 0) {
                            desc.append("\n").append("??????cpu").append("???").append("??????").append("-").append(moduleStateStruct.getSub0() == 1 ? "??????" : "??????");
                        }
//                        if(moduleStateStruct.getSub1() > 0) {
//                            mTempResult = mTempResult | 0x1;
//                            desc.append("\n").append("??????cpu").append("???").append("wifi??????").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
//                        }
                        if (moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("??????cpu").append("???").append("?????????").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x1;
                        break;
                    case 6:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("????????????").append("???").append(moduleStateStruct.getSub0() == 1 ? "??????" : "?????????");
                            mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 7:
                        desc.append("\n").append("????????????").append("???").append(getFanState(moduleStateStruct.getSub0()));
                        result = moduleStateStruct.getSub1() == 1;
                        break;
                    case 8:
                        if (moduleStateStruct.getState() == 0) {
                            desc.append("\n").append("??????????????????????????????");
                            mTempSensorResult = false;
                        } else {
                            desc.append("\n").append("????????????").append("?????????").append(getTempFormat(moduleStateStruct.getSub0())).append("?????????").append(getVoltageFormat(moduleStateStruct.getSub1()));
                        }
                        break;
                    case 9:
                        if (moduleStateStruct.getState() == 0) {
                            desc.append("\n").append("??????????????????????????????");
                            mTempSensorResult = false;
                        } else {
                            desc.append("\n").append("????????????").append("?????????").append(getTempFormat(moduleStateStruct.getSub0())).append("?????????").append(getVoltageFormat(moduleStateStruct.getSub1()));
                        }
                        break;
                    case 10:
                        if (moduleStateStruct.getState() == 0) {
                            desc.append("\n").append("????????????????????????");
                            mTempSensorResult = false;
                        } else {
                            desc.append("\n").append("??????").append("?????????").append(getTempFormat(moduleStateStruct.getSub0())).append("?????????").append(Math.round(moduleStateStruct.getSub1() / 10f)).append("%");
                        }
                        break;
                    case 11:
                        desc.append("\n??????:").append(getZLPmode(moduleStateStruct.getSub0())).append("???P?????????").append(getVoltageFormat(moduleStateStruct.getSub1()))
                                .append("???N?????????").append(getVoltageFormat(moduleStateStruct.getSub2()))
                                .append("??? ?????????").append(moduleStateStruct.getSub3());

                        if (mDeviceTestUnits.get(mCurTestStep).getState() == 1) {
                            result = moduleStateStruct.getSub0() == 1 && moduleStateStruct.getSub1() >= 5000 && moduleStateStruct.getSub1() <= 7000
                                    && moduleStateStruct.getSub2() < 1000
                                    && (moduleStateStruct.getSub3() >= 150 && moduleStateStruct.getSub3() <= 1500);
                        } else {
                            result = moduleStateStruct.getSub0() == 2 && moduleStateStruct.getSub1() < 1000
                                    && moduleStateStruct.getSub2() >= 5000 && moduleStateStruct.getSub2() <= 7000
                                    && (moduleStateStruct.getSub3() >= 150 && moduleStateStruct.getSub3() <= 1500);
                        }
                        if (!result) {
                            desc.append("\n").append("?????????????????????");
                        }
                        break;
                    case 12:
                        desc.append("\n").append("????????????").append("???").append(getVoltageFormat(moduleStateStruct.getSub0()));
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
                        break;
                }

                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                if (result) {
                    mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    refershBtnView();
                }
                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 0:
                                mDescTextView.append("\n??????????????????");
                                break;
                            case 1:
                                mDescTextView.append("\n??????SN??????");
                                CozyUtils.storeSucceedCozyInfo(mDevice, mAgeingResult);
                                mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                refershBtnView();
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
                                mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
            case 222:
                String DISPLAY_GAP = "  ";
                CozyState info = new Gson().fromJson(data, CozyState.class);
                desc = new StringBuilder();
                desc.append("\n").append(getTempFormat(info.getWk_temp())).append(DISPLAY_GAP)
                        .append(getTempFormat(info.getTs_temp())).append(DISPLAY_GAP)
                        .append(getTempFormat(info.getAir_temp())).append(DISPLAY_GAP)
                        .append(Math.round(info.getAir_humi() / 10f)).append("%").append(DISPLAY_GAP)
                        .append(info.getFan()).append(DISPLAY_GAP)
                        .append(getZLPmode(info.getWk_mode()));

                FileUtils.writeStringToFile(mCacheFileName, convertDataToFileContent(data), true);
                if (mDescTextView.getLineCount() > 100) {
                    mDescTextView.setText("");
                }
                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case 167:
                mAgeingResult = data;
                if (mDeviceTestUnits.get(mCurTestStep).getType() == TEST_MODE_AGEINGRESULT) {
                    mDescTextView.setText(mAgeingResult);
                } else {
                    startSetSn();
                }
                break;
        }
    }

    private String convertDataToFileContent(String data) {
        String newData = data.replaceAll(":", ";");
        newData = newData.replaceAll(",", ";");
        StringBuilder builder = new StringBuilder(getDateEN());
        builder.append("  ").append(newData).append("\n");
        return builder.toString();
    }

    private Bundle getPrintParam() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        Bundle param = new Bundle();

        param.putInt(IDzPrinter.PrintParamName.PRINT_DIRECTION, 0);
        param.putInt(IDzPrinter.PrintParamName.PRINT_COPIES, 2);
        param.putInt(IDzPrinter.PrintParamName.GAP_TYPE, sharedPreferences.getInt(KeyGapType, 2));
        param.putInt(IDzPrinter.PrintParamName.PRINT_DENSITY, sharedPreferences.getInt(KeyPrintDensity, 14));
        param.putInt(IDzPrinter.PrintParamName.PRINT_SPEED, sharedPreferences.getInt(KeyPrintSpeed, 2));
        return param;
    }

    private boolean printBarcode(String onedBarcde, String twodBarcde) {
        LoadDialog.show(this, "????????????????????????????????????");

        return PrintUtils.printText(onedBarcde, twodBarcde, mDeviceTestUnits.get(mCurTestStep).getState());
    }

    private String getZLPmode(int mode) {
        switch (mode) {
            case 0:
                return "??????";
            case 1:
                return "??????";
            case 2:
                return "??????";
            default:
                return "??????";
        }
    }

    private String getFanState(int value) {
        switch (value) {
            case 5:
                return "??????";
            case 4:
                return "??????";
            case 3:
                return "??????";
            case 2:
                return "??????";
            case 1:
                return "??????";
            default:
                return "??????";
        }
    }

    private String getVoltageFormat(int voltage) {
        return String.format("%.3fV", voltage / 1000f);
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
                desc = "????????????";
                break;
            case 4:
                desc = "??????";
                break;
            case 5:
                desc = "????????????";
                break;
            case 6:
                desc = "??????";
                break;
            case 7:
                desc = "?????????";
                break;
            case 8:
                desc = "????????????";
                break;
        }

        return desc;
    }

    /********************************************************************************************************************************************/
    // DzPrinter????????????????????????
    /********************************************************************************************************************************************/


    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {
        // ??????????????????????????????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????SN");
        builder.setView(initView(mDevice.getMac(), mDevice.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????????????????????????????????
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                mDevice.setSn(sn);
                PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(161, payload));
            }
        });
        builder.setNegativeButton(R.string.Cancel, null);
        builder.show();
    }

    private boolean isShowing = false;

    private void showMotorDirectionConfirmDialog() {
        if (isShowing) {
            return;
        }

        isShowing = true;
//        startTestModule();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Prompt);
        builder.setMessage("????????????????????????????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                refershBtnView();
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mDeviceTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                refershBtnView();
            }
        });
        builder.show();
    }

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


    private void startReconnect() {
        if (isFinishing()) {
            return;
        }
        WifiAdminSimple simple = new WifiAdminSimple(this);
        String apSsid = simple.getWifiConnectedSsid();
        if (apSsid == null) {
            showShortToast("????????????????????????WIFI");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startReconnect();
                }
            }, 5000);
        } else {
            if (!apSsid.toUpperCase().startsWith("PETKIT_COZY_")) {
                showShortToast("???????????????PETKIT_COZY_?????????WIFI???");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startReconnect();
                    }
                }, 5000);
            } else {
                if (!StringValidation.validateRegex(simple.getCurrentApHostIp(), StringValidation.RegexIP)) {
                    showShortToast("WIFI?????????");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startReconnect();
                        }
                    }, 5000);
                    return;
                }
                PetkitSocketInstance.getInstance().startConnect(simple.getCurrentApHostIp(), 8001);
            }
        }
    }


    private void startSetSn() {
        if (isEmpty(mDevice.getSn()) || (mDeviceTestUnits.get(mCurTestStep).getState() == 2
                && mErrorDevice != null && mDevice.getSn().equals(mErrorDevice.getSn()))) {
            boolean result = true;
            for (CozyTestUnit unit : mDeviceTestUnits) {
                if (unit.getType() != CozyUtils.CozyTestModes.TEST_MODE_SN &&
                        unit.getType() != CozyUtils.CozyTestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("??????????????????????????????????????????SN???");
            } else {
                String sn = CozyUtils.generateSNForTester(mTester);
                if (sn == null) {
                    showShortToast("???????????????SN???????????????????????????SN??????????????????????????????????????????");
                    return;
                }
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mDevice.getMac());
                payload.put("sn", sn);
                if (mDeviceTestUnits.get(mCurTestStep).getState() == 2) {
                    payload.put("force", 100);
                }
                mDevice.setSn(sn);
                mDevice.setCreation(System.currentTimeMillis());
                PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(161, payload));
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mDevice.getMac());
            params.put("sn", mDevice.getSn());
            PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(161, params));
        }
    }


    @Override
    public void onPrintSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadDialog.dismissDialog();
                mDescTextView.append("\n" + getString(R.string.printsuccess));
                mDeviceTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                refershBtnView();
            }
        });
    }

    @Override
    public void onPrintFailed() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadDialog.dismissDialog();
                mDescTextView.append(getString(R.string.printfailed));
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
