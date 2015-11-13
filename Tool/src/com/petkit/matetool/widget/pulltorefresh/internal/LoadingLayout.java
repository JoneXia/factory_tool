/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.petkit.matetool.widget.pulltorefresh.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.petkit.matetool.widget.pulltorefresh.ILoadingLayout;
import com.petkit.matetool.R;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase.Mode;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase.Orientation;

@SuppressLint("ViewConstructor")
public abstract class LoadingLayout extends FrameLayout implements ILoadingLayout {

	static final String LOG_TAG = "PullToRefresh-LoadingLayout";

	static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();

	private FrameLayout mInnerLayout;

	protected final ImageView mHeaderImage;
//	protected final ProgressBar mHeaderProgress;

	private boolean mUseIntrinsicAnimation;

//	private final TextView mHeaderText;
//	private final TextView mSubHeaderText;

	protected final Mode mMode;
	protected final Orientation mScrollDirection;

	private CharSequence mPullLabel;
	private CharSequence mRefreshingLabel;
	private CharSequence mReleaseLabel;
	
	private float imageWidth, imageHeight;
	
	private int verticalHeaderHeight = 0;
	
	

	public LoadingLayout(Context context, final Mode mode, final Orientation scrollDirection, TypedArray attrs) {
		super(context);
		mMode = mode;
		mScrollDirection = scrollDirection;

		switch (scrollDirection) {
			case HORIZONTAL:
				LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_horizontal, this);
				break;
			case VERTICAL:
			default:
				LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_vertical, this);
				break;
		}

		mInnerLayout = (FrameLayout) findViewById(R.id.fl_inner);
