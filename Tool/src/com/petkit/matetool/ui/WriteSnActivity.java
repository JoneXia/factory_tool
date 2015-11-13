package com.petkit.matetool.ui;

import java.net.SocketException;
import java.net.UnknownHostException;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.R;
import com.petkit.matetool.widget.LoadDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class WriteSnActivity extends BaseActivity {

	EditText writeSN;

    private int mateStyle;
    private int workStation;


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

		setContentView(R.layout.activity_write_sn);

		registerBoradcastReceiver();
	}


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button1:
			WriteSN();
			break;

		default:
			break;
		}
	}
	
	
	@Override
	protected void onDestroy() {
		mHander.removeCallbacks(mRunnable);
		super.onDestroy();
	}

    @Override
    protected void setupViews() {

        writeSN = (EditText) findViewById(R.id.write_sn);
        writeSN.setText(Globals.organizationSN(this, workStation, mateStyle));

        findViewById(R.id.button1).setOnClickListener(this);
    }

    private boolean writeSucceed = true;
	
	private void WriteSN() {
		try {
			Utils.sn = writeSN.getEditableText().toString().trim();
			if(Utils.sn.length() != 15) {
				showShortToast(R.string.Hint_sn_not_valid);
				return;
			}
			Utils.receiveWriteSNData(this);
			writeSucceed = false;
			Utils.sendData(this, Utils.SERVER_TEST_MODE_WRITE_SN, false);
			
			mHander.postDelayed(mRunnable, 6000);
            showShortToast(R.string.Hint_wait_result_for_write_sn);
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			showSNdialog(false);
		}
	}
	
	private  Handler mHander = new Handler(); 
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if(!writeSucceed) {
				showSNdialog(false);
			}
		}
	};
	
	private BroadcastReceiver mBroadcastReceiver;
	private void registerBoradcastReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {

                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);

                    LoadDialog.dismissDialog();
                    switch (progress) {
                        case DatagramConsts.WRITEOK:
                            if(!writeSucceed) {
                                mHander.removeCallbacks(mRunnable);
                                writeSucceed = true;
                                showSNdialog(true);
                            }
                            break;
                        case DatagramConsts.WRITESNFAILED:
                            mHander.removeCallbacks(mRunnable);
                            showSNdialog(false);
                            break;
                    }
                }
			}
		};
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
	}
	
	public void onBackPressed() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		setResult(RESULT_OK);
	};

	AlertDialog.Builder builder = null;
	private void showSNdialog(boolean status) {

        if(status) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.Confrim_paste_sn_in_write_way);
            builder.setMessage(Utils.sn);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    WriteSnActivity.this.finish();
                }
            });
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.Hint_failed_to_write_sn);
            builder.setMessage(Utils.sn);
            builder.setPositiveButton(R.string.Retry_to_write_sn,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,  int which) {
                    dialog.dismiss();
                    WriteSN();
                }
            });
        }
        builder.create().show();

    }
}