package com.petkit.android.ble.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.orm.SugarRecord;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.http.apiResponse.DailyDetailRsp;
import com.petkit.android.http.apiResponse.ResultStringArrayRsp;
import com.petkit.android.model.DailyDetailItem;
import com.petkit.android.model.Device;
import com.petkit.android.model.Pet;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.DailyDetailUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class ActivityDataProcessService extends Service {
	
	private Pet curPet;
	private String activityDataSaveUrl, dailyDetailUrl;
	private Device deviceState;
	
	private boolean isSuccess = false;
	private boolean isNeedStoreProgress = false;
	private boolean isUserLogout = false;
	private String logoutMsg = "";

	public ActivityDataProcessService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent != null){
			deviceState = (Device) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			activityDataSaveUrl = intent.getStringExtra(BLEConsts.EXTRA_URL_DATA_SAVE);
			dailyDetailUrl = intent.getStringExtra(BLEConsts.EXTRA_URL_DAILY_DETAIL);
			curPet = (Pet) intent.getSerializableExtra(BLEConsts.EXTRA_DOG);
			isNeedStoreProgress = intent.getBooleanExtra(BLEConsts.EXTRA_BOOLEAN_STORE_PROGRESS, false);
			
			syncActivityDataToService();
		}

		return super.onStartCommand(intent, flags, startId);
	}
	
	
	protected void updateProgressNotification(int progress) {
		updateProgressNotification(progress, "");
	}
	
	protected void updateProgressNotification(int progress, String data) {
		if(isNeedStoreProgress)
			CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, progress);
		
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_PROGRESS);
		broadcast.putExtra(BLEConsts.EXTRA_DATA, data);
		broadcast.putExtra(BLEConsts.EXTRA_PROGRESS, progress);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	
	
	protected void sendErrorBroadcast(final int error) {
		if(isNeedStoreProgress)
			CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, error);
		
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_ERROR);
		broadcast.putExtra(BLEConsts.EXTRA_DATA, error & ~BLEConsts.ERROR_CONNECTION_MASK);
		broadcast.putExtra(BLEConsts.EXTRA_BOOLEAN_LOGOUT, isUserLogout);
		broadcast.putExtra(BLEConsts.EXTRA_LOG_MESSAGE, logoutMsg);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		
		LogcatStorageHelper.addLog("Unexpected error : " + BLEConsts.convertErrorCode(error));
	}

	/**
	 */
	private void syncActivityDataToService(){
		
		if(curPet == null || CommonUtils.isEmpty(activityDataSaveUrl)){
			updateProgressNotification(BLEConsts.PROGRESS_NETWORK_COMPLETED);
			return;
		}
		
		String data = FileUtils.readFileToString(new File(CommonUtils.getAppCacheActivityDataDirPath() + curPet.getId() +  "-" + Consts.TEMP_ACTIVITY_DATA_FILE_NAME));
		
		if(CommonUtils.isEmpty(data)){
			updateProgressNotification(BLEConsts.PROGRESS_NETWORK_COMPLETED);
			PetkitLog.d("syncActivityDataToService null, no data need to send");
			return;
		}
		
		updateProgressNotification(BLEConsts.PROGRESS_UPLOAD_ACTIVITY_DATA);
		isSuccess = false;
		
		final String petId = curPet.getId();
		Map<String, String> params = new HashMap<String, String>();
		params.put("petId", petId);
		params.put("data", data);
		
		if(deviceState != null && curPet != null && curPet.getDevice() != null){
			Device device = new Device();
			device.setFirmware(deviceState.getFirmware());
			device.setHardware(deviceState.getHardware());
			device.setExtra(deviceState.getExtra());
			device.setBattery(deviceState.getBattery());
			device.setId(deviceState.getId());
			Gson gson = new Gson();
			params.put("device", gson.toJson(device));

			if(deviceState.getVoltage() > 0){
				params.put("voltage", deviceState.getVoltage() + "");
			}
		}
		isUserLogout = false;
		
		AsyncHttpUtil.post(activityDataSaveUrl, params, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String responseResult = new String(responseBody);
				if(CommonUtils.isEmpty(responseResult) || (!responseResult.startsWith("{") && !responseResult.startsWith("["))){
					return;
				}
				PetkitLog.d(responseResult);
				ResultStringArrayRsp rsp = new Gson().fromJson(responseResult, ResultStringArrayRsp.class);
				if(rsp.getError() != null){
					if(rsp.getError().getCode() == 5 || rsp.getError().getCode() == 6){
						isUserLogout = true;
						logoutMsg = rsp.getError().getMsg();
					}
					LogcatStorageHelper.addLog("syncActivityDataToService petId: " + curPet.getId() + " failed: " + rsp.getError().getMsg());
				}else{
					LogcatStorageHelper.addLog("activity data upload success ");
					isSuccess = true;
					
					File tempFile = new File(CommonUtils.getAppCacheActivityDataDirPath() + petId +  "-" + Consts.TEMP_ACTIVITY_DATA_FILE_NAME);
					tempFile.delete();
					
					syncChangedActivityDailyDetail(petId, rsp.getResult());
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				LogcatStorageHelper.addLog("activity data upload failure ");
			}

			@Override
			public void onFinish() {
				super.onFinish();
				LogcatStorageHelper.addLog("activity data upload finish ");
				if(!isSuccess){
					sendErrorBroadcast(BLEConsts.ERROR_NETWORK_FAILED);
				}
			}
			@Override
			public void readCacheMessage() {
			}
			@Override
			public void setCacheFilePathString(String arg0) {
			}
			@Override
			public void writeCacheMessage(byte[] arg0) {
			}

		}, false);
		
	}
	
	/**
	 */
	private void syncChangedActivityDailyDetail(final String petId, final String[] days){
		if((days == null || days.length == 0) || CommonUtils.isEmpty(dailyDetailUrl)){
			updateProgressNotification(BLEConsts.PROGRESS_NETWORK_COMPLETED);
			return;
		}

		isUserLogout = false;
		updateProgressNotification(BLEConsts.PROGRESS_DOWNLOAD_DAILY_DETAIL);
		
		Map<String, String> params = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder(days[0]);
		for(int i = 1; i < days.length; i++){
			sb.append("," + days[i]);
		}
		params.put("version", Consts.PETKIT_VERSION_CODE);
		params.put("days", sb.toString());
		params.put("petId", petId);
		params.put("withdata", "true");
		params.put("dataFrequency", "900");
		
		AsyncHttpUtil.post(dailyDetailUrl, params, new AsyncHttpResponseHandler(){

			@Override
			public void onFinish() {
				super.onFinish();
				LogcatStorageHelper.addLog("get daily detail finish");
				if(!isSuccess){
					final String curDay = CommonUtils.getDateStringByOffset(0);
					DailyDetailItem item = DailyDetailUtils.getDailyDetailItem(petId, curDay);
					item.setlastest(false);
					SugarRecord.save(item);
					
					sendErrorBroadcast(BLEConsts.ERROR_NETWORK_FAILED);
                    LogcatStorageHelper.uploadLog();
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String responseResult = new String(responseBody);
				PetkitLog.d(responseResult);
				if(CommonUtils.isEmpty(responseResult) || (!responseResult.startsWith("{") && !responseResult.startsWith("["))){
					isSuccess = false;
					return;
				}
				
				DailyDetailRsp rsp = new Gson().fromJson(responseResult, DailyDetailRsp.class);
				
				if(rsp.getError() != null){
					if(rsp.getError().getCode() == 5 || rsp.getError().getCode() == 6){
						isUserLogout = true;
						logoutMsg = rsp.getError().getMsg();
					}
					isSuccess = false;
					LogcatStorageHelper.addLog("get daily detail failure, error: " + rsp.getError().getMsg());
				}else if(rsp.getResult() != null){
					isSuccess = true;
					DailyDetailUtils.updateDailyDetailItems(petId, rsp.getResult(), null, null);
					updateProgressNotification(BLEConsts.PROGRESS_NETWORK_COMPLETED);
				}
			}

			@Override
			public void readCacheMessage() {
			}

			@Override
			public void setCacheFilePathString(String arg0) {
			}

			@Override
			public void writeCacheMessage(byte[] arg0) {
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				LogcatStorageHelper.addLog("get daily detail failure, network error ");
				isSuccess = false;
			}
			
		}, false);
		
	}
	
}