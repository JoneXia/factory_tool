package com.petkit.matetool.ui.D4H;


import com.petkit.matetool.model.DeviceTestUnit;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class D4HTestUnit extends DeviceTestUnit<D4HUtils.D4HTestModes> {

    private boolean isContainVideo;

    public D4HTestUnit(D4HUtils.D4HTestModes type, String name, int module, int state, boolean isContainVideo) {
        super(type, name, module, state);

        this.isContainVideo = isContainVideo;
    }

    public boolean isContainVideo() {
        return isContainVideo;
    }
}
