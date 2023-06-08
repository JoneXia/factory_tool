package com.petkit.matetool.player;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.petkit.matetool.R;
import com.petkit.matetool.utils.UiUtils;


public class BasePetkitPlayerLandscapeSelectorWindow extends PopupWindow {

    private BasePetkitPlayerLandscapeSelectorView selectorView;
    private String selectorOption1Text;
    private String selectorOption2Text;
    private String selectorOption3Text;

    public BasePetkitPlayerLandscapeSelectorWindow(Context context) {
        super(LayoutInflater.from(context).inflate(R.layout.base_petkit_player_landscape_selector_window, null),
                UiUtils.dip2px(context, 375), WindowManager.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(R.style.Base_Petkit_Player_Landscape_Selector_Window_Appearance_Animation);

        selectorView = (BasePetkitPlayerLandscapeSelectorView) getContentView();
    }

    public void setSelectorOptionsText(String selectorOptions1Text, String selectorOptions2Text, String selectorOptions3Text, View.OnClickListener selectorViewClickListener){
        this.selectorOption1Text = selectorOptions1Text;
        this.selectorOption2Text = selectorOptions2Text;
        this.selectorOption3Text = selectorOptions3Text;

        selectorView.setSelectorTextAndClickListener(selectorOptions1Text, null, selectorViewClickListener,
                selectorOptions2Text, null, selectorOptions3Text, null);
    }

    public void setSelectorOptionViewStyleResId(Integer selectorOption1TextViewStyleResId, Integer selectorOption2TextViewStyleResId,
                                    Integer selectorOption3TextViewStyleResId){
        selectorView.setSelectorTextAndClickListener(selectorOption1Text, selectorOption1TextViewStyleResId, null,
                selectorOption2Text, selectorOption2TextViewStyleResId, selectorOption3Text, selectorOption3TextViewStyleResId);
    }

}
