package com.petkit.android.ble;

import java.io.Serializable;

public class WifiInfo implements Serializable {
	private static final long serialVersionUID = -4523460236390521750L;
	
	private byte[] ssid;
	private String capabilities;
	private String address;
	private String bssid;
	private int password;
	private int level;

	private String displayName;
    private String deviceMac;


	public WifiInfo() {
		super();
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public byte[] getSSID() {
		return ssid;
	}
	
	public void setSSID(byte[] ssid) {
		this.ssid = ssid;
	}
	
	public String getBSSID() {
		return bssid;
	}

	public void setBSSID(String bssid) {
		this.bssid = bssid;
	}
	
	public int getPassword() {
		return password;
	}

	public void setPassword(int password) {
		this.password = password;
	}
	
	public String getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }
}