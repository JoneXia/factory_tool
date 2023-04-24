package com.petkit.matetool.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.petkit.matetool.player.BasePetkitPlayer;

import androidx.annotation.Nullable;

public class PetkitPlayer extends BasePetkitPlayer {

    public PetkitPlayer(Context context) {
        super(context);
    }

    public PetkitPlayer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PetkitPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initCorners() {
        setupCorners();
    }

    @Override
    protected void initOtherView(Context context) {

    }

    @Override
    public void preparedVideo(String videoTime, int start, int max) {
        super.preparedVideo(videoTime, start, max);
        if (playerListener != null){
            playerListener.preparedVideo(videoTime, start, max);
        }
    }
}
