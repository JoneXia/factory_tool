package com.petkit.matetool.ui.feeder.mode;

import java.util.ArrayList;

/**
 * Created by Jone on 17/5/16.
 */

public class FeedersError {
    private ArrayList<Feeder> mac;
    private ArrayList<Feeder> sn;

    public void setMac(ArrayList<Feeder> mac) {
        this.mac = mac;
    }

    public ArrayList<Feeder> getMac() {
        if(mac == null) {
            mac = new ArrayList<>();
        }
        return mac;
    }

    public void setSn(ArrayList<Feeder> sn) {
        this.sn = sn;
    }

    public ArrayList<Feeder> getSn() {
        if(sn == null) {
            sn = new ArrayList<>();
        }
        return sn;
    }
}
