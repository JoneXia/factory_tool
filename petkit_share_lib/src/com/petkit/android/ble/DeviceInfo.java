/**************************************************************************************************
  Filename:       BleDeviceInfo.java
  Revised:        $Date: 2013-08-30 12:08:11 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27477 $

  Copyright 2013 Texas Instruments Incorporated. All rights reserved.
 
  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. 
  The License limits your use, and you acknowledge, that the Software may not be 
  modified, copied or distributed unless used solely and exclusively in conjunction 
  with a Texas Instruments Bluetooth device. Other than for the foregoing purpose, 
  you may not use, reproduce, copy, prepare derivative works of, modify, distribute, 
  perform, display or sell this Software and/or its documentation for any purpose.
 
  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED �AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 
  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com

 **************************************************************************************************/
package com.petkit.android.ble;

import android.bluetooth.BluetoothDevice;

import com.petkit.android.utils.PetkitLog;

import java.io.Serializable;


public class DeviceInfo implements Serializable {

	private static final long serialVersionUID = -4523460236390521750L;

	public static final int DEVICE_TYPE_FIT = 1;
	public static final int DEVICE_TYPE_FIT2 = 2;
	public static final int DEVICE_TYPE_MATE = 3;
	public static final int DEVICE_TYPE_GO = 4;
	public static final int DEVICE_TYPE_T3 = 7;
	public static final int DEVICE_TYPE_K2 = 8;
	public static final int DEVICE_TYPE_AQ = 10;
	public static final int DEVICE_TYPE_P3 = 11;
	public static final int DEVICE_TYPE_W5 = 12;
	public static final int DEVICE_TYPE_K3 = 13;
	public static final int DEVICE_TYPE_AQR = 14;
	public static final int DEVICE_TYPE_AQ1S = 15;
	public static final int DEVICE_TYPE_HG = 16;


	private String address;
	private String name;
	private int mRssi;
	private long deviceId = 0;
	private String mac;
	private boolean checked = false;
	private String owner;
	private byte[] scanRecord;

	//设备类型：1： fit；2: fit2; 3： mate； 4： go
	private int deviceType = 0xff;

	private int hardware;
	private int fireware;
	private String buildDate;

	//AQ 整机测试结果
	private int mTestResult;

	private String sn;
	private long creation;

	public DeviceInfo() {
		super();
	}


