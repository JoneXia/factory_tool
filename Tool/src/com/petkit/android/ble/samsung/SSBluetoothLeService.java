/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petkit.android.ble.samsung;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

public class SSBluetoothLeService extends Service {

	public static final int GATT_TIMEOUT = 1000; // milliseconds
    private static final String TAG = "SSBluetoothLeService";
    
    public final static String ACTION_GATT_CONNECTED = "ti.android.ble.common.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ti.android.ble.common.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ti.android.ble.common.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_READ = "ti.android.ble.common.ACTION_DATA_READ";
	public final static String ACTION_DATA_NOTIFY = "ti.android.ble.common.ACTION_DATA_NOTIFY";
	public final static String ACTION_DATA_WRITE = "ti.android.ble.common.ACTION_DATA_WRITE";
	public final static String ACTION_SCAN_DEVICE = "ti.android.ble.common.ACTION_SCAN_DEVICE";
	public final static String ACTION_NOTIFICATION_CHANGED = "ti.android.ble.common.ACTION_NOTIFICATION_CHANGED";
	public final static String ACTION_DEVICE_CONNECT_FAILED = "ti.android.ble.common.ACTION_DEVICE_CONNECT_FAILED";
	
	public final static String EXTRA_DATA = "ti.android.ble.common.EXTRA_DATA";
	public final static String EXTRA_UUID = "ti.android.ble.common.EXTRA_UUID";
	public final static String EXTRA_STATUS = "ti.android.ble.common.EXTRA_STATUS";
	public final static String EXTRA_ADDRESS = "ti.android.ble.common.EXTRA_ADDRESS";
	
    /** Intent extras */
    public static final String EXTRA_DEVICE = "DEVICE";
    public static final String EXTRA_RSSI = "RSSI";
    public static final String EXTRA_SOURCE = "SOURCE";
    public static final String EXTRA_ADDR = "ADDRESS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    
    private static final int ADV_DATA_FLAG = 0x01;
    private static final int LIMITED_AND_GENERAL_DISC_MASK = 0x03;
    
    private SSBluetoothLeAction mBluetoothLeAction;
    
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothDevice curDevice;
    public boolean isNoti = false;
    
    private Timer timer = null;
    private boolean isDeviceConnected = false;

	private volatile boolean mBusy = false; // Write/read pending response
	
    /**
     * Profile service connection listener
     */
    public class LocalBinder extends Binder {
        SSBluetoothLeService getService() {
            return SSBluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }
    

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example,
		// close() is
		// invoked when the UI is disconnected from the Service.
		closeGatt();
		return super.onUnbind(intent);
	}

    private final IBinder binder = new LocalBinder();

