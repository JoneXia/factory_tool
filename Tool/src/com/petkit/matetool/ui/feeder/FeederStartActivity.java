package com.petkit.matetool.ui.feeder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dothantech.printer.IDzPrinter;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;

/**
 * 喂食器测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class FeederStartActivity extends BaseActivity {

    private Tester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(FeederUtils.EXTRA_FEEDER_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(FeederUtils.EXTRA_FEEDER_TESTER);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
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
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                bundle.putInt("TestType", FeederUtils.TYPE_TEST_PARTIALLY);
                startActivityWithData(FeederTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                bundle = new Bundle();
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                bundle.putInt("TestType", FeederUtils.TYPE_TEST);
                startActivityWithData(FeederTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case3:
                bundle = new Bundle();
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                bundle.putInt("TestType", FeederUtils.TYPE_MAINTAIN);
                startActivityWithData(FeederTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case4:
                bundle = new Bundle();
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                bundle.putInt("TestType", FeederUtils.TYPE_CHECK);
                startActivityWithData(FeederTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case5:
                bundle = new Bundle();
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                startActivityWithData(FeederStorageActivity.class, bundle, false);
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
