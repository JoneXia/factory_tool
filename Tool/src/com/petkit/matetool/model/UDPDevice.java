package com.petkit.matetool.model;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

public class UDPDevice implements Serializable {

    private UDPScanRecord scanRecord;
    private String ip;
    private int port;

    public UDPDevice(String sentence, String ip, int port) {
        try {
            this.scanRecord = new Gson().fromJson(sentence, UDPScanRecord.class);
        } catch (Exception e) {
            this.scanRecord = new UDPScanRecord();
        }
        this.ip = ip;
        this.port = port;
    }

    public UDPScanRecord getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(UDPScanRecord scanRecord) {
        this.scanRecord = scanRecord;
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
                "deviceType='" + scanRecord + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UDPDevice)) return false;
        UDPDevice device = (UDPDevice) o;
        return getPort() == device.getPort() && getScanRecord().equals(device.getScanRecord()) && getIp().equals(device.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScanRecord(), getIp(), getPort());
    }
}
