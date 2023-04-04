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
import android.util.SparseArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.Conversion;
import com.petkit.android.model.Device;
import com.petkit.android.model.Pet;
import com.petkit.android.model.Extra;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.DeviceActivityDataUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;

@SuppressLint({ "NewApi", "UseValueOf", "SimpleDateFormat" })
public abstract class BaseBluetoothLeAction {
	private final static String TAG = "BaseBluetoothLeAction";
	
	public final static String TIMELINE = "2000-01-01 00:00:00";
	public final static int BLUETOOTHLE_ACTION_SYNC_DATA = 0;
	public final static int BLUETOOTHLE_ACTION_CHANGE_DEVICE = 1;
	public final static int BLUETOOTHLE_ACTION_UPDATE_DEVICE = 2;
	public final static int BLUETOOTHLE_ACTION_INIT_DEVICE = 3;
	public final static int BLUETOOTHLE_ACTION_CHECK_DEVICE = 4;
	
	private static final int RESET_DEBUG_MEMORY_REPEAT_COUNT = 100;
	
	private static int MAX_BLE_TRANSMIT_LEN = 20;
	private static int MAX_BLOCK_SIZE = 32;
	private static final char DEVICE_INIT = 'I';
	private static final char DEVICE_TIME_SYNC = 'T';
	private static final char DEVICE_VERIFY = 'V';
	private static final char DEVICE_DATA_READ = 'D';
	private static final char DEVICE_DATA_CONFIRM = 'C';
	private static final char DEVICE_DEBUG_INFOR = 'M';
	private static final char DEVICE_RESET_DEBUG = 'E';
	private static final char VOLTAGE_KEY = 'B';
	protected static final char DEVICE_DOWNLOAD = 'S';

	private static final char DATA_SPIT = ',';

	protected int DataConfirmFlag = 0x00000000;
	
	protected int mComdLength = 0;
	protected int totalDataLength = 0;
	protected int receiveDataLength = 0;
	protected int deviceUpdateRepeatCount;
	protected int actionModel;
	protected byte currentCommand;
	private Pet mCurDog;
	private String secret;
	
	protected Device deviceState = new Device();
	
	private List<StringBuffer> mDataBuffers = new ArrayList<StringBuffer>();
	private SparseArray<StringBuffer> mTempDataBuffers = new SparseArray<StringBuffer>();
	private StringBuffer mDebugInfor;
	
	protected ISamsungBLEListener mBleListener;
	
	private boolean debug = false;
	private boolean startDebug = false;
	
	
	public BaseBluetoothLeAction() {
		super();
		
		LogcatStorageHelper.addLog("BaseBluetoothLeAction init");
		currentCommand = 0;
	}

	private boolean checkCommandValidState(int newCmd, int oldCmd){
		boolean result = true;
		switch (newCmd) {
		case 'D':
			result = (oldCmd == 'D') || (oldCmd == 'M') || (oldCmd == 'G');
			break;
		case 'T':
			result = (oldCmd == 'V');
			break;
		case 'E':
			result = (oldCmd == 'D') || (oldCmd == 'S') || (oldCmd == 'H') || (oldCmd == 'M') || (oldCmd == 'G');
			break;
		case 'B':
			result = (oldCmd == 'T');
			break;
		case 'M':
			result = (oldCmd == 'M') || (oldCmd == 'B') || (oldCmd == 'E') || (oldCmd == 'G');
			break;
		case 'S':
			result = (oldCmd == 'M') || (oldCmd == 'V') || (oldCmd == 'G') || (oldCmd == 'E') || (oldCmd == 'T');
			break;
		case 'H':
			result = (oldCmd == 'S');
			break;
		case 'V':
		case 'I':
		default:
			break;
		}
		if(oldCmd == 'G'){
			result = true;
		}
		if(!result){
			LogcatStorageHelper.addLog("[ERROR] command invalid, newCmd: " + newCmd + " oldCmd:" + oldCmd);
		}
		return result;
	}

