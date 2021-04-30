package com.petkit.matetool.ui.t4.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.t4.utils.T4Utils;

/**
 * 猫厕所测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class T4TestUnit extends DeviceTestUnit<T4Utils.T4TestModes> {


    public T4TestUnit(T4Utils.T4TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
