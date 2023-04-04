package com.petkit.matetool.ui.mate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.service.AndroidBLEActionService;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.WifiParams;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.FileUtils;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 *
 */
public class MainActivity extends BaseActivity {

    //	 private static final String LOG_TAG = "MainActivity.java";
    private final int TEST_MODE = 4;
    private final int TEST_MODE_DETAIL = 5;

    private boolean AutoTestMode = false;

    private int mCurMode = TEST_MODE;
    private int mCurTestIndex = -1;

    private Button mTestOnButton, mTestOffButton, mTestTempButton;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mBleBroadcastReceiver;
    private TestItemAdapter mAdapter;

    private String mWifiInfoString = "";
    private boolean writeSucceed = true;
    private boolean sn_flag = false;

    private boolean needReceiveBroadcast = true;

    private WifiParams mWifiParams;
    private float volt;
    private ArrayList<String> mTestItem = new ArrayList<String>();
    private int[] mTestSysResult = null;
    private int[] mTestResult = null;
    private int mateStyle;
    private int workStation;
    private int mCurCaseMode;
    private String currentSN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mWifiParams = (WifiParams) savedInstanceState.getSerializable(DatagramConsts.EXTRA_WIFI_PARAMS);
            mCurCaseMode = savedInstanceState.getInt(DatagramConsts.EXTRA_CURRENT_MODE);
            mTestResult = savedInstanceState.getIntArray("TestResult");
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mWifiParams = (WifiParams) getIntent().getSerializableExtra(DatagramConsts.EXTRA_WIFI_PARAMS);
            mCurCaseMode = getIntent().getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, 0);
        }

        setContentView(R.layout.activity_main);

        registerBoradcastReceiver();
        registerBleBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
        outState.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
        outState.putIntArray("TestResult", mTestResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGridView();
    }

    @Override
    protected void onDestroy() {
        mHander.removeCallbacks(mRunnable);
        Utils.sendUnmoniCMD(MainActivity.this);

        if(mCurCaseMode == Globals.FinalTestMode) {
            if(!sn_flag) {
                int num;
                if(mateStyle == Globals.MATE_PRO) {
                    num = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_PRO, 0) + 1;
                    CommonUtils.addSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_PRO, num);
                } else {
                    num = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_STYLE, 0) + 1;
                    CommonUtils.addSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_STYLE, num);
                }
            }
        }

        unregisterBleBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void setupViews() {
        setTitle(mCurCaseMode == Globals.SpotTestMode ? R.string.test_case4 :
                (mCurCaseMode == Globals.FinalTestMode ? R.string.test_case3 : R.string.test_case1));

        mTestOnButton = (Button) findViewById(R.id.test_on);
        mTestOnButton.setOnClickListener(this);
        mTestOffButton = (Button) findViewById(R.id.test_off);
        mTestOffButton.setOnClickListener(this);
        mTestTempButton = (Button) findViewById(R.id.test_temp);
        mTestTempButton.setOnClickListener(this);

        findViewById(R.id.btn_test_succeed).setOnClickListener(this);
        findViewById(R.id.btn_test_failed).setOnClickListener(this);

        findViewById(R.id.Button36).setOnClickListener(new myItemOnClickListener());
        findViewById(R.id.Button37).setOnClickListener(new myItemOnClickListener());

        if(mCurCaseMode == Globals.SpotTestMode){
            findViewById(R.id.Button38).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.Button38)).setText("特殊处理");
            findViewById(R.id.Button38).setOnClickListener(new myItemOnClickListener());
        } else if(mCurCaseMode == Globals.FinalTestMode) {
            findViewById(R.id.Button38).setVisibility(View.VISIBLE);
            findViewById(R.id.Button38).setOnClickListener(new myItemOnClickListener());

            findViewById(R.id.Button40).setVisibility(View.VISIBLE);
            findViewById(R.id.Button40).setOnClickListener(new myItemOnClickListener());
        }

        updateAutoTestView();
        updateLayout(TEST_MODE);

        initTestItem();
        GridView gridView =(GridView)findViewById(R.id.gridView1);
        mAdapter = new TestItemAdapter(this);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurTestIndex = position;
                if(position == mTestItem.size() -1) {
                    Intent intent = new Intent(MainActivity.this, ImageTestActivity.class);
                    intent.putExtra(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
                    intent.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                    intent.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                    intent.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                    startActivityForResult(intent, 0x01);
//                    startActivity(new Intent(MainActivity.this, ImageTestActivity.class));
                } else {
                    updateUnitTestLayout(null);
                    updateLayout(TEST_MODE_DETAIL);
                }
            }
        });

        updateSNstatus();
    }

    private class myItemOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            mTestOnButton.setText(getString(R.string.start_test));
            mTestOffButton.setText(getString(R.string.stop_test));
            mTestTempButton.setVisibility(View.GONE);

            switch (view.getId()) {
                case R.id.Button36:
                    if(mTestResult != null) {
                        for (int i = 0; i < mTestResult.length; i++) {
                            if (mTestResult[i] == 0) {
                                showShortToast(R.string.Hint_test_case_not_compelte);
                                return;
                            }
                        }
                    }

                    if(mCurCaseMode == Globals.FinalTestMode) {
                        WriteSN();
                    } else if(mCurCaseMode == Globals.BoardTestMode) {
                        Utils.sendData(MainActivity.this, Utils.SERVER_TEST_BORAD, false);
                        finish();
                    }
                    break;
                case R.id.Button37:
                    AutoTestMode = !AutoTestMode;
                    if(AutoTestMode && mCurTestIndex == -1){
                        mCurTestIndex = 0;
                    }
                    updateAutoTestView();
                    closePA();
                    break;
                case R.id.Button38:
//				startActivity(new Intent(MainActivity.this, PlayActivity.class));
                    if(mCurCaseMode == Globals.SpotTestMode){
                        new AlertDialog.Builder(MainActivity.this).setTitle("选择操作").setItems(
                                new String[]{"回到调焦", "重写SN"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                try {
                                                    Toast.makeText(MainActivity.this, "请等待写入结果。。。", Toast.LENGTH_LONG).show();
                                                    Utils.receiveWriteData(MainActivity.this);
                                                    Utils.sendData(MainActivity.this, Utils.SERVER_TEST_EXIT_TO_FOCUS, false);
                                                } catch (SocketException | UnknownHostException e) {
                                                    Toast.makeText(MainActivity.this, "写入失败。。。。", Toast.LENGTH_LONG).show();
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case 1:
                                                for (int value : mTestResult ) {
                                                    if (value == 0) {
                                                        showShortToast("还有项目未测试，请确认!!!");
                                                        return;
                                                    }
                                                }
                                                startActivityForResult(new Intent(MainActivity.this, WriteSnActivity.class), 0x12);
                                                break;
                                        }
                                    }
                                }).setNegativeButton(
                                R.string.Cancel, null).show();
                    } else {
                        try {
                            Toast.makeText(MainActivity.this, "请等待写入结果。。。", Toast.LENGTH_LONG).show();
                            Utils.receiveWriteData(MainActivity.this);
                            Utils.sendData(MainActivity.this, Utils.SERVER_TEST_EXIT_TO_FOCUS, false);
                        } catch (SocketException | UnknownHostException e) {
                            Toast.makeText(MainActivity.this, "写入失败。。。。", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.Button39:
                    showExitDialog();
                    break;

                case R.id.Button40:
                    for (int value : mTestResult ) {
                        if (value == 0) {
                            showShortToast("还有项目未测试，请确认!!!");
                            return;
                        }
                    }
                    startActivityForResult(new Intent(MainActivity.this, WriteSnActivity.class), 0x12);
                    break;
            }
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.Prompt)
                .setMessage(R.string.Confirm_clear_all_test)
                .setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Utils.receiveWriteData(MainActivity.this);
                            Utils.sendData(MainActivity.this, Utils.SERVER_TEST_EXIT_TO_BORAD, false);
                        } catch (SocketException | UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void closePA() {
            if((mCurCaseMode == Globals.FinalTestMode || mCurCaseMode == Globals.SpotTestMode) && AutoTestMode) {
                Utils.sendData(MainActivity.this, Utils.IOD_GPIO_CTL_PA_OFF, false);
            }
    }

    private void updateAutoTestView() {
        Button btn = (Button) findViewById(R.id.Button37);
        if(AutoTestMode) {
            btn.setTextColor(getResources().getColor(R.color.green));
        } else {
            btn.setTextColor(getResources().getColor(R.color.black));
        }
    }


    private  Handler mHander = new Handler();

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if(!writeSucceed) {
                showSNdialog(false);
            }
        }
    };

    private void WriteSN() {
        if(isEmpty(currentSN)){
            currentSN = Globals.organizationSN(this, workStation, mateStyle);
        }

        WriteSN(currentSN);
    }

    private void WriteSN(String sn) {
        currentSN = sn;
//            currentSN = Globals.organizationSN(this, workStation, mateStyle);
//            Utils.receiveWriteSNData(MainActivity.this);
        writeSucceed = false;
        Utils.sendData(MainActivity.this, Utils.SERVER_TEST_MODE_WRITE_SN, sn);
        mHander.postDelayed(mRunnable, 6000);
        showShortToast(R.string.Hint_wait_result_for_write_sn);
    }

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(!needReceiveBroadcast) {
                    return;
                }

                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);

                    LoadDialog.dismissDialog();
                    switch (progress) {
                        case DatagramConsts.IOD_ADC_VOLT:
                            if(mTestItem.get(mCurTestIndex).equals("电压ADC")) {
                                updateUnitTestLayout("\n电压：" + data + "V");
                                volt = Float.valueOf(data);

                                try {
                                    String min = CommonUtils.getSysMap(MainActivity.this, Globals.SHARED_VOLT_MIN, "4.0");
                                    String max = CommonUtils.getSysMap(MainActivity.this, Globals.SHARED_VOLT_MAX, "5.3");

                                    float volt_min =  Float.parseFloat(min);
                                    float volt_max = Float.parseFloat(max);

                                    if(volt < volt_min || volt > volt_max) {
                                        Utils.showToast(MainActivity.this, "电压值不符合预设值！");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case DatagramConsts.TESTOK:
                            mTestSysResult[mCurTestIndex] = Globals.TEST_PASS;
                            break;
                        case DatagramConsts.TESTFAILED:
                            mTestSysResult[mCurTestIndex] = Globals.TEST_FAILED;
                            break;
                        case DatagramConsts.IOD_GPIO_KEY_IN:
                            if(mCurTestIndex > 0 && mTestItem.get(mCurTestIndex).equals("RESET按键")) {
                                updateUnitTestLayoutForAdd("1 ");
                            }
                            break;
                        case DatagramConsts.WRITEOK://data is a boolean, true is write ok
                            if(!Boolean.valueOf(data)){
                                showShortToast(R.string.Failure);
                                return;
                            }
                            if(mCurCaseMode != Globals.FinalTestMode) {
                                showLongToast(R.string.Succeed);
                                finish();
                            } else {
                                sn_flag = true;
                                if(!writeSucceed) {
                                    mHander.removeCallbacks(mRunnable);
                                    writeSucceed = true;
                                    showSNdialog(true);
                                    int num;
                                    if (mateStyle == Globals.MATE_PRO) {
                                        num = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_PRO, 0) + 1;
                                        CommonUtils.addSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_PRO, num);
                                    } else {
                                        num = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_STYLE, 0) + 1;
                                        CommonUtils.addSysIntMap(MainActivity.this, Globals.SHARED_FACTORY_NUMBER_STYLE, num);
                                    }
                                    saveSNAsFile(currentSN);
                                    saveResultFile();
                                }
                            }
                            break;
                        case DatagramConsts.WRITESNFAILED:
                            mHander.removeCallbacks(mRunnable);
                            showSNdialog(false);
                            break;
                    }
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @SuppressLint("NewApi")
    private void updateUnitTestLayout(String str) {
        TextView textView = (TextView) findViewById(R.id.mate_test_item);
        if(str != null && !str.isEmpty()) {
            if(mCurTestIndex == mTestItem.size() - 2) {
                textView.setText("==="+mTestItem.get(mCurTestIndex) +"==="
                        + "\n"+mWifiParams.mac+ "\n" + str);
            } else {
                textView.setText("==="+mTestItem.get(mCurTestIndex) +"===" + str);
            }
        } else {
            if(mCurTestIndex == mTestItem.size() - 2) {
                textView.setText("==="+mTestItem.get(mCurTestIndex) +"==="
                        + "\n"+mWifiParams.mac+"\n" + (str==null?"":str));
            } else {
                textView.setText("==="+mTestItem.get(mCurTestIndex) +"===\n");
            }
        }
    }

    @SuppressLint("NewApi")
    private void updateUnitTestLayoutForAdd(String str) {
        TextView textView = (TextView) findViewById(R.id.mate_test_item);
        String s = textView.getText().toString();
        if(str != null && !str.isEmpty()) {
            textView.setText(s + str);
        }
    }

    private void updateLayout(final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.mate_unit_test).setVisibility(View.GONE);
                findViewById(R.id.test_item).setVisibility(View.GONE);

                mCurMode = mode;

                switch (mode) {
                    case TEST_MODE:
                        findViewById(R.id.test_item).setVisibility(View.VISIBLE);
                        updateGridView();
                        isBleConnected = false;

                        closePA();
                        break;
                    case TEST_MODE_DETAIL:
                        findViewById(R.id.mate_unit_test).setVisibility(View.VISIBLE);
                        findViewById(R.id.mate_ble_list).setVisibility(View.GONE);

                        String testName = mTestItem.get(mCurTestIndex);

                        if(testName.equals("云台马达") || testName.equals("逗宠")) {
                            mTestTempButton.setVisibility(View.VISIBLE);
                            mTestTempButton.setText("向右旋转");
                            mTestOnButton.setText("向左旋转");
                        } else if(testName.equals("IR-cut")) {
                            mTestTempButton.setVisibility(View.VISIBLE);
                            mTestOnButton.setText("cut-灯1");
                            mTestTempButton.setText("cut-灯2");
                            return;
                        }else {
                            mTestOnButton.setText("开始测试");
                            mTestTempButton.setVisibility(View.GONE);
                        }

                        sendRequest(true, mCurTestIndex);

                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void updateGridView() {
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurMode == TEST_MODE_DETAIL) {
            sendRequest(false, mCurTestIndex);
            updateLayout(TEST_MODE);
        }else if(mCurMode == TEST_MODE) {
            if(Globals.hasTestResult()) {
                showDialog();
            }else {
                finish();
            }
        }
    }

    private void showDialog() {
        new Builder(this)
                .setMessage(R.string.Confirm_clear_all_test)
                .setTitle(R.string.Prompt)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                MainActivity.this.finish();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).create().show();
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_on:
                sendRequest(true, mCurTestIndex);
                break;
            case R.id.test_off:
                sendRequest(false, mCurTestIndex);
                break;
            case R.id.test_temp:
                    String testName = mTestItem.get(mCurTestIndex);

                    if(testName.equals("云台马达")) {
                        Utils.sendData(this, Utils.IOD_MOTO_PTZ_RIGHT, true);
                    }else if(testName.equals("逗宠")) {
                        Utils.sendData(this, Utils.IOD_MOTO_LAS_RIGHT, true);
                    } else if(testName.equals("IR-cut")) {
                        Utils.sendData(this, Utils.IOD_GPIO_LIGHT_LED2_OFF, true);
                    }

                break;
            case R.id.btn_test_succeed:
                mTestResult[mCurTestIndex] = Globals.TEST_PASS;

                if(AutoTestMode) {
                    sendRequest(false, mCurTestIndex);
                    if((++mCurTestIndex) == mTestItem.size()) {
                        updateLayout(TEST_MODE);
                        return;
                    }

                    if(mCurTestIndex == mTestItem.size() - 1) {
                        Intent intent = new Intent(MainActivity.this, ImageTestActivity.class);
                        intent.putExtra(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
                        intent.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                        intent.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                        intent.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                        startActivityForResult(intent, 0x01);
//                        startActivityForResult(new Intent(MainActivity.this, ImageTestActivity.class), 0x01);
                    } else {
                        updateUnitTestLayout(null);
                        updateLayout(TEST_MODE_DETAIL);
                    }
                } else {
                    updateLayout(TEST_MODE);
                }
                break;
            case R.id.btn_test_failed:
                mTestResult[mCurTestIndex] = Globals.TEST_FAILED;
                sendRequest(false, mCurTestIndex);

//			if(AutoTestMode) {
//			} else {
                updateLayout(TEST_MODE);
//			}
                break;
            default:
                break;
        }
    }
    private void sendRequest(boolean onoff, int position) {
        try {
            String testName = mTestItem.get(position);
            if(!onoff) {
                updateUnitTestLayout(null);
            }

            if(testName.equals("灯带")) {
                Utils.sendData(MainActivity.this, onoff ? Utils.IOD_GPIO_LIGHT_BELT : Utils.IOD_GPIO_LIGHT_BELT_OFF, onoff);
            }else if(testName.equals("版本检测")) {
                updateUnitTestLayout("\n版本号：" + mWifiParams.version);
            }else if(testName.equals("指示灯")) {
                Utils.sendData(this, onoff ? Utils.IOD_GPIO_LIGHT_LED1 : Utils.IOD_GPIO_LIGHT_LED1_OFF, onoff);
            }else if(testName.equals("IR-cut")) {
                Utils.sendData(this, onoff ? Utils.IOD_GPIO_LIGHT_LED2 : Utils.IOD_GPIO_LIGHT_LED2_OFF, onoff);
            }else if(testName.equals("红外灯")) {
                Utils.sendData(this, onoff ? Utils.IOD_GPIO_LIGHT_IR: Utils.IOD_GPIO_LIGHT_IR_OFF, onoff);
            }else if(testName.equals("PA控制")) {
                Utils.sendData(this, onoff ? Utils.IOD_GPIO_CTL_PA: Utils.IOD_GPIO_CTL_PA_OFF, onoff);
            }else if(testName.equals("RESET按键")) {
                Utils.sendData(this, onoff ? Utils.IOD_GPIO_KEY_IN: Utils.IOD_GPIO_KEY_IN_OFF, onoff);
                if(onoff) {
                    Utils.receiveSpecData(this);
                }
            }else if(testName.equals("电压ADC")) {
                Utils.sendData(this, onoff ? Utils.IOD_ADC_VOLT: Utils.IOD_ADC_VOLT_OFF, onoff);
                if(onoff) {
                    Utils.receiveSpecData(this);
                }
            }else if(testName.equals("云台马达")) {
                Utils.sendData(this, onoff ? Utils.IOD_MOTO_PTZ_LEFT:Utils. IOD_MOTO_PTZ_OFF, onoff);
            }else if(testName.equals("逗宠")) {
                Utils.sendData(this, onoff ? Utils.IOD_MOTO_LAS_LEFT:Utils. IOD_MOTO_LAS_OFF, onoff);
            }else if(testName.equals("MIC")) {
                Utils.sendData(this, onoff ? Utils.IOD_MAX_BUTT:Utils. IOD_MAX_BUTT_OFF, onoff);
            }else if(testName.equals("蓝牙")) {
                sendAbortActionBroadcast();
                if(onoff) {
                    mDeviceInfos.clear();
                    mDeviceAdapter = new DeviceListAdapter();
                    ListView list = (ListView) findViewById(R.id.mate_ble_list);
                    list.setVisibility(View.VISIBLE);
                    list.setAdapter(mDeviceAdapter);

                    startScanDevice();
                    updateUnitTestLayout(null);
                }else {
                    findViewById(R.id.mate_ble_list).setVisibility(View.GONE);
                    isBleConnected = false;
                }
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x01 && mTestResult != null && mTestResult.length > 0){
            mTestResult[mCurTestIndex] = resultCode == RESULT_OK ? Globals.TEST_PASS : Globals.TEST_FAILED;
            updateLayout(TEST_MODE);
            mAdapter.notifyDataSetChanged();
        }

        if(resultCode == RESULT_OK) {
            if(requestCode == 0x12) {

                String sn = data.getStringExtra("SN");
                if(!Globals.checkSNValid(sn)){
                    showShortToast("无效的SN");
                } else {
                    WriteSN(sn);
                }
            }
        }
    }

    private void updateSNstatus() {
        mWifiInfoString = "IP:"+ mWifiParams.local_rtp_ip + "\n";
        mWifiInfoString += "端口号:" + mWifiParams.local_port+ "\n";
        mWifiInfoString += "WiFi:" + mWifiParams.ssid + "\n";
        mWifiInfoString += "WiFi信号强度:" + mWifiParams.rsq + "\n";
//		mWifiInfoString += "state=" + mWifiParams.state + "\n";
//		mWifiInfoString += "index=" + mWifiParams.index + "\n";
        mWifiInfoString += "mac:" + mWifiParams.mac + "\n";
        mWifiInfoString += "sn:" + mWifiParams.sn + "\n";
        mWifiInfoString += "测试状态:" + mWifiParams.status + "\n";
        mWifiInfoString += "版本号:\n" + mWifiParams.version + "\n";

        if(mCurCaseMode == Globals.FinalTestMode) {
            mWifiInfoString += "写入sn=" + Globals.organizationSN(this, workStation, mateStyle);
        }

        updateSNstatus("");
    }

    private void updateSNstatus(String str) {
        TextView textView = (TextView) findViewById(R.id.mate_wifi_info);
        textView.setText(mWifiInfoString + str);
    }

    private class TestItemAdapter extends BaseAdapter {
        private Context mContext;

        public TestItemAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            if(mTestItem == null)
                return 0;

            return mTestItem.size();
        }

        @Override
        public String getItem(int position) {
//        	if(mTestItem == null)
//        		return null;
            return mTestItem.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.textView1);
            }else{
                convertView = (TextView) convertView;
            }

            holder.name.setText(getItem(position));

            if(mTestResult[position] ==Globals.TEST_PASS) {
                holder.name.setBackgroundColor(getResources().getColor(R.color.green));
            } else if(mTestResult[position] == Globals.TEST_FAILED) {
                holder.name.setBackgroundColor(getResources().getColor(R.color.red));
            } else {
                holder.name.setBackgroundColor(getResources().getColor(R.color.gray));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView name;
        }
    }

    /*=========================BLE Test =====================*/
    private DeviceInfo mBleDeviceInfo;
    private boolean isBleConnected = false;

    private void registerBleBoradcastReceiver() {
        mBleBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (arg1.getAction().equals(BLEConsts.BROADCAST_PROGRESS)) {
                    int progress = arg1.getIntExtra(BLEConsts.EXTRA_PROGRESS, 0);
                    switch (progress) {
                        case BLEConsts.PROGRESS_SCANING:
                            updateUnitTestLayout("正在搜索蓝牙...");
                            break;
                        case BLEConsts.PROGRESS_CONNECTED:
                            String string =  "BLE连接成功!\n" + mBleDeviceInfo.getName()+" Rssi:"+ mBleDeviceInfo.getRssi();
                            Utils.addTestLog(string+"\n");
                            updateUnitTestLayout(string);
                            isBleConnected = true;
                        case BLEConsts.PROGRESS_CONNECTING:
                            if(!isBleConnected)
                                updateUnitTestLayout("正在尝试连接蓝牙...");
                            break;
//					case BLEConsts.PROGRESS_SCANING_TIMEOUT:
                        case BLEConsts.PROGRESS_SCANING_FAILED:
                            updateUnitTestLayout("搜索蓝牙失败!");
                            break;
                        default:
                            break;
                    }
                } else if (arg1.getAction().equals(BLEConsts.BROADCAST_ERROR)) {
//					updateUnitTestLayout("搜索蓝牙失败!");
                } else if (arg1.getAction().equals(BLEConsts.BROADCAST_SCANED_DEVICE)) {
                    DeviceInfo deviceInfo = (DeviceInfo) arg1.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);

                    Log.e("BROADCAST_SCANED_DEVICE", deviceInfo.getMac() +"////"+ mWifiParams.mac.toUpperCase());
                    if (BLEConsts.PET_HOME.equals(deviceInfo.getName()) || BLEConsts.PET_MATE.equals(deviceInfo.getName())) {
                        mDeviceInfos.add(deviceInfo);
                        mDeviceAdapter.notifyDataSetChanged();

                        if(mWifiParams.mac != null) {
                            if(mWifiParams.mac.equalsIgnoreCase(deviceInfo.getMac())) {
                                //SCANED AIM BLE...
                                mBleDeviceInfo = deviceInfo;
                                updateUnitTestLayout(deviceInfo.getMac() + "   " + deviceInfo.getRssi());
                                int ble_value = -50;
                                if(mCurCaseMode == Globals.BoardTestMode) {
                                    ble_value = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_BLE_VALUE,  -50);
                                } else if(mCurCaseMode == Globals.FinalTestMode || mCurCaseMode == Globals.SpotTestMode) {
                                    ble_value = CommonUtils.getSysIntMap(MainActivity.this, Globals.SHARED_BLE_VALUE2,  -60);
                                }

                                if(deviceInfo.getRssi() < ble_value) {
                                    Utils.showToast(MainActivity.this, "当前蓝牙信号值低于设置标准值！");
                                }
//                                startInitDevice(deviceInfo);
                            }
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEConsts.BROADCAST_PROGRESS);
        filter.addAction(BLEConsts.BROADCAST_ERROR);
        filter.addAction(BLEConsts.BROADCAST_LOG);
        filter.addAction(BLEConsts.BROADCAST_SCANED_DEVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBleBroadcastReceiver, filter);
    }

    private void unregisterBleBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBleBroadcastReceiver);
    }

    private void startScanDevice() {
        BluetoothAdapter adpter=BluetoothAdapter.getDefaultAdapter();
        adpter.enable();

        Bundle bundle = new Bundle();
        bundle.putInt(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_SCAN);
        startBLEAction(bundle);
    }

    private void sendAbortActionBroadcast() {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
    }

