package com.petkit.matetool.utils;

public class Consts {

    public static final String HTTP_HEADER_SESSION = "X-Session";
    public static final String HTTP_HEADER_API_VERSION = "X-Api-Version";
    public static final String HTTP_HEADER_LOCATION = "X-Location";
    public static final String HTTP_HEADER_LOCATE_KEY = "X-Locale";
    public static final String HTTP_HEADER_TIMEZONE_KEY = "X-Timezone";
    public static final String HTTP_HEADER_IMAGE = "X-Img-Version";
    public static final String HTTP_HEADER_CLIENT = "X-Client";
    public static final String HTTP_HEADER_TIMEZONE_ID_KEY = "X-TimezoneId";

    public static final String IMAGE_VERSION = "1";

    public static final int MAX_POST_IMAGE_COUNT = 9;

    public static final int LOG_LIMIT = 30;
    public static final int LIMIT = 20;
    public static final int DEFAULT_POST_FAVOR_LIST_COUNT = 8;
    public static final int MESSAGE_PAGE_COUNT = 100;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    public static final int PREGNANT_LACTATION = 2;
    public static final int PREGNANT_PREGNANCY = 1;
    public static final int MALE_YES = 1;
    public static final int MALE_NO = 2;
    public static final int FEMALE_YES = 1;
    public static final int FEMALE_NO = 2;
    public static final int FEMALE_LACTATION = 4;
    public static final int FEMALE_PREGNANT = 3;
    public static final int WEIGHT_KG = 0;
    public static final int WEIGHT_POUND = 1;
    public static final int SPORTS_LAZY = 1;
    public static final int SPORTS_NORMAL = 2;
    public static final int SPORTS_ACTIVE = 3;
    public static final int EMOTION_LAD = 1;
    public static final int EMOTION_FIRST_LOVER = 2;
    public static final int EMOTION_EXPERIENCED_LOVER = 3;
    public static final int EMOTION_PROFESSIONAL_PRARENT = 4;

    public static final int ACTIVITY_DATA_LENGTH_PER_DAY = 96;
    public static final long PETKIT_DATA_SYNC_INTERVAL = 3600 * 1000 * 1L;    //1 hour
    public static final int ACTIVITY_DATA_PAGE_LENGTH = 7;

    public static final int DEVICE_LOW_BATTERY = 30;

    public static final String APP_CACHE_PATH = "PetkitSysCache";

    public static final String UDESK_DOMAIN = "petkit.udesk.cn";
    public static final String UDESK_TEST_APP_ID = "ff2c5b6803390d44";
    public static final String UDESK_TEST_APP_KEY = "596d70e418e70ab7750022caf1d59fdc";

    public static final String UDESK_APP_ID = "b2082bfe608f2f91";
    public static final String UDESK_APP_KEY = "610375fb7839a1ef6e8bb8d9d18236e8";


    public static final String WeiXin_AppId = "wxa465c4d9178bd6a2";//"wxe071a657929d747f";
    public static final String WeiXin_AppSecret = "b6ef03e832825894ee49ed5516344d06";//"7bf2b9aef9372a4dabc47b759a2fbda0";

    public static final String QQZONE_AppId = "1104243077";
    public static final String QQZONE_AppSecret = "bbYlSOn4o2yQYEPZ";
    //阿里云号码认证服务（一键登录）
    public static final String AUTH_SECRET = "ps3oEmZ2WDFT88VMZUQ3ItQUv3X8jZUl60NW5DelmurCmFKVic1AWpzG7MF7ACHsoaw2G/W0g7pPSD8SVnJTfV8jdz1EwjqJgNXEXZ1k1/MGiyDU9Y7mn/L7qiJVgjutSrqeXklqFGwk/W6pm19U6sBXz4BDTZg8GV1MM7bmeXHnzdMJ5tRWpjrvO90v7WrN2SGJZSu2bOSw2Gv7hASDsvUutmhvX5ZF6IJSFlyRT6Tuez1MptT93I2ZArv9yawBIMTB+95dwFbUFDgTV3Y9tyNoLX1kmcR4ObrEoLcYW+Nn55BTnPFiUQ==";


