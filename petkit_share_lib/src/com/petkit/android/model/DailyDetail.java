package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class DailyDetail implements Serializable{

	private static final long serialVersionUID = -6939213770805239822L;
	
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

	private int activityConsumption;
	private int basicConsumption;
	private int deepSleep;
	private ArrayList<ArrayList<Integer>> deepSleeps;
	private int lightSleep;
	private ArrayList<ArrayList<Integer>> lightSleeps;
	private String sleepDetail;
	private int moodLevel;
	private String healthTips;
	private ArrayList<ArrayList<Integer>> rests;
	

	private int basicCalorie;
	private int activityCalorie;
	
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
	public int getDeepSleep() {
		return deepSleep;
	}
	public void setDeepSleep(int deepSleep) {
		this.deepSleep = deepSleep;
	}
	public ArrayList<ArrayList<Integer>> getDeepSleeps() {
		return deepSleeps;
	}
	public void setDeepSleeps(ArrayList<ArrayList<Integer>> deepSleeps) {
		this.deepSleeps = deepSleeps;
	}
	public int getLightSleep() {
		return lightSleep;
	}
	public void setLightSleep(int lightSleep) {
		this.lightSleep = lightSleep;
	}
	public ArrayList<ArrayList<Integer>> getLightSleeps() {
		return lightSleeps;
	}
	public void setLightSleeps(ArrayList<ArrayList<Integer>> lightSleeps) {
		this.lightSleeps = lightSleeps;
	}
	public String getSleepDetail() {
		return sleepDetail;
	}
	public void setSleepDetail(String sleepDetail) {
		this.sleepDetail = sleepDetail;
	}
	public int getMoodLevel() {
		return moodLevel;
	}
	public void setMoodLevel(int moodLevel) {
		this.moodLevel = moodLevel;
	}
	public int getActivityConsumption() {
		return activityConsumption;
	}
	public void setActivityConsumption(int activityConsumption) {
		this.activityConsumption = activityConsumption;
	}
	public int getBasicConsumption() {
		return basicConsumption;
	}
	public void setBasicConsumption(int basicConsumption) {
		this.basicConsumption = basicConsumption;
	}
	public String getHealthTips() {
		return healthTips;
	}
	public void setHealthTips(String healthTips) {
		this.healthTips = healthTips;
	}
	public int getBasicCalorie() {
		return basicCalorie;
	}
	public void setBasicCalorie(int basicCalorie) {
		this.basicCalorie = basicCalorie;
	}
	public int getActivityCalorie() {
		return activityCalorie;
	}
	public void setActivityCalorie(int activityCalorie) {
		this.activityCalorie = activityCalorie;
	}
	public ArrayList<ArrayList<Integer>> getRests() {
		return rests;
	}
	public void setRests(ArrayList<ArrayList<Integer>> rests) {
		this.rests = rests;
	}
	
	
}
