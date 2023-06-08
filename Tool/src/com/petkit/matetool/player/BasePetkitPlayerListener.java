package com.petkit.matetool.player;

public interface BasePetkitPlayerListener {

    void onFastBackwardResult(boolean switchVideo);

    void onFastForwardResult(boolean switchVideo);

    void onStartPlay();

    void onCompleted();

    void playing(String videoTime, long position);

    void onVideoClick();

    void onVideoTouch(boolean isZoon);

    void onSeekCompleted();

    void preparedVideo(String videoTime, int start, int max);

    void onInitSuccess();

    void onPrepared();

    void onPlayerRestart();
}
