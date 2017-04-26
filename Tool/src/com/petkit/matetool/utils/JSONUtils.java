package com.petkit.matetool.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class JSONUtils {

	public static JSONObject getJSONObject(String jsonStr) {
		JSONObject mJson = null;
		try {
			mJson = new JSONObject(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
			mJson = null;
		}
		return mJson;
	}

	public static String getValue(JSONObject mJson, String name) {
		try {
			if (!mJson.isNull(name))
				return mJson.getString(name);
			else
				return "";
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static JSONObject getJSONObject(JSONObject mJson, String name) {
		try {
			if (!mJson.isNull(name))
				return mJson.getJSONObject(name);
			else
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JSONArray getJSONArray(JSONObject mJson, String name) {
		try {
			if (!mJson.isNull(name))
				return mJson.getJSONArray(name);
			else
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * JAVA对象转换成JSON字符串
	 * 
	 * @param
	 * @return
	 */
	public static String mapToJsonStr(Map<?, ?> map) {
		JSONObject jsonObject = new JSONObject(map);
		return jsonObject.toString();
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static String arrayToJsonStr(List<?> list) {
		JSONArray jsonArray = new JSONArray(list);
		return jsonArray.toString();
	}

}