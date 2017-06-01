package com.petkit.matetool.utils;

import android.annotation.SuppressLint;
import android.content.Context;
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
	public static final int CAT_LITTER =4;
//	public static int g_testStyle = 0;

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
	
	public static final String TEST_CASE_BORAD_OR_FINAL = "TEST_CASE_BORAD_OR_FINAL"; 
	public static final String TEST_ITEM_NAME = "TEST_ITEM_NAME"; 
	
	
	public static boolean FocusTestImageMode = false;
	
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
	
}