//    private void startInitDevice(DeviceInfo deviceInfo) {
//        sendAbortActionBroadcast();
//        Bundle bundle = new Bundle();
//        bundle.putInt(BLEConsts.EXTRA_ACTION,
//                deviceInfo.getDeviceId() == 0 ? BLEConsts.BLE_ACTION_INIT_HS : BLEConsts.BLE_ACTION_CHANGE_HS);
//        bundle.putSerializable(BLEConsts.EXTRA_DEVICE_INFO, deviceInfo);
//        startBLEAction(bundle);
//    }

    private void startBLEAction(Bundle bundle) {
        if (CommonUtils.getAndroidSDKVersion() >= 18) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Utils.showToast(MainActivity.this, "你的手机不支持使用小佩功能");
                return;
            }

            final Intent service = new Intent(this, AndroidBLEActionService.class);
            service.putExtras(bundle);
            startService(service);
        } else {
            Utils.showToast(MainActivity.this, "你的手机不支持使用小佩功能");
        }
    }

    private DeviceListAdapter mDeviceAdapter;
    private List<DeviceInfo> mDeviceInfos = new ArrayList<DeviceInfo>();

    private class DeviceListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDeviceInfos.size();
        }

        @Override
        public DeviceInfo getItem(int arg0) {
            return mDeviceInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            DeviceInfo item = getItem(position);
            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.adapter_ble_list, null);
                holder.device_mac = (TextView) convertView.findViewById(R.id.mate_ble_mac);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.device_mac.setText(item.getMac());
            if(mDeviceInfos.get(position).getMac().equals(mWifiParams.mac)) {
                holder.device_mac.setBackgroundColor(getResources().getColor(R.color.red));
            }else{
                holder.device_mac.setBackgroundColor(getResources().getColor(R.color.white));
            }
            return convertView;
        }

        private class ViewHolder {
            TextView device_mac;
        }
    }

    private void initTestItem() {
        mTestItem.clear();
        if(mateStyle == Globals.MATE_STYLE) {
            for(int i = 0; i < Globals.mTestItemStyle.length; i++) {
                if ((mCurCaseMode == Globals.FinalTestMode || mCurCaseMode == Globals.SpotTestMode)
                        && Globals.mTestItemStyle[i].equals("IR-cut")) {
                    continue;
                }
                mTestItem.add(Globals.mTestItemStyle[i]);
            }
        } else {
            for(int i = 0; i < Globals.mTestItemPro.length; i++) {
                if ((mCurCaseMode == Globals.FinalTestMode || mCurCaseMode == Globals.SpotTestMode)
                        && Globals.mTestItemPro[i].equals("IR-cut")) {
                    continue;
                }
                mTestItem.add(Globals.mTestItemPro[i]);
            }
        }

        mTestResult = new int[mTestItem.size()];
        mTestSysResult = new int[mTestItem.size()];
    }

    @SuppressLint("SimpleDateFormat")
    private String getDateOfToday() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String dateString = format.format(new Date());

        return dateString.substring(2);
    }

    AlertDialog.Builder builder = null;
    private void showSNdialog(boolean status) {
//		AlertDialog.Builder builder = null;
        if(status) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("请正确粘贴SN条码！");
            builder.setMessage(currentSN);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                    MainActivity.this.finish();
                }
            });
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("SN写入失败！");
            builder.setMessage(currentSN);
            builder.setPositiveButton("点击重新写SN",  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,  int which) {
                    writeSucceed = false;
                    dialog.dismiss();
                    WriteSN();
                }
            });
        }
        builder.create().show();
    }


	/* ------------write sn file-----------------*/

    protected void saveSNAsFile(String sn) {
        writeTxtToFile(sn, Globals.localUrl, getDateOfToday() + "_sn.txt");
    }

    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("SN_File", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveResultFile() {
        StringBuffer sb = new StringBuffer();
        sb.append("测试日期:" + Globals.getDateOfToday() + "\n");
        sb.append(getString(R.string.fixture_number) + ":" + workStation + "\n");

        sb.append("测项:");
        if(mCurCaseMode == Globals.BoardTestMode) {
            sb.append("板测\n");
        } else if(mCurCaseMode == Globals.FinalTestMode) {
            sb.append("终测\n");
        }else if(mCurCaseMode == Globals.FocusTestMode) {
            sb.append("调焦测试\n");
        } else if(mCurCaseMode == Globals.FocusTestMode2) {
            sb.append("对焦测试\n");
        }else if(mCurCaseMode == Globals.SpotTestMode) {
            sb.append("抽测\n");
        }

        sb.append("测试结果 begin:===================\n");

        if(mTestResult != null && mTestResult.length > 0) {
            for (int i = 0; i < mTestResult.length; i++) {
                sb.append(mTestItem.get(i) + "：(" + mTestResult[i] + "," + mTestResult[i] + ")\n");
                if (mTestResult[i] == 0) {
                    showShortToast("还有项目未测试，请确认!!!");
                    return;
                }
            }
        }
        sb.append("测试结果 end:===================\n");

        String filePath = Globals.localUrl + /*System.currentTimeMillis()*/mWifiParams.mac.toLowerCase() + "_testReport.txt";
        FileUtils.writeStringToFile(filePath, sb.toString());
        showLongToast("保存成功！保存路径:" + filePath);
    }
}