	public DeviceInfo(BluetoothDevice device, int rssi, byte[] scanRecord) {
		address = device.getAddress();
		name = device.getName();
		this.scanRecord = scanRecord;
		setTypeByName();
		mRssi = rssi;

		if (scanRecord == null || scanRecord.length < 19) {
			return;
		}
		PetkitLog.d("deviceName:" + name + " scanRecord:" + parse(scanRecord));

		int count = 0;
		boolean isPetkit = true;
		byte[] petkit = new byte[]{(byte) 0xBB, 0x3E, (byte) 0xE6, 0x08, 0x05, 0x72, 0x4D, (byte) 0x9F, (byte) 0x89, (byte) 0xE7, (byte) 0xBF, (byte) 0x91, (byte) 0xD8, (byte) 0xBD, 0x70, 0x5B};
		while (count < scanRecord.length - 1) {
			int len = scanRecord[count];
			int type = scanRecord[count + 1];
			// 判断是petkit设备
			if (type == 0x07) {
				if (petkit.length != len - 1) {
					break;
				}
				int petkitCount = 0;
				for (int checkCount = count + 2; checkCount <= count + len; checkCount++, petkitCount++) {
					if (scanRecord[checkCount] != petkit[petkitCount]) {
						isPetkit = false;
						break;
					}
				}
				if (!isPetkit) {
					break;
				}

			}
			// 判断设备类型
			if (type == 0x16 && len == 9) {
				getDeviceNameByScanRecord(scanRecord[count + len]);
				if (DEVICE_TYPE_T3 != deviceType && DEVICE_TYPE_K2 != deviceType && DEVICE_TYPE_AQ != deviceType) {
					break;
				}
			}
			// 解析deviceId
			if (type == 0x16 && len == 7) {
				byte[] deviceIdData = new byte[6];
				int tempCount = 0;
				for (int parseCount = count + 2; parseCount <= count + len; parseCount++) {
					deviceIdData[tempCount] = scanRecord[parseCount];
					tempCount++;
				}
				try {
					String value = parse(deviceIdData);
					deviceId = Long.parseLong(value, 16);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					PetkitLog.d("parse devoceId error");
				}
			}
			count += (len + 1);
		}

		if (DEVICE_TYPE_T3 > deviceType) {
			int i = 0;
			while (i < scanRecord.length - 1) {
				int length = scanRecord[i];
				int type = scanRecord[i + 1];
				if ((type == 7 || type == 6) && length >= 11) {
					StringBuilder stringBuilder = new StringBuilder(12);
					for (int j = i + 7; j > i + 1; j--)
						stringBuilder.append(String.format("%02X", scanRecord[j]));
					mac = stringBuilder.toString();

					if (DEVICE_TYPE_GO == deviceType || DEVICE_TYPE_MATE == deviceType) {
						for (int j = 0; j < 8; j++) {
							deviceId += ((scanRecord[j + i + 8] & 0xFF) << 8 * j);
						}
					} else {
						for (int j = 0; j < 4; j++) {
							deviceId += ((scanRecord[j + i + 8] & 0xFF) << 8 * (3 - j));
						}
					}
					break;
				} else if (type == -1 && length == 26) {  //oppo n3 ble special, no device name
					byte[] standardByte = new byte[]{0x15, (byte) 0xBB, 0x3E, (byte) 0xE6, 0x08, 0x05, 0x72, 0x4D, (byte) 0x9F, (byte) 0x89, (byte) 0xE7, (byte) 0xBF, (byte) 0x91};

					int j = 0;
					for (; j < standardByte.length; j++) {
						if (scanRecord[i + 5 + j] != standardByte[j]) {
							break;
						}
					}
					if (j == standardByte.length) {
						name = getDeviceNameByScanRecord(scanRecord[i + 26] & 0xff);
						if (name == null) {
							break;
						}

						if (DEVICE_TYPE_GO == deviceType || DEVICE_TYPE_MATE == deviceType) {
							byte[] valueArray = new byte[8];
							System.arraycopy(scanRecord, i + 18, valueArray, 0, 8);
							String value = parse(valueArray);
							try {
								deviceId = Long.parseLong(value, 16);
							} catch (NumberFormatException e) {

							}
						} else {
							for (int z = 0; z < 4; z++) {
								deviceId += ((scanRecord[i + 22 + z] & 0xFF) << 8 * (3 - z));
							}
						}

						if (deviceId == 0x10102 || deviceId == 0x1020304) {
							deviceId = 0;
						}
					}
					break;
				} else {
					i += (length + 1);
				}
			}

		}
		
		if(name != null){
			if(name.equalsIgnoreCase(BLEConsts.PET_FIT)){
				name = BLEConsts.PET_FIT_DISPLAY_NAME;
			} else if(name.equalsIgnoreCase(BLEConsts.PET_FIT2)){
				name = BLEConsts.PET_FIT2_DISPLAY_NAME;
			} else if(name.equals(BLEConsts.PET_HOME)){
				name = BLEConsts.PET_MATE;
			}
		}
		
		if(mac == null){
			setMac(device.getAddress());
		}
		if(deviceId == 0){
			checked = true;
		}
		
	}

