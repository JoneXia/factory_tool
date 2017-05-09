package com.petkit.matetool.ui.feeder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Feeder;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.mode.FeederTestUnit;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.ui.feeder.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.feeder.utils.WifiAdminSimple;
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
public class FeederTestMainActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private int workStation;
    private int mTestType;

    private WifiAdminSimple mWifiAdminSimple;
    private int mTestState;
    private Feeder mCurFeeder;

    private ArrayList<FeederTestUnit> mFeederTestUnits;
    private TestItemAdapter mAdapter;

    private TextView mInfoTestTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mTestType = savedInstanceState.getInt("TestType");
        } else {
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mTestType = getIntent().getIntExtra("TestType", FeederUtils.TYPE_TEST);
        }

        setContentView(R.layout.activity_feeder_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
        outState.putInt("TestType", mTestType);
    }

    @Override
    protected void setupViews() {
        setTitle("喂食器测试");

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_wifi).setOnClickListener(this);
        findViewById(R.id.connect_dev).setOnClickListener(this);
        findViewById(R.id.test_auto).setOnClickListener(this);

        mFeederTestUnits = FeederUtils.generateFeederTestUnitsForType(mTestType);

        GridView gridView =(GridView) findViewById(R.id.gridView);
        mAdapter = new TestItemAdapter(this);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mTestState == TEST_STATE_CONNECTED) {
                    Intent intent = new Intent(FeederTestMainActivity.this, FeederTestDetailActivity.class);
                    intent.putExtra("TestUnits", mFeederTestUnits);
                    intent.putExtra("CurrentTestStep", position);
                    intent.putExtra("Feeder", mCurFeeder);
                    intent.putExtra("AutoTest", false);
                    startActivityForResult(intent, 0x12);
                } else {
                    showShortToast(mInfoTestTextView.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);
//        refreshView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        disconnectAp();
        unregisterBroadcastReceiver();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_wifi:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.connect_dev:
                refreshView();
                break;
            case R.id.test_auto:
                if(mTestState == TEST_STATE_CONNECTED) {
                    int position = 0;

                    for (FeederTestUnit unit : mFeederTestUnits) {
                        if(unit.getResult() == 1) {
                            position++;
                        } else {
                            break;
                        }
                    }
                    Intent intent = new Intent(FeederTestMainActivity.this, FeederTestDetailActivity.class);
                    intent.putExtra("TestUnits", mFeederTestUnits);
                    intent.putExtra("CurrentTestStep", position);
                    intent.putExtra("Feeder", mCurFeeder);
                    startActivityForResult(intent, 0x12);
                } else {
                    showShortToast(mInfoTestTextView.getText().toString());
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0x11:
                    refreshView();
                    break;
                case 0x12:
                    mFeederTestUnits = (ArrayList<FeederTestUnit>) data.getSerializableExtra("TestUnits");
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    private void refreshView() {
        String apSsid = mWifiAdminSimple.getWifiConnectedSsid();
        if(apSsid == null || !apSsid.startsWith("PETKIT_AP_")) { //PETKIT_AP_
            mInfoTestTextView.setText("请先连接到特定的WIFI，再进行测试！");
        } else {
            connectAp();
        }
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
            mInfoTestTextView.setText("可以开始测试啦");
            mTestState = TEST_STATE_CONNECTED;
        }
    }

    private void disconnectAp() {
        if(PetkitSocketInstance.getInstance().isConnected()) {
            PetkitSocketInstance.getInstance().disconnect();
        }
        mTestState = TEST_STATE_INVALID;
        mFeederTestUnits = FeederUtils.generateFeederTestUnitsForType(mTestType);
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBoradcastReceiver(){
        mBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (arg1.getAction()) {
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        disconnectAp();
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

        PetkitSocketInstance.getInstance().sendString(FeederUtils.getDefaultRequestForKey(110));
    }

    @Override
    public void onDisconnected() {
        mInfoTestTextView.setText("设备已断开连接");
        mTestState = TEST_STATE_INVALID;
    }

    @Override
    public void onResponse(int key, String data) {
        switch (key) {
            case 110:
                try {
                    JSONObject jsonObject = JSONUtils.getJSONObject(data);
                    StringBuilder stringBuilder = new StringBuilder();
                    String mac = null, sn = null;
                    if (!jsonObject.isNull("mac")) {
                        mac = jsonObject.getString("mac");
                        stringBuilder.append("\n").append("mac: ").append(mac).append("\n");
                    }
                    if (!jsonObject.isNull("sn")) {
                        sn = jsonObject.getString("sn");
                        stringBuilder.append("sn: ").append(sn).append("\n");
                    }
                    if (!jsonObject.isNull("hardware")) {
                        stringBuilder.append("hardware: ").append(jsonObject.getInt("hardware")).append("\n");
                    }
                    if (!jsonObject.isNull("version")) {
                        stringBuilder.append("version: ").append(jsonObject.getInt("version")).append("\n");
                    }
                    if (!jsonObject.isNull("id")) {
                        stringBuilder.append("id: ").append(jsonObject.getInt("id")).append("\n");
                    }
                    mCurFeeder = new Feeder(mac, sn);

                   mInfoTestTextView.append(stringBuilder.toString());

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("mac", mCurFeeder.getMac());
                    params.put("state", 1);
                    PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(160, params));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 161:

                break;
        }
    }

    private class TestItemAdapter extends BaseAdapter {
        private Context mContext;

        public TestItemAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mFeederTestUnits.size();
        }

        @Override
        public FeederTestUnit getItem(int position) {
            return mFeederTestUnits.get(position);
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

            FeederTestUnit item = getItem(position);

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
}
