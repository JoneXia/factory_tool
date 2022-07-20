package com.petkit.matetool.ui.t4.utils;

import com.google.gson.Gson;
import com.petkit.matetool.ui.t4.mode.T4TestUnit;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class T4Utils {


    public enum T4TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_LED,  //显示屏和蜂鸣器
        TEST_MODE_KEY,  //按键
        TEST_MODE_MOTOR,    //电机
        TEST_MODE_MOTOR_2,   //集便盒电机
        TEST_MODE_BALANCE_SET,  //半成品秤校准
        TEST_MODE_BALANCE_SET_2,  //成品秤校准
        TEST_MODE_BALANCE,  //秤读取
        TEST_MODE_PROXIMITY, //接近
        TEST_MODE_COVER_HOLZER, //上盖霍尔
        TEST_MODE_BT,   //蓝牙
        TEST_MODE_TIME, //时钟
        TEST_MODE_MAC,
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AGEINGRESULT, //老化数据
        TEST_MODE_AUTO,
        TEST_MODE_PRINT     //打印标签
    }

    /**
     * 获取Socket默认的数据格式
     *
     * @param key key
     * @return String
     */
    public static String getDefaultRequestForKey(int key) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        return new Gson().toJson(data);
    }

    /**
     * 获取Socket数据格式
     *
     * @param key key
     * @param payload content
     * @return json
     */
    public static String getRequestForKeyAndPayload(int key, HashMap<String, Object> payload) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("payload", payload);
        return new Gson().toJson(data);
    }

    /**
     *
     * @return ArrayList
     */
    public static ArrayList<T4TestUnit> generateT4AutoTestUnits() {
        ArrayList<T4TestUnit> results = new ArrayList<>();

        results.add(new T4TestUnit(T4TestModes.TEST_MODE_DC, "电压测试", 0, 1));
        results.add(new T4TestUnit(T4TestModes.TEST_MODE_TIME, "时钟测试", 9, 1));
        results.add(new T4TestUnit(T4TestModes.TEST_MODE_BT, "蓝牙测试", 8, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<T4TestUnit> generateT4TestUnitsForType(int type) {
        ArrayList<T4TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_RESET_SN, "重写SN", 12, 2));
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type != Globals.TYPE_TEST_PARTIALLY) {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_AGEINGRESULT, "老化结果", 97, 1));
            }

            if (type == Globals.TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_DC, "电压测试", 0, 1));
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_TIME, "时钟测试", 9, 1));
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_BT, "蓝牙测试", 8, 1));
            } else {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_AUTO, "自动项测试", 10, 1));
            }

            results.add(new T4TestUnit(T4TestModes.TEST_MODE_LED, "显示屏和蜂鸣器测试", 1, 1));
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_KEY, "按键测试", 2, 1));
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_MOTOR_2, "集便盒电机测试", 3, 1));

            if (type == Globals.TYPE_TEST) {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_BALANCE_SET_2, "秤校准", 5, 1));
            } else if (type != Globals.TYPE_CHECK) {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_BALANCE_SET, "秤校准", 5, 1));
            } else {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_BALANCE, "秤读取", 5, 3));
            }
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_MOTOR, "马达测试", 4, 1));

            results.add(new T4TestUnit(T4TestModes.TEST_MODE_PROXIMITY, "接近模组", 6, 1));
            results.add(new T4TestUnit(T4TestModes.TEST_MODE_COVER_HOLZER, "上盖霍尔", 7, 1));

            if (type == Globals.TYPE_TEST) {
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_SN, "写入SN", 12, 2));
            }

            if (type == Globals.TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                results.add(new T4TestUnit(T4TestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new T4TestUnit(T4TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new T4TestUnit(T4TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }

}
