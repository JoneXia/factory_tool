package com.petkit.android.widget.pulltorefresh.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import com.petkit.android.utils.MResource;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.pulltorefresh.PullToRefreshBase;

/**
 * Created by leon on 15-10-23.
 **/
public class TweenAnimLoadingLayout extends LoadingLayout {

    private AnimationDrawable animationDrawable;

    public TweenAnimLoadingLayout(Context context, PullToRefreshBase.Mode mode, PullToRefreshBase.Orientation scrollDirection, TypedArray attrs) {
        super(context, mode, scrollDirection, attrs);
        // 初始化
        mHeaderImage.setImageResource(getDefaultDrawableResId(mode));
        animationDrawable = (AnimationDrawable) mHeaderImage.getDrawable();
    }

    @Override
    protected void onLoadingDrawableSet(Drawable imageDrawable) {
        PetkitLog.d("TweenAnimLoadingLayout->onLoadingDrawableSet");
    }

    @Override
    protected void onPullImpl(float scaleOfLayout) {
        PetkitLog.d("TweenAnimLoadingLayout->onPullImpl scaleOfLayout->" + scaleOfLayout);
    }

    @Override
    protected void pullToRefreshImpl() {
        PetkitLog.d("TweenAnimLoadingLayout->pullToRefreshImpl");
    }

    @Override
    protected void refreshingImpl() {
        animationDrawable.start();
    }

    @Override
    protected void releaseToRefreshImpl() {
        PetkitLog.d("TweenAnimLoadingLayout->releaseToRefreshImpl");
    }

    @Override
    protected void resetImpl() {
        mHeaderImage.clearAnimation();
    }

    @Override
    protected int getDefaultDrawableResId(PullToRefreshBase.Mode mode) {
        if(mode== PullToRefreshBase.Mode.PULL_FROM_START)
            return MResource.getResourceIdByName(getContext().getPackageName(), "drawable", "loadding_anim");
        else if(mode== PullToRefreshBase.Mode.PULL_FROM_END)
            return MResource.getResourceIdByName(getContext().getPackageName(), "drawable", "loadding_anim_end");
        return MResource.getResourceIdByName(getContext().getPackageName(), "drawable", "loadding_anim");
    }
}
