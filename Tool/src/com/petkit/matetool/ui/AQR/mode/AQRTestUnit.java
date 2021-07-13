package com.petkit.matetool.ui.AQR.mode;


import com.petkit.matetool.model.DeviceTestUnit;
import com.petkit.matetool.ui.AQR.utils.AQRUtils;

/**
 * D3测试单元
 *
 * Created by Jone on 17/5/2.T
 */
public class AQRTestUnit extends DeviceTestUnit<AQRUtils.AQRTestModes> {


    public AQRTestUnit(AQRUtils.AQRTestModes type, String name, int module, int state) {
        super(type, name, module, state);

    }


}
