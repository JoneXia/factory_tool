package com.petkit.matetool.ui.catlitter.mode;

/**
 * Created by Jone on 17/6/1.
 */

public class CatLitterInfo {


    /**
     * work : 256
     * hot : 900
     * room : 256
     * humi : 423
     * state : 250
     * open : 0
     */

    private int work;
    private int hot;
    private int room;
    private int humi;
    private int state;
    private int open;

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getHumi() {
        return humi;
    }

    public void setHumi(int humi) {
        this.humi = humi;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    @Override
    public String toString() {
        return "工作面温度=" + convertTemperature(work) +
                ", 散热面温度=" + convertTemperature(hot) +
                ", 室温=" + convertTemperature(room) +
                ", 湿度=" + (humi / 10f) + "%" +
                ", 设定温度=" + convertTemperature(state) +
                ", 制冷状态=" + (open == 1 ? "打开" : "关闭");
    }

    private String convertTemperature (int value) {
        return value / 10f + "℃";
    }
}
