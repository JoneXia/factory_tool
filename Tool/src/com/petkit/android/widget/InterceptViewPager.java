package com.petkit.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.viewpager.widget.ViewPager;


/**
 * 
 * 自定义Viewpager，有两个功能：
 * 1，可禁止翻页，enable为true时开启
 * 2，完美解决双层嵌套，不支持三层及以上的嵌套
 * 
 * @author Jone
 *
 */
public class InterceptViewPager extends ViewPager {

	private boolean enabled;

	public InterceptViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.enabled = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onTouchEvent(event);
		}
		
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.enabled) {
			ViewParent viewParent = getParent();
			while(viewParent != null){
				if(viewParent instanceof ViewPager){
					break;
				}else{
					viewParent = viewParent.getParent();
				}
			}
			if(viewParent != null){
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					viewParent.requestDisallowInterceptTouchEvent(true);
				}

				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					viewParent.requestDisallowInterceptTouchEvent(true);
				}

				if (event.getAction() == MotionEvent.ACTION_UP) {
					viewParent.requestDisallowInterceptTouchEvent(false);
				}
			}
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}

	public void setPagingEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
