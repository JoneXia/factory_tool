package com.petkit.matetool.ui.P3.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.ui.P3.mode.P3TestUnit;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class P3Utils {


    public static final int P3_SENSOR_STANDARD_VALUE_MIN    = 10;
    public static final int P3_SENSOR_STANDARD_VALUE_MAX    = 1500;

    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum P3TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_SENSOR,   //G-SENSOR
        TEST_MODE_LED,  //蜂鸣器
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_PRINT     //打印标签
    }

    public static String getDefaultResponseForKey(int key) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("state", 1);
        HashMap<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("payload", payload);
        return new Gson().toJson(data);
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
    public static ArrayList<P3TestUnit> generateP3AutoTestUnits() {
        ArrayList<P3TestUnit> results = new ArrayList<>();

        results.add(new P3TestUnit(P3TestModes.TEST_MODE_DC, "电量测试", BLEConsts.OP_CODE_BATTERY_KEY, 1));
        results.add(new P3TestUnit(P3TestModes.TEST_MODE_SENSOR, "G-Sensor测试", BLEConsts.OP_CODE_P3_SENSOR_DATA, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<P3TestUnit> generateP3TestUnitsForType(int type) {
        ArrayList<P3TestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {

        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new P3TestUnit(P3TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new P3TestUnit(P3TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            results.add(new P3TestUnit(P3TestModes.TEST_MODE_LED, "蜂鸣器测试", BLEConsts.OP_CODE_P3_RING, 1));

            if (type == Globals.TYPE_MAINTAIN) {
                results.add(new P3TestUnit(P3TestModes.TEST_MODE_DC, "电量测试", BLEConsts.OP_CODE_BATTERY_KEY, 1));
                results.add(new P3TestUnit(P3TestModes.TEST_MODE_SENSOR, "G-Sensor测试", BLEConsts.OP_CODE_P3_SENSOR_DATA, 1));
            } else {
                results.add(new P3TestUnit(P3TestModes.TEST_MODE_AUTO, "自动项测试", 6, 1));
            }

            if (type != Globals.TYPE_TEST_PARTIALLY) {
                if (type == Globals.TYPE_TEST) {
                    results.add(new P3TestUnit(P3TestModes.TEST_MODE_SN, "写入SN", 98, 2));
                }
            }
            if (type != Globals.TYPE_TEST_PARTIALLY && type != Globals.TYPE_CHECK) {
                results.add(new P3TestUnit(P3TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
            }

            if (type == Globals.TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                if (PERMISSION_ERASE) {
                    results.add(new P3TestUnit(P3TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new P3TestUnit(P3TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }


    public static void stopBle (Context context) {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
