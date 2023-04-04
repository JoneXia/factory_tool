package com.petkit.android.ble.samsung;

import com.petkit.android.ble.DeviceInfo;


/**
 * base ble listener, listen ble state change.
 * 
 * @author Jone
 *
 */
public interface ISamsungBLEListener {

	/**
	 * scan a device
	 * @param device
	 */
	void onScanResultChange(DeviceInfo device);
	
	void updateProgress(int progress, String data);
	
}
