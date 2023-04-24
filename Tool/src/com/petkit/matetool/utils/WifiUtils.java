package com.petkit.matetool.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

public class WifiUtils {
	// 上下文Context对象
	private Context mContext;
	// WifiManager对象
	private WifiManager mWifiManager;


	public interface iWifiStateListener {
		void onAvailable();

		void onUnavailable();

		void onLosing(@NonNull Network network, int maxMsToLive);
	}

	public WifiUtils(Context mContext) {
		this.mContext = mContext;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
	}


	/**
	 * 搜索附近的热点信息，并返回所有热点为信息的SSID集合数据
	 */
	public List<ScanResult> getScanWifiResults() {
		if (mWifiManager.getWifiState() == WIFI_STATE_DISABLING
				|| mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			mWifiManager.setWifiEnabled(true);
		}
		// 开始扫描热点
		mWifiManager.disconnect();
		mWifiManager.reconnect();

		mWifiManager.startScan();
		return mWifiManager.getScanResults();
	}


	public void connectToWifi(String ssid, String password, final iWifiStateListener listener) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// 获取 WifiNetworkSpecifier.Builder 实例
			WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();

			// 创建一个 WifiNetworkSuggestion.Builder 实例
			WifiNetworkSuggestion.Builder suggestionBuilder = new WifiNetworkSuggestion.Builder();

			// 设置 WiFi 网络 SSID 和密码
			suggestionBuilder.setSsid(ssid);
			suggestionBuilder.setWpa2Passphrase(password);

			// 创建一个 WifiNetworkSpecifier 实例
			WifiNetworkSpecifier wifiNetworkSpecifier = builder.setSsid(ssid)
					.setWpa2Passphrase(password)
					.build();

			// 创建一个 WifiNetworkSuggestion 实例
			WifiNetworkSuggestion wifiNetworkSuggestion = suggestionBuilder
					.setPriority(1)
					.setIsAppInteractionRequired(true)
					.build();

			mWifiManager.addNetworkSuggestions(Collections.singletonList(wifiNetworkSuggestion));

			// 设置网络建议
			NetworkRequest networkRequest = new NetworkRequest.Builder()
					.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
					.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
					.setNetworkSpecifier(wifiNetworkSpecifier)
					.build();

			// 连接 WiFi 网络
			ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
				@Override
				public void onAvailable(@NonNull Network network) {
					super.onAvailable(network);
					connectivityManager.bindProcessToNetwork(network);
					listener.onAvailable();
				}

				@Override
				public void onUnavailable() {
					super.onUnavailable();

					listener.onUnavailable();
				}

				@Override
				public void onLosing(@NonNull Network network, int maxMsToLive) {
					super.onLosing(network, maxMsToLive);

					listener.onLosing(network, maxMsToLive);
				}
			});
		} else {

			new Thread() {
				@Override
				public void run() {
					super.run();
					// 创建一个 WifiConfiguration 对象
					WifiConfiguration wifiConfig = new WifiConfiguration();

					// 设置 WiFi 网络 SSID 和密码
					wifiConfig.SSID = "\"" + ssid + "\"";
					wifiConfig.preSharedKey = "\"" + password + "\"";

					// 将 WiFi 网络连接优先级设置为最高
					wifiConfig.priority = 1;

					// 添加 WiFi 网络配置
					int networkId = mWifiManager.addNetwork(wifiConfig);

					// 连接到指定的 WiFi 网络
					mWifiManager.disconnect();
					boolean result = mWifiManager.enableNetwork(networkId, true);
					mWifiManager.reconnect();

					if (result) {
						listener.onAvailable();
					} else {
						listener.onUnavailable();
					}
				}
			}.start();


		}
	}



}