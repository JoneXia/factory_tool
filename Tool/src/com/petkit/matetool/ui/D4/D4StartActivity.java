package com.petkit.matetool.ui.D4;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D4.utils.D4Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;

/**
 * D3测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class D4StartActivity extends BaseActivity {

    private Tester mTester;

    private int mDeviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(D4Utils.EXTRA_D4_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(D4Utils.EXTRA_D4_TESTER);
        }

        if(savedInstanceState != null) {
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
        }
        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        outState.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                bundle.putInt("TestType", D4Utils.TYPE_TEST_PARTIALLY);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(D4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                bundle.putInt("TestType", D4Utils.TYPE_TEST);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(D4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                bundle.putInt("TestType", D4Utils.TYPE_MAINTAIN);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(D4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                bundle.putInt("TestType", D4Utils.TYPE_CHECK);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(D4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(D4StorageFileActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
