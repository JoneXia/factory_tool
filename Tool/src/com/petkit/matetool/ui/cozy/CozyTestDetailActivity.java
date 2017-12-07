package com.petkit.matetool.ui.cozy;

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
import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.cozy.mode.Cozy;
import com.petkit.matetool.ui.cozy.mode.CozyModuleStateStruct;
import com.petkit.matetool.ui.cozy.mode.CozyTestUnit;
import com.petkit.matetool.ui.cozy.mode.CozyTester;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.feeder.PrintActivity;
import com.petkit.matetool.ui.feeder.utils.PetkitSocketInstance;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.ui.feeder.utils.PrintUtils.isPrinterConnected;
import static com.petkit.matetool.utils.Globals.TEST_FAILED;
import static com.petkit.matetool.utils.Globals.TEST_PASS;

;

/**
 *
 * Created by Jone on 17/4/24.
 */
public class CozyTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private CozyTester mTester;
    private int mCurTestStep;

    //制冷片测试，需要连续4个数据来自动判断测试结果
//    private CozyTestZLPTempSimple mZLPTempSimple;


    private ArrayList<CozyTestUnit> mCozyTestUnits;
    private int mTempResult;
    private Cozy mCozy, mErrorCozy;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mCozyTestUnits = (ArrayList<CozyTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mCozy = (Cozy) savedInstanceState.getSerializable("Cozy");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
            mTester = (CozyTester) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY_TESTER);
            mErrorCozy = (Cozy) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY);
        } else {
            mCozyTestUnits = (ArrayList<CozyTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mCozy = (Cozy) getIntent().getSerializableExtra("Cozy");
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
            mTester = (CozyTester) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY_TESTER);
            mErrorCozy = (Cozy) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY);
        }

        setContentView(R.layout.activity_feeder_test_detail);

