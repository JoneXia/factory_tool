package com.petkit.matetool.ui.K3.utils;

import com.petkit.matetool.ui.K3.mode.K3TestUnit;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class K3Utils {


    public enum K3TestModes {
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_MAC,
        TEST_MODE_PRINT     //打印标签
    }


    /**
     *
     TEST_MODE_DC,   //电压
     TEST_MODE_LED,
     TEST_MODE_PUMP,
     */
    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<K3TestUnit> generateK3TestUnitsForType(int type) {
        ArrayList<K3TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new K3TestUnit(K3TestModes.TEST_MODE_MAC, "MAC重复", 97, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new K3TestUnit(K3TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new K3TestUnit(K3TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {

            if (type == Globals.TYPE_TEST || type == Globals.TYPE_AFTERMARKET) {
                results.add(new K3TestUnit(K3TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }

            if (type == Globals.TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                results.add(new K3TestUnit(K3TestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new K3TestUnit(K3TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new K3TestUnit(K3TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }


}
