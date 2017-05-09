package com.petkit.android.model;

import java.io.Serializable;

public class Brand implements Serializable{

	private static final long serialVersionUID = 3126226868756859708L;
	
	private String index;
	private String name;
	private String icon;
	private int id;
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
}
