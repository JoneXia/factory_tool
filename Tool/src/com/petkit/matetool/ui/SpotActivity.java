package com.petkit.matetool.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.WifiParams;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class SpotActivity extends BaseActivity {

	private final int Normal_mode = 0;
	private final int Error_mode = 1;
	private final int Loading_mode = 2;
	
	private int curMode;
	
	private EditText mEditText;
	private BroadcastReceiver mBroadcastReceiver;
	
	private boolean isBackGround = false;

    private WifiParams mWifiParams;
    private int mateStyle;
    private int workStation;
    private int mCurCaseMode;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mWifiParams = (WifiParams) savedInstanceState.getSerializable(DatagramConsts.EXTRA_WIFI_PARAMS);
            mCurCaseMode = savedInstanceState.getInt(DatagramConsts.EXTRA_CURRENT_MODE);
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mWifiParams = (WifiParams) getIntent().getSerializableExtra(DatagramConsts.EXTRA_WIFI_PARAMS);
            mCurCaseMode = getIntent().getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, 0);
        }

		setContentView(R.layout.activity_spot);
		
		registerBoradcastReceiver();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
        outState.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		
		super.onDestroy();
	}

    @Override
    protected void setupViews() {
        findViewById(R.id.send_sn).setOnClickListener(this);
        findViewById(R.id.test_retry).setOnClickListener(this);
        findViewById(R.id.spot_loading).setOnClickListener(this);

        mEditText =  (EditText) findViewById(R.id.sn);

        setMode(Normal_mode);
    }

    @Override
	protected void onStop() {
		isBackGround = true;
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		isBackGround = false;
		super.onStart();
	}
	
	private void registerBoradcastReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if(isBackGround || isFinishing()) {
					return;
				}
                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);

                    LoadDialog.dismissDialog();
                    switch (progress) {
                        case DatagramConsts.SERVER_CHECK_SYS_PASS:
                            if(Boolean.valueOf(data)){
                                if (mCurCaseMode == DatagramConsts.SpotTestMode) {
                                    Intent intent = new Intent(SpotActivity.this, MainActivity.class);
                                    intent.putExtra(DatagramConsts.EXTRA_WIFI_PARAMS, mWifiParams);
                                    intent.putExtra(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
                                    intent.putExtra(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
                                    intent.putExtra(DatagramConsts.EXTRA_WORK_STATION, workStation);
                                    startActivityForResult(intent, DatagramConsts.SpotTestMode);
                                }
                            } else {
                                setMode(Error_mode);
                            }
                            break;
                        case DatagramConsts.SERVER_WRITE_SN_FAILED:
                            Utils.showToast(SpotActivity.this, "该设备没有写入SN！！！！！");
                            setMode(Error_mode);
                            break;
                        case DatagramConsts.SERVER_CHECK_HAS_NO_SN:
                            setMode(Error_mode);
                            Utils.showToast(SpotActivity.this, "该设备没有SN！！！！！");
                            break;
                        case DatagramConsts.WRITESNFAILED:
                            Utils.showToast(SpotActivity.this, "SN 错误，请确认！");
                            break;
                    }
                }
			}
		};

		IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		setMode(Normal_mode);
	}
	
	@Override
	public void onBackPressed() {
		if(curMode == Loading_mode || curMode == Error_mode) {
			Utils.sendUnmoniCMD(SpotActivity.this);
			setMode(Normal_mode);
			
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Utils.closeSocket();
				}
			}, 600);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		
		case R.id.send_sn:
			collapseSoftInputMethod();
			String sn_temp  =  mEditText.getText().toString().trim();
			if(sn_temp.isEmpty() || !checkSN(sn_temp)) {
				Utils.showToast(this, getString(R.string.sn_error));
				return;
			}
			setMode(Loading_mode);
			String sn = sn_temp;
			
			try {
				byte[] mSpotTestRow = new byte[100];

				byte[] head = Utils.intToBytes2(Utils.SERVER_REG_MONI);
				System.arraycopy(head, 0, mSpotTestRow, 0, head.length);
				int index = head.length;

				byte[] len =  Utils.intToBytes2(100);
				System.arraycopy(len, 0, mSpotTestRow, index, len.length);
				index += len.length;


				int i = getRandomValue();
				int j = getRandomValue();

				mSpotTestRow[index++] = (byte) i;
				mSpotTestRow[index++] = (byte) j;
				mSpotTestRow[index++] =(byte)( i^j);

				System.arraycopy(sn.getBytes(), 0, mSpotTestRow, index, sn.getBytes().length);
				index += sn.getBytes().length;
				mSpotTestRow[index++] = 0x00;

		        String spotIP = getBroadcastIp();
		        if(spotIP == null) {
		        	Utils.showToast(this, "IP Ϊ�գ�");
		        }

				System.arraycopy(spotIP.getBytes(), 0, mSpotTestRow, index, spotIP.getBytes().length);
				index += spotIP.getBytes().length;

				mSpotTestRow[index++] = 0x00;

				byte[] port = Utils.intToBytes2(Utils.DEFAULT_PORT);
				System.arraycopy(port, 0, mSpotTestRow, index, port.length);

			try {
				Utils.receiveData(this);
			} catch (SocketException | UnknownHostException e) {
				e.printStackTrace();
			}
				startTimer(spotIP, mSpotTestRow);
			} catch (SocketException e) {
				stopTimer();
				setMode(Normal_mode);
				e.printStackTrace();
			}
			break;
			
		case R.id.spot_loading:
		case R.id.test_retry:
