package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Ignore;
import com.orm.dsl.Table;


@Table(name = "DailyDetailItem")
public class DailyDetailItem extends SugarRecord implements Serializable{

	@Column(name = "dogId", unique = false, notNull = true)
	private String dogId;
	@Column(name = "day", unique = false, notNull = true)
	private String day;
	
	private int calorie;
	private int consumption;
	private int disease;
	private int estrus;
	private int health;
	private int mood;
	private int play;
	private int rank;
	private int rest;
	private int score;
	private int walk;
	
	private String food;
	private String healthDetail;
	private String moodDetail;

	/**
	 * As sugar can not save List<Integer> object, so we convert list ot json string and store it in dataString key
	 */
	@Ignore
	private List<Integer> data;
	private String dataString;
	
	private boolean isInit = false;
	
	//v4 add
	private int basicconsumption;
	private int activityconsumption;
	private int deepsleep;
	@Ignore
	private ArrayList<ArrayList<Integer>> deepsleeps;
	private String deepsleepsstring;
	private int lightsleep;
	@Ignore
	private ArrayList<ArrayList<Integer>> lightsleeps;
	private String lightsleepsstring;
	private String sleepdetail;
	private int moodlevel;
	
	private String healthtips;
	
	//v5 add
	private int basiccalorie;
	private int activitycalorie;
	
	//v6 add
	private boolean islastest = true;
	
	//v7 add 
	private int lazyrank;
	private int lazyscore;

    //v11 add, used to search
    private long dogindex;
    private long dayindex;

	public String getDogId() {
		return dogId;
	}

	public void setDogId(String dogId) {
		this.dogId = dogId;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getCalorie() {
		return calorie;
	}

	public void setCalorie(int calorie) {
		this.calorie = calorie;
	}

	public int getConsumption() {
		return consumption;
	}

	public void setConsumption(int consumption) {
		this.consumption = consumption;
	}

	public int getDisease() {
		return disease;
	}

	public void setDisease(int disease) {
		this.disease = disease;
	}

	public int getEstrus() {
		return estrus;
	}

	public void setEstrus(int estrus) {
		this.estrus = estrus;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getMood() {
		return mood;
	}

	public void setMood(int mood) {
		this.mood = mood;
	}

	public int getPlay() {
		return play;
	}

	public void setPlay(int play) {
		this.play = play;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRest() {
		return rest;
	}

	public void setRest(int rest) {
		this.rest = rest;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getWalk() {
		return walk;
	}

	public void setWalk(int walk) {
		this.walk = walk;
	}

	public String getFood() {
		return food;
	}

	public void setFood(String food) {
		this.food = food;
	}

	public String getHealthDetail() {
		return healthDetail;
	}

	public void setHealthDetail(String healthDetail) {
		this.healthDetail = healthDetail;
	}

	public String getMoodDetail() {
		return moodDetail;
	}

	public void setMoodDetail(String moodDetail) {
		this.moodDetail = moodDetail;
	}

	public List<Integer> getData() {
		return data;
	}

	public void setData(List<Integer> data) {
		this.data = data;
	}

	public String getDataString() {
		return dataString;
	}

	public void setDataString(String dataString) {
		this.dataString = dataString;
	}

	public boolean isInit() {
		return isInit;
	}

	public void setInit(boolean isInit) {
		this.isInit = isInit;
	}

	public int getDeepsleep() {
		return deepsleep;
	}

	public void setDeepsleep(int deepsleep) {
		this.deepsleep = deepsleep;
	}

	public ArrayList<ArrayList<Integer>> getDeepsleeps() {
		return deepsleeps;
	}

	public void setDeepsleeps(ArrayList<ArrayList<Integer>> deepsleeps) {
		this.deepsleeps = deepsleeps;
	}

	public String getDeepsleepsstring() {
		return deepsleepsstring;
	}

	public void setDeepsleepsstring(String deepsleepsstring) {
		this.deepsleepsstring = deepsleepsstring;
	}

	public int getLightsleep() {
		return lightsleep;
	}

	public void setLightsleep(int lightsleep) {
		this.lightsleep = lightsleep;
	}

	public ArrayList<ArrayList<Integer>> getLightsleeps() {
		return lightsleeps;
	}

	public void setLightsleeps(ArrayList<ArrayList<Integer>> lightsleeps) {
		this.lightsleeps = lightsleeps;
	}

	public String getLightsleepsstring() {
		return lightsleepsstring;
	}

	public void setLightsleepsstring(String lightsleepsstring) {
		this.lightsleepsstring = lightsleepsstring;
	}

	public String getSleepdetail() {
		return sleepdetail;
	}

	public void setSleepdetail(String sleepdetail) {
		this.sleepdetail = sleepdetail;
	}

	public int getMoodlevel() {
		return moodlevel;
	}

	public void setMoodlevel(int moodlevel) {
		this.moodlevel = moodlevel;
	}

	public int getBasicconsumption() {
		return basicconsumption;
	}

	public void setBasicconsumption(int basicconsumption) {
		this.basicconsumption = basicconsumption;
	}

	public int getActivityconsumption() {
		return activityconsumption;
	}

	public void setActivityconsumption(int activityconsumption) {
		this.activityconsumption = activityconsumption;
	}

	public String getHealthtips() {
		return healthtips;
	}

	public void setHealthtips(String healthtips) {
		this.healthtips = healthtips;
	}

	public int getBasiccalorie() {
		return basiccalorie;
	}

	public void setBasiccalorie(int basiccalorie) {
		this.basiccalorie = basiccalorie;
	}

	public int getActivitycalorie() {
		return activitycalorie;
	}

	public void setActivitycalorie(int activitycalorie) {
		this.activitycalorie = activitycalorie;
	}

	public boolean islastest() {
		return islastest;
	}

	public void setlastest(boolean isLastest) {
		this.islastest = isLastest;
	}

	public int getLazyrank() {
		return lazyrank;
	}

	public void setLazyrank(int lazyrank) {
		this.lazyrank = lazyrank;
	}

	public int getLazyscore() {
		return lazyscore;
	}

	public void setLazyscore(int lazyscore) {
		this.lazyscore = lazyscore;
	}

    public long getDogindex() {
        return dogindex;
    }

    public void setDogindex(long dogindex) {
        this.dogindex = dogindex;
    }

    public long getDayindex() {
        return dayindex;
    }

    public void setDayindex(long dayindex) {
        this.dayindex = dayindex;
    }
}
