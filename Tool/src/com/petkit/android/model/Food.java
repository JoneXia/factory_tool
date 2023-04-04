package com.petkit.android.model;

import java.io.Serializable;

public class Food implements Serializable{

	private static final long serialVersionUID = 4076171214272651104L;
	
	private Brand brand;
//	private int energy;
	private int id;
	private String index;
	private String name;
	private int price;
	private String weight;
	private String detail;
	private int type;
	
	public Brand getBrand() {
		return brand;
	}
	public void setBrand(Brand brand) {
		this.brand = brand;
	}
//	public int getEnergy() {
//		return energy;
//	}
//	public void setEnergy(int energy) {
//		this.energy = energy;
//	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
