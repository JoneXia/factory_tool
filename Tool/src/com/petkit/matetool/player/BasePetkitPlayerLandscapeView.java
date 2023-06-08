package com.petkit.matetool.player;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.petkit.matetool.R;


public abstract class BasePetkitPlayerLandscapeView extends RelativeLayout {

    protected BasePetkitPlayerLandscapeViewClickListener viewClickListener;

    protected BasePetkitPlayerLandscapeSelectorWindow timesSpeedSelectorWindow;
    protected BasePetkitPlayerLandscapeSelectorWindow moreActionSelectorWindow;

    public BasePetkitPlayerLandscapeView(Context context) {
        super(context);
        initAll(context);
    }

    public BasePetkitPlayerLandscapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAll(context);
    }

    public BasePetkitPlayerLandscapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context);
    }

    private void initAll(Context context){
        initView(context);
        initPopupWindow(context);
        initOtherView(context);
        initClickEvent();
    }

    public void setViewClickListener(BasePetkitPlayerLandscapeViewClickListener viewClickListener) {
        this.viewClickListener = viewClickListener;
    }

    private void initPopupWindow(Context context){
//        initTimesSpeedSelectorWindow(context);
    }

    protected void initTimesSpeedSelectorWindow(OnClickListener selectorViewClickListener,
                                                PopupWindow.OnDismissListener onDismissListener, String... timesSpeedOptions){
        timesSpeedSelectorWindow = new BasePetkitPlayerLandscapeSelectorWindow(getContext());
        if (timesSpeedOptions.length == 1){
            timesSpeedSelectorWindow.setSelectorOptionsText(timesSpeedOptions[0], null, null, selectorViewClickListener);
        } else if (timesSpeedOptions.length == 2){
            timesSpeedSelectorWindow.setSelectorOptionsText(timesSpeedOptions[0], timesSpeedOptions[1], null, selectorViewClickListener);
        } else if (timesSpeedOptions.length == 3){
            timesSpeedSelectorWindow.setSelectorOptionsText(timesSpeedOptions[0], timesSpeedOptions[1], timesSpeedOptions[2], selectorViewClickListener);
        }
        timesSpeedSelectorWindow.setOnDismissListener(onDismissListener);
    }

    protected void showTimesSpeedSelectorWindow(int cloudVideoTimesSpeedType){
        if (cloudVideoTimesSpeedType == 1){
            timesSpeedSelectorWindow.setSelectorOptionViewStyleResId(R.style.New_Style_Content_16_D4sh_Main_Orange_With_Bold,
                    null,null);
        } else if (cloudVideoTimesSpeedType == 2){
            timesSpeedSelectorWindow.setSelectorOptionViewStyleResId(null,
                    R.style.New_Style_Content_16_D4sh_Main_Orange_With_Bold, null);
        } else if (cloudVideoTimesSpeedType == 4){
            timesSpeedSelectorWindow.setSelectorOptionViewStyleResId(null, null,
                    R.style.New_Style_Content_16_D4sh_Main_Orange_With_Bold);
        }
        timesSpeedSelectorWindow.showAtLocation(((Activity)getContext()).getWindow().getDecorView(), Gravity.RIGHT, 0, 0);
    }

    protected void initMoreActionSelectorWindow(OnClickListener selectorViewClickListener,
                                                PopupWindow.OnDismissListener onDismissListener, String... actionOptions){
        moreActionSelectorWindow = new BasePetkitPlayerLandscapeSelectorWindow(getContext());
        if (actionOptions.length == 1){
            moreActionSelectorWindow.setSelectorOptionsText(actionOptions[0], null, null, selectorViewClickListener);
        } else if (actionOptions.length == 2){
            moreActionSelectorWindow.setSelectorOptionsText(actionOptions[0], actionOptions[1], null, selectorViewClickListener);
        } else if (actionOptions.length == 3){
            moreActionSelectorWindow.setSelectorOptionsText(actionOptions[0], actionOptions[1], actionOptions[2], selectorViewClickListener);
        }
        moreActionSelectorWindow.setOnDismissListener(onDismissListener);
    }

    protected void showMoreActionSelectorWindow(){
        moreActionSelectorWindow.showAtLocation(((Activity)getContext()).getWindow().getDecorView(), Gravity.RIGHT, 0, 0);
    }

    protected abstract void initView(Context context);

    protected abstract void initClickEvent();

    protected abstract void initOtherView(Context context);

    public abstract void hideOneself();

    public abstract void showOneself(Boolean liveStatus, Boolean tripodHeadMode);

    public abstract void setQuality(String text);

    public abstract void setTimeSpeed(String text);

    public abstract void setVolumeImageResource(int imageResource);

    public abstract void setTripodHeadStatus(Boolean showTripodHeadImage, Integer tripodHeadImageResource, Boolean showCcvl);

    public abstract void setRecordBtnImageResource(Boolean recording, int imageResource);

    public abstract void setIntercomBtnImageResource(int imageResource);

    public abstract void setRecordTimeText(Boolean showRecordTimeTextView, String recordTimeText);

    public abstract void setExtraMealBtnImageResource(Integer imageResource, Integer imageBackgroundResId);

    public abstract void setPlayerSoundWaveViewStatus(Boolean showPlayerSoundWaveView, Integer db);

    public abstract void setSeekbarStatus(Integer progress, Integer max, String videoTime);

    public abstract void setPlayerSwitchImageViewResource(Integer playerSwitchImageViewResource);

    public abstract void setLiveButtonVisibleStatus(Boolean showLiveButton);

    public abstract void setRecordButtonVisibleStatus(Boolean showRecordButton);

    public abstract void resetRecordViewStatus();

    public abstract void setTosvSelectIndex(int index);

    public abstract void setRecordVideoType(int recordVideoType);

}
