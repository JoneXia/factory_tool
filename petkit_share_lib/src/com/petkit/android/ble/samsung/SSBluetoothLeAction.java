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

import android.annotation.SuppressLint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.Conversion;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

@SuppressLint({ "NewApi", "UseValueOf", "SimpleDateFormat" })
public class SSBluetoothLeAction extends BaseBluetoothLeAction {
	
	private BluetoothGatt mGatt = null;
	private BluetoothGattCharacteristic mWriteCharacteristic;
	private BluetoothGattCharacteristic mReadCharacteristic;
	private SSBluetoothLeService mHrpService = null;
	
	private BluetoothGattCharacteristic mP2OTAControlCharacteristic, mP2OTAPacketCharacteristic;
	
	public SSBluetoothLeAction() {

	}

	public void setBluetoothLeService(SSBluetoothLeService BLeService) {
		mHrpService = BLeService;
	}
	
	public void setBluetoothGatt(BluetoothGatt gatt) {
		mGatt = gatt;
	}
	
	public void setWriteCharacteristic(BluetoothGattCharacteristic charac){
		mWriteCharacteristic = charac;
	}
	
	public void setP2OTACharacteristic(BluetoothGattCharacteristic controlCharacteristic, BluetoothGattCharacteristic packetCharacteristic){
		mP2OTAControlCharacteristic = controlCharacteristic;
		mP2OTAPacketCharacteristic = packetCharacteristic;
	}
	
	public void setReadCharacteristic(BluetoothGattCharacteristic charac) {
		mReadCharacteristic = charac;

		mHrpService.setCharacteristicNotification(mReadCharacteristic, true);
		mHrpService.waitIdle(SSBluetoothLeService.GATT_TIMEOUT);
		mGatt.readCharacteristic(mReadCharacteristic);
	}

	
	public void getBattery(){
		
		UUID servUuid = BLEConsts.BAT_SERV_UUID;
		UUID dataUuid = BLEConsts.BAT_DATA_UUID;
		BluetoothGattService serv = mHrpService.getBtGattService(servUuid);
		if(serv == null){
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattService null");
			return;
		}
		BluetoothGattCharacteristic charac = serv.getCharacteristic(dataUuid);
		if(charac == null){
			LogcatStorageHelper.addLog("enableNotifications BluetoothGattCharacteristic null");
			return;
		}
		mHrpService.readCharacteristic(charac);
//		mHrpService.setCharacteristicNotification(charac, true);
	}
	

	protected void sendCharacterToDevice(byte[] message) {
		
		StringBuilder stringBuilder = new StringBuilder(message.length);
		for (byte tempByte : message)
			stringBuilder.append(String.format("%02X ", tempByte));
		char c = (char) message[0]; 
		LogcatStorageHelper.addLog("[CMD-" + c + "]" + " WriteCharacteristic data: " + stringBuilder.toString());
		
		if(mWriteCharacteristic == null){
			if(mBleListener != null){
				LogcatStorageHelper.addLog("[CMD-" + c + "]" + " onInitDeviceFail mWriteCharacteristic == null");
				mBleListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
			}
			return;
		}
		mWriteCharacteristic.setValue(message);
		mGatt.writeCharacteristic(mWriteCharacteristic);
		if(!mHrpService.waitIdle(SSBluetoothLeService.GATT_TIMEOUT)){
			LogcatStorageHelper.addLog("[CMD-" + c + "]" + " WriteCharacteristic data timeout fail.");
			saveConfirmedData();
			if(mBleListener != null){
				mBleListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
			}
		}
	}
	
	
	private void writeOpCode(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value){
		final boolean reset = value[0] == BLEConsts.OP_CODE_RESET_KEY || value[0] == BLEConsts.OP_CODE_ACTIVATE_AND_RESET_KEY;
		writeOpCode(gatt, characteristic, value, reset);
	}
	
	private void writeOpCode(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value, final boolean reset) {
		/*
		 * Sending a command that will make the DFU target to reboot may cause an error 133 (0x85 - Gatt Error). If so, with this flag set, the error will not be shown to the user
		 * as the peripheral is disconnected anyway. See: mGattCallback#onCharacteristicWrite(...) method
		 */
		
		characteristic.setValue(value);
		gatt.writeCharacteristic(characteristic);

	}
	
