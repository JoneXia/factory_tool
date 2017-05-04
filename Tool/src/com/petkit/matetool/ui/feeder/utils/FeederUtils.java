package com.petkit.matetool.ui.feeder.utils;

import com.google.gson.Gson;
import com.petkit.matetool.ui.feeder.mode.FeederTestUnit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class FeederUtils {

    public static final int TYPE_TEST    = 1;
    public static final int TYPE_MAINTAIN    = 2;
    public static final int TYPE_CHECK    = 3;

    public enum FeederTestModes {
        TEST_MODE_KEY,
        TEST_MODE_DOOR,
        TEST_MODE_MOTOR,
        TEST_MODE_LIGHT,
        TEST_MODE_DC,
        TEST_MODE_BAT,
        TEST_MODE_BALANCE,
        TEST_MODE_LID,
        TEST_MODE_SN,
        TEST_MODE_PRINT
    }

    public static String getDefaultResponseForKey(int key) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("state", 1);
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("payload", payload);
        return new Gson().toJson(data);
    }


    public static String getDefaultRequestForKey(int key) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        return new Gson().toJson(data);
    }

    public static String getRequestForKeyAndPayload(int key, HashMap<String, Object> payload) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("payload", payload);
        return new Gson().toJson(data);
    }

    public static ArrayList<FeederTestUnit> generateFeederTestUnitsForType(int type) {
        ArrayList<FeederTestUnit> results = new ArrayList<>();

        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_KEY, "按键测试", 0, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_LIGHT, "外设测试", 1, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_DOOR, "门马达", 5, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_MOTOR, "叶轮马达", 6, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_LID, "粮盖测试", 14, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_DC, "直流电压", 9, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BAT, "电池电压", 10, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BALANCE, "秤校准", 7, 1));
        results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_SN, "写入SN", 12, 1));
        if(type == TYPE_TEST) {
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_PRINT, "打印标签", 13, 1));
        }

        return results;
    }



}
