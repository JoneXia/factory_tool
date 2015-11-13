package com.petkit.android.model;

import java.io.Serializable;

public class PetType implements Serializable{

	private static final long serialVersionUID = 4015765820196940332L;
	
	private int id;
	private String name;
	
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
