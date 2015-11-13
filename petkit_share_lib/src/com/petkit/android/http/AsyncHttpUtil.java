package com.petkit.android.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.petkit.android.utils.PetkitLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.client.HttpClient;

public class AsyncHttpUtil {

	private static AsyncHttpClient client = new AsyncHttpClient();
	private static String baseUrl;

	static{
		client.setTimeout(1000* 30);
	}
	
	public static void setBaseUrl(String baseUrl){
		AsyncHttpUtil.baseUrl = baseUrl;
	}
	
	public static String sessionID = null; 

	public static void get(String url, Map<String, String> params,
			AsyncHttpResponseHandler responseHandler) {
		get(url, params, responseHandler, false);
	}
	public static void get(String url, Map<String, String> params,
			AsyncHttpResponseHandler responseHandler, boolean isReadCache) {
		PetkitLog.d("HttpUtil", "get url --> " + getAbsoluteUrl(url));
		PetkitLog.d("HttpUtil", "params --> " + params.toString());
		client.get(getAbsoluteUrl(url), new RequestParams(params),
				responseHandler);
	}


	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		get(url, responseHandler, true);
	}
	public static void get(String url, AsyncHttpResponseHandler responseHandler, boolean isReadCache) {
		PetkitLog.d("HttpUtil", "get url --> " + getAbsoluteUrl(url));
		client.get(getAbsoluteUrl(url), responseHandler);
	}

	public static void post(String url, AsyncHttpResponseHandler responseHandler){
		post(url, responseHandler, false);
	}

	public static void post(String url, AsyncHttpResponseHandler responseHandler, boolean isReadCache) {
		PetkitLog.d("HttpUtil", "post url --> " + getAbsoluteUrl(url));
		client.post(getAbsoluteUrl(url), responseHandler, isReadCache);
	}

	public static void post(String url, Map<String, String> params,
			AsyncHttpResponseHandler responseHandler){
		post(url, params, responseHandler, false);
	}

	public static void post(String url, Map<String, String> params,
			AsyncHttpResponseHandler responseHandler, boolean isReadCache) {
		PetkitLog.d("HttpUtil", "post url --> " + getAbsoluteUrl(url));
		PetkitLog.d("HttpUtil", "params --> " + params.toString());
		client.post(getAbsoluteUrl(url), new RequestParams(params), responseHandler, isReadCache);
	}
	
	public static void cancenRequest(Context context, boolean mayInterruptIfRunning){
		client.cancelRequests(context, mayInterruptIfRunning);
	}
	
	public static void post(Context context, String url, Map<String, String> params, ResponseHandlerInterface responseHandler, boolean isReadCache){
		PetkitLog.d("HttpUtil", "get url --> " + getAbsoluteUrl(url));
		PetkitLog.d("HttpUtil", "params --> " + params.toString());
		client.post(context, getAbsoluteUrl(url), new RequestParams(params), responseHandler, isReadCache);
	}
	
	public static void post(String url, Map<String, String> params, Map<String, List<String>> files,
			AsyncHttpResponseHandler responseHandler){
		post(url, params, files, responseHandler, false);
	}

	public static void post(String url, Map<String, String> params, Map<String, List<String>> files,
			AsyncHttpResponseHandler responseHandler, boolean isReadCache) {
		PetkitLog.d("HttpUtil", "post url --> " + getAbsoluteUrl(url));
		PetkitLog.d("HttpUtil", "params --> " + params.toString());
		PetkitLog.d("HttpUtil", "files --> " + files.toString());
		RequestParams params2 = new RequestParams(params);

		for(HashMap.Entry<String, List<String>> part : files.entrySet()){
			try {
				if(part.getValue() == null || part.getValue().size() == 0){
					break;
				}
				File[] files2 = new File[part.getValue().size()];
				int i = 0;
				for(String filename : part.getValue()){
					if(filename != null){
						files2[i++] = new File(filename);
					}
				}
				params2.put(part.getKey(), files2);//, "multipart/form-data; boundary=-----------------7d4a6d158c9"
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		client.post(getAbsoluteUrl(url), params2, responseHandler, isReadCache);
//		client.post(getAbsoluteUrl(url), params2, responseHandler);
	}
	
	private static String getAbsoluteUrl(String relativeUrl) {
		if(!relativeUrl.startsWith("http://")){
			return baseUrl + relativeUrl;
		}
		return relativeUrl;
	}

	public static HttpClient getHttpClient(){
		return client.getHttpClient();
	}
	
	public static void addHttpHeader(String name, String value){
		client.addHeader(name, value);
	}
	
	public static void cancelAllRequest(){
		client.cancelAllRequests(true);
	}

	public static void setTimeout(int miniSec){
		client.setTimeout(miniSec);
	}
}
