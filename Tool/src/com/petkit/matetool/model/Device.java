package com.petkit.matetool.model;

import java.io.Serializable;

/**
 *
 * Created by Jone on 17/5/3.
 */
public class Device implements Serializable {

    private String id = "";
    private String mac = "";
    private String sn = "";
    private String chipid = "";
    private long creation = 0;
    private int inspectStatus = 0;
    private int hardware;
    private int firmware;

    public Device(String mac, String sn, String chipid) {
        this.mac = mac;
        this.sn = sn;
        this.chipid = chipid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSn() {
        if(sn == null) {
            sn = "";
        }
        return sn;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public boolean checkValid() {
        return mac != null && mac.length() > 0 && sn != null && sn.length() >= 12;
    }

    public void setChipid(String chipid) {
        this.chipid = chipid;
    }

    public String getChipid() {
        return chipid;
    }

    public String generateJson() {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\"}", sn, mac);
    }

    public void setInspectStatus(int inspectStatus) {
        this.inspectStatus = inspectStatus;
    }

    public int getInspectStatus() {
        return inspectStatus;
    }

    public String generateCheckJson() {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"inspectStatus\":%d}", sn, mac, inspectStatus);
    }

    public String generateMainJson(String ageingResult) {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"chipId\":\"%s\",\"creation\":%d,\"ageingResult\":%s}", sn, mac, chipid, creation, ageingResult);
    }

    public String generateMainJson(String ageingResult, int withK3) {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"chipId\":\"%s\",\"withK3\":%d,\"creation\":%d,\"ageingResult\":%s}", sn, mac, chipid, withK3, creation, ageingResult);
    }

    public int getHardware() {
        return hardware;
    }

    public void setHardware(int hardware) {
        this.hardware = hardware;
    }

    public int getFirmware() {
        return firmware;
    }

    public void setFirmware(int firmware) {
        this.firmware = firmware;
    }

    @Override
    public String toString() {
        return "Device{" +
                "mac='" + mac + '\'' +
                ", sn='" + sn + '\'' +
                ", id='" + id + '\'' +
                ", version='" + hardware + "." + firmware + '\'' +
                '}';
    }
}
