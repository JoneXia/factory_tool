package com.petkit.matetool.ui.base;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.orm.SugarApp;
import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.utils.CrashHandler;
import com.petkit.matetool.utils.CustomLog;

public class BaseApplication extends SugarApp {

	/**
	 * It is possible to keep a static reference across the application of the
	 * image loader.
	 */
	private static DisplayMetrics dm = new DisplayMetrics();
	public static int mNetWorkState;

	@Override
	public void onCreate() {
		super.onCreate();

		CommonUtils.init(getApplicationContext());
		CustomLog.init(this);
		mNetWorkState = CommonUtils.getAPNType();

        CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}



	public static DisplayMetrics getDisplayMetrics(Activity activity){
		if(dm == null || dm.widthPixels == 0){
			if(activity != null){
				activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			}
		}
		return dm;
	}
}
