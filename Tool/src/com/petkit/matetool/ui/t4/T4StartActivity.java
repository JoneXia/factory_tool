package com.petkit.matetool.ui.t4;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.t4.utils.T4Utils;

/**
 * 猫厕所测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class T4StartActivity extends BaseActivity {

    private Tester mTester;
    private int mWithK3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(T4Utils.EXTRA_T4_TESTER);
            mWithK3 = savedInstanceState.getInt(T4Utils.EXTRA_WITH_K3);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(T4Utils.EXTRA_T4_TESTER);
            mWithK3 = getIntent().getIntExtra(T4Utils.EXTRA_WITH_K3, 0);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
        outState.putInt(T4Utils.EXTRA_WITH_K3, mWithK3);
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

        ((Button) findViewById(R.id.test_case5)).setText("位图生成");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
                bundle.putInt("TestType", T4Utils.TYPE_TEST_PARTIALLY);
                bundle.putInt(T4Utils.EXTRA_WITH_K3, mWithK3);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
                bundle.putInt("TestType", T4Utils.TYPE_TEST);
                bundle.putInt(T4Utils.EXTRA_WITH_K3, mWithK3);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
                bundle.putInt("TestType", T4Utils.TYPE_MAINTAIN);
                bundle.putInt(T4Utils.EXTRA_WITH_K3, mWithK3);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
                bundle.putInt("TestType", T4Utils.TYPE_CHECK);
                bundle.putInt(T4Utils.EXTRA_WITH_K3, mWithK3);
                startActivityWithData(T4TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(T4Utils.EXTRA_T4_TESTER, mTester);
                bundle.putInt("TestType", T4Utils.TYPE_CHECK);
                startActivityWithData(T4LanguageActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
