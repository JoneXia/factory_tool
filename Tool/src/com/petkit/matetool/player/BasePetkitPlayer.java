package com.petkit.matetool.player;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.player.ijkplayer.VideoData;
import com.petkit.matetool.player.ijkplayer.VideoListener;
import com.petkit.matetool.player.ijkplayer.VideoPlayerView;
import com.petkit.matetool.player.ijkplayer.VideoUtils;
import com.petkit.matetool.utils.DataHelper;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.UiUtils;
import com.petkit.matetool.widget.PetkitLogoLoadingView;
import com.petkit.matetool.widget.TextureVideoViewOutlineProvider;

import androidx.annotation.Nullable;

import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PAUSED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_BUFFERING_PLAYING;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_COMPLETED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PAUSED;
import static com.petkit.matetool.player.ijkplayer.VideoConstant.PlayState.STATE_PLAYING;


public abstract class BasePetkitPlayer extends RelativeLayout implements VideoListener {

    protected final String TAG = this.getClass().getSimpleName();

    protected VideoPlayerView playerView;
    private BasePetkitPlayerPortraitView portraitView;
    private BasePetkitPlayerLandscapeView landscapeView;
    private PetkitLogoLoadingView pllv;
    protected Context mContext;
    protected BasePetkitPlayerListener playerListener;
    private LinearLayout.LayoutParams portraitLayoutParams;
    private View coverView;
    private boolean loading;

    public BasePetkitPlayer(Context context) {
        this(context, null);
    }

