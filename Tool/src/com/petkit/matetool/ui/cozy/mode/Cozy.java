package com.petkit.matetool.ui.cozy.mode;

import java.io.Serializable;

/**
 * Created by Jone on 17/5/3.
 */

public class Cozy implements Serializable {

    private String mac = "";
    private String sn = "";
    private long creation = 0;
    private int inspectStatus = 0;

    public Cozy(String mac, String sn) {
        this.mac = mac;
        this.sn = sn;
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
        return String.format("{\"sn\":\"%s\",\"mac\":\"%s\",\"creation\":%d}", sn, mac, creation);
    }

    @Override
    public String toString() {
        return "Feeder{" +
                "mac='" + mac + '\'' +
                ", sn='" + sn + '\'' +
                ", creation='" + creation + '\'' +
                '}';
    }
}
