package com.petkit.matetool.ui.W5;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.data.BaseDataUtils;
import com.petkit.android.ble.data.PetkitBleMsg;
import com.petkit.android.utils.ByteUtil;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.W5.mode.W5TestUnit;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.utils.WifiAdminSimple;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

/**
 *
 * Created by Jone on 17/4/24.
 */
public class W5TestMainActivity extends BaseActivity {

    private static final int TEST_STATE_INVALID      = 0;
    private static final int TEST_STATE_CONNECTING      = 1;
    private static final int TEST_STATE_CONNECTED      = 2;

    private Tester mTester;
    private int mW5Type;
    private int mTestType;

    private WifiAdminSimple mWifiAdminSimple;
    private int mTestState;
    private Device mCurDevice, mErrorDevice;

    private ArrayList<W5TestUnit> mW5TestUnits;
    private TestItemAdapter mAdapter;

    private TextView mInfoTestTextView;
    private boolean testComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(W5Utils.EXTRA_W5_TESTER);
            mTestType = savedInstanceState.getInt("TestType");
            mW5Type = savedInstanceState.getInt(W5Utils.EXTRA_W5_TYPE);
            mCurDevice = (Device) savedInstanceState.getSerializable(W5Utils.EXTRA_W5);
            if (savedInstanceState.getSerializable(W5Utils.EXTRA_ERROR_W5) != null) {
                mErrorDevice = (Device) savedInstanceState.getSerializable(W5Utils.EXTRA_ERROR_W5);
            }
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(W5Utils.EXTRA_W5_TESTER);
            mTestType = getIntent().getIntExtra("TestType", W5Utils.TYPE_TEST);
            mCurDevice = (Device) getIntent().getSerializableExtra(W5Utils.EXTRA_W5);
            mW5Type = getIntent().getIntExtra(W5Utils.EXTRA_W5_TYPE, W5Utils.W5_TYPE_NORMAL);
            if (getIntent().getSerializableExtra(W5Utils.EXTRA_ERROR_W5) != null) {
                mErrorDevice = (Device) getIntent().getSerializableExtra(W5Utils.EXTRA_ERROR_W5);
            }
        }
        mTestState = TEST_STATE_CONNECTED;

        setContentView(R.layout.activity_feeder_main_test);

        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
        outState.putInt("TestType", mTestType);
        outState.putSerializable(W5Utils.EXTRA_W5, mCurDevice);
        outState.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
        if (mErrorDevice != null) {
            outState.putSerializable(W5Utils.EXTRA_ERROR_W5, mErrorDevice);
        }
    }

    @Override
    protected void setupViews() {
        setTitle((mW5Type == W5Utils.W5_TYPE_NORMAL ? "W5-" : "W5C-") + getTitleByType());

        mWifiAdminSimple = new WifiAdminSimple(this);

        mInfoTestTextView = (TextView) findViewById(R.id.test_info);
        findViewById(R.id.set_wifi).setVisibility(View.GONE);
        findViewById(R.id.connect_dev).setVisibility(View.GONE);
        findViewById(R.id.test_auto).setOnClickListener(this);

        mW5TestUnits = W5Utils.generateW5TestUnitsForType(mTestType);

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

        mInfoTestTextView.setText(mCurDevice.toString());

        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_STEP_ENTRY);
        intent.putExtra(BLEConsts.EXTRA_DATA, BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_GET_INFO));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getTitleByType() {
        if (mTestType == W5Utils.TYPE_TEST) {
            return "成品测试";
        } else if (mTestType == W5Utils.TYPE_TEST_PARTIALLY) {
            return "半成品测试";
        } else if (mTestType == W5Utils.TYPE_CHECK) {
            return "抽检";
        } else {
            return "维修";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LoadDialog.dismissDialog();
        W5Utils.stopBle(this);
        unregisterBroadcastReceiver();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_dev:
//                testSN();
//                startActivity(PrintActivity.class);
                break;
            case R.id.test_auto:
                if (testComplete) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    startTestDetail(true, 0);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0x12:
                    mW5TestUnits = (ArrayList<W5TestUnit>) data.getSerializableExtra("TestUnits");
                    mCurDevice = (Device) data.getSerializableExtra(W5Utils.EXTRA_W5);
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

                for (W5TestUnit unit : mW5TestUnits) {
                    if (unit.getResult() == 1) {
                        position++;
                    } else {
                        break;
                    }
                }
                if (position == mW5TestUnits.size()) {
                    showShortToast("测试已完成");
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                pos = position;
            }

            Intent intent = new Intent(W5TestMainActivity.this, W5TestDetailActivity.class);
            intent.putExtra("TestUnits", mW5TestUnits);
            intent.putExtra("CurrentTestStep", pos);
            intent.putExtra(W5Utils.EXTRA_W5, mCurDevice);
            intent.putExtra("AutoTest", isAuto);
            intent.putExtra(W5Utils.EXTRA_W5_TESTER, mTester);
            intent.putExtra(W5Utils.EXTRA_ERROR_W5, mErrorDevice);
            intent.putExtra("TestType", mTestType);
            intent.putExtra(W5Utils.EXTRA_W5_TYPE, mW5Type);
            startActivityForResult(intent, 0x12);
        } else {
            showShortToast(mInfoTestTextView.getText().toString());
        }
    }

    private void refreshBottomButton () {
        ((TextView) findViewById(R.id.test_auto)).setText(testComplete ? "测试完成" : "自动模式");
    }

    /**
     * @param key
     * @param data
     */
    public void processResponseData(int key, byte[] data) {

        switch (key) {
            case BLEConsts.OP_CODE_GET_INFO:
                if (data.length >= 8) {
                    byte[] idRow = new byte[8];
                    System.arraycopy(data, 0, idRow, 0, 8);
                    long deviceId = ByteUtil.bytes2Long(idRow);
                    if (deviceId > 0) {
                        mCurDevice.setId(deviceId + "");
                    }
                }

                if (data.length >= 22) {
                    byte[] snRaw = new byte[14];
                    System.arraycopy(data, 8, snRaw, 0, 14);

                    if (!"0000000000000000000000000000".equals(ByteUtil.byteArrayToHexStr(snRaw))) {
                        String sn = new String(snRaw);
                        mCurDevice.setSn(sn);
                    }
                }

                mInfoTestTextView.setText(mCurDevice.toString());

                Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
                intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_STEP_ENTRY);
                intent.putExtra(BLEConsts.EXTRA_DATA, BaseDataUtils.buildOpCodeBuffer(BLEConsts.OP_CODE_GET_VERSION));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            case BLEConsts.OP_CODE_GET_VERSION:
                if (data.length >= 2) {
                    mCurDevice.setHardware(ByteUtil.toInt(data[0]));
                    mCurDevice.setFirmware(ByteUtil.toInt(data[1]));
                    mInfoTestTextView.setText(mCurDevice.toString());
                }
                break;
        }
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                PetkitLog.d("" + arg1.getAction());

                switch (arg1.getAction()) {
                    case BLEConsts.BROADCAST_PROGRESS:
                        int progress = arg1.getIntExtra(BLEConsts.EXTRA_PROGRESS, 0);

                        switch (progress) {
                            case BLEConsts.PROGRESS_SCANING_TIMEOUT:
                            case BLEConsts.PROGRESS_SCANING_FAILED:
                            case BLEConsts.ERROR_DEVICE_DISCONNECTED:
                            case BLEConsts.PROGRESS_DISCONNECTING:
                            case BLEConsts.ERROR_SYNC_INIT_FAIL:
                            case BLEConsts.ERROR_DEVICE_ID_NULL:
                                finish();
                                break;
                            case BLEConsts.PROGRESS_STEP_DATA:
                                ArrayList<PetkitBleMsg> msgs = (ArrayList<PetkitBleMsg>) arg1.getSerializableExtra(BLEConsts.EXTRA_DATA);
                                if (msgs != null) {
                                    for (PetkitBleMsg msg : msgs) {
                                        processResponseData(msg.getCmd(), msg.getData());
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        break;

                    case BLEConsts.BROADCAST_ERROR:
                        progress = arg1.getIntExtra(BLEConsts.EXTRA_DATA, 0);

                        switch (progress) {
                            default:
                                finish();
                                break;
                        }
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEConsts.BROADCAST_PROGRESS);
        filter.addAction(BLEConsts.BROADCAST_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


    private class TestItemAdapter extends BaseAdapter {
        private Context mContext;

        public TestItemAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mW5TestUnits.size();
        }

        @Override
        public W5TestUnit getItem(int position) {
            return mW5TestUnits.get(position);
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

            W5TestUnit item = getItem(position);

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
        for (W5TestUnit unit : mW5TestUnits) {
            if(unit.getResult() == 1) {
                position++;
            } else {
                break;
            }
        }

        if(position >= mW5TestUnits.size() - 1) {       //维修和抽检，最后一项打印标签可以不执行，其他项都完成了就算成功
            if (mTestType == W5Utils.TYPE_MAINTAIN) {
                W5Utils.storeMainTainInfo(mCurDevice);
                testComplete = position >= mW5TestUnits.size();
            } else if (mTestType == W5Utils.TYPE_CHECK) {
                W5Utils.storeCheckInfo(mCurDevice);
                testComplete = position >= mW5TestUnits.size();
            } else if (mTestType == W5Utils.TYPE_TEST_PARTIALLY) {
                testComplete = position >= mW5TestUnits.size();
            } else if (mTestType == W5Utils.TYPE_TEST) {
                testComplete = position >= mW5TestUnits.size();
            } else {
                testComplete = position >= mW5TestUnits.size();
            }
        }
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
