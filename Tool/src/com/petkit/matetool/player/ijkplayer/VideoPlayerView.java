package com.petkit.matetool.player.ijkplayer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.utils.UiUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_COMPLETED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING;

public class VideoPlayerView extends LinearLayout implements TextureView.SurfaceTextureListener {

    public com.petkit.matetool.player.ijkplayer.H3TextureView textureView;
    public IMediaPlayer mPlayer;

    //默认params
    public ViewGroup.LayoutParams mDefaultParams;
    private Handler mHandler;
    private com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler mNetWorkSpeedHandler;

    public final long SPEED_DELAY = 1000L;
    private AudioManager mAudioManager;

    private com.petkit.matetool.player.ijkplayer.VideoListener videoListener;

    //初始亮度
    public int mStartLight = 0;
    //初始音量
    public int mDefaultVolume = 0;


    private Activity activity;
    private Surface mSurface;

    private boolean mIsAutoPlay = true;
    //是否静音
    private boolean isMute;

    //播放状态
    public int mPlayState = com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_IDLE;

    //播放模式
    public int mPlayMode = com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_NORMAL;

    //播放的视频
    private com.petkit.matetool.player.ijkplayer.VideoData mVideoData;

    private String mSpeed;
    private Runnable mRunnable;
    //缓存进度
    public int mBufferPercent = 0;

    //滑动初始进度
    public long mStartPosition = 0L;

    //播放进度
    public long mPosition = 0L;
    private String mPath;
    private boolean isRecording;

    private int originWidth;
    private int originHeight;
    private ViewGroup.LayoutParams layoutParams;
    private int pointerCount;
    private int pointerWidthDistance;
    private int pointerHeightDistance;
    private int lastX;
    private int lastY;
    private boolean doublePointer;
    private OnTouchListener ontl;
    private int textureViewWidth;
    private int textureViewHeight;
    private int originTextureViewWidth;
    private int originTextureViewHeight;
    private int downX;
    private int downY;


    private boolean isLive;
    private float speed = 1.0f;

    private FrameLayout rootChildLayout;
    private boolean isH3Home;

    //默认的视频宽度
    private int defaultVideoWidth;
    //默认的视频宽度
    private int defaultVideoHeight;

    private boolean initDefaultParams = false;

    private AudioFocusRequest audioFocusRequest;
    private boolean audioHasFocusRequest;
    private long videoTouchDownStartTime;
    private boolean longClickStart;
    private final static long LONG_CLICK_TIME = 500;

    private MainHandler mainHandler;

    private static final String TAG = VideoPlayerView.class.getSimpleName();

    public VideoPlayerView(Context context) {
        super(context);
        this.activity = (Activity) context;
        initControllerView(context, null);
    }

