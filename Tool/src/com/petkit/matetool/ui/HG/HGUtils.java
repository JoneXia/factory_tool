package com.petkit.matetool.ui.HG;

import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;
import static com.petkit.matetool.utils.Globals.TYPE_AFTERMARKET;
import static com.petkit.matetool.utils.Globals.TYPE_MAINTAIN;
import static com.petkit.matetool.utils.Globals.TYPE_TEST;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class HGUtils {

    public static final int AQR_TYPE_NORMAL = 1;
    public static final int AQR_TYPE_MINI = 2;


    public enum HGTestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_SIGNAL,  //通信
        TEST_MODE_KEY,  //按键
        TEST_MODE_LED,  //5个8字灯，6个状态指示灯
        TEST_MODE_FAN,  //风扇
        TEST_MODE_TEMP_ANT,     //温湿度检测
        TEST_MODE_TEMP,         //3路温度传感器读取
        TEST_MODE_TEMP_SET,     //3路温度传感器校准
        TEST_MODE_PTC,  //PTC加热片
        TEST_MODE_ANION,    //负离子开关
        TEST_MODE_LIGHT,    //照明灯
        TEST_MODE_LOCK,    //激活状态
        TEST_MODE_SN,   //写SN
        TEST_MODE_MAC,
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_AGEINGRESULT, //老化数据
        TEST_MODE_PRINT     //打印标签
    }


    /**
     *
     * @return ArrayList
     */
    public static ArrayList<HGTestUnit> generateAutoTestUnits() {
        ArrayList<HGTestUnit> results = new ArrayList<>();
        results.add(new HGTestUnit(HGTestModes.TEST_MODE_SIGNAL, "通信测试", 242, 0));
        results.add(new HGTestUnit(HGTestModes.TEST_MODE_DC, "电压测试", 242, 1));
        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @param deviceType 设备类型
     * @return 测试项
     */
    public static ArrayList<HGTestUnit> generateTestUnitsForType(int type, int deviceType) {
        ArrayList<HGTestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new HGTestUnit(HGTestModes.TEST_MODE_MAC, "MAC重复", 97, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new HGTestUnit(HGTestModes.TEST_MODE_RESET_SN, "重写SN", 98, 2));
            results.add(new HGTestUnit(HGTestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type == TYPE_TEST) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_AGEINGRESULT, "老化测试", 245, 0));
            }

            if (type == TYPE_MAINTAIN || type == TYPE_AFTERMARKET) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_SIGNAL, "通信测试", 242, 0));
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_DC, "电压测试", 242, 1));
            } else {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_AUTO, "自动测试项", 242, 0));
            }

            results.add(new HGTestUnit(HGTestModes.TEST_MODE_KEY, "按键测试", 242, 5));
            results.add(new HGTestUnit(HGTestModes.TEST_MODE_LED, "指示灯测试", 242, 6));
            results.add(new HGTestUnit(HGTestModes.TEST_MODE_LIGHT, "照明灯测试", 242, 7));

            if (type != Globals.TYPE_TEST_MAINBOARD) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_ANION, "负离子测试", 242, 8));
            }

            if (type != Globals.TYPE_TEST_MAINBOARD) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_FAN, "风扇测试", 242, 3));
            }

            results.add(new HGTestUnit(HGTestModes.TEST_MODE_TEMP_ANT, "温湿度测试", 242, 2));

            if (type == Globals.TYPE_TEST_MAINBOARD || type == TYPE_MAINTAIN || type == TYPE_AFTERMARKET) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_TEMP_SET, "温度校准", 242, 2));
            }

            if (type != Globals.TYPE_TEST_MAINBOARD) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_TEMP, "温度读取", 242, 2));
            }

            if (type != Globals.TYPE_TEST_MAINBOARD) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_PTC, "加热片测试", 242, 4));
            }
            if (type == Globals.TYPE_CHECK || type == TYPE_MAINTAIN || type == TYPE_AFTERMARKET) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_LOCK, "激活状态", 'A', 0));
            }

            if (type == Globals.TYPE_TEST || type == TYPE_AFTERMARKET) {
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }

            if (type == Globals.TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                results.add(new HGTestUnit(HGTestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new HGTestUnit(HGTestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new HGTestUnit(HGTestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }



}
