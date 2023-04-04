package com.petkit.matetool.ui.D4SH;

import android.content.Context;
import android.util.AttributeSet;

import com.petkit.matetool.player.BasePetkitPlayer;

import androidx.annotation.Nullable;

public class D4shPlayer extends BasePetkitPlayer {

    public D4shPlayer(Context context) {
        super(context);
    }

    public D4shPlayer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public D4shPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
