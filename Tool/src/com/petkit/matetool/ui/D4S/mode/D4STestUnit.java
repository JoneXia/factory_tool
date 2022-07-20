package com.petkit.matetool.ui.D4S.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.D4S.utils.D4SUtils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class D4STestUnit extends DeviceTestUnit<D4SUtils.D4STestModes> {


    public D4STestUnit(D4SUtils.D4STestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
