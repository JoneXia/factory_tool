package com.petkit.android.model;

import java.io.Serializable;

public class Poi implements Serializable{

	private static final long serialVersionUID = 7052430310347239426L;
	
	private String poiName;
	private String poiAddress;
	private String poiLocation;

	public String getPoiName() {
		return poiName;
	}
	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}
	public String getPoiAddress() {
		return poiAddress;
	}
	public void setPoiAddress(String poiAddress) {
		this.poiAddress = poiAddress;
	}
	public String getPoiLocation() {
		return poiLocation;
	}
	public void setPoiLocation(String poiLocation) {
		this.poiLocation = poiLocation;
	}

}