    //	推送相关
    public static final int TIM_APPID_TEST = 1400411964;//测试
    public static final int MI_PUSH_ID_TEST = 12171;
    public static final int HW_PUSH_ID_TEST = 12172;
    public static final int OPPO_PUSH_ID_TEST = 12174;
    public static final long VIVO_PUSH_ID_TEST = 12173;

    public static final long VIVO_PUSH_ID = 11065;
    public static final int OPPO_PUSH_ID = 6471;
    public static final int MI_PUSH_ID = 4738;
    public static final int HW_PUSH_ID = 4739;


    public static final int TIM_APPID = 1400167556;
    public static final int TIM_TYPE = 36862;
    //萤石云
    public static final String EZ_DEVICE_APPKEY = "80c4261ee1094de4b03b3e0d4c9bf605";

    public static final String UMENG_APPKEY = "53da266056240b1d66002fd4";
    public static final String UMENG_MESSAGE_SECRET = "bc715e33e1efb2b99d9507aa65ae9613";

    public static final String MIPUSH_APPID = "2882303761517307397";
    public static final String MIPUSH_APPKEY = "5161730751397";

    public static final String OPPOPUSH_APPID = "2340839";
    public static final String OPPOPUSH_APPKEY = "3rkK5E09jcG0oWg0cC88goGKW";
    public static final String OPPOPUSH_APPSECRET = "075A1aBf02410bb58574596b7D152A42";

    public static final String VIVO_PUSH_APPID_TEST = "15691";
    public static final String VIVO_PUSH_APPID = "11065";
    public static final String VIVO_PUSH_APPKEY = "ffc6d8c3-0856-4a95-930b-662a766569b8";

    public static final String WEIBO_AppId = "2256396934";
    public static final String WEIBO_AppSecret = "033e04fd159d1ea6d784b8286f86d42b";

    public static final String USER_AGENT = "app=wqbb";
    public static final String USER_HASPASSWORD = "haspassword";