    public VideoPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.activity = (Activity) context;
        initControllerView(context, attrs);

    }

    public VideoPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        initControllerView(context, attrs);

    }

    /**
     * 设置播放视频的宽高，当无视频时使用
     * @param defaultVideoWidth 视频的宽度
     * @param defaultVideoHeight 视频的高度
     */
    public void setDefaultVideoWidthHeight(int defaultVideoWidth, int defaultVideoHeight){
        this.defaultVideoWidth = defaultVideoWidth;
        this.defaultVideoHeight = defaultVideoHeight;
    }

    private void initControllerView(Context context, AttributeSet attrs) {
        View mView = LayoutInflater.from(context).inflate(R.layout.layout_video_player, this);

        rootChildLayout = (FrameLayout) ((LinearLayout)((LinearLayout)mView).getChildAt(0)).getChildAt(0);

        initOnTouchListener();

        textureView = mView.findViewById(R.id.ttv_video_player);

        mHandler = new Handler(Looper.getMainLooper());
        mNetWorkSpeedHandler = new com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler(context, SPEED_DELAY);

        initListener(context);
        textureView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                PetkitLog.d(VideoPlayerView.class.getSimpleName(), "event.getAction():"+event.getAction());
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        videoTouchDownStartTime = System.currentTimeMillis();

                        lastX = (int) event.getX();
                        lastY = (int) event.getY();
                        downX = (int) event.getX();
                        downY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (downX!=-1 && downY!=-1 && Math.abs(event.getX()-downX) < UiUtils.dip2px(activity,10) &&  Math.abs(event.getY()-downY) < UiUtils.dip2px(activity,10)){
                            if (System.currentTimeMillis() - videoTouchDownStartTime < 500){
                                videoListener.onVideoClick();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        initAudio();

        mPlayer = new IjkMediaPlayer();
        mainHandler = new MainHandler(getContext());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!initDefaultParams){
            if (getLayoutParams() instanceof RelativeLayout.LayoutParams){
                if (getWidth()> getHeight() && getWidth() > 0 && getHeight()> 0){
                    mDefaultParams = new RelativeLayout.LayoutParams(getWidth(), getHeight());
                    initDefaultParams = true;
                }
            } else if (getLayoutParams() instanceof LinearLayout.LayoutParams){
                if (getWidth()> getHeight() && getWidth() > 0 && getHeight()> 0){
                    mDefaultParams = new LinearLayout.LayoutParams(getWidth(), getHeight());
                    initDefaultParams = true;
                }
            } else {
                if (getWidth()> getHeight() && getWidth() > 0 && getHeight()> 0) {
                    mDefaultParams = new ViewGroup.LayoutParams(getWidth(), getHeight());
                    initDefaultParams = true;
                }
            }
        }
//        mDefaultParams = getLayoutParams();
//        if (mDefaultParams.height == ViewGroup.LayoutParams.MATCH_PARENT){
//            mDefaultParams.height = Math.round(getMeasuredWidth()*10f/16);
//        }
    }

    /**
     * 适用于多段视频中由于创建的时候没有竖屏状态的宽高的情况
     * @param w
     * @param h
     */
    public void setDefaultParams(int w, int h){
        if (getLayoutParams() instanceof RelativeLayout.LayoutParams){
            mDefaultParams = new RelativeLayout.LayoutParams(w, h);
        } else if (getLayoutParams() instanceof LinearLayout.LayoutParams){
            mDefaultParams = new LinearLayout.LayoutParams(w, h);
        } else {
            mDefaultParams = new ViewGroup.LayoutParams(w, h);
        }
        initDefaultParams = true;
    }

    public ViewGroup.LayoutParams getDefaultParams() {
        return mDefaultParams;
    }

    private void initOnTouchListener(){
        ontl = (view, event)->{
            switch (event.getAction()  & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    videoTouchDownStartTime = System.currentTimeMillis();
                    longClickStart = false;
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            longClickStart = true;
                            videoListener.onVideoLongClick();
                        }
                    },LONG_CLICK_TIME);

                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    downX = (int) event.getX();
                    downY = (int) event.getY();
                    pointerCount = 1;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() == 2){
                        pointerWidthDistance = (int) Math.abs(event.getX(0) - event.getX(1));
                        pointerHeightDistance = (int) Math.abs(event.getY(0) - event.getY(1));
                    } else {
                        pointerWidthDistance = 0;
                        pointerHeightDistance = 0;
                    }
                    pointerCount++;

                    if (pointerCount == 2){
                        //双指触摸后不触发点击事件
                        downX = -1;
                        downY = -1;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    pointerCount--;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (pointerCount == 1 && !doublePointer){
//                        boolean enableScroll = textureViewWidth >= originWidth && textureViewHeight >= originHeight;
//                        if (!enableScroll)break;


                        int x = (int) (event.getX() - lastX);
                        int y = (int) (event.getY() - lastY);
                        System.out.println("x->"+x+",y->"+y);
                        //0-(10-20)>0
                        if (textureView.getLeft() - (textureView.getScroller().getFinalX()+(-x)) >0){
                            x = -(textureView.getLeft() - textureView.getScroller().getFinalX());
                        } else if (textureView.getRight() - (textureView.getScroller().getFinalX()+(-x)) < originWidth){
                            x = -(textureView.getRight() - originWidth - textureView.getScroller().getFinalX());
                        }

                        if (textureView.getTop() - (textureView.getScroller().getFinalY()+(-y)) >0){
                            y = -(textureView.getTop() - textureView.getScroller().getFinalY());
                        } else if (textureView.getBottom() - (textureView.getScroller().getFinalY()+(-y)) < originHeight){
                            y = -(textureView.getBottom() - originHeight - textureView.getScroller().getFinalY());
                        }

                        if (Math.abs(x) >1 || Math.abs(y)>1){
                            textureView.getScroller().startScroll(textureView.getScroller().getFinalX(),textureView.getScroller().getFinalY(),-x,-y);
                            textureView.invalidate();
                        }
                        lastX = (int) event.getX();
                        lastY = (int) event.getY();
                    } else if (pointerCount == 2){
                        doublePointer = true;
                        int a = (int) Math.abs(event.getX(0) - event.getX(1)) - pointerWidthDistance;
                        int b = (int) Math.abs(event.getY(0) - event.getY(1)) - pointerHeightDistance;
                        //增加滑动效果
                        a*=2;
                        b*=2;

                        PetkitLog.d(VideoPlayerView.class.getSimpleName(), "double point scale width difference："+a+",height difference："+b);

                        ViewGroup.LayoutParams lp = textureView.getLayoutParams();

                        float ratio;
                        if (Math.abs(a)>Math.abs(b)){
                            ratio = (textureViewWidth+a)*1.0f/ textureViewWidth;
                            textureViewWidth += a;
                            textureViewHeight *= ratio;
                        } else {
                            ratio = (textureViewHeight+b)*1.0f/ textureViewHeight;
                            textureViewHeight += b;
                            textureViewWidth *= ratio;
                        }

                        PetkitLog.d(VideoPlayerView.class.getSimpleName(), "ratio->"+ratio);

                        if (textureViewWidth < originWidth){
                            textureViewWidth = originWidth;
                        } else if ( textureViewWidth > originWidth*2){
                            textureViewWidth = originWidth*2;
                        }

                        if (textureViewHeight < originHeight){
                            textureViewHeight = originHeight;
                        } else if ( textureViewHeight > originHeight*2){
                            textureViewHeight = originHeight*2;
                        }

                        lp.width = textureViewWidth;
                        lp.height = textureViewHeight;

                        textureView.setLayoutParams(lp);

                        textureView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                textureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int x = 0,y =0;
                                if (textureView.getLeft() - (textureView.getScroller().getFinalX()) > 0){
                                    x = -(textureView.getLeft() - textureView.getScroller().getFinalX());
                                } else if (textureView.getRight() - (textureView.getScroller().getFinalX()) < originWidth){
                                    x = -(textureView.getRight() - originWidth - textureView.getScroller().getFinalX());
                                }

                                if (textureView.getTop() - (textureView.getScroller().getFinalY()) >0){
                                    y = -(textureView.getTop() - textureView.getScroller().getFinalY());
                                } else if (textureView.getBottom() - (textureView.getScroller().getFinalY()) < originHeight){
                                    y = -(textureView.getBottom() - originHeight - textureView.getScroller().getFinalY());
                                }

                                textureView.getScroller().startScroll(textureView.getScroller().getFinalX(),textureView.getScroller().getFinalY(),-x,-y);
                                textureView.invalidate();
                            }
                        });

                        PetkitLog.d(VideoPlayerView.class.getSimpleName(), "textureViewWidth->"+textureViewWidth+",textureViewHeight->"+textureViewHeight);

                        pointerWidthDistance = (int) Math.abs(event.getX(0) - event.getX(1));
                        pointerHeightDistance = (int) Math.abs(event.getY(0) - event.getY(1));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    PetkitLog.d(VideoPlayerView.class.getSimpleName(), "downX->" + downX+",downY->"+downY+",event.getX()->"+event.getX()+",event.getY()->"+event.getY());
                    if (downX!=-1 && downY!=-1 && Math.abs(event.getX()-downX) < UiUtils.dip2px(activity,10) &&  Math.abs(event.getY()-downY) < UiUtils.dip2px(activity,10)){
                        if (System.currentTimeMillis() - videoTouchDownStartTime < 500){
                            videoListener.onVideoClick();
                        }
                    } else {
                        videoListener.onVideoTouch(textureViewWidth == originWidth || textureViewHeight == originHeight);
                    }
                    if (longClickStart){
                        videoListener.onVideoLongClickEnd();
                        longClickStart = false;
                    }
                    mainHandler.removeCallbacksAndMessages(null);
                    pointerCount = 0;
                    doublePointer = false;
                    break;
            }
            return true;
        };
    }



    private void preparedPlay() {
        clearRootChildOtherView();
        if (videoListener != null) {
            videoListener.onPrepared();
        }
        //预加载完成状态
//        mPlayState = VideoConstant.PlayState.STATE_PREPARED;
        setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PREPARED);

        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "setOnPreparedListener");

        textureView.setOnTouchListener(ontl);

        if (videoListener != null) {
            videoListener.preparedVideo(
                    getVideoTimeStr(mVideoData.getProgress()),
                    (int) mVideoData.getProgress(),
                    (int) mPlayer.getDuration()
            );
        }


        //如果开启自动播放的话就直接播放,否则直接滑动到初始位置
        if (mIsAutoPlay && mVideoData != null) {
            startPlay(mVideoData.getProgress());
        }

    }

    public boolean isIsAutoPlay() {
        return mIsAutoPlay;
    }

    public void setIsAutoPlay(boolean mIsAutoPlay) {
        this.mIsAutoPlay = mIsAutoPlay;
    }

    private void startPlay(long position) {
        if (videoListener != null) {
            videoListener.onStartPlay();
        }

        if (isMute){
            mPlayer.setVolume(0,0);
        }

        if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PREPARED) {
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "startPlay:" + position);
            if (mPlayer != null) {
                if (position != 0L) {
                    soughtTo(position);
                }
                mPlayer.start();
//                mPlayState = VideoConstant.PlayState.STATE_PLAYING;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING);
                runVideoTime();
            }
        }
    }


    public void reStartPlay() {
        if (videoListener != null) {
            videoListener.onReStart();
        }

        resetPlay();
        if (mVideoData != null) {
            if (mVideoData.getProgress()>0){
                mVideoData.setProgress(0);
            }
            startVideo(mVideoData,isLive);
        }

    }

    public void startVideo(com.petkit.matetool.player.ijkplayer.VideoData videoData, boolean isLive) {
        this.isLive = isLive;
        //播放视频
        if (mSurface != null) {
            loadVideo(mSurface, videoData);
        }
    }


    public void startVideo(com.petkit.matetool.player.ijkplayer.VideoData videoData) {
        startVideo(videoData,false);
    }


    //视频播放准备
    public void loadVideo(Surface surface, com.petkit.matetool.player.ijkplayer.VideoData videoData) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "entryVideo:$video");
        mVideoData = videoData;
        if (mPlayer != null) {
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "mPlayState:" + mPlayState);
            //如果不是IDLE状态就改变播放器状态
            if (mPlayState != com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_IDLE) {
                resetPlay();
            }
            try {
                if (videoData.getPath() != null){
                    ((IjkMediaPlayer)(mPlayer)).setDataSource(videoData.getPath());
                } else {
//                    HashMap<String, String> headers = new HashMap<>();
//                    headers.put(Consts.HTTP_HEADER_SESSION, DataHelper.getStringSF(activity, Consts.SHARED_SESSION_ID));
//                    ((IjkMediaPlayer)(mPlayer)).setDataSource(videoData.getUrl(),headers);
                    ((IjkMediaPlayer)(mPlayer)).setDataSource(videoData.getUrl());
                }
                //加载url之后为播放器初始化完成状态
//                mPlayState = VideoConstant.PlayState.STATE_INITLIZED;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_INITLIZED);
                //设置渲染画板
                mPlayer.setSurface(surface);
                //初始化播放器监听
                initPlayerListener();
                //异步的方式装载流媒体文件
                mPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                errorPlay(0, 0, e.getMessage());
            }
        }

    }


    public void errorPlay(int what, int extra, String message) {
        if (videoListener != null) {
            videoListener.onError(message);
        }
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, String.format("setOnErrorListener:what:%s, message:%s", what, message));
//        mPlayState = VideoConstant.PlayState.STATE_ERROR;
        setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_ERROR);
        //播放错误时记录下时间点
        if (mVideoData != null) {
            if (getPosition() != 0L) {
                mVideoData.setProgress(getPosition());
            } else {
                if (mStartPosition != 0L) {
                    mVideoData.setProgress(mStartPosition);
                } else {
                    mVideoData.setProgress(mPosition);
                }
            }
        }
    }


    //初始化播放器监听
    public void initPlayerListener() {
        if (mPlayer == null) {
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "mPlayer is null");
            return;
        }
        //设置是否循环播放，默认可不写
        mPlayer.setLooping(false);

        if (mPlayer instanceof AndroidMediaPlayer) {
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "AndroidMediaPlayer");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build();
                ((AndroidMediaPlayer) (mPlayer)).getInternalMediaPlayer().setAudioAttributes(attributes);
            } else {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        if (isMute){
            mPlayer.setVolume(0,0);
        }

        //播放完成监听
        mPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                completedPlay(null);
            }
        });

        //seekTo()调用并实际查找完成之后
        mPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer mp) {
                // mPlayState = PlayState.STATE_IDLE
                Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "setOnSeekCompleteListener");
                seekCompleted();
            }
        });

        //预加载监听
        mPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                preparedPlay();
            }
        });

        //相当于缓存进度条
        mPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                buffering(percent);
            }
        });

        //播放错误监听
        mPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                errorPlay(what, extra, "播放错误，请重试~");
                return true;
            }
        });


        //播放信息监听
        mPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {

                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                        // 播放器开始渲染
//                        mPlayState = VideoConstant.PlayState.STATE_PLAYING;
                        setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING);
                        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
                        if (mNetWorkSpeedHandler != null) {
                            mNetWorkSpeedHandler.bindHandler(new com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler.OnNetWorkSpeedListener() {
                                @Override
                                public void netWorkSpeed(String speed) {
                                    PetkitLog.d("netWorkSpeed", "netWorkSpeed:" + speed);
                                    mSpeed = speed;
                                    if (videoListener != null) {
                                        videoListener.speed(mSpeed);
                                    }
                                }
                            });
                        }
                    }
                    break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        bufferStart();
                    }
                    break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        bufferEnd();
                    }
                    break;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE: {
                        //无法seekTo
                        notSeek();
                    }
                    break;
                }
                return true;
            }
        });

        //播放尺寸
        mPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                //这里是视频的原始尺寸大小
                Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "setOnVideoSizeChangedListener");
                layoutParams = com.petkit.matetool.player.ijkplayer.VideoUtils.changeVideoSize(getMeasuredWidth(),
                        getMeasuredHeight(),
                        mPlayer.getVideoWidth(),
                        mPlayer.getVideoHeight());


                originWidth = layoutParams.width;
                originHeight = layoutParams.height;
                textureViewWidth = layoutParams.width;
                textureViewHeight = layoutParams.height;
                originTextureViewWidth = textureViewWidth;
                originTextureViewHeight = textureViewHeight;

                if (isH3Home){
                    ((LayoutParams)layoutParams).gravity = Gravity.BOTTOM;
                    textureView.setLayoutParams(layoutParams);
                } else {
                    setLayoutParams(layoutParams);
                }
