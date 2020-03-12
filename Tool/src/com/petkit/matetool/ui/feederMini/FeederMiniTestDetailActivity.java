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

import com.dothantech.lpapi.IAtBitmap;
import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.feeder.mode.ModuleStateStruct;
import com.petkit.matetool.ui.print.PrintActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.feederMini.mode.FeederMiniTestUnit;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.ui.utils.PrintUtils;
import com.petkit.matetool.utils.DateUtil;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.petkit.matetool.ui.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils.FeederMiniTestModes.TEST_MODE_AGEINGRESULT;
import static com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils.FeederMiniTestModes.TEST_MODE_LIGHT;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

/**
 * Created by Jone on 17/4/24.
 */
public class FeederMiniTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private Tester mTester;
    private int mCurTestStep;
    private ArrayList<FeederMiniTestUnit> mFeederMiniTestUnits;
    private int mTempResult;
    private Feeder mFeeder, mErrorFeeder;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;
    private String mAgeingResult = null;    //老化测试的结果
    private int mTempStep; //有些测试项中会细分成几步

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

        IDzPrinter.Factory.getInstance().init(this, mCallback);
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
                mPromptTextView.setText("需要分别测试food按键和wifi按键！");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("点击开始，观察喂食器两个灯依次亮灭，蜂鸣器响一秒！");
                break;
            case TEST_MODE_DOOR:
                mPromptTextView.setText("需要分别测试开门和关门！");
                break;
            case TEST_MODE_AGEINGRESULT:
                mPromptTextView.setText("观察老化数据，手动判断结果！");
                break;
            case TEST_MODE_IR:
                mPromptTextView.setText("需要分别测试红外信号遮挡和不遮挡！");
                break;
            case TEST_MODE_DC:
            case TEST_MODE_BAT:
                mPromptTextView.setText("正常电压范围（单位mV）：[5000, 7000]");
                break;
            case TEST_MODE_MOTOR:
                mPromptTextView.setText("需要分别测试顺时针旋转和逆时针旋转！");
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
                                showShortToast("SN还未写入，不能打印！");
                            } else if (isEmpty(mFeeder.getMac())) {
                                showShortToast("MAC为空，不能打印！");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mFeeder.getSn());
                                params.put("MAC", mFeeder.getMac());
                                String oneBarCode = "SN:" + mFeeder.getSn();
//                                String twoBarCode = "SN:" + mFeeder.getSn() + ";MAC:" + mFeeder.getMac();
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

                                    switch (mFeederMiniTestUnits.get(mCurTestStep).getType()) {
                                        case TEST_MODE_DOOR:
                                            showShortToast("请注意当前开、关门的状态");
//                                            showDoorDirectionConfirmDialog();
                                            break;
                                        case TEST_MODE_MOTOR:
                                            showShortToast("请注意当前叶轮转动方向");
//                                            showMotorDirectionConfirmDialog();
                                            break;
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
                ModuleStateStruct moduleStateStruct = new Gson().fromJson(data, ModuleStateStruct.class);
                boolean result = false;
                StringBuilder desc = new StringBuilder();

                if (moduleStateStruct.getModule() != mFeederMiniTestUnits.get(mCurTestStep).getModule()) {
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
                                    desc.append("成功");
                                    showDoorDirectionConfirmDialog();
                                    break;
                                default:
                                    desc.append("异常");
                                    break;
                            }
                        }
                        break;
                    case 6:
                        if (moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("叶轮马达").append("-").append("马达方向").append("-").append(moduleStateStruct.getSub0() == 1 ? "顺时针" : "逆时针");
                        }
                        if (moduleStateStruct.getSub1() > 0) {
                            desc.append("\n").append("叶轮马达").append("-").append("马达方向").append("-");
                            switch (moduleStateStruct.getSub1()) {
                                case 1:
                                    desc.append("成功");
                                    showMotorDirectionConfirmDialog();
                                    break;
                                default:
                                    desc.append("异常");
                                    break;
                            }
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
                                if (mFeederMiniTestUnits.get(mCurTestStep).getState() == 1 && mTempResult == 0x111) {
                                    desc.append("校准完成");
                                    result = true;
                                }
                                break;
                        }
                        desc.append("\n").append("秤").append("-").append("读取数值").append("-").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("秤").append("-").append("实际克数").append("-").append(moduleStateStruct.getSub2()).append("克");
                        break;
                    case 9:
                        desc.append("\n").append("直流电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
                        break;
                    case 10:
                        desc.append("\n").append("电池电压").append(":").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
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
                    case 16:
                        desc.append("\n").append("红外信号").append("-").append(moduleStateStruct.getSub0() == 1 ? "遮挡" : "不遮挡");
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
                                mDescTextView.append("\n写入命令失败");
                                break;
                            case 1:
                                mDescTextView.append("\n写入SN成功");

                                FeederMiniUtils.removeTempFeederInfo(mFeeder);
                                FeederMiniUtils.storeSucceedFeederInfo(mFeeder, mAgeingResult);

                                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                                mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                showShortToast("还有未完成的测试项，不能写入SN！");
            } else {
                String sn = FeederMiniUtils.generateSNForTester(mTester);
                if (sn == null) {
                    showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
                    return;
                }
                mFeeder.setSn(sn);
                mFeeder.setCreation(System.currentTimeMillis());

                //写入设备前先存储到临时数据区，写入成功后需删除
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
        return PrintUtils.printText(onedBarcde, twodBarcde, callback);
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

    // 调用IDzPrinter对象的init方法时用到的IDzPrinterCallback对象
    private final IDzPrinter.IDzPrinterCallback mCallback = new IDzPrinter.IDzPrinterCallback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。

        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterState arg1) {
            final IDzPrinter.PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    break;

                case Disconnected:
                    break;

                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(IDzPrinter.ProgressInfo arg0, Object arg1) {
        }


        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(IDzPrinter.PrinterAddress address, Object bitmapData, IDzPrinter.PrintProgress progress, Object addiInfo) {
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            LoadDialog.dismissDialog();

                            mDescTextView.append("\n" + getString(R.string.printsuccess));
                            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                            refershBtnView();
                        }
                    });
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDescTextView.append(getString(R.string.printfailed));
                        }
                    });
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onPrinterDiscovery(IDzPrinter.PrinterAddress address, IDzPrinter.PrinterInfo info) {

        }
    };


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
        builder.setMessage(mTempStep == 0 ? "请确认马达是在按顺时针方向转动？" : "请确认马达是在按逆时针方向转动？");
        builder.setPositiveButton("正确", new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton("错误", new DialogInterface.OnClickListener() {
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
        builder.setMessage(mTempStep == 0 ? "请确认当前正在开门？" : "请确认当前正在关门？");
        builder.setPositiveButton("正确", new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton("错误", new DialogInterface.OnClickListener() {
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


    private final LPAPI.Callback callback = new LPAPI.Callback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。

        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterState arg1) {
            final IDzPrinter.PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    break;

                case Disconnected:
                    break;

                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(IDzPrinter.ProgressInfo arg0, Object arg1) {
        }

        @Override
        public void onPrinterDiscovery(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterInfo arg1) {
        }

        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(IDzPrinter.PrinterAddress address, Object bitmapData, IDzPrinter.PrintProgress progress, Object addiInfo) {
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            LoadDialog.dismissDialog();

                            mDescTextView.append("\n" + getString(R.string.printsuccess));
                            mFeederMiniTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                            refershBtnView();
                        }
                    });
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDescTextView.append(getString(R.string.printfailed));
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    };

}
