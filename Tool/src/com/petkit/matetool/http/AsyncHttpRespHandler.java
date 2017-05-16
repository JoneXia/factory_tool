package com.petkit.matetool.http;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.petkit.android.http.ConfigCache;
import com.petkit.android.http.apiResponse.BaseRsp;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;

import cz.msebera.android.httpclient.Header;

public class AsyncHttpRespHandler extends AsyncHttpResponseHandler {

	public static final int STATUS_CODE_CACHE = 0xffff;

	private final static String TAG = "AsyncHttpRespHandler";
	private Activity context;
	private boolean showDialog;

	protected String responseResult;
	protected Gson gson = new Gson();

	@SuppressWarnings("unused")
	private String cacheFilePathString;

	public AsyncHttpRespHandler() {
		super();
		this.context = null;
		this.showDialog = false;
	}

	public AsyncHttpRespHandler(Activity baseActivity) {
		super();
		this.context = baseActivity;
		this.showDialog = false;
		init();
	}

	public AsyncHttpRespHandler(Activity baseActivity, boolean showDialog) {
		super();
		this.context = baseActivity;
		this.showDialog = showDialog;
		init();
	}

	private void init(){
		if(context == null){
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (showDialog && context != null) {
			LoadDialog.show(context, null, true, new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					AsyncHttpRespHandler.this.onCancel();
				}
			});
		}
	}

	@Override
	public void onFinish() {
		super.onFinish();

		responseResult = null;
		if (showDialog && context != null) {
			LoadDialog.dismissDialog();

		}
	}

	@Override
	public void onCancel() {
		super.onCancel();
	}

	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		responseResult = new String(responseBody);
		PetkitLog.d(TAG, "onSuccess: " + responseResult);

		if(CommonUtils.isEmpty(responseResult) || (!responseResult.startsWith("{") && !responseResult.startsWith("["))){
			BaseRsp rsp = new BaseRsp(255, context == null ? "network error" : context.getString(R.string.Hint_network_failed));
			responseResult = gson.toJson(rsp);
			return;
		}

		try {
			BaseRsp rsp = gson.fromJson(responseResult, BaseRsp.class);
			if(rsp.getError() != null){
			}
		} catch (JsonSyntaxException e) {
			BaseRsp rsp = new BaseRsp(255, context == null ? "network error" : context.getString(R.string.Hint_network_failed));
			responseResult = gson.toJson(rsp);
		}
	}

	public void onFailure(int statusCode, Header[] headers,
			byte[] responseBody, Throwable error) {
		PetkitLog.d(TAG, "statusCode:" + statusCode + ", onFailure " + error);

		if (context != null && statusCode == 0) {
			CommonUtils.showShortToast(context, R.string.Hint_network_failed);
		}
	}

	@Override
	public void readCacheMessage() {
		String responseBytes = ConfigCache.getUrlCache(cacheFilePathString);
		if(!CommonUtils.isEmpty(responseBytes)){
			sendSuccessMessage(STATUS_CODE_CACHE, null, responseBytes.getBytes());
		}
	}

	@Override
	public void writeCacheMessage(byte[] responseBody) {
		String result = new String(responseBody);
		Gson gson = new Gson();
		BaseRsp rsp = gson.fromJson(result, BaseRsp.class);
		if(rsp.getError() == null){
			ConfigCache.setUrlCache(new String(responseBody), cacheFilePathString);
		}
	}

	@Override
	public void setCacheFilePathString(String filePathString) {
		cacheFilePathString = filePathString;
	}


}
