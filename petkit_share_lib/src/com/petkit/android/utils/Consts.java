package com.petkit.android.utils;

public class Consts {
	
	public static final String HTTP_HEADER_SESSION = "X-Session";
	public static final String HTTP_HEADER_LOCATION = "X-Location";
	public static final String HTTP_HEADER_LOCATE_KEY = "X-Locale";
	public static final String HTTP_HEADER_TIMEZONE_KEY = "X-Timezone";
	public static final String HTTP_HEADER_IMAGE = "X-Img-Version";
	
	public static final String IMAGE_VERSION = "1";
	
	public static final int LIMIT = 20;
	public static final int GENDER_MALE = 1;
	public static final int GENDER_FEMALE = 2;
	public static final int PREGNANT_LACTATION = 2;
	public static final int PREGNANT_PREGNANCY = 1;
	public static final int MALE_YES = 1;
	public static final int MALE_NO = 2;
	public static final int FEMALE_YES =1;
	public static final int FEMALE_NO =2;
	public static final int FEMALE_LACTATION =4;
	public static final int FEMALE_PREGNANT =3;
	public static final int WEIGHT_KG =0;
	public static final int WEIGHT_POUND =1;

	public static final int ACTIVITY_DATA_LENGTH_PER_DAY = 96;
	public static final long PETKIT_DATA_SYNC_INTERVAL = 3600 * 1000 * 1L;	//1 hour
	public static final int ACTIVITY_DATA_PAGE_LENGTH = 7;
	
	public static final int DEVICE_LOW_BATTERY = 30;

	public static final String APP_CACHE_PATH = "PetkitSysCache";
	

	public static final String WeiXin_AppId = "wxa465c4d9178bd6a2";//"wxe071a657929d747f";
	public static final String WeiXin_AppSecret = "b6ef03e832825894ee49ed5516344d06";//"7bf2b9aef9372a4dabc47b759a2fbda0";

	public static final String QQZONE_AppId = "1104243077";
	public static final String QQZONE_AppSecret = "bbYlSOn4o2yQYEPZ";
	
	public static final String USER_AGENT = "app=wqbb";

	public static final String SHARED_LOGIN_RESULT = "SHARED_LOGIN_RESULT";
	public static final String SHARED_USER_ACCOUNT_INFOR = "SHARED_USER_ACCOUNT_INFOR";
	public static final String SHARED_ACTIVITY_DAYS_NEED_SYNC = "SHARED_ACTIVITY_DAY_NEED_SYNC";
	public static final String SHARED_DEFAULT_DOG_ID = "SHARED_DEFAULT_DOG_ID";
	public static final String SHARED_SESSION_ID = "SHARED_SESSION_ID";
	public static final String SHARED_USER_NAME = "SHARED_USER_NAME";
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
	public static final String SHARED_BLE_STATE	= "SHARED_BLE_STATE";
	public static final String SHARED_INFOR_COLLECT_UPDATE_TIME	= "SHARED_INFOR_COLLECT_UPDATE_TIME";
	public static final String SHARED_USER_ID	= "SHARED_USER_ID";
	public static final String SHARED_DEVICE_CONNECT_STATE	= "SHARED_DEVICE_CONNECT_STATE";
	public static final String SHARED_DEVICE_BATTERY_LOW_FLAG	= "SHARED_DEVICE_BATTERY_LOW_FLAG";

	public static final String SHARED_DEFAULT_FEED_DOG_ID = "SHARED_DEFAULT_FEED_DOG_ID";
	
	public static final String Characteristic_TYPE_CALORIE = "calorie";
	public static final String Characteristic_TYPE_MOOD = "mood";
	public static final String Characteristic_TYPE_DISEASE = "disease";
	public static final String Characteristic_TYPE_ESTRUS = "estrus";
	public static final String Characteristic_TYPE_HEALTH = "health";
	public static final String Characteristic_TYPE_REST = "rest";
	public static final String Characteristic_TYPE_CONSUME = "consumption";

	public static final String TEMP_ACTIVITY_DATA_FILE_NAME = "tempActivityData.json";
	public static final String TEMP_DAILY_DETAIL_FILE_NAME = "tempDailyDetail.json";
	

	public static final int NETWORN_NONE = 0;
	public static final int NETWORN_MOBILE = 1;
	public static final int NETWORN_WIFI = 2;
	
	
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
	public static final int DEVICE_CONNECT_STATE_OTA	= 9;
	public static final int DEVICE_CONNECT_STATE_UPLOAD_DATA	= 10;
	public static final int DEVICE_CONNECT_STATE_DOWNLOAD_COMPLETE	= 11;
	public static final int DEVICE_CONNECT_STATE_UPLOAD_FAILED		= 12;
	public static final int DEVICE_CONNECT_STATE_DOWNLOAD_FAILED	= 13;
	
	
	public static final int MESSAGE_TYPE_DOG_INFOR 		= 1;
	public static final int MESSAGE_TYPE_ACTIVITY 		= 2;
	public static final int MESSAGE_TYPE_FAVOR	 		= 3;
	public static final int MESSAGE_TYPE_COMMENT 		= 4;
	public static final int MESSAGE_TYPE_COMMENT_REPLY 	= 5;
	public static final int MESSAGE_TYPE_DOCTOR 		= 6;
	public static final int MESSAGE_TYPE_DEVICE_UPDATE 	= 7;
	public static final int MESSAGE_TYPE_CHAT 			= 8;
	
	public static final String DB_COLLECT_INFOR_LOCATION = "X-location";
	
	public static final int BLE_STATE_USING		= 1;
	public static final int BLE_STATE_NOT_USING	= 0;
	
	public static final int MSG_STATUS_SUC = 0;
	public static final int MSG_STATUS_FAIL = 1;
	public static final int MSG_STATUS_SENDING = 2;
	public static final int MSG_STATUS_NEW = 3;
	
	public static final int CACHE_TYPE_BATTERY			= 1;
	
	public static final String ANDROID_SUPPORTED_DEVICES = "android_supported_devices";
	
	public static final int PET_TYPE_DOG			= 1;
	public static final int PET_TYPE_CAT			= 2;
	
	
	
	/**  ------------------------------------ home station api ---------------------------------- */
	public static final String SHARED_HS_DEVICE_LIST			= "SHARED_HS_DEVICE_LIST";	
	public static final String PETKIT_VERSION_CODE = "5";
	
}
