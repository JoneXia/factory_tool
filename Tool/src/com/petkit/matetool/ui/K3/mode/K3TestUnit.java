package com.petkit.matetool.ui.K3.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.K3.utils.K3Utils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class K3TestUnit extends DeviceTestUnit<K3Utils.K3TestModes> {


    public K3TestUnit(K3Utils.K3TestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
