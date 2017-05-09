package com.petkit.android.model;

import java.io.Serializable;

public class Device implements Serializable{

	private static final long serialVersionUID = -7113426419398995727L;
	
	private long id;
	private int firmware = -1;
	private int hardware = -1;
	private int battery = -1;
	private Extra extra;
	private String mac;
	private String secret;
	private String secretKey;
	private float voltage;
	
	private boolean verify = false;
	private int frequence = 0;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public int getFirmware() {
		return firmware;
	}
	public void setFirmware(int firmware) {
		this.firmware = firmware;
	}
	public int getHardware() {
		return hardware;
	}
	public void setHardware(int hardware) {
		this.hardware = hardware;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public Extra getExtra() {
		return extra;
	}
	public void setExtra(Extra extra) {
		this.extra = extra;
	}
	public int getBattery() {
		return battery;
	}
	public void setBattery(int battery) {
		this.battery = battery;
	}
	public boolean isVerify() {
		return verify;
	}
	public void setVerify(boolean verify) {
		this.verify = verify;
	}
	public int getFrequence() {
		return frequence;
	}
	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}
	public float getVoltage() {
		return voltage;
	}
	public void setVoltage(float voltage) {
		this.voltage = voltage;
	}
	
}
