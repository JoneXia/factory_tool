package com.petkit.matetool.model;

import java.io.Serializable;

/**
 * Created by Jone on 2015/10/30.
 */
public class WifiParams implements Serializable {
    public String local_rtp_ip;
    public int local_port;
    public String ssid;
    public byte rsq;
    public byte state;
    public byte index;
    public String mac;
    public String sn;
    public int status;
    public String version;

    public String getLocal_rtp_ip() {
        return local_rtp_ip;
    }

    public void setLocal_rtp_ip(String local_rtp_ip) {
        this.local_rtp_ip = local_rtp_ip;
    }

    public int getLocal_port() {
        return local_port;
    }

    public void setLocal_port(int local_port) {
        this.local_port = local_port;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public byte getRsq() {
        return rsq;
    }

    public void setRsq(byte rsq) {
        this.rsq = rsq;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "WifiParams{" +
                "local_rtp_ip='" + local_rtp_ip + '\'' +
                ", local_port=" + local_port +
                ", ssid='" + ssid + '\'' +
                ", rsq=" + rsq +
                ", state=" + state +
                ", index=" + index +
                ", mac='" + mac + '\'' +
                ", sn='" + sn + '\'' +
                ", status=" + status +
                ", version='" + version + '\'' +
                '}';
    }
};