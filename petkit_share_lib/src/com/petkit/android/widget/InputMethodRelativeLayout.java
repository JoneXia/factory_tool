package com.petkit.android.widget;

import com.petkit.android.utils.ConvertDipPx;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.RelativeLayout;

public class InputMethodRelativeLayout extends RelativeLayout {
	private int width;
	
	private int height;
	
	private int screenHeight;
	
	private boolean sizeChanged = false;
	
	private OnSizeChangedListener onSizeChangedListener;
	
	@SuppressWarnings("deprecation")
	public InputMethodRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		Display localDisplay = ((Activity)context).getWindowManager().getDefaultDisplay();
		screenHeight = localDisplay.getHeight();
	}

	public InputMethodRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InputMethodRelativeLayout(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.width = widthMeasureSpec;
		this.height = heightMeasureSpec;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		//监听不为空、宽度不变、当前高度与历史高度不为0
		if(this.onSizeChangedListener != null && w == oldw && h != 0 && oldh != 0){
			/**
			 * add  ConvertDipPx.dip2px(getContext(), 80), as some device, softkey change focus, re-come here
			 */
			if(h >= (oldh + ConvertDipPx.dip2px(getContext(), 80)) || (Math.abs(h - oldh) <= 1 * this.screenHeight / 4)){	
				sizeChanged = false;
			}else if(h <= oldh || (Math.abs(h - oldh) <= 1 * this.screenHeight / 4)){
				sizeChanged = true;
			}
			this.onSizeChangedListener.onSizeChange(sizeChanged, 0, 0);
			measure(this.width - w + getWidth(), this.height - h + getHeight());
		}
	}
	
	/** 
	* @Title: setOnSizeChangedListener 
	* @Description: 为当前布局设置onSizeChanged监听器 
	* @param sizeChangedListener
	* @return void 
	*/ 
	public void setOnSizeChangedListener(OnSizeChangedListener sizeChangedListener) {
		this.onSizeChangedListener = sizeChangedListener;
	}
	
	
	public abstract interface OnSizeChangedListener{
		public abstract void onSizeChange(boolean flag, int listHeight, int inputViewHeight);
	}
}