    /**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize(SSBluetoothLeAction bluetoothLeAction) {

		LogcatStorageHelper.addLog("SSBluetoothLeService initialize");
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter == null){
        		LogcatStorageHelper.addLog("Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }

        if (mBluetoothGatt == null) {
            BluetoothGattAdapter.getProfileProxy(this, mProfileServiceListener, BluetoothGattAdapter.GATT);
        }
        
        mBluetoothLeAction = bluetoothLeAction;
        mBluetoothLeAction.setBluetoothLeService(this);
		return true;
	}
	
	//
	// Utility functions
	//
	public BluetoothGatt getBtGatt() {
		return mBluetoothGatt;
	}
	
	public BluetoothGattService getBtGattService(UUID uuid){
		if(mBluetoothGatt != null && curDevice != null){
			return mBluetoothGatt.getService(curDevice, uuid);
		}
		return null;
	}
	
	
	/**
     * GATT client callbacks
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!checkIfBroadcastMode(scanRecord)) {
            	if(!isBLEDevice(device)){
            		return;
            	}
            	
                Intent intent = new Intent(ACTION_SCAN_DEVICE);
        		intent.putExtra(EXTRA_DEVICE, device.getAddress());
        		intent.putExtra(EXTRA_RSSI, rssi);
        		intent.putExtra(EXTRA_DATA, scanRecord);
        		sendBroadcast(intent);
            } else
                Log.i(TAG, "device =" + device + " is in Brodacast mode, hence not displaying");
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
    		LogcatStorageHelper.addLog("onConnectionStateChange (" + device.getAddress() + ")");
            if (newState == BluetoothProfile.STATE_CONNECTED && mBluetoothGatt != null) {
            	mBluetoothLeAction.setBluetoothGatt(mBluetoothGatt);
				broadcastUpdate(ACTION_GATT_CONNECTED, device.getAddress(), status);
				
				try {
					synchronized (this) {
						wait(1600);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				LogcatStorageHelper.addLog("start discoverServices (" + device.getAddress() + ")");
                mBluetoothGatt.discoverServices(device);
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED && mBluetoothGatt != null) {
                broadcastUpdate(ACTION_GATT_DISCONNECTED, device.getAddress(), status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
        	isDeviceConnected = true;

			if(timer != null){
				timer.cancel();
				timer = null;
			}
			
        	broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,
					device.getAddress(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        	if (BLEConsts.DFU_CONTROL_POINT_UUID.equals(characteristic.getUuid())) {
        		mBluetoothLeAction.packetsCharacteristicChanged(characteristic.getValue());
        	}else {
        		broadcastUpdate(ACTION_DATA_NOTIFY, characteristic,
    					BluetoothGatt.GATT_SUCCESS);
			}
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic charac, int status) {
    		LogcatStorageHelper.addLog("onCharacteristicRead (" + charac.getUuid().toString() + ")");
        	broadcastUpdate(ACTION_DATA_READ, charac, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
//            mBusy = false;
//			BluetoothGattCharacteristic mHRMcharac = descriptor.getCharacteristic();
//			setCharacteristicNotification(mHRMcharac, true);
        }

		@Override
		public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic,
				int status) {
			mBusy = false;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BLEConsts.DFU_PACKET_UUID.equals(characteristic.getUuid())) {
					mBluetoothLeAction.writeP2OTAPacket();
				}else if(BLEConsts.DFU_CONTROL_POINT_UUID.equals(characteristic.getUuid())){
					mBluetoothLeAction.controlCharacteristicWrite();
				}
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
//			mBusy = false;
    		LogcatStorageHelper.addLog("onDescriptorWrite (" + descriptor.getUuid().toString() + ")");
    		PetkitLog.d("onDescriptorWrite (" + descriptor.getUuid().toString() + ")");
//    		if (status == BluetoothGatt.GATT_SUCCESS) {
//    			final Intent intent = new Intent(ACTION_NOTIFICATION_CHANGED);
//				intent.putExtra(EXTRA_STATUS, status);
//				sendBroadcast(intent);
//			} else {
//				
//			}
    		
    		final Intent intent = new Intent(ACTION_NOTIFICATION_CHANGED);
			intent.putExtra(EXTRA_STATUS, status);
			sendBroadcast(intent);
		}
    };
    
    private void broadcastUpdate(final String action, final String address,
			final int status) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_ADDRESS, address);
		intent.putExtra(EXTRA_STATUS, status);
		sendBroadcast(intent);

//		PetkitLog.d(TAG, "broadcastUpdate action: " + action);
//		PetkitLog.d(TAG, "broadcastUpdate address: " + address);
//		PetkitLog.d(TAG, "broadcastUpdate status: " + status);
		mBusy = false;
	}
    
    
	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic, final int status) {
		mBusy = false;
		
		final Intent intent = new Intent(action);
		byte[] data = characteristic.getValue();
		
		PetkitLog.d(TAG, "broadcastUpdate action: " + action + " status: " + status);
		if(data != null && data.length > 0){
			StringBuilder stringBuilder = new StringBuilder();
			for (byte tempByte : data)
				stringBuilder.append(String.format("%02X ", tempByte));
			PetkitLog.d(TAG, "broadcastUpdate data: " + stringBuilder.toString());
		}else{
			PetkitLog.d(TAG, "broadcastUpdate data: " + "null");
		}
		
		if (characteristic.getUuid().equals(BLEConsts.BAT_DATA_UUID)) {
			byte b = data[0];
			data = new byte[2];
			data[0] = 'B';
			data[1] = b;
		}
		
		if(characteristic.getUuid().equals(BLEConsts.DFU_CONTROL_POINT_UUID)){
			if(data != null && data.length > 0){
				if (action.equals(ACTION_DATA_WRITE)){
//					mBluetoothLeAction.checkBlocksIndex(data);
				}
			}
		}else if (data != null && data.length > 0) {
			if (action.equals(ACTION_DATA_NOTIFY)
					|| action.equals(ACTION_DATA_READ))
				mBluetoothLeAction.DataCommandParse(data);
		}

		intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
		intent.putExtra(EXTRA_DATA, characteristic.getValue());
		intent.putExtra(EXTRA_STATUS, status);
		sendBroadcast(intent);

//		PetkitLog.d(TAG, "broadcastUpdate action: " + action);
//		PetkitLog.d(TAG, "broadcastUpdate data: " + characteristic.getValue());
//		PetkitLog.d(TAG, "broadcastUpdate status: " + status);
	}
	
	
    @Override
    public void onDestroy() {
        LogcatStorageHelper.addLog("SSBluetoothLeService onDestroy");
        if (mBluetoothGatt != null) {
            BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mBluetoothGatt);
        }
        super.onDestroy();
    }

    /**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void closeGatt() {
		if (mBluetoothGatt != null && curDevice != null) {
			LogcatStorageHelper.addLog("SSBluetoothLeService closeGatt connect");
			mBluetoothGatt.cancelConnection(curDevice);
			mBluetoothLeAction.stop();
		}
	}
	
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothGattAdapter.GATT) {
                mBluetoothGatt = (BluetoothGatt) proxy;
                mBluetoothLeAction.setBluetoothGatt(mBluetoothGatt);
                mBluetoothGatt.registerApp(mGattCallbacks);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {
                if (mBluetoothGatt != null)
                    mBluetoothGatt.unregisterApp();

                mBluetoothGatt = null;
            }
        }
    };

    
    /*
     * Broadcast mode checker API
     */
    public boolean checkIfBroadcastMode(byte[] scanRecord) {
        int offset = 0;
        while (offset < (scanRecord.length - 2)) {
            int len = scanRecord[offset++];
            if (len == 0)
                break; // Length == 0 , we ignore rest of the packet

            int type = scanRecord[offset++];
            switch (type) {
            case ADV_DATA_FLAG:

                if (len >= 2) {
                    // The usual scenario(2) and More that 2 octets scenario.
                    // Since this data will be in Little endian format, we
                    // are interested in first 2 bits of first byte
                    byte flag = scanRecord[offset++];
                    /*
                     * 00000011(0x03) - LE Limited Discoverable Mode and LE
                     * General Discoverable Mode
                     */
                    if ((flag & LIMITED_AND_GENERAL_DISC_MASK) > 0)
                        return false;
                    else
                        return true;
                } else if (len == 1) {
                    continue;// ignore that packet and continue with the rest
                }
            default:
                offset += (len - 1);
                break;
            }
        }
        return false;
    }
    
