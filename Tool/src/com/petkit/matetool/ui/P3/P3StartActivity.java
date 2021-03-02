package com.petkit.matetool.ui.P3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.P3.utils.P3Utils;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * P3测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class P3StartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(P3Utils.EXTRA_P3_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(P3Utils.EXTRA_P3_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
                bundle.putInt("TestType", P3Utils.TYPE_TEST_PARTIALLY);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
                bundle.putInt("TestType", P3Utils.TYPE_TEST);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
                bundle.putInt("TestType", P3Utils.TYPE_MAINTAIN);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
                bundle.putInt("TestType", P3Utils.TYPE_CHECK);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(P3Utils.EXTRA_P3_TESTER, mTester);
                startActivityWithData(P3StorageFileActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