	public void DataCommandParse(byte[] byteChar) {
		int ret, index, blocksize;
		
		if(!checkCommandValidState(byteChar[0], currentCommand)){
			if(byteChar[0] == 'B' && currentCommand == 'B'){
				LogcatStorageHelper.addLog("[ERROR] command invalid, ignore.");
			}else if(mBleListener != null){
				mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
			}
			return;
		}

//		StringBuilder stringBuilder1 = new StringBuilder(byteChar.length);
//		for (int i = 0; i < byteChar.length; i++){
//			byte tempByte = byteChar[i];
//			stringBuilder1.append(String.format("%02X ", tempByte));
//		}
//		LogcatStorageHelper.addLog("DataCommandParse byteChar:" + stringBuilder1.toString());
		
		switch (byteChar[0]) {
		case 'D':
			index = byteChar[1] & 0xFF;
			blocksize = byteChar[2] & 0xFF;
			PetkitLog.d(TAG, "[CMD-D] process data index:" + String.valueOf(index) + '/' + String.valueOf(blocksize));
			StringBuilder stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 2; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[CMD-D] receive index:" + String.valueOf(index) + '/' + String.valueOf(blocksize)
					+ " data: " + stringBuilder.toString());
			
			if(confirm(index)){
				parseMovingData(byteChar);
			}
			
			if ((index+1) == blocksize || (regetMode && checkDataConfirmFlag(DataConfirmFlag, blocksize))) {
				PetkitLog.d(TAG, "send confirmbit to get next packages data. regetMode: " + regetMode);
				LogcatStorageHelper.addLog("[CMD-D] send confirmbit to get next packages data.");
				dataconfirm(blocksize);
			}else if(debug && index == 1){
				LogcatStorageHelper.addLog("debug mode enable");
				if(debug && startDebug){
					debug = false;
					mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
				}
				startDebug = true;
			}
			break;
		case 'T':
			totalDataLength = 0;
			receiveDataLength = 0;
			ret = byteChar[1] & 0xFF;
			for (int i = 0; i < 4; i++) {
				totalDataLength += (byteChar[2+i] & 0x000000FF) << (8*(3 - i));
			}
			PetkitLog.d(TAG, "data length:" + String.valueOf(totalDataLength));
			LogcatStorageHelper.addLog("[CMD-T] data length:" + String.valueOf(totalDataLength));
			
			if(actionModel == BLUETOOTHLE_ACTION_CHANGE_DEVICE || actionModel == BLUETOOTHLE_ACTION_INIT_DEVICE){
				PetkitLog.d(TAG, "Init or change device Success");
				mBleListener.updateProgress(BLEConsts.PROGRESS_BLE_COMPLETED, null);
			}else{
				mBleListener.updateProgress(BLEConsts.PROGRESS_SYNC_BATTERY, null);
				initDataCache();
				if(deviceState.getHardware() == 1){
					getBattery();
				}else{
					readVoltage();
				}
			}
			break;
		case 'V':
			
			deviceState.setVerify(byteChar[1] != 0);
			PetkitLog.d(TAG, "verify:" + String.valueOf(deviceState.isVerify()));
			PetkitLog.d(TAG, "compile time:" + byteChar);
			
			deviceState.setHardware(byteChar[1]);
			deviceState.setFirmware((byteChar[2]));
			
			if(deviceState.getHardware() == 1){
				deviceState.setExtra(new Extra(deviceState.getFirmware() % 2 == 0 ? "A" : "B"));
				deviceState.setFirmware(deviceState.getFirmware() / 2);
			}else {
				deviceState.setExtra(new Extra("A"));
				deviceState.setFirmware(deviceState.getFirmware());
			}
			
			deviceState.setFrequence(byteChar[3] & 0xFF);

			PetkitLog.d(TAG, "hardware: " + deviceState.getHardware());
			PetkitLog.d(TAG, "firmware: " + deviceState.getFirmware());
			PetkitLog.d(TAG, "frequence: " + deviceState.getFrequence());
			
			LogcatStorageHelper.addLog("Current dog id: " + (mCurDog == null ? "null" : mCurDog.getId()) + ", device id: " + (mCurDog == null ? "null" : mCurDog.getDevice().getId()));
			LogcatStorageHelper.addLog("[R-V] verify:" + String.valueOf(deviceState.isVerify()) + "  hardware: " + deviceState.getHardware()
					+ "  firmware: " + deviceState.getFirmware() + "  frequence: " + deviceState.getFrequence());
			
			if(actionModel == BLUETOOTHLE_ACTION_UPDATE_DEVICE){
				LogcatStorageHelper.addLog("[CMD-V] oad verify success");
				if(deviceState.isVerify()){
					mBleListener.updateProgress(BLEConsts.PROGRESS_VERIFY, null);
				}else{
					mBleListener.updateProgress(BLEConsts.ERROR_SYNC_VERIFY_FAIL, null);
				}
			}else{
				mBleListener.updateProgress(BLEConsts.PROGRESS_VERIFY, new Gson().toJson(deviceState));
				synctime();
			}
			break;
		case 'I':		
			ret = byteChar[1] & 0xFF;
			if (1 == ret) {
				LogcatStorageHelper.addLog("[CMD-I] success");
				if(secret == null && (mCurDog == null || mCurDog.getDevice() == null)){	
					LogcatStorageHelper.addLog("[CMD-I] ERROR!!! secret == null && (mCurDog == null || mCurDog.getDevice() == null)");
					mBleListener.updateProgress(BLEConsts.PROGRESS_BLE_COMPLETED, null);
				}else{
					verify(secret == null ? mCurDog.getDevice().getSecret() : secret);
				}
			}else{
				LogcatStorageHelper.addLog("[CMD-I] onInitDeviceFail");
				mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
			}
			break;
		case 'E':
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 0; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[CMD-E] receive " + " data: " + stringBuilder.toString());
			
			if(actionModel == BLUETOOTHLE_ACTION_UPDATE_DEVICE){
				mComdLength = 0;
				getDebugInfor();
			}else{
				LogcatStorageHelper.addLog("[CMD-E] data completed actionModel: " + actionModel);
				saveConfirmedData();
				mBleListener.updateProgress(BLEConsts.PROGRESS_SYNC_DATA_COMPLETED, null);
				mBleListener.updateProgress(BLEConsts.PROGRESS_BLE_COMPLETED, null);
				initDataCache();
			}
			break;
		case 'B':	//battery
			if(deviceState.getHardware() == 1){
				deviceState.setBattery(byteChar[1] & 0xFF);
				LogcatStorageHelper.addLog("[CMD-B] battery: " + deviceState.getBattery());
			}else {
				stringBuilder = new StringBuilder(byteChar.length);
				for (int i = 1; i < byteChar.length && i < 10; i++){
					byte tempByte = byteChar[i];
					stringBuilder.append(String.format("%02X ", tempByte));
				}
				LogcatStorageHelper.addLog("[R-B] " + stringBuilder.toString());
				
				int voltage = byteChar[1] * 16 * 16 + (byteChar[2] & 0xff);
				deviceState.setVoltage(voltage / 1000f);
				deviceState.setBattery(byteChar[3]);
			}

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
			mComdLength = 0;

			mBleListener.updateProgress(BLEConsts.PROGRESS_SYNC_BATTERY, String.valueOf(deviceState.getBattery()));
			getDebugInfor();
			break;
		case 'M':
			PetkitLog.d(TAG, "m command: " + mComdLength);
			if(actionModel == BLUETOOTHLE_ACTION_UPDATE_DEVICE){
				int i = 0;
				stringBuilder = new StringBuilder(byteChar.length);
				for (i = 2; i < byteChar.length && i < 10; i++){
					byte tempByte = byteChar[i];
					stringBuilder.append(String.format("%02X ", tempByte));
					if(byteChar[i] != -1){
						break;
					}
				}
				LogcatStorageHelper.addLog("[CMD-M] " + "debug part: " + mComdLength + " information: " + stringBuilder.toString());
				if(i == byteChar.length || i == 10){
					if(mComdLength < 6){
						getDebugInfor();
					}else{
						startIdentify();		// start p1 ota
					}
				}else{
					if(deviceUpdateRepeatCount-- > 0){
						startResetDebugMemory();
					}else{
						mBleListener.updateProgress(BLEConsts.ERROR_INVALID_RESPONSE, null);
					}
				}
			}else{
				stringBuilder = new StringBuilder(byteChar.length);
				for (int i = 2; i < byteChar.length && i < 10; i++){
					byte tempByte = byteChar[i];
					stringBuilder.append(String.format("%02X ", tempByte));
				}
				mDebugInfor.append(stringBuilder.toString());
				
				if(mComdLength < 6){
					getDebugInfor();
				}else{
					LogcatStorageHelper.addLog("[CMD-M] debug information: " + mDebugInfor.toString());
					sendGCommand();
				}
			}
			break;
		case 'G':
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 0; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[CMD-G] receive " + " data: " + stringBuilder.toString());
			
			fetchdata();
			break;
		case 'S':
			boolean result = (byteChar[1] == 0) ? false : true;
			deviceUpdateRepeatCount = RESET_DEBUG_MEMORY_REPEAT_COUNT;
			if(result){
				LogcatStorageHelper.addLog("[CMD-S] start download success");
				if(deviceState.getFirmware() == 1){
					startSensorTurnOff();
				}else{
					startIdentify();
//					startResetDebugMemory();
				}
			}else{
				LogcatStorageHelper.addLog("[CMD-S] start download fail");
				mBleListener.updateProgress(BLEConsts.ERROR_PREPARE_FAILED, null);
			}
			break;
		case 'H':
			if(byteChar[1] == 'W'){
				LogcatStorageHelper.addLog("[CMD-HW] start HW-command success");
//				startIdentify();
				startResetDebugMemory();
			}else{
				LogcatStorageHelper.addLog("[CMD-HW] start HW-command fail");
				mBleListener.updateProgress(BLEConsts.ERROR_PREPARE_FAILED, null);
			}
			break;
		default:		
			
			break;
		}
		
