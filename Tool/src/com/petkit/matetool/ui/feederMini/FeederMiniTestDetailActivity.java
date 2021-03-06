package com.petkit.matetool.ui.feederMini;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feeder.mode.ModuleStateStruct;
import com.petkit.matetool.ui.feederMini.mode.FeederMiniTestUnit;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
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

import static com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils.FeederMiniTestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils.FeederMiniTestModes.TEST_MODE_LIGHT;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class FeederMiniTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener , PrintResultCallback {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<FeederMiniTestUnit> mFeederMiniTestUnits;
    private int mTempResult;
    private Feeder mFeeder, mErrorFeeder;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String mAgeingResult = null;    //?????????????????????
    private int mTempStep; //????????????????????????????????????

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFeederMiniTestUnits = (ArrayList<FeederMiniTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mFeeder = (Feeder) savedInstanceState.getSerializable("Feeder");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
            mErrorFeeder = (Feeder) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER);
        } else {
            mFeederMiniTestUnits = (ArrayList<FeederMiniTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mFeeder = (Feeder) getIntent().getSerializableExtra("Feeder");
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
            mErrorFeeder = (Feeder) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER);
        }

        setContentView(R.layout.activity_feeder_test_detail);
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
        outState.putSerializable("TestUnits", mFeederMiniTestUnits);
        outState.putSerializable("Feeder", mFeeder);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER, mErrorFeeder);
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
        setTitle(mFeederMiniTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mFeeder.getMac() + "\n" + "sn:" + mFeeder.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mFeeder.getSn())) {
                    if (mFeederMiniTestUnits.get(mCurTestStep).getState() != 2 || (mErrorFeeder != null && !mFeeder.getSn().equals(mErrorFeeder.getSn()))) {
                        mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mFeeder.getMac() + "\n" + "sn:" + mFeeder.getSn());
                } else {
                    mDescTextView.setText("mac:" + mFeeder.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("??????????????????food?????????wifi?????????");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("???????????????????????????????????????????????????????????????????????????");
                break;
            case TEST_MODE_DOOR:
                mPromptTextView.setText("????????????????????????????????????");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("??????????????????????????????????????????");
                break;
            case TEST_MODE_IR:
                mPromptTextView.setText("???????????????????????????????????????????????????");
                break;
            case TEST_MODE_DC:
            case TEST_MODE_BAT:
                mPromptTextView.setText("???????????????????????????mV??????[5000, 7000]");
                break;
            case TEST_MODE_MOTOR:
                mPromptTextView.setText("??????????????????????????????????????????????????????");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LIGHT:
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
                if (mFeederMiniTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mFeederMiniTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mFeederMiniTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if (isPrinterConnected()) {
                            if (isEmpty(mFeeder.getSn())) {
                                showShortToast("SN??????????????????????????????");
                            } else if (isEmpty(mFeeder.getMac())) {
                                showShortToast("MAC????????????????????????");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mFeeder.getSn());
                                params.put("MAC", mFeeder.getMac());
                                String oneBarCode = "SN:" + mFeeder.getSn();
//                                String twoBarCode = "SN:" + mFeeder.getSn() + ";MAC:" + mFeeder.getMac();
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
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MAC:
                        params = new HashMap<>();
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(162, params));
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        params = new HashMap<>();
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LIGHT:
                        isWriteEndCmd = true;
                        mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mFeederMiniTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LIGHT:
                        if (TEST_MODE_LIGHT == mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        } else if (mFeederMiniTestUnits.get(mCurTestStep).getState() == 3) {
                            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        }
                    default:
                        isWriteEndCmd = true;
                        if (mFeederMiniTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mFeederMiniTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mFeederMiniTestUnits.get(mCurTestStep).getModule());
        switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_TIME:
                params.put("state", mFeederMiniTestUnits.get(mCurTestStep).getState());
                params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
                break;
            case TEST_MODE_MOTOR:
                if (mTempStep == 0) {
                    params.put("state", 1);
                } else if (mTempStep == 1) {
                    params.put("state", 2);
                } else {
                    mTempStep = 0;
                    mTempResult = 0;
                    params.put("state", 1);
                }
                break;
            case TEST_MODE_DOOR:
                if (mTempStep == 0) {
                    params.put("state", 1);
                } else if (mTempStep == 1) {
                    params.put("state", 2);
                } else {
                    mTempStep = 0;
                    mTempResult = 0;
                    params.put("state", 1);
                }
                break;

            default:
                params.put("state", mFeederMiniTestUnits.get(mCurTestStep).getState());
                break;
        }

        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(163, params));

        if (mFeederMiniTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (mCurTestStep == mFeederMiniTestUnits.size() - 1 || !isAutoTest) {
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
        intent.putExtra("TestUnits", mFeederMiniTestUnits);
        intent.putExtra("Feeder", mFeeder);
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

                                    switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                                        case TEST_MODE_DOOR:
                                            showShortToast("????????????????????????????????????");
//                                            showDoorDirectionConfirmDialog();
                                            break;
                                        case TEST_MODE_MOTOR:
                                            showShortToast("?????????????????????????????????");
//                                            showMotorDirectionConfirmDialog();
                                            break;
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
                ModuleStateStruct moduleStateStruct = new Gson().fromJson(data, ModuleStateStruct.class);
                boolean result = false;
                StringBuilder desc = new StringBuilder();

                if (moduleStateStruct.getModule() != mFeederMiniTestUnits.get(mCurTestStep).getModule()) {
                    LogcatStorageHelper.addLog("response???request???module?????????????????????");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("??????cpu").append("-").append("??????").append("-").append(moduleStateStruct.getSub0() == 1 ? "??????" : "??????");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("??????cpu").append("-").append("wifi??????").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if (moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("??????cpu").append("-").append("food??????").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 5:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("?????????").append("-").append("?????????").append("-").append(moduleStateStruct.getSub0() == 1 ? "??????" : "??????");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            desc.append("\n").append("?????????").append("-").append("?????????").append("-");
                            switch (moduleStateStruct.getSub1()) {
                                case 1:
                                    desc.append("??????");
                                    showDoorDirectionConfirmDialog();
                                    break;
                                default:
                                    desc.append("??????");
                                    break;
                            }
                        }
                        break;
                    case 6:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("????????????").append("-").append("????????????").append("-").append(moduleStateStruct.getSub0() == 1 ? "?????????" : "?????????");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            desc.append("\n").append("????????????").append("-").append("????????????").append("-");
                            switch (moduleStateStruct.getSub1()) {
                                case 1:
                                    desc.append("??????");
                                    showMotorDirectionConfirmDialog();
                                    break;
                                default:
                                    desc.append("??????");
                                    break;
                            }
                        }
                        break;
                    case 7:
                        desc.append("\n").append("???").append("-").append("????????????").append("-");
                        switch (moduleStateStruct.getSub0()) {
                            case 0:
                                desc.append("??????");
                                mTempResult = mTempResult | 0x1;
                                break;
                            case 1:
                                desc.append("2KG??????");
                                mTempResult = mTempResult | 0x10;
                                break;
                            case 2:
                                desc.append("4KG??????");
                                mTempResult = mTempResult | 0x100;
                                break;
                            case 3:
                                if (mFeederMiniTestUnits.get(mCurTestStep).getState() == 1 && mTempResult == 0x111) {
                                    desc.append("????????????");
                                    result = true;
                                }
                                break;
                        }
                        desc.append("\n").append("???").append("-").append("????????????").append("-").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("???").append("-").append("????????????").append("-").append(moduleStateStruct.getSub2()).append("???");
                        break;
                    case 9:
                        desc.append("\n").append("????????????").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
                        break;
                    case 10:
                        desc.append("\n").append("????????????").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
                        break;
                    case 14:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("????????????").append("-").append(moduleStateStruct.getSub0() == 1 ? "??????" : "??????");
                            mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 15:
                        if (!isEmpty(moduleStateStruct.getTime())) {
                            desc.append("\n").append(DateUtil.getFormatDateFromString(moduleStateStruct.getTime()));
                            result = true;
                        }
                        break;
                    case 16:
                        desc.append("\n").append("????????????").append("-").append(moduleStateStruct.getSub0() == 1 ? "??????" : "?????????");
                        mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                        result = mTempResult == 0x11;
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
                    mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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

                                FeederMiniUtils.removeTempFeederInfo(mFeeder);
                                FeederMiniUtils.storeSucceedFeederInfo(mFeeder, mAgeingResult);

                                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                if (mFeederMiniTestUnits.get(mCurTestStep).getType() == TEST_MODE_AGEINGRESULT) {
                    mDescTextView.setText(mAgeingResult);
                } else {
                    startSetSn();
                }
                break;
        }
    }

    private void startSetSn() {
        if (isEmpty(mFeeder.getSn()) || (mFeederMiniTestUnits.get(mCurTestStep).getState() == 2
                && mErrorFeeder != null && mFeeder.getSn().equals(mErrorFeeder.getSn()))) {
            boolean result = true;
            for (FeederMiniTestUnit unit : mFeederMiniTestUnits) {
                if (unit.getType() != FeederMiniUtils.FeederMiniTestModes.TEST_MODE_SN &&
                        unit.getType() != FeederMiniUtils.FeederMiniTestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("??????????????????????????????????????????SN???");
            } else {
                String sn = FeederMiniUtils.generateSNForTester(mTester);
                if (sn == null) {
                    showShortToast("???????????????SN???????????????????????????SN??????????????????????????????????????????");
                    return;
                }
                mFeeder.setSn(sn);
                mFeeder.setCreation(System.currentTimeMillis());

                //?????????????????????????????????????????????????????????????????????
                FeederMiniUtils.storeTempFeederInfo(mFeeder);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mFeeder.getMac());
                payload.put("sn", sn);
                if (mFeederMiniTestUnits.get(mCurTestStep).getState() == 2) {
                    payload.put("force", 100);
                }
                PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(161, payload));
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mFeeder.getMac());
            params.put("sn", mFeeder.getSn());
            PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(161, params));
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
        return PrintUtils.printText(onedBarcde, twodBarcde, mFeederMiniTestUnits.get(mCurTestStep).getState());
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

        if (mFeeder == null || mFeeder.getMac() == null) {
            showShortToast("??????????????????");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????SN");
        builder.setCancelable(false);
        builder.setView(initView(mFeeder.getMac(), mFeeder.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();

                if (sn == null || sn.length() != 14) {
                    showShortToast("?????????SN");
                    return;
                }
                mFeeder.setSn(sn);

                FeederMiniUtils.storeTempFeederInfo(mFeeder);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                payload.put("force", 100);
                PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(161, payload));
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Prompt);
        builder.setCancelable(false);
        builder.setMessage(mTempStep == 0 ? "????????????????????????????????????????????????" : "????????????????????????????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                if (mTempStep == 0) {
                    mTempResult = mTempResult | 0x1;
                } else {
                    mTempResult = mTempResult | 0x10;
                }
                mTempStep++;

                if (mTempResult == 0x11) {
                    mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                } else {
                    startTestModule();
                }

                refershBtnView();

            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mTempStep = 0;
                mTempResult = 0;
                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                refershBtnView();
            }
        });
        builder.show();
    }

    private void showDoorDirectionConfirmDialog() {
        if (isShowing) {
            return;
        }

        isShowing = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Prompt);
        builder.setCancelable(false);
        builder.setMessage(mTempStep == 0 ? "??????????????????????????????" : "??????????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                if (mTempStep == 0) {
                    mTempResult = mTempResult | 0x1;
                } else {
                    mTempResult = mTempResult | 0x10;
                }
                mTempStep++;

                if (mTempResult == 0x11) {
                    mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                } else {
                    startTestModule();
                }

                refershBtnView();
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mTempStep = 0;
                mTempResult = 0;
                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
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


    @Override
    public void onPrintSuccess() {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               LoadDialog.dismissDialog();
               mDescTextView.append("\n" + getString(R.string.printsuccess));
               mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
}
