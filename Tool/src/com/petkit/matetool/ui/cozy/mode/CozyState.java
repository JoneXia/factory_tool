package com.petkit.matetool.ui.cozy.mode;

/**
 *
 * Created by Jone on 17/12/18.
 */

public class CozyState {

    //工作模式，工作面温度，工作面电压mV，散热面温度，散热面电压mV，空气温度，空气湿度，电源电压，工作电流，风扇转速
    private int wk_mode, wk_temp, wk_tempVM, ts_temp, ts_tempVM, air_temp, air_humi, pom_mV, curmA, fan;

    private String getZLPmode(int mode) {
        switch (mode) {
            case 0:
                return "关闭";
            case 1:
                return "制冷";
            case 2:
                return "制热";
            default:
                return "异常";
        }
    }

    public int getWk_mode() {
        return wk_mode;
    }

    public void setWk_mode(int wk_mode) {
        this.wk_mode = wk_mode;
    }

    public int getWk_temp() {
        return wk_temp;
    }

    public void setWk_temp(int wk_temp) {
        this.wk_temp = wk_temp;
    }

    public int getWk_tempVM() {
        return wk_tempVM;
    }

    public void setWk_tempVM(int wk_tempVM) {
        this.wk_tempVM = wk_tempVM;
    }

    public int getTs_temp() {
        return ts_temp;
    }

    public void setTs_temp(int ts_temp) {
        this.ts_temp = ts_temp;
    }

    public int getTs_tempVM() {
        return ts_tempVM;
    }

    public void setTs_tempVM(int ts_tempVM) {
        this.ts_tempVM = ts_tempVM;
    }

    public int getAir_temp() {
        return air_temp;
    }

    public void setAir_temp(int air_temp) {
        this.air_temp = air_temp;
    }

    public int getAir_humi() {
        return air_humi;
    }

    public void setAir_humi(int air_humi) {
        this.air_humi = air_humi;
    }

    public int getPom_mV() {
        return pom_mV;
    }

    public void setPom_mV(int pom_mV) {
        this.pom_mV = pom_mV;
    }

    public int getCurmA() {
        return curmA;
    }

    public void setCurmA(int curmA) {
        this.curmA = curmA;
    }

    public void setFan(int fan) {
        this.fan = fan;
    }

    public int getFan() {
        return fan;
    }
}
