package com.petkit.matetool.ui.K2;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * K2测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class K2StartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(K2Utils.EXTRA_K2_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(K2Utils.EXTRA_K2_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
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
        findViewById(R.id.test_case5).setVisibility(View.GONE);
        findViewById(R.id.test_case6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                bundle.putInt("TestType", K2Utils.TYPE_TEST_PARTIALLY);
                startActivityWithData(K2TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                bundle.putInt("TestType", K2Utils.TYPE_TEST);
                startActivityWithData(K2TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                bundle.putInt("TestType", K2Utils.TYPE_MAINTAIN);
                startActivityWithData(K2TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                bundle.putInt("TestType", K2Utils.TYPE_CHECK);
                startActivityWithData(K2TestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                startActivityWithData(K2StorageActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
