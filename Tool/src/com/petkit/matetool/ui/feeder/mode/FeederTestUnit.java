package com.petkit.matetool.ui.feeder.mode;

import com.petkit.matetool.ui.feeder.utils.FeederUtils;

import java.io.Serializable;

/**
 * 喂食器测试单元
 *
 * Created by Jone on 17/5/2.
 */
public class FeederTestUnit implements Serializable {
    private FeederUtils.FeederTestModes type;
    private int module;
    private int state;
    private int result;
    private String name;

    public FeederTestUnit(FeederUtils.FeederTestModes type, String name, int module, int state) {
        this.type = type;
        this.name = name;
        this.module = module;
        this.state = state;
    }

    public void setType(FeederUtils.FeederTestModes type) {
        this.type = type;
    }

    public FeederUtils.FeederTestModes getType() {
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
