package com.petkit.matetool.ui.AQR;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.AQR.utils.AQRUtils;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * AQR测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class AQRStartActivity extends BaseActivity {

    private Tester mTester;
    private int mAQRType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mAQRType = savedInstanceState.getInt(AQRUtils.EXTRA_AQR_TYPE);
            mTester = (Tester) savedInstanceState.getSerializable(AQRUtils.EXTRA_AQR_TESTER);
        } else {
            mAQRType = getIntent().getIntExtra(AQRUtils.EXTRA_AQR_TYPE, AQRUtils.AQR_TYPE_NORMAL);
            mTester = (Tester) getIntent().getSerializableExtra(AQRUtils.EXTRA_AQR_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(AQRUtils.EXTRA_AQR_TYPE, mAQRType);
        outState.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
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
                bundle.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
                bundle.putInt("TestType", AQRUtils.TYPE_TEST_PARTIALLY);
                bundle.putInt(AQRUtils.EXTRA_AQR_TYPE, mAQRType);
                startActivityWithData(AQRScanActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
                bundle.putInt("TestType", AQRUtils.TYPE_TEST);
                bundle.putInt(AQRUtils.EXTRA_AQR_TYPE, mAQRType);
                startActivityWithData(AQRScanActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
                bundle.putInt("TestType", AQRUtils.TYPE_MAINTAIN);
                bundle.putInt(AQRUtils.EXTRA_AQR_TYPE, mAQRType);
                startActivityWithData(AQRScanActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
                bundle.putInt("TestType", AQRUtils.TYPE_CHECK);
                bundle.putInt(AQRUtils.EXTRA_AQR_TYPE, mAQRType);
                startActivityWithData(AQRScanActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(AQRUtils.EXTRA_AQR_TESTER, mTester);
                startActivityWithData(AQRStorageFileActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
