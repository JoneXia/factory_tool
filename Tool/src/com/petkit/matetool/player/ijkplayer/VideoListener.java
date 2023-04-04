package com.petkit.matetool.player.ijkplayer;

public interface VideoListener {
    /**
     * 播放器初始化完成
     */
    void onInitSuccess();

    /**
     * 预加载完成
     */
    void onPrepared();

    /**
     * 播放器重启
     */
    void onReset();

    /**
     * 缓冲中
     */
    void onBuffering(int percent);

    /**
     * 播放开始
     */
    void onStartPlay();

    /**
     * 重播
     */
    void onReStart();


    /**
     * 播放中
     */
    void onPlaying();

    /**
     * 播放暂停
     */
    void onPausePlay();

    /**
     * 播放完成
     */
    void onCompleted();

    /**
     * 播放失败
     */
    void onError(String error);

    /**
     * 全屏状态
     */
    void onFullScreen();

    /**
     * 普通状态
     */
    void onNormalScreen();


    /**
     * 播放准备就绪
     *
     * @param videoTime 播放时间
     * @param start     可选任意位置，默认为初始位置 初始进度
     * @param max       最大进度
     */
    void preparedVideo(String videoTime, int start, int max);

    void continueVideo();

    void playing(String videoTime, long position);

    void speed(String speed);

    void onVideoClick();

    void onSeekCompleted();
}
