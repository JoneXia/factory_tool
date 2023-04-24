package com.petkit.matetool.ui.feederMini;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feederMini.mode.FeederMiniTestUnit;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
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
public class FeederMiniTestMainActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private Tester mTester;
    private int mTestType;

    private WifiAdminSimple mWifiAdminSimple;
    private int mTestState;
    private Feeder mCurFeeder, mErrorFeeder;

    private ArrayList<FeederMiniTestUnit> mFeederMiniTestUnits;
    private TestItemAdapter mAdapter;

    private TextView mInfoTestTextView;
    private boolean testComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
            mTestType = savedInstanceState.getInt("TestType");
            mErrorFeeder = (Feeder) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
            mTestType = getIntent().getIntExtra("TestType", FeederMiniUtils.TYPE_TEST);
            mErrorFeeder = (Feeder) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER);
        }

        setContentView(R.layout.activity_feeder_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
        outState.putInt("TestType", mTestType);
        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER, mErrorFeeder);
    }

    @Override
    protected void setupViews() {
        setTitle("喂食器测试");

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_wifi).setOnClickListener(this);
        findViewById(R.id.connect_dev).setOnClickListener(this);
        findViewById(R.id.test_auto).setOnClickListener(this);

        mFeederMiniTestUnits = FeederMiniUtils.generateFeederMiniTestUnitsForType(mTestType);

        GridView gridView =(GridView) findViewById(R.id.gridView);
        mAdapter = new TestItemAdapter(this);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startTestDetail(false, position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshView();
        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);
    }

    @Override
    public void finish() {

        int position = 0;
        for (FeederMiniTestUnit unit : mFeederMiniTestUnits) {
            if(unit.getResult() == 1) {
                position++;
            } else {
                break;
            }
        }
        if(position == mFeederMiniTestUnits.size()) {
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
                    params.put("mac", mCurFeeder.getMac());
                    params.put("state", getTestTypeCode());
                    params.put("opt", 1);

                    PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(160, params));
                } else {
                    startTestDetail(true, 0);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurFeeder != null && testComplete) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.Prompt)
                    .setMessage("测试已完成，请先点击确认来完成测试项目！")
                    .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadDialog.show(FeederMiniTestMainActivity.this);
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("mac", mCurFeeder.getMac());
                            params.put("state", getTestTypeCode());
                            params.put("opt", 1);

                            PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(160, params));
                        }
                    })
                    .show();
        } else if (mCurFeeder != null && mTestType == FeederMiniUtils.TYPE_CHECK) {
            boolean hasError = false;
            for (FeederMiniTestUnit unit : mFeederMiniTestUnits) {
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
                                mCurFeeder.setInspectStatus(0);
                                FeederMiniUtils.storeCheckInfo(mCurFeeder);
                                mCurFeeder = null;
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
                    mFeederMiniTestUnits = (ArrayList<FeederMiniTestUnit>) data.getSerializableExtra("TestUnits");
                    mCurFeeder = (Feeder) data.getSerializableExtra("Feeder");
                    mAdapter.notifyDataSetChanged();
                    checkTestComplete();
                    refreshBottomButton();
                    break;
            }
        }
    }

    private void startTestDetail(boolean isAuto, int pos) {
        if(mTestState == TEST_STATE_CONNECTED) {
            if(mCurFeeder == null) {
                return;
            }

            if(isAuto) {
                int position = 0;

                for (FeederMiniTestUnit unit : mFeederMiniTestUnits) {
                    if (unit.getResult() == 1) {
                        position++;
                    } else {
                        break;
                    }
                }
                if (position == mFeederMiniTestUnits.size()) {
                    showShortToast("测试已完成");
                    return;
                }
                pos = position;
            }

            Intent intent = new Intent(FeederMiniTestMainActivity.this, FeederMiniTestDetailActivity.class);
            intent.putExtra("TestUnits", mFeederMiniTestUnits);
            intent.putExtra("CurrentTestStep", pos);
            intent.putExtra("Feeder", mCurFeeder);
            intent.putExtra("AutoTest", isAuto);
            intent.putExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
            intent.putExtra("TestType", mTestType);
            intent.putExtra(FeederMiniUtils.EXTRA_FEEDER, mErrorFeeder);
            startActivityForResult(intent, 0x12);
        } else {
            showShortToast(mInfoTestTextView.getText().toString());
        }
    }

    private void showWifiManager() {
        switch (mTestType) {
            case FeederMiniUtils.TYPE_TEST_PARTIALLY:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_A_HW2_"));
                break;
            case FeederMiniUtils.TYPE_TEST:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_B_HW2_"));
                break;
            case Globals.TYPE_AFTERMARKET:
            case FeederMiniUtils.TYPE_MAINTAIN:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_"));
                break;
            case FeederMiniUtils.TYPE_CHECK:
            case FeederMiniUtils.TYPE_DUPLICATE_MAC:
            case FeederMiniUtils.TYPE_DUPLICATE_SN:
                startActivity(WifiManagerActivity.getIntent(this, "PETKIT_FEEDER_HW2_"));
                break;
        }
    }
    
    private void refreshView() {
        String apSsid = mWifiAdminSimple.getWifiConnectedSsid();
        if(apSsid == null) {
            mInfoTestTextView.setText("请先连接到特定的WIFI，再进行测试！");
        } else {
            switch (mTestType) {
                case FeederMiniUtils.TYPE_TEST_PARTIALLY:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_A_HW2_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_A_开头的WIFI，再进行测试！");
                    } else {
                        connectAp();
                    }
                    break;
                case FeederMiniUtils.TYPE_TEST:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_B_HW2_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_B_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case FeederMiniUtils.TYPE_MAINTAIN:
                case Globals.TYPE_AFTERMARKET:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case FeederMiniUtils.TYPE_CHECK:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_HW2_")
                            || (apSsid.toUpperCase().startsWith("PETKIT_FEEDER_A_") || apSsid.toUpperCase().startsWith("PETKIT_FEEDER_B_"))) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
                case FeederMiniUtils.TYPE_DUPLICATE_MAC:
                case FeederMiniUtils.TYPE_DUPLICATE_SN:
                    if (!apSsid.toUpperCase().startsWith("PETKIT_FEEDER_HW2_")) {
                        mInfoTestTextView.setText("请先连接到PETKIT_FEEDER_HW2_开头的WIFI，再进行测试！");
                        return;
                    } else {
                        connectAp();
                    }
                    break;
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void refreshBottomButton () {
        ((TextView) findViewById(R.id.test_auto)).setText(testComplete ? "测试完成" : "自动模式");
    }

    private void connectAp() {
        if(mTestState != TEST_STATE_CONNECTED && !PetkitSocketInstance.getInstance().isConnected()) {
            String remoteIp = mWifiAdminSimple.getCurrentApHostIp();
            if(isEmpty(remoteIp)) {
                mInfoTestTextView.setText("获取喂食器IP失败！");
                mTestState = TEST_STATE_INVALID;
            } else if(mTestState != TEST_STATE_CONNECTING){
                mInfoTestTextView.setText("正在连接设备");
                mTestState = TEST_STATE_CONNECTING;

                PetkitSocketInstance.getInstance().startConnect(remoteIp, 8001);
            }
        } else {
//            mInfoTestTextView.setText("可以开始测试啦");
            mTestState = TEST_STATE_CONNECTED;
        }
    }

    private void disconnectAp() {
//        if(PetkitSocketInstance.getInstance().isConnected()) {
            PetkitSocketInstance.getInstance().disconnect();
//        }
        mTestState = TEST_STATE_INVALID;
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBoradcastReceiver(){
        mBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (arg1.getAction()) {
                    case ConnectivityManager.CONNECTIVITY_ACTION:
//                        disconnectAp();
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
        mFeederMiniTestUnits = FeederMiniUtils.generateFeederMiniTestUnitsForType(mTestType);
        mAdapter.notifyDataSetChanged();

        PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getDefaultRequestForKey(110));
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
                    if (!jsonObject.isNull("id")) {
                        stringBuilder.append("id: ").append(jsonObject.getInt("id")).append("\n");
                    }

                    if(isEmpty(mac)) {
                        mInfoTestTextView.setText("设备信息不正确，没有MAC地址！");
                        PetkitSocketInstance.getInstance().disconnect();
                        return;
                    }

                    if(sn == null && FeederMiniUtils.checkMacIsDuplicate(mac)) {
                        mInfoTestTextView.setText("设备MAC出现重复，该设备属于故障设备，不能正常测试！");
                        PetkitSocketInstance.getInstance().disconnect();
                        return;
                    }

                    if(mErrorFeeder != null) {
                        if(!mErrorFeeder.getMac().equals(mac)) {
                            mInfoTestTextView.setText("设备信息不匹配，需要链接的设备MAC为：" + mErrorFeeder.getMac());
                            PetkitSocketInstance.getInstance().disconnect();
                            return;
                        }
                    }

                    mCurFeeder = new Feeder(mac, sn, chipid);

                    mInfoTestTextView.append(stringBuilder.toString());

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("mac", mCurFeeder.getMac());
                    params.put("state", getTestTypeCode());
                    params.put("opt", 0);
                    PetkitSocketInstance.getInstance().sendString(FeederMiniUtils.getRequestForKeyAndPayload(160, params));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 160:
                if(!testComplete) {
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

    private int getTestTypeCode() {
        switch (mTestType) {
            case FeederMiniUtils.TYPE_TEST:
            case FeederMiniUtils.TYPE_TEST_PARTIALLY:
            case Globals.TYPE_AFTERMARKET:
                return 1;
            case FeederMiniUtils.TYPE_MAINTAIN:
                return 2;
            case FeederMiniUtils.TYPE_CHECK:
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
            return mFeederMiniTestUnits.size();
        }

        @Override
        public FeederMiniTestUnit getItem(int position) {
            return mFeederMiniTestUnits.get(position);
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

            FeederMiniTestUnit item = getItem(position);

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
        for (FeederMiniTestUnit unit : mFeederMiniTestUnits) {
            if(unit.getResult() == 1) {
                position++;
            } else {
                break;
            }
        }

        if(position >= mFeederMiniTestUnits.size() - 1) {       //维修和抽检，最后一项打印标签可以不执行，其他项都完成了就算成功
            if (mTestType == FeederMiniUtils.TYPE_MAINTAIN) {
                FeederMiniUtils.storeMainTainInfo(mCurFeeder);
                testComplete = position >= mFeederMiniTestUnits.size();
            } else if (mTestType == FeederMiniUtils.TYPE_CHECK) {
                mCurFeeder.setInspectStatus(1);
                FeederMiniUtils.storeCheckInfo(mCurFeeder);
                testComplete = position >= mFeederMiniTestUnits.size();
            } else if (mTestType == FeederMiniUtils.TYPE_TEST_PARTIALLY) {
                testComplete = position >= mFeederMiniTestUnits.size();
            } else {
                testComplete = position >= mFeederMiniTestUnits.size();
            }
        }
    }

    @Override
    public void cancel(View view) {
        onBackPressed();
    }
}
