package com.petkit.matetool.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.Globals;

/**
 * 测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class BLEStartActivity extends BaseActivity {

    private Tester mTester;
    private int mDeviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(DeviceCommonUtils.EXTRA_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(DeviceCommonUtils.EXTRA_TESTER);
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
        outState.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_select_mode);

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText(getString(R.string.Feeder_test_info_format));

        findViewById(R.id.test_case0).setOnClickListener(this);
        findViewById(R.id.test_case0).setVisibility(View.GONE);
        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case1).setVisibility(View.GONE);

        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case3).setOnClickListener(this);
        findViewById(R.id.test_case4).setOnClickListener(this);
        findViewById(R.id.test_case5).setOnClickListener(this);
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case6).setOnClickListener(this);

        switch (mDeviceType) {
            case Globals.K3:
            case Globals.AQ1S:
            case Globals.W4X:
            case Globals.W5N:
            case Globals.AQR:
            case Globals.CTW2:
                findViewById(R.id.test_case1).setVisibility(View.GONE);
                findViewById(R.id.test_case4).setVisibility(View.GONE);
                findViewById(R.id.test_case5).setVisibility(View.GONE);
                break;
            case Globals.HG:
            case Globals.HG_110V:
                findViewById(R.id.test_case1).setVisibility(View.VISIBLE);
                findViewById(R.id.test_case0).setVisibility(View.VISIBLE);
//                findViewById(R.id.test_case4).setVisibility(View.GONE);
                findViewById(R.id.test_case5).setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_TEST_PARTIALLY);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
            case R.id.test_case0:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_TEST_MAINBOARD);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_TEST);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_MAINTAIN);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_CHECK);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(BLEStorageFileActivity.class, bundle, false);
                break;
            case R.id.test_case6:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_AFTERMARKET);
                startActivityWithData(BLEScanActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