    protected String getDeviceNameByScanRecord(int value){
        switch (value){
			case 0xC5:
				deviceType = DEVICE_TYPE_FIT;
				return BLEConsts.PET_FIT_DISPLAY_NAME;
			case 0xC3:
				deviceType = DEVICE_TYPE_FIT2;
				return BLEConsts.PET_FIT2_DISPLAY_NAME;
			case 0xC4:
				deviceType = DEVICE_TYPE_MATE;
				return BLEConsts.PET_MATE;
			case 0xC6:
				deviceType = DEVICE_TYPE_GO;
				return BLEConsts.GO_DISPLAY_NAME;
			case 0xC7:
				deviceType = DEVICE_TYPE_T3;
				return BLEConsts.T3_DISPLAY_NAME;
			case 0xC8:
				deviceType = DEVICE_TYPE_K2;
				return BLEConsts.K2_DISPLAY_NAME;
			case 0xCA:
				deviceType = DEVICE_TYPE_AQ;
				return BLEConsts.AQ_DISPLAY_NAME;
			case 0xCC:
				deviceType = DEVICE_TYPE_P3;
				return BLEConsts.P3C_DISPLAY_NAME;
			case 0xCD:
			case 0xCE:
				deviceType = DEVICE_TYPE_W5;
				return BLEConsts.W5C_DISPLAY_NAME;
			case 0xD0:
				deviceType = DEVICE_TYPE_K3;
				return BLEConsts.K3_DISPLAY_NAME;
			case 0xD1:
				deviceType = DEVICE_TYPE_AQR;
				return BLEConsts.AQR_DISPLAY_NAME;
			case 0xD4:
				deviceType = DEVICE_TYPE_AQ1S;
				return BLEConsts.AQ1S_DISPLAY_NAME;
			case 0xDE:
				deviceType = DEVICE_TYPE_HG;
				return BLEConsts.HG_DISPLAY_NAME;
			default:
				return null;
		}
    }

	private void setTypeByName() {
		if(name == null) {
			return;
		}
		switch (name){
			case BLEConsts.PET_FIT_DISPLAY_NAME:
			case BLEConsts.PET_FIT:
			case "Petkit":
				deviceType = DEVICE_TYPE_FIT;
				break;
			case BLEConsts.PET_FIT2_DISPLAY_NAME:
			case BLEConsts.PET_FIT2:
			case "Petkit2":
				deviceType = DEVICE_TYPE_FIT2;
				break;
			case BLEConsts.PET_MATE:
				deviceType = DEVICE_TYPE_MATE;
				break;
			case BLEConsts.GO_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_GO;
				break;
			case BLEConsts.AQ_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_AQ;
				break;
			case BLEConsts.P3C_DISPLAY_NAME:
			case BLEConsts.P3D_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_P3;
				break;
			case BLEConsts.W5C_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_W5;
				break;
			case BLEConsts.K3_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_K3;
				break;
			case BLEConsts.AQR_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_AQR;
				break;
			case BLEConsts.AQ1S_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_AQ1S;
				break;
			case BLEConsts.HG_DISPLAY_NAME:
				deviceType = DEVICE_TYPE_HG;
				break;
		}
	}
	
	public String parse(final byte[] data) {
		if (data == null)
			return "";
		final int length = data.length;
		if (length == 0)
			return "";

		final char[] out = new char[length * 2];
		for (int j = 0; j < length; j++) {
			int v = data[j] & 0xFF;
			out[j * 2] = BLEConsts.HEX_ARRAY[v >>> 4];
			out[j * 2 + 1] = BLEConsts.HEX_ARRAY[v & 0x0F];
//			if (j != length - 1)
//				out[j * 3 + 2] = '-';
		}
		return new String(out);
	}

	public int getRssi() {
		return mRssi;
	}

	public void updateRssi(int rssiValue) {
		mRssi = rssiValue;
	}

	public long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		if(mac.contains(":")){
			mac = mac.replaceAll(":", "");
		}
		this.mac = mac;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public void setCreation(long creation) {
		this.creation = creation;
	}

	public long getCreation() {
		return creation;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return "BleDeviceInfo mac: " + mac
				+ " rssi: " + mRssi
				+ " deviceId: " + deviceId
				+ " device Address: " + address
				+ " device type: " + deviceType
				+ " device Name: " + name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setType(int type) {
		this.deviceType = type;
	}

	public int getType() {
		return deviceType;
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public int getHardware() {
		return hardware;
	}

	public void setHardware(int hardware) {
		this.hardware = hardware;
	}

	public int getFireware() {
		return fireware;
	}

	public void setFireware(int fireware) {
		this.fireware = fireware;
	}

	public void setTestResult(int testResult) {
		mTestResult = testResult;
	}

	public int getTestResult() {
		return mTestResult;
	}
}
