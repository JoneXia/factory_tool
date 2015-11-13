package com.petkit.android.model;

import java.io.Serializable;

public class CategoryInfo implements Serializable{

	private static final long serialVersionUID = -5641873692546494051L;
	
	private String avatar;
	private int id;
	private String name;
	
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
