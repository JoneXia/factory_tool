package com.petkit.matetool.ui.D4.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.D4.utils.D4Utils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class D4TestUnit extends DeviceTestUnit<D4Utils.D4TestModes> {


    public D4TestUnit(D4Utils.D4TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
