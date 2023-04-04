package com.petkit.matetool.ui.D4SH;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.petkit.matetool.R;
import com.petkit.matetool.player.BasePetkitPlayerListener;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.UiUtils;

public class D4SHVideoPlayActivity extends BaseActivity implements BasePetkitPlayerListener {

    private D4shPlayer player;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_d4sh_video_play);

    }

    @Override
    protected void setupViews() {
        initPlayer();
    }

    @Override
    public void onClick(View v) {

    }



    private void initPlayer() {
        player = findViewById(R.id.d4sh_player);
        player.setPlayerListener(this);
        player.post(() -> {
            int videoPlayerHeight = Math.round((player.getWidth() - UiUtils.dip2px(this, 32))* 9f / 16);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) player.getLayoutParams();
            layoutParams.height = videoPlayerHeight;
            player.setLayoutParams(layoutParams);
        });

    }


    @Override
    public void onFastBackwardResult(boolean switchVideo) {

    }

    @Override
    public void onFastForwardResult(boolean switchVideo) {

    }

    @Override
    public void onStartPlay() {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onInitSuccess() {
        player.startVideo("http://192.168.33.105");
    }

    @Override
    public void playing(String videoTime, long position) {

    }

    @Override
    public void onVideoClick() {

    }

    @Override
    public void onSeekCompleted() {

    }

    @Override
    public void preparedVideo(String videoTime, int start, int max) {

    }

    @Override
    public void onPrepared() {

    }
}
