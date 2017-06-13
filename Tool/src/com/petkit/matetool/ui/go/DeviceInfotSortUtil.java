package com.petkit.matetool.ui.go;

import com.petkit.android.ble.DeviceInfo;

import java.util.Comparator;

public class DeviceInfotSortUtil implements Comparator<DeviceInfo> {

	public int compare(DeviceInfo o1, DeviceInfo o2) {
		DeviceInfo deviceInfo = o1;
		DeviceInfo deviceInfo1 = o2;

		return deviceInfo1.getRssi() - deviceInfo.getRssi();
	}
}
