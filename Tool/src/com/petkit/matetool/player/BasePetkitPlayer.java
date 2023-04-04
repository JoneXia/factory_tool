package com.petkit.matetool.player;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.petkit.android.utils.Consts;
import com.petkit.matetool.R;
import com.petkit.matetool.player.ijkplayer.VideoData;
import com.petkit.matetool.player.ijkplayer.VideoListener;
import com.petkit.matetool.player.ijkplayer.VideoPlayerView;
import com.petkit.matetool.ui.base.BaseApplication;
import com.petkit.matetool.utils.DataHelper;
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
        int playSpeed = DataHelper.getIntergerSF(context, Consts.H3_VIDEO_PLAY_SPEED,1);
        playerView.setSpeed(playSpeed == 3 ? 2 : 1);
        playerView.setVideoListener(this);
    }

    public void setPlayerListener(BasePetkitPlayerListener playerListener) {
        this.playerListener = playerListener;
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

    public void startVideo(String url){
        playerView.startVideo(new VideoData("", url, 0, 0),true);
    }

    public boolean isRecording(){
        return playerView.isRecording();
    }

    public void onConfigurationChanged(Configuration newConfig){
        if (playerView != null) {
            playerView.onConfigChanged(newConfig);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            setLayoutParams(layoutParams);
        } else {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.height =  Math.round(BaseApplication.getDisplayMetrics((Activity) mContext).widthPixels *9f/16);
            setLayoutParams(layoutParams);
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

    public void setPlaySpeed(float speed) {
        playerView.setPlaySpeed(speed);
    }

    public void showLoadingView(){
        playerView.pausePlay();
        pllv.setVisibility(View.VISIBLE);
        pllv.startLoadingAnimation();
    }

    public void hideLoadingView(){
        pllv.setVisibility(View.GONE);
        pllv.cancelLoadingAnimation();
    }

    public void pausePlay(){
        playerView.pausePlay();
    }

    public void addPowerOffView(OnClickListener l){
        playerView.addPowerOffView(v->{
            playerView.clearRootChildOtherView();
            l.onClick(v);
        });
    }

    @Override
    public void onInitSuccess() {
        if (playerListener != null) {
            playerListener.onInitSuccess();
        }
    }

    @Override
    public void onPrepared() {
        hideLoadingView();
    }

    @Override
    public void onReset() {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onStartPlay() {
    }

    @Override
    public void onReStart() {

    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPausePlay() {

    }

    @Override
    public void onCompleted() {

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

    }

    @Override
    public void continueVideo() {

    }

    @Override
    public void playing(String videoTime, long position) {

    }

    @Override
    public void speed(String speed) {

    }

    @Override
    public void onVideoClick() {

    }

    @Override
    public void onSeekCompleted() {

    }
}
