package com.petkit.matetool.ui.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.service.AndroidBLEActionService;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.ConvertDipPx;
import com.petkit.android.utils.DateUtil;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.Date;

/**
 * Created by Jone on 17/5/16.
 */
public class BLEErrorListActivity extends BaseListActivity {

    private Tester mTester;
    private int mDeviceType;

    private DevicesError mDevicesError;
    private DevicesListAdapter mAdapter;

    private int mSelectPosition;
    private boolean scanState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
        }

        super.onCreate(savedInstanceState);
        registerBoradcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        setTitle("异常设备列表");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mDevicesError = DeviceCommonUtils.getDevicesErrorMsg(mDeviceType);

        mAdapter = new DevicesListAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(ConvertDipPx.dip2px(this, 2));

        setListViewState(ListView_State_Normal);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterBroadcastReceiver();
    }

    @Override
    protected void onRefresh() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectPosition = position;

        startScanDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == 0x11) {
                if(mSelectPosition < mDevicesError.getMac().size()) {
                    mDevicesError.getMac().remove(mSelectPosition);
                } else {
                    mDevicesError.getSn().remove(mSelectPosition - mDevicesError.getMac().size());
                }
                DeviceCommonUtils.storeDuplicatedInfo(mDeviceType, mDevicesError);
                mSelectPosition = 0;

                if(mAdapter.getCount() == 0) {
                    showShortToast("异常已经处理完成！");
                    finish();
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    class DevicesListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDevicesError.getMac().size() + mDevicesError.getSn().size();
        }

        @Override
        public Device getItem(int position) {
            if(position < mDevicesError.getMac().size()) {
                return mDevicesError.getMac().get(position);
            } else {
                return mDevicesError.getSn().get(position - mDevicesError.getMac().size());
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DevicesListAdapter.ViewHolder viewHolder;
            Device item = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(BLEErrorListActivity.this).inflate(R.layout.adapter_feeder_list, parent, false);
                viewHolder = new DevicesListAdapter.ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.date = (TextView) convertView.findViewById(R.id.date);
                viewHolder.state = (TextView) convertView.findViewById(R.id.state);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DevicesListAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText("MAC: " + item.getMac() + "\nSN: " + item.getSn());
            Date date = new Date();
            date.setTime(item.getCreation());
            viewHolder.date.setText(DateUtil.formatDate(date));
            viewHolder.state.setText(position < mDevicesError.getMac().size() ? "MAC地址重复" : "SN重复");

            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView date;
            TextView state;
        }
    }

    private void startScanDevice() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            CommonUtils.showShortToast(this, "你的手机蓝牙不支持");
            return;
        }

        if (scanState) {
            return;
        }

        scanState = true;

        Bundle bundle = new Bundle();
        bundle.putInt(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_SCAN);

        startBLEAction(bundle);

        CommonUtils.addSysIntMap(this, Consts.SHARED_BLE_STATE, Consts.BLE_STATE_USING);

        showLoadDialog();
    }


    private void entryDetailTestActivity() {
        scanFinish();

        Intent intent = new Intent(this, DeviceCommonUtils.getMainActivityByType(mDeviceType));
        intent.putExtra(DeviceCommonUtils.EXTRA_TESTER, mTester);
        intent.putExtra(DeviceCommonUtils.EXTRA_TEST_TYPE, mSelectPosition < mDevicesError.getMac().size() ? Globals.TYPE_DUPLICATE_MAC : Globals.TYPE_DUPLICATE_SN);
        intent.putExtra(DeviceCommonUtils.EXTRA_ERROR_DEVICE, mAdapter.getItem(mSelectPosition));
        intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE, mAdapter.getItem(mSelectPosition));
        startActivityForResult(intent, 0x11);
    }

    private void startBLEAction(Bundle bundle){
        if(CommonUtils.getAndroidSDKVersion() >= 18){
            if (!getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_BLUETOOTH_LE)) {
                return;
            }

            final Intent service = new Intent(this, AndroidBLEActionService.class);
            service.putExtras(bundle);
            startService(service);
        }else {
            CommonUtils.showShortToast(this, "你的手机蓝牙不支持");
        }
    }


    private void startConnectDevice(DeviceInfo deviceInfo) {
        scanState = false;

        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Bundle bundle = new Bundle();
        bundle.putInt(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_DEVICE_TEST);
        bundle.putSerializable(BLEConsts.EXTRA_DEVICE_INFO, deviceInfo);
        startBLEAction(bundle);
    }

    private void scanFinish() {
        dismissLoadDialog();
        scanState = false;
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
                            case BLEConsts.PROGRESS_CONNECTED:
                                entryDetailTestActivity();
                                break;
                            case BLEConsts.PROGRESS_SCANING_TIMEOUT:
                            case BLEConsts.PROGRESS_SCANING_FAILED:
                            case BLEConsts.ERROR_DEVICE_DISCONNECTED:
                            case BLEConsts.PROGRESS_DISCONNECTING:
                            case BLEConsts.ERROR_SYNC_INIT_FAIL:
                            case BLEConsts.ERROR_DEVICE_ID_NULL:
                                if (scanState) {
                                    scanFinish();
                                }
                                break;
                            default:
                                break;
                        }
                        break;

                    case BLEConsts.BROADCAST_ERROR:
                        progress = arg1.getIntExtra(BLEConsts.EXTRA_DATA, 0);

                        switch (progress) {
                            case BLEConsts.ERROR_ABORTED:
                                break;
                            case BLEConsts.ERROR_INVALID_PARAMETERS:
                            case BLEConsts.ERROR_INVALID_RESPONSE:
                            case BLEConsts.ERROR_SYNC_TIMEOUT:
                            default:
                                LoadDialog.dismissDialog();
                                CommonUtils.showShortToast(BLEErrorListActivity.this, "设备已断开");
                                break;
                        }
                        break;
                    case BLEConsts.BROADCAST_SCANED_DEVICE:
                        DeviceInfo deviceInfo = (DeviceInfo) arg1.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);

                        if(deviceInfo.getName() == null){
                            return;
                        }

                        if (deviceInfo.getMac()!=null && deviceInfo.getMac().toLowerCase().equals(mAdapter.getItem(mSelectPosition).getMac().toLowerCase())){
                            startConnectDevice(deviceInfo);
                        }
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEConsts.BROADCAST_PROGRESS);
        filter.addAction(BLEConsts.BROADCAST_ERROR);
        filter.addAction(BLEConsts.BROADCAST_LOG);
        filter.addAction(BLEConsts.BROADCAST_SCANED_DEVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }
}
