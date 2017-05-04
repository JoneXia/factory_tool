package com.petkit.matetool.model;

import java.io.Serializable;

/**
 * Created by Jone on 17/5/3.
 */

public class Feeder implements Serializable {

    private String mac;
    private String sn;

    public Feeder(String mac, String sn) {
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
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
