package com.petkit.android.model;

import java.util.ArrayList;

public class SupportBLEDevices {

	private String data;
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public class SupportBLEDevicesData{
		private String version;
		private String brand;
		private ArrayList<String> devices;
		
		
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getBrand() {
			return brand;
		}
		public void setBrand(String brand) {
			this.brand = brand;
		}
		public ArrayList<String> getDevices() {
			return devices;
		}
		public void setDevices(ArrayList<String> devices) {
			this.devices = devices;
		}
	}
	
	
}
