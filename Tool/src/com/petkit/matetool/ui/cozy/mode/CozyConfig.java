package com.petkit.matetool.ui.cozy.mode;

import java.util.List;

/**
 * Created by Jone on 18/3/13.
 */

public class CozyConfig {
    private int coolTemp;
    private int heatTemp;
    private int restTime;
    private int cTimeOut;
    private int HTimeOut;
    private int count;
    private List<Integer> fan_speed_current_range;

    public CozyConfig(int coolTemp, int heatTemp, int restTime, int cTimeOut, int HTimeOut, int count) {
        this.coolTemp = coolTemp;
        this.heatTemp = heatTemp;
        this.restTime = restTime;
        this.cTimeOut = cTimeOut;
        this.HTimeOut = HTimeOut;
        this.count = count;
    }

    public int getCoolTemp() {
        return coolTemp;
    }

    public void setCoolTemp(int coolTemp) {
        this.coolTemp = coolTemp;
    }

    public int getHeatTemp() {
        return heatTemp;
    }

    public void setHeatTemp(int heatTemp) {
        this.heatTemp = heatTemp;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public int getcTimeOut() {
        return cTimeOut;
    }

    public void setcTimeOut(int cTimeOut) {
        this.cTimeOut = cTimeOut;
    }

    public int getHTimeOut() {
        return HTimeOut;
    }

    public void setHTimeOut(int HTimeOut) {
        this.HTimeOut = HTimeOut;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Integer> getFan_speed_current_range() {
        return fan_speed_current_range;
    }

    public void setFan_speed_current_range(List<Integer> fan_speed_current_range) {
        this.fan_speed_current_range = fan_speed_current_range;
    }
}
