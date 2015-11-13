package com.petkit.matetool.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.petkit.matetool.R;
import com.petkit.matetool.service.DatagramConsts;

public class Utils {
    private static final String LOG_TAG = "Utils.java";
    public static int head = 0;
    public static int video_value = 0;
    public static int video_value1 = 0;
    public static int check_value = 0;
    public static String sn = null;

    public static boolean testPass = false;

    public static final int DEFAULT_PORT = 5555;
    public static final int VIDEO_PORT = 5556;
    public static final int IMAGE_PORT = 5557;

    public static final int SPOT_PORT = 2135;

    public static final int MAX_DATA_PACKET_LENGTH = 256;
    public static byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];

    public static void initGlobalValues() {
        Globals.mTestResult = new int[Globals.mTestItem.size()] ;

        head = 0;
        sn = null;
        mIsReged = false;
        wifi_params = new wifi_test_params();
        testPass = false;

        closeSocket();
        clearTestLog();

        if(mFinalParams != null) {
            mFinalParams.clear();
        }
        if(mFinalMAC != null) {
            mFinalMAC.clear();
        }
        mFinalIndex = -1;
    }

    // video
    public static final int SERVER_RTP_SESSION_CREATE = 101;
    public static final int SERVER_RTP_SESSION_STOP = 103;
    public static final int SERVER_RTP_SESSION_CREATE_IMG = 104;
    public static final int SERVER_RTP_SESSION_STOP_IMG = 105;

    public static final int SERVER_WIFI_PARAM = 305;
    public static final int SERVER_REG_MONI = 319;
    public static final int SERVER_TEST_MODE = 320;


    /*------------ status write cmd begin -------*/
    public static final int SERVER_TEST_MODE_WRITE_SN = 321;
    //	public static final int SERVER_TEST_MODE_RETEST = 322;
    public static final int SERVER_TEST_FOCUS = 323;
    public static final int SERVER_TEST_BORAD = 324;
    public static final int SERVER_TEST_FOCUS2 = 325;
    //	public static final int SERVER_TEST_EXIT = 326;
    public static final int SERVER_TEST_EXIT_TO_BORAD = 327;
    public static final int SERVER_TEST_EXIT_TO_FOCUS = 328;
    public static final int SERVER_TEST_EXIT_TO_FOCUS2 = 329;
	/*------------ status write cmd end -------*/


    public static final int SERVER_DEVSTA_PARAM = 310;

    public static final int SERVER_CHECK_SYS_PASS = 400;
    public static final int SERVER_CHECK_SYS_FAILED = 401;
    public static final int SERVER_WRITE_SN_PASS = 402;
    public static final int SERVER_WRITE_SN_FAILED = 403;

    public static final int SERVER_REG_FAILED = 499;
    public static final int SERVER_REG_UNMONI = 500;
    public static final int SERVER_WIFI_LISTEN = 501;

    public static final String updateWifiStatus = "updateWifiStatus";
    public static final String addWifiStatus = "addWifiStatus";
    public static final String updateVoltStatus = "updateVoltStatus";
    public static final String testok = "testok";
    public static final String testfailed = "testfailed";

    public static final String writeok = "writeok";
    public static final String writesnok = "writesnok";
    public static final String writesnfailed = "writesnfailed";

    public static boolean mIsReged = false;
    public static boolean mIsReging = false;

    public static final String SERVER_CHECK_HAS_SN = "HAS_SN";
    public static final String SERVER_CHECK_HAS_NO_SN = "HAS_NO_SN";

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value
     *            要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。 和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }

    public static String intToIp(int i) {
        return (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." +  ( i >> 24 & 0xFF);
    }

    public static void sendData(final Context context, final int cmd, String data) {

        Intent intent = new Intent(DatagramConsts.BROADCAST_ACTION);
        intent.putExtra(DatagramConsts.EXTRA_ACTION, DatagramConsts.ACTION_WRITE);
        intent.putExtra(DatagramConsts.EXTRA_WRITE_CMD, cmd);
        intent.putExtra(DatagramConsts.EXTRA_DATA, data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendData(final Context context, final int cmd, boolean receive) {

        Intent intent = new Intent(DatagramConsts.BROADCAST_ACTION);
        intent.putExtra(DatagramConsts.EXTRA_ACTION, DatagramConsts.ACTION_WRITE);
        intent.putExtra(DatagramConsts.EXTRA_WRITE_CMD, cmd);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

//        if(receive) {
//            receiveData(context);
//        }
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    if (Globals.mCurCaseMode != Globals.FinalTestMode
//                            && Globals.mCurCaseMode != Globals.SpotTestMode
//                            && wifi_params.local_rtp_ip == null) {
//                        Log.e(LOG_TAG, "local_rtp_ip == null");
//                        return;
//                    }
//
//                    byte[] data = null;
//                    byte[] head = null, len = null, row = new byte[100];
//                    Log.e(LOG_TAG, "send..."+cmd);
//
//                    switch (cmd) {
//                        case SERVER_RTP_SESSION_CREATE:
//                            head = intToBytes2(SERVER_RTP_SESSION_CREATE - 100);
//                            row[0] = 0;
//                            row[1] = 0;
//                            break;
//                        case SERVER_RTP_SESSION_STOP:
//                            head = intToBytes2(SERVER_RTP_SESSION_STOP - 100);
//                            row[0] = 0;
//                            row[1] = 0;
//                            break;
//                        //FOR VIDEO BY IMAGE =======
//                        case SERVER_RTP_SESSION_CREATE_IMG:
//                            head = intToBytes2(SERVER_RTP_SESSION_CREATE - 100);
//                            row[0] = 1;
//                            row[1] = 1;
//                            break;
//                        case SERVER_RTP_SESSION_STOP_IMG:
//                            head = intToBytes2(SERVER_RTP_SESSION_STOP - 100);
//                            row[0] = 1;
//                            row[1] = 1;
//                            break;
//                        //FOR VIDEO BY IMAGE =======
////					case SERVER_TEST_MODE_OFF:
////						head = intToBytes2(SERVER_TEST_MODE);
////						row[0] = 2;
////						row[1] = 0;
////						break;
////					case TEST_PUSH_RESULT:
////						head = intToBytes2(SERVER_TEST_MODE);
////						row[0] = 0;
////						break;
//                        case SERVER_REG_UNMONI:
//                            mIsReged = false;
//                            mIsReging = false;
//                            head = intToBytes2(SERVER_REG_MONI);
//                            row[0] = 0;
//                            if(Globals.mCurCaseMode >= Globals.FocusTestMode2) {
//                                row[1] = (byte) Globals.FocusTestMode;
//                            } else {
//                                row[1] = (byte) Globals.mCurCaseMode;
//                            }
//                            if(Globals.g_station != null)
//                                row[2] = (byte) Integer.parseInt(Globals.g_station);
//                            break;
//                        case SERVER_REG_MONI:
//                            head = intToBytes2(SERVER_REG_MONI);
//                            row[0] = 1;
//                            if(Globals.mCurCaseMode >= Globals.FocusTestMode2) {
//                                row[1] = (byte) Globals.FocusTestMode;
//                            } else {
//                                row[1] = (byte) Globals.mCurCaseMode;
//                            }
//                            if(Globals.g_station != null)
//                                row[2] = (byte) Integer.parseInt(Globals.g_station);
//                            break;
//                        case SERVER_TEST_MODE:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 1;
//                            row[1] = 0;
//                            break;
////					case SERVER_TEST_EXIT:
////						head = intToBytes2(SERVER_TEST_MODE);
////						row[0] = 0;
////						row[1] = 1;
////						row[2] = 1;
////						row[3] = 0;
////						break;
//                        case SERVER_TEST_EXIT_TO_BORAD:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 0;
//                            row[1] = 1;
//                            row[2] = 1;
//                            row[3] = 0;
//                            break;
//                        case SERVER_TEST_EXIT_TO_FOCUS:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 0;
//                            row[1] = 1;
//                            row[2] = 1;
//                            row[3] = 1;
//                            break;
//                        case SERVER_TEST_EXIT_TO_FOCUS2:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 0;
//                            row[1] = 1;
//                            row[2] = 1;
//                            row[3] = 2;
//                            break;
//
//                        case SERVER_TEST_BORAD:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 1;
//                            row[1] = 1;
//                            break;
//                        case SERVER_TEST_FOCUS:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 2;
//                            row[1] = 1;
//                            break;
//                        case SERVER_TEST_FOCUS2:
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 3;
//                            row[1] = 1;
//                            break;
////					case SERVER_TEST_MODE_RETEST:
////						head = intToBytes2(SERVER_TEST_MODE);
////						row[0] = 1;
////						break;
//                        case SERVER_TEST_MODE_WRITE_SN:
//                            if(sn == null || sn.isEmpty()) {
//                                showToast(context, "SN 为空！");
//                                return;
//                            }
//                            head = intToBytes2(SERVER_TEST_MODE);
//                            row[0] = 4;
//                            row[1] = 1;
//                            row[2] = (byte) sn.getBytes().length; //SN len
//                            System.arraycopy(sn.getBytes(), 0, row, 3, sn.getBytes().length);
//                            break;
//                        // GPIO
//                        case IOD_GPIO_LIGHT_BELT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_BELT;
//                            row[1] = 1;
//                            break;
//                        case IOD_GPIO_LIGHT_BELT_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_BELT;
//                            row[1] = 0;
//                            break;
//                        case IOD_GPIO_LIGHT_LED1:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_LED1;
//                            row[1] = 1;
//                            break;
//                        case IOD_GPIO_LIGHT_LED1_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_LED1;
//                            row[1] = 0;
//                            break;
//                        case IOD_GPIO_LIGHT_LED2:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_LED2;
//                            row[1] = 1;
//                            row[2] = 1;
//                            break;
//                        case IOD_GPIO_LIGHT_LED2_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_LED2;
//                            row[1] = 0;
//                            row[2] = 0;
//                            break;
//                        case IOD_GPIO_LIGHT_IR:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_IR;
//                            row[1] = 1;
//                            break;
//                        case IOD_GPIO_LIGHT_IR_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_LIGHT_IR;
//                            row[1] = 0;
//                            break;
//                        case IOD_GPIO_CTL_PA:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_CTL_PA;
//                            row[1] = 1;
//                            break;
//                        case IOD_GPIO_CTL_PA_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_CTL_PA;
//                            row[1] = 0;
//                            break;
//                        case IOD_GPIO_CTL_VPOW:
//                            break;
//                        case IOD_GPIO_CTL_VRST:
//                            break;
//                        case IOD_GPIO_KEY_IN:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_KEY_IN;
//                            row[1] = 1;
//                            break;
//                        case IOD_GPIO_KEY_IN_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_GPIO_KEY_IN;
//                            row[1] = 0;
//                            break;
//                        case IOD_GPIO_CTL_USB_POW:
//                            break;
//                        case IOD_ADC_TEMP:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_ADC_TEMP;
//                            row[1] = 1;
//                            break;
//                        case IOD_ADC_TEMP_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_ADC_TEMP;
//                            row[1] = 0;
//                            break;
//                        case IOD_ADC_VOLT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_ADC_VOLT;
//                            row[1] = 1;
//                            break;
//                        case IOD_ADC_VOLT_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_ADC_VOLT;
//                            row[1] = 0;
//                            break;
//                        case IOD_MOTO_PTZ_LEFT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_PTZ;
//                            row[1] = 1;
//                            row[2] = 1;
//                            break;
//                        case IOD_MOTO_PTZ_RIGHT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_PTZ;
//                            row[1] = 1;
//                            row[2] = 0;
//                            break;
//                        case IOD_MOTO_PTZ_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_PTZ;
//                            row[1] = 0;
//                            break;
//                        case IOD_MOTO_LAS_LEFT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_LAS;
//                            row[1] = 1;
//                            row[2] = 1;
//                            break;
//                        case IOD_MOTO_LAS_RIGHT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_LAS;
//                            row[1] = 1;
//                            row[2] = 0;
//                            break;
//                        case IOD_MOTO_LAS_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MOTO_LAS;
//                            row[1] = 0;
//                            break;
//                        case IOD_LED_LAS:
//                            Log.e(LOG_TAG, "send........ IOD_LED_LAS");
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_LED_LAS;
//                            row[1] = 1;
//                            break;
//                        case IOD_LED_LAS_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_LED_LAS;
//                            row[1] = 0;
//                            break;
//                        case IOD_MAX_BUTT:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MAX_BUTT;
//                            row[1] = 1;
//                            break;
//                        case IOD_MAX_BUTT_OFF:
//                            head = intToBytes2(SERVER_DEVSTA_PARAM);
//                            row[0] = IOD_MAX_BUTT;
//                            row[1] = 0;
//                            break;
//                        default:
//                            return;
//                    }
//
//                    len = intToBytes2(0);
//                    data = new byte[head.length + len.length + row.length];
//                    System.arraycopy(head, 0, data, 0, head.length);
//                    System.arraycopy(len, 0, data, head.length, len.length);
//                    System.arraycopy(row, 0, data, head.length + len.length,row.length);
//
//                    InetAddress addr;
//                    DatagramSocket ds = new DatagramSocket();
//                    DatagramPacket dp = null;
//
//                    if(Globals.mCurCaseMode == Globals.BoardTestMode
//                            || Globals.mCurCaseMode == Globals.FocusTestMode
//                            || Globals.mCurCaseMode == Globals.FocusTestMode2
//                            || Globals.mCurCaseMode == Globals.SpotTestMode) {
//                        addr = InetAddress.getByName(wifi_params.local_rtp_ip.trim().replace("/", ""));
//                        dp = new DatagramPacket(data, data.length, addr, wifi_params.local_port);
//                    } else {
//                        addr = InetAddress.getByName(mFinalParams.get(mFinalIndex).local_rtp_ip.trim().replace("/", ""));
//                        dp = new DatagramPacket(data, data.length, addr, mFinalParams.get(mFinalIndex).local_port);
//                    }
//                    ds.send(dp);
//
//                    if( cmd== SERVER_REG_UNMONI) {
//                        Thread.sleep(500);
//                        ds.send(dp);
//                        ds.send(dp);
//                    } else if(cmd >= SERVER_TEST_MODE_WRITE_SN && cmd <= SERVER_TEST_EXIT_TO_FOCUS2){
//                        Thread.sleep(1000);
//                        ds.send(dp);
//                        Thread.sleep(1000);
//                        ds.send(dp);
//                        Thread.sleep(1000);
//                        ds.send(dp);
//                        Thread.sleep(1000);
//                        ds.send(dp);
//                    }
//
//                    ds.close();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, e.toString());
//                }
//
//                addTestLog("send cmd=" + cmd);
//            }
//        }.start();
    }

    public static class wifi_test_params {
        public String local_rtp_ip; // 目标板地址
        public int local_port; // 目标板接收服务端口
        public String ssid; // 目标板链接的WIFI名称
        public byte rsq; // 目标板链接的WIFI信号
        public byte state; // 目标板链接的WIFI状态
        public byte index; // 目标板的夹具序号
        public String mac; // 目标板的MAC
        public String sn; // 目标板的sn
        public int status; // 目标板的检测状态
        public String version; // 目标板的version
    };

    public static wifi_test_params wifi_params = new wifi_test_params();
    public static ArrayList<wifi_test_params> mFinalParams = new ArrayList<wifi_test_params>();
    public static ArrayList<String> mFinalMAC = new ArrayList<String>();
    public static int mFinalIndex = -1;

    static class unit_test_params {
        char devid;// HS_MD_IOD_DEV_S 系统将各个控制设备编制了设备号，从设备号中找到即可
        char command;// 此处表示状态 0 关闭 1开启 2 事件（开启状态下如果需要有值的在dir中体现）
        char dir;// adc 0-99（0.0-9.9），key （0-释放1-长‘按//
        // 2-短按），ptz-moto（0-左转或左靠底，1-右转或右靠底）
    };

    // GPIO TEST ON
    public static final int IOD_GPIO_LIGHT_BELT = 0; // 0灯带----break fun
    public static final int IOD_GPIO_LIGHT_LED1 = 1;// 1指示灯1----break fun
    public static final int IOD_GPIO_LIGHT_LED2 = 2;// 2指示灯2
    public static final int IOD_GPIO_LIGHT_IR = 3; // 3红外灯
    public static final int IOD_GPIO_CTL_PA = 4; // 4PA控制
    public static final int IOD_GPIO_CTL_VPOW = 5; // 5VIDEO_电压控制
    public static final int IOD_GPIO_CTL_VRST = 6; // 6VIDEO_复位控制
    public static final int IOD_GPIO_KEY_IN = 7; // 7KEY IN-用户RESET按键（低电平有效）
    public static final int IOD_GPIO_CTL_USB_POW = 8; // USB WIFI模块
    public static final int IOD_ADC_TEMP = 9; // 温度ADC
    public static final int IOD_ADC_VOLT = 10; // 电压ADC
    public static final int IOD_MOTO_PTZ = 11; // 云台马达
    public static final int IOD_MOTO_LAS = 12; // 逗宠
    public static final int IOD_LED_LAS = 13; // 激光灯
    public static final int IOD_MAX_BUTT = 14; // MIC

    public static final int VIDEO_VALUE = 15;
    public static final int BLE_CONNECT = 16; //
    public static final int VIDEO = 17; // MIC
    // GPIO TEST OFF
    public static final int IOD_GPIO_LIGHT_BELT_OFF = 600; // 0灯带----break fun
    public static final int IOD_GPIO_LIGHT_LED1_OFF = 601;// 1指示灯1----break fun
    public static final int IOD_GPIO_LIGHT_LED2_OFF = 602;// 2指示灯2
    public static final int IOD_GPIO_LIGHT_IR_OFF = 603; // 3红外灯
    public static final int IOD_GPIO_CTL_PA_OFF = 604; // 4PA控制
    public static final int IOD_GPIO_CTL_VPOW_OFF = 605; // 5VIDEO_电压控制
    public static final int IOD_GPIO_CTL_VRST_OFF = 606; // 6VIDEO_复位控制
    public static final int IOD_GPIO_KEY_IN_OFF = 607; // 7KEY IN-用户RESET按键（低电平有效）
    // public static final int IOD_GPIO_CTL_USB_POW_OFF = 608; // USB WIFI模块
    public static final int IOD_ADC_TEMP_OFF = 609; // 温度ADC
    public static final int IOD_ADC_VOLT_OFF = 610; // 电压ADC
    public static final int IOD_MOTO_PTZ_OFF = 611; // 云台马达ZUO
    public static final int IOD_MOTO_PTZ_LEFT = 711; // 云台马达ZUO
    public static final int IOD_MOTO_PTZ_RIGHT = 712; // 云台马达YOU

    public static final int IOD_MOTO_LAS_OFF = 612; // 激光灯马达
    public static final int IOD_MOTO_LAS_LEFT = 812; // 逗宠ZUO
    public static final int IOD_MOTO_LAS_RIGHT = 813; // 逗宠YOU

    public static final int IOD_LED_LAS_OFF = 613; // 激光灯
    public static final int IOD_MAX_BUTT_OFF = 614; // MIC

    //	public static final int TEST_PUSH_RESULT = 654; // 提交测试结果
    public static final int SERVER_TEST_MODE_OFF = 655; // 关闭测试

    private static byte[] newByteForInt() {
        byte[] temp = new byte[4];

        temp[0] = 0x00;
        temp[1] = 0x00;

        return temp;
    }

    private static DatagramSocket udpSocket = null;
    private static void initReceiver() {
        if(udpSocket == null) {
            closeSocket();
            try {
//				closeSocket();
                udpSocket = new DatagramSocket(DEFAULT_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeSocket() {
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        System.gc();
//			}
//		}, 300);
    }



    public static void receiveSpecData(final Context context) throws SocketException, UnknownHostException {
//        initReceiver();

//        new Thread() {
//
//            @Override
//            public void run() {
//
//                try {
//                    DatagramPacket recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
//                    udpSocket.receive(recivedata);
//
//                    if (recivedata.getLength() != 0) {
//                        byte[] temp = new byte[4];
//                        System.arraycopy(buffer, 0, temp, 0, 4);
//                        int g_head = bytesToInt2(temp, 0);
//                        int index = 8;
//                        byte[] data = recivedata.getData();
//
//                        Log.e(LOG_TAG, "receive..." + g_head);
//                        switch (g_head) {
//                            case SERVER_DEVSTA_PARAM:
//                                index = 8;
//                                byte devid = data[index++];
//                                byte command = data[index++];
//                                byte dir = data[index++];
//
//                                switch (devid) {
//                                    case IOD_GPIO_KEY_IN:
//                                        sendBroadcast(context, "keyin");
//                                        receiveSpecData(context);
//                                        break;
//                                    case IOD_ADC_VOLT:
//                                        sendBroadcast(context, updateVoltStatus);
//                                        receiveSpecData(context);
//                                        break;
//                                    default:
//                                        receiveSpecData(context);
//                                        break;
//                                }
//                            default:
//                                receiveSpecData(context);
//                                break;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    closeSocket();
//                }
//            }
//        }.start();
    }

    public static void receiveWriteData(final Context context) throws SocketException, UnknownHostException {
//        initReceiver();
//
//        new Thread() {
//
//            @Override
//            public void run() {
//
//                try {
//                    DatagramPacket recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
//                    udpSocket.receive(recivedata);
//
//                    if (recivedata.getLength() != 0) {
//                        byte[] temp = new byte[4];
//                        System.arraycopy(buffer, 0, temp, 0, 4);
//                        int g_head = bytesToInt2(temp, 0);
//                        int index = 8;
//                        byte[] data = recivedata.getData();
//
//                        Log.e(LOG_TAG, "receive..." + g_head);
//                        switch (g_head) {
//                            case SERVER_TEST_MODE:
//                                sendBroadcast(context, writeok);
//                                showToast(context, "写入标志位成功！");
//                                break;
//                            default:
//                                receiveWriteData(context);
//                                break;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    closeSocket();
//                }
//            }
//        }.start();
    }

    public static void receiveWriteSNData(final Context context) throws SocketException, UnknownHostException {
//        initReceiver();
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    DatagramPacket recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
//                    udpSocket.receive(recivedata);
//
//                    if (recivedata.getLength() != 0) {
//                        byte[] temp = new byte[4];
//                        System.arraycopy(buffer, 0, temp, 0, 4);
//                        int g_head = bytesToInt2(temp, 0);
//                        int index = 8;
//                        byte[] data = recivedata.getData();
//
//                        Log.e(LOG_TAG, "receive..." + g_head);
//                        switch (g_head) {
//                            case SERVER_TEST_MODE:
//                                index = 8;
//
//                                byte data0 = data[index++];
//                                byte data1 = data[index++];
////							byte data2 = data[index++];
////							byte data3 = data[index++];
//
////							index = 50;
////							byte data50 = data[index++];
////							byte data51 = data[index++];
//
////							 if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                if(data1 == 0x01) { //has sn
//                                    sendBroadcast(context, writesnok);
//                                    Log.e(LOG_TAG, "receiveWriteSNData........ 写入SN成功！");
//                                    showToast(context, "写入SN成功！");
//                                }else {
//                                    sendBroadcast(context, writesnfailed);
//                                    showToast(context, "写入SN失败！");
//                                }
////							}
//                                break;
//                            default:
//                                receiveWriteSNData(context);
//                                break;
//                        }
//                    }
//                } catch (Exception e) {
//                    try {
//                        receiveWriteSNData(context);
//                    } catch (SocketException | UnknownHostException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        }.start();
    }


    public static void receiveData(final Context context) throws SocketException, UnknownHostException {
//        initReceiver();
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                String logString = "";
//                try {
//                    DatagramPacket recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
//                    udpSocket.receive(recivedata);
//                    if (recivedata.getLength() != 0) {
//                        byte[] temp = new byte[4];
//                        System.arraycopy(buffer, 0, temp, 0, 4);
//                        int g_head = bytesToInt2(temp, 0);
//                        int index = 8;
//                        byte[] data = recivedata.getData();
//
//                        Log.e(LOG_TAG, "receive..." + g_head);
//                        switch (g_head) {
//                            case SERVER_REG_MONI:
//                                Log.e(LOG_TAG, "receive........ SERVER_REG_MONI");
//                                if (data[index] == 1) {
//                                    sendData(context, SERVER_TEST_MODE, true);
//                                    logString += "SERVER_TEST_MODE\n";
//
//                                    mIsReged = true;
//                                    sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                } else {
//                                    if(data[index] >= 100) {
//                                        sendBroadcast(context, "" + SERVER_REG_FAILED);
//                                        logString += "reg failed...\n";
//                                    } else {
//                                        sendBroadcast(context, "" + SERVER_REG_UNMONI);
//                                        logString += "reg unmoni...\n";
//                                    }
//                                    mIsReged = false;
//                                }
//                                break;
//                            case SERVER_WIFI_PARAM:
//                                logString += "receive...SERVER_WIFI_PARAM\n";
//                                if (mIsReged || mIsReging) {
//                                    logString +="reg succeed...";
//                                    return;
//                                }
//
//                                wifi_test_params params = new wifi_test_params();
//                                temp = new byte[16];
//                                System.arraycopy(data, index, temp, 0, 16);
//                                params.local_rtp_ip = new String(temp);
//                                index += 16;
//
//                                temp = newByteForInt();
//                                System.arraycopy(data, index, temp, 2, 2);
//                                params.local_port = bytesToInt2(temp, 0);
//                                index += 2;
//
//                                temp = new byte[64];
//                                System.arraycopy(data, index, temp, 0, 64);
//                                params.ssid = new String(temp);
//                                index += 64;
//
//                                byte[] tempchar = new byte[1];
//                                System.arraycopy(data, index, tempchar, 0, 1);
//                                params.rsq = tempchar[0];
//                                index++;
//
//                                System.arraycopy(data, index, tempchar, 0, 1);
//                                params.state = tempchar[0];
//                                index++;
//
//                                System.arraycopy(data, index, tempchar, 0, 1);
//                                params.index = tempchar[0];
//                                index++;
//
//                                // mac
//                                temp = new byte[20];
//                                System.arraycopy(data, index, temp, 0, 20);
//                                String mactemp = new String(temp);
//                                params.mac = getmac(mactemp.trim());
//                                index += 20;
//
//                                //sn
//                                temp = new byte[32];
//                                System.arraycopy(data, index, temp, 0, 32);
//                                params.sn = new String(temp);
//                                index += 32;
//
//                                //status
//                                System.arraycopy(data, index, tempchar, 0, 1);
//                                params.status = tempchar[0];
//                                index++;
//
//                                if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                    if(params.status != 0) {
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.FocusTestMode) {
//                                    if(params.status != 1) {
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.FocusTestMode2) {
//                                    if(params.status != 2) {
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                    if(params.status != 3) {
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                    if(params.status < 3 || (params.sn != null && !params.sn.trim().equals(sn))) {
//                                        if(params.status >= 3 && params.sn != null && !params.sn.trim().equals(sn)) {
//                                            sendBroadcast(context, "snerror");
//                                        }
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                }
//
//                                if(Globals.mCurCaseMode == Globals.BoardTestMode
//                                        || Globals.mCurCaseMode == Globals.FocusTestMode2
//                                        || Globals.mCurCaseMode == Globals.FocusTestMode) {
//                                    if(!mIsReging) {
//                                        mIsReging = true;
//                                    } else {
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                }
//
//                                //version
//                                temp = new byte[32];
//                                System.arraycopy(data, index, temp, 0, 32);
//                                params.version = new String(temp);
//
//                                if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                    if (!String.valueOf(params.index).equals(Globals.g_station)) {
////									sendBroadcast(context, FixtureNumNotSame);
////									logString += "Fixture Num Not Same\n";
//                                        mIsReging = false;
//                                        receiveData(context);
//                                        return;
//                                    }
//                                }
//
//                                if(Globals.mCurCaseMode == Globals.BoardTestMode
//                                        || Globals.mCurCaseMode == Globals.FocusTestMode
//                                        || Globals.mCurCaseMode == Globals.FocusTestMode2
//                                        || Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                    // update wifi info
//                                    head = g_head;
//                                    wifi_params.local_rtp_ip = params.local_rtp_ip.replace("/", "");
//                                    wifi_params.local_port = params.local_port;
//                                    wifi_params.ssid = params.ssid;
//                                    wifi_params.rsq = params.rsq;
//                                    wifi_params.state = params.state;
//                                    wifi_params.index = params.index;
//                                    wifi_params.mac = params.mac;
//                                    wifi_params.sn = params.sn;
//                                    wifi_params.status = params.status;
//                                    wifi_params.version = params.version;
//
//                                    if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                        sendBroadcast(context, updateWifiStatus);
//                                    }
//                                    sendData(context, SERVER_REG_MONI, true);
//                                } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                    // add wifi info
//                                    if(!isExist(params.mac)) {
//                                        if(params.mac != null) {
//                                            mFinalParams.add(params);
//                                            mFinalMAC.add(params.mac);
//                                            sendBroadcast(context, addWifiStatus);
//                                        }
//                                    }
//                                    receiveData(context);
//                                    return;
//                                }
//
//                                logString += "ip="+ wifi_params.local_rtp_ip + "\n";
//                                logString += "port=" + wifi_params.local_port + "\n";
//                                logString += "ssid=" + wifi_params.ssid + "\n";
//                                logString += "rsq=" + wifi_params.rsq + "\n";
//                                logString += "state=" + wifi_params.state + "\n";
//                                logString += "index=" + wifi_params.index + "\n";
//                                logString += "mac=" + wifi_params.mac + "\n\n";
//                                logString += "sn=" + wifi_params.sn + "\n\n";
//                                logString += "status=" + wifi_params.status + "\n";
//                                logString += "version=" + wifi_params.version + "\n\n";
//                                break;
//
//                            case SERVER_DEVSTA_PARAM:
//                                logString += "receive.......SERVER_DEVSTA_PARAM\n";
//                                index = 8;
//                                byte devid = data[index++];
//                                byte command = data[index++];
//                                byte dir = data[index++];
//
//                                switch (devid) {
//                                    case IOD_GPIO_LIGHT_BELT:
//                                        logString += "LIGHT_BELT\n";
//                                        break;
//                                    case IOD_GPIO_LIGHT_LED1:
//                                        logString += "LED1\n";
//                                        break;
//                                    case IOD_GPIO_LIGHT_LED2:
//                                        logString += "LIGHT_LED2\n";
//                                        break;
//                                    case IOD_GPIO_LIGHT_IR:
//                                        logString += "LIGHT_IR\n";
//                                        break;
//                                    case IOD_GPIO_CTL_PA:
//                                        logString += "CTL_PA\n";
//                                        break;
//                                    case IOD_GPIO_CTL_VPOW:
//                                        logString += "CTL_VPOW\n";
//                                        break;
//                                    case IOD_GPIO_CTL_VRST:
//                                        logString += "CTL_VRST\n";
//                                        break;
////							case IOD_GPIO_KEY_IN:
////								logString += "KEY_IN";
////								sendBroadcast(context, "keyin");
////								receiveData(context);
////								break;
//                                    case IOD_GPIO_CTL_USB_POW:
//                                        logString += "USB_POW\n";
//                                        break;
//                                    case IOD_ADC_TEMP:
//                                        logString += "ADC_TEMP\n";
//                                        break;
////							case IOD_ADC_VOLT:
////								volt = (float) dir / 10;
////								logString += "ADC_VOLT + volt=" + volt + "v\n";
////								sendBroadcast(context, updateVoltStatus);
//////								receiveData(context);
////								return;
//                                    case IOD_MOTO_PTZ:
//                                        logString += "MOTO_PTZ\n";
//                                        break;
//                                    case IOD_MOTO_LAS:
//                                        logString += "MOTO_LAS\n";
//                                        break;
//                                    case IOD_LED_LAS:
//                                        logString += "LED_LAS\n";
//                                        break;
//                                    case IOD_MAX_BUTT:
//                                        logString += "MAX_BUTT\n";
//                                        break;
//                                    case VIDEO_VALUE:
//                                        logString += "VIDEO_VALUE\n";
//                                        video_value = dir;
//                                        sendBroadcast(context, ""+VIDEO_VALUE);
//                                        receiveData(context);
//                                        return;
//                                    default:
//                                        return;
//                                }
//
//                                if(command == -1) {
//                                    sendBroadcast(context, testfailed);
//                                    logString += " Failed!";
//                                } else if (command == 0) {
//                                    sendBroadcast(context, testok);
//                                    logString += " Pass!\n";
//                                }
//                                break;
//                            case SERVER_TEST_MODE:
//                                logString += "SERVER_TEST_MODE ";
//                                index = 8;
//
//                                byte data0 = data[index++];
//                                byte data1 = data[index++];
////							byte data2 = data[index++];
////							byte data3 = data[index++];
//
//                                index = 50;
//                                byte data50 = data[index++];
//                                byte data51 = data[index++];
//
//                                if(data1 == 0x00) { //no sn
//                                    if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                        sendBroadcast(context, SERVER_CHECK_HAS_NO_SN);
//                                        mIsReging = false;
//                                        return;
//                                    }
//
//                                    if ((data50 & data51) == 0x1) {
//                                        sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                    } else {
////									mIsReged = true;
////									sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                    }
//                                } else if(data1 == 0x01) { //has sn
//                                    String rtnsn = new String(data, 8 + 3, 46).trim();
//                                    if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                        if(sn != null && rtnsn.equals(sn)) {
//                                            if((data50 & data51) != 0x1) {
////											mIsReged = true;
////											sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                            } else {
//                                                sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                            }
//                                        } else {
//                                            sendBroadcast(context, "snerror");
//                                            return;
//                                        }
//                                    } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                        if(rtnsn != null) {
//                                            sendBroadcast(context, SERVER_CHECK_HAS_SN);
//                                            if(sn != null) {
//                                                if(rtnsn.equals(sn)) {
////												mIsReged = true;
////												sendBroadcast(context, "" + SERVER_WRITE_SN_PASS);
//                                                    sendBroadcast(context, writesnok);
//                                                    logString += "write sn pass=====  sn=" + sn + "\n";
//                                                }else {
//                                                    sendBroadcast(context, "" + SERVER_WRITE_SN_FAILED);
//                                                    sendBroadcast(context, writesnfailed);
//                                                    logString += "write sn failed===== sn=" + sn + "\n";
//                                                }
//                                            }
//                                        } else {
//                                            sendBroadcast(context, SERVER_CHECK_HAS_NO_SN);
//                                        }
//                                    } else if(Globals.mCurCaseMode == Globals.FocusTestMode
//                                            || Globals.mCurCaseMode == Globals.FocusTestMode2) {
//                                        if ((data50 & data51) == 0x01) {
//                                            sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                        } else {
////										mIsReged = true;
////										sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                        }
//                                    } else {
////									mIsReged = true;
//                                        sendBroadcast(context, SERVER_CHECK_HAS_SN);
////									sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                    }
//                                }
//
//                                mIsReging = false;
//                                break;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    closeSocket();
//                }
//
//                addTestLog(logString);
//            }
//        }.start();
    }

    public static void sendUnmoniCMD(Context context) {
            mIsReged = false;
            mIsReging = false;
            Log.e(LOG_TAG, "发送反注册。。。。");
            Utils.sendData(context, Utils.SERVER_REG_UNMONI, false);
    }

    protected static boolean isExist(String mac) {
        if(mac != null) {
            for(String m : mFinalMAC) {
                if(m.equals(mac) || m.equals(mac.toUpperCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressLint("DefaultLocale")
    private static String getmac(String mactemp) {
        if(mactemp == null || mactemp.length() != 17) {
            return null;
        }

        String temp = mactemp.replace(":", "");
        String mac = "";

        for(int i = 10; i < temp.length() && i >= 0; i -= 2) {
            mac += (temp.substring(i, i + 2)/* + (i == 0 ? "" : ":")*/);
        }

        return mac.toUpperCase();
    }

    public static void showToast(Context context, int StringID) {
        showToast(context, context.getString(StringID));
    }

    @SuppressLint("DefaultLocale")

    private static String lastTxt = "";
    public static void showToast(final Context context, final String text) {
        if(!lastTxt.equals(text)) {
            lastTxt = text;
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lastTxt = "";
                }
            }, 1000);
        }
    }

    public static void sendBroadcast(Context context, final String message) {
        if (context != null) {
            Intent intent = new Intent(message);
            context.sendBroadcast(intent);
        }
    }

    //	private static Object object = new Object();
    private static ArrayList<String> mTestLogResult = new ArrayList<String>();
    public static synchronized void clearTestLog() {
//		synchronized (object) {
        mTestLogResult.clear();
//		}
    }

    public static synchronized void addTestLog(String str) {
//		synchronized (object) {
        mTestLogResult.add(str);
//		}
    }

    @SuppressLint("DefaultLocale")
    public static void saveResultFile(Context context, int station) {
        String str =  "测试日期:" + Globals.g_date + "\n";
        str += (context.getResources().getString(R.string.fixture_number) + ":" + station + "\n");

        str += "测项:";
        if(Globals.mCurCaseMode == Globals.BoardTestMode) {
            str += "板测\n";
        } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
            str += "终测\n";
        }else if(Globals.mCurCaseMode == Globals.FocusTestMode) {
            str += "调焦测试\n";
        } else if(Globals.mCurCaseMode == Globals.FocusTestMode2) {
            str += "对焦测试\n";
        }else if(Globals.mCurCaseMode == Globals.SpotTestMode) {
            str += "抽测\n";
        }

        str +="测试结果 begin:===================\n";

        if(Globals.mTestResult != null) {
            for (int i = 0; i < Globals.mTestResult.length; i++) {
                str += (Globals.mTestItem.get(i) + "：(" + Globals.mTestResult[i] + "," + Globals.mTestResult[i] + ")\n");
                if (Globals.mTestResult[i] == 0) {
                    showToast(context, "还有项目未测试，请确认!!!");
                    return;
                }
            }
        }
        str +="测试结果 end:===================\n";

        if(mTestLogResult != null) {
            for (String reString : mTestLogResult) {
                str += reString;
            }
        }
        String filePath = Globals.localUrl + /*System.currentTimeMillis()*/wifi_params.mac.toLowerCase() + "_testReport.txt";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();

            showToast(context, "保存成功！保存路径:" + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "保存失败！");
        }
    }

}