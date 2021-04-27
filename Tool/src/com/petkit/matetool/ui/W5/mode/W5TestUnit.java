package com.petkit.matetool.ui.W5.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.W5.utils.W5Utils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class W5TestUnit extends DeviceTestUnit<W5Utils.W5TestModes> {


    public W5TestUnit(W5Utils.W5TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
