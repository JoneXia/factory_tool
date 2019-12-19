package com.petkit.matetool.ui.cozy.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;

/**
 * 猫窝测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class CozyTestUnit extends DeviceTestUnit<CozyUtils.CozyTestModes> {


    public CozyTestUnit(CozyUtils.CozyTestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
