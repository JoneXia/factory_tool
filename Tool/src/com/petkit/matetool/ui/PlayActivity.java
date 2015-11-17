package com.petkit.matetool.ui;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.android.petkit.Jni;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.service.DatagramConsts;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;
import com.petkit.matetool.R;
import com.petkit.matetool.widget.LoadDialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class PlayActivity extends BaseActivity {

    //	private Jni instance = null;
    // 向服务器注册的基本设置
    int m_width = 1280;
    int m_height = 720;

    // 来自网络端的视频预览和预览控制器
    SurfaceView surfaceView_remote = null;
    SurfaceHolder holder_remote = null;
    // 视频播放线程
    private Thread gettingVideoThread;
    boolean bIsGettingVideo = false;
    boolean bExitGettingVideo = true;

    // private Thread playAudioThread;
    // audioRecordPlay audio=new audioRecordPlay();

    // 接收每一帧视频的缓存——该缓存是时刻在存入和写出，需要对其加锁
    private final int bytesPerPackage = m_width * m_height * 2;
    private byte[] videoBufferPlay = new byte[bytesPerPackage];
    private byte[] videoBufferRemote = new byte[bytesPerPackage];

    // 视频贴图的画布
    private Canvas mCanvas = null;
    Bitmap bt = null;
    BitmapFactory.Options opt;

    private int mCurCaseMode;
    private int mateStyle;
    private int workStation;


    static
    {
        System.loadLibrary("x264");
        System.loadLibrary("avutil");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("ffmpeg");
        System.loadLibrary("PlayDemo");
    }


    private Runnable gettingVideoStream = new Runnable() {
        public void run() {
            Log.d("play", "进入了接收视频流的子线程");
            while (bIsGettingVideo == true) {
                int actualNum = 0;
                Arrays.fill(videoBufferPlay, (byte) 0);
                // 当一只收到0字节数据时，不往渲染缓存里存
                actualNum = Jni.getInstance().readVideoData(videoBufferRemote);
                if (actualNum > 0) {
                    if(!bIsGettingVideo) {
                        return;
                    }

                    System.arraycopy(videoBufferRemote, 0, videoBufferPlay, 0, actualNum);
                    ByteBuffer buffer = ByteBuffer.wrap(videoBufferPlay);// 将byte数组包装到缓冲区中
                    bt.copyPixelsFromBuffer(buffer);
                    Message msg = myHandler.obtainMessage(0xAA, actualNum, 0);
                    // 发送消息
                    myHandler.sendMessage(msg);
                }
            }
            bExitGettingVideo = true;
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(savedInstanceState != null){
            mateStyle = savedInstanceState.getInt(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = savedInstanceState.getInt(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = savedInstanceState.getInt(DatagramConsts.EXTRA_CURRENT_MODE, -1);
        } else {
            mateStyle = getIntent().getIntExtra(DatagramConsts.EXTRA_MATE_STYLE, -1);
            workStation = getIntent().getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);
            mCurCaseMode = getIntent().getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, -1);
        }


        setContentView(R.layout.activity_play);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DatagramConsts.EXTRA_MATE_STYLE, mateStyle);
        outState.putInt(DatagramConsts.EXTRA_WORK_STATION, workStation);
        outState.putInt(DatagramConsts.EXTRA_CURRENT_MODE, mCurCaseMode);
    }


    private BroadcastReceiver mBroadcastReceiver;
    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(DatagramConsts.BROADCAST_PROGRESS.equals(arg1.getAction())) {
                    int progress = arg1.getIntExtra(DatagramConsts.EXTRA_PROGRESS, 0);
                    String data = arg1.getStringExtra(DatagramConsts.EXTRA_DATA);
                    PetkitLog.d("progress : " + progress + "  data = " + data);

                    LoadDialog.dismissDialog();
                    switch (progress) {
                        case DatagramConsts.WRITEOK://data is a boolean, true is write ok
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
            Utils.sendData(this, Utils.SERVER_RTP_SESSION_CREATE, true);
    }

    private void stopVideo() {
            Utils.sendData(this, Utils.SERVER_RTP_SESSION_STOP, false);

            if(mCurCaseMode == Globals.FocusTestMode
                    || mCurCaseMode == Globals.FinalTestMode
                    ||mCurCaseMode == Globals.FocusTestMode2) {
                Utils.sendUnmoniCMD(this);
            }
    }

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0xAA:
//				synchronized (bt) {
                    if(!bIsGettingVideo) {
                        return;
                    }
//					Log.d("play", "收到了绘制消息，即将绘制一帧bitmap");
                    drawBmp(holder_remote, msg.arg1);
            }
//			}
        }
    };

    // 绘图函数
    private void drawBmp(SurfaceHolder holder, int bytesNum) {
        if(!bIsGettingVideo) {
            return;
        }
        Rect viewrect = holder.getSurfaceFrame();
        if ((viewrect.width() <= 0) || (viewrect.height() <= 0))
            return;
        Rect localrect = new Rect();

        localrect.left = 0;
        localrect.top = 0;
        localrect.right = m_height;
        localrect.bottom = m_width;

        Matrix vMatrix = new Matrix();
        vMatrix.setRotate(270);

        Bitmap vB2 = Bitmap.createBitmap(bt, 0, 0, bt.getWidth() // 宽度
                , bt.getHeight() // 高度
                , vMatrix, true);

        mCanvas = holder.lockCanvas();
        // 将bitmap绘制到canvas依附的缓存——surfaceview自己的默认缓存区 之中

        if(mCanvas != null) {
            Paint paint = new Paint();
            int flags = paint.getFlags();

            paint.setFlags(flags | Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

            mCanvas.drawBitmap(vB2, localrect, viewrect, paint);// 这里画的是旋转后的
//			mCanvas.rotate(180);
            holder.unlockCanvasAndPost(mCanvas);
        }
    }

    // 显示网络通话端视频
    private class remoteSurfaceListener implements SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        bIsGettingVideo = false;
        stopVideo();
        Jni.getInstance().releaseLib();
        Utils.sendUnmoniCMD(this);
    }

    @Override
    protected void setupViews() {
// 显示通话端视频的SurfaceView
        Jni.getInstance().initLib("", Utils.VIDEO_PORT, 4);
        setTitle(R.string.Focus_mode_video);

        surfaceView_remote = (SurfaceView) this.findViewById(R.id.surfaceView_remote);
        holder_remote = surfaceView_remote.getHolder();
        holder_remote.setFormat(PixelFormat.TRANSLUCENT);
        holder_remote.addCallback(new remoteSurfaceListener());

        opt = new BitmapFactory.Options();
        // 设定BitmapFactory.Options对象特性
        opt.inPreferredConfig = Config.RGB_565;
        opt.outHeight = m_height;
        opt.outWidth = m_width;
        bt = Bitmap.createBitmap(opt.outWidth, opt.outHeight,
                opt.inPreferredConfig);
        if (bt == null) {
            Log.d("play", "createBitmap false");
        }

        bIsGettingVideo = true;
        bExitGettingVideo = false;
        // 开启接收网络端视频的子线程
        gettingVideoThread = new Thread(gettingVideoStream);
        gettingVideoThread.start();

        // 开启音频播放子线程
        // playAudioThread = new Thread(playAudioProc);
        // playAudioThread.start();
        startVideo();

        registerBoradcastReceiver();
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button1:
                try {
                    if(mCurCaseMode == Globals.FocusTestMode) {
                        Utils.receiveWriteData(this);
                        Utils.sendData(this, Utils.SERVER_TEST_FOCUS, false);
                    } else if(mCurCaseMode == Globals.FocusTestMode2) {
                        Utils.receiveWriteData(this);
                        Utils.sendData(this, Utils.SERVER_TEST_FOCUS2, false);
                    }
                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button2:
                try {
                    if(mCurCaseMode == Globals.FocusTestMode) {
                        Utils.receiveWriteData(this);
                        Utils.sendData(this, Utils.SERVER_TEST_EXIT_TO_BORAD, false);
                    } else if(mCurCaseMode == Globals.FocusTestMode2) {
                        Utils.receiveWriteData(this);
                        Utils.sendData(this, Utils.SERVER_TEST_EXIT_TO_FOCUS, false);
                    }
                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
