package com.petkit.matetool.ui.HG;


import com.petkit.matetool.model.DeviceTestUnit;

/**
 * HG测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class HGTestUnit extends DeviceTestUnit<HGUtils.HGTestModes> {


    public HGTestUnit(HGUtils.HGTestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