    public BasePetkitPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePetkitPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context);
    }

    private void initAll(Context context){
        this.mContext = context;

        initPlayerView(context);
        initCorners();
        initLoadingView(context);
        initOtherView(context);
    }

    private void initPlayerView(Context context){
        LayoutInflater.from(context).inflate(R.layout.base_petkit_player,this);
        playerView = findViewById(R.id.video_player);
        int playSpeed = DataHelper.getIntergerSF(context, Globals.H3_VIDEO_PLAY_SPEED,1);
        playerView.setSpeed(playSpeed == 3 ? 2 : 1);
        playerView.setVideoListener(this);
    }

    public void setPlayerListener(BasePetkitPlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    public void setDefaultVideoWidthHeight(int defaultVideoWidth, int defaultVideoHeight){
        playerView.setDefaultVideoWidthHeight(defaultVideoWidth, defaultVideoHeight);
    }

    /*
                初始化圆角效果
                 */
    protected abstract void initCorners();

    /*
    添加横屏、竖屏共有并且不用调整布局参数的View，如H3中的播放按钮，一直在播放器的中间。PS：也有一些View会改变布局参数
     */
    protected abstract void initOtherView(Context context);

    private void initLoadingView(Context context){
        pllv = LayoutInflater.from(context).inflate(R.layout.base_petkit_player_loading_view,this).findViewById(R.id.pllv);
    }

    public void setupCorners(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new TextureVideoViewOutlineProvider(UiUtils.dip2px(getContext(),16)));
            setClipToOutline(true);
        }
    }

    public void cancelCorners(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(null);
            setClipToOutline(false);
        }
    }

    public void startVideo(String url, boolean appendBaseUrl){
        if (appendBaseUrl){
            url = VideoUtils.getVideoUrl(url);
        }
        startVideo(new VideoData("", url, 0, 0));
    }

    public void startVideo(String url, boolean appendBaseUrl, float timesSpeed){
        if (appendBaseUrl){
            url = VideoUtils.getVideoUrl(url);
        }
        playerView.setPlaySpeed(timesSpeed);
        startVideo(new VideoData("", url, 0, 0));
    }

    public void startVideo(String url){
        startVideo(url, false);
    }

    public void startVideo(VideoData videoData){
        boolean isLive = false;
        boolean shortVideo = true;
        if (videoData.getPath() != null){

        } else {
            isLive = true;//TODO: !videoData.getUrl().contains(ApiTools.getApiHTTPUri());
            shortVideo = videoData.getUrl().contains("EVENT_VIDEO");
        }
        if (shortVideo){
            playerView.setPlaySpeed(1);
        }
        playerView.startVideo(videoData, isLive);
    }

    public boolean isRecording(){
        return playerView.isRecording();
    }

    public void setConfiguration(Configuration newConfig){
        if (playerView != null) {
            playerView.onConfigChanged(newConfig);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setPortraitLayoutParams((LinearLayout.LayoutParams) getLayoutParams());

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.setMargins(0,0,0,0);
            setLayoutParams(layoutParams);

            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (landscapeView != null) {
                        ViewGroup.LayoutParams layoutParams1 = landscapeView.getLayoutParams();
                        layoutParams1.width = getMeasuredWidth();
                        layoutParams1.height = getMeasuredHeight();
                        landscapeView.setLayoutParams(layoutParams1);

                        PetkitLog.d("setConfiguration onGlobalLayout landscapeView: width: " + layoutParams1.width + ", height: " + layoutParams1.height);
                    }

                    if (coverView != null){
                        ViewGroup.LayoutParams layoutParams2 = coverView.getLayoutParams();
                        layoutParams2.width = getMeasuredWidth();
                        layoutParams2.height = getMeasuredHeight();
                        coverView.setLayoutParams(layoutParams2);

                        PetkitLog.d("setConfiguration onGlobalLayout coverView: width: " + layoutParams2.width + ", height: " + layoutParams2.height);
                    }

                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else if (portraitLayoutParams != null){
            setLayoutParams(portraitLayoutParams);
        }
    }

    public void playerStatusSwitch(){
        if (isPlayerPlayingState()){
            playerView.pausePlay();
        }else if (isPlayerPauseState() || isPlayerCompleteState()){
            playerView.continuePlay();
        }
    }

    /*
    播放器上层的View从Activity传入
     */
    public void addPortraitView(BasePetkitPlayerPortraitView view){
        this.portraitView = view;
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void addLandscapeView(BasePetkitPlayerLandscapeView landscapeView) {
        this.landscapeView = landscapeView;
        addView(landscapeView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void switchFullOrWindowMode(int switchMode, boolean isRotateScreen) {
        playerView.switchFullOrWindowMode(switchMode,isRotateScreen);
    }

    public void setQuality(String text){
        if (portraitView!=null){
            portraitView.setQuality(text);
        }
        if (landscapeView!=null){
            landscapeView.setQuality(text);
        }
    }

    public void setTimeSpeed(String text){
        if (portraitView!=null){
            portraitView.setTimeSpeed(text);
        }
        if (landscapeView!=null){
            landscapeView.setTimeSpeed(text);
        }
    }

    public boolean isPlayerAvailableState(){
        return playerView.getPlayState() == STATE_PLAYING || playerView.getPlayState() == STATE_BUFFERING_PLAYING ||
                playerView.getPlayState() == STATE_PAUSED || playerView.getPlayState() == STATE_BUFFERING_PAUSED ||
                playerView.getPlayState() == STATE_COMPLETED;
    }

    public boolean isPlayerPlayingState(){
        return playerView.getPlayState() == STATE_PLAYING || playerView.getPlayState() == STATE_BUFFERING_PLAYING;
    }

    public boolean isPlayerPauseState(){
        return playerView.getPlayState() == STATE_PAUSED ||  playerView.getPlayState() == STATE_BUFFERING_PAUSED;
    }

    public boolean isPlayerCompleteState(){
        return playerView.getPlayState() == STATE_COMPLETED;
    }

    public VideoPlayerView getPlayerView() {
        return playerView;
    }

    public void startRecord(String path) {
        playerView.startRecord(path);
    }

    public String stopRecord() {
        return playerView.stopRecord();
    }

    public void showLoadingView(){
        playerView.pausePlay();
        pllv.setVisibility(View.VISIBLE);
        pllv.startLoadingAnimation();
        loading = true;
    }

    public void hideLoadingView(){
        pllv.setVisibility(View.GONE);
        pllv.cancelLoadingAnimation();
        loading = false;
    }

    public boolean isLoading() {
        return loading;
    }

    public void pausePlay(){
        playerView.pausePlay();
    }

    public void continuePlay(){
        playerView.continuePlay();
    }

    public void seekCompletePlay(long position){
        playerView.seekCompletePlay(position);
    }

    public boolean isMute(){
        return playerView.isMute();
    }

    public int getVolume(boolean isMax) {
        return playerView.getVolume(isMax);
    }

    public void switchMuteVolume(){
        playerView.switchMuteVolume();
    }

    public void addPowerOffView(OnClickListener l){
        playerView.addPowerOffView(v->{
            playerView.clearRootChildOtherView();
            l.onClick(v);
        });
    }

    public String getRecordFilePath(){
        return playerView.getRecordFilePath();
    }

    public Bitmap getCurrentBitmap() {
        return playerView.getCurrentBitmap();
    }

    private void setPortraitLayoutParams(LinearLayout.LayoutParams playerPortraitLayoutParams){
        portraitLayoutParams = new LinearLayout.LayoutParams(playerPortraitLayoutParams.width, playerPortraitLayoutParams.height);
        ((LinearLayout.LayoutParams)portraitLayoutParams).setMargins(((LinearLayout.LayoutParams)playerPortraitLayoutParams).leftMargin,
                ((LinearLayout.LayoutParams)playerPortraitLayoutParams).topMargin,
                ((LinearLayout.LayoutParams)playerPortraitLayoutParams).rightMargin,
                ((LinearLayout.LayoutParams)playerPortraitLayoutParams).bottomMargin);
    }

//    public View addCoverView(Integer coverViewLayoutResId){
//        if (coverViewLayoutResId != null){
//            return playerView.addCoverView(coverViewLayoutResId);
//        }
//        return null;
//    }

    public void clearCoverView(){
        int childCount = getChildCount();
        if (childCount > 4){
            removeView(getChildAt(4));
            coverView = null;
        }
    }

    public void removePlayerBlackBackground(){
        playerView.clearRootChildOtherView();
    }

    public void addPlayerBlackBackground(){
        playerView.addCoverView(R.layout.petkit_player_black_background_cover_view);
    }

//    public void clearCoverViewByCount(int childViewCount){
//        playerView.clearRootChildOtherView();
//        int childCount = getChildCount();
//        if (childCount > childViewCount){
//            removeView(getChildAt(childViewCount));
//            coverView = null;
//        }
//    }

    public void clearCoverView(int index){
        int childCount = getChildCount();
        if (childCount > index){
            removeView(getChildAt(index));
        }
    }

    public View addCoverView(Integer coverViewLayoutResId){
        if (coverViewLayoutResId != null){
            clearCoverView();
            addPlayerBlackBackground();
            if ((getContext()).getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                coverView = LayoutInflater.from(getContext()).inflate(coverViewLayoutResId, this, false);
                //横屏后match_parent不生效，先这样处理
                addView(coverView, new LayoutParams(getMeasuredWidth(), getMeasuredHeight()));
                return this;
            } else {
                LayoutInflater.from(getContext()).inflate(coverViewLayoutResId,this);
                coverView = getChildAt(getChildCount()-1);
                return this;
            }
        }
        return null;
    }

    public View addCoverWithBackgroundView(Integer coverViewLayoutRes, Integer backgroundLayoutRes){
        if (coverViewLayoutRes != null){
            clearCoverView();
            playerView.addCoverView(backgroundLayoutRes);
            if ((getContext()).getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                coverView = LayoutInflater.from(getContext()).inflate(coverViewLayoutRes, this, false);
                //横屏后match_parent不生效，先这样处理
                addView(coverView, new LayoutParams(getMeasuredWidth(), getMeasuredHeight()));
                return this;
            } else {
                LayoutInflater.from(getContext()).inflate(coverViewLayoutRes,this);
                coverView = getChildAt(getChildCount()-1);
                return this;
            }
        }
        return null;
    }

    public View addCoverView(Integer coverViewLayoutResId, int index){
        if (coverViewLayoutResId != null){
            clearCoverView();
            View coverView = LayoutInflater.from(getContext()).inflate(coverViewLayoutResId, this, false);
            addView(coverView, index);
            return this;
        }
        return null;
    }

    /**
     * 设置云存视频播放倍速
     * @param timesSpeed 倍速
     */
    public void setTimesSpeed(float timesSpeed){
        float speed = timesSpeed > 2? 2: timesSpeed;
        playerView.setPlaySpeed(speed);
        if (isPlayerPlayingState()){
            playerView.pausePlay();
            long currentPosition = playerView.getCurrentPosition();
            VideoData currentPlayingVideoData = playerView.getCurrentPlayingVideoData();
            if (currentPlayingVideoData != null){
                //CLOUD_STORAGE : 1倍速  CLOUD_DOUBLE：2倍速
                if (timesSpeed>2){
                    if (currentPlayingVideoData.getUrl().contains("CLOUD_STORAGE")){
                        currentPlayingVideoData.setUrl(currentPlayingVideoData.getUrl().replace("CLOUD_STORAGE","CLOUD_DOUBLE"));
                    }
                } else {
                    if (currentPlayingVideoData.getUrl().contains("CLOUD_DOUBLE")){
                        currentPlayingVideoData.setUrl(currentPlayingVideoData.getUrl().replace("CLOUD_DOUBLE","CLOUD_STORAGE"));
                    }
                }
                currentPlayingVideoData.setProgress(currentPosition);
                startVideo(currentPlayingVideoData);
            }
        }
    }

    public void reStartPlay(){
        playerView.reStartPlay();
    }


    @Override
    public void onInitSuccess() {
        if (playerListener != null){
            playerListener.onInitSuccess();
        }
    }

    @Override
    public void onPrepared() {
        hideLoadingView();
        if (playerListener != null){
            playerListener.onPrepared();
        }
    }

    @Override
    public void onReset() {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onStartPlay() {
        if (playerListener != null){
            playerListener.onStartPlay();
        }
    }

    @Override
    public void onReStart() {
        if (playerListener != null){
            playerListener.onPlayerRestart();
        }
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPausePlay() {

    }

    @Override
    public void onCompleted() {
        if (playerListener != null){
            playerListener.onCompleted();
        }
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onFullScreen() {

    }

    @Override
    public void onNormalScreen() {

    }

    @Override
    public void preparedVideo(String videoTime, int start, int max) {
        if (playerListener != null){
            playerListener.preparedVideo(videoTime, start, max);
        }
    }

    @Override
    public void continueVideo() {

    }

    @Override
    public void playing(String videoTime, long position) {
        PetkitLog.d(getClass().getSimpleName(), "videoTime:"+videoTime+", position:"+position);
        if (playerListener != null){
            playerListener.playing(videoTime, position);
        }
    }

    @Override
    public void speed(String speed) {

    }

    @Override
    public void onVideoClick() {
        if (playerListener != null){
            playerListener.onVideoClick();
        }
    }

    @Override
    public void onVideoTouch(boolean isZoon) {
        if (playerListener != null){
            playerListener.onVideoTouch(isZoon);
        }
    }

    @Override
    public void onSeekCompleted() {

    }
}
