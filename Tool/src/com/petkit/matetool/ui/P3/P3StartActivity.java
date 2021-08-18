package com.petkit.matetool.ui.P3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.Globals;

/**
 * P3测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class P3StartActivity extends BaseActivity {

    private Tester mTester;
    private int mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
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
        Bundle bundle = new Bundle();
        bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
        bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);

        switch (v.getId()) {
            case R.id.test_case1:
                bundle.putInt("TestType", Globals.TYPE_TEST_PARTIALLY);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle.putInt("TestType", Globals.TYPE_TEST);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle.putInt("TestType", Globals.TYPE_MAINTAIN);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle.putInt("TestType", Globals.TYPE_CHECK);
                startActivityWithData(P3ScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                startActivityWithData(P3StorageFileActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