		currentCommand = byteChar[0];
	}
	
	private void initDataCache(){
		mDataBuffers.clear();
		mDebugInfor = new StringBuffer();
		mTempDataBuffers.clear();
		DataConfirmFlag = 0; 
	}
	
	public void saveConfirmedData(){
		saveConfirmedData(mCurDog);
	}
	
	public List<StringBuffer> getDataCache(){
		return mDataBuffers;
	}
	
	private boolean checkDataConfirmFlag(int dataConfirm, int blockSize){
		int confirmbit = 0;
		for (int i = 0; i < blockSize; i++) {
			confirmbit = confirmbit | (1 << i);
		}
		return dataConfirm == confirmbit;
	}
	

	private boolean confirm(int index) {
		boolean result = (DataConfirmFlag & (1 << index)) == 0;
		
		if(result){
			DataConfirmFlag = DataConfirmFlag | (1 << index);
		}
		return result;
	}

	private int getSeconds() {
		long quot = 0;
		int seconds = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date1 = ft.parse(TIMELINE);
			Date date2 = ft.parse(ft.format(new Date()));
			quot = date2.getTime() - date1.getTime();
			seconds = (int) (quot / 1000);
			
			getOffsetByTime(quot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return seconds;
	}

	@SuppressLint("SimpleDateFormat") @SuppressWarnings("deprecation")
	private int getOffsetByTime(long timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		try {
			timestamp += format.parse(BaseBluetoothLeAction.TIMELINE).getTime();
			Date date = new Date(timestamp);
			gc.setTime(date);
			int offset = (date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds())/10;
			return offset;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void initdevice(String id, String secretKey, String secret, ISamsungBLEListener listener) {
		mComdLength = 0;
		actionModel = BLUETOOTHLE_ACTION_INIT_DEVICE;
		mBleListener = listener;
		LogcatStorageHelper.addLog("start action init device");
		this.secret = secret;
		
		byte[] command = new byte[13];
		command[0] = new Integer((DEVICE_INIT) & 0xff).byteValue();

		String hexString = Integer.toHexString(Integer.valueOf(id));
		int idLength = hexString.length();
		for(int i = 0; i < Math.ceil(((float)idLength)/2); i++){
			int startPos = idLength-2*(i+1);
			if(startPos < 0){
				startPos = 0;
			}
			command[4-i] = ((Integer)Integer.parseInt(hexString.substring(startPos, idLength-2*i), 16)).byteValue();
		}

		command[5] = ((Integer)Integer.parseInt(secretKey.substring(0, 2), 16)).byteValue();
		command[6] = ((Integer)Integer.parseInt(secretKey.substring(2, 4), 16)).byteValue();
		command[7] = ((Integer)Integer.parseInt(secretKey.substring(4, 6), 16)).byteValue();
		command[8] = ((Integer)Integer.parseInt(secretKey.substring(6, 8), 16)).byteValue();

		command[9] = ((Integer)Integer.parseInt(secret.substring(0, 2), 16)).byteValue();
		command[10] = ((Integer)Integer.parseInt(secret.substring(2, 4), 16)).byteValue();
		command[11] = ((Integer)Integer.parseInt(secret.substring(4, 6), 16)).byteValue();
		command[12] = ((Integer)Integer.parseInt(secret.substring(6, 8), 16)).byteValue();

		sendCharacterToDevice(command);
	}
	
	public void startUpdate(String secret, ISamsungBLEListener listener) {
		mBleListener = listener;
		actionModel = BLUETOOTHLE_ACTION_UPDATE_DEVICE;
		LogcatStorageHelper.addLog("start action update device");
		
		verify(secret);
	}
	
	public void startSync(Pet dog, ISamsungBLEListener listener) {
		mBleListener = listener;
		actionModel = BLUETOOTHLE_ACTION_SYNC_DATA;
		initDataCache();
		mCurDog = dog;
		LogcatStorageHelper.addLog("start action sync device mCurDog: " + dog.getId());
		
		verify(mCurDog.getDevice().getSecret());
	}
	
	/**
	 *start check device
	 */
	public void startCheck(Pet dog, ISamsungBLEListener listener){
		mBleListener = listener;
		actionModel = BLUETOOTHLE_ACTION_CHECK_DEVICE;
		initDataCache();
		LogcatStorageHelper.addLog("start action check device");
		mCurDog = dog;
		
		verify(mCurDog.getDevice().getSecret());
	}
	
	private void verify(String secret){
		byte[] command = new byte[13];
		command[0] = new Integer((DEVICE_VERIFY) & 0xff).byteValue();
		
		command[1] = ((Integer)Integer.parseInt(secret.substring(0, 2), 16)).byteValue();
		command[2] = ((Integer)Integer.parseInt(secret.substring(2, 4), 16)).byteValue();
		command[3] = ((Integer)Integer.parseInt(secret.substring(4, 6), 16)).byteValue();
		command[4] = ((Integer)Integer.parseInt(secret.substring(6, 8), 16)).byteValue();

		mBleListener.updateProgress(BLEConsts.PROGRESS_VERIFY, null);
		sendCharacterToDevice(command);
	}
	
	public void changeDevice(String secret, ISamsungBLEListener listener) {
		mBleListener = listener;
		actionModel = BLUETOOTHLE_ACTION_CHANGE_DEVICE;
		LogcatStorageHelper.addLog("start action change device");
		
		byte[] command = new byte[13];
		command[0] = new Integer((DEVICE_VERIFY) & 0xff).byteValue();
		// secret.getBytes(0, 4, command, 1);
		command[1] = ((Integer)Integer.parseInt(secret.substring(0, 2), 16)).byteValue();
		command[2] = ((Integer)Integer.parseInt(secret.substring(2, 4), 16)).byteValue();
		command[3] = ((Integer)Integer.parseInt(secret.substring(4, 6), 16)).byteValue();
		command[4] = ((Integer)Integer.parseInt(secret.substring(6, 8), 16)).byteValue();
		
		sendCharacterToDevice(command);
	}

	private void synctime() {
		byte[] command = new byte[13];
		int sec = getSeconds();
		command[0] = new Integer((DEVICE_TIME_SYNC) & 0xff).byteValue();
		command[2] = (byte) ((sec >> 24) & 0xFF);
		command[3] = (byte) ((sec >> 16) & 0xFF);
		command[4] = (byte) ((sec >> 8) & 0xFF);
		command[5] = (byte) ((sec >> 0) & 0xFF);

		mBleListener.updateProgress(BLEConsts.PROGRESS_SYNC_TIME, null);
		sendCharacterToDevice(command);
	}

	
	private void readVoltage() {
		byte[] command = new byte[13];
		command[0] = new Integer((VOLTAGE_KEY) & 0xff).byteValue();

		sendCharacterToDevice(command);
	}
	
	private void fetchdata() {
		byte[] command = new byte[13];
		command[0] = new Integer((DEVICE_DATA_READ) & 0xff).byteValue();
		sendCharacterToDevice(command);
	}
	
	private void startResetDebugMemory(){
		byte[] command = new byte[13];
		int address = (deviceState.getExtra().getImageType().equals("B") ? 0x8c00 : 0x2000);
		String confirmbitString = Integer.toHexString(address);
		
		command[0] = new Integer((DEVICE_RESET_DEBUG) & 0xff).byteValue();
		command[1] = ((Integer)Integer.parseInt(confirmbitString.substring(0, 2), 16)).byteValue();
		command[2] = ((Integer)Integer.parseInt(confirmbitString.substring(2, 4), 16)).byteValue();
		
		sendCharacterToDevice(command);
		mComdLength = 0;
		LogcatStorageHelper.addLog("[CMD-E] reset debug memory, repeat count: " + (RESET_DEBUG_MEMORY_REPEAT_COUNT - deviceUpdateRepeatCount));
	}
	
	private void getDebugInfor() {
		byte[] command = new byte[13];
		int address = (deviceState.getExtra().getImageType().equals("B") ? 0x8c00 : 0x2000) + mComdLength * 2;
		String confirmbitString = Integer.toHexString(address);
		
		command[0] = new Integer((DEVICE_DEBUG_INFOR) & 0xff).byteValue();
		command[1] = ((Integer)Integer.parseInt(confirmbitString.substring(0, 2), 16)).byteValue();
		command[2] = ((Integer)Integer.parseInt(confirmbitString.substring(2, 4), 16)).byteValue();
		
		sendCharacterToDevice(command);
		mComdLength++;
	}
	
	private void sendGCommand() {
		byte[] command = new byte[13];
		command[0] = new Integer(('G') & 0xff).byteValue();
		LogcatStorageHelper.addLog("send g command");
		
		sendCharacterToDevice(command);
	}

	private boolean regetMode = false;
	private void dataconfirm(int blocksize) {
		
		int confirmbit = DataConfirmFlag;
		byte[] command = new byte[13];
		if (blocksize < MAX_BLOCK_SIZE) {
			for (int i = MAX_BLOCK_SIZE - 1; i >= blocksize; i--) {
				confirmbit = confirmbit | (1 << i);
			}
		}
		
		if(confirmbit == -1){
			saveConfimData();
			DataConfirmFlag = 0; 
			regetMode = false;
		}else{
			regetMode = true;
			LogcatStorageHelper.addLog("start reget mode, some data dismiss");
		}
		
		String confirmbitString = Integer.toHexString(confirmbit);
		command[0] = new Integer((DEVICE_DATA_CONFIRM) & 0xff).byteValue();
		
		command[1] = ((Integer)Integer.parseInt(confirmbitString.substring(0, 2), 16)).byteValue();
		command[2] = ((Integer)Integer.parseInt(confirmbitString.substring(2, 4), 16)).byteValue();
		command[3] = ((Integer)Integer.parseInt(confirmbitString.substring(4, 6), 16)).byteValue();
		command[4] = ((Integer)Integer.parseInt(confirmbitString.substring(6, 8), 16)).byteValue();

		sendCharacterToDevice(command);

		if(mBleListener != null){
			if(receiveDataLength > totalDataLength){
				receiveDataLength = totalDataLength;
			}
			mBleListener.updateProgress(receiveDataLength * 100 / totalDataLength, null);
		}else{
			LogcatStorageHelper.addLog("dataconfirm mSyncDeviceDataListener = null ERROR");
		}
	}

	private void calculateEnergy(byte[] byteRaw, int index, StringBuffer tempBuffer) {
		int type, energy = 0, len = 0, m = index;
		while (m < MAX_BLE_TRANSMIT_LEN) {
			energy = 0;
			type = byteRaw[m] >> 4;
			len = byteRaw[m] & 0x0f;
			if (type == 0){ // 0x03 for data
				for (int i = 0; i < len; i++) {
				    energy += (((byteRaw[m+1+i])&0xFF)<<((2-i)*8)); //offset the timeline
	            }
				tempBuffer.append(DATA_SPIT).append(energy);
			}
			if (type == 1) {// 0x13 for compact data
				for (int i = 0; i < len; i++) {
				    energy += ((byteRaw[m+1+i]&0xFF)<<(8*(2-i))); //offset the timeline
				}
				for (int i = 0; i < energy; i++) {
					tempBuffer.append(DATA_SPIT).append(0);
				}
			}
			m = m + len + 1;
		}
	}
	
//	private void saveTempData(int value){
//		mTempDataBuffer.append(DATA_SPIT).append(value);
//	}
	
	private void saveConfimData(){
		if(mTempDataBuffers.size() > 0){
			StringBuffer tempBuffer = mTempDataBuffers.get(0);
			if(tempBuffer == null){
				PetkitLog.d("saveConfimData tempBuffer == null");
			}
			for(int i = 1; i < mTempDataBuffers.size(); i++){
				tempBuffer.append(mTempDataBuffers.get(i));
			}
			
			if(mDataBuffers.size() == 0){
				if(tempBuffer.charAt(0) == BLEConsts.DATA_SPIT){
					tempBuffer.insert(0, 0);
				}
				mDataBuffers.add(tempBuffer);
			}else{
				if(tempBuffer.charAt(0) == BLEConsts.DATA_SPIT){
					mDataBuffers.get(mDataBuffers.size() - 1).append(tempBuffer);
				}else{
					saveConfirmedData(mCurDog);
					mDataBuffers.add(tempBuffer);
				}
			}
			mTempDataBuffers.clear();
		}
	}
	

	protected void saveConfirmedData(Pet dog){
		if(mDataBuffers != null && mDataBuffers.size() == 0){
			return;
		}
		
		DeviceActivityDataUtils.saveChangedActivityData(mDataBuffers.get(0), dog, 10);
		mDataBuffers.remove(0);
		
		mBleListener.updateProgress(BLEConsts.PROGRESS_DATA_SAVED, null);
//		mDataBuffers.clear();
	}

	private void parseMovingData(byte[] byteRaw) {
		StringBuffer tempBuffer = new StringBuffer();
		
		if (byteRaw[3] == 0x00){ // time data
			// caculate the data time
			int date_time_offset = 0;
			
			for (int i = 0; i < 4; i++) {
				date_time_offset += ((byteRaw[4+i] & 0x000000FF) << (8*(3 - i))); 
			}

			for (int i = 0; i < 4; i++) {
				date_time_offset += ((byteRaw[8+i] & 0x000000FF) << (8*(3 - i))); 
			}
			
			tempBuffer.append(date_time_offset);
			calculateEnergy(byteRaw, 12, tempBuffer);
		}else{ //byteRaw.charAt(3) == 0xFF
			calculateEnergy(byteRaw, 4, tempBuffer);
		}
		PetkitLog.d("mTempDataBuffers put " + "index: " + (byteRaw[1] & 0xFF) + "data: " + tempBuffer.toString());
		mTempDataBuffers.put((byteRaw[1] & 0xFF), tempBuffer);
		receiveDataLength += 16;
	}
	
//	private void calculateDataCacheTime(String time){
//		if(mTempDataBuffer.length() != 0){
//			PetkitLog.e(TAG, "calculateDataCache mTempDataBuffer.length() != 0 ");
//			mTempDataBuffer.insert(0, time);
//		}else{
//			mTempDataBuffer.append(time);
//		}
//		PetkitLog.d(TAG, "calculateDataCache time: " + time);
//	}
	
	protected void startProgramming() {
		LogcatStorageHelper.addLog("start download");
		
		byte[] buf = new byte[13];
		buf[0] = new Integer((DEVICE_DOWNLOAD) & 0xff).byteValue();

		sendCharacterToDevice(buf);
	}
	
	protected void startSensorTurnOff() {
		LogcatStorageHelper.addLog("startSensorTurnOff");
		
		byte[] buf = new byte[13];
		buf[0] = 'H';//new Integer(('H') & 0xff).byteValue();
		buf[1] = 'W';//new Integer(('W') & 0xff).byteValue();
		buf[2] = 0x1b;//Integer.valueOf("1B", 16).byteValue();
		buf[3] = 0x00;//Integer.valueOf("00", 16).byteValue();
		buf[4] = 0x1e;//Integer.valueOf("1E", 16).byteValue();
		buf[5] = 0x00;//Integer.valueOf("00", 16).byteValue();

		sendCharacterToDevice(buf);
	}
	
	public void resetSensor(){
		LogcatStorageHelper.addLog("start reset sensor");
		
		byte[] buf = new byte[13];
		buf[0] = 'H';//new Integer(('H') & 0xff).byteValue();
		buf[1] = 'S';//new Integer(('W') & 0xff).byteValue();

		sendCharacterToDevice(buf);
	}
	
	
	/**
	 * --------------------------------------- OAD ---------------------------
	 */

	protected static final int PKT_INTERVAL = 60; // Milliseconds
	protected static final int OAD_BLOCK_SIZE = 16;
	protected static final int HAL_FLASH_WORD_SIZE = 4;
	protected static final int GATT_WRITE_TIMEOUT = 100; // Milliseconds
	protected static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
	protected static final int P2_OAD_BUFFER_SIZE = 20;

	protected ImgHdr mFileImgHdr = new ImgHdr();
	protected ProgInfo mProgInfo = new ProgInfo();
	protected Timer mTimer = null;
	// Programming
	protected byte[] mFileBuffer;
	protected final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];
	protected final byte[] mP2OadBuffer = new byte[P2_OAD_BUFFER_SIZE];
	protected TimerTask mTimerTask = null;
	protected boolean mProgramming;

	public class ProgTimerTask extends TimerTask {
		
		@Override
		public void run() {
			if (mProgramming) {
				onBlockTimer(-1);
			} else {
				if (mTimerTask != null) {
					mTimerTask.cancel();
					mTimerTask = null;
				}
				if(mTimer != null){
					mTimer.cancel();
					mTimer = null;
				}
			}
		}
	}

	protected boolean initBufferData(byte[] oadBuffer) {
		// Show image info
		mFileImgHdr.ver = Conversion
				.buildUint16(mFileBuffer[5], mFileBuffer[4]);
		mFileImgHdr.len = Conversion
				.buildUint16(mFileBuffer[7], mFileBuffer[6]);
		mFileImgHdr.imgType = (mFileImgHdr.ver & 1);
		System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);

		// Verify image types
		return mFileImgHdr.imgType != (deviceState.getExtra().getImageType().equals("A") ? 0 : 1);
	}

	public void stop() {
		saveConfirmedData();
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		mFileBuffer = null;
	}
	
	protected class ImgHdr {
		public short ver;
		public short len;
		public int imgType;
		public byte[] uid = new byte[4];
	}

	protected class ProgInfo {
		public int iBytes = 0; // Number of bytes programmed
		public short iBlocks = 0; // Number of blocks programmed
		public short nBlocks = 0; // Total number of blocks

		public void reset() {
			iBytes = 0;
			iBlocks = 0;
			nBlocks = (short) (mFileImgHdr.len / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
			PetkitLog.d("nBlocks: " + nBlocks);
		}
	}
	
	
	public Device getDeviceInfor(){
		return deviceState;
	}

	/**
	 * get battery
	 */
	protected abstract void getBattery();
	
	/**
	 * write message to character
	 * @param message
	 */
	protected abstract void sendCharacterToDevice(byte[] message);
	protected abstract void onBlockTimer(int index);
	
	
	protected abstract void startOad(String filePath);
	protected abstract void startIdentify();
	protected abstract void startUpdate();

}