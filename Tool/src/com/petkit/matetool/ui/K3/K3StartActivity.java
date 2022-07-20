package com.petkit.matetool.ui.K3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.Globals;

/**
 * K3测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class K3StartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_select_mode);

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText(getString(R.string.Feeder_test_info_format));

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case1).setVisibility(View.GONE);

        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case3).setOnClickListener(this);
        findViewById(R.id.test_case4).setOnClickListener(this);
        findViewById(R.id.test_case5).setOnClickListener(this);
        findViewById(R.id.test_case6).setOnClickListener(this);
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt("TestType", Globals.TYPE_TEST_PARTIALLY);
                startActivityWithData(K3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt("TestType", Globals.TYPE_TEST);
                startActivityWithData(K3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt("TestType", Globals.TYPE_MAINTAIN);
                startActivityWithData(K3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt("TestType", Globals.TYPE_CHECK);
                startActivityWithData(K3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                startActivityWithData(K3StorageFileActivity.class, bundle, false);
                break;
            case R.id.test_case6:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt("TestType", Globals.TYPE_AFTERMARKET);
                startActivityWithData(K3ScanActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
