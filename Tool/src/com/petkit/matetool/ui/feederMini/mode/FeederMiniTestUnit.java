package com.petkit.matetool.ui.feederMini.mode;


import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;

import java.io.Serializable;

/**
 * 喂食器测试单元
 *
 * Created by Jone on 17/5/2.
 */
public class FeederMiniTestUnit implements Serializable {
    private FeederMiniUtils.FeederMiniTestModes type;
    private int module;
    private int state;
    private int result;
    private String name;

    public FeederMiniTestUnit(FeederMiniUtils.FeederMiniTestModes type, String name, int module, int state) {
        this.type = type;
        this.name = name;
        this.module = module;
        this.state = state;
    }

    public void setType(FeederMiniUtils.FeederMiniTestModes type) {
        this.type = type;
    }

    public FeederMiniUtils.FeederMiniTestModes getType() {
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
