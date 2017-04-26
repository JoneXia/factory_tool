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
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.ui.feeder.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.feeder.utils.WifiAdminSimple;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.petkit.matetool.ui.feeder.utils.PrintUtils.isPrinterConnected;

/**
 * Created by Jone on 17/4/24.
 */

public class FeederTestMainActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {

    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private WifiAdminSimple mWifiAdminSimple;
    private boolean isPrintConnected = false;
    private int mTestState;
    private int[] mTestResult;
    private String mCurDeviceMac;

    private TestItemAdapter mAdapter;

    private TextView mInfoTestTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feeder_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void setupViews() {
        setTitle("喂食器测试");

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_print).setOnClickListener(this);
        findViewById(R.id.set_wifi).setOnClickListener(this);

        mTestResult = new int[getStringArray(R.array.Feeder_test_items).length];

        GridView gridView =(GridView) findViewById(R.id.gridView);
        mAdapter = new TestItemAdapter(this);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mTestState == TEST_STATE_CONNECTED) {

                } else {
                    showShortToast(mInfoTestTextView.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshView();
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
            case R.id.set_print:
                startActivityForResult(PrintActivity.class, 0x11);
                break;
            case R.id.set_wifi:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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
            }
        }
    }

    private void refreshView() {
        isPrintConnected = isPrinterConnected();
        if(!isPrintConnected) {
            mInfoTestTextView.setText("打印机还未连接！");
        } else {
            String apSsid = mWifiAdminSimple.getWifiConnectedSsid();
            if(apSsid == null || !apSsid.startsWith("wifi")) { //PETKIT_AP_
                mInfoTestTextView.setText("请先连接到特定的WIFI，再进行测试！");
            } else {
                connectAp();
            }
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

                PetkitSocketInstance.getInstance().setPetkitSocketListener(this);
                PetkitSocketInstance.getInstance().startConnect(remoteIp, 8002);
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
                    String sn = null;
                    String mac = null;
                    int hardware = 0;
                    String software = null;
                    String id = null;
                    if (!jsonObject.isNull("sn")) {
                        sn = jsonObject.getString("sn");
                    }
                    if (!jsonObject.isNull("mac")) {
                        mac = jsonObject.getString("mac");
                    }
                    if (!jsonObject.isNull("hardware")) {
                        hardware = jsonObject.getInt("hardware");
                    }
                    if (!jsonObject.isNull("software")) {
                        software = jsonObject.getString("software");
                    }
                    if (!jsonObject.isNull("id")) {
                        id = jsonObject.getString("id");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onFailed() {

    }

    private class TestItemAdapter extends BaseAdapter {
        private Context mContext;

        public TestItemAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return getStringArray(R.array.Feeder_test_items).length;
        }

        @Override
        public String getItem(int position) {
            return getStringArray(R.array.Feeder_test_items)[position];
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

            holder.name.setText(getItem(position));

            if(mTestResult[position] == Globals.MATE_TEST_PASS) {
                holder.name.setBackgroundColor(getResources().getColor(R.color.green));
            } else if(mTestResult[position] == Globals.MATE_TEST_FAILED) {
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
}
