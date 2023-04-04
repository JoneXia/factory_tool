package com.petkit.matetool.player;

public interface BasePetkitPlayerLandscapeViewClickListener {

    void onExitFullScreenBtnClick();

    void onLandscapeQualityBtnClick();

    void onLandscapeTimeSpeedBtnClick();

    void onLandscapeVolumeBtnClick();

    void onLandscapeTripodHeadBtnClick();

    void onLandscapeCcvlBtnClick(boolean push, int orientation);

    void onLandscapeScreenShotBtnClick();

    void onLandscapeRecordBtnClick();

    void onLandscapeIntercomBtnTouchDown();

    void onLandscapeIntercomBtnTouchUp();

}
