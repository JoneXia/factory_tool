package com.petkit.matetool.ui.cozy;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dothantech.printer.IDzPrinter;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;

/**
 * 猫窝测试，选择工站
 *
 * Created by Jone on 17/11/30.
 */
public class CozyStartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(CozyUtils.EXTRA_COZY_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(CozyUtils.EXTRA_COZY_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
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
                bundle.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
                bundle.putInt("TestType", CozyUtils.TYPE_TEST_PARTIALLY);
                startActivityWithData(CozyTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
                bundle.putInt("TestType", CozyUtils.TYPE_TEST);
                startActivityWithData(CozyTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
                bundle.putInt("TestType", CozyUtils.TYPE_MAINTAIN);
                startActivityWithData(CozyTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
                bundle.putInt("TestType", CozyUtils.TYPE_CHECK);
                startActivityWithData(CozyTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(CozyUtils.EXTRA_COZY_TESTER, mTester);
                startActivityWithData(CozyStorageActivity.class, bundle, false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 应用退出时，调用IDzPrinter对象的quit方法断开打印机连接
        IDzPrinter.Factory.getInstance().quit();
    }



}
