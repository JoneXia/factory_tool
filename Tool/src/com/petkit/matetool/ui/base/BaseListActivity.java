package com.petkit.matetool.ui.base;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.petkit.matetool.widget.pulltorefresh.PullToRefreshBase;
import com.petkit.matetool.widget.pulltorefresh.PullToRefreshListView;
import com.petkit.matetool.R;

public abstract class BaseListActivity extends BaseActivity implements PullToRefreshBase.OnRefreshListener2<ListView>,
						AdapterView.OnItemClickListener, OnClickListener{
	
	protected final static int ListView_State_Loadding = 0;
	protected final static int ListView_State_Normal = 1;
	protected final static int ListView_State_Fail = 2;
	protected final static int ListView_State_Empty = 3;
	protected final static int ListView_State_GONE = 4;
	
	
	protected PullToRefreshListView mListView;
	protected View mEmptyView, mLoaddingView;
	protected LinearLayout mBottomView, mTopView;
	
	/**
	 * 存储当前的网络状态，不控制界面显示，因为有些界面固定显示listview，失败的状态显示在listview的item里面
	 */
	protected int mNetworkState = ListView_State_Loadding;
	
	private final AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(BaseListActivity.this.getCurrentFocus() != null){
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(BaseListActivity.this
										.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_normal_list);
	}

	@Override
	protected void setupViews() {
		
		mEmptyView = findViewById(R.id.list_empty);
		mLoaddingView = findViewById(R.id.fullscreen_loading_indicator);
		mEmptyView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onRefresh();
			}
		});
		
		mListView = (PullToRefreshListView) findViewById(R.id.list);
		
		mListView.setScrollbarFadingEnabled(true);
		mListView.setOnScrollListener(mScrollListener);
		mListView.setOnRefreshListener(this);
		mListView.setOnItemClickListener(this);
		
		ImageButton leftButton = (ImageButton) findViewById(R.id.title_left_btn);
		leftButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		mBottomView = (LinearLayout) findViewById(R.id.list_bottom_view);
		mTopView = (LinearLayout) findViewById(R.id.list_top_view);
	}
	
	@Override
	public void onSizeChange(boolean flag, int listHeight, int inputViewHeight) {
		if(listHeight > 0 || inputViewHeight > 0){
			Rect r = new Rect();
			View rootview = this.getWindow().getDecorView();
			rootview.getWindowVisibleDisplayFrame(r);
			
			int maxHeight = (int) (r.bottom - r.top - getResources().getDimension(R.dimen.base_titleheight) - inputViewHeight);
			ViewGroup.LayoutParams params = mListView.getRefreshableView().getLayoutParams();  
		    params.height = flag ? (maxHeight) : ViewGroup.LayoutParams.MATCH_PARENT;  
		    mListView.getRefreshableView().setLayoutParams(params);  
		    mListView.getRefreshableView().requestLayout();
		}else{
			super.onSizeChange(flag, listHeight, inputViewHeight);
		}
	}
	
	
	/**
	 * 设置listview的顶部view，和listview平级
	 * @param layoutId
	 */
	protected void setListTopView(int layoutId){
		View view = LayoutInflater.from(this).inflate(layoutId, null);
		mTopView.addView(view);
		mTopView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 设置listview的底部view，和listview平级
	 * @param layoutId
	 */
	protected void setListBottomView(int layoutId){
		View view = LayoutInflater.from(this).inflate(layoutId, null);
		mBottomView.addView(view);
		mBottomView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 设置listview的状态״̬
	 * @param state  ListView_State_Loadding = 0;
	 * 				 ListView_State_Normal = 1;
	 * 				 ListView_State_Fail = 2;
	 * 				 ListView_State_Empty = 3;
	 * 				 ListView_State_GONE = 4;
	 */
//	protected void setListViewState(int state) {
//		setListViewState(state, 0, 0, 0);
//	}
//	
//	/**
//	 * 设置listview的状态
//	 * 
//	 * @param state	 ListView_State_Loadding = 0;
//	 * 				 ListView_State_Normal = 1;
//	 * 				 ListView_State_Fail = 2;
//	 * 				 ListView_State_Empty = 3;
//	 * 				 ListView_State_GONE = 4;
//	 * @param iconResId
//	 * @param textResId
//	 */
	protected void setListViewState(int state){
		switch (state) {
		case ListView_State_Loadding:
			mEmptyView.setVisibility(View.GONE);
			mLoaddingView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			break;
		case ListView_State_Normal:
			mEmptyView.setVisibility(View.GONE);
			mLoaddingView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			break;
		case ListView_State_Fail:
		case ListView_State_Empty:
			mEmptyView.setVisibility(View.VISIBLE);
			mLoaddingView.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			break;
		case ListView_State_GONE:
			mEmptyView.setVisibility(View.GONE);
			mLoaddingView.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}
	
	protected void setListViewEmpty(int iconResId, int textResId, int btnResId, OnClickListener listener) {
		if(textResId > 0){
			TextView emptyTextView = (TextView) mEmptyView.findViewById(R.id.list_empty_text);
			emptyTextView.setVisibility(View.VISIBLE);
			emptyTextView.setText(textResId);
		}
		if(iconResId > 0){
			ImageView emptyImageView = (ImageView) mEmptyView.findViewById(R.id.list_empty_image);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyImageView.setImageResource(iconResId);
		}
		if(btnResId > 0){
			Button emptyButton = (Button) mEmptyView.findViewById(R.id.list_empty_btn);
			emptyButton.setText(btnResId);
			emptyButton.setVisibility(View.VISIBLE);
			emptyButton.setOnClickListener(listener);
		}
	}
	
	protected void setListViewEmpty(int iconResId, String text, int btnResId, OnClickListener listener) {
		if(text != null){
			TextView emptyTextView = (TextView) mEmptyView.findViewById(R.id.list_empty_text);
			emptyTextView.setVisibility(View.VISIBLE);
			emptyTextView.setText(text);
		}
		if(iconResId > 0){
			ImageView emptyImageView = (ImageView) mEmptyView.findViewById(R.id.list_empty_image);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyImageView.setImageResource(iconResId);
		}
		if(btnResId > 0){
			Button emptyButton = (Button) mEmptyView.findViewById(R.id.list_empty_btn);
			emptyButton.setText(btnResId);
			emptyButton.setVisibility(View.VISIBLE);
			emptyButton.setOnClickListener(listener);
		}
	}
	
	/**
	 * 获取listview的item view，根据mNetworkState的状态，返回loadingview或者emptyview
	 * @return
	 */
	protected View getStateView(){
		View view;
		if(mNetworkState == ListView_State_Loadding){
			view = LayoutInflater.from(this).inflate(
					R.layout.layout_fullscreen_loading_indicator, null);
		}else{
			view = LayoutInflater.from(this).inflate(
					R.layout.layout_list_empty, null);
			
			view.setVisibility(View.VISIBLE);
			if(mNetworkState == ListView_State_Fail){
				TextView emptyTextView = (TextView) view.findViewById(R.id.list_empty_text);
				emptyTextView.setText(R.string.Status_error);
			}
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					onRefresh();
				}
			});
		}
		
		return view;
	}
	
	/**
	 * 列表为空或者联网失败时，点击刷新
	 */
	protected abstract void onRefresh();
}
