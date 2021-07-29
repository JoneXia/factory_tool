package com.petkit.matetool.ui.t3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.t3.utils.T3Utils;

/**
 * 猫厕所测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class T3StartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(T3Utils.EXTRA_T3_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(T3Utils.EXTRA_T3_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
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
        ((Button) findViewById(R.id.test_case5)).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                bundle.putInt("TestType", T3Utils.TYPE_TEST_PARTIALLY);
                startActivityWithData(T3TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                bundle.putInt("TestType", T3Utils.TYPE_TEST);
                startActivityWithData(T3TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                bundle.putInt("TestType", T3Utils.TYPE_MAINTAIN);
                startActivityWithData(T3TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                bundle.putInt("TestType", T3Utils.TYPE_CHECK);
                startActivityWithData(T3TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                startActivityWithData(T3LanguageActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
