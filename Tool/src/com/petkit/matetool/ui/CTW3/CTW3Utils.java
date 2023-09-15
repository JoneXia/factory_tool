package com.petkit.matetool.ui.CTW3;

import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;
import static com.petkit.matetool.utils.Globals.TYPE_AFTERMARKET;
import static com.petkit.matetool.utils.Globals.TYPE_MAINTAIN;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class CTW3Utils {

    public static final int AQR_TYPE_NORMAL = 1;
    public static final int AQR_TYPE_MINI = 2;


    public enum CTW3TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_BAT,   //BAT电压
        TEST_MODE_LED,
        TEST_MODE_TIME,
        TEST_MODE_KEY,  //按键
        TEST_MODE_PUMP_RESET,   //校验没水泵时的电压
        TEST_MODE_PUMP,
        TEST_MODE_PROXIMITY, //接近
        TEST_MODE_BAT_SHIP, //电池运输模式
        TEST_MODE_CHARGING, //充电
        TEST_MODE_SN,   //写SN
        TEST_MODE_MAC,
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_PRINT     //打印标签
    }


    /**
     *
     * @return ArrayList
     */
    public static ArrayList<CTW3TestUnit> generateAutoTestUnits() {
        ArrayList<CTW3TestUnit> results = new ArrayList<>();

        results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_BAT, "电池电压测试", 242, 0));
        results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_DC, "电源电压测试", 242, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<CTW3TestUnit> generateTestUnitsForType(int type) {
        ArrayList<CTW3TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_MAC, "MAC重复", 97, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_RESET_SN, "写入SN", 98, 2));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type == TYPE_AFTERMARKET || type == TYPE_MAINTAIN) {
                results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_BAT, "电池电压测试", 242, 0));
                results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_DC, "电源电压测试", 242, 1));
            } else {
                results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_AUTO, "自动测试项", 99, 0));
            }
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_TIME, "时间测试", 242, 8));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_LED, "指示灯测试", 242, 2));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_KEY, "按键测试", 242, 3));
//            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_PUMP_RESET, "水泵校准", 242, 7));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_PUMP, "水泵测试", 242, 4));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_PROXIMITY, "接近测试", 242, 5));
            results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_CHARGING, "充电测试", 242, 6));

            if (type == Globals.TYPE_TEST || type == TYPE_AFTERMARKET) {
                results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }

            if (type == TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new CTW3TestUnit(CTW3TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }



}
