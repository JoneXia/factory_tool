package com.petkit.android.model;

import java.io.Serializable;

public class Extra implements Serializable{

	private static final long serialVersionUID = -7027017999490257205L;
	
	
	private String imageType;

	
	public Extra(String imageType) {
		super();
		this.imageType = imageType;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	
	
}
