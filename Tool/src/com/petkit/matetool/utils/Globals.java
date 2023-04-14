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

	public static final String EXTRA_PERMISSION_CONTENT      = "com.petkit.android.EXTRA_PERMISSION_CONTENT";
	public static final String BROADCAST_PERMISSION_FINISHED = "com.petkit.android.BROADCAST_PERMISSION_FINISHED";

	public static final boolean PERMISSION_ERASE = false;
	
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
	public static final int W5 = 12;
	public static final int W5C = 13;
	public static final int P3C = 14;
	public static final int T4 = 15;
	public static final int T4_p = 16;   //T4标配K3
	public static final int K3 = 17;
	public static final int AQR = 18;
	public static final int P3D = 19;
	public static final int AQ1S = 20;
	public static final int R2 = 21;
	public static final int W5N = 22;
	public static final int W4X = 23;
	public static final int AQH1_500 = 24;
	public static final int AQH1_1000 = 25;
	public static final int CTW2 = 26;
	public static final int D3_1 = 27;
	public static final int D4_1 = 28;
	public static final int D4S = 29;
	public static final int AQH1_500_A = 30;
	public static final int AQH1_1000_A = 31;
	public static final int HG = 32;
	public static final int HG_110V = 33;
	public static final int HG_P = 34;
	public static final int HG_P_110V = 35;
	public static final int W4X_UV = 36;
	public static final int CTW3 = 37;
	public static final int D4_2 = 38;


	//TODO: 新增设备时，max值需加一
	public static final int MAX = 39;
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
	public static final String DEVICE_TYPE_CODE_P3C = "H";
	public static final String DEVICE_TYPE_CODE_W5 = "I";
	public static final String DEVICE_TYPE_CODE_W5C = "J";
	public static final String DEVICE_TYPE_CODE_T4 = "L";
	public static final String DEVICE_TYPE_CODE_K3 = "M";
	public static final String DEVICE_TYPE_CODE_AQR = "N";
	public static final String DEVICE_TYPE_CODE_P3D = "K";
	public static final String DEVICE_TYPE_CODE_AQ1S = "O";
	public static final String DEVICE_TYPE_CODE_R2 = "Q";
	public static final String DEVICE_TYPE_CODE_W5N = "R";
	public static final String DEVICE_TYPE_CODE_W4X = "S";
	public static final String DEVICE_TYPE_CODE_AQH1_500 = "T";
	public static final String DEVICE_TYPE_CODE_AQH1_1000 = "U";
	public static final String DEVICE_TYPE_CODE_D4_1 = "D";

	/**
	 * 设备代码必须是2位
	 */
	public static final String DEVICE_TYPE_CODE_NEW_D1 = "P1";
	public static final String DEVICE_TYPE_CODE_NEW_Z1 = "A1";
	public static final String DEVICE_TYPE_CODE_NEW_D2 = "B1";
	public static final String DEVICE_TYPE_CODE_NEW_Z1S = "C1";
	public static final String DEVICE_TYPE_CODE_NEW_T3 = "D1";
	public static final String DEVICE_TYPE_CODE_NEW_K2 = "E1";
	public static final String DEVICE_TYPE_CODE_NEW_D3 = "F1";
	public static final String DEVICE_TYPE_CODE_NEW_D4 = "G1";
	public static final String DEVICE_TYPE_CODE_NEW_P3C = "H1";
	public static final String DEVICE_TYPE_CODE_NEW_W5 = "I1";
	public static final String DEVICE_TYPE_CODE_NEW_W5C = "J1";
	public static final String DEVICE_TYPE_CODE_NEW_T4 = "L1";
	public static final String DEVICE_TYPE_CODE_NEW_T4_P = "L2";	//产线区分出厂是否标配K3
	public static final String DEVICE_TYPE_CODE_NEW_K3 = "M1";
	public static final String DEVICE_TYPE_CODE_NEW_P3D = "K1";
	public static final String DEVICE_TYPE_CODE_NEW_R2 = "Q1";
	public static final String DEVICE_TYPE_CODE_NEW_W5N = "R1";
	public static final String DEVICE_TYPE_CODE_NEW_W4X = "S1";

	public static final String DEVICE_TYPE_CODE_NEW_AQR = "N1";
	public static final String DEVICE_TYPE_CODE_NEW_AQ1S = "O1";
	public static final String DEVICE_TYPE_CODE_NEW_AQH1_1000 = "U1";
	public static final String DEVICE_TYPE_CODE_NEW_AQH1_500 = "T1";
	public static final String DEVICE_TYPE_CODE_NEW_CTW2 = "W2";
	public static final String DEVICE_TYPE_CODE_NEW_D3_1 = "D3";
	public static final String DEVICE_TYPE_CODE_NEW_D4_1 = "D4";
	public static final String DEVICE_TYPE_CODE_NEW_D4S = "D5";
	public static final String DEVICE_TYPE_CODE_NEW_HG 	= "H2";
	public static final String DEVICE_TYPE_CODE_NEW_HG_100V 	= "H3";
	public static final String DEVICE_TYPE_CODE_NEW_HG_P 	= "H4";
	public static final String DEVICE_TYPE_CODE_NEW_HG_P_100V 	= "H5";
	public static final String DEVICE_TYPE_CODE_NEW_CTW3 	= "W3";
	public static final String DEVICE_TYPE_CODE_NEW_W4X_UV 	= "W4";
	public static final String DEVICE_TYPE_CODE_NEW_D4_2 	= "D8";

	public static final int TYPE_TEST_PARTIALLY         = 1;
	public static final int TYPE_TEST                   = 2;
	public static final int TYPE_MAINTAIN               = 3;
	public static final int TYPE_CHECK                  = 4;
	public static final int TYPE_AFTERMARKET           	= 7;
	public static final int TYPE_DUPLICATE_MAC          = 5;
	public static final int TYPE_DUPLICATE_SN           = 6;
	public static final int TYPE_TEST_MAINBOARD         = 8;
















	public static final String localUrl = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/mate_test/";
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
