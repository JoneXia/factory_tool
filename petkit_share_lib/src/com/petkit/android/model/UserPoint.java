package com.petkit.android.model;

import java.io.Serializable;

public class UserPoint implements Serializable {
	
	private static final long serialVersionUID = -4723417584515865593L;
	private String honour;
	private int rank;
	private int growth;
	private String icon;
	private String icon2;
	private int startGrowth;
	private int endGrowth;
	
	public String getHonour() {
		return honour;
	}
	public void setHonour(String honour) {
		this.honour = honour;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getGrowth() {
		return growth;
	}
	public void setGrowth(int growth) {
		this.growth = growth;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getStartGrowth() {
		return startGrowth;
	}
	public void setStartGrowth(int startGrowth) {
		this.startGrowth = startGrowth;
	}
	public int getEndGrowth() {
		return endGrowth;
	}
	public void setEndGrowth(int endGrowth) {
		this.endGrowth = endGrowth;
	}
	public String getIcon2() {
		return icon2;
	}
	public void setIcon2(String icon2) {
		this.icon2 = icon2;
	}
}
