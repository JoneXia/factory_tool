package com.petkit.android.ble.samsung;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.model.Device;
import com.petkit.android.model.Pet;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

@SuppressLint("NewApi")
public class SSBluetoothLeUtils extends BaseBluetoothLeUtils {

	public static final String TAG = "SSBluetoothLeUtils";

	private SSBluetoothLeService mBluetoothLeService = null;
	private SSBluetoothLeAction mBluetoothLeAction;
	
	private String deviceId, secret, secretKey, filePath;
	private ISamsungBLEListener mBleListener;
	private Pet curPet;

	public SSBluetoothLeUtils(Activity activity, ISamsungBLEListener listener) {
		super(activity, listener);
	}
	
	public SSBluetoothLeUtils(Context context, ISamsungBLEListener listener) {
		super(context, listener);
	}
	
	private int actionType;
	

	/**
	 * 关闭蓝牙相关
	 */
	public void destroyBluetoothLeService() {
		if (mBluetoothLeService != null) {
			scanLeDevice(false);
			if(mActivity != null){
				mActivity.unbindService(mServiceConnection);
			}else if(mContext != null){
				mContext.unbindService(mServiceConnection);
			}
			mBluetoothLeService.closeGatt();
			mBluetoothLeService = null;
			mBluetoothLeAction.stop();
		}
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
	 * 初始化
	 */
	@Override
	protected void initBLE() {
		// Use this check to determine whether BLE is supported on the device.
		// Then
		// you can selectively disable BLE-related features.
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null){
        	LogcatStorageHelper.addLog("Unable to obtain a BluetoothAdapter.");
        	mBleSupported = false;
        }

		// Register the BroadcastReceiver
		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(SSBluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(SSBluetoothLeService.ACTION_GATT_DISCONNECTED);
		mFilter.addAction(SSBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		mFilter.addAction(SSBluetoothLeService.ACTION_DATA_NOTIFY);
		mFilter.addAction(SSBluetoothLeService.ACTION_DATA_WRITE);
		mFilter.addAction(SSBluetoothLeService.ACTION_DATA_READ);
		mFilter.addAction(SSBluetoothLeService.ACTION_SCAN_DEVICE);
		mFilter.addAction(SSBluetoothLeService.ACTION_NOTIFICATION_CHANGED);
		mFilter.addAction(SSBluetoothLeService.ACTION_DEVICE_CONNECT_FAILED);
		
		mDeviceInfoList = new ArrayList<DeviceInfo>();
		mBluetoothLeAction = new SSBluetoothLeAction();
		
		// ////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// Broadcasted actions from Bluetooth adapter and BluetoothLeService
		//
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
//				PetkitLog.d(TAG, action);

				if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					// Bluetooth adapter state change
					switch (mBtAdapter.getState()) {
					case BluetoothAdapter.STATE_ON:
						startBluetoothLeService();
						break;
					case BluetoothAdapter.STATE_OFF:
						destroyBluetoothLeService();
						break;
					default:
						// Log.w(TAG, "Action STATE CHANGED not processed ");
						break;
					}

				} else if (SSBluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//					if(mDeviceInfo == null){
//						String address = intent.getStringExtra(SSBluetoothLeService.EXTRA_ADDRESS);
//						BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
//						if(device != null){
//							mBluetoothLeService.getBtGatt().cancelConnection(device);
//						}
//					}
				} else if (SSBluetoothLeService.ACTION_GATT_DISCONNECTED
						.equals(action)) {
					// GATT disconnect
					LogcatStorageHelper.addLog("ACTION_GATT_DISCONNECTED");
					mBluetoothLeAction.saveConfirmedData();
					stopScan();
					mBluetoothLeService.closeGatt();
					mBluetoothLeListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
				} else if (SSBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
						.equals(action)) {
					LogcatStorageHelper.addLog("ACTION_GATT_SERVICES_DISCOVERED enableGattService");
					enableGattService();
				} else if (SSBluetoothLeService.ACTION_SCAN_DEVICE.equals(action)) {
					String deviceAddress = intent.getStringExtra(SSBluetoothLeService.EXTRA_DEVICE);
					BluetoothDevice device = mBtAdapter.getRemoteDevice(deviceAddress);
					int rssi = intent.getIntExtra(SSBluetoothLeService.EXTRA_RSSI, 0);
					byte[] scanRecord = intent.getByteArrayExtra(SSBluetoothLeService.EXTRA_DATA);
					final DeviceInfo deviceInfo = new DeviceInfo(device, rssi, scanRecord);
					if(mActivity != null){
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								if (deviceInfo == null
										|| deviceInfo.getName() == null) {
									return;
								}
								if (checkDeviceFilter(deviceInfo.getName())) {
									if (!deviceInfoExists(deviceInfo.getAddress())) {
										// New device
										addDevice(deviceInfo);
									} else {
										// Already in list, update RSSI info
										DeviceInfo deviceInfo2 = findDeviceInfo(deviceInfo.getAddress());
										deviceInfo2.updateRssi(deviceInfo.getRssi());
									}
								}
							}
						});
					}else if(mContext != null){
						if (deviceInfo == null
								|| deviceInfo.getName() == null) {
							return;
						}
						if (checkDeviceFilter(deviceInfo.getName())) {
							if (!deviceInfoExists(deviceInfo.getAddress())) {
								// New device
								addDevice(deviceInfo);
							} else {
								// Already in list, update RSSI info
								DeviceInfo deviceInfo2 = findDeviceInfo(deviceInfo.getAddress());
								deviceInfo2.updateRssi(deviceInfo.getRssi());
							}
						}
					}
				}else if(SSBluetoothLeService.ACTION_NOTIFICATION_CHANGED.equals(action)){

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					LogcatStorageHelper.addLog("start action: " + getActionTypeString(actionType));
					
					switch (actionType) {
					case BLEConsts.BLE_ACTION_OTA:
//						mBluetoothLeAction.startOad(filePath);
						break;
					case BLEConsts.BLE_ACTION_OTA_RECONNECT:
						mBluetoothLeAction.startUpdate();
						break;
					case BLEConsts.BLE_ACTION_SYNC:
						mBluetoothLeAction.startSync(curPet, mBleListener);
						break;
					case BLEConsts.BLE_ACTION_CHANGE:
						mBluetoothLeAction.changeDevice(secret, mBleListener);
						break;
					case BLEConsts.BLE_ACTION_INIT:
						mBluetoothLeAction.initdevice(deviceId, secretKey, secret, mBleListener);
						break;
					case BLEConsts.BLE_ACTION_CHECK:
						mBluetoothLeAction.startCheck(curPet, mBleListener);
						break;

					default:
						break;
					}
					
					actionType = 0;
				}else if(SSBluetoothLeService.ACTION_DEVICE_CONNECT_FAILED.equals(action)){
					mBluetoothLeListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
				}
			}
		};
		
		// Code to manage Service life cycle.
		mServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName componentName,
					IBinder service) {
				mBluetoothLeService = ((SSBluetoothLeService.LocalBinder) service)
						.getService();
				if (!mBluetoothLeService.initialize(mBluetoothLeAction)) {
					LogcatStorageHelper.addLog("Unable to initialize BluetoothLeService");
					return;
				}
				LogcatStorageHelper.addLog("BluetoothLeService connected");
				mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_BLE_START, null);
			}

			public void onServiceDisconnected(ComponentName componentName) {
				mBluetoothLeService = null;
				LogcatStorageHelper.addLog("BluetoothLeService disconnected");
			}
		};
	}

	/**
	 * 链接指定蓝牙设备
	 * 
	 * @param device
	 */
	@Override
	public boolean onDeviceConnect(DeviceInfo device) {
		curConnectStep = 1;
		if(mBluetoothLeService == null){
			return false;
		}
		
		if (mDeviceInfo != null
				&& mDeviceInfo.getAddress() != null) {
			if (!mDeviceInfo.equals(device)) {
				mBluetoothLeService.disconnect(mBtAdapter.getRemoteDevice(device.getAddress()));
			}
		}
		
		mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_CONNECTING, null);

		mDeviceInfo = device;
		if (mScanning){	// TODO: after this connect finished, stop scan
			mScanning = false;
			stopScan();
		}
		// if connected then disconnect it
		if(!mBluetoothLeService.connect(mDeviceInfo.getAddress())){
			stopScan();
			LogcatStorageHelper.addLog("onConnectFail");
			mBluetoothLeListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
			return false;
		}
		return true;
	}


	

	public void enableGattService() {

		if(curConnectStep == 3){
			return;
		}
		curConnectStep = 3;
		
		if(mTimer == null){
			mTimer = new Timer();
		}
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(mActivity != null){
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							LogcatStorageHelper.addLog("onGattSuccess");
							mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_CONNECTED, null);
						}
						
					});
				}
			}
		}, 500);
	}

	/**
	 * 打开蓝牙服务
	 */
	@Override
	protected void startBluetoothLeService() {
		boolean f;

		if(mBluetoothLeService != null){
			return;
		}

		Context context = mActivity != null ? mActivity : mContext;
		Intent bindIntent = new Intent(context, SSBluetoothLeService.class);
		context.startService(bindIntent);
		
		f = context.bindService(bindIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
		if (f){
			LogcatStorageHelper.addLog("BluetoothLeService - success");
		}
		else {
			LogcatStorageHelper.addLog("onStartServiceFail");
			mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_BLE_NOT_SUPPORT, null);
		}
	}

	/**
	 * 开始搜索device
	 */
	public void startScan() {

		LogcatStorageHelper.addLog("SSBluetoothLeUtils startScan");

		if(mBluetoothLeService == null){
			mBtAdapter.enable();
			return;
		}
		
		if (mScanning) {
			return;
		}
		
		mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_SCANING, null);

		if(mTimer == null){
			mTimer = new Timer();
		}
		
		// Start device discovery
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (mScanning) {
					LogcatStorageHelper.addLog("onScanComplete timeout");
					mBluetoothLeListener.updateProgress(BLEConsts.PROGRESS_SCANING_TIMEOUT, null);
					stopScan();
				}
			}
		}, SCAN_TIMEOUT);

		if (mBleSupported) {
			mDeviceInfoList.clear();
			scanLeDevice(true);
			mBluetoothLeListener.onScanResultChange(null);
			if (!mScanning) {
				stopScan();
				scanFail();
			}
		} else {
			stopScan();
			scanFail();
		}

	}

	/**
	 * 停止搜索
	 */
	public void stopScan() {
		scanLeDevice(false);
	}

	/**
	 * 断开连接
	 */
	public void stopGatt() {
		stopScan();
		if (mBluetoothLeService != null) {
			mBluetoothLeService.closeGatt();
		}
	}


	public void initWriteCharacteristic() {
		if(mBluetoothLeService == null || mDeviceInfo == null){
			return;
		}
		
		if (mBluetoothLeService.getBtGatt() == null) {
			return;
		}
		
		UUID servUuid = BLEConsts.ACC_SERVICE_UUID;
		UUID confUuid = BLEConsts.ACC_CONTROL_UUID;

		// Skip keys
		if (confUuid == null){
			LogcatStorageHelper.addLog("initWriteCharacteristic confUuid null");
			return;
		}

		BluetoothGattService serv = mBluetoothLeService.getBtGatt()
				.getService(mBtAdapter.getRemoteDevice(mDeviceInfo.getAddress()), servUuid);

		if (serv == null) {
			PetkitLog.d(TAG, "BluetoothGattService null");
			return;
		}
		BluetoothGattCharacteristic charac = serv
				.getCharacteristic(confUuid);

		mBluetoothLeAction.setWriteCharacteristic(charac);

	}

	private void enableNotifications(boolean enable) {
		if (mBluetoothLeService.getBtGatt() == null || mDeviceInfo == null) {
			return;
		}
		
		UUID servUuid = BLEConsts.ACC_SERVICE_UUID;
		UUID dataUuid = BLEConsts.ACC_DATA_UUID;
		BluetoothGattService serv = mBluetoothLeService.getBtGatt()
				.getService(mBtAdapter.getRemoteDevice(mDeviceInfo.getAddress()), servUuid);
		if (serv == null) {
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattService null");
			return;
		}
		BluetoothGattCharacteristic charac = serv
				.getCharacteristic(dataUuid);
		if (charac == null) {
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattCharacteristic null");
			return;
		}
		mBluetoothLeService.setCharacteristicNotification(charac, enable);
	}

	private void initP2OTAWriteCharacteristic() {
		if(mBluetoothLeService == null || mDeviceInfo == null){
			return;
		}
		
		if (mBluetoothLeService.getBtGatt() == null) {
			return;
		}
		
		UUID servUuid = BLEConsts.DFU_SERVICE_UUID;
		UUID confUuid = BLEConsts.DFU_CONTROL_POINT_UUID;
		UUID packetUuid = BLEConsts.DFU_PACKET_UUID;

		// Skip keys
		if (confUuid == null){
			LogcatStorageHelper.addLog("initWriteCharacteristic confUuid null");
			return;
		}

		BluetoothGattService serv = mBluetoothLeService.getBtGatt()
				.getService(mBtAdapter.getRemoteDevice(mDeviceInfo.getAddress()), servUuid);

		if (serv == null) {
			PetkitLog.d(TAG, "BluetoothGattService null");
			return;
		}
		BluetoothGattCharacteristic controlCharacteristic = serv.getCharacteristic(confUuid);
		BluetoothGattCharacteristic packetCharacteristic = serv.getCharacteristic(packetUuid);

		mBluetoothLeAction.setP2OTACharacteristic(controlCharacteristic, packetCharacteristic);

	}
	
	private void enableP2OTANotifications(boolean enable) {
		if (mBluetoothLeService.getBtGatt() == null || mDeviceInfo == null) {
			return;
		}
		
		UUID servUuid = BLEConsts.DFU_SERVICE_UUID;
		UUID dataUuid = BLEConsts.DFU_CONTROL_POINT_UUID;
		BluetoothGattService serv = mBluetoothLeService.getBtGatt()
				.getService(mBtAdapter.getRemoteDevice(mDeviceInfo.getAddress()), servUuid);
		if (serv == null) {
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattService null");
			return;
		}
		BluetoothGattCharacteristic charac = serv
				.getCharacteristic(dataUuid);
		if (charac == null) {
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattCharacteristic null");
			return;
		}
		mBluetoothLeService.setCharacteristicNotification(charac, enable);
	}

	
	protected boolean scanLeDevice(boolean enable) {
		if(mBluetoothLeService == null){
			return false;
		}
		
		mScanning = enable;
		if(mScanning){
			mTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					if(mBluetoothLeService != null)
						mBluetoothLeService.scan(mScanning);
				}
			}, 1000);
		}else{
			mBluetoothLeService.scan(mScanning);
			if(mTimer != null){
				mTimer.cancel();
				mTimer = null;
			}
		}
		LogcatStorageHelper.addLog("scanLeDevice enable: " + enable);
		
		return true;
	}

	@Override
	public void startSync(Pet dog, ISamsungBLEListener listener) {
		if(mBluetoothLeService == null){
			return;
		}
		
		curPet = dog;
		mBleListener = listener;
		actionType = BLEConsts.BLE_ACTION_SYNC;

		initWriteCharacteristic();
		enableNotifications(true);
	}

	@Override
	public List<StringBuffer> getDataCacheBuffers() {
		if(mBluetoothLeService == null){
			return null;
		}
		return mBluetoothLeAction.getDataCache();
	}


	@Override
	public void changeDevice(String secret, ISamsungBLEListener listener) {
		if(mBluetoothLeService == null){
			return;
		}
		this.secret = secret;
		mBleListener = listener;
		
		actionType = BLEConsts.BLE_ACTION_CHANGE;
		enableNotifications(true);
		initWriteCharacteristic();
		
	}

	@Override
	public void initdevice(String id, String secretKey, String secret,
			ISamsungBLEListener listener) {
		if(mBluetoothLeService == null){
			return;
		}
		deviceId = id;
		this.secret = secret;
		this.secretKey = secretKey;

		actionType = BLEConsts.BLE_ACTION_INIT;
		
		enableNotifications(true);
		initWriteCharacteristic();
	}

	@Override
	public void startOad(String filePath) {
		
//		initP2OTAWriteCharacteristic();
//		enableP2OTANotifications(true);
//		enableNotifications(false);
		actionType = BLEConsts.BLE_ACTION_OTA;
		this.filePath = filePath;
		
		mBluetoothLeAction.startOad(filePath);
		
	}


	@Override
	public void startUpdate() {
		initP2OTAWriteCharacteristic();
		enableP2OTANotifications(true);
		
		actionType = BLEConsts.BLE_ACTION_OTA_RECONNECT;
	}
	@Override
	public Device getDeviceInfor() {
		return mBluetoothLeAction.getDeviceInfor();
	}
	
	@Override
	public void resetSensor() {
		mBluetoothLeAction.resetSensor();
	}

	@Override
	public boolean checkConnectState() {
		return mBluetoothLeService.getDeviceConnectState();
	}

	@Override
	public void startCheck(Pet dog, ISamsungBLEListener listener) {
		enableNotifications(true);
		initWriteCharacteristic();
		curPet = dog;
		mBleListener = listener;
		
		actionType = BLEConsts.BLE_ACTION_CHECK;
	}

	
	private String getActionTypeString(int action){
		switch (action) {
		case BLEConsts.BLE_ACTION_SYNC:
			return "SYNC";
		case BLEConsts.BLE_ACTION_CHECK:
			return "CHECK";
		case BLEConsts.BLE_ACTION_CHANGE:
			return "CHANGE";
		case BLEConsts.BLE_ACTION_CHANGE_HS:
			return "CHANGE_HS";
		case BLEConsts.BLE_ACTION_HS_INIT_WIFI:
			return "HS_INIT_WIFI";
		case BLEConsts.BLE_ACTION_INIT_HS:
			return "HS_INIT";
		case BLEConsts.BLE_ACTION_OTA:
			return "OTA";
		case BLEConsts.BLE_ACTION_OTA_RECONNECT:
			return "OTA RECONNECT";
		case BLEConsts.BLE_ACTION_SCAN:
			return "SCAN";
		}
		
		return "";
	}
}
