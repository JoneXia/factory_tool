package com.petkit.android.ble.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.Conversion;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.GattError;
import com.petkit.android.ble.ZipHexInputStream;
import com.petkit.android.ble.exception.DeviceDisconnectedException;
import com.petkit.android.ble.exception.BLEErrorException;
import com.petkit.android.ble.exception.HexFileValidationException;
import com.petkit.android.ble.exception.RemoteDfuException;
import com.petkit.android.ble.exception.UnexpectedCompleteException;
import com.petkit.android.ble.exception.UnknownParametersException;
import com.petkit.android.ble.exception.UnknownResponseException;
import com.petkit.android.ble.exception.BLEAbortedException;
import com.petkit.android.model.Pet;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressLint("NewApi")
public class AndroidBLEActionService extends BLEActionService {

	@SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			// check whether an error occurred 
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					logi("Connected to GATT server");
					mConnectionState = BLEConsts.STATE_CONNECTED;

					/*
					 *  The onConnectionStateChange callback is called just after establishing connection and before sending Encryption Request BLE event in case of a paired device. 
					 *  In that case and when the Service Changed CCCD is enabled we will get the indication after initializing the encryption, about 1600 milliseconds later. 
					 *  If we discover services right after connecting, the onServicesDiscovered callback will be called immediately, before receiving the indication and the following 
					 *  service discovery and we may end up with old, application's services instead.
					 *  
					 *  This is to support the buttonless switch from application to bootloader mode where the DFU bootloader notifies the master about service change.
					 *  Tested on Nexus 4 (Android 4.4.4 and 5), Nexus 5 (Android 5), Samsung Note 2 (Android 4.4.2). The time after connection to end of service discovery is about 1.6s 
					 *  on Samsung Note 2.
					 *  
					 *  NOTE: We are doing this to avoid the hack with calling the hidden gatt.refresh() method, at least for bonded devices.
					 */
					if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						try {
							synchronized (this) {
								logd("Waiting 1600 ms for a possible Service Changed indication...");
								wait(1600);

								// After 1.6s the services are already discovered so the following gatt.discoverServices() finishes almost immediately.

								// NOTE: This also works with shorted waiting time. The gatt.discoverServices() must be called after the indication is received which is
								// about 600ms after establishing connection. Values 600 - 1600ms should be OK.
							}
						} catch (InterruptedException e) {
							// do nothing
						}
					}

					// Attempts to discover services after successful connection.
					// NOTE: do not refresh the gatt device here!
					final boolean success = gatt.discoverServices();
					logi("Attempting to start service discovery... " + (success ? "succeed" : "failed"));

					if (!success) {
						mError = BLEConsts.ERROR_SERVICE_DISCOVERY_NOT_STARTED;
					} else {
						// just return here, lock will be notified when service discovery finishes
						return;
					}
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					logi("Disconnected from GATT server");
					mPaused = false;
					mConnectionState = BLEConsts.STATE_DISCONNECTED;
				}
			} else {
				loge("Connection state change error: " + status + " newState: " + newState);
				mPaused = false;
				mError = BLEConsts.ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				logi("Services discovered");
				mConnectionState = BLEConsts.STATE_CONNECTED_AND_READY;
			} else {
				loge("Service discovery error: " + status);
				mError = BLEConsts.ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BLEConsts.CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) {
					if (BLEConsts.SERVICE_CHANGED_UUID.equals(descriptor.getCharacteristic().getUuid())) {
						// we have enabled notifications for the Service Changed characteristic
						mServiceChangedIndicationsEnabled = descriptor.getValue()[0] == 2;
					} else {
						// we have enabled notifications for this characteristic
						mNotificationsEnabled = descriptor.getValue()[0] == 1;
					}
				}
			} else {
				loge("Descriptor write error: " + status);
				mError = BLEConsts.ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		};

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				/*
				 * This method is called when either a CONTROL POINT or PACKET characteristic has been written.
				 * If it is the CONTROL POINT characteristic, just set the flag to true.
				 * If the PACKET characteristic was written we must:
				 * - if the image size was written in DFU Start procedure, just set flag to true
				 * - else
				 * - send the next packet, if notification is not required at that moment
				 * - do nothing, because we have to wait for the notification to confirm the data received
				 */
				if (BLEConsts.DFU_PACKET_UUID.equals(characteristic.getUuid())) {
					
					if(deviceState.getHardware() == 1){
						PetkitLog.d("onCharacteristicWrite index: " + mProgInfo.iBlocks + " total: " + mProgInfo.nBlocks);
						if(mProgInfo.iBlocks < mProgInfo.nBlocks){
							try {
								waitUntilTimeOut(50);
								synchronized (mLock) {
									while(!timeOut && !mAborted && !mPaused){
										mLock.wait();
									}
								}
							} catch (InterruptedException e) {
							}
							
							
							// The writing might have been aborted (mAborted = true), an error might have occurred.
							// In that case quit sending.
							if (mAborted || mError != 0 || mResetRequestSent) {
								// notify waiting thread
								synchronized (mLock) {
									sendLogBroadcast("Upload terminated");
									mLock.notifyAll();
									return;
								}
							}
	
							final byte[] buffer = mBuffer;
							buffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
							buffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
							System.arraycopy(mFileBuffer, mProgInfo.iBytes, buffer, 2,
									OAD_BLOCK_SIZE);
							PetkitLog.d("writePacket index: " + mProgInfo.iBlocks + " total: " + mProgInfo.nBlocks);
							writePacket(gatt, characteristic, buffer, OAD_BUFFER_SIZE);
							updateProgressNotification();
							return;
						}else{
							mReceivedData = new byte[8];	//set a temp value to notify lock
						}
					}
					
					if (mImageSizeSent && mInitPacketSent) {
						// if the PACKET characteristic was written with image data, update counters
						mBytesSent += characteristic.getValue().length;
						mPacketsSentSinceNotification++;

						// if a packet receipt notification is expected, or the last packet was sent, do nothing. There onCharacteristicChanged listener will catch either 
						// a packet confirmation (if there are more bytes to send) or the image received notification (it upload process was completed)
						final boolean notificationExpected = mPacketsBeforeNotification > 0 && mPacketsSentSinceNotification == mPacketsBeforeNotification;
						final boolean lastPacketTransfered = mBytesSent == mImageSizeInBytes;

						if (notificationExpected || lastPacketTransfered)
							return;

						// when neither of them is true, send the next packet
						try {
							waitIfPaused(false);
							// The writing might have been aborted (mAborted = true), an error might have occurred.
							// In that case quit sending.
							if (mAborted || mError != 0 || mRemoteErrorOccured || mResetRequestSent) {
								// notify waiting thread
								synchronized (mLock) {
									sendLogBroadcast("Upload terminated");
									mLock.notifyAll();
									return;
								}
							}

							final byte[] buffer = mBuffer;
							final int size = mInputStream.read(buffer);
							writePacket(gatt, characteristic, buffer, size);
							updateProgressNotification();
							return;
						} catch (final HexFileValidationException e) {
							loge("Invalid HEX file");
							mError = BLEConsts.ERROR_FILE_INVALID;
						} catch (final IOException e) {
							loge("Error while reading the input stream", e);
							mError = BLEConsts.ERROR_FILE_IO_EXCEPTION;
						}
					} else if (!mImageSizeSent) {
						// we've got confirmation that the image size was sent
						sendLogBroadcast("Data written to " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
						mImageSizeSent = true;
					} else if (!mInitPacketSent) {
						// we've got confirmation that the init packet was sent
						sendLogBroadcast("Data written to " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
						mInitPacketSent = true;
					}
				} else if(BLEConsts.ACC_CONTROL_UUID.equals(characteristic.getUuid())){
					
					try {
						waitUntilTimeOut(100);
						synchronized (mLock) {
							while(!timeOut && !mAborted && !mPaused){
								mLock.wait();
							}
						}
					} catch (InterruptedException e) {
					}
					mPartCurrent++;
					if(mWriteData != null && mPartCurrent < mWriteData.size()){
						try {
							PetkitLog.d("write mate data: " + mPartCurrent);
							writePacket(gatt, characteristic, buildMateOpCodeBuffer(BLEConsts.MATE_OP_CODE_REQUEST_KEY), 20);
							return;
						} catch (BLEAbortedException e) {
							mError = BLEConsts.ERROR_ABORTED;
						} catch (UnknownParametersException e) {
							mError = BLEConsts.ERROR_INVALID_PARAMETERS;
						}
					}else {
						// if the CONTROL POINT characteristic was written just set the flag to true
						sendLogBroadcast("Data written to " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
						mRequestCompleted = true;
					}
					
				} else {
					// if the CONTROL POINT characteristic was written just set the flag to true
					sendLogBroadcast("Data written to " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
					mRequestCompleted = true;
				}
			} else {
				/*
				 * If a Reset (Op Code = 6) or Activate and Reset (Op Code = 5) commands are sent the DFU target resets and sometimes does it so quickly that does not manage to send
				 * any ACK to the controller and error 133 is thrown here.
				 */
				if (mResetRequestSent)
					mRequestCompleted = true;
				else {
					loge("Characteristic write error: " + status);
					mError = BLEConsts.ERROR_CONNECTION_MASK | status;
				}
			}

			refreshHeartbeatTime();
			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		};

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				/*
				 * This method is called when the DFU Version and battery characteristic has been read.
				 */
				sendLogBroadcast("Read Response received from " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
				mReceivedData = characteristic.getValue();
				mRequestCompleted = true;
			} else {
				loge("Characteristic read error: " + status);
				mError = BLEConsts.ERROR_CONNECTION_MASK | status;
			}

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		};

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			int responseType = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

			if(deviceState.getHardware() == 1 && responseType == BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_KEY){
				responseType = 0;
			}
			
			PetkitLog.d("onCharacteristicChanged responseType: " + responseType);
			switch (responseType) {
			case BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_KEY:
				final BluetoothGattCharacteristic packetCharacteristic = gatt.getService(BLEConsts.DFU_SERVICE_UUID).getCharacteristic(BLEConsts.DFU_PACKET_UUID);

				try {
					mBytesConfirmed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 1);
					mPacketsSentSinceNotification = 0;

					waitIfPaused(false);
					// The writing might have been aborted (mAborted = true), an error might have occurred.
					// In that case quit sending.
					if (mAborted || mError != 0 || mRemoteErrorOccured || mResetRequestSent) {
						sendLogBroadcast("Upload terminated");
						break;
					}

					final byte[] buffer = mBuffer;
					final int size = mInputStream.read(buffer);
					writePacket(gatt, packetCharacteristic, buffer, size);
					updateProgressNotification();
					return;
				} catch (final HexFileValidationException e) {
					loge("Invalid HEX file");
					mError = BLEConsts.ERROR_FILE_INVALID;
				} catch (final IOException e) {
					loge("Error while reading the input stream", e);
					mError = BLEConsts.ERROR_FILE_IO_EXCEPTION;
				}
				break;
			case BLEConsts.OP_CODE_DEBUG_INFOR_KEY:
				BluetoothGattCharacteristic controlCharacteristic = gatt.getService(BLEConsts.ACC_SERVICE_UUID).getCharacteristic(BLEConsts.ACC_CONTROL_UUID);
				try {
					parserReceivedData(characteristic.getValue());
				} catch (UnexpectedCompleteException e1) {
				} catch (UnknownResponseException e) {
				} catch (UnsupportedEncodingException e) {
				}
				
				if(mComdLength < 6){
					try {
						writePacket(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEBUG_INFOR_KEY), BLEConsts.MAX_DATA_CONTROL_WRITE_SIZE);
						return;
					} catch (UnknownParametersException e) {
						mError = BLEConsts.ERROR_INVALID_PARAMETERS;
					} catch (BLEAbortedException e) {
						mError = BLEConsts.ERROR_ABORTED;
					}
				}else {
					mReceivedData = characteristic.getValue();
				}
				break;
			////////////////
//			case BLEConsts.MATE_OP_CODE_COMPLETE_KEY:
//				controlCharacteristic = gatt.getService(BLEConsts.ACC_SERVICE_UUID).getCharacteristic(BLEConsts.ACC_CONTROL_UUID);
//				try {
//					writePacket(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.MATE_OP_CODE_COMPLETE_KEY), 1);
//					mReceivedData = characteristic.getValue();
//				} catch (UnknownParametersException e1) {
//					mError = BLEConsts.ERROR_INVALID_PARAMETERS;
//				} catch (BLEAbortedException e1) {
//					mError = BLEConsts.ERROR_ABORTED;
//				}
//				break;
			case BLEConsts.MATE_OP_CODE_REQUEST_KEY:
				controlCharacteristic = gatt.getService(BLEConsts.ACC_SERVICE_UUID).getCharacteristic(BLEConsts.ACC_CONTROL_UUID);
				
				try {
					checkResponseValid(characteristic.getValue(), BLEConsts.MATE_OP_CODE_REQUEST_KEY);
//					if(!mIsPetHomeInit) {
						parserReceivedData(characteristic.getValue());
						if(checkSectionDataComplete(characteristic.getValue())){
							if(DataConfirmFlag == 0){
								return;
							}
							writePacket(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.MATE_OP_CODE_CONFIRM_KEY), 
									BLEConsts.MAX_DATA_CONTROL_WRITE_SIZE);
						}
