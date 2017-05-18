package com.petkit.matetool.ui.feeder;

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
import com.petkit.matetool.ui.base.BaseListActivity;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feeder.mode.FeederTester;
import com.petkit.matetool.ui.feeder.mode.FeedersError;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.Date;

/**
 * Created by Jone on 17/5/16.
 */
public class FeederErrorListActivity extends BaseListActivity {

    private FeederTester mTester;

    private FeedersError mFeedersError;
    private FeedersListAdapter mAdapter;

    private int mSelectPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mTester = (FeederTester) savedInstanceState.getSerializable(FeederUtils.EXTRA_FEEDER_TESTER);
        } else {
            mTester = (FeederTester) getIntent().getSerializableExtra(FeederUtils.EXTRA_FEEDER_TESTER);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        setTitle("异常设备列表");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mFeedersError = FeederUtils.getFeedersErrorMsg();

        mAdapter = new FeedersListAdapter();
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

        Intent intent = new Intent(this, FeederTestMainActivity.class);
        intent.putExtra(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
        intent.putExtra("TestType", position < mFeedersError.getMac().size() ? FeederUtils.TYPE_DUPLICATE_MAC : FeederUtils.TYPE_DUPLICATE_SN);
        intent.putExtra(FeederUtils.EXTRA_FEEDER, mAdapter.getItem(position));
        startActivityForResult(intent, 0x11);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == 0x11) {
                if(mSelectPosition < mFeedersError.getMac().size()) {
                    mFeedersError.getMac().remove(mSelectPosition);
                } else {
                    mFeedersError.getSn().remove(mSelectPosition - mFeedersError.getMac().size());
                }
                FeederUtils.storeDuplicatedInfo(mFeedersError);

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

    class FeedersListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFeedersError.getMac().size() + mFeedersError.getSn().size();
        }

        @Override
        public Feeder getItem(int position) {
            if(position < mFeedersError.getMac().size()) {
                return mFeedersError.getMac().get(position);
            } else {
                return mFeedersError.getSn().get(position - mFeedersError.getMac().size());
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            Feeder item = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(FeederErrorListActivity.this).inflate(R.layout.adapter_feeder_list, parent, false);
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
            viewHolder.state.setText(position < mFeedersError.getMac().size() ? "MAC地址重复" : "SN重复");

            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView date;
            TextView state;
        }
    }
}
