package com.petkit.matetool.ui.cozy.mode;

import java.util.ArrayList;

/**
 *
 * Created by Jone on 17/5/16.
 */
public class CozysError {
    private ArrayList<Cozy> mac;
    private ArrayList<Cozy> sn;

    public void setMac(ArrayList<Cozy> mac) {
        this.mac = mac;
    }

    public ArrayList<Cozy> getMac() {
        if(mac == null) {
            mac = new ArrayList<>();
        }
        return mac;
    }

    public void setSn(ArrayList<Cozy> sn) {
        this.sn = sn;
    }

    public ArrayList<Cozy> getSn() {
        if(sn == null) {
            sn = new ArrayList<>();
        }
        return sn;
    }
}
