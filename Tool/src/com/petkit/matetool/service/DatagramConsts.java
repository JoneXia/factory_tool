package com.petkit.matetool.service;

/**
 * Created by Jone on 2015/10/27.
 */
public class DatagramConsts {

    /** Activity may broadcast this broadcast in order to write, destory service. */
    public static final String BROADCAST_ACTION = "com.petkit.matetool.broadcast.BROADCAST_ACTION";
    public static final String EXTRA_ACTION = "com.petkit.matetool.extra.EXTRA_ACTION";

    public static final String BROADCAST_PROGRESS = "com.petkit.matetool.broadcast.BROADCAST_PROGRESS";
    public static final String EXTRA_DATA = "com.petkit.matetool.extra.EXTRA_DATA";
    public static final String EXTRA_PROGRESS = "com.petkit.matetool.extra.EXTRA_PROGRESS";
    public static final String EXTRA_WORK_STATION = "com.petkit.matetool.extra.EXTRA_WORK_STATION";
    public static final String EXTRA_WRITE_CMD = "com.petkit.matetool.extra.EXTRA_WRITE_CMD";
    public static final String EXTRA_CURRENT_MODE = "com.petkit.matetool.extra.EXTRA_CURRENT_MODE";
    public static final String EXTRA_WIFI_PARAMS = "com.petkit.matetool.extra.EXTRA_WIFI_PARAMS";
    public static final String EXTRA_MATE_STYLE = "com.petkit.matetool.extra.EXTRA_MATE_STYLE";

    public static final int ACTION_WRITE         = 1;
    public static final int ACTION_DESTROY       = 2;
    public static final int ACTION_CLEAR       = 3;



    public static final int DEFAULT_PORT = 5555;
    public static final int VIDEO_PORT = 5556;
    public static final int IMAGE_PORT = 5557;
    public static final int SPOT_PORT = 2135;

    //test case
    public final static int BoardTestMode = 6;
    public final static int FinalTestMode = 7;
    public final static int FocusTestMode = 8;
    public final static int SpotTestMode = 9;
    public final static int FocusTestMode2 = 10;

    public static final int MAX_DATA_PACKET_LENGTH = 256;


    public static final int DATAGRAM_START = 0xffe;
    public static final int DATAGRAM_DESTROY = 0xfff;
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

//    public static final int SERVER_REG_FAILED = 499;
    public static final int SERVER_REG_UNMONI = 500;
    public static final int SERVER_WIFI_LISTEN = 501;


    public static final int WRITESNOK = 1001;
    public static final int UPDATEWIFISTATUS = 1002;
    public static final int ADDWIFISTATUS = 1003;
    public static final int TESTOK = 1005;
    public static final int TESTFAILED = 1006;

    public static final int WRITEOK = 1007;
    public static final int WRITESNFAILED = 1008;

    public static final int SERVER_CHECK_HAS_SN = 1009;
    public static final int SERVER_CHECK_HAS_NO_SN = 1010;




}
