package com.petkit.matetool.model;

import java.util.ArrayList;

/**
 *
 * Created by Jone on 17/5/16.
 */
public class DevicesError {
    private ArrayList<Device> mac;
    private ArrayList<Device> sn;

    public void setMac(ArrayList<Device> mac) {
        this.mac = mac;
    }

    public ArrayList<Device> getMac() {
        if(mac == null) {
            mac = new ArrayList<>();
        }
        return mac;
    }

    public void setSn(ArrayList<Device> sn) {
        this.sn = sn;
    }

    public ArrayList<Device> getSn() {
        if(sn == null) {
            sn = new ArrayList<>();
        }
        return sn;
    }
}
