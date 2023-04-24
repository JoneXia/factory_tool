package com.petkit.matetool.ui.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.petkit.android.utils.PetkitToast;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseApplication;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.utils.WifiUtils;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WifiManagerActivity extends BaseListActivity {

    private String mWifiFilterString;
    private WifiUtils mWifiUtils;
    private List<ScanResult> mWifiList;

    private WifisListAdapter mAdapter;

    public static Intent getIntent(Context context, String ssidFilterString) {
        Intent intent = new Intent(context, WifiManagerActivity.class);
        intent.putExtra("ssidFilterString", ssidFilterString);
        return  intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mWifiFilterString = getIntent().getStringExtra("ssidFilterString");
        } else {
            mWifiFilterString = savedInstanceState.getString("ssidFilterString");
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ssidFilterString", mWifiFilterString);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        mWifiUtils = new WifiUtils(this);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        int width = BaseApplication.getDisplayMetrics(this).widthPixels;
        lp.width = (int) (width * 0.8);
        lp.height = (int) (BaseApplication.getDisplayMetrics(this).heightPixels * 0.8);
        dialogWindow.setAttributes(lp);

        setTitle("设置WiFi");
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        refreshWifiList();
        mAdapter = new WifisListAdapter();
        mListView.setAdapter(mAdapter);

        setListViewEmpty(0, "未找到有用WiFi，需包含: " + mWifiFilterString, R.string.Tap_to_refresh, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWifiList();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onRefresh() {
        refreshWifiList();
        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        mWifiUtils.connectToWifi(mAdapter.getItem(position).SSID, "", new WifiUtils.iWifiStateListener() {
            @Override
            public void onAvailable() {
                runOnUiThread(() -> {
                    PetkitToast.showToast("连接成功");
                    finish();
                });
            }

            @Override
            public void onUnavailable() {
                runOnUiThread(() -> PetkitToast.showToast("连接失败"));
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                PetkitToast.showToast("连接失败");
            }
        });
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

    private void refreshWifiList() {
        List<ScanResult> wifis = mWifiUtils.getScanWifiResults();

        if (TextUtils.isEmpty(mWifiFilterString)) {
            mWifiList = wifis;
        } else {
            mWifiList = new ArrayList<>();
            for (ScanResult ssid : wifis) {
                if (ssid != null && ssid.SSID.contains(mWifiFilterString)) {
                    mWifiList.add(ssid);
                }
            }
        }

        Collections.sort(mWifiList, new ScanResultSortUtil());

        if (mWifiList.size() == 0) {
            setListViewState(ListView_State_Empty);
        } else {
            setListViewState(ListView_State_Normal);
        }

    }

    class WifisListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mWifiList == null ? 0 : mWifiList.size();
        }

        @Override
        public ScanResult getItem(int position) {
            return mWifiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = LayoutInflater.from(WifiManagerActivity.this).inflate(R.layout.adapter_wifi_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.wifi_name);
                viewHolder.rssiValue = (TextView) convertView.findViewById(R.id.tv_wifi_rssi);
                viewHolder.rssi = (ImageView) convertView.findViewById(R.id.wifi_rssi);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText(mWifiList.get(position).SSID);
            viewHolder.rssiValue.setText(mWifiList.get(position).level + "");
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
