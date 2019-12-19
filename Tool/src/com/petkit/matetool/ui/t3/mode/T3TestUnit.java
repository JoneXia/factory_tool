package com.petkit.matetool.ui.t3.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.t3.utils.T3Utils;

/**
 * 猫厕所测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class T3TestUnit extends DeviceTestUnit<T3Utils.T3TestModes> {


    public T3TestUnit(T3Utils.T3TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