    public static final String SHARED_LOGIN_RESULT = "SHARED_LOGIN_RESULT";
    public static final String SHARED_USER_ACCOUNT_INFOR = "SHARED_USER_ACCOUNT_INFOR";
    public static final String SHARED_USER_ACCOUNT_INFOR_NEW = "SHARED_USER_ACCOUNT_INFOR_NEW";
    public static final String SHARED_ACTIVITY_DAYS_NEED_SYNC = "SHARED_ACTIVITY_DAY_NEED_SYNC";
    public static final String SHARED_DEFAULT_DOG_ID = "SHARED_DEFAULT_DOG_ID";
    public static final String SHARED_SESSION_ID = "SHARED_SESSION_ID";
    public static final String SHARED_USER_NAME = "SHARED_USER_NAME";
    public static final String LOGIN_PASSWORD_FLAG = "LOGIN_PASSWORD_FLAG";
    public static final String SHARED_USER_NAME_LIST = "SHARED_USER_NAME_LIST";
    public static final String SHARED_LAST_SYNC_TIME = "SHARED_LAST_SYNC_TIME";
    public static final String SHARED_COLLECTS_ID = "SHARED_COLLECTS_ID";
    public static final String SHARED_MESSAGE_LIST = "SHARED_MESSAGE_LIST";
    public static final String SHARED_NEW_MESSAGE_LIST = "SHARED_NEW_MESSAGE_LIST";
    public static final String SHARED_DEVICE_UPDATE_MESSAGE_LIST = "SHARED_DEVICE_UPDATE_MESSAGE_LIST";
    public static final String SHARED_NEW_DOCTOR_MESSAGE_COUNT = "SHARED_NEW_DOCTOR_MESSAGE_COUNT";
    public static final String SHARED_NEW_FAVOR_MESSAGE_COUNT = "SHARED_NEW_FAVOR_MESSAGE_COUNT";
    public static final String SHARED_NEW_COMMENT_MESSAGE_COUNT = "SHARED_NEW_COMMENT_MESSAGE_COUNT";
    public static final String SHARED_NEW_SYSTEM_MESSAGE_COUNT = "SHARED_NEW_SYSTEM_MESSAGE_COUNT";
    public static final String SHARED_NEW_CHAT_MESSAGE_COUNT = "SHARED_NEW_CHAT_MESSAGE_COUNT";
    public static final String SHARED_FIRST_INIT_FLAG = "SHARED_FIRST_INIT_FLAG";
    public static final String SHARED_DOG_CHARACTERISTIC = "SHARED_DOG_CHARACTERISTIC";
    public static final String SHARED_DOG_CHARACTERISTIC_UPDATE_TIME = "SHARED_DOG_CHARACTERISTIC_UPDATE_TIME";
    public static final String SHARED_CURRENT_APP_VERSION = "SHARED_CURRENT_APP_VERSION";
    public static final String SHARED_ANDROID_DEVICE_LIST = "SHARED_ANDROID_DEVICE_LIST";
    public static final String SHARED_SYSTEM_TIME_VALID_STATE = "SHARED_SYSTEM_TIME_VALID_STATE";
    public static final String SHARED_OLD_VERSION = "SHARED_OLD_VERSION";
    public static final String SHARED_DEFAULT_TAG_ID = "SHARED_DEFAULT_TAG_ID";
    public static final String SHARED_BLE_STATE = "SHARED_BLE_STATE";
    public static final String SHARED_INFOR_COLLECT_UPDATE_TIME = "SHARED_INFOR_COLLECT_UPDATE_TIME";
    public static final String SHARED_USER_ID = "SHARED_USER_ID";
    public static final String SHARED_DEVICE_CONNECT_STATE = "SHARED_DEVICE_CONNECT_STATE";
    public static final String SHARED_DEVICE_BATTERY_LOW_FLAG = "SHARED_DEVICE_BATTERY_LOW_FLAG";
    public static final String SHARED_POST_BLOCKED_ID_LIST = "SHARED_POST_BLOCKED_ID_LIST";

    public static final String SHARED_DEFAULT_FEED_DOG_ID = "SHARED_DEFAULT_FEED_DOG_ID";
    public static final String SHARED_EMOTION_GROUP_RESULT = "SHARED_EMOTION_GROUP_RESULT";

    public static final String SHARED_SETTING_LANGUAGE = "SHARED_SETTING_LANGUAGE";

    public static final String Characteristic_TYPE_CALORIE = "calorie";
    public static final String Characteristic_TYPE_MOOD = "mood";
    public static final String Characteristic_TYPE_DISEASE = "disease";
    public static final String Characteristic_TYPE_ESTRUS = "estrus";
    public static final String Characteristic_TYPE_HEALTH = "health";
    public static final String Characteristic_TYPE_REST = "rest";
    public static final String Characteristic_TYPE_CONSUME = "consumption";

    public static final String TEMP_ACTIVITY_DATA_FILE_NAME = "tempActivityData.json";
    public static final String TEMP_DAILY_DETAIL_FILE_NAME = "tempDailyDetail.json";


    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;


    public static final String IMAGE_STYLE_AVATAR = "@!120-120";
    public static final String IMAGE_STYLE_SMALL = "@!shortedge200";
    public static final String IMAGE_STYLE_MIDDLE = "@!shortedge400";
    public static final String IMAGE_STYLE_LARGE = "";

    public static final String IMAGE_STYLE_OLD_AVATAR = "!120.120";
    public static final String IMAGE_STYLE_OLD_SMALL = "!shortedge200";
    public static final String IMAGE_STYLE_OLD_MIDDLE = "!shortedge400";
    public static final String IMAGE_STYLE_OLD_LARGE = "";

    public static final String WEB_URL_HTTP_HEADER = "http://";

