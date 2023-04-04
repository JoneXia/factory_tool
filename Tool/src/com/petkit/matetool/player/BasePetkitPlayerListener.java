package com.petkit.matetool.player;

public interface BasePetkitPlayerListener {

    void onFastBackwardResult(boolean switchVideo);

    void onFastForwardResult(boolean switchVideo);

    void onStartPlay();

    void onCompleted();

    void onInitSuccess();

    void playing(String videoTime, long position);

    void onVideoClick();

    void onSeekCompleted();

    void preparedVideo(String videoTime, int start, int max);

    void onPrepared();
}
