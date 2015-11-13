package com.petkit.matetool.widget.pulltorefresh;

import com.petkit.matetool.R;
import com.petkit.matetool.widget.pulltorefresh.impl.IIndexBarFilter;
import com.petkit.matetool.widget.pulltorefresh.impl.IPinnedHeader;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * 支持ListView置顶功能
 */
public class PinnedHeaderListView extends ListView implements IIndexBarFilter {

	IPinnedHeader mAdapter;

	View mHeaderView, mIndexBarView, mPreviewTextView;

	boolean mHeaderViewVisible;

	Context mContext;

	int mHeaderViewWidth, mHeaderViewHeight,

	mIndexBarViewWidth, mIndexBarViewHeight, mIndexBarViewMargin,

	mPreviewTextViewWidth, mPreviewTextViewHeight;

	public PinnedHeaderListView(Context context) {
		super(context);
		this.mContext = context;
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	public void setAdapter(ListAdapter mAdapter) {
		this.mAdapter = (IPinnedHeader) mAdapter;
		super.setAdapter(mAdapter);
	}

	public void setPinnedHeaderView(View mHeaderView) {
		this.mHeaderView = mHeaderView;
		// Disable vertical fading when the pinned header is present
		// change ListView to allow separate measures for top and bottom
		// fading edge;
		// in this particular case we would like to disable the top, but not the
		// bottom edge.
		if (mHeaderView != null) {
			setFadingEdgeLength(0);
		}

	}

	public void setIndexBarView(View mIndexBarView) {
		mIndexBarViewMargin = (int) mContext.getResources().getDimension(R.dimen.index_bar_view_margin);
		this.mIndexBarView = mIndexBarView;
	}

	public void setPreviewView(View mPreviewTextView) {
		this.mPreviewTextView = mPreviewTextView;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mHeaderView != null) {
			measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
			mHeaderViewWidth = mHeaderView.getMeasuredWidth();
			mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		}

		if (mIndexBarView != null && IS_INDEX_BAR_VIEW_VISIBLE) {
			measureChild(mIndexBarView, widthMeasureSpec, heightMeasureSpec);
			mIndexBarViewWidth = mIndexBarView.getMeasuredWidth();
			mIndexBarViewHeight = mIndexBarView.getMeasuredHeight();
		}

		if (mPreviewTextView != null && IS_PREVIEW_TEXT_VISIBLE) {
			measureChild(mPreviewTextView, widthMeasureSpec, heightMeasureSpec);
			mPreviewTextViewWidth = mPreviewTextView.getMeasuredWidth();
			mPreviewTextViewHeight = mPreviewTextView.getMeasuredHeight();
		}

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mHeaderView != null) {
			mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			configureHeaderView(getFirstVisiblePosition());
		}

		if (mIndexBarView != null && IS_INDEX_BAR_VIEW_VISIBLE) {
			mIndexBarView.layout(getMeasuredWidth() - mIndexBarViewMargin
					- mIndexBarViewWidth, mIndexBarViewMargin,
					getMeasuredWidth() - mIndexBarViewMargin,
					getMeasuredHeight() - mIndexBarViewMargin);
		}

		if (mPreviewTextView != null && IS_PREVIEW_TEXT_VISIBLE) {
			mPreviewTextView.layout(mIndexBarView.getLeft()
					- mPreviewTextViewWidth, (int) msideIndexY
					- (mPreviewTextViewHeight / 2), mIndexBarView.getLeft(),
					(int) (msideIndexY - (mPreviewTextViewHeight / 2))
							+ mPreviewTextViewHeight);
		}

	}

	boolean IS_PREVIEW_TEXT_VISIBLE;
	boolean IS_INDEX_BAR_VIEW_VISIBLE = true;

	public void showIndexBarView() {
		IS_INDEX_BAR_VIEW_VISIBLE = true;
	}

	public void hideIndexBarView() {
		IS_INDEX_BAR_VIEW_VISIBLE = false;
	}

	private void showPreviewTextView() {
		IS_PREVIEW_TEXT_VISIBLE = true;
	}

	private void hidePreviewTextView() {
		IS_PREVIEW_TEXT_VISIBLE = false;
	}

	public void configureHeaderView(int position) {
		if (mHeaderView == null) {
			return;
		}

		int state = mAdapter.getPinnedHeaderState(position);

//		Log.e("state", "" + state);
		switch (state) {
		case IPinnedHeader.PINNED_HEADER_GONE: {
			mHeaderViewVisible = false;
			break;
		}

		case IPinnedHeader.PINNED_HEADER_VISIBLE: {

//			Log.e("mHeaderView",
//					mHeaderView.getLeft() + " , " + mHeaderView.getTop()
//							+ " , " + mHeaderView.getBottom());

			if (mHeaderView.getTop() != 0) {
				mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			}

			mAdapter.configurePinnedHeader(mHeaderView, position);
			mHeaderViewVisible = true;
			break;
		}

		case IPinnedHeader.PINNED_HEADER_PUSHED_UP: {
			// 第一个item
			View firstView = getChildAt(0);
			int bottom = firstView.getBottom();
			// int itemHeight = firstView.getHeight();
			int headerHeight = mHeaderView.getHeight();

//			Log.e("bottom + headerHeight", bottom + " , " + headerHeight);

			int y;
			if (bottom <= headerHeight) {
				y = (bottom - headerHeight);
			} else {
				y = 0;
			}

//			Log.e("y", "" + y);
//
			if (mHeaderView.getTop() != y) {
				mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight
						+ y);
			} else {
				mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight);
			}

			mAdapter.configurePinnedHeader(mHeaderView, position);
			mHeaderViewVisible = true;
			break;
		}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);// draw list view elements (zIndex == 1)

		if (mHeaderView != null && mHeaderViewVisible)
			drawChild(canvas, mHeaderView, getDrawingTime()); // draw pinned
																// header view
																// (zIndex == 2)

		if (mIndexBarView != null && IS_INDEX_BAR_VIEW_VISIBLE)
			drawChild(canvas, mIndexBarView, getDrawingTime()); // draw index
																// bar view

		if (mPreviewTextView != null && IS_PREVIEW_TEXT_VISIBLE)
			drawChild(canvas, mPreviewTextView, getDrawingTime()); // draw
																	// preview
																	// text view
																	// (zIndex
																	// == 4)
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mIndexBarView != null
				&& ((IndexBarView) mIndexBarView).onTouchEvent(ev)) {
			showPreviewTextView();
			return true;
		} else {
			hidePreviewTextView();
			return super.onTouchEvent(ev);
		}
	}

	float msideIndexY;

	@Override
	public void filterList(float msideIndexY, int position, String preview_text) {
		this.msideIndexY = msideIndexY;

		if (mPreviewTextView instanceof TextView)
			((TextView) mPreviewTextView).setText(preview_text);

		setSelection(position);
	}

}
