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
import com.dothantech.printer.IDzPrinter;
import com.google.gson.Gson;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Feeder;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.mode.FeederTestUnit;
import com.petkit.matetool.ui.feeder.mode.ModuleStateStruct;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.ui.feeder.utils.PetkitSocketInstance;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.ui.feeder.utils.PrintUtils.isPrinterConnected;

/**
 *
 * Created by Jone on 17/4/24.
 */
public class FeederTestDetailActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private int mCurTestStep;
    private ArrayList<FeederTestUnit> mFeederTestUnits;
    private int mTempResult;
    private Feeder mFeeder;
    private boolean isWriteEndCmd = false;
    private boolean isAutoTest = false;

    private TextView mDescTextView, mPromptTextView;
    private Button mBtn1, mBtn2, mBtn3;
    private ScrollView mDescScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mFeederTestUnits = (ArrayList<FeederTestUnit>) savedInstanceState.getSerializable("TestUnits");
            mCurTestStep = savedInstanceState.getInt("CurrentTestStep");
            mFeeder = (Feeder) savedInstanceState.getSerializable("Feeder");
            isAutoTest = savedInstanceState.getBoolean("AutoTest");
        } else {
            mFeederTestUnits = (ArrayList<FeederTestUnit>) getIntent().getSerializableExtra("TestUnits");
            mCurTestStep = getIntent().getIntExtra("CurrentTestStep", 0);
            mFeeder = (Feeder) getIntent().getSerializableExtra("Feeder");
            isAutoTest = getIntent().getBooleanExtra("AutoTest", true);
        }

        setContentView(R.layout.activity_feeder_test_detail);

        IDzPrinter.Factory.getInstance().init(this, mCallback);
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
        outState.putSerializable("TestUnits", mFeederTestUnits);
        outState.putSerializable("Feeder", mFeeder);
        outState.putBoolean("AutoTest", isAutoTest);
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
                if(!isEmpty(mFeeder.getSn())) {
                    mFeederTestUnits.get(mCurTestStep).setResult(1);
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
            default:
                break;
        }

        refershBtnView();
    }

    private void refershBtnView() {
        switch (mFeederTestUnits.get(mCurTestStep).getType()) {
            case TEST_MODE_LIGHT:
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
                if(mFeederTestUnits.get(mCurTestStep).getResult() == 1) {
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
                if(mFeederTestUnits.get(mCurTestStep).getResult() == 1) {
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
                if(mFeederTestUnits.get(mCurTestStep).getResult() == 1) {
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
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_PRINT:
                        if(isPrinterConnected()) {
                            if(isEmpty(mFeeder.getSn())) {
                                showShortToast("SN还未写入，不能打印！");
                            } else if(isEmpty(mFeeder.getMac())) {
                                showShortToast("MAC为空，不能打印！");
                            } else {
                                String oneBarCode = "SN:" + mFeeder.getSn();
                                String twoBarCode = "SN:" + mFeeder.getSn() + ",MAC:" + mFeeder.getMac();
                                printBarcode(getString(R.string.defaulttextone), oneBarCode, twoBarCode);
                            }
                        } else {
                            showShortToast("请先连接打印机！");
                        }
                        break;
                    case TEST_MODE_SN:
                        if(isEmpty(mFeeder.getSn())) {
                            boolean result = true;
                            for (FeederTestUnit unit : mFeederTestUnits) {
                                if(unit.getResult() != 1) {
                                    result = false;
                                    break;
                                }
                            }

                            if(!result) {
                                showShortToast("还有未完成的测试项，不能写入SN！");
                            } else {
                                showSNSetDialog();
                            }
                        } else {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("mac", mFeeder.getMac());
                            params.put("sn", mFeeder.getSn());
                            PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(161, params));
                        }
                        break;
                    default:
                        startTestModule();
                        break;
                }
                break;
            case R.id.test_btn_2:
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_LIGHT:
                        isWriteEndCmd = true;

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("module", mFeederTestUnits.get(mCurTestStep).getModule());
                        params.put("state", 0);
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(163, params));
                        break;
                    case TEST_MODE_PRINT:
                        startActivity(PrintActivity.class);
                        break;
                }
                break;
            case R.id.test_btn_3:
                switch (mFeederTestUnits.get(mCurTestStep).getType()) {
                    case TEST_MODE_SN:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_PRINT:
                        gotoNextTestModule();
                        break;
                    case TEST_MODE_LIGHT:
                        mFeederTestUnits.get(mCurTestStep).setResult(1);
                    default:
                        isWriteEndCmd = true;

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
        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(163, params));
    }

    private void gotoNextTestModule() {
        if(mCurTestStep == mFeederTestUnits.size() - 1 || !isAutoTest) {
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
                        if(jsonObject.getInt("state") == 1) {
                            if(isWriteEndCmd) {
                                isWriteEndCmd = false;
                                gotoNextTestModule();
                            } else {
                                mDescTextView.append("\n指令发送成功！");
                            }
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

                switch (moduleStateStruct.getModule()) {
                    case 0:
                        if(moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("扩展cpu").append("-").append("通信").append("-").append(moduleStateStruct.getSub0() == 1 ? "正常" : "异常");
                        }
                        if(moduleStateStruct.getSub1() > 0) {
                            mTempResult = mTempResult | 0x1;
                            desc.append("\n").append("扩展cpu").append("-").append("wifi按键").append("-").append(getKeyDescByState(moduleStateStruct.getSub1()));
                        }
                        if(moduleStateStruct.getSub2() > 0) {
                            mTempResult = mTempResult | 0x10;
                            desc.append("\n").append("扩展cpu").append("-").append("food按键").append("-").append(getKeyDescByState(moduleStateStruct.getSub2()));
                        }
                        result = mTempResult == 0x11;
                        break;
                    case 5:
                        if(moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("门马达").append("-").append("门方向").append("-").append(moduleStateStruct.getSub0() == 1 ? "开门" : "关门");
                        }
                        if(moduleStateStruct.getSub1() > 0) {
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
                        if(moduleStateStruct.getSub2() > -1) {
                            desc.append("\n").append("门马达").append("-").append("门信号").append("-").append(moduleStateStruct.getSub2() == 1 ? "遮挡" : "不遮挡");
                        }
                        break;
                    case 6:
                        if(moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("叶轮马达").append("-").append("马达方向").append("-").append(moduleStateStruct.getSub0() == 1 ? "旋转" : "停止");
                        }
                        if(moduleStateStruct.getSub1() > 0) {
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
                                    result = true;
                                    break;
                                default:
                                    desc.append("异常");
                                    break;
                            }
                        }
                        if(moduleStateStruct.getSub2() > -1) {
                            desc.append("\n").append("叶轮马达").append("-").append("叶轮信号").append("-").append(moduleStateStruct.getSub2() == 1 ? "遮挡" : "不遮挡");
                        }
                        break;
                    case 7:
                        desc.append("\n").append("秤").append("-").append("校准模式").append("-");
                        switch (moduleStateStruct.getSub0()) {
                            case 0:
                                desc.append("空桶");
                                break;
                            case 1:
                                desc.append("2KG模式");
                                break;
                            case 2:
                                desc.append("4KG模式");
                                break;
                            case 3:
                                desc.append("校准完成");
                                result = true;
                                break;
                        }
                        desc.append("\n").append("秤").append("-").append("读取数值").append("-").append(moduleStateStruct.getSub1());
                        desc.append("\n").append("秤").append("-").append("实际克数").append("-").append(moduleStateStruct.getSub2()).append("克");
                        break;
                    case 9:
                        desc.append("\n").append("直流电压").append("-").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() > 6000;
                        break;
                    case 10:
                        desc.append("\n").append("电池电压").append("-").append(moduleStateStruct.getSub0()).append("mv");
                        result = moduleStateStruct.getSub0() > 5000;
                        break;
                    case 14:
                        if(moduleStateStruct.getSub0() > -1) {
                            desc.append("\n").append("粮盖信号").append("-").append(moduleStateStruct.getSub0() == 1 ? "打开" : "合上");
                            mTempResult = mTempResult | (moduleStateStruct.getSub0() == 1 ? 0x1 : 0x10);
                            result = mTempResult == 0x11;
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


                if(result) {
                    mFeederTestUnits.get(mCurTestStep).setResult(1);
                    refershBtnView();
                }
                break;
            case 161:
                jsonObject = JSONUtils.getJSONObject(data);
                if(!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 0:
                                mDescTextView.append("\n数据命令失败");
                                break;
                            case 1:
                                mDescTextView.append("\n写入SN成功");
                                mFeederTestUnits.get(mCurTestStep).setResult(1);
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
        }
    }

    private Bundle getPrintParam() {
        Bundle param = new Bundle();

        param.putInt(IDzPrinter.PrintParamName.PRINT_DIRECTION, 0);
        param.putInt(IDzPrinter.PrintParamName.PRINT_COPIES, 1);
        param.putInt(IDzPrinter.PrintParamName.GAP_TYPE, 2);
        param.putInt(IDzPrinter.PrintParamName.PRINT_DENSITY, 14);
        param.putInt(IDzPrinter.PrintParamName.PRINT_SPEED, 2);
        return param;
    }

    private boolean printBarcode(String text, String onedBarcde, String twodBarcde) {
        IAtBitmap api = IAtBitmap.Factory.createInstance();

        api.startJob(50 * 100, 30 * 100);
        api.draw2DQRCode(twodBarcde, 4 * 100, 4 * 100, 16 * 100);
        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.AUTO, 22 * 100, 4 * 100, 24 * 100, 16 * 100, 190);
        api.drawText(text, 4 * 100, 22 * 100, 40 * 100, 10 * 100, 4 * 100, IAtBitmap.FontStyle.REGULAR);
        api.endJob();

        return IDzPrinter.Factory.getInstance().print(api, getPrintParam());
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
                            mDescTextView.append(getString(R.string.printsuccess));
                            mFeederTestUnits.get(mCurTestStep).setResult(1);
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
        builder.setView(initView(mFeeder.getMac(), mFeeder.getSn()));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取打印数据并进行打印
                String mac = et1.getText().toString();
                String sn = et2.getText().toString();
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("mac", mac);
                payload.put("sn", sn);
                mFeeder.setSn(sn);
                PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(161, payload));
            }
        });
        builder.setNegativeButton(R.string.Cancel, null);
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
