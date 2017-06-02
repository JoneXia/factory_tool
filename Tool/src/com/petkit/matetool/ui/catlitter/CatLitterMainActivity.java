package com.petkit.matetool.ui.catlitter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.catlitter.mode.CatLitterInfo;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.ui.feeder.utils.PetkitSocketInstance;
import com.petkit.matetool.ui.feeder.utils.WifiAdminSimple;
import com.petkit.matetool.utils.DateUtil;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getDateEN;


/**
 *
 */
public class CatLitterMainActivity extends BaseActivity implements PetkitSocketInstance.IPetkitSocketListener {


    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private WifiAdminSimple mWifiAdminSimple;
    private int mTestState;

    private TextView mInfoTestTextView;
    private TextView mDescTextView;
    private ScrollView mDescScrollView;

    private String mCacheFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
        } else {
        }

        setContentView(R.layout.activity_cat_litter_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void setupViews() {
        setTitle("猫窝测试");

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_wifi).setOnClickListener(this);
        findViewById(R.id.connect_dev).setOnClickListener(this);

        mDescTextView = (TextView) findViewById(R.id.test_detail);
        mDescScrollView = (ScrollView) findViewById(R.id.test_scrllview);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PetkitSocketInstance.getInstance().setPetkitSocketListener(this);
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
//                testSN();
//                startActivity(PrintActivity.class);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
            }
        }
    }

    private void refreshView() {
        String apSsid = mWifiAdminSimple.getWifiConnectedSsid();
        if(apSsid == null || !apSsid.startsWith("PETKIT_")) {
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
        mDescTextView.setText("");
        mTestState = TEST_STATE_CONNECTED;
        mCacheFileName = CommonUtils.getAppDirPath() + DateUtil.formatISO8601Date(new Date()) + ".txt";

        PetkitSocketInstance.getInstance().sendString(FeederUtils.getDefaultRequestForKey(110));
    }

    @Override
    public void onDisconnected() {
        mInfoTestTextView.append("\n设备已断开连接");
        mDescTextView.setText("");
        mTestState = TEST_STATE_INVALID;
    }

    @Override
    public void onResponse(int key, String data) {

        switch (key) {
            case 110:
                StringBuilder desc = new StringBuilder();
                try {
                    JSONObject jsonObject = JSONUtils.getJSONObject(data);
                    String mac = null;
                    if (!jsonObject.isNull("mac")) {
                        mac = jsonObject.getString("mac");
                        desc.append("\n").append("mac: ").append(mac).append("\n");
                    }
                    if(mac != null) {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mac", mac);
                        params.put("state", 2);
                        PetkitSocketInstance.getInstance().sendString(FeederUtils.getRequestForKeyAndPayload(160, params));
                    } else {
                        desc.append("\n").append("获取MAC失败，无法继续测试！").append("\n");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    desc.append("\n").append("数据解析失败").append("\n");
                }
                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case 222:
                CatLitterInfo info = new Gson().fromJson(data, CatLitterInfo.class);
                desc = new StringBuilder();
                desc.append("\n").append(info.toString()).append("\n");

                FileUtils.writeStringToFile(mCacheFileName, convertDataToFileContent(data), true);
                if(mDescTextView.getLineCount() > 100) {
                    mDescTextView.setText("");
                }
                mDescTextView.append(desc.toString());
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDescScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
        }
    }

    private String convertDataToFileContent(String data) {
        String newData = data.replaceAll(":", ";");
        newData = newData.replaceAll(",", ";");
        StringBuilder builder = new StringBuilder(getDateEN());
        builder.append("  ").append(newData).append("\n");
        return builder.toString();
    }



}
