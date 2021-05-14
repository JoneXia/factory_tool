package com.petkit.matetool.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.petkit.android.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Globals {
	public static final String localUrl = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/mate_test/";

	public static final String EXTRA_PERMISSION_CONTENT      = "com.petkit.android.EXTRA_PERMISSION_CONTENT";

	public static final String BROADCAST_PERMISSION_FINISHED = "com.petkit.android.BROADCAST_PERMISSION_FINISHED";

	public static final boolean PERMISSION_ERASE = false;
	//test date
	public static String g_date;
	//worker station
//	public static String g_station;
	
	//00000-99999
	public static String SHARED_FACTORY_NUMBER_PRO = "PRO";
	public static String SHARED_FACTORY_NUMBER_STYLE = "STYLE";
	
	public static String SHARED_BLE_VALUE = "SHARED_BLE_VALUE";
	public static String SHARED_BLE_VALUE2 = "SHARED_BLE_VALUE2";
	public static String SHARED_VOLT_MAX = "SHARED_VOLT_MAX";
	public static String SHARED_VOLT_MIN = "SHARED_VOLT_MIN";
	
	//test style  1--mate style , 2---mate pro
	public static final int MATE_STYLE = 1;
	public static final int MATE_PRO =2;
	public static final int FEEDER =3;
	public static final int COZY =4;
	public static final int GO =5;
	public static final int FEEDER_MINI =6;
	public static final int T3 = 7;
    public static final int K2 = 8;
	public static final int AQ = 9;
	public static final int D3 = 10;
	public static final int D4 = 11;
	public static final int P3 = 12;
	public static final int W5 = 13;
	public static final int T4 = 14;

//	public static int g_testStyle = 0;

	/**
	 * SN中的设备类型标记位定义
	 */
	public static final String DEVICE_TYPE_CODE_D1 = "P";
	public static final String DEVICE_TYPE_CODE_Z1 = "A";
	public static final String DEVICE_TYPE_CODE_D2 = "B";
	public static final String DEVICE_TYPE_CODE_Z1S = "C";
	public static final String DEVICE_TYPE_CODE_T3 = "D";
	public static final String DEVICE_TYPE_CODE_K2 = "E";
	public static final String DEVICE_TYPE_CODE_D3 = "F";
	public static final String DEVICE_TYPE_CODE_D4 = "G";
	public static final String DEVICE_TYPE_CODE_P3 = "H";
	public static final String DEVICE_TYPE_CODE_W5 = "I";
	public static final String DEVICE_TYPE_CODE_W5C = "J";
	public static final String DEVICE_TYPE_CODE_T4 = "L";

	public static ArrayList<String> mTestItem = new ArrayList<String>();
	public static int[] mTestResult = null;
	public static int[] mTestSysResult = null;
    public final static String[] mTestItemStyle = {
            "版本检测","指示灯","红外灯","电压ADC","PA控制","IR-cut","RESET按键","云台马达","逗宠","MIC", "蓝牙","视频"};

    public final static String[] mTestItemPro = {
            "版本检测","灯带", "指示灯","红外灯","电压ADC","PA控制","IR-cut","RESET按键","云台马达","逗宠","MIC", "蓝牙","视频"};


    public static boolean hasTestResult() {
		if(mTestResult != null) {
			for(int i = 0; i< mTestResult.length; i++) {
				if(mTestResult[i] > 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	//test case
	public final static int BoardTestMode = 6;
	public final static int FinalTestMode = 7;
	public final static int FocusTestMode = 8;
	public final static int SpotTestMode = 9;
	public final static int FocusTestMode2 = 10;
	public static int mCurCaseMode = -1;
	
	//test result 
	public static final int TEST_PASS      = 1;
	public static final int TEST_FAILED = 2;

	public static String organizationSN(Context context, int station, int style) {
		String sn;
		if(style == Globals.MATE_PRO) {
			sn = "001";
		} else if(style == Globals.MATE_STYLE){
			sn = "002";
		} else {
			return null;
		}
		
		String date = getDateOfToday();
		int factoryNum;
		if(style == Globals.MATE_PRO) {
			factoryNum = CommonUtils.getSysIntMap(context, Globals.SHARED_FACTORY_NUMBER_PRO, 0) + 1;
		} else {
			factoryNum = CommonUtils.getSysIntMap(context, Globals.SHARED_FACTORY_NUMBER_STYLE, 0) + 1;
		}
		
		String serialNum = String.format("%05d", factoryNum);
		return (sn + date + station + serialNum).trim();
	}

    public static boolean checkSNValid(String sn){
        return sn != null && sn.length() == 15 && (sn.startsWith("001") || sn.startsWith("002"));
    }
	
	
	@SuppressLint("SimpleDateFormat")
	public static String getDateOfToday() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String dateString = format.format(new Date());
		
		return dateString.substring(2);
	}

	public static boolean checkPermission(Activity activity, String permission) {
		PackageManager pm = activity.getPackageManager();
		return (PackageManager.PERMISSION_GRANTED ==
				pm.checkPermission(permission, activity.getPackageName()));
	}

}
