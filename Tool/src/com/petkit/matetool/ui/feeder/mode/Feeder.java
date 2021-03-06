package com.petkit.matetool.ui.feeder.mode;

import java.io.Serializable;

/**
 * Created by Jone on 17/5/3.
 */

public class Feeder implements Serializable {

    private String mac = "";
    private String sn = "";
    private String chipid = "";
    private long creation = 0;
    private int inspectStatus = 0;

    public Feeder(String mac, String sn, String chipid) {
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
        return mac != null && sn != null;
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

    public String generateMainJson() {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"chipId\":\"%s\",\"creation\":%d}", sn, mac, chipid, creation);
    }

    public String generateMainJson(String ageingResult) {
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"chipId\":\"%s\",\"creation\":%d,\"ageingResult\":%s}", sn, mac, chipid, creation, ageingResult);
    }

    @Override
    public boolean equals(Object feeder) {
        return feeder != null && feeder instanceof Feeder
                && sn != null && sn.equals(((Feeder) feeder).getSn())
                && mac != null && mac.equals(((Feeder) feeder).getMac());
    }

    @Override
    public String toString() {
        return "Feeder{" +
                "mac='" + mac + '\'' +
                ", sn='" + sn + '\'' +
                ", chipid='" + chipid + '\'' +
                ", creation='" + creation + '\'' +
                '}';
    }
}
