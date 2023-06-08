package com.petkit.matetool.player;

public interface BasePetkitPlayerLandscapeViewClickListener {

    void onExitFullScreenBtnClick();

    void onLandscapeQualityBtnClick(int qualityType);

    void onLandscapeTimeSpeedBtnClick(int timesSpeed);

    void onLandscapeVolumeBtnClick();

    void onLandscapeTripodHeadBtnClick();

    void onLandscapeCcvlBtnClick(boolean push, int orientation);

    void onLandscapePlayBtnClick();

    void onLandscapeScreenShotBtnClick();

    void onLandscapeRecordBtnClick();

    void onLandscapeIntercomBtnTouchDown();

    void onLandscapeIntercomBtnTouchUp();

    void onLandscapeExtraMealBtnClick();

    void onLandscapeEatVideoBtnClick();

    void onLandscapeDatePickerBtnClick();

    void onVideoRecordClick(Integer videoType, String shortVideoUrl, String fullVideoUrl, Float timesSpeed, Long expire);

    void onBackLiveBtnClick();

    void onDeleteEatVideoBtnClick(String eventId, int endTime, int startTime, int position);

    void onDownloadFullVideo(Long startTime, Long mark);

    void onSeekbarProgressChanged(int progress);

    void onPreviewVideoDownload();

    void onFullVideoDownload();

    void onEventDelete();

}
