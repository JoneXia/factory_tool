package com.petkit.matetool.ui.common.utils;

public class DeviceConfigInfo {

    private boolean isBleDevice;
    private String deviceKey;
    private String deviceTesterKey;
    private String deviceName;
    private String deviceSNFlag;
    private Class deviceMainActiviy;

    public DeviceConfigInfo(boolean isBleDevice, String deviceKey, String deviceTesterKey, String deviceName, String deviceSNFlag, Class deviceMainActiviy) {
        this.isBleDevice = isBleDevice;
        this.deviceKey = deviceKey;
        this.deviceTesterKey = deviceTesterKey;
        this.deviceName = deviceName;
        this.deviceSNFlag = deviceSNFlag;
        this.deviceMainActiviy = deviceMainActiviy;
    }

    public boolean isBleDevice() {
        return isBleDevice;
    }

    public void setBleDevice(boolean bleDevice) {
        isBleDevice = bleDevice;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getDeviceTesterKey() {
        return deviceTesterKey;
    }

    public void setDeviceTesterKey(String deviceTesterKey) {
        this.deviceTesterKey = deviceTesterKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSNFlag() {
        return deviceSNFlag;
    }

    public void setDeviceSNFlag(String deviceSNFlag) {
        this.deviceSNFlag = deviceSNFlag;
    }

    public Class getDeviceMainActiviy() {
        return deviceMainActiviy;
    }

    public void setDeviceMainActiviy(Class deviceMainActiviy) {
        this.deviceMainActiviy = deviceMainActiviy;
    }
}
