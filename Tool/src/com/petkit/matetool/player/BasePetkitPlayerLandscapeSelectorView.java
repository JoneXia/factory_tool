package com.petkit.matetool.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petkit.matetool.R;


public class BasePetkitPlayerLandscapeSelectorView extends RelativeLayout {

    private TextView selectorOption1TextView;
    private TextView selectorOption2TextView;
    private TextView selectorOption3TextView;

    public BasePetkitPlayerLandscapeSelectorView(Context context) {
        this(context, null);
    }

    public BasePetkitPlayerLandscapeSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePetkitPlayerLandscapeSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context);
    }

    private void initAll(Context context){
        setGravity(Gravity.CENTER_VERTICAL);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.base_petkit_player_landscape_selector_view, this);

        selectorOption1TextView = findViewById(R.id.selector_option1_text_view);
        selectorOption2TextView = findViewById(R.id.selector_option2_text_view);
        selectorOption3TextView = findViewById(R.id.selector_option3_text_view);
    }

    public void setSelectorTextAndClickListener(String selectorOption1Text, Integer selectorOption1TextStyleResId, OnClickListener selectorOptionViewClickListener,
                                                String selectorOption2Text, Integer selectorOption2TextStyleResId, String selectorOption3Text, Integer selectorOption3TextStyleResId){
        if (selectorOption1Text != null){
            selectorOption1TextView.setVisibility(VISIBLE);
            selectorOption1TextView.setText(selectorOption1Text);
            if (selectorOption1TextStyleResId != null){
                selectorOption1TextView.setTextAppearance(getContext(), selectorOption1TextStyleResId);
            } else {
                selectorOption1TextView.setTextAppearance(getContext(), R.style.New_Style_Content_16_Light_White_With_Bold);
            }
            if (selectorOptionViewClickListener != null){
                selectorOption1TextView.setOnClickListener(selectorOptionViewClickListener);
            }
        } else {
            selectorOption1TextView.setVisibility(GONE);
        }

        if (selectorOption2Text != null){
            selectorOption2TextView.setVisibility(VISIBLE);
            selectorOption2TextView.setText(selectorOption2Text);
            if (selectorOption2TextStyleResId != null){
                selectorOption2TextView.setTextAppearance(getContext(), selectorOption2TextStyleResId);
            } else {
                selectorOption2TextView.setTextAppearance(getContext(), R.style.New_Style_Content_16_Light_White_With_Bold);
            }
            if (selectorOptionViewClickListener != null){
                selectorOption2TextView.setOnClickListener(selectorOptionViewClickListener);
            }
        } else {
            selectorOption2TextView.setVisibility(GONE);
        }

        if (selectorOption3Text != null){
            selectorOption3TextView.setVisibility(VISIBLE);
            selectorOption3TextView.setText(selectorOption3Text);
            if (selectorOption3TextStyleResId != null){
                selectorOption3TextView.setTextAppearance(getContext(), selectorOption3TextStyleResId);
            } else {
                selectorOption3TextView.setTextAppearance(getContext(), R.style.New_Style_Content_16_Light_White_With_Bold);
            }
            if (selectorOptionViewClickListener != null){
                selectorOption3TextView.setOnClickListener(selectorOptionViewClickListener);
            }
        } else {
            selectorOption3TextView.setVisibility(GONE);
        }
    }

}
