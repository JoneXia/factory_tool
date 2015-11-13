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

import java.io.Serializable;

import com.petkit.android.utils.PetkitLog;

import android.bluetooth.BluetoothDevice;


public class DeviceInfo implements Serializable {

	private static final long serialVersionUID = -4523460236390521750L;
	
	
	private String address;
	private String name;
	private int mRssi;
	private long deviceId = 0;
	private String mac;
	private boolean checked = false;
	private String owner;

	public DeviceInfo() {
		super();
	}


	public DeviceInfo(BluetoothDevice device, int rssi, byte[] scanRecord) {
		address = device.getAddress();
		name = device.getName();
		mRssi = rssi;
		
		if(scanRecord == null || scanRecord.length < 19){
			return;
		}
		
		int i = 0; 
		while(i < scanRecord.length - 1){
			int length = scanRecord[i];
			int type = scanRecord[i+1];
			if((type == 7 || type == 6) && length >= 11){
				StringBuilder stringBuilder = new StringBuilder(12);
				for (int j = i + 7; j > i + 1; j--)
					stringBuilder.append(String.format("%02X", scanRecord[j]));
				mac = stringBuilder.toString();
				
				if(name != null && (name.equals(BLEConsts.PET_HOME) || name.equals(BLEConsts.PET_MATE))){
					for(int j = 0; j < 8; j++){
						deviceId += ((scanRecord[j + i + 8] & 0xFF) << 8 * j);
					}
				}else {
					for(int j = 0; j < 4; j++){
						deviceId += ((scanRecord[j + i + 8] & 0xFF) << 8 * (3 - j));
					}
				}
				break;
			}else{
				i += (length + 1);
			}
		}
		
		if(name != null){
			if(name.equalsIgnoreCase(BLEConsts.PET_FIT)){
				name = BLEConsts.PET_FIT_DISPLAY_NAME;
			} else if(name.equalsIgnoreCase(BLEConsts.PET_FIT2)){
				name = BLEConsts.PET_FIT2_DISPLAY_NAME;
			} else if(name.equals(BLEConsts.PET_MATE)){
				name = BLEConsts.PET_MATE;
			}
		}
		
//		if(address != null && address.equalsIgnoreCase("D9:B6:C7:91:D1:49")){
//			if(deviceId == 0){
//				checked = true;
//			}
//		}
//		
		if(mac == null){
			if(deviceId == 0){
				checked = true;
			}
		}
		if(deviceId == 0){
			checked = true;
		}
		
	}

	
	public String parse(final byte[] data) {
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
		PetkitLog.d("mac : " + mac);
		if(mac.contains(":")){
			mac = mac.replaceAll(":", "");
			PetkitLog.d("mac modify: " + mac);
		}
		this.mac = mac;
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
	
	

}
