package com.petkit.matetool.ui.AQH1;

import com.google.gson.Gson;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class AQH1Utils {


    public enum AQH1TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_LED,  //显示屏和蜂鸣器
        TEST_MODE_KEY,  //按键
        TEST_MODE_TEMP,    //温度
        TEST_MODE_WATER,   //水位检测
        TEST_MODE_TEMP_SET_1,
        TEST_MODE_TEMP_SET_2,  //温度校准
        TEST_MODE_HOT,  //加热
        TEST_MODE_BT,   //蓝牙
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
    public static ArrayList<AQH1TestUnit> generateAutoTestUnits() {
        ArrayList<AQH1TestUnit> results = new ArrayList<>();

//        results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_DC, "电压测试", 0, 1));
//        results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_TIME, "时钟测试", 9, 1));
//        results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_BT, "蓝牙测试", 8, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<AQH1TestUnit> generateTestUnitsForType(int type) {
        ArrayList<AQH1TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_RESET_SN, "重写SN", 12, 2));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {

            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_BT, "蓝牙测试", 2, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_LED, "显示屏和蜂鸣器测试", 1, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_KEY, "按键测试", 3, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_TEMP_SET_1, "温度1校准", 7, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_TEMP_SET_2, "温度2校准", 7, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_TEMP, "温度测试", 4, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_WATER, "水位检测", 5, 1));
            results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_HOT, "加热测试", 6, 2));

            if (type != Globals.TYPE_TEST_PARTIALLY) {
                if (type == Globals.TYPE_TEST) {
                    results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_SN, "写入SN", 12, 2));
                }
            }

            if (type == Globals.TYPE_MAINTAIN || type == Globals.TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new AQH1TestUnit(AQH1TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }

}
