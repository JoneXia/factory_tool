package com.petkit.android.ble.samsung;

import java.util.List;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.model.Device;
import com.petkit.android.model.Pet;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;

@SuppressLint("NewApi") 
public abstract class BaseBluetoothLeUtils {

	public static final String TAG = "BluetoothLeUtils";

	// Requests to other activities
	public static final int REQ_ENABLE_BT = 0;

	protected final int SCAN_TIMEOUT = 10000; // Seconds

	// BLE management
	protected boolean mBleSupported = true;
	protected boolean mScanning = false;
	protected List<DeviceInfo> mDeviceInfoList;
	protected static BluetoothManager mBluetoothManager;
	protected BluetoothAdapter mBtAdapter = null;
	protected IntentFilter mFilter;
	protected String[] mDeviceFilter = new String[]{BLEConsts.PET_FIT_DISPLAY_NAME, BLEConsts.PET_FIT2_DISPLAY_NAME};
	protected DeviceInfo mDeviceInfo = null;
	
	/**
	 * 1，开始链接；
	 * 2，链接成功，开始discoverService
	 * 3，discoverService成功，开始enableService
	 */
	protected int curConnectStep = 0;

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasted actions from Bluetooth adapter and BluetoothLeService
	//
	protected BroadcastReceiver mReceiver;
	
	protected ServiceConnection mServiceConnection;

	protected Activity mActivity;
	protected Context mContext;
	protected Timer mTimer;
	
	protected ISamsungBLEListener mBluetoothLeListener;
	
	/**
	 * 用于activity调用
	 * @param activity
	 * @param listener
	 */
	public BaseBluetoothLeUtils(Activity activity, ISamsungBLEListener listener) {
		super();

		mActivity = activity;
		mBluetoothLeListener = listener;
		mTimer = new Timer();

		initBLE();

	}
	
	
	/**
	 * 仅用于service调用，只做搜索设备，不会链接
	 * @param context
	 * @param listener
	 */
	public BaseBluetoothLeUtils(Context context, ISamsungBLEListener listener){
		mContext = context;
		mBluetoothLeListener = listener;
		mTimer = new Timer();
		initBLE();
	}

	public boolean isBleSupport() {
		return mBleSupported;
	}

	/**
	 * 启动蓝牙，开始搜索
	 */
	public void start() {
		// Broadcast receiver
		if (!mBleSupported) {
			return;
		}
		PetkitLog.d(TAG, "start");
		LogcatStorageHelper.addLog("BaseBluetoothLeUtils start");
		resume();

		if (mBtAdapter.isEnabled()) {
			// Start straight away
			startBluetoothLeService();
		} else if(mActivity != null){
			// Request BT adapter to be turned on
//			Intent enableIntent = new Intent(
//					BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			mActivity.startActivityForResult(enableIntent, REQ_ENABLE_BT);
			mBtAdapter.enable();
		} else{
//			mBtAdapter.enable();
			LogcatStorageHelper.addLog("BaseBluetoothLeUtils start fail, ble disable");
		}
	}

	private boolean isRegister = false;

	public void resume() {
		if (!isRegister) {
			isRegister = true;
			if(mActivity != null){
				mActivity.registerReceiver(mReceiver, mFilter);
			}
			if(mContext != null){
				mContext.registerReceiver(mReceiver, mFilter);
			}
		}
	}

	public void stop() {
		if (isRegister) {
			isRegister = false;
			if(mActivity != null){
				mActivity.unregisterReceiver(mReceiver);
			}
			if(mContext != null){
				mContext.unregisterReceiver(mReceiver);
			}
		}
		
		stopGatt();
	}

	/**
	 * 关闭蓝牙相关
	 */
	public void destroy() {

		if (!mBleSupported) {
			return;
		}

		LogcatStorageHelper.addLog("BaseBluetoothLeUtils destroy");
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
		stop();
		destroyBluetoothLeService();
	}

	/**
	 * 获取搜索到的Device列表
	 * 
	 * @return
	 */
	public List<DeviceInfo> getDeviceInfoList() {
		return mDeviceInfoList;
	}


	/**
	 * 停止搜索
	 */
	public void stopScan() {
		mScanning = false;
		scanLeDevice(false);
	}


	protected void scanFail() {
		LogcatStorageHelper.addLog("onScanFail");
		mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_SCANING_FAILED, null);
	}

	protected boolean deviceInfoExists(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getAddress()
					.equals(address)) {
				return true;
			}
		}
		return false;
	}

	protected DeviceInfo createDeviceInfo(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		DeviceInfo deviceInfo = new DeviceInfo(device, rssi, scanRecord);
		return deviceInfo;
	}

	protected DeviceInfo findDeviceInfo(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getAddress()
					.equals(address)) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}

	protected boolean checkDeviceFilter(String deviceName) {
		int n = mDeviceFilter.length;
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equalsIgnoreCase(mDeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

	protected void addDevice(DeviceInfo device) {
		LogcatStorageHelper.addLog("find device: " + (device == null ? "device == null" : device.toString()));

//		if(device != null){
//			Context context = mActivity != null ? mActivity : mContext; 
//			InforCollectUtils.insertBeaconsInfor(context, CommonUtils.getCurrentUserId(), device);
//		}
//		
		mDeviceInfoList.add(device);
		mBluetoothLeListener.onScanResultChange(device);
	}



	/**
	 * 链接指定蓝牙设备
	 * 
	 * @param device
	 */
	public abstract boolean onDeviceConnect(DeviceInfo device);

	/**
	 * 开始搜索device
	 */
	public abstract void startScan();

	/**
	 * 断开连接
	 */
	public abstract void stopGatt();
	
	/**
	 * start to sync data from petkit device
	 * @param secret
	 * @param listener
	 */
	public abstract void startSync(Pet secret, ISamsungBLEListener listener);
	public abstract void startCheck(Pet secret, ISamsungBLEListener listener);
	public abstract void startUpdate();
	public abstract void startOad(String filePath);
	
	public abstract Device getDeviceInfor();
	
	public abstract List<StringBuffer> getDataCacheBuffers();

	public abstract void changeDevice(String secret, ISamsungBLEListener listener);
	
	public abstract void initdevice(String id, String secretKey, String secret, ISamsungBLEListener listener);
	
	public abstract void resetSensor();
	
	public abstract boolean checkConnectState();
	/**
	 * 初始化
	 */
	protected abstract void initBLE();
	protected abstract void destroyBluetoothLeService();
	protected abstract boolean scanLeDevice(boolean enable);
	/**
	 * 打开蓝牙服务
	 */
	protected abstract void startBluetoothLeService();
	
}
