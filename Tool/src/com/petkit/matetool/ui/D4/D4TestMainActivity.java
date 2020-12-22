package com.petkit.matetool.ui.D4;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D4.mode.D4TestUnit;
import com.petkit.matetool.ui.D4.utils.D4Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.utils.WifiAdminSimple;
import com.petkit.matetool.ui.wifi.WifiManagerActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Created by Jone on 17/4/24.
 */
public class D4TestMainActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private Tester mTester;
    private int mTestType;

    private WifiAdminSimple mWifiAdminSimple;
    private int mTestState;
    private Device mCurDevice, mErrorDevice;

    private ArrayList<D4TestUnit> mD4TestUnits;
    private TestItemAdapter mAdapter;

    private TextView mInfoTestTextView;
    private boolean testComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(D4Utils.EXTRA_D4_TESTER);
            mTestType = savedInstanceState.getInt("TestType");
            mErrorDevice = (Device) savedInstanceState.getSerializable(D4Utils.EXTRA_D4);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(D4Utils.EXTRA_D4_TESTER);
            mTestType = getIntent().getIntExtra("TestType", D4Utils.TYPE_TEST);
            mErrorDevice = (Device) getIntent().getSerializableExtra(D4Utils.EXTRA_D4);
        }

        setContentView(R.layout.activity_feeder_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
        outState.putInt("TestType", mTestType);
        outState.putSerializable(D4Utils.EXTRA_D4, mErrorDevice);
    }

    @Override
    protected void setupViews() {
        setTitle("行星喂食器");

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_wifi).setOnClickListener(this);
        findViewById(R.id.connect_dev).setOnClickListener(this);
        findViewById(R.id.test_auto).setOnClickListener(this);

        mD4TestUnits = D4Utils.generateD4TestUnitsForType(mTestType);

        GridView gridView =(GridView) findViewById(R.id.gridView);
        mAdapter = new TestItemAdapter(this);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startTestDetail(false, position);
            }
        });

        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);
    }

    @Override
    public void finish() {

        int position = 0;
        for (D4TestUnit unit : mD4TestUnits) {
            if(unit.getResult() == 1) {
                position++;
            } else {
                break;
            }
        }
        if(position == mD4TestUnits.size()) {
            setResult(RESULT_OK);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LoadDialog.dismissDialog();
        disconnectAp();
        unregisterBroadcastReceiver();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_wifi:
//                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                showWifiManager();
                break;
            case R.id.connect_dev:
                refreshView();
//                testSN();
//                startActivity(PrintActivity.class);
                break;
            case R.id.test_auto:
                if (testComplete) {
                    LoadDialog.show(this);
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("mac", mCurDevice.getMac());
                    params.put("state", getTestTypeCode());
                    params.put("opt", 1);
//
                    PetkitSocketInstance.getInstance().sendString(D4Utils.getRequestForKeyAndPayload(160, params));
//                    finish();
                } else {
                    startTestDetail(true, 0);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurDevice != null && testComplete) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.Prompt)
                    .setMessage("测试已完成，请先点击确认来完成测试项目！")
                    .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadDialog.show(D4TestMainActivity.this);
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("mac", mCurDevice.getMac());
                            params.put("state", getTestTypeCode());
                            params.put("opt", 1);

                            PetkitSocketInstance.getInstance().sendString(D4Utils.getRequestForKeyAndPayload(160, params));
//                            finish();
                        }
                    })
                    .show();
        } else if (mCurDevice != null && mTestType == D4Utils.TYPE_CHECK) {
            boolean hasError = false;
            for (D4TestUnit unit : mD4TestUnits) {
                if(unit.getResult() == Globals.TEST_FAILED) {
                    hasError = true;
                    break;
                }
            }

            if (hasError) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.Prompt)
                        .setMessage("当前抽检结果异常，退出将记录异常数据，确认吗？")
                        .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurDevice.setInspectStatus(0);
                                D4Utils.storeCheckInfo(mCurDevice);
                                mCurDevice = null;
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0x12:
                    mD4TestUnits = (ArrayList<D4TestUnit>) data.getSerializableExtra("TestUnits");
                    mCurDevice = (Device) data.getSerializableExtra(D4Utils.EXTRA_D4);
                    mAdapter.notifyDataSetChanged();
                    checkTestComplete();
                    refreshBottomButton();
                    break;
            }
        }
    }

    private void startTestDetail(boolean isAuto, int pos) {
        if(mTestState == TEST_STATE_CONNECTED) {
            if(mCurDevice == null) {
                return;
            }

            if(isAuto) {
                int position = 0;

                for (D4TestUnit unit : mD4TestUnits) {
                    if (unit.getResult() == 1) {
                        position++;
                    } else {
                        break;
                    }
                }
                if (position == mD4TestUnits.size()) {
                    showShortToast("测试已完成");
                    return;
                }
                pos = position;
            }

            Intent intent = new Intent(D4TestMainActivity.this, D4TestDetailActivity.class);
            intent.putExtra("TestUnits", mD4TestUnits);
            intent.putExtra("CurrentTestStep", pos);
            intent.putExtra(D4Utils.EXTRA_D4, mCurDevice);
            intent.putExtra("AutoTest", isAuto);
            intent.putExtra(D4Utils.EXTRA_D4_TESTER, mTester);
            intent.putExtra(D4Utils.EXTRA_ERROR_D4, mErrorDevice);
            startActivityForResult(intent, 0x12);
        } else {
            showShortToast(mInfoTestTextView.getText().toString());
        }
    }

    private void refreshView() {
        String apSsid = mWifiAdminSimple.getWifiConnectedSsid();
        if(apSsid == null) {
            mInfoTestTextView.setText("请先连接到特定的WIFI，再进行测试！");
        } else {
            switch (mTestType) {
                case D4Utils.TYPE_TEST_PARTIALLY:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_A_HW1_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_4_A_HW1_开头的WIFI，再进行测试！");
                    } else {
                        connectAp();
                    }
                    break;
                case D4Utils.TYPE_TEST:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_B_HW1_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_4_B_HW1_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case D4Utils.TYPE_MAINTAIN:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_4_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case D4Utils.TYPE_CHECK:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_HW1_")
                            || (apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_A_") || apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_B_"))) {
                        mInfoTestTextView.setText("请先连接到<PETKIT_FEEDER_4_HW1_>开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case D4Utils.TYPE_DUPLICATE_MAC:
                case D4Utils.TYPE_DUPLICATE_SN:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_4_HW1_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_4_HW1_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void showWifiManager() {
        switch (mTestType) {
            case D4Utils.TYPE_TEST_PARTIALLY:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_4_A_HW1_"));
                break;
            case D4Utils.TYPE_TEST:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_4_B_HW1_"));
                break;
            case D4Utils.TYPE_MAINTAIN:
            case D4Utils.TYPE_CHECK:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_4_"));
                break;
            case D4Utils.TYPE_DUPLICATE_MAC:
            case D4Utils.TYPE_DUPLICATE_SN:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_4_HW1_"));
                break;
        }
    }

    private void refreshBottomButton () {
        ((TextView) findViewById(R.id.test_auto)).setText(testComplete ? "测试完成" : "自动模式");
    }

    private void connectAp() {
        if(mTestState != TEST_STATE_CONNECTED && !PetkitSocketInstance.getInstance().isConnected()) {
            String remoteIp = mWifiAdminSimple.getCurrentApHostIp();
            if(isEmpty(remoteIp)) {
                mInfoTestTextView.setText("获取设备IP失败！");
                mTestState = TEST_STATE_INVALID;
            } else if(mTestState != TEST_STATE_CONNECTING){
                mInfoTestTextView.setText("正在连接设备");
                mTestState = TEST_STATE_CONNECTING;

                PetkitSocketInstance.getInstance().startConnect(remoteIp, 8001);
            }
        } else {
            mInfoTestTextView.setText("可以开始测试啦");
            mTestState = TEST_STATE_CONNECTED;
        }
    }

    private void disconnectAp() {
        PetkitSocketInstance.getInstance().disconnect();
        mTestState = TEST_STATE_INVALID;
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBoradcastReceiver(){
        mBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (arg1.getAction()) {
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        refreshView();
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver(){
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onConnected() {
        mInfoTestTextView.setText("设备已连接");
        mTestState = TEST_STATE_CONNECTED;
        mD4TestUnits = D4Utils.generateD4TestUnitsForType(mTestType);
        mAdapter.notifyDataSetChanged();

        PetkitSocketInstance.getInstance().sendString(D4Utils.getDefaultRequestForKey(110));
    }

    @Override
    public void onDisconnected() {
        mInfoTestTextView.append("\n设备已断开连接");
        mTestState = TEST_STATE_INVALID;
    }

    @Override
    public void onResponse(int key, String data) {
        switch (key) {
            case 110:
                try {
                    JSONObject jsonObject = JSONUtils.getJSONObject(data);
                    StringBuilder stringBuilder = new StringBuilder();
                    String mac = null, sn = null, chipid = null;
                    if (!jsonObject.isNull("mac")) {
                        mac = jsonObject.getString("mac");
                        stringBuilder.append("\n").append("mac: ").append(mac).append("\n");
                    }
                    if (!jsonObject.isNull("sn")) {
                        sn = jsonObject.getString("sn");
                        stringBuilder.append("sn: ").append(sn).append("\n");
                    }
                    if (!jsonObject.isNull("chipid")) {
                        chipid = jsonObject.getString("chipid");
                        stringBuilder.append("chipid: ").append(chipid).append("\n");
                    }
                    if (!jsonObject.isNull("hardware")) {
                        stringBuilder.append("hardware: ").append(jsonObject.getInt("hardware")).append("\n");
                    }
                    if (!jsonObject.isNull("version")) {
                        stringBuilder.append("version: ").append(jsonObject.getString("version")).append("\n");
                    }
//                    if (!jsonObject.isNull("id")) {
//                        stringBuilder.append("id: ").append(jsonObject.getInt("id")).append("\n");
//                    }

                    if(isEmpty(mac)) {
                        mInfoTestTextView.setText("设备信息不正确，没有MAC地址！");
                        PetkitSocketInstance.getInstance().disconnect();
                        return;
                    }

                    if(sn == null && D4Utils.checkMacIsDuplicate(mac)) {
                        mInfoTestTextView.setText("设备MAC出现重复，该设备属于故障设备，不能正常测试！");
                        PetkitSocketInstance.getInstance().disconnect();
                        return;
                    }

                    if(mErrorDevice != null) {
                        if(!mErrorDevice.getMac().equals(mac)) {
                            mInfoTestTextView.setText("设备信息不匹配，需要链接的设备MAC为：" + mErrorDevice.getMac());
                            PetkitSocketInstance.getInstance().disconnect();
                            return;
                        }
                    }

                    mCurDevice = new Device(mac, sn, chipid);

                    mInfoTestTextView.append(stringBuilder.toString());

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("mac", mCurDevice.getMac());
                    params.put("state", getTestTypeCode());
                    params.put("opt", 0);
                    PetkitSocketInstance.getInstance().sendString(D4Utils.getRequestForKeyAndPayload(160, params));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 160:
                if(!testComplete) {
//                    //最近写入SN后，没有及时收到写入成功的通知，需补打印条码
//                    if(D4Utils.isDeviceInTemp(mCurDevice)) {
//                        LogcatStorageHelper.addLog("检测到该设备写入SN时异常: " + mCurDevice.toString());
//                        D4Utils.removeTempDeviceInfo(mCurDevice);
//                        D4Utils.storeSucceedDeviceInfo(mCurDevice, "");
//                        showDeviceInTempDialog();
//                    }
                    return;
                }

                LoadDialog.dismissDialog();
                JSONObject jsonObject = JSONUtils.getJSONObject(data);
                if(!jsonObject.isNull("state")) {
                    try {
                        switch (jsonObject.getInt("state")) {
                            case 1:
                                testComplete = false;
                                showShortToast("测试完成");
                                finish();
                                break;
                            default:
                                showShortToast("指令写入失败！");
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private void showDeviceInTempDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.Prompt)
                .setMessage("检测到该设备写入SN时异常，已自动保存设备信息，请记得打印标签。")
                .setCancelable(false)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }


    private int getTestTypeCode() {
        switch (mTestType) {
            case D4Utils.TYPE_TEST:
            case D4Utils.TYPE_TEST_PARTIALLY:
                return 1;
            case D4Utils.TYPE_MAINTAIN:
                return 2;
            case D4Utils.TYPE_CHECK:
                return 3;
            default:
                return 4;
        }
    }

    private class TestItemAdapter extends BaseAdapter {
        private Context mContext;

        public TestItemAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mD4TestUnits.size();
        }

        @Override
        public D4TestUnit getItem(int position) {
            return mD4TestUnits.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.textView1);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            D4TestUnit item = getItem(position);

            holder.name.setText(item.getName());

            switch (item.getResult()) {
                case Globals.TEST_PASS:
                    holder.name.setBackgroundResource(R.drawable.selector_blue);
                    break;
                case Globals.TEST_FAILED:
                    holder.name.setBackgroundResource(R.drawable.selector_red);
                    break;
                default:
                    holder.name.setBackgroundResource(R.drawable.selector_gray);
                    break;
            }

            return convertView;
        }

        private class ViewHolder {
            TextView name;
        }
    }


    private void checkTestComplete() {
        int position = 0;
        for (D4TestUnit unit : mD4TestUnits) {
            if(unit.getResult() == 1) {
                position++;
            } else {
                break;
            }
        }

        if(position >= mD4TestUnits.size() - 1) {       //维修和抽检，最后一项打印标签可以不执行，其他项都完成了就算成功
            if (mTestType == D4Utils.TYPE_MAINTAIN) {
                D4Utils.storeMainTainInfo(mCurDevice);
                testComplete = position >= mD4TestUnits.size();
            } else if (mTestType == D4Utils.TYPE_CHECK) {
                mCurDevice.setInspectStatus(1);
                D4Utils.storeCheckInfo(mCurDevice);
                testComplete = position >= mD4TestUnits.size();
            } else if (mTestType == D4Utils.TYPE_TEST_PARTIALLY) {
                testComplete = position >= mD4TestUnits.size();
            } else if (mTestType == D4Utils.TYPE_TEST) {
                testComplete = position >= mD4TestUnits.size();
            }
        }
    }

    @Override
    public void cancel(View view) {
        onBackPressed();
    }

    private void checkPermission() {
        WifiManager mWifiManager = (WifiManager) getApplication().getApplicationContext().getSystemService(WIFI_SERVICE);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            showLongToast("请先打开手机Wi-Fi");
            return;
        }
        // 判断GPS模块是否开启，如果没有则跳转至设置开启界面，设置完毕后返回到首页
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showWifiPermissionDialog();
        }
    }

    private void showWifiPermissionDialog() {

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.Prompt)
                .setMessage("无法获取WiFi信息，Android系统要求打开GPS定位服务才能获取到WiFi信息，请开启。")
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 转到手机设置界面，用户设置GPS
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                    }
                })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }



}