//			stopTimer();
			try {
				Utils.receiveData(this);
			} catch (SocketException | UnknownHostException e) {
				e.printStackTrace();
			}
			setMode(Normal_mode);
			break;
		}
	}
	
	private int getRandomValue() {
		int value = 0;
		while(true) {
			value = (int) (Math.random() * 100);
			if(value >=4) return value;
		}
	}

	private void stopTimer() {
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
	}

	private Timer mTimer;
	private void startTimer(final String spotIP, final byte[] row) {
		if(mTimer == null){
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						DatagramSocket ds = new DatagramSocket();
						InetAddress addr = InetAddress.getByName(spotIP);
						DatagramPacket dp = new DatagramPacket(row, row.length, addr, Utils.SPOT_PORT);
						ds.send(dp);
						ds.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 0, 1000);
		}
	}

	private String getBroadcastIp() throws SocketException {
	    System.setProperty("java.net.preferIPv4Stack", "true");
	    for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
	        NetworkInterface ni = niEnum.nextElement();
	        if (!ni.isLoopback()) {
	            for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
	            	if (interfaceAddress.getBroadcast() != null) {
	            		return interfaceAddress.getBroadcast().toString().substring(1);
					}
	            }
	        }
	    }
	    return null;
	}
	
	private void setMode(int mode) {
		curMode = mode;
		
		switch (mode) {
			case Normal_mode:
				findViewById(R.id.spot_normal).setVisibility(View.VISIBLE);
				findViewById(R.id.spot_loading).setVisibility(View.GONE);
				findViewById(R.id.spot_failed).setVisibility(View.GONE);
				break;
			case Error_mode:
				findViewById(R.id.spot_normal).setVisibility(View.GONE);
				findViewById(R.id.spot_loading).setVisibility(View.GONE);
				findViewById(R.id.spot_failed).setVisibility(View.VISIBLE);
				break;
			case Loading_mode:
				findViewById(R.id.spot_normal).setVisibility(View.GONE);
				findViewById(R.id.spot_loading).setVisibility(View.VISIBLE);
				findViewById(R.id.spot_failed).setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}
	
	private boolean checkSN(String sn) {
		if(sn.length() ==15/* && (sn.startsWith("00115")||sn.startsWith("00215"))*/) {
			return true;
		} else {
			return false;
		}
	}
	
	// Common
	private void collapseSoftInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}
}