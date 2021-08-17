package com.petkit.matetool.ui.W5;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * W5测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class W5StartActivity extends BaseActivity {

    private Tester mTester;
    private int mW5Type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mW5Type = savedInstanceState.getInt(W5Utils.EXTRA_W5_TYPE);
            mTester = (Tester) savedInstanceState.getSerializable(W5Utils.EXTRA_W5_TESTER);
        } else {
            mW5Type = getIntent().getIntExtra(W5Utils.EXTRA_W5_TYPE, W5Utils.W5_TYPE_NORMAL);
            mTester = (Tester) getIntent().getSerializableExtra(W5Utils.EXTRA_W5_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
        outState.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_select_mode);

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText(getString(R.string.Feeder_test_info_format));

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case3).setOnClickListener(this);
        findViewById(R.id.test_case4).setOnClickListener(this);
        findViewById(R.id.test_case5).setOnClickListener(this);
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case1).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt("TestType", W5Utils.TYPE_TEST_PARTIALLY);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
                startActivityWithData(W5ScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt("TestType", W5Utils.TYPE_TEST);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
                startActivityWithData(W5ScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt("TestType", W5Utils.TYPE_MAINTAIN);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
                startActivityWithData(W5ScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                bundle.putInt("TestType", W5Utils.TYPE_CHECK);
                bundle.putInt(W5Utils.EXTRA_W5_TYPE, mW5Type);
                startActivityWithData(W5ScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(W5Utils.EXTRA_W5_TESTER, mTester);
                startActivityWithData(W5StorageFileActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