//                textureView.setLayoutParams(layoutParams);
//                textureView.setLayoutParams(VideoUtils.changeVideoSize(getMeasuredWidth(),
//                        getMeasuredHeight(),
//                        mPlayer.getVideoWidth(),
//                        mPlayer.getVideoHeight()));
            }
        });

        //设置IjkMediaPlayer Option
        if (mPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer player = (IjkMediaPlayer) mPlayer;
            player.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

            if (isLive){
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,hls");
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "buffer-size", 1024 * 1024 * 5);
            } else {
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,hls");
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024 * 25);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");

//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "skip_loop_filter", 48);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzeduration",1);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 2048);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"find_stream_info", 0);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "render-wait-start", 1);;
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek-at-start", mVideoData.getProgress());
                player.setSpeed(speed);
//                setCachePath();
            }
//            if (mVideoData != null && mVideoData.getFrameSpeed() != 0){
//                PetkitLog.e("playing","video url："+mVideoData.getUrl()+",frameSpeed："+mVideoData.getFrameSpeed());
//                player.setSpeed(mVideoData.getFrameSpeed());
//                player.setMaxPacketNum(2);
//            }else {
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,hls");
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
//                player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "buffer-size", 1024 * 1024 * 5);
//            }
            //设置是否保持屏幕常亮
            player.setScreenOnWhilePlaying(true);

        }

    }

    public void notSeek() {

    }


    public void switchFullOrWindowMode(int switchMode, boolean isRotateScreen) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "playMode$mPlayMode");
        switch (mPlayMode) {
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_NORMAL: {
                if (switchMode == com.petkit.matetool.player.ijkplayer.VideoConstant.SwitchMode.SWITCH_FULL_OR_NORMAL) {
                    //进入全屏模式（在dialog的模式下似乎会有适配问题）
                    mPlayMode = com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_FULL_SCREEN;
                    if (videoListener != null) {
                        videoListener.onFullScreen();
                    }
                    //没有开启旋转的情况下要强制转屏来达到全屏效果
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//                    if (getIsOpenRotate(activity)) {
//                        //开启旋转的情况下可以在转屏后恢复到默认状态， 屏幕旋转时指定默认的屏幕方向不然会转不过来...
//                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//                    }
                }
            }
            break;
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_FULL_SCREEN: {
                exitFullOrWindowMode(true, isRotateScreen);
            }
            break;
        }


    }

    public void exitFullOrWindowMode(boolean isBackNormal, boolean isRotateScreen) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "exitMode");
        if (getPlayMode() != com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_NORMAL && isBackNormal) {
            //屏幕方向改为竖屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            if (getIsOpenRotate(activity)) {
//                //开启旋转的情况下可以在转屏后恢复到默认状态，确保下次能够旋转屏幕
//                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            }
            if (videoListener != null) {
                videoListener.onNormalScreen();
            }
        }
    }

    public int getPlayMode() {
        return mPlayMode;
    }

    public int getPlayState() {
        return mPlayState;
    }


    public void continuePlay() {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "continuePlay"+",mPlayState:"+mPlayState);
        PetkitLog.d(TAG, "continuePlay"+",mPlayState:"+mPlayState);
        switch (mPlayState) {
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED:
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_COMPLETED: {
                mPlayer.start();
//                mPlayState = VideoConstant.PlayState.STATE_PLAYING;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING);
            }
            break;
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED: {
                mPlayer.start();
//                mPlayState = VideoConstant.PlayState.STATE_BUFFERING_PLAYING;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING);
            }
            break;
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_ERROR: {
                reStartPlay();
            }
            break;
        }
        if (videoListener != null) {
            videoListener.continueVideo();
        }
        runVideoTime();
    }

    public void pausePlay() {
        if (videoListener != null) {
            videoListener.onPausePlay();
        }
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "pausePlay");
        PetkitLog.d(TAG, "pausePlay");
        if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING || mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PREPARED) {
//            mPlayState = VideoConstant.PlayState.STATE_PAUSED;
            setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED);
        } else if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING) {
//            mPlayState = VideoConstant.PlayState.STATE_BUFFERING_PAUSED;
            setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED);
        } else {
            return;
        }
        mPlayer.pause();
        stopVideoTime();
    }

    //播放进度停止计时
    public void stopVideoTime() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void bufferEnd() {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "buffered");
        // 填充缓冲区后，MediaPlayer恢复播放/暂停
        if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING) {
            continuePlay();
        } else if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED) {
            pausePlay();
        }
        //缓冲结束解绑网速获取器
