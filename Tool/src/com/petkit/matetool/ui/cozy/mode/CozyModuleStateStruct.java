package com.petkit.matetool.ui.cozy.mode;

/**
 * Created by Jone on 17/5/2.
 */

public class CozyModuleStateStruct {

    private int module;
    private int sub0, sub1, sub2;
    private String time;

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getSub0() {
        return sub0;
    }

    public void setSub0(int sub0) {
        this.sub0 = sub0;
    }

    public int getSub1() {
        return sub1;
    }

    public void setSub1(int sub1) {
        this.sub1 = sub1;
    }

    public int getSub2() {
        return sub2;
    }

    public void setSub2(int sub2) {
        this.sub2 = sub2;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}

