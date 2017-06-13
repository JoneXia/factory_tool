package com.petkit.matetool.ui.go;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.petkit.android.ble.DeviceInfo;
import com.petkit.matetool.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jone on 16/10/31.
 */

public class GoScanListAdapter extends BaseAdapter{


    protected List<DeviceInfo> mList;
    protected Activity mActivity;


    public GoScanListAdapter(Activity mActivity, List list) {
        this.mActivity = mActivity;
        this.mList = list;

        if(mList == null) {
            mList = new ArrayList<>();
        }

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public DeviceInfo getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void clearList(){
        if(this.mList != null) {
            this.mList.clear();
            notifyDataSetChanged();
        }
    }
    public List<DeviceInfo> getList(){
        return mList;
    }

    public void addList(List<DeviceInfo> list){
        if(list == null) {
            return;
        }

        if(this.mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(list);
        Collections.sort(mList, new DeviceInfotSortUtil());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DeviceInfo item = getItem(position);

        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.adapter_scan_device_list, null);
            holder.device_name = (TextView) convertView.findViewById(R.id.device_name);
            holder.device_mac = (TextView) convertView.findViewById(R.id.device_mac);
            holder.device_rssi = (TextView) convertView.findViewById(R.id.device_rssi);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.device_name.setText(item.getName());
        holder.device_mac.setText(item.getMac());
        holder.device_rssi.setText(" " + (item.getRssi()));

        return convertView;
    }

    private class ViewHolder {
        TextView device_name;
        TextView device_mac;
        TextView device_rssi;
    }

}