    public static final int XMPPMessageTypeMessage = 0;
    public static final int XMPPMessageTypeImage = 1;
    public static final int XMPPMessageTypeAudio = 2;
    public static final int XMPPMessageTypeVideo = 3;
    public static final int XMPPMessageTypeDogHistoryData = 4;


    public static final int DEVICE_CONNECT_STATE_NONE = -1;
    public static final int DEVICE_CONNECT_STATE_SCANING = 0;
    public static final int DEVICE_CONNECT_STATE_CONNECTING = 1;
    public static final int DEVICE_CONNECT_STATE_SYNC = 2;
    public static final int DEVICE_CONNECT_STATE_SYNCTIMEOUT = 7;
    public static final int DEVICE_CONNECT_STATE_COMPELTE = 3;
    public static final int DEVICE_CONNECT_STATE_SCAN_FAIL = 4;
    public static final int DEVICE_CONNECT_STATE_CONNECT_FAIL = 5;
    public static final int DEVICE_CONNECT_STATE_CONNECT_SUCCESS = 6;
    public static final int DEVICE_CONNECT_STATE_SCAN_TIMEOUT = 8;
    public static final int DEVICE_CONNECT_STATE_OTA = 9;
    public static final int DEVICE_CONNECT_STATE_UPLOAD_DATA = 10;
    public static final int DEVICE_CONNECT_STATE_DOWNLOAD_COMPLETE = 11;
    public static final int DEVICE_CONNECT_STATE_UPLOAD_FAILED = 12;
    public static final int DEVICE_CONNECT_STATE_DOWNLOAD_FAILED = 13;


    public static final int MESSAGE_TYPE_DOG_INFOR = 1;
    public static final int MESSAGE_TYPE_ACTIVITY = 2;
    public static final int MESSAGE_TYPE_FAVOR = 3;
    public static final int MESSAGE_TYPE_COMMENT = 4;
    public static final int MESSAGE_TYPE_COMMENT_REPLY = 5;
    public static final int MESSAGE_TYPE_DOCTOR = 6;
    public static final int MESSAGE_TYPE_DEVICE_UPDATE = 7;
    public static final int MESSAGE_TYPE_CHAT = 8;

    public static final String DB_COLLECT_INFOR_LOCATION = "X-location";

    public static final int BLE_STATE_USING = 1;
    public static final int BLE_STATE_NOT_USING = 0;

    public static final int MSG_STATUS_SUC = 0;
    public static final int MSG_STATUS_FAIL = 1;
    public static final int MSG_STATUS_SENDING = 2;
    public static final int MSG_STATUS_NEW = 3;

    public static final int MSG_TIM_SOURCE = 1;

    public static final int CACHE_TYPE_BATTERY = 1;

    public static final String ANDROID_SUPPORTED_DEVICES = "android_supported_devices";

    public static final int PET_TYPE_NULL = 0;
    public static final int PET_TYPE_DOG = 1;
    public static final int PET_TYPE_CAT = 2;

    public static final String MATE_GET_WIFI_STEP_KEY = "get_wifi_step";
    public static final int MATE_GET_WIFI_STEP_ONE = 1;
    public static final int MATE_GET_WIFI_STEP_TWO = 2;
    /**
     * ------------------------------------ home station api ----------------------------------
     */
    public static final String SHARED_HS_DEVICE_LIST = "SHARED_HS_DEVICE_LIST";
    public static final String PETKIT_VERSION_CODE = "5";

    public static final int WIFI_CONFIG_SUCCESS = 0;
    public static final int WIFI_CONFIG_FAIL = 1;

    public static final int NETWORK_CLOSE = 0;
    public static final int NETWORK_WIFI_ONLY = 1;
    public static final int NETWORK_WIFI_AND_DATA = 2;

    public static final int VIDEO_SOURCE_CAPTURE = 0;
    public static final int VIDEO_SOURCE_IMPORT = 1;

    public static final int LOCATION_MYSELFFRAGMENT = 0;
    public static final int LOCATION_PERSONALACTIVITY = 1;

