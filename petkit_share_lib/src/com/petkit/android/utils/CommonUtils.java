package com.petkit.android.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.model.Address;
import com.petkit.android.model.Client;
import com.petkit.android.model.PetSyncTime;
import com.petkit.android.model.SupportBLEDevices.SupportBLEDevicesData;


/**
 * 工具类，app启动时需要调用init方法实现初始化，保存ApplicationContext，创建缓存路径
 * 
 * @author Jone
 *
 */
@SuppressLint({ "SimpleDateFormat", "DefaultLocale" }) 
public class CommonUtils {
	
	public static String CACHE_ROOT;
	public static String PETKIT_ROOT;
	public static final String CACHE_ACTIVITY_PATH = "activities/";
	public static final String CACHE_HTTP_DATA_PATH = "data/";
	public static final String CACHE_IMAGE_PATH = "images/";
	public static final String CACHE_INFOR_COLLECT_PATH = "infors/";
	public static final String PETKIT_VIDEO_PATH = "video/";

	private static final boolean isDebug = false;
	
	public static String cookies;
	public static CookieStore cookieStore;
	
	private static Context mContext;
	private static boolean isAppActive = false;
	
	public static void init(Context context){
		mContext = context;
		isAppActive = true;
		
		PetkitToast.init(context);
		createAppCacheDir();
	}
	
	
	/**
	 * 系统安全退出
	 */
	public static void exit(Activity ct){
		//清除通知
		android.os.Process.killProcess(android.os.Process.myPid());
		ct.onBackPressed();
	}
	
	public static Context getAppContext(){
		return mContext;
	}
	
	/**
	 * 是否为空
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		if (value == null || value.length() == 0) {
			return true;
		}
		return false;
	}
	
	private static void createAppCacheDir() {
		String caString = CommonUtils.getSdCardDir(mContext);
		if (caString == null) {
			caString = mContext.getFilesDir().getAbsolutePath()
					.toString();
		}
		
		String dir; 
		
		if(mContext.getPackageName().contains("mate")){
			dir = "/MATE/";
		}else if(mContext.getPackageName().contains("abroad")){
			dir = "/ABROAD/";
		}else{
			dir = "/PETKIT/";
		}
		
		String cache = caString + dir;//mContext.getString(MResource.getResourceIdByName(mContext.getPackageName(), "string", "app_name")); /*mContext.getPackageName()  "/PETKIT_MATE/"*/;

		CACHE_ROOT = cache + "cache/";
		PETKIT_ROOT = cache;
		File file = new File(cache);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		
		file = new File(CACHE_ROOT);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		
		file = new File(PETKIT_ROOT + PETKIT_VIDEO_PATH);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(CACHE_ROOT + CACHE_ACTIVITY_PATH);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(CACHE_ROOT + CACHE_HTTP_DATA_PATH);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		
		file = new File(CACHE_ROOT + CACHE_IMAGE_PATH);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		
		file = new File(CACHE_ROOT + CACHE_INFOR_COLLECT_PATH);
		if (!file.isDirectory()) {
			file.delete();
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		
	}

	/**
	 * 获取缓存目录
	 * @return
	 */
	public static String getAppCacheDirPath() {
		File file = new File(CACHE_ROOT);
		if(!file.exists())
			file.mkdirs();
		return CACHE_ROOT;
	}

	/**
	 * 获取数据目录
	 * @return
	 */
	public static String getAppDirPath() {
		File file = new File(PETKIT_ROOT);
		if(!file.exists())
			file.mkdirs();
		return PETKIT_ROOT;
	}


	/**
	 * 获取活动数据缓存目录
	 * @return
	 */
	public static String getAppVideoDataDirPath() {
		File file = new File(getAppDirPath() + PETKIT_VIDEO_PATH);
		if(!file.exists())
			file.mkdirs();
		return getAppDirPath() + PETKIT_VIDEO_PATH;
	}
	
	/**
	 * 获取活动数据缓存目录
	 * @return
	 */
	public static String getAppCacheActivityDataDirPath() {
		File file = new File(getAppCacheDirPath() + CACHE_ACTIVITY_PATH);
		if(!file.exists())
			file.mkdirs();
		return getAppCacheDirPath() + CACHE_ACTIVITY_PATH;
	}
	
	/**
	 * 获取联网数据缓存目录 CACHE_HTTP_DATA_PATH
	 * @return
	 */
	public static String getAppCacheHttpDataDirPath(){
		File file = new File(getAppCacheDirPath() + CACHE_HTTP_DATA_PATH);
		if(!file.exists())
			file.mkdirs();
		return getAppCacheDirPath() + CACHE_HTTP_DATA_PATH;
	}
	
