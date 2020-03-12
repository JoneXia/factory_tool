package com.petkit.matetool.ui.feeder;

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

import com.dothantech.lpapi.IAtBitmap;
import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feeder.mode.FeederTestUnit;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.feeder.mode.ModuleStateStruct;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
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

import static com.petkit.matetool.ui.feeder.utils.FeederUtils.FeederTestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.feeder.utils.FeederUtils.FeederTestModes.TEST_MODE_BALANCE;
import static com.petkit.matetool.ui.feeder.utils.FeederUtils.FeederTestModes.TEST_MODE_LIGHT;
import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class FeederTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener, PrintResultCallback {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<FeederTestUnit> mFeederTestUnits;
    private int mTempResult;
    private Feeder mFeeder, mErrorFeeder;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String mAgeingResult = null;    //老化测试的结果

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFeederTestUnits = (ArrayList<FeederTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mFeeder = (Feeder) savedInstanceState.getSerializable("Feeder");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (Tester) savedInstanceState.getSerializable(FeederUtils.EXTRA_FEEDER_TESTER);
            mErrorFeeder = (Feeder) savedInstanceState.getSerializable(FeederUtils.EXTRA_FEEDER);
        } else {
            mFeederTestUnits = (ArrayList<FeederTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mFeeder = (Feeder) getIntent().getSerializableExtra("Feeder");
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (Tester) getIntent().getSerializableExtra(FeederUtils.EXTRA_FEEDER_TESTER);
            mErrorFeeder = (Feeder) getIntent().getSerializableExtra(FeederUtils.EXTRA_FEEDER);
        }

        setContentView(R.layout.activity_feeder_test_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);

        PrintUtils.initApi(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CurrentTestStep", mCurTestStep);
        outState.putSerializable("TestUnits", mFeederTestUnits);
        outState.putSerializable("Feeder", mFeeder);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
        outState.putSerializable(FeederUtils.EXTRA_FEEDER, mErrorFeeder);
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
        setTitle(mFeederTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mFeederTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mFeeder.getMac() + "\n" + "sn:" + mFeeder.getSn());
                break;
            case TEST_MODE_SN:
                if (!isEmpty(mFeeder.getSn())) {
                    if (mFeederTestUnits.get(mCurTestStep).getState() != 2 || (mErrorFeeder != null && !mFeeder.getSn().equals(mErrorFeeder.getSn()))) {
                        mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mFeeder.getMac() + "\n" + "sn:" + mFeeder.getSn());
                } else {
                    mDescTextView.setText("mac:" + mFeeder.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("需要分别测试food按键和wifi按键！");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("点击开始，观察喂食器三个灯依次亮灭，蜂鸣器响一秒！");
                break;
            case TEST_MODE_DOOR:
                mPromptTextView.setText("需要测试开门和关门！");
                break;
            case TEST_MODE_LID:
                mPromptTextView.setText("需要粮桶盖打开和合上！");
                break;
            case TEST_MODE_BALANCE:
                mPromptTextView.setText("点击开始后，按照提示放指定重量的砝码！");
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
        switch (mFeederTestUnits.get(mCurTestStep).getType()) {
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
                if (mFeederTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mFeederTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (TEST_MODE_BALANCE == mFeederTestUnits.get(mCurTestStep).getType()
                        && mFeederTestUnits.get(mCurTestStep).getState() == 3) {
                    mBtn2.setText(R.string.Failure);
                    mBtn2.setBackgroundResource(R.drawable.selector_red);
                    mBtn2.setVisibility(View.VISIBLE);
                    mBtn3.setText(R.string.Succeed);
                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
                } else {
                    if (mFeederTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
                        mBtn3.setText(R.string.Succeed);
                        mBtn3.setBackgroundResource(R.drawable.selector_blue);
                    } else {
                        mBtn3.setText(R.string.Failure);
                        mBtn3.setBackgroundResource(R.drawable.selector_red);
                    }
                }
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_btn_1:
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if (isPrinterConnected()) {
                            if (isEmpty(mFeeder.getSn())) {
                                showShortToast("SN还未写入，不能打印！");
                            } else if (isEmpty(mFeeder.getMac())) {
                                showShortToast("MAC为空，不能打印！");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mFeeder.getSn());
                                params.put("MAC", mFeeder.getMac());
                                String oneBarCode = "SN:" + mFeeder.getSn();
//                                String twoBarCode = "SN:" + mFeeder.getSn() + ";MAC:" + mFeeder.getMac();
                                LoadDialog.show(this, "正在打印标签，请稍后……");
                                PrintUtils.printText(oneBarCode, new Gson().toJson(params));
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
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(167, params));
                        break;
                    case TEST_MODE_MAC:
                        params = new HashMap<>();
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mFeeder.getMac());
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(162, params));
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
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_BALANCE:
                        isWriteEndCmd = true;
                        mFeederTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mFeederTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mFeederTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        gotoNextTestModule();
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_AGEINGRESULT:
                        mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_BALANCE:
                        if (TEST_MODE_LIGHT == mFeederTestUnits.get(mCurTestStep).getType()) {
                            mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        } else if (mFeederTestUnits.get(mCurTestStep).getState() == 3) {
                            mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                        }
                    default:
                        isWriteEndCmd = true;
                        if (mFeederTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mFeederTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mFeederTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mFeederTestUnits.get(mCurTestStep).getModule());
        params.put("state", mFeederTestUnits.get(mCurTestStep).getState());
        if (mFeederTestUnits.get(mCurTestStep).getModule() == 15) {
            params.put("time", DateUtil.formatISO8601DateWithMills(new Date()));
        }
        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(163, params));

        mTempResult = 0;
        if (mFeederTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mFeederTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if (mCurTestStep == mFeederTestUnits.size() - 1 || !isAutoTest) {
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
        intent.putExtra("TestUnits", mFeederTestUnits);
        intent.putExtra("Feeder", mFeeder);
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
                ModuleStateStruct moduleStateStruct = new Gson().fromJson(data, ModuleStateStruct.class);
                boolean result = false;
                StringBuilder desc = new StringBuilder();

                if (moduleStateStruct.getModule() != mFeederTestUnits.get(mCurTestStep).getModule()) {
                    LogcatStorageHelper.addLog("response和request的module不一致！放弃！");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("扩展cpu").append("-").append("通信").append("-").append(moduleStateStruct.getSub0() == 1 ? "正常" : "异常");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("扩展cpu").append("-").append("wifi按键").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if (moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("扩展cpu").append("-").append("food按键").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 5:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("门马达").append("-").append("门方向").append("-").append(moduleStateStruct.getSub0() == 1 ? "开门" : "关门");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            desc.append("\n").append("门马达").append("-").append("门结果").append("-");
                            switch (moduleStateStruct.getSub1()) {
                                case 1:
                                    desc.append("信号已经遮挡");
                                    break;
                                case 2:
                                    desc.append("信号未遮挡");
                                    break;
                                case 3:
                                    desc.append("成功");
                                    mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                                    result = mTempResult == 0x11;
                                    break;
                                case 4:
                                    desc.append("堵转");
                                    break;
                                default:
                                    desc.append("异常");
                                    break;
                            }
                        }
                        if (moduleStateStruct.getSub2() > -1) {
                            desc.append("\n").append("门马达").append("-").append("门信号").append("-").append(moduleStateStruct.getSub2() != 1 ? "遮挡" : "不遮挡");
                        }
                        break;
                    case 6:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("叶轮马达").append("-").append("马达方向").append("-").append(moduleStateStruct.getSub0() == 1 ? "旋转" : "停止");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            desc.append("\n").append("叶轮马达").append("-").append("马达方向").append("-");
                            switch (moduleStateStruct.getSub1()) {
                                case 1:
                                    desc.append("信号已经遮挡");
                                    break;
                                case 2:
                                    desc.append("信号未遮挡");
                                    break;
                                case 3:
                                    desc.append("成功");
                                    showMotorDirectionConfirmDialog();
                                    break;
                                default:
                                    desc.append("异常");
                                    break;
                            }
                        }
                        if (moduleStateStruct.getSub2() > -1) {
                            desc.append("\n").append("叶轮马达").append("-").append("叶轮信号").append("-").append(moduleStateStruct.getSub2() != 1 ? "遮挡" : "不遮挡");
                        }
                        break;
                    case 7:
                        desc.append("\n").append("秤").append("-").append("校准模式").append("-");
                        switch (moduleStateStruct.getSub0()) {
                            case 0:
                                desc.append("空桶");
                                mTempResult = mTempResult | 0x1;
                                break;
                            case 1:
                                desc.append("2KG模式");
                                mTempResult = mTempResult | 0x10;
                                break;
                            case 2:
                                desc.append("4KG模式");
                                mTempResult = mTempResult | 0x100;
                                break;
                            case 3:
                                if (mFeederTestUnits.get(mCurTestStep).getState() == 1 && mTempResult == 0x111) {
                                    desc.append("校准完成");
                                    result = true;
                                }
                                break;
                        }
                        desc.append("\n").append("秤").append("-").append("读取数值").append("-").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("秤").append("-").append("实际克数").append("-").append(moduleStateStruct.getSub2()).append("克");
                        break;
                    case 9:
                        desc.append("\n").append("直流电压").append("-").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() > 5000;
                        break;
                    case 10:
                        desc.append("\n").append("电池电压").append("-").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() > 5000;
                        break;
                    case 14:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("粮盖信号").append("-").append(moduleStateStruct.getSub0() == 1 ? "打开" : "合上");
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
                }
                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });


                if (result) {
                    mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    refershBtnView();
                }
                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if (!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 0:
                                mDescTextView.append("\n写入命令失败");
                                break;
                            case 1:
                                mDescTextView.append("\n写入SN成功");
                                FeederUtils.storeSucceedFeederInfo(mFeeder, mAgeingResult);
                                mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                                refershBtnView();
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
                                mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                if (mFeederTestUnits.get(mCurTestStep).getType() == TEST_MODE_AGEINGRESULT) {
                    mDescTextView.setText(mAgeingResult);
                } else {
                    startSetSn();
                }
                break;
        }
    }

    private void startSetSn() {
        if (isEmpty(mFeeder.getSn()) || (mFeederTestUnits.get(mCurTestStep).getState() == 2
                && mErrorFeeder != null && mFeeder.getSn().equals(mErrorFeeder.getSn()))) {
            boolean result = true;
            for (FeederTestUnit unit : mFeederTestUnits) {
                if (unit.getType() != FeederUtils.FeederTestModes.TEST_MODE_SN &&
                        unit.getType() != FeederUtils.FeederTestModes.TEST_MODE_PRINT
                        && unit.getResult() != TEST_PASS) {
                    result = false;
                    break;
                }
            }

            if (!result) {
                showShortToast("还有未完成的测试项，不能写入SN！");
            } else {
                String sn = FeederUtils.generateSNForTester(mTester);
                if (sn == null) {
                    showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
                    return;
                }
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mFeeder.getMac());
                payload.put("sn", sn);
                if (mFeederTestUnits.get(mCurTestStep).getState() == 2) {
                    payload.put("force", 100);
                }
                mFeeder.setSn(sn);
                mFeeder.setCreation(System.currentTimeMillis());
                PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(161, payload));
            }
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mac", mFeeder.getMac());
            params.put("sn", mFeeder.getSn());
            PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(161, params));
        }
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


    private EditText et1 = null;
    private EditText et2 = null;

    private void showSNSetDialog() {

        if (mFeeder == null || mFeeder.getMac() == null) {
            showShortToast("无效的喂食器");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置SN");
        builder.setCancelable(false);
        builder.setView(initView(mFeeder.getMac(), mFeeder.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();

                if (sn == null || sn.length() != 14) {
                    showShortToast("无效的SN");
                    return;
                }

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                payload.put("force", 100);
                mFeeder.setSn(sn);
                PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(161, payload));
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
        builder.setCancelable(false);
        builder.setMessage("请确认马达是在按顺时针方向转动？");
        builder.setPositiveButton("正确", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                refershBtnView();
            }
        });
        builder.setNegativeButton("错误", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mFeederTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                refershBtnView();
            }
        });
        builder.show();
    }

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
                mFeederTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
        PrintUtils.quit();
        super.onDestroy();
    }
}