//        mZLPTempSimple = new CozyTestZLPTempSimple();
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
        outState.putSerializable("TestUnits", mCozyTestUnits);
        outState.putSerializable("Cozy", mCozy);
        outState.putBoolean("AutoTest", isAutoTest);
        outState.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
        outState.putSerializable(CozyUtils.EXTRA_COZY, mErrorCozy);
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
        setTitle(mCozyTestUnits.get(mCurTestStep).getName());

        mDescTextView.setText("");
        mPromptTextView.setText("");
        switch (mCozyTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_PRINT:
                mDescTextView.setText("mac:" + mCozy.getMac() + "\n" + "sn:" + mCozy.getSn());
                break;
            case TEST_MODE_SN:
                if(!isEmpty(mCozy.getSn())) {
                    if(mCozyTestUnits.get(mCurTestStep).getState() != 2 || (mErrorCozy != null && !mCozy.getSn().equals(mErrorCozy.getSn()))) {
                        mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    }
                    mDescTextView.setText("mac:" + mCozy.getMac() + "\n" + "sn:" + mCozy.getSn());
                } else {
                    mDescTextView.setText("mac:" + mCozy.getMac());
                }
                break;
            case TEST_MODE_KEY:
                mPromptTextView.setText("需要分别测试电源键和wifi按键！");
                break;
            case TEST_MODE_LIGHT:
                mPromptTextView.setText("点击开始，观察灯环颜色依次红绿蓝变化，wifi灯亮起，蜂鸣器响一秒！");
                break;
            case TEST_MODE_COOL:
                mPromptTextView.setText("观察温度值是否在下降！\nP电压       N电压      工作温度  散热温度");
                break;
            case TEST_MODE_HOT:
                mPromptTextView.setText("观察温度值是否在上升！\nP电压       N电压      工作温度  散热温度");
                break;
            case TEST_MODE_VOLTAGE:
                mPromptTextView.setText("观察电压值，在5v ~ 7v之间为正常！");
                break;
            case TEST_MODE_TEMP:
                mPromptTextView.setText("观察当前的显示的温度值，是否在合理的范围！");
                break;
            case TEST_MODE_FAN:
                mPromptTextView.setText("观察风扇正常运行，点击开始按键会在中速、全速和关闭之间切换！");
                break;
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mCozyTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LIGHT:
            case TEST_MODE_TEMP:
            case TEST_MODE_FAN:
            case TEST_MODE_COOL:
            case TEST_MODE_HOT:
                mBtn1.setText(R.string.Start);
                mBtn2.setText(R.string.Failure);
                mBtn2.setBackgroundResource(R.drawable.selector_red);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn3.setText(R.string.Succeed);
                mBtn3.setBackgroundResource(R.drawable.selector_blue);
                break;
//            case TEST_MODE_ZLP:
//                mBtn1.setText("制冷");
//                mBtn2.setText("制热");
//                mBtn2.setBackgroundResource(R.drawable.selector_gray);
//                mBtn2.setVisibility(View.VISIBLE);
//                if (mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
//                    mBtn3.setText(R.string.Succeed);
//                    mBtn3.setBackgroundResource(R.drawable.selector_blue);
//                } else {
//                    mBtn3.setText(R.string.Failure);
//                    mBtn3.setBackgroundResource(R.drawable.selector_red);
//                }
//                break;
            case TEST_MODE_PRINT:
                mBtn1.setText(R.string.Print);
                mBtn2.setText(R.string.Set_print);
                mBtn2.setVisibility(View.VISIBLE);
                mBtn2.setBackgroundResource(R.drawable.selector_gray);
                if(mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if(mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                if (mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
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
                switch (mCozyTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if(isPrinterConnected()) {
                            if(isEmpty(mCozy.getSn())) {
                                showShortToast("SN还未写入，不能打印！");
                            } else if(isEmpty(mCozy.getMac())) {
                                showShortToast("MAC为空，不能打印！");
                            } else {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("SN", mCozy.getSn());
                                params.put("MAC", mCozy.getMac());
                                String oneBarCode = "SN:" + mCozy.getSn();
//                                String twoBarCode = "SN:" + mCozy.getSn() + ";MAC:" + mCozy.getMac();
                                printBarcode(oneBarCode, new Gson().toJson(params));
                            }
                        } else {
                            showShortToast("请先连接打印机！");
                        }
                        break;
                    case TEST_MODE_SN:
                        if(isEmpty(mCozy.getSn()) || (mCozyTestUnits.get(mCurTestStep).getState() == 2
                                        && mErrorCozy != null && mCozy.getSn().equals(mErrorCozy.getSn()))) {
                            boolean result = true;
                            for (CozyTestUnit unit : mCozyTestUnits) {
                                if(unit.getType() != CozyUtils.CozyTestModes.TEST_MODE_SN &&
                                        unit.getType() != CozyUtils.CozyTestModes.TEST_MODE_PRINT
                                        && unit.getResult() != TEST_PASS) {
                                    result = false;
                                    break;
                                }
                            }

                            if(!result) {
                                showShortToast("还有未完成的测试项，不能写入SN！");
                            } else {
                                String sn = CozyUtils.generateSNForTester(mTester);
                                if(sn == null) {
                                    showShortToast("今天生成的SN已经达到上限，上传SN再更换账号才可以继续测试哦！");
                                    return;
                                }
                                HashMap<String, Object> payload = new HashMap<>();
                                payload.put("mac", mCozy.getMac());
                                payload.put("sn", sn);
                                if(mCozyTestUnits.get(mCurTestStep).getState() == 2) {
                                    payload.put("force", 100);
                                }
                                mCozy.setSn(sn);
                                mCozy.setCreation(System.currentTimeMillis());
                                PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(161, payload));
                            }
                        } else {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("mac", mCozy.getMac());
                            params.put("sn", mCozy.getSn());
                            PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(161, params));
                        }
                        break;
                    case TEST_MODE_MAC:
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mCozy.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(165, params));
                        break;
                    case TEST_MODE_RESET_ID:
                        params = new HashMap<>();
                        params.put("mac", mCozy.getMac());
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(162, params));
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mCozyTestUnits.get(mCurTestStep).getType()) {
//                    case TEST_MODE_ZLP: { //制热
//                            HashMap<String, Object> params = new HashMap<>();
//                            params.put("module", mCozyTestUnits.get(mCurTestStep).getModule());
//                            params.put("state", 2);
//                            if(mCozyTestUnits.get(mCurTestStep).getModule() == 11) {
//                                if (!mZLPTempSimple.startHotTest()) {
//                                    showShortToast("当前正在测试，请稍后！");
//                                    return;
//                                }
//                            }
//                            PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
//
//                            mTempResult = 0;
//                            if(mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
//                                mCozyTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
//                                refershBtnView();
//                            }
//                        }
//                        break;
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_FAN:
                    case TEST_MODE_TEMP:
                        isWriteEndCmd = true;
                        mCozyTestUnits.get(mCurTestStep).setResult(TEST_FAILED);

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mCozyTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mCozyTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                    case TEST_MODE_MAC:
                    case TEST_MODE_PRINT:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LIGHT:
                    case TEST_MODE_FAN:
                    case TEST_MODE_TEMP:
                    case TEST_MODE_COOL:
                    case TEST_MODE_HOT:
                        mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    default:
                        isWriteEndCmd = true;
                        if(mCozyTestUnits.get(mCurTestStep).getResult() != TEST_PASS) {
                            mCozyTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
                        }

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mCozyTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
                        break;
                }
                break;
        }
    }

    private void startTestModule() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("module", mCozyTestUnits.get(mCurTestStep).getModule());
        params.put("state", mCozyTestUnits.get(mCurTestStep).getState());