	/**
	 * 获取图片缓存目录 CACHE_IMAGE_PATH
	 * @return
	 */
	public static String getAppCacheImageDirPath(){
		File file = new File(getAppCacheDirPath() + CACHE_IMAGE_PATH);
		if(!file.exists())
			file.mkdirs();
		return getAppCacheDirPath() + CACHE_IMAGE_PATH;
	}
	
	/**
	 * 获取信息采集缓存目录 CACHE_INFOR_COLLECT_PATH
	 * @return
	 */
	public static String getAppCacheInforCollectDirPath(){
		File file = new File(getAppCacheDirPath() + CACHE_INFOR_COLLECT_PATH);
		if(!file.exists())
			file.mkdirs();
		return getAppCacheDirPath() + CACHE_INFOR_COLLECT_PATH;
	}
	
    /**
     * 获取根据屏幕获取实际大小
     * 在自定义控件中，根据屏幕的大小来获取实际的大小
     * 
     * 例如：orgSize为10，
     * 
     * 如果在density为160的屏幕，返回值为10
     * 如果在屏幕为240的屏幕上返回15
     * 如果在屏幕为320的屏幕上返回20
     * 
     * @param ctx
     * @param orgSize dip
     * @return 返回相应的px
     */
    public static int getActualSize(Context ctx, int orgSize) {
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        float density = (float) displayMetrics.density;
        return (int)(orgSize * density);
    }
    
	
	/**
	 * 得到系统级别的缓存对象
	 * @param context
	 * @return
	 */
	public static SharedPreferences getSysShare(Context context){
		if(context!=null)
			return context.getSharedPreferences(Consts.APP_CACHE_PATH, Context.MODE_PRIVATE);
		return null;
	}
	
	
	/**
	 * 添加系统缓存信息
	 * 
	 * @param key
	 * @param value
	 */
	public static void addSysMap(String key , String value){
		Editor sysEdit =  getSysShare(mContext).edit();
		sysEdit.putString(key, value);
		sysEdit.commit();
	}
	
	
	/**
	 * 添加系统缓存信息
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void addSysMap(Context context , String key , String value){
		Editor sysEdit =  getSysShare(context).edit();
		sysEdit.putString(key, value);
		sysEdit.commit();
	}
	
	/**
	 * 得到系统缓存
	 * @param key
	 * @return
	 */
	public static String getSysMap(String key){
		return getSysMap(mContext, key, "");
	}
	
	
	/**
	 * 得到系统缓存
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getSysMap(Context context , String key){
		return getSysMap(context, key, "");
	}
	
	public static void addSysIntMap(Context context , String key , int value){
		Editor sysEdit =  getSysShare(context).edit();
		sysEdit.putInt(key, value);
		sysEdit.commit();
	}
	
	public static int getSysIntMap(Context context , String key){
		return getSysIntMap(context, key, 0);
	}
	
	public static int getSysIntMap(Context context , String key, int defaultValue){
		SharedPreferences share = getSysShare(context);
		if(share != null){
			return share.getInt(key, defaultValue);
		}
		return 0;
	}
	/**
	 * 得到系统缓存
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static String getSysMap(Context context , String key, String defValue){
		SharedPreferences share = getSysShare(context);
		if(share != null){
			return share.getString(key, defValue);
		}
		return defValue;
	}
	
	/**
	 * 退出的时候清除通知
	 */
	public static void clearNotify(Context mContext){
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
	
	
	/**
	 * 返回当前程序版本名
	 */
	public static int getAppVersionCode(Context context) {
		int versioncode = 0;
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versioncode = pi.versionCode;
			
		} catch (Exception e) {
			PetkitLog.e("VersionInfo", "Exception");
		}
		return versioncode;
	}
	
