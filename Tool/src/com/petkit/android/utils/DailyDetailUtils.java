package com.petkit.android.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.petkit.android.model.DailyDetail;
import com.petkit.android.model.DailyDetailItem;
import com.petkit.android.model.DailyDetailResult;

import static com.orm.SugarRecord.save;

public class DailyDetailUtils {

	public static DailyDetailItem getDailyDetailItem(String petId, String day){
		
		DailyDetailItem item = Select.from(DailyDetailItem.class)
									.where(Condition.prop("dogId").eq(Long.valueOf(petId)), Condition.prop("day").eq(Long.valueOf(day))).first();
		if(item == null){
			item = new DailyDetailItem();
			item.setDogId(petId);
			item.setDay(day);
            item.setDogindex(Long.valueOf(petId));
            item.setDayindex(Long.valueOf(day));
		}
		
		if(item.getDataString() != null){
			List<Integer> data = new Gson().fromJson(item.getDataString(), new TypeToken<List<Integer>>(){}.getType());
			item.setData(data);
		}else {
			List<Integer> data = new ArrayList<>();
			for(int i = 0; i < Consts.ACTIVITY_DATA_LENGTH_PER_DAY; i++){
				data.add(-1);
			}
			item.setData(data);
		}
		
		if(!CommonUtils.isEmpty(item.getLightsleepsstring())){
			ArrayList<ArrayList<Integer>> data = new Gson().fromJson(item.getLightsleepsstring(), new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType());
			item.setLightsleeps(data);
		}else {
			item.setLightsleeps(new ArrayList<ArrayList<Integer>>());
		}
		
		if(!CommonUtils.isEmpty(item.getDeepsleepsstring())){
			ArrayList<ArrayList<Integer>> data = new Gson().fromJson(item.getDeepsleepsstring(), new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType());
			item.setDeepsleeps(data);
		}else {
			item.setLightsleeps(new ArrayList<ArrayList<Integer>>());
		}
		
		return item;
	}
	
	
	public static void updateDailyDetailItems(String petId, List<DailyDetailResult> results, String since, String until){
		if(results != null){
			for (DailyDetailResult dailyDetailResult : results) {
				updateDailyDetailItem(petId, dailyDetailResult);
			}
		}
		
		if(CommonUtils.isEmpty(since) || CommonUtils.isEmpty(until)){
			return;
		}
		
		int startDay = Integer.valueOf(since);
		int endDay = Integer.valueOf(until);
		for(int i = startDay; i <= endDay; i = CommonUtils.getDayAfterOffset(i, 1)){
			DailyDetailItem item = getDailyDetailItem(petId, String.valueOf(i));
			save(item);
		}
	}
	
	
	public static void updateDailyDetailItem(String petId, DailyDetailResult result){
		if(result == null){
			return;
		}
		
		DailyDetailItem item = getDailyDetailItem(petId, String.valueOf(result.getDay()));
		DailyDetail dailyDetail = result.getDetail();
		
		if(dailyDetail == null){
			item.setInit(true);
			save(item);
			return; 
		}
		
		item.setCalorie(dailyDetail.getCalorie());
		item.setConsumption(dailyDetail.getConsumption());
		item.setDataString(new Gson().toJson(result.getData()));
		item.setDisease(dailyDetail.getDisease());
		item.setEstrus(dailyDetail.getEstrus());
		item.setFood(dailyDetail.getFood());
		item.setHealth(dailyDetail.getHealth());
		item.setHealthDetail(dailyDetail.getHealthDetail());
		item.setMood(dailyDetail.getMood());
		item.setMoodDetail(dailyDetail.getMoodDetail());
		item.setPlay(dailyDetail.getPlay());
		item.setRank(dailyDetail.getRank());
		item.setRest(dailyDetail.getRest());
		item.setScore(dailyDetail.getScore());
		item.setWalk(dailyDetail.getWalk());

		item.setInit(true);
		
		//db v4 add
		if(dailyDetail.getRests() != null && dailyDetail.getRests().size() > 0){
			dailyDetail.setLightSleeps(dailyDetail.getRests());
			dailyDetail.setDeepSleeps(new ArrayList<ArrayList<Integer>>());
		}
		
		item.setLightsleepsstring(new Gson().toJson(dailyDetail.getLightSleeps()));
		item.setDeepsleepsstring(new Gson().toJson(dailyDetail.getDeepSleeps()));
		item.setBasicconsumption(dailyDetail.getBasicConsumption());
		item.setActivityconsumption(dailyDetail.getActivityConsumption());
		item.setHealthtips(dailyDetail.getHealthTips());
		item.setDeepsleep(dailyDetail.getDeepSleep());
		item.setLightsleep(dailyDetail.getLightSleep());
		item.setSleepdetail(dailyDetail.getSleepDetail());
		item.setMoodlevel(dailyDetail.getMoodLevel());
		
		//db v5 add
		item.setBasiccalorie(dailyDetail.getBasicCalorie());
		item.setActivitycalorie(dailyDetail.getActivityCalorie());
		
		//db v6 add
		item.setlastest(true);
		
		save(item);
	}
}
