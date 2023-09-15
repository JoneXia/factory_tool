package com.petkit.matetool.ui.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.utils.PetkitToast;
import com.petkit.matetool.R;
import com.petkit.matetool.model.UDPDevice;
import com.petkit.matetool.model.UDPScanRecord;
import com.petkit.matetool.ui.base.BaseApplication;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.ui.utils.UDPServer;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class UDPManagerActivity extends BaseListActivity {

    private UDPScanRecord mFlterScanRecord;
    private List<UDPDevice> mList;

    private IPsListAdapter mAdapter;
    private UDPServer mServer;


    public static Intent  getIntent(Context context, UDPScanRecord scanRecord) {
        Intent intent = new Intent(context, UDPManagerActivity.class);
        intent.putExtra("mFlterScanRecord", scanRecord);
        return  intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mFlterScanRecord = (UDPScanRecord) getIntent().getSerializableExtra("mFlterScanRecord");
        } else {
            mFlterScanRecord = (UDPScanRecord) savedInstanceState.getSerializable("mFlterScanRecord");
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ssidFilterString", mFlterScanRecord);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        int width = BaseApplication.getDisplayMetrics(this).widthPixels;
        lp.width = (int) (width * 0.8);
        lp.height = (int) (BaseApplication.getDisplayMetrics(this).heightPixels * 0.8);
        dialogWindow.setAttributes(lp);

        setTitle("扫描摄像头设备");
        mList = new ArrayList<>();
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        refreshWifiList();
        mAdapter = new IPsListAdapter();
        mListView.setAdapter(mAdapter);

        setListViewEmpty(0, "未找到有用设备，请将手机连接到热点《PETKIT_PT_WIFI》后再试。", R.string.Tap_to_refresh, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWifiList();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onRefresh() {
        mList.clear();
        refreshWifiList();
        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Intent data = new Intent();
        data.putExtra("UDPDevice", mList.get(position));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        refreshWifiList();
        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }


    private void startScanUDP() {
        try {
            if (mServer == null) {
                mServer = new UDPServer();
                mServer.setListener((device) -> {
                    PetkitLog.d("find new device: " + device.toString());
                    LogcatStorageHelper.addLog("find UDP new device: " + device.toString());
                    if (!mList.contains(device) && device.getScanRecord().equals(mFlterScanRecord)) {
                        mList.add(device);

                        runOnUiThread(() -> {
                            if (mList.size() == 1) {
                                setListViewState(ListView_State_Normal);
                            } else {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
                mServer.startScan();
            } else {
                if (mServer.isAbort()) {
                    mServer.setAbort(false);
                    mServer.startScan();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScan() {
        if (mServer != null) {
            mServer.setAbort(true);
        }
    }

    private void refreshWifiList() {

        startScanUDP();

        if (mList.size() == 0) {
            setListViewState(ListView_State_Empty);
        } else {
            setListViewState(ListView_State_Normal);
        }

    }

    class IPsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public UDPDevice getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = LayoutInflater.from(UDPManagerActivity.this).inflate(R.layout.adapter_wifi_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.wifi_name);
                viewHolder.rssiValue = (TextView) convertView.findViewById(R.id.tv_wifi_rssi);
                viewHolder.rssi = (ImageView) convertView.findViewById(R.id.wifi_rssi);
                viewHolder.rssi.setVisibility(View.INVISIBLE);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText("ip=" + mList.get(position).getIp() + ", " +  mList.get(position).getScanRecord().toString());
//            switch (mWifiList.get(position).level) {
//                case 1:
//                    viewHolder.rssi.setImageResource(R.drawable.ic_wifi_1);
//                    break;
//                case 2:
//                    viewHolder.rssi.setImageResource(R.drawable.ic_wifi_2);
//                    break;
//                case 3:
//                    viewHolder.rssi.setImageResource(R.drawable.ic_wifi_3);
//                    break;
//                default:
//                    viewHolder.rssi.setImageResource(R.drawable.ic_wifi_4);
//                    break;
//            }


            return convertView;
        }

        class ViewHolder {
            TextView name, rssiValue;
            ImageView rssi;
        }
    }

}
