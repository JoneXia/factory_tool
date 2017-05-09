package com.petkit.matetool.ui.feeder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * 喂食器测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class FeederTestPrepareActivity extends BaseActivity {

    private int workStation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
        } else {
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
        }

        setContentView(R.layout.activity_feeder_prepare);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_prepare);

        TextView testInfo = (TextView) findViewById(R.id.test_prompt);
        testInfo.setText("请先完成登录");

        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Bundle bundle = new Bundle();
                bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
                startActivityWithData(FeederStartActivity.class, bundle, false);
                break;
            case R.id.logout:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