//		resId = MResource.getResourceIdByName(getContext().getPackageName(), "id", "pull_to_refresh_text");
//		mHeaderText = (TextView) mInnerLayout.findViewById(resId);
//		resId = MResource.getResourceIdByName(getContext().getPackageName(), "id", "pull_to_refresh_progress");
//		mHeaderProgress = (ProgressBar) mInnerLayout.findViewById(resId);
//		resId = MResource.getResourceIdByName(getContext().getPackageName(), "id", "pull_to_refresh_sub_text");
//		mSubHeaderText = (TextView) mInnerLayout.findViewById(resId);
		mHeaderImage = (ImageView) mInnerLayout.findViewById(R.id.pull_to_refresh_image);

		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInnerLayout.getLayoutParams();

		switch (mode) {
			case PULL_FROM_END:
				lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.TOP : Gravity.LEFT;

				// Load in labels
				mPullLabel = context.getString(R.string.Pull_to_load_more);
				mRefreshingLabel = context.getString(R.string.Loading);
				mReleaseLabel = context.getString(R.string.Release_to_refresh);
				break;

			case PULL_FROM_START:
			default:
				lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.BOTTOM : Gravity.RIGHT;

				// Load in labels
				mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
				mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
				mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
				break;
		}

		// Try and get defined drawable from Attrs
		Drawable imageDrawable = null;

		// Check Specific Drawable from Attrs, these overrite the generic
		// drawable attr above
		switch (mode) {
			case PULL_FROM_START:
			default:
//				if (attrs != null && attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableStart)) {
//					imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableStart);
//				} else if (attrs != null && attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableTop)) {
//					Utils.warnDeprecation("ptrDrawableTop", "ptrDrawableStart");
//					imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableTop);
//				}
				break;

			case PULL_FROM_END:
//				if (attrs != null && attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableEnd)) {
//					imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableEnd);
//				} else if (attrs != null && attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableBottom)) {
//					Utils.warnDeprecation("ptrDrawableBottom", "ptrDrawableEnd");
//					imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableBottom);
//				}
				break;
		}

		// If we don't have a user defined drawable, load the default
		if (null == imageDrawable) {
			imageDrawable = context.getResources().getDrawable(getDefaultDrawableResId());
			
			imageWidth = getResources().getDimension(R.dimen.header_image_width);
			imageHeight = getResources().getDimension(R.dimen.header_image_height);

//			int paddingTop = MResource.getResourceIdByName(getContext().getPackageName(), "dimen", "header_footer_top_bottom_padding");
			verticalHeaderHeight = (int) (imageHeight * 0.8f);//(int) (imageHeight + getResources().getDimension(paddingTop));
			
//			AnimationDrawable animationDrawable = new AnimationDrawable();
//			for(int i = 1; i <= 5; i++){   
//	            int id = getResources().getIdentifier("loadding_icon" + i, "drawable", getContext().getPackageName());   
//	            //此方法返回一个可绘制的对象与特定的资源ID相关联    
//	            Drawable mBitAnimation = getResources().getDrawable(id);   
//	            /*为动画添加一帧*/  
//	            //参数mBitAnimation是该帧的图片   
//	            //参数500是该帧显示的时间，按毫秒计算   
//	            animationDrawable.addFrame(mBitAnimation, 150);   
//	        }
//			
//			imageDrawable = animationDrawable;
		}

		// Set Drawable, and save width/height
		setLoadingDrawable(imageDrawable);

		reset();
	}

	public final void setHeight(int height) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
		lp.height = height;
		requestLayout();
	}

	public final void setWidth(int width) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
		lp.width = width;
		requestLayout();
	}

	public final int getContentSize() {
		switch (mScrollDirection) {
			case HORIZONTAL:
				return mInnerLayout.getWidth();
			case VERTICAL:
			default:
				if(verticalHeaderHeight > 0){
					return verticalHeaderHeight;
				}
				return mInnerLayout.getHeight();
		}
	}

	public final void hideAllViews() {
//		if (View.VISIBLE == mHeaderText.getVisibility()) {
//			mHeaderText.setVisibility(View.INVISIBLE);
//		}
//		if (View.VISIBLE == mHeaderProgress.getVisibility()) {
//			mHeaderProgress.setVisibility(View.INVISIBLE);
//		}
		if (View.VISIBLE == mHeaderImage.getVisibility()) {
			mHeaderImage.setVisibility(View.INVISIBLE);
		}
//		if (View.VISIBLE == mSubHeaderText.getVisibility()) {
//			mSubHeaderText.setVisibility(View.INVISIBLE);
//		}
	}

	public final void onPull(float scaleOfLayout) {
		
		changeImageLayoutParams(scaleOfLayout / 2 * 0.95f);
		if (!mUseIntrinsicAnimation) {
			onPullImpl(scaleOfLayout);
		}
	}

	public final void pullToRefresh() {
//		if (null != mHeaderText) {
//			mHeaderText.setText(mPullLabel);
//		}

		// Now call the callback
		pullToRefreshImpl();
	}

	public final void refreshing() {
//		if (null != mHeaderText) {
//			mHeaderText.setText(mRefreshingLabel);
//		}

		if (mUseIntrinsicAnimation) {
			((AnimationDrawable) mHeaderImage.getDrawable()).start();
			changeImageLayoutParams(0.6f);
		} else {
			// Now call the callback
			refreshingImpl();
		}

//		if (null != mSubHeaderText) {
//			mSubHeaderText.setVisibility(View.GONE);
//		}
	}
	
	private void changeImageLayoutParams(float scale){
		ViewGroup.LayoutParams params = mHeaderImage.getLayoutParams();
		params.height = (int) (imageHeight * scale);
		mHeaderImage.setLayoutParams(params);
	}

	public final void releaseToRefresh() {
//		if (null != mHeaderText) {
//			mHeaderText.setText(mReleaseLabel);
//		}
		// Now call the callback
		releaseToRefreshImpl();
	}

	public final void reset() {
//		if (null != mHeaderText) {
//			mHeaderText.setText(mPullLabel);
//		}
		
//		ViewGroup.LayoutParams params = mHeaderImage.getLayoutParams();
//		if(params.height > 0){
//			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//			mHeaderImage.setLayoutParams(params);
//		}
		
		mHeaderImage.setVisibility(View.VISIBLE);
		

		if (mUseIntrinsicAnimation) {
			((AnimationDrawable) mHeaderImage.getDrawable()).stop();
		} else {
			// Now call the callback
			resetImpl();
		}

//		if (null != mSubHeaderText) {
//			if (TextUtils.isEmpty(mSubHeaderText.getText())) {
//				mSubHeaderText.setVisibility(View.GONE);
//			} else {
//				mSubHeaderText.setVisibility(View.VISIBLE);
//			}
//		}
	}

	@Override
	public void setLastUpdatedLabel(CharSequence label) {
		setSubHeaderText(label);
	}

	public final void setLoadingDrawable(Drawable imageDrawable) {
		// Set Drawable
		mHeaderImage.setImageDrawable(imageDrawable);
		mUseIntrinsicAnimation = (imageDrawable instanceof AnimationDrawable);

		// Now call the callback
		onLoadingDrawableSet(imageDrawable);
	}

	public void setPullLabel(CharSequence pullLabel) {
		mPullLabel = pullLabel;
	}

	public void setRefreshingLabel(CharSequence refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(CharSequence releaseLabel) {
		mReleaseLabel = releaseLabel;
	}

	@Override
	public void setTextTypeface(Typeface tf) {
//		mHeaderText.setTypeface(tf);
	}

	public final void showInvisibleViews() {
//		if (View.INVISIBLE == mHeaderText.getVisibility()) {
//			mHeaderText.setVisibility(View.VISIBLE);
//		}
//		if (View.INVISIBLE == mHeaderProgress.getVisibility()) {
//			mHeaderProgress.setVisibility(View.VISIBLE);
//		}
		if (View.INVISIBLE == mHeaderImage.getVisibility()) {
			mHeaderImage.setVisibility(View.VISIBLE);
		}
//		if (View.INVISIBLE == mSubHeaderText.getVisibility()) {
//			mSubHeaderText.setVisibility(View.VISIBLE);
//		}
	}

	/**
	 * Callbacks for derivative Layouts
	 */

	protected abstract int getDefaultDrawableResId();

	protected abstract void onLoadingDrawableSet(Drawable imageDrawable);

	protected abstract void onPullImpl(float scaleOfLayout);

	protected abstract void pullToRefreshImpl();

	protected abstract void refreshingImpl();

	protected abstract void releaseToRefreshImpl();

	protected abstract void resetImpl();

	private void setSubHeaderText(CharSequence label) {
//		if (null != mSubHeaderText) {
//			if (TextUtils.isEmpty(label)) {
//				mSubHeaderText.setVisibility(View.GONE);
//			} else {
//				mSubHeaderText.setText(label);
//
//				// Only set it to Visible if we're GONE, otherwise VISIBLE will
//				// be set soon
//				if (View.GONE == mSubHeaderText.getVisibility()) {
//					mSubHeaderText.setVisibility(View.VISIBLE);
//				}
//			}
//		}
	}

	private void setSubTextAppearance(int value) {
//		if (null != mSubHeaderText) {
//			mSubHeaderText.setTextAppearance(getContext(), value);
//		}
	}

	private void setSubTextColor(ColorStateList color) {
//		if (null != mSubHeaderText) {
//			mSubHeaderText.setTextColor(color);
//		}
	}

	private void setTextAppearance(int value) {
//		if (null != mHeaderText) {
//			mHeaderText.setTextAppearance(getContext(), value);
//		}
//		if (null != mSubHeaderText) {
//			mSubHeaderText.setTextAppearance(getContext(), value);
//		}
	}

	private void setTextColor(ColorStateList color) {
//		if (null != mHeaderText) {
//			mHeaderText.setTextColor(color);
//		}
//		if (null != mSubHeaderText) {
//			mSubHeaderText.setTextColor(color);
//		}
	}

}
