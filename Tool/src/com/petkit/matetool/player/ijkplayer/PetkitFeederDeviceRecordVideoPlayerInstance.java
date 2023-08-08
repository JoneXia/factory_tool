package com.petkit.matetool.player.ijkplayer;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Description:
 * author:jiawei.hao
 * Date:11/2/22
 */
public class PetkitFeederDeviceRecordVideoPlayerInstance {

    private com.petkit.matetool.player.ijkplayer.PetkitFeederDeviceRecordVideoPlayer simplePlayer;
    private RelativeLayout.LayoutParams playerLp;

    private static class PetkitFeederDeviceRecordVideoPlayerInstanceHolder{
        private static PetkitFeederDeviceRecordVideoPlayerInstance instance = new PetkitFeederDeviceRecordVideoPlayerInstance();
    }

    public static PetkitFeederDeviceRecordVideoPlayerInstance getInstance(){
        return PetkitFeederDeviceRecordVideoPlayerInstanceHolder.instance;
    }

    public PetkitFeederDeviceRecordVideoPlayerInstance() {
    }

    //初始化事件列表预览公共播放器x
    public void initPlayer(Activity activity){
        simplePlayer = new com.petkit.matetool.player.ijkplayer.PetkitFeederDeviceRecordVideoPlayer(activity);
        playerLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        playerLp.addRule(RelativeLayout.CENTER_IN_PARENT);
//        playerLp.setMargins(UiUtils.dip2px(activity,2),UiUtils.dip2px(activity,2),UiUtils.dip2px(activity,2),UiUtils.dip2px(activity,2));
    }

    public com.petkit.matetool.player.ijkplayer.PetkitFeederDeviceRecordVideoPlayer getSimplePlayer() {
        return simplePlayer;
    }

    public RelativeLayout.LayoutParams getPlayerLp() {
        return playerLp;
    }

    public void releasePlayer(){
        simplePlayer.releasePlayer();
    }
}
