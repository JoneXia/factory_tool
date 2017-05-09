package com.petkit.android.utils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.petkit.android.model.ActivityData;
import com.petkit.android.model.DailyDetailItem;
import com.petkit.android.model.Pet;

public class DeviceActivityDataUtils {

	public static void saveChangedActivityData(StringBuffer buffer, Pet dog, int frequence){
		
		if(dog == null){
			LogcatStorageHelper.addLog("saveChangedActivityData Error! dog == null");
//			if(buffers != null && buffers.size() > 0){
//				for(StringBuffer buffer : buffers){
					LogcatStorageHelper.addLog("saveChangedActivityData Error! buffer:" + buffer.toString());
//				}
//			}
			return;
		}
		
		ArrayList<ActivityData> list = getTempActivityData(dog.getId());
		
		if(buffer == null){	// || buffers.size() == 0
			LogcatStorageHelper.addLog("saveChangedActivityData Error! buffers == null || buffers.size() == 0");
			return;
		}
		
		long curSyncTime = 0;
//		for(StringBuffer buffer : buffers){
			String[] temp = buffer.toString().split(",");
			ActivityData activityData = new ActivityData();
			if(temp[0] != null && temp[0].length() > 1){
				activityData.setTimestamp(CommonUtils.getTimestampByTime(Long.valueOf(temp[0]) * 1000));
				curSyncTime = Long.valueOf(temp[0]) * 1000 + (temp.length - 1) * frequence * 1000;
			}else{
				long lastTime = CommonUtils.getDogLastSyncTime(dog.getId());
				if(lastTime == 0){
					return;
				}
				activityData.setTimestamp(CommonUtils.getTimestampByTime(lastTime));
			}
			List<Integer> dataList = new ArrayList<Integer>();

			int i = 1;
			if(dog.getCreatedAt() != null && activityData.getTimestamp().compareTo(dog.getCreatedAt()) < 0){	//判断活动数据是否在狗狗出生以后
				long between = CommonUtils.getDaysBetweenMilis(dog.getCreatedAt(), activityData.getTimestamp());
				if(between > 0 && between < (temp.length * frequence * 1000)){
					i = (int) (between / (frequence * 1000));
					activityData.setTimestamp(CommonUtils.getTimestampByTime(Long.valueOf(temp[0]) * 1000 + between));
				}else{
					return;
				}
			}
			for(; i < temp.length; i++){
				dataList.add(Integer.valueOf(temp[i]));
			}
			activityData.setData(dataList);
			activityData.setFrequency(frequence);

			Gson gson = new Gson();
			LogcatStorageHelper.addLog("Activity data length: " + activityData.getData().size() + " data: "+ gson.toJson(activityData));
			list.add(activityData);
			saveActivityDataToDB(dog, activityData);
//		}
		
		FileUtils.writeStringToFile(CommonUtils.getAppCacheActivityDataDirPath() + dog.getId() +  "-" + Consts.TEMP_ACTIVITY_DATA_FILE_NAME, 
				gson.toJson(list));
		CommonUtils.saveDogSyncTime(dog.getId(), curSyncTime);
		
	}
	
	
	private static ArrayList<ActivityData> getTempActivityData(String petId){
		if(petId == null || petId.length() == 0){
			return new ArrayList<ActivityData>();
		}
		
		String data = FileUtils.readFileToString(new File(CommonUtils.getAppCacheActivityDataDirPath() + petId +  "-" + Consts.TEMP_ACTIVITY_DATA_FILE_NAME));
		if(data == null || data.length() == 0){
			return new ArrayList<ActivityData>();
		}
		
		Gson gson = new Gson();
		return gson.fromJson(data, new TypeToken<List<ActivityData>>(){}.getType());
	}
	
	
	private static void saveActivityDataToDB(Pet dog, ActivityData activityData){
		if(activityData == null){
			return;
		}
		
		String day = DateUtil.getFormatDate7FromString(activityData.getTimestamp());
		DailyDetailItem item = DailyDetailUtils.getDailyDetailItem(dog.getId(), day);
		int offset = getOffsetByTime(activityData.getTimestamp());
//		LogcatStorageHelper.addLog("Save: " + new Gson().toJson(activityData));
//		LogcatStorageHelper.addLog("Save day: " + day + " offset: " + offset);
//		LogcatStorageHelper.addLog("Save origin data: " + item.getDataString());
		syncActivityData(dog, day, item.getData(), offset, activityData.getData(), 0);
		item.setDataString(new Gson().toJson(item.getData()));
//		LogcatStorageHelper.addLog("Save complete data: " + item.getDataString());
		SugarRecord.save(item);
	}
	
	
	private static void syncActivityData(Pet dog, String day, List<Integer> list,
			int listOffset, List<Integer> dataStringList, int dataOffset) {

		for (int i = dataOffset; i < dataStringList.size(); i++) {
			if(listOffset >= Consts.ACTIVITY_DATA_LENGTH_PER_DAY * 90){ // activity data contains two days
				String newDay = String.valueOf(CommonUtils.getDayAfterOffset(Integer.valueOf(day), 1));
				DailyDetailItem item = DailyDetailUtils.getDailyDetailItem(dog.getId(), newDay);
				syncActivityData(dog, newDay, item.getData(), 0, dataStringList, i);
				item.setDataString(new Gson().toJson(item.getData()));
				SugarRecord.save(item);
				break;
			}else{
				int lastData = list.get(listOffset/90);
				list.set(listOffset/90, (lastData == -1 ? 0 : lastData) + dataStringList.get(i));
				listOffset++;
			}
		}
	}
	
	private static int getOffsetByTime(String timestamp){

		try {
			Date date = DateUtil.parseISO8601Date(timestamp);
			@SuppressWarnings("deprecation")
			int offset = (date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds())/10;
			return offset;
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
