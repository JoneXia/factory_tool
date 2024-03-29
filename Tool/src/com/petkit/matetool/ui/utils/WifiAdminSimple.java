package com.petkit.matetool.ui.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;

import java.util.List;

public class WifiAdminSimple {

	private final Context mContext;

	
	public WifiAdminSimple(Context context) {
		mContext = context;
	}

	public String getWifiConnectedSsid() {
		String ssid = null;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			ConnectivityManager ctm = (ConnectivityManager) mContext.
					getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = ctm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				ssid = networkInfo.getExtraInfo();
			}
		} else {
			WifiInfo mWifiInfo = getConnectionInfo();
			if (mWifiInfo != null && isWifiConnected()) {
				ssid = mWifiInfo.getSSID();
			}
		}
		if (ssid != null) {
			int len = ssid.length();
			if (ssid.startsWith("\"")
					&& ssid.endsWith("\"")) {
				ssid = ssid.substring(1, len - 1);
			}

		}
		return ssid;
	}
	
	public String getWifiConnectedBssid() {
		WifiInfo mWifiInfo = getConnectionInfo();
		String bssid = null;
		if (mWifiInfo != null && isWifiConnected()) {
			bssid = mWifiInfo.getBSSID();
		}
		return bssid;
	}

	public boolean isWifiConnected5GHz() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int freq = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			freq = wifiInfo.getFrequency();
		} else {
			String tempSsidString = wifiInfo.getSSID();
			if (tempSsidString != null && tempSsidString.length() > 2) {
				String wifiSsid = tempSsidString.substring(1, tempSsidString.length() - 1);
				List<ScanResult> scanResults=wifiManager.getScanResults();
				for(ScanResult scanResult:scanResults){
					if(scanResult.SSID.equals(wifiSsid)){
						freq = scanResult.frequency;
						break;
					}
				}
			}
		}

		return freq > 4900 && freq < 5900;
	}

	public String getCurrentApHostIp() {
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		if(wifi != null && wifi.isWifiEnabled()){
			DhcpInfo dhcpInfo = wifi.getDhcpInfo();
			return Formatter.formatIpAddress(dhcpInfo.gateway);
		}
		return null;
	}

	// get the wifi info which is "connected" in wifi-setting
	private WifiInfo getConnectionInfo() {
		WifiManager mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return wifiInfo;
	}

	private boolean isWifiConnected() {
		NetworkInfo mWiFiNetworkInfo = getWifiNetworkInfo();
		boolean isWifiConnected = false;
		if (mWiFiNetworkInfo != null) {
			isWifiConnected = true;//mWiFiNetworkInfo.isConnected();
		}
		return isWifiConnected;
	}

	private NetworkInfo getWifiNetworkInfo() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWiFiNetworkInfo;
	}
}
