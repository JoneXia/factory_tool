package com.petkit.matetool.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class BasePetkitPlayerLandscapeView extends RelativeLayout {

    protected BasePetkitPlayerLandscapeViewClickListener viewClickListener;

    public BasePetkitPlayerLandscapeView(Context context) {
        this(context, null);
    }

    public BasePetkitPlayerLandscapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePetkitPlayerLandscapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context);
    }

    private void initAll(Context context){
        initView(context);
        initClickEvent();
    }

    public void setViewClickListener(BasePetkitPlayerLandscapeViewClickListener viewClickListener) {
        this.viewClickListener = viewClickListener;
    }

    protected abstract void initView(Context context);

    protected abstract void initClickEvent();

    public abstract void hideOneself();

    public abstract void showOneself(boolean liveStatus, boolean tripodHeadMode);

    public abstract void setQuality(String text);

    public abstract void setTimeSpeed(String text);

    public abstract void setVolumeImageResource(int imageResource);

    public abstract void setTripodHeadStatus(Boolean showTripodHeadImage, Integer tripodHeadImageResource, Boolean showCcvl);

    public abstract void setRecordBtnImageResource(int imageResource);

    public abstract void setIntercomBtnImageResource(int imageResource);

}
