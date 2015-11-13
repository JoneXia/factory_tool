package com.petkit.android.model;

import java.io.Serializable;

public class Address implements Serializable{

	private static final long serialVersionUID = -8310241748813161541L;
	
	
	private String city;
	private String country;
	private String district;
	private String province;
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	
	
}
