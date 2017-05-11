package com.petkit.matetool.http;

import com.petkit.android.http.AsyncHttpUtil;

public class ApiTools {

//	public static final String MODEL = "TEST";
	public static final String MODEL = "PRD";

	public static String SAMPLE_API_URI =
			MODEL.equals("TEST") ? "api-sandbox.petkit.com" : "api.petkit.com";

	public static String getSampleApiUri() {
		return getApiHTTPSUri();
	}

	public static String getApiHTTPSUri() {
		return String.format("https://%s/6/", SAMPLE_API_URI);
	}

	public static String getApiHTTPUri() {
		return String.format("http://%s/6/", SAMPLE_API_URI);
	}

	public static void setApiBaseUrl(){
		AsyncHttpUtil.setBaseUrl(getSampleApiUri());
	}

	public static final String PASSPORT_API_URI = 
			MODEL.equals("TEST") ? "https://api-sandbox.petkit.com/6/" : "https://api.petkit.com/6/";
	
	public static final String SAMPLE_SERVICE_UPDATE_RUI = 
			MODEL.equals("TEST") ? "https://app-sandbox.petkit.com/v1/" : "https://app.petkit.com/v1/";



}
