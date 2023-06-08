package com.petkit.matetool.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.petkit.matetool.R;
import com.petkit.matetool.player.BasePetkitPlayerPortraitView;


public class PetkitPlayerPortraitView extends BasePetkitPlayerPortraitView implements View.OnClickListener{

    private ImageView ivVolume;
    private ImageView ivBowl;
    private ImageView privacyModePlayImageView;

    public PetkitPlayerPortraitView(Context context) {
        super(context);
    }

    public PetkitPlayerPortraitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PetkitPlayerPortraitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.petkit_player_portrait_view, this);
        ivVolume = findViewById(R.id.iv_volume);
        ivBowl = findViewById(R.id.bowl);
        privacyModePlayImageView = findViewById(R.id.privacy_mode_play_image_view);

        setTransparentBackground();
    }

    public void setBowlImage(int resId) {
        ivBowl.setImageResource(resId);
    }

    @Override
    protected void initClickEvent() {
//        ivVolume.setOnClickListener(this);
//        privacyModePlayImageView.setOnClickListener(this);
//        setOnClickListener(this);
    }

    @Override
    public void hideOneself() {
        setVisibility(GONE);
    }

    @Override
    public void showOneself(Boolean liveStatus) {
        setVisibility(VISIBLE);
    }

    @Override
    public void setQuality(String text) {

    }

    @Override
    public void setTimeSpeed(String text) {

    }

    @Override
    public void setMute(boolean isMute) {
        ivVolume.setImageResource(isMute ? R.drawable.petkit_player_portrait_black_background_mute_icon : R.drawable.petkit_player_portrait_black_background_volume_icon);
    }

    @Override
    public void setPlayerSwitchImageViewResource(int playerSwitchImageViewResource) {

    }

    @Override
    public void setVolumeImageVisible(Boolean showVolumeImage) {
        if (showVolumeImage != null){
            ivVolume.setVisibility(showVolumeImage ?VISIBLE:GONE);
        }
    }

    @Override
    public void setPrivacyModePlayImageVisible(Boolean showPrivacyModePlayImageView) {
        if (showPrivacyModePlayImageView != null){
            privacyModePlayImageView.setVisibility(showPrivacyModePlayImageView? VISIBLE:GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_volume:
                if (viewClickListener != null){
                    viewClickListener.onVolumeBtnClick();
                }
                break;
            case R.id.privacy_mode_play_image_view:
                if (viewClickListener != null){
                    viewClickListener.onPrivacyModePlayBtnClick();
                }
                break;
            default:
                if (viewClickListener != null){
                    viewClickListener.onPlayerPortraitViewClick();
                }
                break;
        }
    }
}