    public static final int FRAGMENT_REGISTERTYPE_1 = 1;
    public static final int FRAGMENT_REGISTERTYPE_2 = 2;
    public static final int FRAGMENT_CATEGORY = 3;
    public static final int FRAGMENT_BIRTHDAY = 4;
    public static final int FRAGMENT_GENDER = 5;
    public static final int FRAGMENT_WEIGHT = 6;

    public static final int FRAGMENT_ACTIVITY = 7;
    public static final int FRAGMENT_FOOD_SCAN = 8;
    public static final int FRAGMENT_FOOD_LIST = 9;
    public static final int FRAGMENT_FOOD_BRAND = 10;
    public static final int FRAGMENT_FOOD_SCAN_RESULT = 11;

    public static final String PET_NAME = "name";
    public static final String PET_GENDER = "gender";
    public static final String PET_CATEGORY = "category";
    public static final String PET_WEIGHT = "weight";
    public static final String PET_BIRTH = "birth";
    public static final String PET_FOOD = "food";
    public static final String PET_PRIVATE_FOOD = "privateFood";
    public static final String PET_MALE_STATE = "maleState";
    public static final String PET_FEMALE_STATE = "femaleState";
    public static final String PET_PREGNANT_STATE = "pregnantStart";
    public static final String PET_LACTATION_STATE = "lactationStart";
    public static final String PET_ACTIVEDEGREE = "activeDegree";
    public static final String PET_TYPE = "type";
    public static final String PET_SIZE = "size";
    public static final String PET_AVATAR = "avatar";
    public static final String PET_GROUP_ID = "groupId";

    public static final int VIEW_FROM_ACCOUNT_FIRST_REGISTER_PET = 1;  //用户注册完善信息后直接第一次进入添加宠物界面
    public static final int BIND_DEVICE_FIT = 1;
    public static final int BIND_DEVICE_MATE = 2;
    public static final int BIND_DEVICE_GO = 3;
    public static final int BIND_DEVICE_FEEDER = 4;
    public static final int BIND_DEVICE_COZY = 5;
    public static final int BIND_DEVICE_D2 = 6;
    public static final int BIND_DEVICE_T3 = 7;
    public static final int BIND_DEVICE_K2 = 8;
    public static final int BIND_DEVICE_D3 = 9;
    public static final int BIND_DEVICE_AQ = 10;
    public static final int BIND_DEVICE_AQ1S = 101; //与其他设备类型不同，仅用于扫描时返回过滤Aq或Aq1s
    public static final int BIND_DEVICE_D4 = 11;
    public static final int BIND_DEVICE_P3 = 12;
    public static final int BIND_DEVICE_H2 = 13;
    public static final int BIND_DEVICE_W5 = 14;
    public static final int BIND_DEVICE_AQR = 17;
    public static final int BIND_DEVICE_T4 = 15;
    public static final int BIND_DEVICE_K3 = 16;
    public static final int BIND_DEVICE_R2 = 18;
    public static final int BIND_DEVICE_AQH1 = 19;
    public static final int BIND_DEVICE_D4S = 20;
    public static final int BIND_DEVICE_HG = 22;
    public static final int BIND_DEVICE_D4SH = 25;
    public static final int BIND_DEVICE_D4H = 26;


    public static final String FEEDER_HOME_IS_FIRST = "FEEDER_HOME_IS_FIRST";
    public static final String D2_HOME_IS_FIRST = "D2_HOME_IS_FIRST";
    public static final String K2_HOME_IS_FIRST = "K2_HOME_IS_FIRST";
    public static final String T3_HOME_IS_FIRST = "T3_HOME_IS_FIRST";
    public static final String AQ_HOME_IS_FIRST = "AQ_HOME_IS_FIRST";
    public static final String D3_HOME_IS_FIRST = "D3_HOME_IS_FIRST";
    public static final String W5_HOME_IS_FIRST = "W5_HOME_IS_FIRST";
    public static final String AQH1_HOME_IS_FIRST = "AQH1_HOME_IS_FIRST";
    public static final String AQH1_AI_IS_FIRST = "AQH1_AI_IS_FIRST";

