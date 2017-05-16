package com.petkit.matetool.http;

import com.petkit.android.http.AsyncHttpUtil;

public class ApiTools {

	public static final String MODEL = "TEST";
//	public static final String MODEL = "PRD";

	public static String SAMPLE_API_URI =
			MODEL.equals("TEST") ? "sandbox.food.petkit.com:/petkit-factory" : "api.petkit.com";

	public static String getSampleApiUri() {
		return getApiHTTPUri();
	}

	public static String getApiHTTPUri() {
		return String.format("http://%s", SAMPLE_API_URI);
	}

	public static void setApiBaseUrl(){
		AsyncHttpUtil.setBaseUrl(getSampleApiUri());
	}




}
