package com.petkit.matetool.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FocusTestActivity extends BaseActivity {
	private ImageView image;
	private boolean needReceive;

	private ProgressBar progressBar;
	private DatagramSocket dataSocket = null;

	private TextView value;
	private Button check_value;

	private BroadcastReceiver mBroadcastReceiver;

    private int mCurCaseMode;
    private int mateStyle;
    private int workStation;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = savedInstanceState.getInt(DatagramConsts.EXTRA_CURRENT_MODE, -1);
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = getIntent().getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, -1);
        }

		setContentView(R.layout.layout_focustest);
	}


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
        outState.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
    }

	private void closeUdpSocket() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (dataSocket != null) {
					dataSocket.close();
					dataSocket = null;
				}
			}
		}, 600);
		System.gc();
	}

	String path = Globals.localUrl + "image1/";
	String path0 = path + "0.jpg";

	private class MyThread extends Thread {
		public void run() {
			int index = 0;

			FileOutputStream out = null;
			// closeUdpSocket();

			try {
				dataSocket = new DatagramSocket(Utils.IMAGE_PORT);
				byte[] receiveByte = new byte[1500];
				DatagramPacket dataPacket = new DatagramPacket(receiveByte, receiveByte.length);

				File file = new File(path0);
				if (file.exists()) {
					file.delete();
				}
				
				out = new FileOutputStream(path0, true);

				while (needReceive) {
					dataSocket.receive(dataPacket);

					try {
						byte[] id = new byte[] { 0, 0, 0, 0 };
						int size = dataPacket.getLength();

						if (size > 0) {
							byte[] data = dataPacket.getData();
							id[2] = data[0];
							id[3] = data[1];

							int d = Utils.bytesToInt2(id, 0);

							if (d == 0) {
								index++;
								out = new FileOutputStream(path + index + ".jpg", true);
								Message message = new Message();
								message.what = index - 1;
								mHandler.sendMessage(message);
							}

							out.write(data, 2, size - 2);
							out.flush();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (dataSocket != null) {
					dataSocket.close();
					dataSocket = null;
				}
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (msg.what >= 0 && needReceive) {
					String path1 = path + msg.what + ".jpg";
					image.setImageBitmap(BitmapFactory.decodeFile(path1));
					
					final String path2 = path1;
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							File file = new File(path2);
							file.delete();
						}
					}, 1000);
					
					System.gc();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void showProgressBar(boolean show) {
		if (show) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		stopVideo();
		needReceive = false;
		// make sure unmoni send out....
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				setResult(RESULT_OK);
				finish();
			}
		}, 200);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utils.sendUnmoniCMD(FocusTestActivity.this);
		closeUdpSocket();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}

    @Override
    protected void setupViews() {
        setTitle(R.string.Focus_mode_images);

        image = (ImageView) findViewById(R.id.imageView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        showProgressBar(false);
        check_value = (Button) findViewById(R.id.check_value);
        check_value.setText("长按校准:" + Utils.check_value);
        check_value.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                Utils.check_value = Utils.video_value;
                check_value.setText("校准:" + Utils.check_value);
                return false;
            }
        });

        value = (TextView) findViewById(R.id.video_value);
        initFile();
        needReceive = false;
        registerBoradcastReceiver();
        startVideo();

        registerBoradcastReceiver();
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    private void registerBoradcastReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
                if (DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);
                    switch (progress){
                        case DatagramConsts.VIDEO_VALUE:
                            value.setText(Utils.video_value + "/" + Utils.video_value1);
                            break;
                        case DatagramConsts.WRITEOK:
                            finish();
                            break;
                    }
                }
			}
		};

		IntentFilter filter = new IntentFilter();
        filter.addAction(DatagramConsts.BROADCAST_PROGRESS);
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
	}

	private void startVideo() {
		if (!needReceive) {
				needReceive = true;
				new MyThread().start();
				Utils.sendData(FocusTestActivity.this,
						Utils.SERVER_RTP_SESSION_CREATE_IMG, true);
		}
	}

	private void stopVideo() {
			needReceive = false;
			Utils.sendData(FocusTestActivity.this, Utils.SERVER_RTP_SESSION_STOP, false);
	}

	private void initFile() {
		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.mkdirs();
		} else {
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				for (File f : childFile) {
					f.delete();
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.button1:
            if(mCurCaseMode == Globals.FocusTestMode) {
                Utils.sendData(this, Utils.SERVER_TEST_FOCUS, false);
            } else if(mCurCaseMode == Globals.FocusTestMode2) {
                Utils.sendData(this, Utils.SERVER_TEST_FOCUS2, false);
            }
			break;
			
		case R.id.button2:
            if(mCurCaseMode == Globals.FocusTestMode) {
                Utils.sendData(this, Utils.SERVER_TEST_EXIT_TO_BORAD, false);
            } else if(mCurCaseMode == Globals.FocusTestMode2) {
                Utils.sendData(this, Utils.SERVER_TEST_EXIT_TO_FOCUS, false);
            }
			break;
		}
	}
}