	private void writeImageSize(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int softDeviceImageSize, final int bootloaderImageSize, final int appImageSize)
			{
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		characteristic.setValue(new byte[12]);
		characteristic.setValue(softDeviceImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
		characteristic.setValue(bootloaderImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 4);
		characteristic.setValue(appImageSize, BluetoothGattCharacteristic.FORMAT_UINT32, 8);
		gatt.writeCharacteristic(characteristic);
	}

	
	private void writePacket(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] buffer, final int size) {
		byte[] locBuffer = buffer;
		if (buffer.length != size) {
			locBuffer = new byte[size];
			System.arraycopy(buffer, 0, locBuffer, 0, size);
		}
		characteristic.setValue(locBuffer);
		gatt.writeCharacteristic(characteristic);
		
		mBleListener.updateProgress(mBytesSent * 100 / mImageSizeInBytes, null);
		
		PetkitLog.d("write packet index: " + mPacketsSentSinceNotification);
	}
	
	
	/**
	 *  ---------------------------------P1 OTA-------------------------------------
	 */
	

	private BluetoothGattService mOadService;
	private BluetoothGattCharacteristic mCharIdentify = null;
	private BluetoothGattCharacteristic mCharBlock = null;

	protected InputStream mInputStream;
	
	
	@Override
	protected void startOad(String filePath) {
		
		if(deviceState.getHardware() == 1){
			try {
				mFileBuffer = FileUtils.readFileByteContent(filePath);
			} catch (IOException e) {
				mBleListener.updateProgress(BLEConsts.ERROR_FILE_ERROR, filePath);
				return;
			}
			
			mProgramming = true;
			
			if(initService() && initBufferData(mFileBuffer)){
				startProgramming();		
			}else{
				LogcatStorageHelper.addLog("startOad init fail");
				mBleListener.updateProgress(BLEConsts.ERROR_FILE_ERROR, null);
			}
		}else if(deviceState.getHardware() == 2){
			
			try {
				mInputStream = new FileInputStream(filePath);
				try {
					mImageSizeInBytes = mInputStream.available();
					startProgramming();				//device will reboot when received this command, do next when received diconnected message
				} catch (IOException e) {
					mBleListener.updateProgress(BLEConsts.ERROR_FILE_ERROR, filePath);
				}
			} catch (FileNotFoundException e) {
				mBleListener.updateProgress(BLEConsts.ERROR_FILE_ERROR, filePath);
			}
		}
	}

	@Override
	protected void startIdentify() {
		// Prepare image notification
	    byte[] buf = new byte[13];
	    buf[0] = Conversion.loUint16(mFileImgHdr.ver);
	    buf[1] = Conversion.hiUint16(mFileImgHdr.ver);
	    buf[2] = Conversion.loUint16(mFileImgHdr.len);
	    buf[3] = Conversion.hiUint16(mFileImgHdr.len);
	    System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);
	    
	    mCharIdentify.setValue(buf);
	    mGatt.writeCharacteristic(mCharIdentify);
	    
	 // Initialize stats
	    mProgInfo.reset();