//					}else {
//						mReceivedData = characteristic.getValue();
//					}
				} catch (UnknownResponseException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				} catch (UnknownParametersException e) {
					mError = BLEConsts.ERROR_INVALID_PARAMETERS;
				} catch (UnexpectedCompleteException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				} catch (BLEAbortedException e) {
					mError = BLEConsts.ERROR_ABORTED;
				} catch (UnsupportedEncodingException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				}
				break;
				////////////////			
			case BLEConsts.OP_CODE_DATA_READ_KEY:
				controlCharacteristic = gatt.getService(BLEConsts.ACC_SERVICE_UUID).getCharacteristic(BLEConsts.ACC_CONTROL_UUID);
				byte[] response = characteristic.getValue();
				try {
					checkResponseValid(response, BLEConsts.OP_CODE_DATA_READ_KEY);
					parserReceivedData(characteristic.getValue());
					if(checkSectionDataComplete(response)){
						writePacket(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DATA_CONFIRM_KEY), 
								BLEConsts.MAX_DATA_CONTROL_WRITE_SIZE);
						updateProgressNotification();
						
						if(mLastProgress == 100){
							mDebugSyncProgressCompleteCount++;
						}
						
						if(mDebugSyncProgressCompleteCount > 3){		//device not complete send data with command E, repeat for three times and them cancel connect and compelete
							mReceivedData = new byte[8];
							mReceivedData[0] = BLEConsts.OP_CODE_DATA_COMPLETE_KEY;

							// notify waiting thread
							synchronized (mLock) {
								mLock.notifyAll();
								return;
							}
						}
					}
					return;
				} catch (UnknownResponseException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				} catch (UnknownParametersException e) {
					mError = BLEConsts.ERROR_INVALID_PARAMETERS;
				} catch (UnexpectedCompleteException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				} catch (BLEAbortedException e) {
					mError = BLEConsts.ERROR_ABORTED;
				} catch (UnsupportedEncodingException e) {
					mError = BLEConsts.ERROR_INVALID_RESPONSE;
				}
				break;
			case BLEConsts.OP_CODE_RESPONSE_CODE_KEY:

			case BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY:
				if(mKeepAliveTimer != null){
					PetkitLog.d("KeepAlive  onCharacteristicChanged -> value (0x): " + parse(characteristic));
					return;
				}
			default:
				
				if (BLEConsts.DFU_PACKET_UUID.equals(characteristic.getUuid())
						|| BLEConsts.DFU_CONTROL_POINT_UUID.equals(characteristic.getUuid())) {
					/*
					 * If the DFU target device is in invalid state (f.e. the Init Packet is required but has not been selected), the target will send DFU_STATUS_INVALID_STATE error
					 * for each firmware packet that was send. We are interested may ignore all but the first one.
					 * After obtaining a remote DFU error the OP_CODE_RESET_KEY will be sent.
					 */
					if (mRemoteErrorOccured)
						break;
					final int status = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
					if (status != BLEConsts.DFU_STATUS_SUCCESS)
						mRemoteErrorOccured = true;
				}
				
				sendLogBroadcast("Notification received from " + characteristic.getUuid() + ", value (0x): " + parse(characteristic));
				mReceivedData = characteristic.getValue();
				break;
			}

			refreshHeartbeatTime();
			stopKeepAlive();
			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		};

		public String parse(final BluetoothGattCharacteristic characteristic) {
			final byte[] data = characteristic.getValue();
			if (data == null)
				return "";
			final int length = data.length;
			if (length == 0)
				return "";

			final char[] out = new char[length * 3 - 1];
			for (int j = 0; j < length; j++) {
				int v = data[j] & 0xFF;
				out[j * 3] = BLEConsts.HEX_ARRAY[v >>> 4];
				out[j * 3 + 1] = BLEConsts.HEX_ARRAY[v & 0x0F];
				if (j != length - 1)
					out[j * 3 + 2] = '-';
			}
			return new String(out);
		}
	};
	
	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list.
	 */
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			if (device != null && checkDeviceFilter(device)) {
				if(deviceInfoExists(device.getAddress())){
					DeviceInfo deviceInfo = findDeviceInfo(device);
					deviceInfo.updateRssi(rssi);
				}else {
					DeviceInfo deviceInfo = createDeviceInfo(device, rssi, scanRecord);
					addScanedDevice(deviceInfo);
					sendScanedDeviceBroadcast(deviceInfo);
					Log.d("petkit", "device name:" + deviceInfo.getName());
					if((targetDeviceId != 0 && targetDeviceId == deviceInfo.getDeviceId())){		//BLEConsts.PET_HOME.equals(deviceInfo.getName())
						mBleDevice = deviceInfo;
						mConnectionState = BLEConsts.STATE_SCANED;
						mDeviceAddress = deviceInfo.getAddress();
					}
				}
			}else {
				if(device != null && scanRecord != null){	//oppo n3 ble special, no device name and no scan record response
					if(deviceInfoExists(device.getAddress())){
						DeviceInfo deviceInfo = findDeviceInfo(device);
						deviceInfo.updateRssi(rssi);
					}else {
						int i = 0; 
						while(i < scanRecord.length - 1){
							int length = scanRecord[i];
							int type = scanRecord[i+1];
							if((type == -1) && length == 26){
								byte[] standardByte = new byte[]{0x00, 0x02, 0x15, (byte) 0xBB, 0x3E, (byte) 0xE6, 0x08, 0x05, 0x72, 0x4D, (byte) 0x9F, (byte) 0x89, (byte) 0xE7, (byte) 0xBF, (byte) 0x91, (byte) 0xD8, (byte) 0xBD, 0x70, 0x5B};
								int j = 0;
								for (; j < standardByte.length; j++) {
									if(scanRecord[i+3+j] != standardByte[j]){
										break;
									}
								}
								if(j == standardByte.length){
									int curDeviceId = 0;
									for(int z = 0; z < 4; z++){
										curDeviceId += ((scanRecord[j + i + 3 + z] & 0xFF) << 8 * (3 - z));
									}
									if(curDeviceId <= 0x10102){
										curDeviceId = 0;
									}
									if(scanRecord[i+26] != (byte)0xC5
											&& scanRecord[i+26] != (byte)0xC3){
										break;
									}
									DeviceInfo deviceInfo = new DeviceInfo();
									deviceInfo.setName(scanRecord[i+26] == (byte)0xC5 ? BLEConsts.PET_FIT_DISPLAY_NAME : BLEConsts.PET_FIT2_DISPLAY_NAME);
									deviceInfo.setMac(device.getAddress());
									deviceInfo.setDeviceId(curDeviceId);
									if(curDeviceId == 0){
										deviceInfo.setChecked(true);
									}
									deviceInfo.setAddress(device.getAddress());
									addScanedDevice(deviceInfo);
									sendScanedDeviceBroadcast(deviceInfo);
									if((targetDeviceId != 0 && targetDeviceId == deviceInfo.getDeviceId())){
										mBleDevice = deviceInfo;
										mConnectionState = BLEConsts.STATE_SCANED;
										mDeviceAddress = deviceInfo.getAddress();
									}
								}
								break;
							}else{
								i += (length + 1);
							}
						}
					}
				}
			}
			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		}
	};
	
	@SuppressLint("NewApi")
	protected boolean initialize(final int action) {
		// For API level 18 and above, get a reference to BluetoothAdapter through
		// BluetoothManager.
		int sdkVersion = CommonUtils.getAndroidSDKVersion();
		if(sdkVersion < 18){
			return false;
		}
		
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager == null) {
			loge("Unable to initialize BluetoothManager.");
			return false;
		}

		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			loge("Unable to obtain a BluetoothAdapter.");
			return false;
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			if(action == BLEConsts.BLE_ACTION_BEACONS){
				return false;
			}
			mBluetoothAdapter.enable();
			
			waitUntilTimeOut(3000);

			synchronized (mLock) {
				while(!timeOut){
					try {
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return true;
	}
	

	@Override
	protected DeviceInfo startScan(Intent intent) {
		mIsScanning = true;
		
		updateProgressNotification(BLEConsts.PROGRESS_SCANING);
		mBluetoothAdapter.startLeScan(mLEScanCallback);
		mConnectionState = BLEConsts.STATE_SCANING;
		mStartTime = SystemClock.elapsedRealtime();
		
		try {
			waitUntilTimeOut(BLEConsts.SCAN_DURATION);
			synchronized (mLock) {
				while (((mConnectionState == BLEConsts.STATE_SCANING && !timeOut) && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		
		stopScan();
		return mBleDevice;
	}
	
	
	/**
	 * Stop scan if user tap Cancel button.
	 */
	private void stopScan() {
		if (mIsScanning) {
			try {
				mBluetoothAdapter.stopLeScan(mLEScanCallback);
			}catch (Exception e){
				PetkitLog.d("petkit stop scan exception");
				e.printStackTrace();
			}
			mIsScanning = false;
		}
		
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
	}
	
	
	private BluetoothGatt connect(String address) {
		mConnectionState = BLEConsts.STATE_CONNECTING;

		logi("Connecting to the device...");
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		final BluetoothGatt gatt = device.connectGatt(this, false, mGattCallback);

		// We have to wait until the device is connected and services are discovered
		// Connection error may occur as well.
		try {
			synchronized (mLock) {
				while (((mConnectionState == BLEConsts.STATE_CONNECTING || mConnectionState == BLEConsts.STATE_CONNECTED) && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		return gatt;
	}
	
	
	private boolean startConnectAndReconnect(Intent intent, String address,
			UUID serviceUUID, UUID controlUUID, UUID dataUUID) throws BLEErrorException{

		if (mAborted) {
			sendLogBroadcast("Upload aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
			return true;
		}
		
//		startHeartbeatToCheckActionInNormal(5000);
		updateProgressNotification(BLEConsts.PROGRESS_CONNECTING);
		gatt = connect(address);
		
		if (mAborted) {
			sendLogBroadcast("Upload aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
			return true;
		}
		
		if (mError <= 0) { // error occurred
			// We have connected to data device and services are discoverer
			final BluetoothGattService dataService = gatt.getService(serviceUUID); 
			if (dataService != null) {
				controlCharacteristic = dataService.getCharacteristic(controlUUID);
				dataCharacteristic = dataService.getCharacteristic(dataUUID);
				if (controlCharacteristic != null && dataCharacteristic != null) {
					return false;
				} else {
					mError = BLEConsts.ERROR_CHARACTERISTICS_NOT_FOUND;
				}
			} else {
				mError = BLEConsts.ERROR_SERVICE_NOT_FOUND;
			}
		}
		
		final int error = mError & ~BLEConsts.ERROR_CONNECTION_MASK;
		loge("An error occurred while connecting to the device:" + error);
		sendLogBroadcast(String.format("Connection failed (0x%02X): %s", error, GattError.parse(error)));

		LogcatStorageHelper.addLog("reconnect device times: " + reconnectTimes);
		if(reconnectTimes < BLEConsts.MAX_RECONNECT_TIMES){
			terminateConnection(gatt, 0);
			
			final Intent newIntent = new Intent();
			newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
			newIntent.putExtra(BLEConsts.EXTRA_DEVICE_RECONNECT_TIMES, ++reconnectTimes);
			startService(newIntent);
		} else {
			terminateConnection(gatt, mError);
		}
		
		return true;
	}

	
	/**
	 * Writes the buffer to the characteristic. The maximum size of the buffer is 20 bytes. This method is ASYNCHRONOUS and returns immediately after adding the data to TX queue.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param buffer
	 *            the buffer with 1-20 bytes
	 * @param size
	 *            the number of bytes from the buffer to send
	 */
	private void writePacket(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] buffer, final int size) {
		byte[] locBuffer = buffer;
		if (buffer.length != size) {
			locBuffer = new byte[size];
			System.arraycopy(buffer, 0, locBuffer, 0, size);
		}
		characteristic.setValue(locBuffer);
		gatt.writeCharacteristic(characteristic);
		
		if(deviceState.getHardware() == 1){
			mProgInfo.iBlocks++;
			mBytesSent++;
			mProgInfo.iBytes += OAD_BLOCK_SIZE;
		}

		lastHeartbeatTimeMillis = System.currentTimeMillis();
		
		// FIXME BLE buffer overflow
		// after writing to the device with WRITE_NO_RESPONSE property the onCharacteristicWrite callback is received immediately after writing data to a buffer.
		// The real sending is much slower than adding to the buffer. This method does not return false if writing didn't succeed.. just the callback is not invoked.
		// 
		// More info: this works fine on Nexus 5 (Andorid 4.4) (4.3 seconds) and on Samsung S4 (Android 4.3) (20 seconds) so this is a driver issue.
		// Nexus 4 and 7 uses Qualcomm chip, Nexus 5 and Samsung uses Broadcom chips.
	}
	
	
	protected void startInitAndChangeFit(final Intent intent) {
		
		// Read input parameters
		final int action = intent.getIntExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_CHECK);
		
		deviceId = null;
		secretKey = null;
		secret = null;
		
		/*
		 * Now let's connect to the device.
		 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
		 */
		sendLogBroadcast("Connecting to target...");

		try {

			if(startConnectAndReconnect(intent, mBleDevice.getAddress(), BLEConsts.ACC_SERVICE_UUID, BLEConsts.ACC_CONTROL_UUID, BLEConsts.ACC_DATA_UUID)){
				return;
			}
			
			// Set up the temporary variable that will hold the responses
			byte[] response = null;
			
			// Enable notifications
			enableCCCD(gatt, dataCharacteristic, BLEConsts.NOTIFICATIONS);
			sendLogBroadcast("Notifications enabled");

			updateProgressNotification(BLEConsts.PROGRESS_CONNECTED);
			startHeartbeatToCheckActionInNormal(5000);
			
			mPaused = true;
			waitIfPaused(true);
			
			if (mAborted) {
				logi("Upload aborted");
				sendLogBroadcast("Upload aborted");
				terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
				return;
			}
			
			if(action == BLEConsts.BLE_ACTION_INIT){
				writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEVICE_INIT_KEY, deviceId, secretKey, secret));
				response = readNotificationResponse();
				checkResponseValid(response, BLEConsts.OP_CODE_DEVICE_INIT_KEY);
				parserReceivedData(response);
			}
			
			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, secret));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
			parserReceivedData(response);
			
			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_TIME_SYNC_KEY));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.OP_CODE_TIME_SYNC_KEY);
			parserReceivedData(response);
			
			// We have connected to data device and services are discoverer
			final BluetoothGattService batteryService = gatt.getService(BLEConsts.BAT_SERV_UUID); 
			if (batteryService == null) {
				loge("battery service does not exists on the device");
				sendLogBroadcast("Connected. battery Service not found");
				terminateConnection(gatt, BLEConsts.ERROR_SERVICE_NOT_FOUND);
				return;
			}
			final BluetoothGattCharacteristic batteryCharacteristic = batteryService.getCharacteristic(BLEConsts.BAT_DATA_UUID);
			if (batteryCharacteristic == null) {
				loge("battery characteristics not found in the battery service");
				sendLogBroadcast("Connected. battery Characteristics not found");
				terminateConnection(gatt, BLEConsts.ERROR_CHARACTERISTICS_NOT_FOUND);
				return;
			}
			deviceState.setBattery(readBattery(gatt, batteryCharacteristic));
			LogcatStorageHelper.addLog("[R-B] battery: " + String.valueOf(deviceState.getBattery()));
			updateProgressNotification(BLEConsts.PROGRESS_SYNC_BATTERY, String.valueOf(deviceState.getBattery()));

			gatt.setCharacteristicNotification(dataCharacteristic, false);
			disconnect(gatt);
			
			// Close the device
			refreshDeviceCache(gatt, false);
			close(gatt);
			updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
			
		} catch (DeviceDisconnectedException e) {
			sendLogBroadcast("Device has disconneted");
			loge(e.getMessage());
			if (mNotificationsEnabled)
				gatt.setCharacteristicNotification(dataCharacteristic, false);
			close(gatt);
			updateProgressNotification(BLEConsts.ERROR_DEVICE_DISCONNECTED); //TODO:
		} catch (UnknownParametersException e) {
			final int error = BLEConsts.ERROR_INVALID_PARAMETERS;
			loge(e.getMessage());
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (UnknownResponseException e) {
			final int error = BLEConsts.ERROR_INVALID_RESPONSE;
			loge(e.getMessage());
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (BLEErrorException e) {
			final int error = e.getErrorNumber() & ~ BLEConsts.ERROR_CONNECTION_MASK;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			terminateConnection(gatt, e.getErrorNumber());
		} catch (BLEAbortedException e) {
			sendLogBroadcast("action aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
		} catch (UnexpectedCompleteException e) {
			final int error = e.getErrorNumber();
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, e.getErrorNumber());
		} catch (UnsupportedEncodingException e) {
			final int error = BLEConsts.ERROR_UNSUPPORTED_ENCODING;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, error);
		}
		
		
	}
	
	
	private BluetoothGattCharacteristic controlCharacteristic;
//	private BluetoothGattCharacteristic dataCharacteristic;
	
	protected void startSyncDevice(final Intent intent) {
		
		// Read input parameters
		mCurDog = (Pet) intent.getSerializableExtra(BLEConsts.EXTRA_DOG);
		
		mDataBuffers.clear();
		mDebugInfor = new StringBuffer();
		mTempDataBuffers.clear();
		DataConfirmFlag = 0; 
		mComdLength = 0;
		mBytesSent = 0;
		
		if(mBleDevice == null){
			updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
			return;
		}
		
		/*
		 * Now let's connect to the device.
		 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
		 */
		sendLogBroadcast("Connecting to target...");

		try {

			if(startConnectAndReconnect(intent, mBleDevice.getAddress(), BLEConsts.ACC_SERVICE_UUID, BLEConsts.ACC_CONTROL_UUID, BLEConsts.ACC_DATA_UUID)){
				return;
			}

			// Enable notifications
			enableCCCD(gatt, dataCharacteristic, BLEConsts.NOTIFICATIONS);
			sendLogBroadcast("Notifications enabled");

			updateProgressNotification(BLEConsts.PROGRESS_CONNECTED);
			
			startHeartbeatToCheckActionInNormal(5000);

			syncData(gatt, controlCharacteristic, mCurDog);
			
			gatt.setCharacteristicNotification(dataCharacteristic, false);
			disconnect(gatt);
			
			// Close the device
			refreshDeviceCache(gatt, false);
			close(gatt);
			saveConfirmedData(mCurDog);
			updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
			
			startServiceToUploadData();
		} catch (DeviceDisconnectedException e) {
			sendLogBroadcast("Device has disconneted");
			// TODO reconnect n times?
			loge(e.getMessage());
			if (mNotificationsEnabled)
				gatt.setCharacteristicNotification(dataCharacteristic, false);
			close(gatt);
			saveConfirmedData(mCurDog);
			sendErrorBroadcast(BLEConsts.ERROR_DEVICE_DISCONNECTED);
		} catch (UnknownParametersException e) {
			final int error = BLEConsts.ERROR_INVALID_PARAMETERS;
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (UnknownResponseException e) {
			final int error = BLEConsts.ERROR_INVALID_RESPONSE;
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (BLEErrorException e) {
			final int error = e.getErrorNumber() & ~ BLEConsts.ERROR_CONNECTION_MASK;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			terminateConnection(gatt, e.getErrorNumber());
		} catch (BLEAbortedException e) {
			saveConfirmedData(mCurDog);
			sendLogBroadcast("action aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
		} catch (UnexpectedCompleteException e) {
			final int error = e.getErrorNumber();
			saveConfirmedData(mCurDog);
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, e.getErrorNumber());
		} catch (UnsupportedEncodingException e) {
			final int error = BLEConsts.ERROR_UNSUPPORTED_ENCODING;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, error);
		}
		
		
	}
	
	
	private void syncData(final BluetoothGatt gatt, final BluetoothGattCharacteristic controlCharacteristic, final Pet curDog) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException, UnknownParametersException, UnknownResponseException, UnexpectedCompleteException, UnsupportedEncodingException{
		
		// Set up the temporary variable that will hold the responses
		byte[] response = null;
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, curDog.getDevice().getSecret()));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
		parserReceivedData(response);
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("hardware", "" + deviceState.getHardware());
		map.put("firmware", "" + deviceState.getFirmware());
		map.put("rssi", "" + mBleDevice.getRssi());
		MobclickAgent.onEventValue(this, "petkit_p_syncDeviceStatus", map, 0);
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_TIME_SYNC_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_TIME_SYNC_KEY);
		parserReceivedData(response);

		if(deviceState.getHardware() == 1){
			// We have connected to data device and services are discoverer
			final BluetoothGattService batteryService = gatt.getService(BLEConsts.BAT_SERV_UUID); 
			if (batteryService == null) {
				loge("battery service does not exists on the device");
				sendLogBroadcast("Connected. battery Service not found");
				terminateConnection(gatt, BLEConsts.ERROR_SERVICE_NOT_FOUND);
				return;
			}
			final BluetoothGattCharacteristic batteryCharacteristic = batteryService.getCharacteristic(BLEConsts.BAT_DATA_UUID);
			if (batteryCharacteristic == null) {
				loge("battery characteristics not found in the battery service");
				sendLogBroadcast("Connected. battery Characteristics not found");
				terminateConnection(gatt, BLEConsts.ERROR_CHARACTERISTICS_NOT_FOUND);
				return;
			}
			deviceState.setBattery(readBattery(gatt, batteryCharacteristic));

			if (deviceState.getBattery() < Consts.DEVICE_LOW_BATTERY) {
				if (!CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).contains("&" + deviceState.getId())) {
					CommonUtils.addSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG, CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG) + "&"
							+ deviceState.getId());
				}
			} else {
				if (CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).contains("&" + deviceState.getId())) {
					CommonUtils.addSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG,
							CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).replace("&" + deviceState.getId(), ""));
				}
			}
			
			LogcatStorageHelper.addLog("[R-B] battery: " + String.valueOf(deviceState.getBattery()));
			updateProgressNotification(BLEConsts.PROGRESS_SYNC_BATTERY, String.valueOf(deviceState.getBattery()));
		} else {
			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_BATTERY_KEY));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.OP_CODE_BATTERY_KEY);
			parserReceivedData(response);
			updateProgressNotification(BLEConsts.PROGRESS_SYNC_BATTERY, String.valueOf(deviceState.getBattery()));
		}
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEBUG_INFOR_KEY));
		response = readNotificationResponse();

		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY);
		parserReceivedData(response);

		updateProgressNotification(BLEConsts.PROGRESS_SYNC_DATA);
		updateProgressNotification();
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DATA_READ_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_DATA_CONFIRM_KEY);
		parserReceivedData(response);

		updateProgressNotification(BLEConsts.PROGRESS_SYNC_DATA_COMPLETED);
		saveConfirmedData(curDog);
	}
	
	
	protected void startOTA(final Intent intent){

		// Read input parameters
		mCurDog = (Pet) intent.getSerializableExtra(BLEConsts.EXTRA_DOG);
		final int action = intent.getIntExtra(BLEConsts.EXTRA_ACTION, 0);
		
		mDataBuffers.clear();
		mDebugInfor = new StringBuffer();
		mTempDataBuffers.clear();
		DataConfirmFlag = 0; 
		mComdLength = 0;
		mBytesSent = 0;
		mImageSizeSent = false;

//		filePath = intent.getStringExtra(BLEConsts.EXTRA_FILE_PATH);

		if(mBleDevice == null){
			updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
			return;
		}
		
		/*
		 * Now let's connect to the device.
		 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
		 */

		if(action != BLEConsts.BLE_ACTION_OTA_RECONNECT){
			sendLogBroadcast("Connecting to target...");
			updateProgressNotification(BLEConsts.PROGRESS_CONNECTING);
		}

		if(action == BLEConsts.BLE_ACTION_OTA_RECONNECT){
			startP2OTA(intent);
			return;
		}
		
		try {

			if(startConnectAndReconnect(intent, mBleDevice.getAddress(), 
					BLEConsts.ACC_SERVICE_UUID, BLEConsts.ACC_CONTROL_UUID, BLEConsts.ACC_DATA_UUID)){
				return;
			}
			
			// Enable notifications
			enableCCCD(gatt, dataCharacteristic, BLEConsts.NOTIFICATIONS);
			sendLogBroadcast("Notifications enabled");

			updateProgressNotification(BLEConsts.PROGRESS_CONNECTED);

			startHeartbeatToCheckActionInNormal(5000);
			syncData(gatt, controlCharacteristic, mCurDog);
			
			updateProgressNotification();
			startServiceToUploadData();
			
			mPaused = true;
			waitIfPaused(true);
			
			if (mAborted) {
				logi("Upload aborted");
				sendLogBroadcast("Upload aborted");
				terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
				return;
			}
			
			if(deviceState.getHardware() == 2){		//PETKIT V2 TODO:
//				startP2OTA(intent);
				
//				writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, mCurDog.getDevice().getSecret()));
//				byte[] response = readNotificationResponse();
//				checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
//				parserReceivedData(response);
				
				writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY));
				waitUntilDisconnected();
				
				if (mAborted) {
					logi("Upload aborted");
					sendLogBroadcast("Upload aborted");
					terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
					return;
				}
				
				disconnect(gatt);
				
				refreshDeviceCache(gatt, false);

				// Close the device
				close(gatt);

				logi("Starting service that will connect to the DFU bootloader");
				final Intent newIntent = new Intent();
				newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
				newIntent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_OTA_RECONNECT);
				startService(newIntent);
			}else {
				/*
				 * First the service is trying to read the firmware and init packet files.
				 */
				sendLogBroadcast("Opening file...");
				if(filePath == null){
					throw new UnknownParametersException("invalid file", BLEConsts.ERROR_FILE_NOT_FOUND);
				}
				mFileBuffer = FileUtils.readFileByteContent(filePath);
				
				startP1OTA(gatt, controlCharacteristic);
				
				waitUntilDisconnected();
				
				PetkitLog.d("ota p1 finish, mError: " + mError);
				if (mError != 0 && mError != (BLEConsts.ERROR_CONNECTION_MASK | 8)){		//android 5.0 special
					throw new BLEErrorException("Uploading Fimrware Image failed", mError);
				}
				
				PetkitLog.d("ota p1 finish, BLEConsts.PROGRESS_BLE_COMPLETED: ");
				updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
			}
			
		} catch (DeviceDisconnectedException e) {
			sendLogBroadcast("Device has disconneted");
			// TODO reconnect n times?
			loge(e.getMessage());
			if (mNotificationsEnabled)
				gatt.setCharacteristicNotification(dataCharacteristic, false);
			close(gatt);
			saveConfirmedData(mCurDog);
			sendErrorBroadcast(BLEConsts.ERROR_DEVICE_DISCONNECTED);
		} catch (UnknownParametersException e) {
			final int error = BLEConsts.ERROR_INVALID_PARAMETERS;
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (UnknownResponseException e) {
			final int error = BLEConsts.ERROR_INVALID_RESPONSE;
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (BLEErrorException e) {
			final int error = e.getErrorNumber() & ~ BLEConsts.ERROR_CONNECTION_MASK;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			saveConfirmedData(mCurDog);
			terminateConnection(gatt, e.getErrorNumber());
		} catch (BLEAbortedException e) {
			saveConfirmedData(mCurDog);
			sendLogBroadcast("action aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
		} catch (UnexpectedCompleteException e) {
			final int error = e.getErrorNumber();
			saveConfirmedData(mCurDog);
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, e.getErrorNumber());
		} catch (IOException e) {
			saveConfirmedData(mCurDog);
			terminateConnection(gatt, BLEConsts.ERROR_FILE_ERROR);
		}
				
	}
	
	private void startP1OTA(final BluetoothGatt gatt, final BluetoothGattCharacteristic controlCharacteristic) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException, UnknownParametersException, UnknownResponseException, UnexpectedCompleteException, IOException{

		// Set up the temporary variable that will hold the responses
		byte[] response = null;
		mBuffer = new byte[OAD_BUFFER_SIZE];
		mComdLength = 0;
		mDebugInfor = new StringBuffer();
		
		// We have connected to data device and services are discoverer
		final BluetoothGattService otaService = gatt.getService(BLEConsts.DFU_SERVICE_UUID);
		if (otaService == null) {
			loge("data service does not exists on the device");
			sendLogBroadcast("Connected. data Service not found");
			terminateConnection(gatt, BLEConsts.ERROR_SERVICE_NOT_FOUND);
			return;
		}
		
		updateProgressNotification(BLEConsts.PROGRESS_OTA_START);
		//TODO:
//		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, mCurDog.getDevice().getSecret()));
//		response = readNotificationResponse();
//		checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
//		parserReceivedData(response);
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY);
		parserReceivedData(response);
		
		if(deviceState.getFirmware() == 1){
			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY);
			parserReceivedData(response);
		}
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_START_RESET_DEBUG_INFOR_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_START_RESET_DEBUG_INFOR_KEY);
		
		writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEBUG_INFOR_KEY));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.OP_CODE_DEBUG_INFOR_KEY);
		
		checkOTADebugInforResetSuccess();

		if(!initBufferData(mFileBuffer)){
			throw new UnknownParametersException("invalid file", BLEConsts.ERROR_INVALID_PARAMETERS, "image type not compared");
		}

		gatt.setCharacteristicNotification(controlCharacteristic, false);
		mNotificationsEnabled = false;

		final BluetoothGattCharacteristic controlPointCharacteristic = otaService.getCharacteristic(BLEConsts.DFU_CONTROL_POINT_UUID);
		final BluetoothGattCharacteristic dataCharacteristic = otaService.getCharacteristic(BLEConsts.DFU_PACKET_UUID);
		if (controlPointCharacteristic == null || dataCharacteristic == null) {
			loge("data characteristics not found in the data service");
			sendLogBroadcast("Connected. data Characteristics not found");
			terminateConnection(gatt, BLEConsts.ERROR_CHARACTERISTICS_NOT_FOUND);
			return;
		}
		
		// Enable notifications
		enableCCCD(gatt, controlPointCharacteristic, BLEConsts.NOTIFICATIONS);