	/**
	 * 返回当前程序版本号
	 */
	public static String getAppVersionName(Context context) {
		String versionName = null;
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			
		} catch (Exception e) {
			PetkitLog.e("VersionInfo", "Exception");
		}
		return versionName;
	}
	
	/**
	 * 获取GridView的高度
	 * @param gridView
	 * @param numColumns
	 * @return
	 */
	public static int getGridViewHeight(GridView gridView, int numColumns, int space) {  
        ListAdapter listAdapter = gridView.getAdapter();   
        if (listAdapter == null) {  
            return 0;  
        }  
        
        int totalHeight = 0;  
        for (int i = 0; i < listAdapter.getCount(); i+=numColumns) {  
            View listItem = listAdapter.getView(i, null, gridView);
            if (listItem != null) {
                listItem.measure(0, 0);
                totalHeight += (listItem.getMeasuredHeight() + space);
            }
        }  
        
        return totalHeight;
    }
	
	/**
     * 手机号码校验
     * @param phoneNumber
     * @return
     */
    public static boolean checkPhoneNumber(String phoneNumber){
    	return phoneNumber.matches("^((\\+86)|(86))?(13|14|15|18)\\d{9}$");
    }
    
    /**
	 * 
	 * @param emailStr 
	 * @return 判断email格式是否正确
	 */
	public static boolean checkEmail(String emailStr) {
		boolean ret = false;
		if (!isEmpty(emailStr)) {
			ret = emailStr.contains("@")&&emailStr.contains(".");
		}

		return ret;
	}

    /**  
     * 计算日期与当前日期相差的天数  
     * @param smdate 较大的时间 
     * @return 相差天数 
     * @throws ParseException  
     */ 
    @SuppressLint("SimpleDateFormat")
	public static int daysBetweenFutureDay(String smdate) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();  
        try {
        	cal.setTime(sdf.parse(sdf.format(cal.getTime())));
	        long time1 = cal.getTimeInMillis();               
	        cal.setTime(sdf.parse(smdate));  
	        long time2 = cal.getTimeInMillis();       
	        long between_days=(time2-time1)/(1000*3600*24);
	        return Integer.parseInt(String.valueOf(between_days)); 
		} catch (ParseException e) {
			e.printStackTrace();
		}  
        return -1;
    }

	public static boolean isSD() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取sd卡路径
	 * @param context
	 * @return
	 */
    public static String getSdCardDir(Context context) {
		if (!isSD()) {
			return null;
		}
		String sdDir = Environment.getExternalStorageDirectory()
				.getAbsolutePath().toString();
		PetkitLog.d("getSdCardDir", "sd dir path = " + sdDir);
		return sdDir;
	}
    
    
    /**
     * @return true if the app is debuggable, false otherwise
     */
    public static boolean isDebuggable(Context context) {
        if (context == null) {
            return  false;
        }
        
        if(isDebug){
        	return true;
        }

        ApplicationInfo appInfo = context.getApplicationInfo();
        if (appInfo != null) {
            return ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        } else {
            return (ApplicationInfo.FLAG_DEBUGGABLE != 0);
        }
    }
    
    /**
     * 获取客户端信息
     * @return
     */
    public static String getClientInfor(String appId){
		Client client = new Client();
		client.setPlatform("android");
		client.setName(Build.MODEL);
		client.setOsVersion(Build.VERSION.RELEASE);
		client.setVersion(getAppVersionName(mContext));
		client.setSource("app."+appId);
		
		Locale locale = mContext.getResources().getConfiguration().locale;
		client.setLocale(locale.toString());
		
		TimeZone timeZone = TimeZone.getDefault();
		long offset = timeZone.getRawOffset();
		client.setTimezone((offset) / (3600 * 1000f));
		
		Gson gson = new Gson();
		return gson.toJson(client);
    }
    

    /**
     * 获取offset天数以前的日期，格式是yyyyMMdd
     * @param offset
     * @return
     */
	public static String getDateStringByOffset(int offset){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long cur = new java.util.Date().getTime();
		long offsetT = ((long)offset) * 24*60*60*1000;
		long ts = cur - offsetT;
		String date = sdf.format(ts);
		
		return date;
	}
	
	/**
     * 获取offset天数以前的日期，格式是MM-dd
     * @param offset
     * @return
     */
	public static String getDateStringByOffset2(int offset,Date curDate){
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		long cur = curDate.getTime();
		long offsetT = ((long)offset) * 24*60*60*1000;
		long ts = cur - offsetT;
		String date = sdf.format(ts);
		
		return date;
	}
	
	/**
     * 获取offset月数以前的日期，格式是yyyy-MM
     * @param offset
     * @return
     */
	public static String getDateStringByOffsetMouth(int offset){
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - offset);
		
		return sdf.format(cal.getTime());
	}
	
	/**
	 *  获取offset天数以前的日期，格式是MM-dd
	 * @param baseDay
	 * @param offset
	 * @return
	 */
	public static String getDateStringByOffset2(int baseDay, int offset){
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd");
		long cur;
		try {
			cur = sdf1.parse(String.valueOf(baseDay)).getTime();
			long offsetT = ((long)offset) * 24*60*60*1000;
			long ts = cur - offsetT;
			
			return sdf2.format(ts);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	/**  
     * 计算两个日期之间相差的天数  
     * @param day1 较大的时间 
     * @param day2  较小的时间 
     * @return 相差天数 
     */    
	public static int daysBetween(int day1, int day2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			Date smdate = sdf.parse(String.valueOf(day1));
			Date bdate = sdf.parse(String.valueOf(day2));
			Calendar cal = Calendar.getInstance();
			cal.setTime(smdate);
			long time1 = cal.getTimeInMillis();
			cal.setTime(bdate);
			long time2 = cal.getTimeInMillis();
			long between_days = (time1 - time2) / (1000 * 3600 * 24);
			return Integer.parseInt(String.valueOf(between_days));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 获取相对某天的偏差值
	 * @param day 格式yyyyMMdd
	 * @param offset
	 * @return
	 */
	public static int getDayAfterOffset(int day, int offset){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date;
		try {
			date = sdf.parse(String.valueOf(day));
			long dayTime = date.getTime();
			long offsetT = ((long)offset) * 24*60*60*1000;
			
			return Integer.valueOf(sdf.format(dayTime + offsetT));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 获取两个日期间隔的毫秒数
	 * @param time1		yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 * @param time2		yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 * @return
	 */
	public static long getDaysBetweenMilis(String time1, String time2){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		try {
			Date date1 = sdf.parse(time1);
			Date date2 = sdf.parse(time2);
			
			return date1.getTime() - date2.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * 判断狗狗活动数据的时间是否在狗狗创建以后
	 * @param createdTime	"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	 * @param curDay			"yyyyMMdd"
	 * @param offset		0点开始的偏移量
	 * @param frequence		偏移频率
	 * @return
	 */
	public static boolean isActivityDataValid(String createdTime, String curDay, int offset, int frequence){
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
		if(isEmpty(createdTime)){
			return true;
		}
		try {
			Date date1 = DateUtil.parseISO8601Date(createdTime);
			Date date2 = sdf2.parse(curDay);
			
			return date1.getTime() < (date2.getTime() + offset * frequence * 1000);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 
	 * 获取显示时用到的时间格式，如刚刚、几分钟前等
	 * 
	 * @param context
	 * @param petId
	 * @return
	 */
	public static String getDogLastSyncTimeString(Context context, String petId){
		long lastSyncTime = getDogLastSyncTime(petId);
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		try {
			if(lastSyncTime > 0){
				lastSyncTime += format.parse(BLEConsts.BASE_TIMELINE).getTime();
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date date = new Date();
				date.setTime(lastSyncTime);
				return getDisplayTimeFromDate(context, DateUtil.formatISO8601Date(date));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return "-";
	}
	
	/**
	 * 获取显示时用到的时间格式，如刚刚、几分钟前等
     * @param context
	 * @param date 格式是yyyy-MM-dd'T'HH:mm:ss'Z'
	 * @return
	 */
	public static String getDisplayTimeFromDate(Context context, Date date){
		return getDisplayTimeFromDate(context, DateUtil.formatISO8601Date(date));
	}
	
	
	
	/**
	 * 获取显示时用到的时间格式，如刚刚、几分钟前等
	 * @param dateString 格式是yyyy-MM-dd'T'HH:mm:ss'Z'
	 * @return
	 */
	public static String getDisplayTimeFromDate(Context context, String dateString){
		
		if(isEmpty(dateString) || dateString.length() < 20){
			return "";
		}
		
		SimpleDateFormat sdf;
		if(dateString.length() <= 20){
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}else{
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		}
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date1;
		try {
			date1 = sdf.parse(dateString);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date1);
			long time1 = cal.getTimeInMillis();
			long time2 = new java.util.Date().getTime();
			long between_time = (time2 - time1) / (1000 * 60);
			String between_time_string;
			if(between_time < 1){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_time_just_now");
				return context.getString(resId);
			}
			if(between_time < 60){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", between_time > 1 ? "Unit_minutes" : "Unit_minute");
				between_time_string = between_time + context.getString(resId);
				int resId2 = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_time_ago");
				return context.getString(resId2, between_time_string);
			}
			between_time = between_time / 60;
			if(between_time < 24){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", between_time > 1 ? "Unit_hours" : "Unit_hour");
				between_time_string = between_time + context.getString(resId);
				int resId2 = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_time_ago");
				return context.getString(resId2, between_time_string);
			}
			between_time = between_time / 24;
			if(between_time < 7){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", between_time > 1 ? "Unit_days" : "Unit_day");
				between_time_string = between_time + context.getString(resId);
				int resId2 = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_time_ago");
				return context.getString(resId2, between_time_string);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return DateUtil.getFormatDate2FromString(dateString);
	}
	
	
	/**
	 * 获取基于TimeZone的时间，格式是yyyy-MM-dd'T'HH:mm:ss'Z'，基于2000-01-01 00:00:00时间
	 * @param time 
	 * @return
	 */
	public static String getTimestampByTime(long time){
		
		long milliseconds = time;

		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		try {
			milliseconds += format.parse(BLEConsts.BASE_TIMELINE).getTime();
			
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(milliseconds);
			
			return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03dZ", cal.get(Calendar.YEAR), 
					cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), 
					cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	/**
	 * 获取系统当前时间
	 * @return 格式是yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 */
	public static String getCurrentTimestamp(){
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03dZ", cal.get(Calendar.YEAR), 
				cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), 
				cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
	}
	
	/**
	 * 时间格式转换
	 * @param time yyyy-MM-dd
	 * @return yyyyMMdd
	 */
	public static int getConvertTimeFormat(String time){
		
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		java.text.SimpleDateFormat format2 = new java.text.SimpleDateFormat(
				"yyyyMMdd");

		try {
			return Integer.valueOf(format2.format(format.parse(time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * 获取显示时用到的时间格式，如几分几秒等
     * @param context
	 * @param seconds
	 * @return
	 */
	

	public static String getLeftTime(Context context, int seconds) {
		int mins = seconds / 60;
		int second = seconds % 60;
		
		if(mins == 0) {
			if(second == 0) {
				return null;
			} else {
				return (second + context.getString(MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_second")));
			}
		} else {
			if(second == 0) {
				return (mins + context.getString(MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_minute")));
			} else {
				int resId1 = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_minute");
				int resId2 = MResource.getResourceIdByName(context.getPackageName(), "string", "Unit_second");
				return(mins + context.getString(resId1) + second + context.getString(resId2));
			}
		}
	}
	
	/**
	 * 获取显示时用到的时间格式，如刚刚、几分钟前等
	 * @param date1String 格式是yyyy-MM-dd'T'HH:mm:ss'Z'
     * @param date2String 格式是yyyy-MM-dd'T'HH:mm:ss'Z'
	 * @return
	 */
	public static long getDateBetweenMilis(String date1String, String date2String){
		
		if(isEmpty(date1String) || date1String.length() < 20){
			return -1;
		}
		
		if(isEmpty(date2String) || date2String.length() < 20){
			return -1;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date1, date2;
		try {
			date1 = sdf.parse(date1String);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date1);
			long time1 = cal.getTimeInMillis();
			
			date2 = sdf.parse(date2String);
			cal.setTime(date2);
			long time2 = cal.getTimeInMillis();
			
			return time1 - time2;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * 
	 * @param time	
	 * @return 	
	 */
	public static String getChatTimeFromString(String time){
		if(isEmpty(time)){
			return null;
		}
		java.text.SimpleDateFormat format2 = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date;
		try {
			date = DateUtil.parseISO8601Date(time);
			Date curDate = new Date();
			switch (compareTwoDateState(date, curDate)) {
			case 1:
				return DateUtil.getFormatDate6FromString(time);
			case 2:
			case 3:
				return DateUtil.getFormatDate5FromString(time);
			case 4:
				return DateUtil.getFormatDateFromString(time);
			default:
				break;
			}
			return format2.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}
	
	/**
	 * 判断两个日期是否是同一天、同一月、同一年
	 * @param dateA
	 * @param dateB
	 * @return  1： 同一天；2：同一月；3：同一年；4：其他
	 */
	public static int compareTwoDateState(Date dateA,Date dateB) {
	    Calendar calDateA = Calendar.getInstance();
	    calDateA.setTime(dateA);

	    Calendar calDateB = Calendar.getInstance();
	    calDateB.setTime(dateB);

	    if(calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)){
	    	if(calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)){
	    		if(calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH)){
	    			return 1;
	    		}
	    		return 2;
	    	}
	    	return 3;
	    }
	    return 4;
	}
	
	
	/**
	 * float取一位小数
	 * @param value
	 * @return
	 */
	public static String formatStringValueFromFloat(float value){
		String result;
		if(value == 0){
			result = "-";
		}else{
			result = String.format("%.1f", value);
		}
		
		return result;
	}
	
	
	/**
	 * 获取color
	 * @param cId
	 * @return
	 */
	public static int getColorById(int cId) {
    	return mContext.getResources().getColor(cId);
    }
	
	
	/**
	 * 获取当前联网类型
	 * @return
	 */
	public static int getAPNType() {

		int netType = Consts.NETWORN_NONE;
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			PetkitLog.d("networkInfo.getExtraInfo()",
					"networkInfo.getExtraInfo() is "
							+ networkInfo.getExtraInfo());
			netType = Consts.NETWORN_MOBILE;
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = Consts.NETWORN_WIFI;
		}

		return netType;

	} 
	
	
	/**
	 * 获取地址的描述
	 * @param address
	 * @return
	 */
	public static String getAddressInformation(Address address){
		StringBuilder locBuilder = new StringBuilder();
		if(address != null){
			if(!isEmpty(address.getCity()) && !isEmpty(address.getProvince()) 
					&& address.getCity().equals(address.getProvince())){
				locBuilder.append(address.getCity());
			}else{
				if(!isEmpty(address.getCountry())){
					locBuilder.append(address.getCountry());
				}
				if(!isEmpty(address.getProvince())){
					locBuilder.append(address.getProvince());
				}
				if(!isEmpty(address.getCity())){
					locBuilder.append(address.getCity());
				}
			}
			
			if(!isEmpty(address.getDistrict())){
				locBuilder.append(address.getDistrict());
			}
		}else{
			locBuilder.append(" ");
		}
		
		return locBuilder.toString();
	}
	
	/**
	 * 设置App是否在前台
	 * @param state
	 */
	public static void setAppActiveState(boolean state){
		isAppActive = state;
	}
	
	
	public static boolean isRunningForeground() {
//		if(mContext == null){
//			return false;
//		}
//		
//		ActivityManager am = (ActivityManager) mContext
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		if(am.getRunningTasks(1).size() > 0){
//			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//			String currentPackageName = cn.getPackageName();
//			if (!TextUtils.isEmpty(currentPackageName)
//					&& currentPackageName.equals(mContext.getPackageName())) {
//				return true;
//			}
//		}
		return isTopActivity();
	}
	
	/**
	 * 判断App是否在前台
	 * @return
	 */
	public static boolean isTopActivity(){
		if(!isAppActive || mContext == null){
			return false;
		}

//        PackageManager packageManager = mContext.getPackageManager();
//        // getPackageName()是你当前类的包名，0代表是获取版本信息
//        PackageInfo packInfo;
//		try {
//			packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
//		} catch (NameNotFoundException e) {
//			return false;
//		}
//        String packageName = packInfo.packageName;
//        
//        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningTaskInfo>  tasksInfo = activityManager.getRunningTasks(1);  
//        if(tasksInfo.size() > 0){  
//            //应用程序位于堆栈的顶层  
//            if(packageName.equals(tasksInfo.get(0).topActivity.getPackageName())){  
//                return true;  
//            }  
//        }  
//        return false;
        
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if(appProcesses == null){
            return false;
        }
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(mContext.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				} else {
					return false;
				}
			}
		}
        return false;
    }
	
	
	public static int getAndroidSDKVersion() {
		int version = 0;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		} catch (NumberFormatException e) {
		}
		return version;
	}
	
	
	/**
	 * 根据设备型号判断是否是三星设备（包含所有三星 4.2、蓝牙4.0的设备）
	 * @param name
	 * @return
	 */
	public static boolean isSamsungDevice(String name){
		boolean result = false;
		String deviceString = getSysMap(mContext, Consts.SHARED_ANDROID_DEVICE_LIST);
		if(isEmpty(deviceString)){
//			samsungList = "I9500,I959,I9508,I9505,I9502,I545,N7108,I9158,I9152,P709,I9150";
//			addSysMap(BaseApplication.getAppContext(), Consts.SHARED_ANDROID_DEVICE_LIST, samsungList);
			deviceString = "[{\"brand\":\"samsung\",\"devices\":[\"I9500\",\"I959\",\"I9508\",\"I9505\",\"I9502\",\"I545\",\"N7108\",\"I9158\",\"I9152\",\"P709\",\"I9150\",\"I9200\",\"I9208\",\"P729\",\"I9205\",\"I9190\",\"I9195\",\"I9192\",\"I9198\",\"C101\",\"I9118\",\"G3818\",\"I9168\",\"G3858\",\"I9082\",\"E330\",\"I8580\",\"I9060\",\"I9295\",\"N7100\"],\"version\":\"4.2\"}]";
			addSysMap(mContext, Consts.SHARED_ANDROID_DEVICE_LIST, deviceString);
		}
		Gson gson = new Gson();
		List<SupportBLEDevicesData> devices = gson.fromJson(deviceString, new TypeToken<List<SupportBLEDevicesData>>(){}.getType());
		
		if(devices == null || devices.size() == 0){
			return result;
		}
		SupportBLEDevicesData samsungDevices = null;
		for(SupportBLEDevicesData device : devices){
			if(device.getBrand().equalsIgnoreCase("samsung")){
				samsungDevices = device;
				break;
			}
		}
		if(samsungDevices == null){
			return result;
		}
		String lowerName = name.toLowerCase();
		for(String deviceName : samsungDevices.getDevices()){
			String lowerDeviceName = deviceName.toLowerCase();
			if(lowerName.equals(lowerDeviceName) || lowerName.contains(lowerDeviceName)){
				result = true;
				break;
			}
		}
		return result;
	}


	/**
	 * 根据出生日期取年龄，返回格式为：X岁X个月
     * @param context
	 * @param dateString 格式为 yyyy-MM-dd
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getAgeByBirthday(Context context, String dateString){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date, curDate = new Date();
		try {
			date = sdf.parse(dateString);
			int year, month;
			month = curDate.getMonth() - date.getMonth();
			year = curDate.getYear() - date.getYear();
			if(month < 0){
				year--;
				month += 12;
			}
			
			if(year < 0){
				return "";
			}
			StringBuilder sBuilder = new StringBuilder();
			if(year > 0 ){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", year > 1 ? "Unit_ages" : "Unit_age");
				sBuilder.append(year).append(context.getString(resId));
			}
			if(month > 0){
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", year > 1 ? "Unit_months" : "Unit_month");
				sBuilder.append(month).append(context.getString(resId));
			}
			
			if(year == 0 && month == 0){
				int day = (int) ((curDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
				if(day < 0){
					return "";
				}
				int resId = MResource.getResourceIdByName(context.getPackageName(), "string", day > 1 ? "Unit_days" : "Unit_day");
				sBuilder.append(day).append(context.getString(resId));
			}
			
			return sBuilder.toString();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public static String getConnectionStateDesc(int state){
		String stateString = null;
		switch (state) {
		case Consts.DEVICE_CONNECT_STATE_NONE:
			stateString = "NONE";
			break;
		case Consts.DEVICE_CONNECT_STATE_COMPELTE:
			stateString = "COMPELTE";
			break;
		case Consts.DEVICE_CONNECT_STATE_CONNECT_FAIL:
			stateString = "CONNECT_FAIL";
			break;
		case Consts.DEVICE_CONNECT_STATE_CONNECT_SUCCESS:
			stateString = "CONNECT_SUCCESS";
			break;
		case Consts.DEVICE_CONNECT_STATE_CONNECTING:
			stateString = "CONNECTING";
			break;
		case Consts.DEVICE_CONNECT_STATE_SCAN_FAIL:
			stateString = "SCAN_FAIL";
			break;
		case Consts.DEVICE_CONNECT_STATE_SCAN_TIMEOUT:
			stateString = "TIMEOUT";
			break;
		case Consts.DEVICE_CONNECT_STATE_SCANING:
			stateString = "SCANING";
			break;
		case Consts.DEVICE_CONNECT_STATE_SYNC:
			stateString = "SYNC";
			break;
		case Consts.DEVICE_CONNECT_STATE_SYNCTIMEOUT:
			stateString = "SYNCTIMEOUT";
			break;
		case Consts.DEVICE_CONNECT_STATE_UPLOAD_DATA:
			stateString = "UPLOAD_DATA";
			break;
		case Consts.DEVICE_CONNECT_STATE_UPLOAD_FAILED:
			stateString = "UPLOAD_FAILED";
			break;
		case Consts.DEVICE_CONNECT_STATE_DOWNLOAD_FAILED:
			stateString = "DOWNLOAD_FAILED";
			break;
		case Consts.DEVICE_CONNECT_STATE_DOWNLOAD_COMPLETE:
			stateString = "DOWNLOAD_COMPLETE";
			break;

		default:
			break;
		}
		
		return stateString;
	}
	
	
	/**
	 * 保存狗狗的同步时间。
	 * @param petId
	 * @param time
	 */
	public static void saveDogSyncTime(String petId, long time){
		
		Gson gson = new Gson();
		List<PetSyncTime> list = getSyncTimeList();
		if(list == null){
			list = new ArrayList<PetSyncTime>();
		}
		
		boolean flag = false;
		for(PetSyncTime dogSyncTime : list){
			if(petId != null && dogSyncTime.getDogId().equals(petId)){
				dogSyncTime.setTime(time);
				flag = true;
				break;
			}
		}
		if(!flag){
			PetSyncTime dogSyncTime = new PetSyncTime();
			dogSyncTime.setDogId(petId);
			dogSyncTime.setTime(time);
			list.add(dogSyncTime);
		}
		
		CommonUtils.addSysMap(mContext, Consts.SHARED_LAST_SYNC_TIME, gson.toJson(list));
	}
	
	/**
	 * 狗狗最后一次的同步时间
	 * @param petId
	 * @return
	 */
	public static long getDogLastSyncTime(String petId){
		List<PetSyncTime> list = getSyncTimeList();
		if(list != null && petId != null){
			for(PetSyncTime dogSyncTime : list){
				if(dogSyncTime.getDogId().equals(petId)){
					return dogSyncTime.getTime();
				}
			}
		}
		return 0;
	}
	
	/**
	 * 获取狗狗最后同步时间的列表
	 * @return
	 */
	public static List<PetSyncTime> getSyncTimeList(){
		String lastSyncTimeString = CommonUtils.getSysMap(mContext, Consts.SHARED_LAST_SYNC_TIME);
		
		Gson gson = new Gson();
		if(isEmpty(lastSyncTimeString)){
			return null;
		}else{
			return gson.fromJson(lastSyncTimeString, new TypeToken<List<PetSyncTime>>(){}.getType());
		}
	}
	
	/**
	 * 获取listview的高度
	 * @param listView
	 * @return
	 */
	public static int getTotalHeightofListView(ListView listView) {
		ListAdapter mAdapter = listView.getAdapter();
		int totalHeight = 0;
		if (mAdapter != null) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				View mView = mAdapter.getView(i, null, listView);
				mView.measure(
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				// mView.measure(0, 0);
				totalHeight += mView.getMeasuredHeight();
			}
			totalHeight += (listView.getDividerHeight() * (mAdapter.getCount() - 1));
		}
		return totalHeight;
	}
	
	/**
	 * 获取当前user id，用于beacons数据采集，存在
	 * @return
	 */
	public static String getCurrentUserId(){
		return getSysMap(Consts.SHARED_USER_ID);
	}
	
	/**
	 * 判断当前语言是否是中文
	 * @param context
	 * @return
	 */
	public static boolean isSystemLanguateZh(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
			return true;
		else
			return false;
	}
	
	public static void showLongToast(Activity activity,String msg) {
		showToast(activity,msg, 0, Toast.LENGTH_LONG);
	}
	
	public static void showLongToast(Activity activity,String msg, int iconResId) {
		showToast(activity,msg, iconResId, Toast.LENGTH_LONG);
	}

	public static void showLongToast(Activity activity,int stringId) {
		showToast(activity,activity.getString(stringId), 0, Toast.LENGTH_LONG);
	}
	
	public static void showLongToast(Activity activity,int stringId, int iconResId) {
		showToast(activity,activity.getString(stringId), iconResId, Toast.LENGTH_LONG);
	}

	public static void showShortToast(Activity activity,String msg) {
		showToast(activity,msg, 0, Toast.LENGTH_SHORT);
	}
	
	public static void showShortToast(Activity activity,String msg, int iconResId) {
		showToast(activity,msg, iconResId, Toast.LENGTH_SHORT);
	}

	public static void showShortToast(Activity activity,int stringId) {
		showToast(activity,activity.getString(stringId), 0, Toast.LENGTH_SHORT);
	}

	public static void showShortToast(Activity activity,int stringId, int iconResId) {
		showToast(activity,activity.getString(stringId), iconResId, Toast.LENGTH_SHORT);
	}
	
	public static void showToast(Activity activity,String text, int iconResId, int type) {
		PetkitToast.showToast(activity, text, iconResId, type);
	}
	
	public static boolean hasAtTags(String string) {
		if(!isEmpty(string)) {
			Pattern pattern = Pattern.compile("<a\\s+href=\"([^<>\"]*)\"[^<>]*>(.+?)</a>", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(string);
			while (matcher.find()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String getRankFormatString(Context context, int rank){
		int resId = 0;
		switch (rank) {
		case 1:
			resId = MResource.getResourceIdByName(context.getPackageName(), "string", "Rank_first");
			break;
		case 2:
			resId = MResource.getResourceIdByName(context.getPackageName(), "string", "Rank_second");
			break;
		case 3:
			resId = MResource.getResourceIdByName(context.getPackageName(), "string", "Rank_Third");
			break;

		default:
			resId = MResource.getResourceIdByName(context.getPackageName(), "string", "Rank_other");
			break;
		}
		
		return context.getString(resId);
	}
	
	
}
