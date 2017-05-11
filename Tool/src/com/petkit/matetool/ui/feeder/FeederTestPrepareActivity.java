package com.petkit.matetool.ui.feeder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.matetool.R;
import com.petkit.matetool.http.AsyncHttpRespHandler;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.mode.FeederTester;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * 喂食器测试，选择工站
 *
 * Created by Jone on 17/4/19.
 */
public class FeederTestPrepareActivity extends BaseActivity {

    private FeederTester mTester;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feeder_prepare);

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
                if(mTester == null) {
                    mTester = new FeederTester();
                    mTester.setName("安迪信");
                    mTester.setCode("OA");
                    mTester.setStation("1");
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(FeederUtils.EXTRA_FEEDER_TESTER, mTester);
                startActivityWithData(FeederStartActivity.class, bundle, false);
                break;
            case R.id.logout:

                break;
        }
    }


    private void login() {
        HashMap<String, String> params = new HashMap<>();

        AsyncHttpUtil.post("", params, new AsyncHttpRespHandler(this, true) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);


            }
        });
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<>();

        AsyncHttpUtil.post("", params, new AsyncHttpRespHandler(this, true) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);


            }
        });
    }

    private void uploadSn() {
        HashMap<String, String> params = new HashMap<>();

        AsyncHttpUtil.post("", params, new AsyncHttpRespHandler(this) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);


            }
        });
    }

}
