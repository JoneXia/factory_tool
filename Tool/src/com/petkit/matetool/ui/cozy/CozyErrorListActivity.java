package com.petkit.matetool.ui.cozy;

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
import com.petkit.matetool.ui.cozy.mode.Cozy;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.cozy.mode.CozysError;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;

import java.util.Date;

/**
 * Created by Jone on 17/5/16.
 */
public class CozyErrorListActivity extends BaseListActivity {

    private Tester mTester;

    private CozysError mCozysError;
    private CozysListAdapter mAdapter;

    private int mSelectPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY_TESTER);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        setTitle("异常设备列表");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mCozysError = CozyUtils.getCozysErrorMsg();

        mAdapter = new CozysListAdapter();
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

        Intent intent = new Intent(this, CozyTestMainActivity.class);
        intent.putExtra(CozyUtils.EXTRA_COZY_TESTER, mTester);
        intent.putExtra("TestType", position < mCozysError.getMac().size() ? CozyUtils.TYPE_DUPLICATE_MAC : CozyUtils.TYPE_DUPLICATE_SN);
        intent.putExtra(CozyUtils.EXTRA_COZY, mAdapter.getItem(position));
        startActivityForResult(intent, 0x11);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == 0x11) {
                if(mSelectPosition < mCozysError.getMac().size()) {
                    mCozysError.getMac().remove(mSelectPosition);
                } else {
                    mCozysError.getSn().remove(mSelectPosition - mCozysError.getMac().size());
                }
                CozyUtils.storeDuplicatedInfo(mCozysError);

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

    class CozysListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCozysError.getMac().size() + mCozysError.getSn().size();
        }

        @Override
        public Cozy getItem(int position) {
            if(position < mCozysError.getMac().size()) {
                return mCozysError.getMac().get(position);
            } else {
                return mCozysError.getSn().get(position - mCozysError.getMac().size());
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            Cozy item = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(CozyErrorListActivity.this).inflate(R.layout.adapter_feeder_list, parent, false);
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
            viewHolder.state.setText(position < mCozysError.getMac().size() ? "MAC地址重复" : "SN重复");

            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView date;
            TextView state;
        }
    }
}
