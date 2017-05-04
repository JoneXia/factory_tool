package com.petkit.matetool.ui.feeder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dothantech.printer.IDzPrinter;
import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * 喂食器测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class FeederStartActivity extends BaseActivity {

    private int workStation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
        } else {
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
        }

        setContentView(R.layout.activity_feeder_start);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_select_mode);

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText(getString(R.string.Test_info_format, getString(R.string.Feeder), String.valueOf(workStation)));

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
                startActivityWithData(FeederTestMainActivity.class, bundle, false);
                break;
            case R.id.test_case2:

                break;
            case R.id.test_case3:

                break;
            case R.id.test_case5:
                startActivity(PrintActivity.class);
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
