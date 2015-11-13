package com.petkit.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InterceptLinearLayout extends LinearLayout {
	
	public InterceptLinearLayout(Context context) {
		super(context);
	}

	public InterceptLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private boolean isNeedIntercept = false;
	private onInterceptListener interceptListener = null;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(interceptListener != null){
			interceptListener.onIntercept(isNeedIntercept);
		}
		return isNeedIntercept;
	}

	public boolean isNeedIntercept() {
		return isNeedIntercept;
	}

	public void setNeedIntercept(boolean isNeedIntercept) {
		this.isNeedIntercept = isNeedIntercept;
	}
	
	public interface onInterceptListener{
		public void onIntercept(boolean isIntercept);
	}

	public onInterceptListener getInterceptListener() {
		return interceptListener;
	}

	public void setInterceptListener(onInterceptListener interceptListener) {
		this.interceptListener = interceptListener;
	}


}
