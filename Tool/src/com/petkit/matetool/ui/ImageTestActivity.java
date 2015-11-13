package com.petkit.matetool.ui;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

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


/**
 *
 */
public class ImageTestActivity extends BaseActivity {
	private ImageView image;
	private boolean needReceive = false;
	private DatagramSocket dataSocket = null;

    private int mateStyle;
    private int workStation;
    private int mCurCaseMode;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = savedInstanceState.getInt(DatagramConsts.EXTRA_CURRENT_MODE);
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = getIntent().getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, 0);
        }
		setContentView(R.layout.layout_imagetest);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
    }

	private void closeUdpSocket() {
		if (dataSocket != null) {
			dataSocket.close();
			dataSocket = null;
		}
		
		System.gc();
	}

	String path = Globals.localUrl + "image1/";
	String path0 = path + "0.jpg";

	private class MyThread extends Thread {
		public void run() {
			int index = 0;

			FileOutputStream out = null;
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

				closeUdpSocket();
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
			
		stopVideo();
		closeUdpSocket();
	}

    @Override
    protected void setupViews() {
        setTitle(mCurCaseMode == Globals.SpotTestMode ? R.string.test_case4 :
                (mCurCaseMode == Globals.FinalTestMode ? R.string.test_case3 : R.string.test_case1));

        image = (ImageView) findViewById(R.id.imageView1);

        findViewById(R.id.btn_test_succeed).setOnClickListener(this);
        findViewById(R.id.btn_test_failed).setOnClickListener(this);

        if(mCurCaseMode == DatagramConsts.FinalTestMode || mCurCaseMode == DatagramConsts.SpotTestMode) {
            findViewById(R.id.cut_led).setVisibility(View.VISIBLE);
            findViewById(R.id.video).setVisibility(View.GONE);
            findViewById(R.id.cut_led1).setOnClickListener(this);
            findViewById(R.id.cut_led2).setOnClickListener(this);
            findViewById(R.id.cut_led_end).setOnClickListener(this);
        } else {
            findViewById(R.id.video).setVisibility(View.VISIBLE);
            findViewById(R.id.cut_led).setVisibility(View.GONE);
            findViewById(R.id.video_start).setOnClickListener(this);
            findViewById(R.id.video_stop).setOnClickListener(this);
        }

        initFile();
        needReceive = false;
        startVideo();
    }

    @Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_test_succeed:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.btn_test_failed:
			finish();
			break;
		case R.id.video_start:
			startVideo();
			break;
		case R.id.video_stop:
			stopVideo();
			break;
			
		case R.id.cut_led_end:
		case R.id.cut_led2:
				Utils.sendData(this, Utils.IOD_GPIO_LIGHT_LED2_OFF, false);
			break;
		case R.id.cut_led1:
				Utils.sendData(this, Utils.IOD_GPIO_LIGHT_LED2, false);
			break;
		default:
			break;
		}
	}
	
	private void startVideo() {
		if (!needReceive) {
				needReceive = true;
				new MyThread().start();
				Utils.sendData(this, Utils.SERVER_RTP_SESSION_CREATE_IMG, true);
		}
	}

	private void stopVideo() {
			needReceive = false;
			Utils.sendData(this, Utils.SERVER_RTP_SESSION_STOP_IMG, false);
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
}