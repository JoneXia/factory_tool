package com.petkit.matetool.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.common.CommonScanActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.widget.InputMethodRelativeLayout;
import com.petkit.matetool.widget.LoadDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivity extends Activity implements OnClickListener, InputMethodRelativeLayout.OnSizeChangedListener {

	public static final String BROADCAST_MSG_CLOSE_ACTIVITY = "com.petkit.android.exit";
	
	private BaseUIUtils<BaseActivity> mBaseUIUtils;

	private RelativeLayout titleView;
	protected InputMethodRelativeLayout contentView;
    protected ImageButton imb_titleleft;
	private Button btnTitleRight;
	private TextView tv_title;
	protected View mainView;
	
	protected PopupWindow mPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mainView = getLayoutInflater().inflate(R.layout.activity_base, null);
		initBaseViews();
		LayoutParams params = new LayoutParams(-1, -1);
		setContentView(mainView, params);
		
		mBaseUIUtils = new BaseUIUtils<BaseActivity>(this);
		
		registerBoradcastReceiver();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterBroadcastReceiver();
	}


	@Override
	public void onSizeChange(boolean flag, int listHeight, int inputViewHeight) {
		if(flag){  
			int deviceHeight = BaseApplication.getDisplayMetrics(this).heightPixels;
			Rect r = new Rect();
			View rootview = this.getWindow().getDecorView();
			rootview.getWindowVisibleDisplayFrame(r);
			
			contentView.setPadding(0, -(deviceHeight - r.bottom), 0, 0);  
        }else  {
        	contentView.setPadding(0, 0, 0, 0);  
        }
	}

	private void initBaseViews() {
		titleView = (RelativeLayout) mainView.findViewById(R.id.re_toptitle);
		contentView = (InputMethodRelativeLayout) mainView.findViewById(R.id.re_basecontent);
		contentView.setOnSizeChangedListener(this);  
		tv_title = (TextView) mainView.findViewById(R.id.title_name);
		imb_titleleft = (ImageButton) mainView.findViewById(R.id.title_left_btn);
		btnTitleRight = (Button) mainView.findViewById(R.id.title_right_btn);
	}
	
	@Override
	public void setContentView(int layoutResID) {
		contentView.removeAllViews();
		contentView.addView(getLayoutInflater().inflate(layoutResID, null),
				new LayoutParams(-1, -1));
		setupViews();
	}
	
	@Override
	public void setContentView(View view) {
		contentView.removeAllViews();
		contentView.addView(view, new LayoutParams(-1, -1));
	}
	
	
	public void cancel(View view){
		onBackPressed();
	}
	
	protected abstract void setupViews();
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		MobclickAgent.onResume(this);
	}
	
	public void setTitle(int stringid) {
		tv_title.setText(getString(stringid));
	}

	public void setTitle(String string) {
		tv_title.setText(string);
	}
	
	public void setTitleBackgroundColor(int color){
		titleView.setBackgroundColor(color);
	}
	
	public void setNoTitle() {
		if (titleView.getVisibility() == View.VISIBLE && titleView != null) {
			titleView.setVisibility(View.GONE);
		}
	}
	
	public void setHasTitle() {
		if (titleView.getVisibility() == View.GONE && titleView != null) {
			titleView.setVisibility(View.VISIBLE);
		}
	}

	public void setTitleLeftButton(int drawableId, OnClickListener listener) {
		if (imb_titleleft.getVisibility() != View.VISIBLE
				&& imb_titleleft != null) {
			imb_titleleft.setVisibility(View.VISIBLE);
		}
		imb_titleleft.setImageResource(drawableId);
		imb_titleleft.setOnClickListener(listener);
	}

	/**
	 * 设置标题右button的文件内容
	 * @param resId String id
	 * @param listener
	 */
	public void setTitleRightButton(int resId, OnClickListener listener) {
		setTitleRightButton(getString(resId), listener);
	}

	/**
	 * 设置标题右button的文件内容
	 * @param label
	 * @param listener
	 */
	public void setTitleRightButton(String label, OnClickListener listener) {
		btnTitleRight.setText(label);
		btnTitleRight.setOnClickListener(listener);
		btnTitleRight.setVisibility(View.VISIBLE);
	}
	
