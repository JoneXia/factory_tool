package com.petkit.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ScrollView;


/**
 * 
 * @author Jone
 *
 */
public class InterceptScrollView extends ScrollView {

	private boolean enabled;

	public InterceptScrollView(Context context, AttributeSet attrs) {
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
				if(viewParent instanceof ScrollView){
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

	public void setScrollingEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
