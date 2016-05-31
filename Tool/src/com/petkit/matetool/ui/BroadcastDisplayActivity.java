package com.petkit.matetool.ui;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.ConvertDipPx;
import com.petkit.matetool.R;
import com.petkit.matetool.model.WifiParams;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by petkit on 16/5/27.
 */
public class BroadcastDisplayActivity extends BaseListActivity {

    private List<WifiParams> mWifiParamses;
    private BroadcastDisplayAdapter mBroadcastDisplayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerBoradcastReceiver();
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        setTitle(R.string.broadcast_receiving);

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mWifiParamses = new ArrayList<>();

        mBroadcastDisplayAdapter = new BroadcastDisplayAdapter();
        mListView.setAdapter(mBroadcastDisplayAdapter);
        mListView.setDividerHeight(ConvertDipPx.dip2px(this, 2));

        setListViewState(ListView_State_Normal);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBoradcastReceiver();
    }

    @Override
    protected void onRefresh() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }


    private BroadcastReceiver mBroadcastReceiver;


    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(isFinishing()) {
                    return;
                }

                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    switch (progress){
                        case DatagramConsts.SERVER_WIFI_LISTEN:
                            WifiParams wifiParams = new Gson().fromJson(data, WifiParams.class);

                            if(wifiParams == null || wifiParams.mac == null) {
                                return;
                            }

                            for (WifiParams params : mWifiParamses) {
                                if(wifiParams.getMac().equals(params.getMac())){
                                    return;
                                }
                            }

                            mWifiParamses.add(wifiParams);
                            mBroadcastDisplayAdapter.notifyDataSetChanged();
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBoradcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


    class BroadcastDisplayAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mWifiParamses.size();
        }

        @Override
        public WifiParams getItem(int position) {
            return mWifiParamses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            WifiParams item = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(BroadcastDisplayActivity.this).inflate(R.layout.adapter_broadcast_display, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mac = (TextView) convertView.findViewById(R.id.device_mac);
                viewHolder.ip = (TextView) convertView.findViewById(R.id.device_ip);
                viewHolder.station = (TextView) convertView.findViewById(R.id.device_station);
                viewHolder.status = (TextView) convertView.findViewById(R.id.device_status);
                viewHolder.rssi = (TextView) convertView.findViewById(R.id.device_rssi);
                viewHolder.sn = (TextView) convertView.findViewById(R.id.device_sn);
                viewHolder.copy = (Button) convertView.findViewById(R.id.device_sn_copy);

                viewHolder.copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clip.setText((CharSequence) v.getTag()); // 复制
                        showShortToast(R.string.Succeed);
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mac.setText(getString(R.string.Device_mac_format, ("" + item.getMac())));
            viewHolder.ip.setText(getString(R.string.Device_ip_format, "" + item.getLocal_rtp_ip()));
            viewHolder.station.setText(getString(R.string.Device_station_format, "" + item.getIndex()));
            viewHolder.status.setText(getString(R.string.Device_status_format, "" + item.getStatus()));
            viewHolder.rssi.setText(getString(R.string.Device_rssi_format, "" + item.getRsq()));

            if(!CommonUtils.isEmpty(item.getSn())){
                viewHolder.copy.setTag(item.getSn());
                viewHolder.copy.setVisibility(View.VISIBLE);
                viewHolder.sn.setText(getString(R.string.Device_sn_format, item.getSn()));
            } else {
                viewHolder.copy.setVisibility(View.GONE);
                viewHolder.sn.setText(getString(R.string.Device_sn_format, "  "));
            }

            return convertView;
        }

        class ViewHolder {
            TextView mac;
            TextView ip;
            TextView station;
            TextView status;
            TextView rssi;
            TextView sn;
            Button copy;
        }
    }

}
