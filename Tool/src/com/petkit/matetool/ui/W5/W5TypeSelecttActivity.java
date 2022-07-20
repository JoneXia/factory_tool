package com.petkit.matetool.ui.W5;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * W5测试，选择设备类型，包括W5、W5C
 *
 * Created by Jone on 17/4/19.
 */
public class W5TypeSelecttActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(W5Utils.EXTRA_W5_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(W5Utils.EXTRA_W5_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        setTitle("选择设备型号");

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText("请选择待测试设备的型号");

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case1).setVisibility(View.VISIBLE);
        findViewById(R.id.test_case3).setVisibility(View.GONE);
        findViewById(R.id.test_case4).setVisibility(View.GONE);
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case6).setOnClickListener(this);

        ((TextView) findViewById(R.id.test_case1)).setText("智能饮水机（W5）");
        ((TextView) findViewById(R.id.test_case2)).setText("智能饮水机Mini（W5C）");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, W5Utils.W5_TYPE_NORMAL);
                startActivityWithData(W5StartActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, W5Utils.W5_TYPE_MINI);
                startActivityWithData(W5StartActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
