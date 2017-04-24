package com.petkit.matetool.ui.feeder;

import android.os.Bundle;
import android.view.View;

import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;

/**
 * Created by Jone on 17/4/24.
 */

public class FeederMainTestActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feeder_main_test);
    }

    @Override
    protected void setupViews() {
        setTitle("喂食器测试");


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onClick(View v) {

    }
}
