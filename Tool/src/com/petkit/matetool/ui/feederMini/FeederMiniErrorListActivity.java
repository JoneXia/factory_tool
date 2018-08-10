package com.petkit.matetool.ui.feederMini;

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
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.feeder.mode.FeedersError;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.Date;

/**
 * Created by Jone on 17/5/16.
 */
public class FeederMiniErrorListActivity extends BaseListActivity {

    private Tester mTester;

    private FeedersError mFeedersError;
    private FeedersListAdapter mAdapter;

    private int mSelectPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        setTitle("异常设备列表");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mFeedersError = FeederMiniUtils.getFeedersErrorMsg();

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

        Intent intent = new Intent(this, FeederMiniTestMainActivity.class);
        intent.putExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
        intent.putExtra("TestType", position < mFeedersError.getMac().size() ? FeederMiniUtils.TYPE_DUPLICATE_MAC : FeederMiniUtils.TYPE_DUPLICATE_SN);
        intent.putExtra(FeederMiniUtils.EXTRA_FEEDER, mAdapter.getItem(position));
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
                FeederMiniUtils.storeDuplicatedInfo(mFeedersError);

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
                convertView = LayoutInflater.from(FeederMiniErrorListActivity.this).inflate(R.layout.adapter_feeder_list, parent, false);
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