//        if (mNetWorkSpeedHandler != null) {
//            mNetWorkSpeedHandler.unbindHandler();
//        }
    }


    public void resetPlay() {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "resetPlay");
        if (videoListener != null) {
            videoListener.onReset();
        }
        mPlayer.reset();
//        mPlayState = VideoConstant.PlayState.STATE_IDLE;
        setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_IDLE);
        setOnTouchListener(ontl);
    }

    public void completedPlay(String videoUrl) {
        if (videoListener != null) {
            videoListener.onCompleted();
        }
        //播放完成状态
//        mPlayState = VideoConstant.PlayState.STATE_COMPLETED;
        setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_COMPLETED);
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "completedPlay");
    }

    //播放进度开始计时
    public void runVideoTime() {
        mHandler.removeCallbacksAndMessages(null);

        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "runVideoTime");
        PetkitLog.d(TAG, "runVideoTime");
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        //更新播放进度showCenterControlView
                        if (videoListener != null) {
                            videoListener.playing(
                                    getVideoTimeStr(mPlayer.getCurrentPosition()),
                                    mPlayer.getCurrentPosition()
                            );

                        }
                    }
                }

                //重复调起自身
                mHandler.postDelayed(mRunnable, 200);
            }
        };


        mHandler.post(mRunnable);
    }

    public void bufferStart() {
        //loading
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "bufferStart:state"+mPlayState);
        // MediaPlayer暂时不播放，以缓冲更多的数据
        switch (mPlayState) {
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED: {
//                mPlayState = VideoConstant.PlayState.STATE_BUFFERING_PAUSED;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED);
            }
            break;
            case com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING: {
//                mPlayState = VideoConstant.PlayState.STATE_BUFFERING_PLAYING;
                setPlayState(com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING);
            }
            break;
        }
        //缓冲开始时绑定实时网速获取器
        if (mNetWorkSpeedHandler != null) {
            mNetWorkSpeedHandler.bindHandler(new com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler.OnNetWorkSpeedListener() {
                @Override
                public void netWorkSpeed(String speed) {
                    PetkitLog.d("netWorkSpeed", "netWorkSpeed:" + speed);


                    mSpeed = speed;
                    if (videoListener != null) {
                        videoListener.speed(mSpeed);
                    }
                }
            });
        }


    }


    public int getBufferPercent() {
        return mBufferPercent;
    }

    public void buffering(int percent) {
        if (videoListener != null) {
            videoListener.onBuffering(percent);
        }
        if (percent != 0) {
            mBufferPercent = percent;
        }
    }

    public void soughtTo(long position) {
        PetkitLog.d(TAG, "seekTo,position:"+position);
        //loading
        mPlayer.seekTo(position);
        //缓冲开始时显示网速

        if (mNetWorkSpeedHandler != null) {
            mNetWorkSpeedHandler.bindHandler(new com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler.OnNetWorkSpeedListener() {
                @Override
                public void netWorkSpeed(String speed) {
                    PetkitLog.d("netWorkSpeed", "netWorkSpeed:" + speed);


                    mSpeed = speed;
                    if (videoListener != null) {
                        videoListener.speed(mSpeed);
                    }
                }
            });
        }
    }

    public void seekCompletePlay(long position, com.petkit.matetool.player.ijkplayer.H3SeekCompleteListener l) {
//        soughtTo(position);

        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "seekToPlay:" + position);
        PetkitLog.d(TAG, "start seek,position:" + position);
        if (mPlayer != null) {
            PetkitLog.d(TAG, "start seek,mPlayState:"+mPlayState);
            if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED || mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED || mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_COMPLETED) {
                soughtTo(position);
                continuePlay();//拖动时播放一秒再暂停
                pausePlay();
            } else if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING || mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING) {
                soughtTo(position);
                continuePlay();
            } else if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PREPARED) {
                startPlay(position);
            }
        }

        if (l!= null){
            l.onComplete();
        }

        mPosition = position;
    }

    public void seekCompletePlay(long position) {
       seekCompletePlay(position ,null);
    }


    private void initListener(Context context) {
        //设置Texture监听
        textureView.setSurfaceTextureListener(this);

    }

    public void setVideoListener(com.petkit.matetool.player.ijkplayer.VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    private void setPlayState(int mPlayState){
        this.mPlayState = mPlayState;
        PetkitLog.d(TAG,"setPlayState,mPlayState:"+mPlayState);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "onSurfaceTextureAvailable:$width - $height" + width + "-" + height);
        if (mSurface == null) {
            mSurface = new Surface(surfaceTexture);
        }
        if (videoListener != null) {
            videoListener.onInitSuccess();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "onSurfaceTextureSizeChanged:$width - $height" + width + "-" + height);

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "onSurfaceTextureDestroyed");
        releasePlay(false);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "onSurfaceTextureUpdated");

    }

    public int getLight(boolean isMax) {
        int nowBrightnessValue = 0;
        try {
            nowBrightnessValue =
                    Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    public int getVolume(boolean isMax) {
        if (mAudioManager == null) {
            return 0;
        }
        if (isMax) {
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } else {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }


    //初始化Media和volume
    public void initAudio() {
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);


        //初始音量值
        mDefaultVolume = getVolume(false);

        if (mDefaultVolume == 0) {
            isMute = true;
        } else {
            isMute = false;
        }

        //初始亮度值
        mStartLight = getLight(false);
    }

    public void requestAudioFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null){
                //8.0以上需要响应音频焦点的状态改变
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
            /*
            AUDIOFOCUS_GAIN  的使用场景：应用需要聚焦音频的时长会根据用户的使用时长改变，属于不确定期限。例如：多媒体播放或者播客等应用。
            AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK  的使用场景：应用只需短暂的音频聚焦，来播放一些提示类语音消息，或录制一段语音。例如：闹铃，导航等应用。
            AUDIOFOCUS_GAIN_TRANSIENT  的使用场景：应用只需短暂的音频聚焦，但包含了不同响应情况，例如：电话、QQ、微信等通话应用。
            AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE  的使用场景：同样您的应用只是需要短暂的音频聚焦。未知时长，但不允许被其它应用截取音频焦点。例如：录音软件。
            */
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int i) {

                            }
                        }).build();

                if (mAudioManager != null) {
                    mAudioManager.requestAudioFocus(audioFocusRequest);
                }
            }
        } else {
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }

        }

        audioHasFocusRequest = true;
    }

    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            mAudioManager.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {

                }
            });
        }

    }

    public boolean isMute() {
        return isMute;
    }


    public void seekCompleted() {
        Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "seekCompleted");
        PetkitLog.d(TAG, "seekCompleted");
        if (mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED || mPlayState == com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING) {
            //缓冲开始时绑定实时网速获取器

            if (mNetWorkSpeedHandler != null) {
                mNetWorkSpeedHandler.bindHandler(new com.petkit.matetool.player.ijkplayer.NetWorkSpeedHandler.OnNetWorkSpeedListener() {
                    @Override
                    public void netWorkSpeed(String speed) {
                        PetkitLog.d("netWorkSpeed", "netWorkSpeed:" + speed);
                        mSpeed = speed;
                        if (videoListener != null) {
                            videoListener.speed(mSpeed);
                        }
                    }
                });
            }
        }

        if (videoListener != null){
            videoListener.onSeekCompleted();
        }

        //缓冲结束解绑实时网速获取器