	    // Start the packet timer
	    mTimer = null;
	    mTimer = new Timer();
	    mTimerTask = new ProgTimerTask();
	    mTimer.scheduleAtFixedRate(mTimerTask, PKT_INTERVAL, PKT_INTERVAL);
	}

	private boolean initService() {
		boolean result = false;
		mOadService = mHrpService.getBtGattService(BLEConsts.DFU_SERVICE_UUID);
		if(mOadService != null){
			@SuppressWarnings("unchecked")
			List<BluetoothGattCharacteristic> mCharListOad = mOadService.getCharacteristics();
			result = (mCharListOad.size() == 2);
			if(result){
				mCharIdentify = mCharListOad.get(0);
				mCharBlock = mCharListOad.get(1);
//				mHrpService.setCharacteristicNotification(mCharBlock, true);
			}
		}
		
		return result;
	}

	@Override
	protected void onBlockTimer(int index) {
		
		if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
			mProgramming = true;

			// Prepare block
			mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
			mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
			System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);

			mCharBlock.setValue(mOadBuffer);
			boolean success = (mGatt == null ? false : mGatt.writeCharacteristic(mCharBlock));

			if (success) {
				mHrpService.waitIdle(GATT_WRITE_TIMEOUT);
				// Update stats
				mProgInfo.iBlocks++;
				mProgInfo.iBytes += OAD_BLOCK_SIZE;
				mBleListener.updateProgress((mProgInfo.iBlocks * 100) / mProgInfo.nBlocks, null);
			} else {
				// Check if the device has been prematurely disconnected
				mProgramming = false;
				
				resetSensor();
				LogcatStorageHelper.addLog("oad fail");
				mBleListener.updateProgress(BLEConsts.ERROR_DEVICE_DISCONNECTED, null);
			}
		} else {
			mProgramming = false;
			LogcatStorageHelper.addLog("oad complete");
			mBleListener.updateProgress(BLEConsts.PROGRESS_BLE_COMPLETED, null);
		}
	}

	private int getStatusCode(final byte[] response, final int request) {
		if (response == null || response.length != 3 || response[0] != BLEConsts.OP_CODE_RESPONSE_CODE_KEY || response[1] != request || response[2] < 1 || response[2] > 6){
			return -1;
		}
		return response[2];
	}
	
	//--------------------------  P2 OTA  ------------------------------
	private boolean mImageSizeSent = false;
	private int mPacketsBeforeNotification, mPacketsSentSinceNotification;

	/** Number of bytes confirmed by the notification. */
	protected int mBytesConfirmed;
	
	private int mImageSizeInBytes;
	
	private int mBytesSent;
	
	private byte[] mReceivedData;
	
	private int step = 0;
	
	private boolean waitData = false;
	
	private long lastNotifyTime;
	
	
	@Override
	protected void startUpdate() {
		PetkitLog.d("startUpdate" );
		
		if(deviceState.getHardware() != 2){
			return;
		}
		
		if(mP2OTAControlCharacteristic == null || mP2OTAPacketCharacteristic == null){
			mBleListener.updateProgress(BLEConsts.ERROR_SERVICE_NOT_FOUND, null);
			return;
		}
		
		step = 0;
		mBytesSent = 0;
		mPacketsBeforeNotification = BLEConsts.SETTINGS_NUMBER_OF_PACKETS_DEFAULT;
		
		doP2OTAStepByStep();
	}
	
	public void controlCharacteristicWrite(){
		PetkitLog.d("controlCharacteristicWrite");
		
		if(deviceState.getHardware() != 2){
			return;
		}
		
		if(!waitData)
			doP2OTAStepByStep();
	}
	
	public void packetsCharacteristicChanged(byte[] values){
		PetkitLog.d("packetsCharacteristicChanged values: " );
		
		if(deviceState.getHardware() != 2){
			return;
		}
		
		if(values == null || values.length == 0){
			return;
		}
		
		switch (values[0]) {
		case BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_KEY:
			
//			if(step != 6){
//				return;
//			}
			
			mBytesConfirmed = (int) values[1];
			mPacketsSentSinceNotification = 0;
			LogcatStorageHelper.addLog("writeP2OTAPacket OP_CODE_PACKET_RECEIPT_NOTIF_KEY reset");
			
//			lastNotifyTime = System.currentTimeMillis();
			PetkitLog.d("write packet notify time: " + System.currentTimeMillis());
			
			final byte[] buffer = mP2OadBuffer;
			int size;
			try {
				size = mInputStream.read(buffer);
				writePacket(mGatt, mP2OTAPacketCharacteristic, buffer, size);
			} catch (IOException e) {
			}
			break;

		default:
			if(waitData){
				mReceivedData = values;
				
				PetkitLog.d("mReceivedData " + parse(mReceivedData));
				doP2OTAStepByStep();
			}
			break;
		}
	}
	

	public String parse(byte[] data) {
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
	
	
	private void doP2OTAStepByStep(){
		
		if(deviceState.getHardware() != 2){
			return;
		}
		
		waitData = false;
		
		switch (step++) {
		case 0:
			BLEConsts.OP_CODE_START_DFU[1] = (byte) BLEConsts.TYPE_APPLICATION;
			PetkitLog.d("doP2OTAStepByStep write OP_CODE_START_DFU");
			writeOpCode(mGatt, mP2OTAControlCharacteristic, BLEConsts.OP_CODE_START_DFU);
			break;
		case 1:
			PetkitLog.d("doP2OTAStepByStep writeImageSize");
			writeImageSize(mGatt, mP2OTAPacketCharacteristic, 0, 0, mImageSizeInBytes);
			
			waitData = true;
			break;
		case 2:
			if(getStatusCode(mReceivedData, BLEConsts.OP_CODE_START_DFU_KEY) != BLEConsts.DFU_STATUS_SUCCESS){
				mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
				return;
			}
			doP2OTAStepByStep();
			break;
		case 3:
			if (mPacketsBeforeNotification > 0) {
				BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_REQ[1] = (byte) (mPacketsBeforeNotification & 0xFF);
				BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_REQ[2] = (byte) ((mPacketsBeforeNotification >> 8) & 0xFF);

				PetkitLog.d("doP2OTAStepByStep write OP_CODE_PACKET_RECEIPT_NOTIF_REQ");
				writeOpCode(mGatt, mP2OTAControlCharacteristic, BLEConsts.OP_CODE_PACKET_RECEIPT_NOTIF_REQ);
			} else {
				doP2OTAStepByStep();
			}
			break;
		case 4:
			PetkitLog.d("doP2OTAStepByStep write OP_CODE_RECEIVE_FIRMWARE_IMAGE");
			writeOpCode(mGatt, mP2OTAControlCharacteristic, BLEConsts.OP_CODE_RECEIVE_FIRMWARE_IMAGE);
			break;
		case 5:
			final byte[] buffer = mP2OadBuffer;
			int size;
			try {
				size = mInputStream.read(buffer);
				PetkitLog.d("doP2OTAStepByStep write writePacket");
				writePacket(mGatt, mP2OTAPacketCharacteristic, buffer, size);
				
				waitData = true;
			} catch (IOException e) {
				
			}
			break;
		case 6:
			if(getStatusCode(mReceivedData, BLEConsts.OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY) != BLEConsts.DFU_STATUS_SUCCESS){
				mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
				return;
			}

			mBleListener.updateProgress(100, null);
			PetkitLog.d("doP2OTAStepByStep write OP_CODE_VALIDATE");
			writeOpCode(mGatt, mP2OTAControlCharacteristic, BLEConsts.OP_CODE_VALIDATE);
			waitData = true;
			break;
		case 7:
			if(getStatusCode(mReceivedData, BLEConsts.OP_CODE_VALIDATE_KEY) != BLEConsts.DFU_STATUS_SUCCESS){
				mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
				return;
			}
			
			mGatt.setCharacteristicNotification(mP2OTAControlCharacteristic, false);
			PetkitLog.d("doP2OTAStepByStep write OP_CODE_ACTIVATE_AND_RESET");
			writeOpCode(mGatt, mP2OTAControlCharacteristic, BLEConsts.OP_CODE_ACTIVATE_AND_RESET);
			
			mBleListener.updateProgress(BLEConsts.PROGRESS_BLE_COMPLETED, null);
			break;

		default:
			break;
		}
	}
	
	
	protected boolean timeOut = false;
	private Object mLock = new Object();
	
	protected void waitUntilTimeOut(long duraion) {
		
		if(mTimer == null){
			mTimer = new Timer();
		}
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				timeOut = true;
				synchronized (mLock) {
					mLock.notifyAll();
				}
			}
		}, duraion);
		timeOut = false;
	}
	
	public void writeP2OTAPacket(){

		if(deviceState.getHardware() != 2){	// || step != 6
			return;
		}
		
		if (mImageSizeSent) {
			// if the PACKET characteristic was written with image data, update counters
			mBytesSent += mP2OTAPacketCharacteristic.getValue().length;
			mPacketsSentSinceNotification++;
			
			try {
				waitUntilTimeOut(30);
				synchronized (mLock) {
					while(!timeOut){
						mLock.wait();
					}
				}
			} catch (InterruptedException e) {
			}

			// if a packet receipt notification is expected, or the last packet was sent, do nothing. There onCharacteristicChanged listener will catch either 
			// a packet confirmation (if there are more bytes to send) or the image received notification (it upload process was completed)
			final boolean notificationExpected = mPacketsBeforeNotification > 0 && mPacketsSentSinceNotification == mPacketsBeforeNotification;
			final boolean lastPacketTransfered = mBytesSent == mImageSizeInBytes;

			LogcatStorageHelper.addLog("writeP2OTAPacket mBytesSent: " + mBytesSent);
			if (notificationExpected || lastPacketTransfered){
				LogcatStorageHelper.addLog("notificationExpected || lastPacketTransfered");
				return;
			}

			// when neither of them is true, send the next packet
			try {
				final byte[] buffer = mP2OadBuffer;
				final int size = mInputStream.read(buffer);
				writePacket(mGatt, mP2OTAPacketCharacteristic, buffer, size);
				return;
			} catch (final IOException e) {
			}
		} else if (!mImageSizeSent) {
			// we've got confirmation that the image size was sent
			mImageSizeSent = true;
		}
	}
	
}