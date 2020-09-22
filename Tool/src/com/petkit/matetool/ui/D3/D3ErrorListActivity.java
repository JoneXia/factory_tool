package com.petkit.matetool.ui.D3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.petkit.android.utils.ConvertDipPx;
import com.petkit.android.utils.DateUtil;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D3.utils.D3Utils;
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.Date;

/**
 * Created by Jone on 17/5/16.
 */
public class D3ErrorListActivity extends BaseListActivity {

    private Tester mTester;

    private DevicesError mDevicesError;
    private DevicesListAdapter mAdapter;

    private int mSelectPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(D3Utils.EXTRA_D3_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(D3Utils.EXTRA_D3_TESTER);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(D3Utils.EXTRA_D3_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        setTitle("异常设备列表");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mDevicesError = D3Utils.getDevicesErrorMsg();

        mAdapter = new DevicesListAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(ConvertDipPx.dip2px(this, 2));

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
        mSelectPosition = position;

        Intent intent = new Intent(this, D3TestMainActivity.class);
        intent.putExtra(D3Utils.EXTRA_D3_TESTER, mTester);
        intent.putExtra("TestType", position < mDevicesError.getMac().size() ? D3Utils.TYPE_DUPLICATE_MAC : D3Utils.TYPE_DUPLICATE_SN);
        intent.putExtra(D3Utils.EXTRA_D3, mAdapter.getItem(position));
        startActivityForResult(intent, 0x11);
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
                D3Utils.storeDuplicatedInfo(mDevicesError);

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

            ViewHolder viewHolder;
            Device item = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(D3ErrorListActivity.this).inflate(R.layout.adapter_feeder_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.date = (TextView) convertView.findViewById(R.id.date);
                viewHolder.state = (TextView) convertView.findViewById(R.id.state);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
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
}
