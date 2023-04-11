package com.petkit.matetool.ui.D4SH;


import com.petkit.matetool.model.DeviceTestUnit;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class D4SHTestUnit extends DeviceTestUnit<D4SHUtils.D4SHTestModes> {

    private boolean isContainVideo;

    public D4SHTestUnit(D4SHUtils.D4SHTestModes type, String name, int module, int state, boolean isContainVideo) {
        super(type, name, module, state);

        this.isContainVideo = isContainVideo;
    }

    public boolean isContainVideo() {
        return isContainVideo;
    }
}
