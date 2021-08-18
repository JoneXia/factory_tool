package com.petkit.matetool.ui.AQ1S.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.AQ1S.utils.AQ1SUtils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class AQ1STestUnit extends DeviceTestUnit<AQ1SUtils.AQ1STestModes> {


    public AQ1STestUnit(AQ1SUtils.AQ1STestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
