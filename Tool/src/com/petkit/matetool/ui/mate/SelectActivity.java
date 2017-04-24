package com.petkit.matetool.ui.mate;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.WifiParams;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.widget.LoadDialog;

import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * 选择工站
 *
 */
public class SelectActivity extends BaseActivity {

    private BroadcastReceiver mBroadcastReceiver;

    private WifiParams mWifiParams;
    private int mCurCaseMode;
    private int mateStyle;
    private int workStation;
    private boolean focusTestImageMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
        }

		setContentView(R.layout.activity_select);

        if(mateStyle == -1){
            showShortToast(R.string.Hint_work_station_empty);
            finish();
        }

        if(workStation == -1){
            showShortToast(R.string.Hint_mate_style_empty);
            finish();
        }
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_select_mode);

        TextView testInfo = (TextView) findViewById(R.id.test_info);
        testInfo.setText(getString(R.string.Test_info_format, mateStyle == Globals.MATE_STYLE ? "Mate Style" : "Mate Pro", workStation));

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);
        findViewById(R.id.test_case3).setOnClickListener(this);
        findViewById(R.id.test_case4).setOnClickListener(this);
        findViewById(R.id.test_case5).setOnClickListener(this);
        findViewById(R.id.test_case6).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerBoradcastReceiver();
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//
//        LoadDialog.show(this, "反注册中...");
//    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterBoradcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(DatagramConsts.BROADCAST_ACTION);
        intent.putExtra(DatagramConsts.EXTRA_ACTION, DatagramConsts.ACTION_DESTROY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.test_case1:
			mCurCaseMode = Globals.BoardTestMode;
            LoadDialog.show(this, getString(R.string.Connecting));
            writeCMD(DatagramConsts.SERVER_REG_MONI, DatagramConsts.BoardTestMode);
			break;
		case R.id.test_case2:
			mCurCaseMode = Globals.FocusTestMode;
			showDialog();
			break;
		case R.id.test_case3:
			mCurCaseMode = Globals.FinalTestMode;
            LoadDialog.show(this, getString(R.string.Connecting));
            writeCMD(DatagramConsts.SERVER_REG_MONI, DatagramConsts.FinalTestMode);
			break;
		case R.id.test_case4:
            showInputCodeDialog();
			break;
		case R.id.test_case5:
			mCurCaseMode = Globals.FocusTestMode2;
			showDialog();
			break;
        case R.id.test_case6:
            startActivity(BroadcastDisplayActivity.class);
            break;
		}
	}

    private void showInputCodeDialog(){
        final View inputView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);

        TextView title = (TextView) inputView.findViewById(R.id.dialog_input_title);
        title.setText(R.string.Title_input_sn);

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(inputView)
                .setPositiveButton(R.string.OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                EditText inputEditText = (EditText) inputView.findViewById(R.id.dialog_input);
                                String sn = inputEditText.getEditableText().toString();
                                if (!Globals.checkSNValid(sn)) {
                                    showShortToast(R.string.Hint_sn_not_valid);
                                } else {
                                    mCurCaseMode = Globals.SpotTestMode;
                                    LoadDialog.show(SelectActivity.this, getString(R.string.Connecting));
                                    writeCMD(DatagramConsts.SERVER_REG_MONI, DatagramConsts.SpotTestMode, sn);
                                }
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                            }
                        }).show();
    }

	private void showDialog() {
		  new Builder(this).setTitle(R.string.Prompt)
		     .setMessage(R.string.Confirm_select_focus_mode)
                  .setPositiveButton(R.string.Focus_mode_video, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          focusTestImageMode = false;
                          LoadDialog.show(SelectActivity.this, getString(R.string.Connecting));
                          writeCMD(DatagramConsts.SERVER_REG_MONI, mCurCaseMode);
//		 			 startActivity(new Intent(SelectActivity.this, InputActivity.class));
                      }
                  }).setNegativeButton(R.string.Focus_mode_images, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  focusTestImageMode = true;
                  LoadDialog.show(SelectActivity.this, getString(R.string.Connecting));
                  writeCMD(DatagramConsts.SERVER_REG_MONI, mCurCaseMode);
//                  startActivity(new Intent(SelectActivity.this, InputActivity.class));
              }
          }).show();
    }

    private void writeCMD(int cmd, int mode){
        writeCMD(cmd, mode, null);
    }

    private void writeCMD(int cmd, int mode, String sn){
        Intent intent = new Intent(DatagramConsts.BROADCAST_ACTION);
        intent.putExtra(DatagramConsts.EXTRA_ACTION, DatagramConsts.ACTION_WRITE);
        intent.putExtra(DatagramConsts.EXTRA_WRITE_CMD, cmd);
        intent.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mode);
        if(!isEmpty(sn)){
            intent.putExtra(DatagramConsts.EXTRA_DATA, sn);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void writeClear(){
        Intent intent = new Intent(DatagramConsts.BROADCAST_ACTION);
        intent.putExtra(DatagramConsts.EXTRA_ACTION, DatagramConsts.ACTION_CLEAR);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(0x111 == requestCode){
            LoadDialog.show(this, "反注册中...", true);
            startTimeoutTimer();

//        } else if(0x112 == requestCode) {
//            LoadDialog.show(this, "反注册中...", false, new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    writeClear();
//                }
//            });
        }
    }

    private Timer mTimeoutTimer;
    private void startTimeoutTimer(){
        if(mTimeoutTimer == null){
            mTimeoutTimer = new Timer();
            mTimeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showShortToast("设备出现异常，请检查！");
                            LoadDialog.dismissDialog();
                            writeClear();
                        }
                    });
                }
            }, 5000);
        }
    }

    private void stopTimeoutTimer(){
        if(mTimeoutTimer != null){
            mTimeoutTimer.cancel();
            mTimeoutTimer = null;
        }
    }

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(isFinishing()) {
                    return;
                }

                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);
                    LogcatStorageHelper.addLog("[broadcast]progress : " + progress + "  data = " + data);

                    switch (progress){
                        case DatagramConsts.SERVER_WIFI_PARAM:
                            mWifiParams = new Gson().fromJson(data, WifiParams.class);
                            break;
                        case DatagramConsts.SERVER_REG_MONI:
                            if(!isEmpty(data) && Boolean.valueOf(data)){
                                showShortToast(R.string.Regist_successful);
                            }
                            break;
                        case DatagramConsts.SERVER_TEST_MODE:
                            LoadDialog.dismissDialog();
                            if(isEmpty(data) || !Boolean.valueOf(data)){
                                showShortToast(R.string.Entry_test_mode_failed);
                            } else {
                                switch (mCurCaseMode){
                                    case Globals.BoardTestMode:
                                    case Globals.FinalTestMode:
//                                        Bundle bundle = new Bundle();
//                                        bundle.putSerializable(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
//                                        bundle.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
//                                        bundle.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
//                                        bundle.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
//                                        startActivityWithData(MainActivity.class, bundle, false);

                                        Intent intent1 = new Intent(SelectActivity.this, MainActivity.class);
                                        intent1.putExtra(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
                                        intent1.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                                        intent1.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                                        intent1.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                                        startActivityForResult(intent1, 0x111);
                                        break;
                                    case Globals.SpotTestMode:
                                        Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                                        intent.putExtra(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
                                        intent.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                                        intent.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                                        intent.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                                        startActivityForResult(intent, 0x111);
                                        break;
                                    default:
//                                        Bundle bundle1 = new Bundle();
//                                        bundle1.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
//                                        bundle1.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
//                                        bundle1.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
//                                        startActivityWithData(focusTestImageMode ? FocusTestActivity.class : PlayActivity.class,
//                                                       bundle1, false);

                                        Intent intent2 = new Intent(SelectActivity.this, focusTestImageMode ? FocusTestActivity.class : PlayActivity.class);
                                        intent2.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                                        intent2.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                                        intent2.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                                        startActivityForResult(intent2, 0x111);
                                        break;
                                }
                            }
                            break;
                        case DatagramConsts.DATAGRAM_DESTROY:
                            LoadDialog.dismissDialog();
                            showShortToast(R.string.Test_canceled);
                            finish();
                            break;
                        case DatagramConsts.SERVER_CHECK_SYS_PASS:
                            if(!Boolean.valueOf(data)){
                                LoadDialog.dismissDialog();
                                showLongToast(R.string.Regist_failed);
                                finish();
                            } else {
                                stopTimeoutTimer();
                                LoadDialog.dismissDialog();
                            }
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBoradcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


}
