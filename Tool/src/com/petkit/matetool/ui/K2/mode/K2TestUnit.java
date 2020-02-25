package com.petkit.matetool.ui.K2.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.K2.utils.K2Utils;

/**
 * K2测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class K2TestUnit extends DeviceTestUnit<K2Utils.K2TestModes> {


    public K2TestUnit(K2Utils.K2TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
