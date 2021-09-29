package com.petkit.matetool.ui.R2;

import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class R2Utils {


    public enum R2TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_LED,
        TEST_MODE_TEMP,     //温度
        TEST_MODE_WATER,    //水位
        TEST_MODE_HEAT, //加热
        TEST_MODE_HEAT_PROTECT, //加热保护
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_PRINT     //打印标签
    }


    /**
     *
     * @return ArrayList
     */
    public static ArrayList<R2TestUnit> generateAutoTestUnits() {
        ArrayList<R2TestUnit> results = new ArrayList<>();

        results.add(new R2TestUnit(R2TestModes.TEST_MODE_DC, "电量测试", 210, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<R2TestUnit> generateAQRTestUnitsForType(int type) {
        ArrayList<R2TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {

        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_AUTO, "自动测试项", 98, 2));
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_TEMP, "温度检测", 210, 1));
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_WATER, "水位检测", 210, 1));
            results.add(new R2TestUnit(R2TestModes.TEST_MODE_HEAT, "加热测试", 210, 1));

            if (type == Globals.TYPE_CHECK) {
                results.add(new R2TestUnit(R2TestModes.TEST_MODE_HEAT_PROTECT, "加热保护", 210, 1));
            }
            if (type == Globals.TYPE_TEST) {
                results.add(new R2TestUnit(R2TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }
            if (type != Globals.TYPE_TEST_PARTIALLY && type != Globals.TYPE_CHECK) {
                results.add(new R2TestUnit(R2TestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
            }

            if (type == Globals.TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                if (PERMISSION_ERASE) {
                    results.add(new R2TestUnit(R2TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new R2TestUnit(R2TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }



}
