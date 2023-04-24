package com.petkit.matetool.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static android.content.Context.WIFI_SERVICE;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

public class WifiUtils {
	// 上下文Context对象
	private Context mContext;
	// WifiManager对象
	private WifiManager mWifiManager;
	private Timer timer;

	public WifiUtils(Context mContext) {
		this.mContext = mContext;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * 判断手机是否连接在Wifi上
	 */
	public boolean isConnectWifi() {
		// 获取ConnectivityManager对象
		ConnectivityManager conMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取NetworkInfo对象
		NetworkInfo info = conMgr.getActiveNetworkInfo();
		// 获取连接的方式为wifi
		State wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();

		if (info != null && info.isAvailable() && wifi == State.CONNECTED)

		{
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 获取当前手机所连接的wifi信息
	 */
	public WifiInfo getCurrentWifiInfo() {
		return mWifiManager.getConnectionInfo();
	}

	/**
	 * 添加一个网络并连接 传入参数：WIFI发生配置类WifiConfiguration
	 */
	public boolean addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		return mWifiManager.enableNetwork(wcgID, true);
	}

	/**
	 * 搜索附近的热点信息，并返回所有热点为信息的SSID集合数据
	 */
	public List<String> getScanWifiResult() {
		// 扫描的热点数据
		List<ScanResult> resultList;
		// 开始扫描热点
		mWifiManager.disconnect();
		mWifiManager.reconnect();

		mWifiManager.startScan();
		resultList = mWifiManager.getScanResults();
		ArrayList<String> ssids = new ArrayList<String>();
		if (resultList != null) {
			for (ScanResult scan : resultList) {
				ssids.add(scan.SSID);// 遍历数据，取得ssid数据集
			}
		}
		return ssids;
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


	/**
	 * 连接wifi 参数：wifi的ssid及wifi的密码
	 */
	public boolean connectWifiTest(final String ssid, final String pwd) {
		boolean isSuccess = false;
		int flag = 5;
//		mWifiManager.disconnect();
//		int netId = getExistingNetworkId(ssid);
		String ssid2 = "petkit5g";
		String pw = "olab+petkit";

		WifiNetworkSpecifier specifier = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
			specifier = new WifiNetworkSpecifier.Builder()
					.setSsid(ssid2)
					.setWpa2Passphrase(pw)
					.build();

			NetworkRequest request = new NetworkRequest.Builder()
					.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
					.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
					.setNetworkSpecifier(specifier)
					.build();

			ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
				@Override
				public void onAvailable(Network network) {
					super.onAvailable(network);
					connectivityManager.bindProcessToNetwork(network);
				}
			});



		} else {
			WifiConfiguration wifiConfig = new WifiConfiguration();
			wifiConfig.SSID = ssid;
			wifiConfig.preSharedKey = pwd;
			wifiConfig.status = WifiConfiguration.Status.ENABLED;
			wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


			WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			int networkId = wifiManager.addNetwork(wifiConfig);
			isSuccess = wifiManager.enableNetwork(networkId, true);

		}
		
		return isSuccess;

	}

	/**
	 * 创建WifiConfiguration对象 分为三种情况：1没有密码;2用wep加密;3用wpa加密
	 *
	 * @param ssid
	 * @param password
	 * @param type
	 * @return
	 */
	public WifiConfiguration CreateWifiInfo(String ssid, String password,
			int type) {
		WifiConfiguration config = new WifiConfiguration();
		mWifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + ssid + "\"";

		List<WifiConfiguration> tempConfigs = isExist(ssid);
		if (mWifiManager != null && tempConfigs.size() != 0) {
			for (WifiConfiguration configRemove : tempConfigs)
				mWifiManager.removeNetwork(configRemove.networkId);
		}

		if (type == 1) {
			config.hiddenSSID = true;
//            config.wepKeys[0] = "\""+"\"";//网上流传的错误写法
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;

		} else if (type == 2) {
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == 3) {
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	private List<WifiConfiguration> isExist(String ssid) {
		List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
		List<WifiConfiguration> configRomoves = new ArrayList<>();

		if (configs == null) {
			return configRomoves;
		}

		for (WifiConfiguration config : configs) {
			if (config.SSID != null && config.SSID.equals("\"" + ssid + "\"")) {
				configRomoves.add(config);
			}
		}
		return configRomoves;
	}

	public int getExistingNetworkId(String SSID) {
		WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		if (configuredNetworks != null) {
			for (WifiConfiguration existingConfig : configuredNetworks) {
				if (existingConfig.SSID == null) {
					continue;
				}
				String ssid;
				if (existingConfig.SSID.startsWith("\"")
						&& existingConfig.SSID.endsWith("\"")) {
					ssid = existingConfig.SSID.substring(1, existingConfig.SSID.length() - 1);
				} else {
					ssid = existingConfig.SSID;
				}
				if (ssid.equals(SSID)) {
					return existingConfig.networkId;
				}
			}
		}
		return -1;
	}

}