//	public Drawable getDrawable(int resid) {
//		return mBaseUIUtils.getDrawable(resid);
//	}

	public String[] getStringArray(int resid) {
		return mBaseUIUtils.getStringArray(resid);
	}

	public void showLongToast(String msg) {
		mBaseUIUtils.showLongToast(msg);
	}
	
	public void showLongToast(String msg, int iconResId) {
		mBaseUIUtils.showLongToast(msg, iconResId);
	}

	public void showLongToast(int stringId) {
		mBaseUIUtils.showLongToast(stringId);
	}

	public void showLongToast(int stringId, int iconResId) {
		mBaseUIUtils.showLongToast(stringId, iconResId);
	}

	public void showShortToast(String msg) {
		mBaseUIUtils.showShortToast(msg);
	}

	public void showShortToast(String msg, int iconResId) {
		mBaseUIUtils.showShortToast(msg, iconResId);
	}

	public void showShortToast(int stringId) {
		mBaseUIUtils.showShortToast(stringId);
	}

	public void showShortToast(int stringId, int iconResId) {
		mBaseUIUtils.showShortToast(stringId, iconResId);
	}

	public int dip2px(float dip) {
		return mBaseUIUtils.dip2px(dip);
	}

	public int px2dip(float px) {
		return mBaseUIUtils.px2dip(px);
	}

	public void startActivity(Class<?> cls) {
		startActivity(cls, false);
	}
	
	public void startActivity(Class<?> cls, boolean finish) {
		mBaseUIUtils.startActivity(cls, finish);
	}
	

	public void startActivityForResult(Class<?> cls, int requestCode) {
		mBaseUIUtils.startActivityForResult(cls, requestCode);
	}

	public void startActivityWithData(Class<?> cls, Map<String, String> map,
			boolean finish) {
		mBaseUIUtils.startActivityWithData(cls, map, finish);
	}
	
	public void startActivityWithData(Class<?> cls, Bundle bundle, boolean finish) {
		mBaseUIUtils.startActivityWithData(cls, bundle, finish);
	}

	public void changeEditTextInputType(EditText editText, boolean visiable) {
		mBaseUIUtils.changeEditTextInputType(editText, visiable);
	}


	public boolean isEmpty(String str) {
		return mBaseUIUtils.isEmpty(str);
	}


	public AlertDialog.Builder buttonDialogBuilder(String msg,
			String positivetext, DialogInterface.OnClickListener listener) {
		return mBaseUIUtils.buttonDialogBuilder(msg, positivetext, listener);
	}


	public AlertDialog.Builder button2DialogBuilder(String msg,
			String positivetext, String negtext,
			DialogInterface.OnClickListener poslistener,
			DialogInterface.OnClickListener neglistener) {
		return mBaseUIUtils.button2DialogBuilder(msg, positivetext, negtext, poslistener, neglistener);
	}

	public AlertDialog.Builder buttonTitleDialogBuilder(String title,
			String msg, String positivetext,
			DialogInterface.OnClickListener listener) {
		return mBaseUIUtils.buttonTitleDialogBuilder(title, msg, positivetext, listener);
	}

	public AlertDialog.Builder button2TitleDialogBuilder(String title,
			String msg, String positivetext, String negtext,
			DialogInterface.OnClickListener poslistener,
			DialogInterface.OnClickListener neglistener) {
		return mBaseUIUtils.button2TitleDialogBuilder(title, msg, positivetext, negtext, poslistener, neglistener);
	}

	public String getAppCacheDirPath() {
		return mBaseUIUtils.getAppCacheDirPath();
	}

	public void recycleBitmap(Bitmap bm) {
		mBaseUIUtils.recycleBitmap(bm);
	}
	
	
	public void showLoadDialog(){
		LoadDialog.show(this);
	}

	public void dismissLoadDialog(){
		LoadDialog.dismissDialog();
	}
	

	private BroadcastReceiver mBroadcastReceiver;
	
	private void registerBoradcastReceiver(){
		mBroadcastReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				finish();
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MSG_CLOSE_ACTIVITY);
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
	}
	
	private void unregisterBroadcastReceiver(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}
	
	/**
	 * 发送关闭activity的广播
	 */
	protected void sendCloseActivityBroadcast() {
		Intent intent = new Intent(BROADCAST_MSG_CLOSE_ACTIVITY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	/**
	 * 弹出软键盘
	 * @param editText
	 */
	protected void showSoftInput(final EditText editText) {
		editText.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) editText
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 200);
	}

    protected void collapseSoftInputMethod(final EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

	public static final int FLAG_CHOOSE_ALBUM = 0; 
	public static final int FLAG_CHOOSE_CAMERA = 1; 

	protected String localTempImageFileName, imageFilePath;
	
	protected void getPhotoFromAlbum(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, FLAG_CHOOSE_ALBUM);
	}
	
	protected void getPhotoFromCamera(){
		localTempImageFileName = "";
		localTempImageFileName = String.valueOf((new Date()).getTime()) + ".png";
		File filePath = new File(CommonUtils.getAppCacheImageDirPath());
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(filePath, localTempImageFileName);

		Uri u = Uri.fromFile(f);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
		startActivityForResult(intent, FLAG_CHOOSE_CAMERA);
	}

	protected void startScanSN(int deviceType) {
		Intent intent = new Intent(this, CommonScanActivity.class);
		intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, deviceType);

		startActivityForResult(intent, 0x199);
	}

	protected void showQuitConfirmDialog() {
		button2DialogBuilder("正在写入SN，请稍后...", "确认", "坚持退出", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ShowQuitConfirmAgainDialog();
			}
		}).show();
	}

	protected void ShowQuitConfirmAgainDialog() {
		button2DialogBuilder("现在退出可能会导致SN写入状态异常，建议再等等...", "坚持退出", "取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}


	protected void showRemindDialog() {

		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle("PETKIT质量提醒您")
				.setMessage("请确认您是否已对产品完成全面的检查，\n" +
						"确认外观没有脏污刮花，漏缺零件，间隙段差等不良。\n")
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton(R.string.back_to_check,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						}).show();

	}

}