    public static final String AQ_SPOTLIGHT_IS_FIRST = "AQ_SPOTLIGHT_IS_FIRST";

    public static final String SPLASH_IMAGE_DATA = "SPLASH_IMAGE_DATA";
    public static final String SPLASH_IMAGE_COUNT = "SPLASH_IMAGE_COUNT";
    public static final String SPLASH_IMAGE_CACHE = "SPLASH_IMAGE_CACHE";

    public static final String WIFI_SSID_COZY_HEADER = "PETKIT_COZY_HW1_";
    public static final String WIFI_SSID_D1_HEADER = "PETKIT_AP_";
    public static final String WIFI_SSID_D2_HEADER = "PETKIT_FEEDER_HW2_";

    public static final String UNREAD_MSG_COUNT = "UNREAD_MSG_COUNT";
    public static final String NAVIGATION_BAR_STATUS = "NAVIGATION_BAR_STATUS";

    public static final String NOTIFICATION_IS_SHOW = "NOTIFICATION_IS_SHOW";

    public static final String D3_LAST_FEED_AMOUNT = "D3_LAST_FEED_AMOUNT";
    public static final String D4_LAST_FEED_AMOUNT = "D4_LAST_FEED_AMOUNT";

    public static final String UPDATE_DEVICES_USER = "UPDATE_DEVICES_USER";

    public static final String D3_HOME_TIP = "D3_HOME_TIP";

    public static final String H2_TOKEN = "H2_TOKEN";

    public static final String CHANGE_WATER_REMIND_TYPE = "CHANGE_WATER_REMIND_TYPE";
    //定时换水提醒Type
    public static final int REMIND_TYPE_CHANGE_WATER = 1;

    public static final String IS_CLOSE_BANNER = "IS_CLOSE_BANNER";

    public static final String LAST_HOST_TYPE = "LAST_HOST_TYPE";


    public static final int BIND_PET_NORMAL = 1;
    public static final int BIND_PET_ADD = 2;

    public static final String K3_ALREADY_SHOW_BIND_T4_WINDOW = "K3_ALREADY_SHOW_BIND_T4_WINDOW";
    public static final String T4_ALREADY_SHOW_BIND_K3_WINDOW = "T4_ALREADY_SHOW_BIND_K3_WINDOW";


    public static final String WRITING_FEED = "WRITING_FEED";
    public static final String WRITING_FEED_PART = "WRITING_FEED_PART";
    public static final String WRITING_EAT = "WRITING_EAT";
    public static final String WRITING_EAT_COUNT = "WRITING_EAT_COUNT";
    public static final String WRITING_TOILET = "WRITING_TOILET";
    public static final String WRITING_SLEEP = "WRITING_SLEEP";
    public static final String WRITING_ACTIVITY = "WRITING_ACTIVITY";
    public static final String WRITING_PET_LOG = "WRITING_PET_LOG";
    public static final String WRITING_WEIGHT = "WRITING_WEIGHT";
    public static final String WRITING_WALK_PET = "WRITING_WALK_PET";

    public static final String FAMILY_INFOR = "FAMILY_INFOR";
    public static final String CURRENT_FAMILY_ID = "CURRENT_FAMILY_ID";
    public static final String FAMILY_LIST = "FAMILY_LIST";

    public static final String SP_DEVICE_ROSTER = "SP_DEVICE_ROSTER";
    public static final String SP_DEVICE_ROSTER_IS_FIRST = "SP_DEVICE_ROSTER_IS_FIRST";

    public static final String GUIDE_HOME_FIRST = "GUIDE_HOME_FIRST";
    public static final String GUIDE_HOME_TODO = "GUIDE_HOME_TODO";

