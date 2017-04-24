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

        if(workStation == -1){
            showShortToast(R.string.Hint_mate_style_empty);
            finish();
        }

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
        findViewById(R.id.test_case4).setOnClickListener(this);
        findViewById(R.id.test_case5).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_case1:

                break;
            case R.id.test_case2:

                break;
            case R.id.test_case3:

                break;
            case R.id.test_case4:

                break;
            case R.id.test_case5:
                startActivity(PrintActivity.class);
                break;
        }
    }
}