//        if(mCozyTestUnits.get(mCurTestStep).getModule() == 11) {
//            if (!mZLPTempSimple.startCoolTest()) {
//                showShortToast("当前正在测试，请稍后！");
//                return;
//            }
//        }
        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));

        mTempResult = 0;
        if(mCozyTestUnits.get(mCurTestStep).getResult() == TEST_PASS) {
            mCozyTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
            refershBtnView();
        }
    }

    private void gotoNextTestModule() {
        if(mCurTestStep == mCozyTestUnits.size() - 1 || !isAutoTest) {
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
        intent.putExtra("TestUnits", mCozyTestUnits);
        intent.putExtra("Cozy", mCozy);
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
                if(!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 1:
                                if(isWriteEndCmd) {
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
                CozyModuleStateStruct moduleStateStruct = new Gson().fromJson(data, CozyModuleStateStruct.class);
                boolean result = false;
                StringBuilder desc = new StringBuilder();

                if (mCozyTestUnits.get(mCurTestStep).getModule() != 10
                        && mCozyTestUnits.get(mCurTestStep).getModule() != 11
                        && moduleStateStruct.getModule() != mCozyTestUnits.get(mCurTestStep).getModule()) {
                    LogcatStorageHelper.addLog("response和request的module不一致！放弃！");
                    return;
                }

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        if(moduleStateStruct.getSub0() > 0) {
                            desc.append("\n").append("扩展cpu").append("：").append("通信").append("-").append(moduleStateStruct.getSub0() == 1 ? "正常" : "异常");
                        }
                        if(moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("扩展cpu").append("：").append("wifi按键").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if(moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("扩展cpu").append("：").append("功能键").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 6:
                        if(moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("红外信号").append("：").append(moduleStateStruct.getSub0() == 1 ? "遮挡" : "不遮挡");
                            mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                            result = mTempResult == 0x11;
                        }
                        break;
                    case 7:
                        desc.append("\n").append("风扇转速").append("：").append(getFanState(moduleStateStruct.getSub0()));
                        break;
                    case 8:
                        if (mCozyTestUnits.get(mCurTestStep).getType() == CozyUtils.CozyTestModes.TEST_MODE_TEMP) {
                            desc.append("\n").append("工作面板").append("：温度").append(getTempFormat(moduleStateStruct.getSub0())).append("，电压").append(getVoltageFormat(moduleStateStruct.getSub1()));
                        } else {
                            desc.append("      ").append(getTempFormat(moduleStateStruct.getSub0()));
                        }
//                        mZLPTempSimple.addTemp(moduleStateStruct.getSub0());
                        break;
                    case 9:
                        if (mCozyTestUnits.get(mCurTestStep).getType() == CozyUtils.CozyTestModes.TEST_MODE_TEMP) {
                            desc.append("\n").append("散热面板").append("：温度").append(getTempFormat(moduleStateStruct.getSub0())).append("，电压").append(getVoltageFormat(moduleStateStruct.getSub1()));
                        } else {
                            desc.append("      ").append(getTempFormat(moduleStateStruct.getSub0()));
                        }
                        break;
                    case 10:
                        desc.append("\n").append("环境").append("：温度").append(getTempFormat(moduleStateStruct.getSub0())).append("，湿度").append(Math.round(moduleStateStruct.getSub1()/10f)).append("%");
                        break;
                    case 11:
                        desc.append("\n").append(getVoltageFormat(moduleStateStruct.getSub1()))
                                .append("      ").append(getVoltageFormat(moduleStateStruct.getSub2()));
//                        mZLPTempSimple.addVol(moduleStateStruct.getSub0(), moduleStateStruct.getSub1(), moduleStateStruct.getSub2());
                        break;
                    case 12:
                        desc.append("\n").append("直流电压").append("：").append(getVoltageFormat(moduleStateStruct.getSub0()));
                        result = moduleStateStruct.getSub0() >= 5000 && moduleStateStruct.getSub0() <= 7000;
                        break;
                }
//                if (mCozyTestUnits.get(mCurTestStep).getType() == CozyUtils.CozyTestModes.TEST_MODE_ZLP) {
//                    if (!mZLPTempSimple.isTesting()) {
//                        desc.append("\n").append(mZLPTempSimple.isSuccess() ? "当前测试成功" : "当前测试失败");
//                        result = mZLPTempSimple.isTotalComplete();
//
//                        HashMap<String, Object> params = new HashMap<>();
//                        params.put("module", mCozyTestUnits.get(mCurTestStep).getModule());
//                        params.put("state", 0);
//                        PetkitSocketInstance.getInstance().sendString(CozyUtils.getRequestForKeyAndPayload(163, params));
//                    }
//                }

                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                if(result) {
                    mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                    refershBtnView();
                }
                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if(!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 0:
                                mDescTextView.append("\n写入命令失败");
                                break;
                            case 1:
                                mDescTextView.append("\n写入SN成功");
                                CozyUtils.storeSucceedCozyInfo(mCozy);
                                mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
                if(!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 1:
                                mDescTextView.append("\n指令发送成功");
                                mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
        LoadDialog.show(this, "正在打印标签，请稍后……");

        IAtBitmap api = IAtBitmap.Factory.createInstance();

//        api.startJob(48 * 100, 30 * 100);
//        api.draw2DQRCode(twodBarcde, 18 * 100, 2 * 100, 14 * 100);
//        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.AUTO, 6 * 100, 18 * 100, 38 * 100, 10 * 100, 180);
//        api.endJob();
        api.startJob(48 * 100, 30 * 100);
        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
        api.draw2DQRCode(twodBarcde, 16 * 100, 2 * 100, 15 * 100);
        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0 * 100, 18 * 100, 48 * 100, 7 * 100, 0);
        api.drawText(onedBarcde, 0 * 100, 25 * 100, 48 * 100, 3 *100, 280, IAtBitmap.FontStyle.REGULAR);
        api.endJob();

        return IDzPrinter.Factory.getInstance().print(api, getPrintParam());
    }

    private String getZLPmode(int mode) {
        switch (mode) {
            case 0:
                return "关闭";
            case 1:
                return "制冷";
            case 2:
                return "制热";
            default:
                return "异常";
        }
    }

    private String getFanState(int value) {
        switch (value) {
            case 5:
                return "全速";
            case 4:
                return "高速";
            case 3:
                return "中速";
            case 2:
                return "低速";
            case 1:
                return "最低";
            default:
                return "关闭";
        }
    }

    private String getVoltageFormat(int voltage) {
        return String.format("%.3fV", voltage/1000f);
    }

    private String getTempFormat(int temp) {
        return String.format("%.1f℃", temp/10f);
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
                            mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
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
        // 显示打印数据设置界面
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置SN");
        builder.setView(initView(mCozy.getMac(), mCozy.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取打印数据并进行打印
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                mCozy.setSn(sn);
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
        builder.setMessage("请确认马达是在按顺时针方向转动？");
        builder.setPositiveButton("正确", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mCozyTestUnits.get(mCurTestStep).setResult(TEST_PASS);
                refershBtnView();
            }
        });
        builder.setNegativeButton("错误", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                mCozyTestUnits.get(mCurTestStep).setResult(TEST_FAILED);
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

}
