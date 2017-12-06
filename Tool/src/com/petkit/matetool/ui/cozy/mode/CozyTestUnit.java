package com.petkit.matetool.ui.cozy.mode;

import com.petkit.matetool.ui.cozy.utils.CozyUtils;

import java.io.Serializable;

/**
 * 猫窝测试单元
 *
 * Created by Jone on 17/11/30.
 */
public class CozyTestUnit implements Serializable {
    private CozyUtils.CozyTestModes type;
    private int module;
    private int state;
    private int result;
    private String name;

    public CozyTestUnit(CozyUtils.CozyTestModes type, String name, int module, int state) {
        this.type = type;
        this.name = name;
        this.module = module;
        this.state = state;
    }

    public void setType(CozyUtils.CozyTestModes type) {
        this.type = type;
    }

    public CozyUtils.CozyTestModes getType() {
        return type;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getModule() {
        return module;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
