package com.petkit.android.model;

import java.util.ArrayList;

public class DailyDetailResult {

	private int day;
	private DailyDetail detail;
	private ArrayList<Integer> data;
	
	
	public DailyDetailResult(int day, DailyDetail detail,
			ArrayList<Integer> data) {
		super();
		this.day = day;
		this.detail = detail;
		this.data = data;
	}
	
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public DailyDetail getDetail() {
		return detail;
	}
	public void setDetail(DailyDetail detail) {
		this.detail = detail;
	}
	public ArrayList<Integer> getData() {
		return data;
	}
	public void setData(ArrayList<Integer> data) {
		this.data = data;
	}
}
