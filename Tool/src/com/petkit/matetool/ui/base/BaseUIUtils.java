package com.petkit.matetool.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.petkit.android.utils.CommonUtils;

import java.util.Iterator;
import java.util.Map;

import androidx.fragment.app.Fragment;

public class BaseUIUtils<T> {

	public final static int CAMERA_PHOTO = 1001;
	public final static int GALLERY_PHOTO = 1002;
	public final static int CROP_PHOTO = 1003;
	public final static int ZXING_REQUESTCODE = 2001;

	public final static int IMAGESIZE_BIG = 256;
	public final static int IMAGESIZE_SMALL = 120;
	
	private Activity mActivity;
	
	public BaseUIUtils(T activity) {
		super();
		
		if(activity instanceof Fragment){
			mActivity = ((Fragment) activity).getActivity();
		}else{
			mActivity = (Activity) activity;
		}
	}

	public Drawable getDrawable(int resid) {
		return mActivity.getResources().getDrawable(resid);
	}

	public String[] getStringArray(int resid) {
		return mActivity.getResources().getStringArray(resid);
	}

	public void showLongToast(String msg) {
		showToast(msg, 0, Toast.LENGTH_LONG);
	}
	
	public void showLongToast(String msg, int iconResId) {
		showToast(msg, iconResId, Toast.LENGTH_LONG);
	}

	public void showLongToast(int stringId) {
		showToast(mActivity.getString(stringId), 0, Toast.LENGTH_LONG);
	}
	
	public void showLongToast(int stringId, int iconResId) {
		showToast(mActivity.getString(stringId), iconResId, Toast.LENGTH_LONG);
	}

	public void showShortToast(String msg) {
		showToast(msg, 0, Toast.LENGTH_SHORT);
	}
	
	public void showShortToast(String msg, int iconResId) {
		showToast(msg, iconResId, Toast.LENGTH_SHORT);
	}

	public void showShortToast(int stringId) {
		showToast(mActivity.getString(stringId), 0, Toast.LENGTH_SHORT);
	}

	public void showShortToast(int stringId, int iconResId) {
		showToast(mActivity.getString(stringId), iconResId, Toast.LENGTH_SHORT);
	}
	
	private void showToast(String text, int iconResId, int type) {
		Toast toast = Toast.makeText(mActivity, text, type);
		toast.setGravity(Gravity.CENTER, 0, 0);
		if(iconResId > 0){
			LinearLayout toastView = (LinearLayout) toast.getView();
			ImageView toastImageView = new ImageView(mActivity);
			toastImageView.setImageResource(iconResId);
			toastView.addView(toastImageView, 0);
		}
		toast.show();
	}

	public int dip2px(Context context, float dipValue){
		final float scale=context.getResources().getDisplayMetrics().density;
		return (int)(dipValue*scale+0.5f);
	}

	public int px2dip(Context context,float pxValue){
		final float scale=context.getResources().getDisplayMetrics().density;
		return (int)(pxValue/scale+0.5f);
	}

	public int dip2px(float dip) {
		return dip2px(mActivity, dip);
	}

	public int px2dip(float px) {
		return px2dip(mActivity, px);
	}

	public void startActivity(Class<?> cls, boolean finish) {
		Intent it = new Intent(mActivity, cls);
		mActivity.startActivity(it);
		if (finish)
			mActivity.finish();
	}

	public void startActivityForResult(Class<?> cls, int requestCode) {
		Intent it = new Intent(mActivity, cls);
		mActivity.startActivityForResult(it, requestCode);
	}

	public void startActivityWithData(Class<?> cls, Map<String, String> map,
			boolean finish) {
		Intent it = new Intent(mActivity, cls);
		Bundle bundle = new Bundle();
		String key, value;
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			key = (String) iterator.next();
			value = map.get(key);
			bundle.putString(key, value);
		}
		it.putExtras(bundle);
		mActivity.startActivity(it);
		if (finish)
			mActivity.finish();
	}
	
	public void startActivityWithData(Class<?> cls, Bundle bundle, boolean finish) {
		Intent it = new Intent(mActivity, cls);
		it.putExtras(bundle);
		mActivity.startActivity(it);
		if (finish)
			mActivity.finish();
	}

	public void changeEditTextInputType(EditText editText, boolean visiable) {
		if (editText == null)
			return;
		if (visiable) {
			editText.setTransformationMethod(HideReturnsTransformationMethod
					.getInstance());// ʹ����ɼ�
		} else {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());// ʹ���벻�ɼ�
		}
		editText.setSelection(editText.getText().toString().length());
	}


	public boolean isEmpty(String str) {
		if (str == null || str.equals(""))
			return true;
		return false;
	}


	public AlertDialog.Builder buttonDialogBuilder(String msg,
			String positivetext, DialogInterface.OnClickListener listener) {
		return new AlertDialog.Builder(mActivity).setMessage(msg).setPositiveButton(
				positivetext, listener);
	}

	public AlertDialog.Builder button2DialogBuilder(String msg,
			String positivetext, String negtext,
			DialogInterface.OnClickListener poslistener,
			DialogInterface.OnClickListener neglistener) {
		AlertDialog.Builder builder = buttonDialogBuilder(msg, positivetext,
				poslistener);
		builder.setNegativeButton(negtext, neglistener);
		return builder;
	}

	public AlertDialog.Builder buttonTitleDialogBuilder(String title,
			String msg, String positivetext,
			DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = buttonDialogBuilder(msg, positivetext,
				listener);
		builder.setTitle(title);
		return builder;
	}

	public AlertDialog.Builder button2TitleDialogBuilder(String title,
			String msg, String positivetext, String negtext,
			DialogInterface.OnClickListener poslistener,
			DialogInterface.OnClickListener neglistener) {
		AlertDialog.Builder builder = button2DialogBuilder(msg, positivetext,
				negtext, poslistener, neglistener);
		builder.setTitle(title);
		return builder;
	}

	public String getAppCacheDirPath() {
		return CommonUtils.getAppCacheDirPath();
	}

	public void recycleBitmap(Bitmap bm) {
		if (bm == null)
			return;
		if (!bm.isRecycled()) {
			bm.recycle();
			bm = null;
		}
	}
}