    public void readCharacteristic(BluetoothGattCharacteristic charac){
    	if(!checkGatt()){
    		return;
    	}
		mBusy = true;
		
		mBluetoothLeAction.setReadCharacteristic(charac);
		boolean result = mBluetoothGatt.readCharacteristic(charac);
		if(result == false){
			LogcatStorageHelper.addLog("SSBluetoothLeService readCharacteristic fail");
		}
    }
    
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] val) {
    	if(!checkGatt()){
    		return false;
    	}

    	mBluetoothLeAction.setWriteCharacteristic(characteristic);
		characteristic.setValue(val);

		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
    
    public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic, boolean b) {
    	if(!checkGatt()){
    		return false;
    	}
    	
		byte[] val = new byte[1];

		val[0] = (byte) (b ? 1 : 0);
		characteristic.setValue(val);
		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
    
    public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic) {
    	if(!checkGatt()){
    		return false;
    	}
    	
		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
    

    /**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public boolean setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enable) {
		if (!checkGatt())
			return false;

		if (!mBluetoothGatt.setCharacteristicNotification(characteristic,
				enable)) {
			LogcatStorageHelper.addLog("setCharacteristicNotification failed");
			return false;
		}

		BluetoothGattDescriptor clientConfig = characteristic
				.getDescriptor(BLEConsts.CLIENT_CHARACTERISTIC_CONFIG);
		if (clientConfig == null)
			return false;

		if (enable) {
			LogcatStorageHelper.addLog("setCharacteristicNotification enable notification");
			clientConfig
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		} else {
			LogcatStorageHelper.addLog("setCharacteristicNotification disable notification");
			clientConfig
					.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}

		mBusy = true;
		return mBluetoothGatt.writeDescriptor(clientConfig);
	}

	public boolean isNotificationEnabled(
			BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return false;

		BluetoothGattDescriptor clientConfig = characteristic
				.getDescriptor(BLEConsts.CLIENT_CHARACTERISTIC_CONFIG);
		if (clientConfig == null)
			return false;

		return clientConfig.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
	}
	
	
	private boolean checkGatt() {
		if (mBtAdapter == null) {
			PetkitLog.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (mBluetoothGatt == null) {
			PetkitLog.w(TAG, "BluetoothGatt not initialized");
			return false;
		}

		if (mBusy) {
			PetkitLog.w(TAG, "LeService busy");
			return false;
		}
		return true;

	}
	
	public boolean connect(String deviceAddress) {
        
        if (mBluetoothGatt == null || deviceAddress == null) {
			PetkitLog.w(TAG,
					"BluetoothAdapter not initialized or unspecified device.");
			return false;
		}
		if (mBluetoothGatt != null) {
			curDevice = mBtAdapter.getRemoteDevice(deviceAddress);
	        @SuppressWarnings("unchecked")
			List<BluetoothDevice> connectDevices = mBluetoothGatt.getConnectedDevices();
	        
	        boolean isConnected = false;
	        if(connectDevices != null && connectDevices.size() >= 0){
	        	for(BluetoothDevice device2 : connectDevices){
	        		if(device2.getAddress().equals(curDevice.getAddress())){
	        			isConnected = true;
	        			curDevice = device2;
	        			break;
	        		}
	        	}
	        }

        	LogcatStorageHelper.addLog("connect device " + curDevice.getAddress());
	        if(!isConnected){
	        	PetkitLog.d(TAG, "use GATT connection");
	        	if (mBluetoothGatt.connect(curDevice, false)) {
	        		isDeviceConnected = false;
	        		if(timer == null){
	        			timer = new Timer();
	        		}
		        	timer.schedule(new TimerTask() {
						@Override
						public void run() {
							if(!isDeviceConnected){
								if(timer != null){
									timer.cancel();
									timer = null;
								}
								
								LogcatStorageHelper.addLog("connect device failed, time out");
								PetkitLog.d("connect device failed, time out");
								broadcastUpdate(ACTION_DEVICE_CONNECT_FAILED, curDevice.getAddress(), 0);
							}
						}
					}, 20000);
		        	
					return true;
				} else {
					LogcatStorageHelper.addLog("connect device " + curDevice.getAddress() + "fail");
					return false;
				}
	        	
	        	
	        }else{
	        	PetkitLog.d(TAG, "Re-use GATT connection cancelConnection");
	        	LogcatStorageHelper.addLog("Re-use GATT connection cancelConnection");
//	        	@SuppressWarnings("unchecked")
//				List<BluetoothGattService> services = mBluetoothGatt.getServices(curDevice);
//	        	if(services == null || services.size() == 0){
//	        		mBluetoothGatt.discoverServices(curDevice);
//	        	}else{
//	        		broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,
//	        				curDevice.getAddress(), 0);
//	        	}
	        	mBluetoothGatt.cancelConnection(curDevice);	//TODO;
	        	return false;
	        }
		} else {
			PetkitLog.w(TAG, "Attempt to connect mBluetoothGatt = null ");
			return false;
		}
    }
	
	public boolean getDeviceConnectState(){
        boolean isConnected = false;
		if (mBluetoothGatt != null && curDevice != null) {
			isConnected = (mBluetoothGatt.getConnectionState(curDevice) == BluetoothProfile.STATE_CONNECTED);
		}
		return isConnected;
	}

    public void disconnect(BluetoothDevice device) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.cancelConnection(device);
            device = null;
            curDevice = null;
        }
    }

    public void scan(boolean start) {
        if (mBluetoothGatt == null)
            return;
        if (start) {
            mBluetoothGatt.startScan();
        } else {
            mBluetoothGatt.stopScan();
        }
    }

    public boolean isBLEDevice(BluetoothDevice device) {
        return mBluetoothGatt.isBLEDevice(device);
    }

	public boolean waitIdle(int i) {
		i /= 10;
		while (--i > 0) {
			if (mBusy)
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				break;
		}

		return i > 0;
	}
	
}
