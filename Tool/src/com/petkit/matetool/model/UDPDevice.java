package com.petkit.matetool.model;

import java.io.Serializable;
import java.util.Objects;

public class UDPDevice implements Serializable {

    private String deviceType;
    private String ip;
    private int port;

    public UDPDevice(String deviceType, String ip, int port) {
        this.deviceType = deviceType;
        this.ip = ip;
        this.port = port;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "UDPDevice{" +
                "deviceType='" + deviceType + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UDPDevice)) return false;
        UDPDevice device = (UDPDevice) o;
        return getPort() == device.getPort() && getDeviceType().equals(device.getDeviceType()) && getIp().equals(device.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceType(), getIp(), getPort());
    }
}
