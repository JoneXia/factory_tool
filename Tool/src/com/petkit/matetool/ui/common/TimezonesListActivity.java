package com.petkit.matetool.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.TimeZone;

import static java.util.TimeZone.SHORT;

public class TimezonesListActivity  extends BaseListActivity {

    ArrayList<TimeZoneWrap> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mList = getTimeZones();
        mListView.setAdapter(new TextAdapter());
        setListViewState(ListView_State_Normal);
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


    private ArrayList<TimeZoneWrap> getTimeZones() {

        ArrayList<TimeZoneWrap> list = new ArrayList<>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            TimeZone timeZone = TimeZone.getTimeZone(id);
            TimeZoneWrap timeZoneWrap = new TimeZoneWrap(timeZone, timeZone.getDisplayName() + ": " + timeZone.getDisplayName(false, SHORT)) ;
            if (!list.contains(timeZoneWrap)) {
                list.add(timeZoneWrap);
            }
        }
        return list;
    }



    class TextAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public TimeZoneWrap getItem(int position) {
            return mList.get(position);
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
                convertView = LayoutInflater.from(TimezonesListActivity.this).inflate(R.layout.adapter_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.textView1);
                holder.name.setTextColor(getResources().getColor(R.color.black));
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(getItem(position).getName());

            return convertView;
        }

        private class ViewHolder {
            TextView name;
        }
    }
}
