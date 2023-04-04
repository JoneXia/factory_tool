package com.petkit.android.widget;


import com.petkit.android.utils.MResource;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


/**
 * 安全退出
 *
 */
public class LoadDialog extends Dialog{
	
	/** LoadDialog对象 **/
	private static LoadDialog loadDialog;
	/** 是否允许取消 **/
	private boolean canNotCancel;
	/** 不能取消提示语 **/
//	private String tipMsg;

	/**
	 * 构造方法
	 * @param ctx
	 * @param canNotCancel
	 * @param tipMsg
	 */
	public LoadDialog(final Context ctx, boolean canNotCancel, String tipMsg) {
		super(ctx);

		this.canNotCancel = canNotCancel;
		if(TextUtils.isEmpty(tipMsg)){
//			tipMsg = "该操作不能返回！";
		}
//		this.tipMsg = tipMsg;
		this.getContext().setTheme(android.R.style.Theme_InputMethod);
		Log.d("LoadDialog", "packageName: " + ctx.getPackageName());
		int resId = MResource.getResourceIdByName(ctx.getPackageName(), "layout", "layout_dialog_loading");
		Log.d("LoadDialog", "packageName resId: " + resId);
		setContentView(resId);

		int resId2 = MResource.getResourceIdByName(ctx.getPackageName(), "id", "loading_text");
		TextView loadingText = (TextView) findViewById(resId2);
		if(!TextUtils.isEmpty(tipMsg)){
			loadingText.setText(tipMsg);
		}
		
		Window window = getWindow();
		WindowManager.LayoutParams attributesParams = window.getAttributes();
		attributesParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		attributesParams.dimAmount = 0.5f;
		attributesParams.verticalMargin = 0.2f;

		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (canNotCancel) {
//				Toast.makeText(getContext(), tipMsg, Toast.LENGTH_SHORT).show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
     * 启动加载动画
     * @param context
     */
    public static void show(Context context) {
    	show(context, null, false);
    }

    /**
     * 启动加载动画
     * @param context
     * @param message 不能取消提示语
     */
    public static void show(Context context, String message) {
    	show(context, message, false);
    }
    
    /**
     * 启动加载动画
     * @param context 上下文
     * @param message 不能取消提示语
     * @param isCancel true表示不能取消，false表示可以取消
     */
    public static void show(Context context, String message, boolean isCancel) {
    	show(context, message, isCancel, null);
    }
    
    
    /**
     * 启动加载动画
     * @param context 上下文
     * @param message 不能取消提示语
     * @param isCancel true表示不能取消，false表示可以取消
     */
    public static void show(Context context, String message, boolean isCancel, OnCancelListener listener) {
    	if (context instanceof Activity) {
	        if(((Activity) context).isFinishing()) {
	            return;
	        }
		}
    	if (loadDialog != null && loadDialog.isShowing()) {
            return;
        }
    	loadDialog = new LoadDialog(context, isCancel, message);
    	if(listener != null){
    		loadDialog.setOnCancelListener(listener);
    	}
    	loadDialog.show();
    }
    
    /**
     * 取消加载动画
     */
    public static void dismissDialog(){
    	if(loadDialog != null && loadDialog.isShowing() && !loadDialog.getContext().isRestricted()){
    		loadDialog.dismiss();
    		loadDialog = null;
    	}
    }
}
