package com.petkit.matetool.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.petkit.matetool.R;


public abstract class BasePetkitPlayerPortraitView extends RelativeLayout {

    protected BasePetkitPlayerPortraitViewClickListener viewClickListener;

    public BasePetkitPlayerPortraitView(Context context) {
        this(context, null);
    }

    public BasePetkitPlayerPortraitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePetkitPlayerPortraitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context);
    }

    private void initAll(Context context){
        initView(context);
        initClickEvent();
    }

    public void setViewClickListener(BasePetkitPlayerPortraitViewClickListener viewClickListener) {
        this.viewClickListener = viewClickListener;
    }

    protected void setTransparentBackground(){
        setBackground(getContext().getResources().getDrawable(R.color.transparent));
    }

    protected abstract void initView(Context context);

    protected abstract void initClickEvent();

    public abstract void hideOneself();

    public abstract void showOneself(Boolean liveStatus);

    public abstract void setQuality(String text);

    public abstract void setTimeSpeed(String text);

    public abstract void setMute(boolean isMute);

    public abstract void setPlayerSwitchImageViewResource(int playerSwitchImageViewResource);

    public abstract void setVolumeImageVisible(Boolean showVolumeImage);

    public abstract void setPrivacyModePlayImageVisible(Boolean showPrivacyModePlayImageView);

}
