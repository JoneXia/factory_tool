package com.petkit.matetool.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.utils.UiUtils;

import androidx.annotation.Nullable;

/**
 * Description:
 * author:jiawei.hao
 * Date:11/4/22
 */
public class PetkitLogoLoadingView extends LinearLayout {

    private ImageView logoImageView;
    private ImageView loadingImageView;
    private Animator rotateAnimator;
    private Context mContext;

    public PetkitLogoLoadingView(Context context) {
        super(context);
    }

    public PetkitLogoLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public PetkitLogoLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs){
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        mContext = context;

        View petkitLogoView = LayoutInflater.from(context).inflate(R.layout.layout_petkit_logo_loading, this, false);
        addView(petkitLogoView);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PetkitLogoLoadingView);
        int logoWidth = a.getDimensionPixelSize(R.styleable.PetkitLogoLoadingView_pllv_icon_width, UiUtils.dip2px(context, 44));
        int logoHeight = a.getDimensionPixelSize(R.styleable.PetkitLogoLoadingView_pllv_icon_height, UiUtils.dip2px(context,44));
        LayoutParams layoutParams = (LayoutParams) petkitLogoView.getLayoutParams();
        layoutParams.width = logoWidth;
        layoutParams.height = logoHeight;
        petkitLogoView.setLayoutParams(layoutParams);

        logoImageView = petkitLogoView.findViewById(R.id.logo_image_view);
        loadingImageView = petkitLogoView.findViewById(R.id.loading_image_view);

        int logoSrc = a.getResourceId(R.styleable.PetkitLogoLoadingView_pllv_src, R.drawable.petkit_circle_gray_logo);
        int loadingSrc = a.getResourceId(R.styleable.PetkitLogoLoadingView_pllv_loading_src, R.drawable.circle_gray_loading_icon);

        logoImageView.setImageResource(logoSrc);
        loadingImageView.setImageResource(loadingSrc);

        TextView textView = new TextView(context);
        String text = a.getString(R.styleable.PetkitLogoLoadingView_pllv_text);
        textView.setText(text);
        int textStyle = a.getResourceId(R.styleable.PetkitLogoLoadingView_pllv_text_style, R.style.New_Style_Content_12_Gray);
        textView.setTextAppearance(context, textStyle);
        int marginTop = a.getDimensionPixelSize(R.styleable.PetkitLogoLoadingView_pllv_text_margin_top, UiUtils.dip2px(context, 11));
        LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.topMargin = marginTop;
        addView(textView,layoutParams1);

        a.recycle();
    }

    public void startLoadingAnimation(){
        if (rotateAnimator == null){
            rotateAnimator = AnimatorInflater.loadAnimator(mContext, R.animator.maintenance_mode_loading_infinite);
            rotateAnimator.setTarget(loadingImageView);
        }
        if (!rotateAnimator.isRunning()){
            rotateAnimator.start();
        }
    }

    public void cancelLoadingAnimation(){
        if (rotateAnimator != null && rotateAnimator.isRunning()){
            rotateAnimator.cancel();
        }
    }

}