    public static final String AQR_HOME_IS_FIRST = "AQR_HOME_IS_FIRST";
    public static final String HOME_SHOW_UNAVAILABLE_DEVICE_TIP = "HOME_SHOW_UNAVAILABLE_DEVICE_TIP";
    public static final String HOME_TO_DEVICE_PAGE_DEVICE_ID = "HOME_TO_DEVICE_PAGE_DEVICE_ID";

    public static final String T3_RECORD_TIP_IS_FIRST = "T3_RECORD_TIP_IS_FIRST";
    public static final String T3_NEW_FUNCTION_TIP_IS_FIRST = "T3_NEW_FUNCTION_TIP_IS_FIRST";
    public static final String T4_RECORD_TIP_IS_FIRST = "T4_RECORD_TIP_IS_FIRST";
    public static final String T4_NEW_FUNCTION_TIP_IS_FIRST = "T4_NEW_FUNCTION_TIP_IS_FIRST";
    public static final String T4_BLUE_POINT_IS_FIRST = "T4_BLUE_POINT_IS_FIRST";
    public static final String T3_BLUE_POINT_IS_FIRST = "T3_BLUE_POINT_IS_FIRST";

    public static final String D3_HOME_GUIDE_PAGE_IS_FIRST = "D3_HOME_GUIDE_PAGE_IS_FIRST";
    public static final String D3_REMOVE_RECORD_GUIDE_PAGE_SHOWED = "D3_REMOVE_RECORD_GUIDE_PAGE_SHOWED";
    public static final String D3_HOME_ADJUST_TIP = "D3_HOME_ADJUST_TIP";
    public static final String D3_SETTING_VOICE = "D3_SETTING_VOICE";
    public static final String D3_HOME_SET_TIP = "D3_HOME_SET_TIP";

    public static final String D4S_EAT_RECORD_IS_FIRST = "D4S_EAT_RECORD_IS_FIRST";

    public static final int APP_WIDGET_REFRESH_LOGIN = -1;

    public static final String D3_SURPLUS_GRAIN_CONTROL = "D3_SURPLUS_GRAIN_CONTROL";

    public static final String MALL_YZ_INFO = "MALL_YZ_INFO";
    public static final String MALL_YZ_CONSUMABLES = "https://h5.youzan.com/wscshop/category/RGXKw9YkUo";

    public static final String PURCHASE_ENTRANCE_DATA = "PURCHASE_ENTRANCE_DATA";


    public static final int FISH_NORMAL = 1;
    public static final int FISH_ADD = 2;

    public static final int MEMBER_NORMAL = 1;
    public static final int MEMBER_ADD = 2;

    public static final int D4S_FEED_ADAPTER = 1;
    public static final int D3_D4_FEED_ADAPTER = 2;
    public static final int D1_D2_FEED_ADAPTER = 3;

    public static final String D4_TAG = "D4_TAG";

    public static final String T4_DEODORIZATION_TAG = "T4_DEODORIZATION_TAG";
    public static final String T4_DEODORIZATION_INLET_ISVISIBLE = "T4_DEODORIZATION_INLET_ISVISIBLE";
    public static final String T4_LITTER_INLET_ISVISIBLE = "T4_LITTER_INLET_ISVISIBLE";
    public static final String T4_BOX_INLET_ISVISIBLE = "T4_BOX_INLET_ISVISIBLE";
    public static final String T4_K3_INLET_ISVISIBLE = "T4_K3_INLET_ISVISIBLE";
    public static final String T3_LITTER_INLET_ISVISIBLE = "T3_LITTER_INLET_ISVISIBLE";
    public static final String T3_BOX_INLET_ISVISIBLE = "T3_BOX_INLET_ISVISIBLE";
    public static final String T3_LIQUID_INLET_ISVISIBLE = "T3_LIQUID_INLET_ISVISIBLE";

    public static final String HG_GIF_URL = "HG_GIF_URL";


    public static final String W5_BLE_CONNECT_GUIDE_SHOWED = "W5_BLE_CONNECT_GUIDE_SHOWED";


