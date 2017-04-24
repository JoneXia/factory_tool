package com.petkit.matetool.ui.mate;

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
import android.widget.EditText;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.net.SocketException;
import java.net.UnknownHostException;

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
            String sn = writeSN.getEditableText().toString().trim();

            if(!Globals.checkSNValid(sn)){
                showShortToast(R.string.Hint_sn_not_valid);
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("SN", sn);
            setResult(RESULT_OK, intent);
            finish();
//			WriteSN();
			break;

		default:
			break;
		}
	}

    @Override
    protected void setupViews() {

        setTitle("输入SN");

        writeSN = (EditText) findViewById(R.id.write_sn);
        writeSN.setText(Globals.organizationSN(this, workStation, mateStyle));

        findViewById(R.id.button1).setOnClickListener(this);
    }
}