//        if (mNetWorkSpeedHandler != null) {
//            mNetWorkSpeedHandler.unbindHandler();
//        }
    }

    public void switchMuteVolume() {
        if (mPlayer != null) {
            if (isMute) {
                if (!audioHasFocusRequest){
                    requestAudioFocus();
                }
                int volume = getVolume(false);
                mPlayer.setVolume(volume, volume);
                mDefaultVolume = volume;
                isMute = false;
            } else {
                mPlayer.setVolume(0f, 0f);
                mDefaultVolume = 0;
                isMute = true;
            }
        }

    }

    public void turnUpVolume() {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ++mDefaultVolume, 0);
        }
    }

    public void turnDownVolume() {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, --mDefaultVolume, 0);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 开始录制
     *
     * @param path
     */
    public void startRecord(String path) {
        mPath = path;
        if (mPlayer != null) {
            isRecording = true;
            mPlayer.startRecord(mPath);
        }
    }


    /**
     * 停止录制
     *
     * @return
     */
    public String stopRecord() {
        if (mPlayer != null) {
            isRecording = false;
            mPlayer.stopRecord();
            return mPath;
        }
        return "";
    }


    public Bitmap getCurrentBitmap() {
        if (textureView != null && mPlayer != null) {
            Bitmap bitmap = Bitmap.createBitmap(mPlayer.getVideoWidth(), mPlayer.getVideoHeight(), Bitmap.Config.RGB_565);
            return textureView.getBitmap(bitmap);
        }
        return null;
    }

    public void releasePlay(boolean destroyUi) {
        if (mSurface != null) {
            mSurface.release();
        }
        mHandler.removeCallbacks(mRunnable);
        mPlayer.stop();
        mPlayer.release();//调用release()方法来释放资源，资源可能包括硬件加速组件的单态固件
        mSurface = null;
    }


    public void onConfigChanged(Configuration newConfig) {
        textureView.getScroller().startScroll(textureView.getScroller().getFinalX(),textureView.getScroller().getFinalY(),-textureView.getScroller().getFinalX(),-textureView.getScroller().getFinalY(),0);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            autoEntryPortraitScreen();
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "Configuration.ORIENTATION_PORTRAIT");
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            autoEntryFullScreen();
            Log.d(com.petkit.matetool.player.ijkplayer.VideoConstant.VIDEO_TAG, "Configuration.ORIENTATION_LANDSCAPE");
        }
    }

    /**
     * 根据onConfigChanged自动切换竖屏
     */
    public void autoEntryPortraitScreen() {
        //进入普通模式
        if (mDefaultParams != null) {
            mPlayMode = com.petkit.matetool.player.ijkplayer.VideoConstant.PlayMode.MODE_NORMAL;
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            setLayoutParams(mDefaultParams);
            int phoneWidth = com.petkit.matetool.player.ijkplayer.VideoUtils.getPhoneDisplayWidth(activity);
            int phoneHeight = com.petkit.matetool.player.ijkplayer.VideoUtils.getPhoneDisplayHeight(activity);
            //退出全屏后，需要获取播放器在竖屏下的高度
            int oldWidth = mDefaultParams.width == -1 ? Math.min(phoneWidth,phoneHeight) : mDefaultParams.width;
            int oldHeight = mDefaultParams.height == -1 ? Math.max(phoneWidth,phoneHeight) : mDefaultParams.height;
            if (textureView != null) {
                LayoutParams layoutParams = com.petkit.matetool.player.ijkplayer.VideoUtils.changeVideoSize(oldWidth, oldHeight,
                        mPlayer.getVideoWidth()== 0? defaultVideoWidth : mPlayer.getVideoWidth(),
                        mPlayer.getVideoHeight() == 0? defaultVideoHeight: mPlayer.getVideoHeight());
                originWidth = oldWidth;
                originHeight = oldHeight;
                textureViewWidth = layoutParams.width;
                textureViewHeight = layoutParams.height;
                originTextureViewWidth = textureViewWidth;
                originTextureViewHeight = textureViewHeight;
                if (isH3Home){
                    layoutParams.gravity = Gravity.BOTTOM;
                    textureView.setLayoutParams(layoutParams);
                } else {
                    textureView.setLayoutParams(layoutParams);
                }
            }
        }
    }


    /**
     * 根据onConfigChanged自动切换横屏
     */
    public void autoEntryFullScreen() {
        //该方案只适合父容器为linearLayout且根布局中没有滑动控件，其他父容器下适配捉急
        //直接设置根布局改为横屏，然后View宽高改为MATCH_PARENT来实现
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        //设置为充满父布局
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
//        setLayoutParams(params);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        //全屏直接使用手机大小,此时未翻转的话，高宽对调
        int phoneWidth = com.petkit.matetool.player.ijkplayer.VideoUtils.getPhoneDisplayWidth(activity);
        int phoneHeight = com.petkit.matetool.player.ijkplayer.VideoUtils.getPhoneDisplayHeight(activity);


        if (textureView != null) {
            LayoutParams layoutParams = com.petkit.matetool.player.ijkplayer.VideoUtils.changeVideoSize(Math.max(phoneHeight, phoneWidth), Math.min(phoneHeight, phoneWidth),
                    mPlayer.getVideoWidth() == 0? defaultVideoWidth : mPlayer.getVideoWidth(),
                    mPlayer.getVideoHeight() == 0? defaultVideoHeight: mPlayer.getVideoHeight());
            originWidth = layoutParams.width;
            originHeight = layoutParams.height;
            textureViewWidth = layoutParams.width;
            textureViewHeight = layoutParams.height;
            originTextureViewWidth = textureViewWidth;
            originTextureViewHeight = textureViewHeight;

            PetkitLog.d("setConfiguration autoEntryFullScreen: width: " + originWidth + ", height: " + originHeight);
            PetkitLog.d("setConfiguration autoEntryFullScreen: originTextureViewWidth: " + originTextureViewWidth + ", originTextureViewHeight: " + originTextureViewHeight);

            setLayoutParams(layoutParams);
            textureView.setLayoutParams(new LayoutParams(textureViewWidth, textureViewHeight));
//            textureView.setLayoutParams(layoutParams);
        }
    }

    @SuppressLint("WrongConstant")
    public void onBackPressed() {
        if (activity.getRequestedOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            exitFullOrWindowMode(true, false);
        }
    }

    public String getVideoTimeStr(long position) {
        return com.petkit.matetool.player.ijkplayer.VideoUtils.progress2Time(position) + "&" + com.petkit.matetool.player.ijkplayer.VideoUtils.progress2Time(getDuration());
    }

    public long getDuration() {
        return mPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public long getPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPlaySpeed(float speed){
        this.speed = speed;
        ((IjkMediaPlayer)mPlayer).setSpeed(speed);
    }

    public float getSpeed(){
        return ((IjkMediaPlayer)mPlayer).getSpeed(1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mNetWorkSpeedHandler != null) {
            mNetWorkSpeedHandler.unbindHandler();
        }
    }

    public void addPowerOffView(OnClickListener l){
        clearRootChildOtherView();
        LayoutInflater.from(activity).inflate(R.layout.d4sh_player_turn_off_cover_view,rootChildLayout);
        TextView turnOnCameraTextView = rootChildLayout.findViewById(R.id.turn_on_camera_text_view);
        turnOnCameraTextView.setOnClickListener(l);
    }

    public void clearRootChildOtherView(){
        int childCount = rootChildLayout.getChildCount();
        if (childCount > 1){
            for (int i = 0; i < childCount; i++) {
                if (i>0){
                    rootChildLayout.removeViewAt(i);
                }
            }
        }
    }

    public void setH3Home(boolean h3Home) {
        isH3Home = h3Home;
    }

    /*
    在player上盖一个开通云服务的View
     */
    public void addOpenCloudServiceView(OnClickListener l){
        clearRootChildOtherView();
        LayoutInflater.from(activity).inflate(R.layout.layout_h3_open_cloud_service,rootChildLayout);
        rootChildLayout.findViewById(R.id.open_immediately).setOnClickListener(l);
    }

    private void setCachePath(){
         ((IjkMediaPlayer)mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_file_path", getContext().getCacheDir()+"/"+mVideoData.getUrl()+"_cache.tmp");//缓存文件的路径
         ((IjkMediaPlayer)mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_map_path", getContext().getCacheDir()+"/"+mVideoData.getUrl()+"_cache.temp");//缓存文件的路径
         ((IjkMediaPlayer)mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "parse_cache_map", 1);
         ((IjkMediaPlayer)mPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "auto_save_map", 1);
    }

    public String getRecordFilePath(){
        return mPath;
    }

    public View addCoverView(Integer coverViewLayoutResId){
        clearRootChildOtherView();
        return LayoutInflater.from(activity).inflate(coverViewLayoutResId,rootChildLayout);
    }

    public com.petkit.matetool.player.ijkplayer.VideoData getCurrentPlayingVideoData(){
        return mVideoData;
    }


    public void setPlayerSpeed() {
        if (videoListener != null) {
            videoListener.onReStart();
        }

        resetPlay();
        if (mVideoData != null) {
            startVideo(mVideoData,isLive);
        }

    }

    public boolean isPlayerAvailableState(){
        return getPlayState() == STATE_PLAYING || getPlayState() == STATE_BUFFERING_PLAYING ||
                getPlayState() == STATE_PAUSED || getPlayState() == STATE_BUFFERING_PAUSED ||
                getPlayState() == STATE_COMPLETED;
    }

    public boolean isPlayerPlayingState(){
        return getPlayState() == STATE_PLAYING || getPlayState() == STATE_BUFFERING_PLAYING;
    }

    public boolean isPlayerPauseState(){
        return getPlayState() == STATE_PAUSED ||  getPlayState() == STATE_BUFFERING_PAUSED;
    }

    public boolean isPlayerCompleteState(){
        return getPlayState() == STATE_COMPLETED;
    }


    public boolean isLive() {
        return isLive;
    }

    static class MainHandler extends Handler {
        private final WeakReference<Context> mContext;

        public MainHandler(Context context) {
            super(Looper.getMainLooper());
            mContext = new WeakReference<Context>(context);
        }

    }


}