    public static final String FEED_PLAN_GUIDE_ONE = "FEED_PLAN_GUIDE_ONE";
    public static final String FEED_PLAN_GUIDE_THREE = "FEED_PLAN_GUIDE_THREE";

    public static final String DEVICE_FEED_D4SH_LAST_AMOUNT1 = "DEVICE_FEED_D4SH_LAST_AMOUNT1";
    public static final String DEVICE_FEED_D4SH_LAST_AMOUNT2 = "DEVICE_FEED_D4SH_LAST_AMOUNT2";
    public static final String DEVICE_FEED_D4S_LAST_AMOUNT1 = "DEVICE_FEED_D4S_LAST_AMOUNT1";
    public static final String DEVICE_FEED_D4S_LAST_AMOUNT2 = "DEVICE_FEED_D4S_LAST_AMOUNT2";
    public static final String DEVICE_FEED_D4_LAST_AMOUNT = "DEVICE_FEED_D4_LAST_AMOUNT";
    public static final String DEVICE_FEED_D3_LAST_AMOUNT = "DEVICE_FEED_D3_LAST_AMOUNT";
    public static final String DEVICE_FEED_D2_LAST_AMOUNT = "DEVICE_FEED_D2_LAST_AMOUNT";
    public static final String DEVICE_FEED_D1_LAST_AMOUNT = "DEVICE_FEED_D1_LAST_AMOUNT";

    public static final String DEVICE_ADD_MEAL_NOW_D4S_LAST_AMOUNT1 = "DEVICE_ADD_MEAL_NOW_D4S_LAST_AMOUNT1";
    public static final String DEVICE_ADD_MEAL_NOW_D4S_LAST_AMOUNT2 = "DEVICE_ADD_MEAL_NOW_D4S_LAST_AMOUNT2";

    //显示T4维护模式引导标志位，false为需要显示，true为不需要显示
    public static final String T4_SHOW_MAINTENANCE_MODE_GUIDE_FLAG = "T4_SHOW_MAINTENANCE_MODE_GUIDE_FLAG";

    //第一次使用维护模式标志位，false为第一次使用，true为非第一次使用
    public static final String T4_MAINTENANCE_MODE_USED_FLAG = "T4_MAINTENANCE_MODE_USED_FLAG";
    public static final int T4_MAINTENANCE_MODE_REQUEST_CODE = 100;
    public static final int T4_MAINTENANCE_MODE_RESPONSE_CODE = 101;
    public static final String EXTRA_BOOLEAN_PARAM_1 = "EXTRA_BOOLEAN_PARAM_1";

    public static final String GUIDE_HOME_TODO_CARD = "GUIDE_HOME_TODO_CARD";
    public static final int T4_EMPTY_CAT_LITTER_REQUEST_CODE = 102;
    public static final int T4_EMPTY_CAT_LITTER_RESPONSE_CODE = 103;

    public static final String FEEDBACK_TIP_IS_FIRST = "FEEDBACK_TIP_IS_FIRST";

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_W5C = 1.26;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_W4X_W5N = 2.43;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_T4 = 1.407;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_T3 = 1.439;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_D3 = 1.443;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_D4 = 1.234;

    public static final double MULTIPLE_TIME_PERIODS_FIRMWARE_D4S = 1.149;

    public static final int HG_MIN_CENTIFRADE_TEMPERATURE = 360; //十倍数

    public static final int HG_MAX_CENTIFRADE_TEMPERATURE = 400; //十倍数

    public static final int HG_MIN_FAHRENHEIT_TEMPERATURE = 970; //十倍数

    public static final int HG_MAX_FAHRENHEIT_TEMPERATURE = 1040; //十倍数


    public static final String SP_DEVICE_SERVER_CONFIGS = "SP_DEVICE_SERVER_CONFIGS";

    public static final double T3_FIRMWARE_SUPPORT_RESET_LIQUIT = 1.445;

    public static final int FAMILY_TYPE_FAMILY = 1;
    public static final int FAMILY_TYPE_PET = 2;
    public static final int FAMILY_TYPE_DEVICE = 3;


}