//		
		mProgInfo.reset();
		
		writeOpCode(gatt, controlPointCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_INIT_DFU_PARAMS_KEY));
//		response = readNotificationResponse();
		sendLogBroadcast("OTA Start sent indentify. ");

		try {
			waitUntilTimeOut(10);
			synchronized (mLock) {
				while(!timeOut){
					mLock.wait();
				}
			}
		} catch (InterruptedException e) {
		}
		
		uploadP1FirmwareImage(gatt, dataCharacteristic, mFileBuffer);
		
		updateProgressNotification(100);
	}
	
	
	private void startP2OTA(final Intent intent){
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Read input parameters
		final Uri fileUri = intent.getParcelableExtra(BLEConsts.EXTRA_FILE_URI);
		final String initFilePath = intent.getStringExtra(BLEConsts.EXTRA_INIT_FILE_PATH);
		final Uri initFileUri = intent.getParcelableExtra(BLEConsts.EXTRA_INIT_FILE_URI);
//		final Uri logUri = intent.getParcelableExtra(BLEConsts.EXTRA_LOG_URI);
		int fileType = intent.getIntExtra(BLEConsts.EXTRA_FILE_TYPE, BLEConsts.TYPE_AUTO);
		if (filePath != null && fileType == BLEConsts.TYPE_AUTO)
			fileType = filePath.toLowerCase(Locale.US).endsWith("zip") ? BLEConsts.TYPE_AUTO : BLEConsts.TYPE_APPLICATION;
		String mimeType = intent.getStringExtra(BLEConsts.EXTRA_FILE_MIME_TYPE);
		mimeType = mimeType != null ? mimeType : (fileType == BLEConsts.TYPE_AUTO ? BLEConsts.MIME_TYPE_ZIP : BLEConsts.MIME_TYPE_OCTET_STREAM); // FIXME check if it's better
//		mLogSession = Logger.openSession(this, logUri);
		mPartCurrent = intent.getIntExtra(BLEConsts.EXTRA_PART_CURRENT, 1);
		mPartsTotal = intent.getIntExtra(BLEConsts.EXTRA_PARTS_TOTAL, 1);

		// Check file type and mime-type
		if ((fileType & ~(BLEConsts.TYPE_SOFT_DEVICE | BLEConsts.TYPE_BOOTLOADER | BLEConsts.TYPE_APPLICATION)) > 0 || !(BLEConsts.MIME_TYPE_ZIP.equals(mimeType) || BLEConsts.MIME_TYPE_OCTET_STREAM.equals(mimeType))) {
			logw("File type or file mime-type not supported");
			sendLogBroadcast("File type or file mime-type not supported");
			sendErrorBroadcast(BLEConsts.ERROR_FILE_TYPE_UNSUPPORTED);
			return;
		}
		if (BLEConsts.MIME_TYPE_OCTET_STREAM.equals(mimeType) && fileType != BLEConsts.TYPE_SOFT_DEVICE && fileType != BLEConsts.TYPE_BOOTLOADER && fileType != BLEConsts.TYPE_APPLICATION) {
			logw("Unable to determine file type");
			sendLogBroadcast("Unable to determine file type");
			sendErrorBroadcast(BLEConsts.ERROR_FILE_TYPE_UNSUPPORTED);
			return;
		}

		mPacketsSentSinceNotification = 0;
		mImageSizeSent = false;
		mBuffer = new byte[20];

		// Read preferences
		final boolean packetReceiptNotificationEnabled = preferences.getBoolean(BLEConsts.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, true);
		String value = preferences.getString(BLEConsts.SETTINGS_NUMBER_OF_PACKETS, String.valueOf(BLEConsts.SETTINGS_NUMBER_OF_PACKETS_DEFAULT));
		int numberOfPackets = BLEConsts.SETTINGS_NUMBER_OF_PACKETS_DEFAULT;
		try {
			numberOfPackets = Integer.parseInt(value);
			if (numberOfPackets < 0 || numberOfPackets > 0xFFFF)
				numberOfPackets = BLEConsts.SETTINGS_NUMBER_OF_PACKETS_DEFAULT;
		} catch (final NumberFormatException e) {
			numberOfPackets = BLEConsts.SETTINGS_NUMBER_OF_PACKETS_DEFAULT;
		}
		if (!packetReceiptNotificationEnabled)
			numberOfPackets = 0;
		mPacketsBeforeNotification = numberOfPackets;
		// The Soft Device starts where MBR ends (by default from the address 0x1000). Before there is a MBR section, which should not be transmitted over DFU. 
		// Applications and bootloader starts from bigger address. However, in custom DFU implementations, user may want to transmit the whole whole data, even from address 0x0000.
		value = preferences.getString(BLEConsts.SETTINGS_MBR_SIZE, String.valueOf(BLEConsts.SETTINGS_DEFAULT_MBR_SIZE));
		int mbrSize = BLEConsts.SETTINGS_DEFAULT_MBR_SIZE;
		try {
			mbrSize = Integer.parseInt(value);
			if (mbrSize < 0)
				mbrSize = 0;
		} catch (final NumberFormatException e) {
			mbrSize = BLEConsts.SETTINGS_DEFAULT_MBR_SIZE;
		}

		sendLogBroadcast("Starting DFU service");

		updateProgressNotification(BLEConsts.PROGRESS_OTA_START);
		
		/*
		 * First the service is trying to read the firmware and init packet files.
		 */
		InputStream is = null;
		InputStream initIs = null;
		int imageSizeInBytes;
		try {
			// Prepare data to send, calculate stream size
			try {
				sendLogBroadcast("Opening file...");
				if (fileUri != null) {
					is = openInputStream(fileUri, mimeType, mbrSize, fileType);
				} else {
					is = openInputStream(filePath, mimeType, mbrSize, fileType);
				}

				if (initFileUri != null) {
					// Try to read the Init Packet file from URI
					initIs = getContentResolver().openInputStream(initFileUri);
				} else if (initFilePath != null) {
					// Try to read the Init Packet file from path
					initIs = new FileInputStream(initFilePath);
				}

				mInputStream = is;
				imageSizeInBytes = mImageSizeInBytes = is.available();
				// Update the file type bit field basing on the ZIP content
				if (fileType == BLEConsts.TYPE_AUTO && BLEConsts.MIME_TYPE_ZIP.equals(mimeType)) {
					final ZipHexInputStream zhis = (ZipHexInputStream) is;
					fileType = zhis.getContentType();
				}
				mFileType = fileType;
				// Set the Init packet stream in case of a ZIP file
				if (BLEConsts.MIME_TYPE_ZIP.equals(mimeType)) {
					final ZipHexInputStream zhis = (ZipHexInputStream) is;
					if (fileType == BLEConsts.TYPE_APPLICATION) {
						if (zhis.getApplicationInit() != null)
							initIs = new ByteArrayInputStream(zhis.getApplicationInit());
					} else {
						if (zhis.getSystemInit() != null)
							initIs = new ByteArrayInputStream(zhis.getSystemInit());
					}
				}
				sendLogBroadcast("Image file opened (" + mImageSizeInBytes + " bytes in total)");
			} catch (final SecurityException e) {
				initIs = null;
				loge("A security exception occured while opening file", e);
				updateProgressNotification(BLEConsts.ERROR_FILE_NOT_FOUND);
				return;
			} catch (final FileNotFoundException e) {
				initIs = null;
				loge("An exception occured while opening file", e);
				updateProgressNotification(BLEConsts.ERROR_FILE_NOT_FOUND);
				return;
			} catch (final IOException e) {
				initIs = null;
				loge("An exception occured while calculating file size", e);
				updateProgressNotification(BLEConsts.ERROR_FILE_ERROR);
				return;
			}

			/*
			 * Now let's connect to the device.
			 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
			 */
			sendLogBroadcast("Connecting to DFU target...");
			//has do this before
			gatt = connect(mBleDevice.getAddress());
			// Are we connected?
			if (mError > 0) { // error occurred
				final int error = mError & ~BLEConsts.ERROR_CONNECTION_MASK;
				loge("An error occurred while connecting to the device:" + error);
				sendLogBroadcast(String.format("Connection failed (0x%02X): %s", error, GattError.parse(error)));
				terminateConnection(gatt, mError);
				return;
			}
			if (mAborted) {
				logi("Upload aborted");
				sendLogBroadcast("Upload aborted");
				terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
				return;
			}

			// We have connected to DFU device and services are discoverer
			final BluetoothGattService dfuService = gatt.getService(BLEConsts.DFU_SERVICE_UUID); // there was a case when the service was null. I don't know why
			if (dfuService == null) {
				loge("DFU service does not exists on the device");
				sendLogBroadcast("Connected. DFU Service not found");
				terminateConnection(gatt, BLEConsts.ERROR_SERVICE_NOT_FOUND);
				return;
			}
			final BluetoothGattCharacteristic controlPointCharacteristic = dfuService.getCharacteristic(BLEConsts.DFU_CONTROL_POINT_UUID);
			final BluetoothGattCharacteristic packetCharacteristic = dfuService.getCharacteristic(BLEConsts.DFU_PACKET_UUID);
			if (controlPointCharacteristic == null || packetCharacteristic == null) {
				loge("DFU characteristics not found in the DFU service");
				sendLogBroadcast("Connected. DFU Characteristics not found");
				terminateConnection(gatt, BLEConsts.ERROR_CHARACTERISTICS_NOT_FOUND);
				return;
			}
			/*
			 * The DFU Version characteristic has been added in SDK 7.0.
			 * 
			 * It may return version number in 2 bytes (f.e. 0x05-00), where the first one is the minor version and the second one is the major version.
			 * In case of 0x05-00 the DFU has the version 0.5.
			 * 
			 * Currently the following version numbers are supported:
			 * 
			 *   - 0.1 (0x01-00) - Device is connected to the application, not to the Bootloader. The application supports Long Term Key (LTK) sharing and buttonless update.
			 *                     Enable notifications on the DFU Control Point characteristic and write 0x01-04 into it to jump to the Bootloader. 
			 *                     Check the Bootloader version again for more info about the Bootloader version.
			 *                     
			 *   - 0.5 (0x05-00) - Device is in OTA-DFU Bootloader. The Bootloader supports LTK sharing and required the Extended Init Packet. It supports
			 *                     a SoftDevice, Bootloader or an Application update. SoftDevice and a Bootloader may be sent together.
			 *                     
			 */
			final BluetoothGattCharacteristic versionCharacteristic = dfuService.getCharacteristic(BLEConsts.DFU_VERSION); // this may be null for older versions of the Bootloader

			sendLogBroadcast("Connected. Services discovered");
			try {
				updateProgressNotification(BLEConsts.PROGRESS_STARTING);

				// Read the version number if available. The version number consists of 2 bytes: major and minor. Therefore f.e. the version 5 (00-05) can be read as 0.5.
				int version = 0;
				if (versionCharacteristic != null) {
					version = readVersion(gatt, versionCharacteristic);
					final int minor = (version & 0x0F);
					final int major = (version >> 8);
					logi("Version number read: " + major + "." + minor);
					sendLogBroadcast("Version number read: " + major + "." + minor);
				}

				/*
				 *  Check if we are in the DFU Bootloader or in the Application that supports the buttonless update.
				 *  
				 *  In the DFU from SDK 6.1, which was also supporting the buttonless update, there was no DFU Version characteristic. In that case we may find out whether
				 *  we are in the bootloader or application by simply checking the number of characteristics.  
				 */
				if (version == 1 || ( version == 0 && gatt.getServices().size() > 3  /*No DFU Version char but more services than Generic Access, Generic Attribute, DFU Service */)) {
					// the service is connected to the application, not to the bootloader
					logw("Application with buttonless update found");
					sendLogBroadcast("Application with buttonless update found");

					// if we are bonded we may want to enable Service Changed characteristic indications
					if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						final BluetoothGattService genericAttributeService = gatt.getService(BLEConsts.GENERIC_ATTRIBUTE_SERVICE_UUID);
						if (genericAttributeService != null) {
							final BluetoothGattCharacteristic serviceChangedCharacteristic = genericAttributeService.getCharacteristic(BLEConsts.SERVICE_CHANGED_UUID);
							if (serviceChangedCharacteristic != null) {
								enableCCCD(gatt, serviceChangedCharacteristic, BLEConsts.INDICATIONS);
								sendLogBroadcast("Service Changed indications enabled");
							}
						}
					}

					sendLogBroadcast("Jumping to the DFU Bootloader...");

					// Enable notifications
					enableCCCD(gatt, controlPointCharacteristic, BLEConsts.NOTIFICATIONS);
					sendLogBroadcast("Notifications enabled");

					// Send 'jump to bootloader command' (Start DFU)
					updateProgressNotification(BLEConsts.PROGRESS_ENABLING_DFU_MODE);
					BLEConsts.OP_CODE_START_DFU[1] = 0x04;
					logi("Sending Start DFU command (Op Code = 1, Upload Mode = 4)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_START_DFU, true);
					sendLogBroadcast("Jump to bootloader sent (Op Code = 1, Upload Mode = 4)");

					// The device will reset so we don't have to send Disconnect signal.
					waitUntilDisconnected();
					sendLogBroadcast("Disconnected by the remote device");

					/*
					 * We would like to avoid using the hack with refreshing the device (refresh method is not in the public API). The refresh method clears the cached services and causes a 
					 * service discovery afterwards (when connected). Android, however, does it itself when receive the Service Changed indication when bonded. 
					 * In case of unpaired device we may either refresh the services manually (using the hack), or include the Service Changed characteristic.
					 * 
					 * According to Bluetooth Core 4.0 (and 4.1) specification:
					 * 
					 * [Vol. 3, Part G, 2.5.2 - Attribute Caching]
					 * Note: Clients without a trusted relationship must perform service discovery on each connection if the server supports the Services Changed characteristic.
					 *  
					 * However, as up to Android 5 the system does NOT respect this requirement and servers are cached for every device, even if Service Changed is enabled -> Android BUG?
					 * For bonded devices Android performs service re-discovery when SC indication is received. 
					 */
					refreshDeviceCache(gatt, false);

					// Close the device
					close(gatt);

					logi("Starting service that will connect to the DFU bootloader");
					final Intent newIntent = new Intent();
					newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
					newIntent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_OTA_RECONNECT);
					startService(newIntent);
					return;
				}

				// Enable notifications
				enableCCCD(gatt, controlPointCharacteristic, BLEConsts.NOTIFICATIONS);
				sendLogBroadcast("Notifications enabled");

				try {
					// Set up the temporary variable that will hold the responses
					byte[] response = null;
					int status = 0;

					/*
					 * DFU v.1 supports updating only an Application.
					 * Initializing procedure:
					 * [DFU Start (0x01)] -> DFU Control Point
					 * [App size in bytes (UINT32)] -> DFU Packet
					 * ---------------------------------------------------------------------
					 * DFU v.2 supports updating Soft Device, Bootloader and Application
					 * Initializing procedure:
					 * [DFU Start (0x01), <Update Mode>] -> DFU Control Point
					 * [SD size in bytes (UINT32), Bootloader size in bytes (UINT32), Application size in bytes (UINT32)] -> DFU Packet
					 * where <Upload Mode> is a bit mask:
					 * 0x01 - Soft Device update
					 * 0x02 - Bootloader update
					 * 0x04 - Application update
					 * so that
					 * 0x03 - Soft Device and Bootloader update
					 * If <Upload Mode> equals 5, 6 or 7 DFU target may return OPERATION_NOT_SUPPORTED [10, 01, 03]. In that case service will try to send
					 * Soft Device and/or Bootloader first, reconnect to the new Bootloader and send the Application in the second connection.
					 * --------------------------------------------------------------------
					 * If DFU target supports only DFU v.1 a response [10, 01, 03] will be send as a notification on DFU Control Point characteristic, where:
					 * 10 - Response for...
					 * 01 - DFU Start command
					 * 03 - Operation Not Supported
					 * (see table below)
					 * In that case:
					 * 1. If this is application update - service will try to upload using DFU v.1
					 * 2. In case of SD or BL update an error is returned
					 */

					// Obtain size of image(s)
					int softDeviceImageSize = (fileType & BLEConsts.TYPE_SOFT_DEVICE) > 0 ? imageSizeInBytes : 0;
					int bootloaderImageSize = (fileType & BLEConsts.TYPE_BOOTLOADER) > 0 ? imageSizeInBytes : 0;
					int appImageSize = (fileType & BLEConsts.TYPE_APPLICATION) > 0 ? imageSizeInBytes : 0;
					// The sizes above may be overwritten if a ZIP file was passed
					if (BLEConsts.MIME_TYPE_ZIP.equals(mimeType)) {
						final ZipHexInputStream zhis = (ZipHexInputStream) is;
						softDeviceImageSize = zhis.softDeviceImageSize();
						bootloaderImageSize = zhis.bootloaderImageSize();
						appImageSize = zhis.applicationImageSize();
					}

					try {
						BLEConsts.OP_CODE_START_DFU[1] = (byte) fileType;

						// send Start DFU command to Control Point
						logi("Sending Start DFU command (Op Code = 1, Upload Mode = " + fileType + ")");
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_START_DFU);
						sendLogBroadcast("DFU Start sent (Op Code = 1, Upload Mode = " + fileType + ")");

						// send image size in bytes to DFU Packet
						logi("Sending image size array to DFU Packet (" + softDeviceImageSize + "b, " + bootloaderImageSize + "b, " + appImageSize + "b)");
						writeImageSize(gatt, packetCharacteristic, softDeviceImageSize, bootloaderImageSize, appImageSize);
						sendLogBroadcast("Firmware image size sent (" + softDeviceImageSize + "b, " + bootloaderImageSize + "b, " + appImageSize + "b)");

						// a notification will come with confirmation. Let's wait for it a bit
						response = readNotificationResponse();

						/*
						 * The response received from the DFU device contains:
						 * +---------+--------+----------------------------------------------------+
						 * | byte no | value | description |
						 * +---------+--------+----------------------------------------------------+
						 * | 0 | 16 | Response code |
						 * | 1 | 1 | The Op Code of a request that this response is for |
						 * | 2 | STATUS | See DFU_STATUS_* for status codes |
						 * +---------+--------+----------------------------------------------------+
						 */
						status = getStatusCode(response, BLEConsts.OP_CODE_START_DFU_KEY);
						sendLogBroadcast("Responce received (Op Code = " + response[1] + " Status = " + status + ")");
						if (status != BLEConsts.DFU_STATUS_SUCCESS)
							throw new RemoteDfuException("Starting DFU failed", status);
					} catch (final RemoteDfuException e) {
						try {
							if (e.getErrorNumber() != BLEConsts.DFU_STATUS_NOT_SUPPORTED)
								throw e;

							// If user wants to send soft device and/or bootloader + application we may try to send the Soft Device/Bootloader files first, 
							// and then reconnect and send the application
							if ((fileType & BLEConsts.TYPE_APPLICATION) > 0 && (fileType & (BLEConsts.TYPE_SOFT_DEVICE | BLEConsts.TYPE_BOOTLOADER)) > 0) {
								// Clear the remote error flag
								mRemoteErrorOccured = false;

								logw("DFU target does not support (SD/BL)+App update");
								sendLogBroadcast("DFU target does not support (SD/BL)+App update");

								fileType &= ~BLEConsts.TYPE_APPLICATION; // clear application bit
								mFileType = fileType;
								BLEConsts.OP_CODE_START_DFU[1] = (byte) fileType;
								mPartsTotal = 2;

								// set new content type in the ZIP Input Stream and update sizes of images
								final ZipHexInputStream zhis = (ZipHexInputStream) is;
								zhis.setContentType(fileType);
								try {
									appImageSize = 0;
									mImageSizeInBytes = is.available();
								} catch (final IOException e1) {
									// never happen
								}

								// send Start DFU command to Control Point
								sendLogBroadcast("Sending only SD/BL");
								logi("Resending Start DFU command (Op Code = 1, Upload Mode = " + fileType + ")");
								writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_START_DFU);
								sendLogBroadcast("DFU Start sent (Op Code = 1, Upload Mode = " + fileType + ")");

								// send image size in bytes to DFU Packet
								logi("Sending image size array to DFU Packet: [" + softDeviceImageSize + "b, " + bootloaderImageSize + "b, " + appImageSize + "b]");
								writeImageSize(gatt, packetCharacteristic, softDeviceImageSize, bootloaderImageSize, appImageSize);
								sendLogBroadcast("Firmware image size sent [" + softDeviceImageSize + "b, " + bootloaderImageSize + "b, " + appImageSize + "b]");

								// a notification will come with confirmation. Let's wait for it a bit
								response = readNotificationResponse();
								status = getStatusCode(response, BLEConsts.OP_CODE_START_DFU_KEY);
								sendLogBroadcast("Responce received (Op Code = " + response[1] + " Status = " + status + ")");
								if (status != BLEConsts.DFU_STATUS_SUCCESS)
									throw new RemoteDfuException("Starting DFU failed", status);
							} else
								throw e;
						} catch (final RemoteDfuException e1) {
							if (e1.getErrorNumber() != BLEConsts.DFU_STATUS_NOT_SUPPORTED)
								throw e1;

							// If operation is not supported by DFU target we may try to upload application with legacy mode, using DFU v.1
							if (fileType == BLEConsts.TYPE_APPLICATION) {
								// Clear the remote error flag
								mRemoteErrorOccured = false;

								// The DFU target does not support DFU v.2 protocol
								logw("DFU target does not support DFU v.2");
								sendLogBroadcast("DFU target does not support DFU v.2");

								// send Start DFU command to Control Point
								sendLogBroadcast("Switching to DFU v.1");
								logi("Resending Start DFU command (Op Code = 1)");
								writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_START_DFU); // If has 2 bytes, but the second one is ignored
								sendLogBroadcast("DFU Start sent (Op Code = 1)");

								// send image size in bytes to DFU Packet
								logi("Sending application image size to DFU Packet: " + imageSizeInBytes + " bytes");
								writeImageSize(gatt, packetCharacteristic, mImageSizeInBytes);
								sendLogBroadcast("Firmware image size sent (" + imageSizeInBytes + " bytes)");

								// a notification will come with confirmation. Let's wait for it a bit
								response = readNotificationResponse();
								status = getStatusCode(response, BLEConsts.OP_CODE_START_DFU_KEY);
								sendLogBroadcast("Responce received (Op Code = " + response[1] + ", Status = " + status + ")");
								if (status != BLEConsts.DFU_STATUS_SUCCESS)
									throw new RemoteDfuException("Starting DFU failed", status);
							} else
								throw e1;
						}
					}

					// Since SDK 6.1 this delay is no longer required as the Receive Start DFU notification is postponed until the memory is clear.

					//		if ((fileType & TYPE_SOFT_DEVICE) > 0) {
					//			// In the experimental version of bootloader (SDK 6.0.0) we must wait some time until we can proceed with Soft Device update. Bootloader must prepare the RAM for the new firmware.
					//			// Most likely this step will not be needed in the future as the notification received a moment before will be postponed until Bootloader is ready.
					//			synchronized (this) {
					//				try {
					//					wait(6000);
					//				} catch (final InterruptedException e) {
					//					// do nothing
					//				}
					//			}
					//		}

					// Send DFU Init Packet
					/*
					 * If the DFU Version characteristic is present and the version returned from it is greater or equal to 0.5, the Extended Init Packet is required.
					 * For older versions, or if the DFU Version characteristic is not present (pre SDK 7.0.0), the Init Packet (which could have contained only the firmware CRC) was optional.
					 * To calculate the CRC (CRC-CCTII-16 0xFFFF) the following application may be used: http://www.lammertbies.nl/comm/software/index.html -> CRC library.
					 * 
					 * The Init Packet is read from the [firmware].dat file as a binary file. This means:
					 * 1. If the firmware is in HEX or BIN file, f.e. my_firmware.hex (or .bin), the init packet will be read from my_firmware.dat file.
					 * 2. If the new firmware consists of more files (combined in the ZIP) or the ZIP file is used to store it, the ZIP must additionally contain the following files:
					 * 
					 *    a) If the ZIP file contain a softdevice.hex (or .bin) and/or bootloader.hex (or .bin) the 'system.dat' must also be included.
					 *       In case when both files are present the CRC should be calculated from the two BIN contents merged together.
					 *       This means: if there are softdevice.hex and bootloader.hex files in the ZIP file you have to convert them to BIN
					 *       (f.e. using: http://hex2bin.sourceforge.net/ application), put into one file where the soft device is placed as the first one and calculate the CRC for the 
					 *       whole big file. 
					 *       
					 *    b) If the ZIP file contains a application.hex (or .bin) file the 'application.dat' file must be included and contain the Init packet for the application.
					 */
					if (initIs != null) {
						sendLogBroadcast("Writing Initialize DFU Parameters...");

						logi("Sending the Initialize DFU Parameters START (Op Code = 2, Value = 0)");
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_INIT_DFU_PARAMS_START);

						try {
							byte[] data = new byte[20];
							int size = 0;
							while ((size = initIs.read(data, 0, data.length)) != -1) {
								writeInitPacket(gatt, packetCharacteristic, data, size);
							}
						} catch (final IOException e) {
							loge("Error while reading Init packet file");
							throw new BLEErrorException("Error while reading Init packet file", BLEConsts.ERROR_FILE_ERROR);
						}
						logi("Sending the Initialize DFU Parameters COMPLETE (Op Code = 2, Value = 1)");
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_INIT_DFU_PARAMS_COMPLETE);
						sendLogBroadcast("Initialize DFU Parameters completed");

						// a notification will come with confirmation. Let's wait for it a bit
						response = readNotificationResponse();
						status = getStatusCode(response, BLEConsts.OP_CODE_INIT_DFU_PARAMS_KEY);
						sendLogBroadcast("Responce received (Op Code = " + response[1] + ", Status = " + status + ")");
						if (status != BLEConsts.DFU_STATUS_SUCCESS)
							throw new RemoteDfuException("Device returned error after sending init packet", status);
					} else
						mInitPacketSent = true;

					// Send the number of packets of firmware before receiving a receipt notification
					final int numberOfPacketsBeforeNotification = mPacketsBeforeNotification;
					if (numberOfPacketsBeforeNotification > 0) {
						logi("Sending the number of packets before notifications (Op Code = 8, Value = " + numberOfPacketsBeforeNotification + ")");
						setNumberOfPackets(BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_REQ, numberOfPacketsBeforeNotification);
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_REQ);
						sendLogBroadcast("Packet Receipt Notif Req (Op Code = 8) sent (Value = " + numberOfPacketsBeforeNotification + ")");
					}

					// Initialize firmware upload
					logi("Sending Receive Firmware Image request (Op Code = 3)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_RECEIVE_FIRMWARE_IMAGE);
					sendLogBroadcast("Receive Firmware Image request sent");

					// This allow us to calculate upload time
					final long startTime = mLastProgressTime = mStartTime = SystemClock.elapsedRealtime();
					updateProgressNotification();
					try {
						logi("Starting upload...");
						sendLogBroadcast("Starting upload...");
						response = uploadP2FirmwareImage(gatt, packetCharacteristic, is);
					} catch (final DeviceDisconnectedException e) {
						loge("Disconnected while sending data");
						throw e;
					}
					final long endTime = SystemClock.elapsedRealtime();

					updateProgressNotification(100);
					
					// Check the result of the operation
					status = getStatusCode(response, BLEConsts.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY);
					logi("Response received. Op Code: " + response[0] + " Req Op Code = " + response[1] + ", Status = " + response[2]);
					sendLogBroadcast("Responce received (Op Code = " + response[1] + ", Status = " + status + ")");
					if (status != BLEConsts.DFU_STATUS_SUCCESS)
						throw new RemoteDfuException("Device returned error after sending file", status);

					logi("Transfer of " + mBytesSent + " bytes has taken " + (endTime - startTime) + " ms");
					sendLogBroadcast("Upload completed in " + (endTime - startTime) + " ms");

					// Send Validate request
					logi("Sending Validate request (Op Code = 4)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_VALIDATE);
					sendLogBroadcast("Validate request sent");

					// A notification will come with status code. Let's wait for it a bit.
					response = readNotificationResponse();
					status = getStatusCode(response, BLEConsts.OP_CODE_VALIDATE_KEY);
					logi("Response received. Op Code: " + response[0] + " Req Op Code = " + response[1] + ", Status = " + response[2]);
					sendLogBroadcast("Responce received (Op Code = " + response[1] + ", Status = " + status + ")");
					if (status != BLEConsts.DFU_STATUS_SUCCESS)
						throw new RemoteDfuException("Device returned validation error", status);

					// Disable notifications locally
					updateProgressNotification(BLEConsts.PROGRESS_DISCONNECTING);
					gatt.setCharacteristicNotification(controlPointCharacteristic, false);

					// Send Activate and Reset signal.
					logi("Sending Activate and Reset request (Op Code = 5)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_ACTIVATE_AND_RESET);
					sendLogBroadcast("Activate and Reset request sent");

					// The device will reset so we don't have to send Disconnect signal.
					waitUntilDisconnected();
					sendLogBroadcast("Disconnected by the remote device");

					/*
					 * Since version 0.6 the unpaired bootloader advertises as a different device (with the last byte of its address incremented by 1). 
					 * Therefore there is no need to refresh the cache of the current (DFU bootloader) device. 
					 */
					if (version < 6)
						refreshDeviceCache(gatt, true); // The new application may have lost bonding information (if there was bonding). Force refresh it just for sure.
					// Close the device
					close(gatt);

					// During the update the bonding information on the target device may have been removed.
					// To create bond with the new application set the EXTRA_RESTORE_BOND extra to true.
					// In case the bond information is copied to the new application the new bonding is not required.
					if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						final boolean restoreBond = intent.getBooleanExtra(BLEConsts.EXTRA_RESTORE_BOND, false);

						if (restoreBond || (fileType & (BLEConsts.TYPE_SOFT_DEVICE | BLEConsts.TYPE_BOOTLOADER)) > 0) {
							// In case the SoftDevice and Bootloader were updated the bond information was lost.
							removeBond(gatt.getDevice());

							// Give some time for removing the bond information. 300ms was to short, let's set it to 2 seconds just to be sure.
							synchronized (this) {
								try {
									wait(2000);
								} catch (InterruptedException e) {
									// do nothing
								}
							}
						}

						if (restoreBond && (fileType & BLEConsts.TYPE_APPLICATION) > 0) {
							// Restore pairing when application was updated.
							createBond(gatt.getDevice());
						}
					}

					/*
					 * We need to send PROGRESS_BLE_COMPLETED message only when all files has been transmitted.
					 * In case user wants to send Soft Device and/or Bootloader and Application service will be started twice: one to send SD+BL,
					 * second time to send Application using the new Bootloader. In the first case we do not send PROGRESS_BLE_COMPLETED notification.
					 */
					if (mPartCurrent == mPartsTotal) {
						updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
					} else {
						/*
						 * The current service handle will try to upload Soft Device and/or Bootloader.
						 * We need to enqueue another Intent that will try to send application only.
						 */
						logi("Starting service that will upload application");
						final Intent newIntent = new Intent();
						newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
						newIntent.putExtra(BLEConsts.EXTRA_FILE_MIME_TYPE, BLEConsts.MIME_TYPE_ZIP); // ensure this is set (f.e. for scripts)
						newIntent.putExtra(BLEConsts.EXTRA_FILE_TYPE, BLEConsts.TYPE_APPLICATION); // set the type to application only
						newIntent.putExtra(BLEConsts.EXTRA_PART_CURRENT, mPartCurrent + 1);
						newIntent.putExtra(BLEConsts.EXTRA_PARTS_TOTAL, mPartsTotal);
						newIntent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_OTA_RECONNECT);
						startService(newIntent);
					}
				} catch (final UnknownResponseException e) {
					final int error = BLEConsts.ERROR_INVALID_RESPONSE;
					loge(e.getMessage());
					sendLogBroadcast(e.getMessage());

					logi("Sending Reset command (Op Code = 6)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_RESET);
					sendLogBroadcast("Reset request sent");
					terminateConnection(gatt, error);
				} catch (final RemoteDfuException e) {
					final int error = BLEConsts.ERROR_REMOTE_MASK | e.getErrorNumber();
					loge(e.getMessage());
					sendLogBroadcast(String.format("Remote DFU error: %s", GattError.parse(error)));

					logi("Sending Reset command (Op Code = 6)");
					writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_RESET);
					sendLogBroadcast("Reset request sent");
					terminateConnection(gatt, error);
				}
			} catch (final BLEAbortedException e) {
				logi("Upload aborted");
				sendLogBroadcast("Upload aborted");
				if (mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY)
					try {
						mAborted = false;
						logi("Sending Reset command (Op Code = 6)");
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_RESET);
						sendLogBroadcast("Reset request sent");
					} catch (final Exception e1) {
						// do nothing
					}
				terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
			} catch (final DeviceDisconnectedException e) {
				sendLogBroadcast("Device has disconneted");
				// TODO reconnect n times?
				loge(e.getMessage());
				if (mNotificationsEnabled)
					gatt.setCharacteristicNotification(controlPointCharacteristic, false);
				close(gatt);
				sendErrorBroadcast(BLEConsts.ERROR_DEVICE_DISCONNECTED);
			} catch (final BLEErrorException e) {
				final int error = e.getErrorNumber() & ~BLEConsts.ERROR_CONNECTION_MASK;
				sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
				loge(e.getMessage());
				if (mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY)
					try {
						logi("Sending Reset command (Op Code = 6)");
						writeOpCode(gatt, controlPointCharacteristic, BLEConsts.OP_CODE_RESET);
						sendLogBroadcast("Reset request sent");
					} catch (final Exception e1) {
						// do nothing
					}
				terminateConnection(gatt, e.getErrorNumber());
			}
		} finally {
			try {
				// ensure that input stream is always closed
				mInputStream = null;
				if (is != null)
					is.close();
				is = null;
			} catch (final IOException e) {
				// do nothing
			}
		}
	}
	
	private BluetoothGatt gatt;
	private BluetoothGattCharacteristic dataCharacteristic;
	
	/*
	   * Called when a notification with the current image info has been received
	   */
	protected void onBlockTimer() {
		
		if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
			final byte[] buffer = mBuffer;
			// Prepare block
			buffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
			buffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
			System.arraycopy(mFileBuffer, mProgInfo.iBytes, buffer, 2,
					OAD_BLOCK_SIZE);

			PetkitLog.d("writePacket index: " + mProgInfo.iBlocks + " total: " + mProgInfo.nBlocks);
			writePacket(gatt, dataCharacteristic, buffer, OAD_BUFFER_SIZE);
			updateProgressNotification();
		} else {
			LogcatStorageHelper.addLog("oad complete");
			mReceivedData = new byte[OAD_BUFFER_SIZE];
		}
	}
	/**
	 * Disconnects from the device and cleans local variables in case of error. This method is SYNCHRONOUS and wait until the disconnecting process will be completed.
	 * 
	 * @param gatt
	 *            the GATT device to be disconnected
	 * @param error
	 *            error number
	 */
	private void terminateConnection(final BluetoothGatt gatt, final int error) {
		if (mConnectionState != BLEConsts.STATE_DISCONNECTED
				&& mConnectionState != BLEConsts.STATE_CONNECTING) {
//			updateProgressNotification(BLEConsts.PROGRESS_DISCONNECTING);

			// Disconnect from the device
			disconnect(gatt);
			sendLogBroadcast("Disconnected");
		}

		// Close the device
		refreshDeviceCache(gatt, false);
		close(gatt);
		if(error > 0){
			if(error == 16517){		//TODO:
				mBluetoothAdapter.disable();
			}
			sendErrorBroadcast(error);
		}
	}
	
	
	/**
	 * Enables or disables the notifications for given characteristic. This method is SYNCHRONOUS and wait until the
	 * {@link BluetoothGattCallback#onDescriptorWrite(BluetoothGatt, BluetoothGattDescriptor, int)} will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}. If
	 * connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to enable or disable notifications for
	 * @param enable
	 *            <code>true</code> to enable notifications, <code>false</code> to disable them
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	@SuppressLint("NewApi")
	private void enableCCCD(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int type) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		final String debugString = type == BLEConsts.NOTIFICATIONS ? "notifications" : "indications";
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to set " + debugString + " state", mConnectionState);

		mReceivedData = null;
		mError = 0;
		if ((type == BLEConsts.NOTIFICATIONS && mNotificationsEnabled) || (type == BLEConsts.INDICATIONS && mServiceChangedIndicationsEnabled))
			return;

		logi("Enabling " + debugString + "...");
		sendLogBroadcast("Enabling " + debugString + " for " + characteristic.getUuid());

		// enable notifications locally
		gatt.setCharacteristicNotification(characteristic, true);

		// enable notifications on the device
		final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEConsts.CLIENT_CHARACTERISTIC_CONFIG);
		descriptor.setValue(type == BLEConsts.NOTIFICATIONS ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		sendLogBroadcast("gatt.writeDescriptor(" + descriptor.getUuid() + (type == BLEConsts.NOTIFICATIONS ? ", value=0x01-00)" : ", value=0x02-00)"));
		gatt.writeDescriptor(descriptor);

		// We have to wait until device gets disconnected or an error occur
		try {
			synchronized (mLock) {
				while ((((type == BLEConsts.NOTIFICATIONS && !mNotificationsEnabled) || (type == BLEConsts.INDICATIONS && !mServiceChangedIndicationsEnabled))
						&& mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to set " + debugString + " state", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to set " + debugString + " state", mConnectionState);
	}
	
	
	/**
	 * Clears the device cache. After uploading new firmware the DFU target will have other services than before.
	 * 
	 * @param gatt
	 *            the GATT device to be refreshed
	 * @param force
	 *            <code>true</code> to force the refresh
	 */
	@SuppressLint("NewApi")
	private void refreshDeviceCache(final BluetoothGatt gatt, final boolean force) {
		/*
		 * If the device is bonded this is up to the Service Changed characteristic to notify Android that the services has changed.
		 * There is no need for this trick in that case.
		 * If not bonded the Android is unable to get the information about changing services. The hidden refresh method may be used to force refreshing the device cache. 
		 */
		if (force || (gatt != null && gatt.getDevice() != null && gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE)) {
			sendLogBroadcast("gatt.refresh()");
			/*
			 * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
			 */
			try {
				final Method refresh = gatt.getClass().getMethod("refresh");
				if (refresh != null) {
					final boolean success = (Boolean) refresh.invoke(gatt);
					logi("Refreshing result: " + success);
				}
			} catch (Exception e) {
				loge("An exception occured while refreshing device", e);
				sendLogBroadcast("Refreshing failed");
			}
		}
	}
	
	/**
	 * Disconnects from the device. This is SYNCHRONOUS method and waits until the callback returns new state. Terminates immediately if device is already disconnected. Do not call this method
	 * directly, use {@link #terminateConnection(BluetoothGatt, int)} instead.
	 * 
	 * @param gatt
	 *            the GATT device that has to be disconnected
	 */
	private void disconnect(final BluetoothGatt gatt) {
		if (mConnectionState == BLEConsts.STATE_DISCONNECTED)
			return;

		mConnectionState = BLEConsts.STATE_DISCONNECTING;

		logi("Disconnecting from the device...");
		gatt.disconnect();

		// We have to wait until device gets disconnected or an error occur
		waitUntilDisconnected();

		stopKeepAlive(); // stop mKeepAliveTimer
	}
	
	/**
	 * Closes the GATT device and cleans up.
	 * 
	 * @param gatt
	 *            the GATT device to be closed
	 */
	private void close(final BluetoothGatt gatt) {
		logi("Cleaning up...");
		sendLogBroadcast("gatt.close()");
		gatt.close();
		mConnectionState = BLEConsts.STATE_CLOSED;
	}
	
	/**
	 * Writes the operation code to the characteristic. This method is SYNCHRONOUS and wait until the
	 * {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)} will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}.
	 * If connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU CONTROL POINT
	 * @param value
	 *            the value to write to the characteristic
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private void writeOpCode(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		final boolean reset = value[0] == BLEConsts.OP_CODE_RESET_KEY || value[0] == BLEConsts.OP_CODE_ACTIVATE_AND_RESET_KEY;
		writeOpCode(gatt, characteristic, value, reset);
	}

	/**
	 * Writes the operation code to the characteristic. This method is SYNCHRONOUS and wait until the
	 * {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)} will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}.
	 * If connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU CONTROL POINT
	 * @param value
	 *            the value to write to the characteristic
	 * @param reset
	 *            whether the command trigger restarting the device
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private void writeOpCode(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value, final boolean reset) throws DeviceDisconnectedException, BLEErrorException,
			BLEAbortedException {
		mReceivedData = null;
		mError = 0;
		mRequestCompleted = false;
		/*
		 * Sending a command that will make the DFU target to reboot may cause an error 133 (0x85 - Gatt Error). If so, with this flag set, the error will not be shown to the user
		 * as the peripheral is disconnected anyway. See: mGattCallback#onCharacteristicWrite(...) method
		 */
		mResetRequestSent = reset;

		characteristic.setValue(value);
		sendLogBroadcast("Writing to characteristic " + characteristic.getUuid());
		sendLogBroadcast("gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
		gatt.writeCharacteristic(characteristic);

		// We have to wait for confirmation
		try {
			synchronized (mLock) {
				while ((mRequestCompleted == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (!mResetRequestSent && mError != 0)
			throw new BLEErrorException("Unable to write Op Code " + value[0], mError);
		if (!mResetRequestSent && mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Op Code " + value[0], mConnectionState);
	}
	
	
	/**
	 * Reads the DFU Version characteristic if such exists. Otherwise it returns 0.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to read
	 * @return a version number or 0 if not present on the bootloader
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private int readVersion(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to read version number", mConnectionState);
		// If the DFU Version characteristic is not available we return 0.
		if (characteristic == null)
			return 0;

		mReceivedData = null;
		mError = 0;

		logi("Reading DFU version number...");
		sendLogBroadcast("Reading DFU version number...");

		gatt.readCharacteristic(characteristic);

		// We have to wait until device gets disconnected or an error occur
		try {
			synchronized (mLock) {
				while ((mRequestCompleted == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to read version number", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to read version number", mConnectionState);

		// The version is a 16-bit UINT
		return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
	}
	
	/**
	 * Reads the battery characteristic if such exists. Otherwise it returns 0.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to read
	 * @return battery
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private int readBattery(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to read version number", mConnectionState);
		// If the DFU Version characteristic is not available we return 0.
		if (characteristic == null)
			return 0;

		mRequestCompleted = false;
		mReceivedData = null;
		mError = 0;

		logi("Reading battery...");
		sendLogBroadcast("Reading battery...");

		gatt.readCharacteristic(characteristic);

		// We have to wait until device gets disconnected or an error occur
		try {
			synchronized (mLock) {
				while ((mRequestCompleted == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to read battery", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to read battery", mConnectionState);
		
		return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
	}
	
	/**
	 * Writes the image size to the characteristic. This method is SYNCHRONOUS and wait until the {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}
	 * will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param imageSize
	 *            the image size in bytes
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private void writeImageSize(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int imageSize) throws DeviceDisconnectedException, BLEErrorException,
			BLEAbortedException {
		mReceivedData = null;
		mError = 0;
		mImageSizeSent = false;

		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		characteristic.setValue(new byte[4]);
		characteristic.setValue(imageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
		sendLogBroadcast("Writing to characteristic " + characteristic.getUuid());
		sendLogBroadcast("gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
		gatt.writeCharacteristic(characteristic);

		// We have to wait for confirmation
		try {
			synchronized (mLock) {
				while ((mImageSizeSent == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to write Image Size", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Image Size", mConnectionState);
	}

	/**
	 * <p>
	 * Writes the Soft Device, Bootloader and Application image sizes to the characteristic. Soft Device and Bootloader update was added in DFU v.2. Sizes of SD, BL and App are uploaded as 3x UINT32
	 * even though some of them may be 0s. F.e. if only App is being updated the data will be <0x00000000, 0x00000000, [App size]>
	 * </p>
	 * <p>
	 * This method is SYNCHRONOUS and wait until the {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)} will be called or the connection state will
	 * change from {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or an error will occur, an exception will be thrown.
	 * </p>
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param softDeviceImageSize
	 *            the Soft Device image size in bytes
	 * @param bootloaderImageSize
	 *            the Bootloader image size in bytes
	 * @param appImageSize
	 *            the Application image size in bytes
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private void writeImageSize(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int softDeviceImageSize, final int bootloaderImageSize, final int appImageSize)
			throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		mReceivedData = null;
		mError = 0;
		mImageSizeSent = false;

		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		characteristic.setValue(new byte[12]);
		characteristic.setValue(softDeviceImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
		characteristic.setValue(bootloaderImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 4);
		characteristic.setValue(appImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 8);
		sendLogBroadcast("Writing to characteristic " + characteristic.getUuid());
		sendLogBroadcast("gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
		gatt.writeCharacteristic(characteristic);

		// We have to wait for confirmation
		try {
			synchronized (mLock) {
				while ((mImageSizeSent == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to write Image Sizes", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Image Sizes", mConnectionState);
	}

	/**
	 * Writes the Init packet to the characteristic. This method is SYNCHRONOUS and wait until the {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}
	 * will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}. If connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device
	 * @param characteristic
	 *            the characteristic to write to. Should be the DFU PACKET
	 * @param initPacket
	 *            the init packet as a byte array. This must be shorter or equal to 20 bytes (TODO check this restriction).
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	private void writeInitPacket(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] buffer, final int size) throws DeviceDisconnectedException, BLEErrorException,
			BLEAbortedException {
		byte[] locBuffer = buffer;
		if (buffer.length != size) {
			locBuffer = new byte[size];
			System.arraycopy(buffer, 0, locBuffer, 0, size);
		}
		mReceivedData = null;
		mError = 0;
		mInitPacketSent = false;

		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		characteristic.setValue(locBuffer);
		logi("Sending init packet (Value = " + parse(locBuffer) + ")");
		sendLogBroadcast("Writing to characteristic " + characteristic.getUuid());
		sendLogBroadcast("gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
		gatt.writeCharacteristic(characteristic);

		// We have to wait for confirmation
		try {
			synchronized (mLock) {
				while ((mInitPacketSent == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to write Init DFU Parameters", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Init DFU Parameters", mConnectionState);
	}

	/**
	 * Starts sending the data. This method is SYNCHRONOUS and terminates when the whole file will be uploaded or the connection status will change from {@link #STATE_CONNECTED_AND_READY}. If
	 * connection state will change, or an error will occur, an exception will be thrown.
	 * 
	 * @param gatt
	 *            the GATT device (DFU target)
	 * @param packetCharacteristic
	 *            the characteristic to write file content to. Must be the DFU PACKET
	 * @return The response value received from notification with Op Code = 3 when all bytes will be uploaded successfully.
	 * @throws DeviceDisconnectedException
	 *             Thrown when the device will disconnect in the middle of the transmission. The error core will be saved in {@link #mConnectionState}.
	 * @throws BLEErrorException
	 *             Thrown if DFU error occur
	 * @throws BLEAbortedException
	 */
	private byte[] uploadP2FirmwareImage(final BluetoothGatt gatt, final BluetoothGattCharacteristic packetCharacteristic, final InputStream inputStream) throws DeviceDisconnectedException,
			BLEErrorException, BLEAbortedException {
		mReceivedData = null;
		mError = 0;

		final byte[] buffer = mBuffer;
		try {
			final int size = inputStream.read(buffer);
			sendLogBroadcast("Sending firmware to characteristic " + packetCharacteristic.getUuid() + "...");
			writePacket(gatt, packetCharacteristic, buffer, size);

			startHeartbeatToCheckActionInNormal(30000);
		} catch (final HexFileValidationException e) {
			throw new BLEErrorException("HEX file not valid", BLEConsts.ERROR_FILE_INVALID);
		} catch (final IOException e) {
			throw new BLEErrorException("Error while reading file", BLEConsts.ERROR_FILE_IO_EXCEPTION);
		}

		try {
			synchronized (mLock) {
				while ((mReceivedData == null && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Uploading Fimrware Image failed", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Uploading Fimrware Image failed: device disconnected", mConnectionState);

		return mReceivedData;
	}
	
	private byte[] uploadP1FirmwareImage(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic packetCharacteristic,
			final byte[] fileBuffer) throws DeviceDisconnectedException,
			BLEErrorException, BLEAbortedException, HexFileValidationException, IOException {
		mReceivedData = null;
		mError = 0;

		final byte[] buffer = mBuffer;
		// Prepare block
		buffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
		buffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
		System.arraycopy(mFileBuffer, mProgInfo.iBytes, buffer, 2,
				OAD_BLOCK_SIZE);
		sendLogBroadcast("Sending firmware to characteristic "
						+ packetCharacteristic.getUuid() + "...");
		writePacket(gatt, packetCharacteristic, buffer, buffer.length);

		startHeartbeatToCheckActionInNormal(30000);
		
		try {
			synchronized (mLock) {
				while ((mReceivedData == null
						&& mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY
						&& mError == 0 && !mAborted)
						|| mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Uploading Fimrware Image failed",
					mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException(
					"Uploading Fimrware Image failed: device disconnected",
					mConnectionState);

		return mReceivedData;
	}
	
	
	
	private void writeSyncCode(final BluetoothGatt gatt,
			final BluetoothGattCharacteristic characteristic,
			final byte[] value)
			throws DeviceDisconnectedException, BLEErrorException,
			BLEAbortedException {
		mReceivedData = null;
		mError = 0;
		mRequestCompleted = false;
		/*
		 * Sending a command that will make the DFU target to reboot may cause
		 * an error 133 (0x85 - Gatt Error). If so, with this flag set, the
		 * error will not be shown to the user as the peripheral is disconnected
		 * anyway. See: mGattCallback#onCharacteristicWrite(...) method
		 */
		PetkitLog.d("writeSyncCode  " + parse(value));
		characteristic.setValue(value);
		sendLogBroadcast("writeSyncCode" + characteristic.getUuid());
		gatt.writeCharacteristic(characteristic);
		refreshHeartbeatTime();

		// We have to wait for confirmation
		try {
			synchronized (mLock) {
				while ((mRequestCompleted == false && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY
						&& mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (!mResetRequestSent && mError != 0)
			throw new BLEErrorException("Unable to write Op Code " + value[0], mError);
		if (!mResetRequestSent && mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Op Code " + value[0], mConnectionState);
	}

	private void writeKeepAliveCode(final BluetoothGatt gatt,
									final BluetoothGattCharacteristic characteristic,
									final byte[] value)
			throws DeviceDisconnectedException, BLEErrorException,
			BLEAbortedException {
		/*
		 * Sending a command that will make the DFU target to reboot may cause
		 * an error 133 (0x85 - Gatt Error). If so, with this flag set, the
		 * error will not be shown to the user as the peripheral is disconnected
		 * anyway. See: mGattCallback#onCharacteristicWrite(...) method
		 */
		PetkitLog.d("writeKeepAliveCode  " + parse(value));
		characteristic.setValue(value);
//		sendLogBroadcast("writeSyncCode" + characteristic.getUuid());
		gatt.writeCharacteristic(characteristic);

	}
	
	protected void stop() {

	}
	
	
	

	@Override
	protected void startMateWifiInit(final Intent intent) {
		
		// Read input parameters
		mDataBuffers.clear();
		mDebugInfor = new StringBuffer();
		mTempDataBuffers.clear();
		DataConfirmFlag = 0; 
		mComdLength = 0;
		mBytesSent = 0;
		//////
		mTempReceivedWifiData.clear();
	
		if(mBleDevice == null){
			updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
			return;
		}
		
		/*
		 * Now let's connect to the device.
		 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
		 */
		sendLogBroadcast("Connecting to target...");
		try {
			if(startConnectAndReconnect(intent, mBleDevice.getAddress(), 
					BLEConsts.ACC_SERVICE_UUID, BLEConsts.ACC_CONTROL_UUID, BLEConsts.ACC_DATA_UUID)){
				return;
			}
			
			// Enable notifications
			enableCCCD(gatt, dataCharacteristic, BLEConsts.NOTIFICATIONS);
			sendLogBroadcast("Notifications enabled");

			updateProgressNotification(BLEConsts.PROGRESS_CONNECTED);
			startHeartbeatToCheckActionInNormal(5000);
			
			// Set up the temporary variable that will hold the responses
			byte[] response = null;

			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, secret));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
			parserReceivedData(response);

			doMateWifiInit(gatt, controlCharacteristic);
			
			if (mAborted) {
				throw new BLEAbortedException();
			}
			
			gatt.setCharacteristicNotification(dataCharacteristic, false);
			disconnect(gatt);
			
			// Close the device
			refreshDeviceCache(gatt, false);
			close(gatt);
			updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
			
		} catch (DeviceDisconnectedException e) {
			sendLogBroadcast("Device has disconneted");
			loge(e.getMessage());
			if (mNotificationsEnabled)
				gatt.setCharacteristicNotification(dataCharacteristic, false);
			close(gatt);
//			saveConfirmedData(mCurDog);
			sendErrorBroadcast(BLEConsts.ERROR_DEVICE_DISCONNECTED);
		} catch (UnknownParametersException e) {
			final int error = BLEConsts.ERROR_INVALID_PARAMETERS;
			loge(e.getMessage());
//			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (UnknownResponseException e) {
			final int error = BLEConsts.ERROR_INVALID_RESPONSE;
			loge(e.getMessage());
//			saveConfirmedData(mCurDog);
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (BLEErrorException e) {
			final int error = e.getErrorNumber() & ~ BLEConsts.ERROR_CONNECTION_MASK;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
//			saveConfirmedData(mCurDog);
			terminateConnection(gatt, e.getErrorNumber());
		} catch (BLEAbortedException e) {
//			saveConfirmedData(mCurDog);
			sendLogBroadcast("action aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
		} catch (UnexpectedCompleteException e) {
			final int error = e.getErrorNumber();
//			saveConfirmedData(mCurDog);
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, e.getErrorNumber());
		} catch (UnsupportedEncodingException e) {
			final int error = BLEConsts.ERROR_UNSUPPORTED_ENCODING;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, error);
		}
	}
	
	protected void startInitAndChangeMate(final Intent intent){
		
		// Read input parameters
		final int action = intent.getIntExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_CHECK);
		
		deviceId = null;
		secretKey = null;
//		secret = null;
		
		/*
		 * Now let's connect to the device.
		 * All the methods below are synchronous. The mLock object is used to wait for asynchronous calls.
		 */
		sendLogBroadcast("Connecting to target...");

		try {
			if(startConnectAndReconnect(intent, mBleDevice.getAddress(), 
					BLEConsts.ACC_SERVICE_UUID, BLEConsts.ACC_CONTROL_UUID, BLEConsts.ACC_DATA_UUID)){
				return;
			}
			
			// Set up the temporary variable that will hold the responses
			byte[] response = null;
			
			// Enable notifications
			enableCCCD(gatt, dataCharacteristic, BLEConsts.NOTIFICATIONS);
			sendLogBroadcast("Notifications enabled");

			startHeartbeatToCheckActionInNormal(5000);
			mReceivedWifiData = new ByteArrayBuffer(3000);
			writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_GET_SN));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
			parserReceivedData(response);
			
			if(action == BLEConsts.BLE_ACTION_CHANGE_HS){
				writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, secret));
				response = readNotificationResponse();
				checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
				parserReceivedData(response);
				
				////////////////
				if(mateServer == null){
					throw new UnknownParametersException("mate server is null", BLEConsts.MATE_COMMAND_WRITE_SERVER);
				}
				writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_WRITE_SERVER, mateServer));
				response = readNotificationResponse();
				checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
				parserReceivedData(response);
			////////////////////////	
			}

			updateProgressNotification(BLEConsts.PROGRESS_CONNECTED);
			PetkitLog.d("mate init pause 1");
			
			mPaused = true;
			waitIfPaused(true);
			
			PetkitLog.d("mate init continue 1");
			
			if (mAborted) {
				logi("Upload aborted");
				sendLogBroadcast("Upload aborted");
				terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
				return;
			}
			
			if(action == BLEConsts.BLE_ACTION_INIT_HS) {
				writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEVICE_INIT_KEY, deviceId, secretKey, secret));
				response = readNotificationResponse();
				checkResponseValid(response, BLEConsts.OP_CODE_DEVICE_INIT_KEY);
				parserReceivedData(response);

				if(mateServer == null){
					throw new UnknownParametersException("mate server is null", BLEConsts.MATE_COMMAND_WRITE_SERVER);
				}
				writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_WRITE_SERVER, mateServer));
				response = readNotificationResponse();
				checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
				parserReceivedData(response);
			}
			
//			writeSyncCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_VERIFY_KEY, secret));
//			response = readNotificationResponse();
//			checkResponseValid(response, BLEConsts.OP_CODE_VERIFY_KEY);
//			parserReceivedData(response);
//			
//			if(mateServer == null){
//				throw new UnknownParametersException("mate server is null", BLEConsts.MATE_COMMAND_WRITE_SERVER);
//			}
//			writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_WRITE_SERVER, mateServer));
//			response = readNotificationResponse();
//			checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
//			parserReceivedData(response);
			
			if(action == BLEConsts.BLE_ACTION_INIT_HS){
				updateProgressNotification(BLEConsts.PROGRESS_MATE_SERVER_SET_COMPLETE);
				
				PetkitLog.d("mate init pause 2");
				mPaused = true;
				waitIfPaused(true);
				PetkitLog.d("mate init continue 2");
			}
			
			doMateWifiInit(gatt, controlCharacteristic);

			if (mAborted) {
				throw new BLEAbortedException();
			}
			
			gatt.setCharacteristicNotification(dataCharacteristic, false);
			disconnect(gatt);
			
			// Close the device
			refreshDeviceCache(gatt, false);
			close(gatt);
			updateProgressNotification(BLEConsts.PROGRESS_BLE_COMPLETED);
			
		} catch (DeviceDisconnectedException e) {
			sendLogBroadcast("Device has disconneted");
			// TODO reconnect n times?
			loge(e.getMessage());
			if (mNotificationsEnabled)
				gatt.setCharacteristicNotification(dataCharacteristic, false);
			close(gatt);
			updateProgressNotification(BLEConsts.ERROR_DEVICE_DISCONNECTED); //TODO:
		} catch (UnknownParametersException e) {
			final int error = BLEConsts.ERROR_INVALID_PARAMETERS;
			loge(e.getMessage());
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (UnknownResponseException e) {
			final int error = BLEConsts.ERROR_INVALID_RESPONSE;
			loge(e.getMessage());
			sendLogBroadcast(e.getMessage());
			terminateConnection(gatt, error);
		} catch (BLEErrorException e) {
			final int error = e.getErrorNumber() & ~ BLEConsts.ERROR_CONNECTION_MASK;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			terminateConnection(gatt, e.getErrorNumber());
		} catch (BLEAbortedException e) {
			sendLogBroadcast("action aborted");
			terminateConnection(gatt, BLEConsts.ERROR_ABORTED);
		} catch (UnexpectedCompleteException e) {
			final int error = e.getErrorNumber();
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, e.getErrorNumber());
		} catch (UnsupportedEncodingException e) {
			final int error = BLEConsts.ERROR_UNSUPPORTED_ENCODING;
			sendLogBroadcast(String.format("Error (0x%02X): %s", error, GattError.parse(error)));
			loge(e.getMessage());
			terminateConnection(gatt, error);
		}
		
	}
	
	private void doMateWifiInit(final BluetoothGatt gatt, final BluetoothGattCharacteristic controlCharacteristic) throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException, UnknownParametersException, UnknownResponseException, UnexpectedCompleteException, UnsupportedEncodingException{

		byte[] response;
		
		updateProgressNotification(BLEConsts.PROGRESS_WIFI_SET_START);

		PetkitLog.d("mate init pause 3");
		mPaused = true;
		waitIfPaused(true);

		PetkitLog.d("mate init continue 3");
		if (mAborted) {
			throw new BLEAbortedException();
		}
		
		//branker add
		mReceivedWifiData = new ByteArrayBuffer(1500);

		startHeartbeatToCheckActionInNormal(10000);
		writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_GET_WIFI));
		response = readNotificationResponse();
		checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
		parserReceivedData(response);
		
		mPaused = true;
		waitIfPaused(true);

		if (mAborted) {
			throw new BLEAbortedException();
		}
		
		startHeartbeatToCheckActionInNormal(30000);
		while (!mAborted) {
			writeSyncCode(gatt, controlCharacteristic, buildMateOpCodeBuffer(BLEConsts.MATE_COMMAND_WRITE_WIFI, secretKey, secret));
			response = readNotificationResponse();
			checkResponseValid(response, BLEConsts.MATE_OP_CODE_COMPLETE_KEY);
			parserReceivedData(response);
			
			if(!isWriteWifiSuccess){
				mPaused = true;
				waitIfPaused(true);
			} else {
				break;
			}
		}
	}

	@Override
	protected void sendKeepAliveMessage() {
		try {
			if(gatt != null && controlCharacteristic != null){
				writeKeepAliveCode(gatt, controlCharacteristic, buildOpCodeBuffer(BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY));
			}
		} catch (DeviceDisconnectedException e) {
			e.printStackTrace();
		} catch (BLEErrorException e) {
			e.printStackTrace();
		} catch (BLEAbortedException e) {
			e.printStackTrace();
		} catch (UnknownParametersException e) {
			e.printStackTrace();
		}

	}

}
