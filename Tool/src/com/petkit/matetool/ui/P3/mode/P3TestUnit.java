package com.petkit.matetool.ui.P3.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.P3.utils.P3Utils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class P3TestUnit extends DeviceTestUnit<P3Utils.P3TestModes> {


    public P3TestUnit(P3Utils.P3TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
