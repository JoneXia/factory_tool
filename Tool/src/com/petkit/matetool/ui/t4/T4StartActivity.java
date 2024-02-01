package com.petkit.matetool.ui.t4;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.Globals;

/**
 * 猫厕所测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class T4StartActivity extends BaseActivity {

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
        findViewById(R.id.test_case5).setOnClickListener(this);
        findViewById(R.id.test_case6).setOnClickListener(this);

        ((Button) findViewById(R.id.test_case5)).setText("位图生成");
        ((Button) findViewById(R.id.test_case5)).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_TEST_PARTIALLY);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_TEST);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_MAINTAIN);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_CHECK);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_CHECK);
                startActivityWithData(T4LanguageActivity.class, bundle, false);
                break;
            case R.id.test_case6:
                bundle = new Bundle();
                bundle.putSerializable(DeviceCommonUtils.EXTRA_TESTER, mTester);
                bundle.putInt(DeviceCommonUtils.EXTRA_TEST_TYPE, Globals.TYPE_AFTERMARKET);
                bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
