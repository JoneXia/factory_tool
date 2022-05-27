package com.petkit.matetool.ui.W5.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.W5.mode.W5TestUnit;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_W5;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_W5C;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class W5Utils {

    public static final String W5_SESSION = "W5_SESSION";

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final int W5_SENSOR_STANDARD_VALUE_MIN    = 10;
    public static final int W5_SENSOR_STANDARD_VALUE_MAX    = 1500;

    public static final String EXTRA_W5_TESTER   = "EXTRA_W5_TESTER";
    public static final String EXTRA_W5   = "EXTRA_W5";
    public static final String EXTRA_W5_TYPE = "EXTRA_W5_TYPE";
    public static final String EXTRA_ERROR_W5   = "EXTRA_ERROR_W5";

    public static final int W5_TYPE_NORMAL = 1;
    public static final int W5_TYPE_MINI = 2;

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_W5_TESTER = "SHARED_W5_TESTER";

    private static final String SHARED_SERIALIZABLE_DAY     = "W5_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "W5_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "W5_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "W5_SnFileNumber";
    private static final String SHARED_W5_ERROR_INFO     = "W5_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "W5_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "W5_check_info.txt";
    public static final String W5_STORE_DIR     = ".W5/";

    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum W5TestModes {
        TEST_MODE_DC,   //电压
        TEST_MODE_LED,
        TEST_MODE_PUMP,
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_MAC,
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
    public static ArrayList<W5TestUnit> generateW5AutoTestUnits() {
        ArrayList<W5TestUnit> results = new ArrayList<>();

        results.add(new W5TestUnit(W5TestModes.TEST_MODE_DC, "电量测试", BLEConsts.OP_CODE_BATTERY_KEY, 1));

        return results;
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
    public static ArrayList<W5TestUnit> generateW5TestUnitsForType(int type) {
        ArrayList<W5TestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new W5TestUnit(W5TestModes.TEST_MODE_MAC, "MAC重复", 97, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new W5TestUnit(W5TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new W5TestUnit(W5TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
//            results.add(new W5TestUnit(W5TestModes.TEST_MODE_LED, "LED灯测试", BLEConsts.OP_CODE_W5_TEST_STEP, 0));
            results.add(new W5TestUnit(W5TestModes.TEST_MODE_DC, "电压测试", BLEConsts.OP_CODE_BATTERY_KEY, 0));
            results.add(new W5TestUnit(W5TestModes.TEST_MODE_PUMP, "水泵测试", BLEConsts.OP_CODE_W5_TEST_STEP, 1));
//            if (type == TYPE_MAINTAIN) {
//                results.add(new W5TestUnit(W5TestModes.TEST_MODE_DC, "电量测试", BLEConsts.OP_CODE_BATTERY_KEY, 1));
//                results.add(new W5TestUnit(W5TestModes.TEST_MODE_SENSOR, "G-Sensor测试", BLEConsts.OP_CODE_W5_SENSOR_DATA, 1));
//            } else {
//                results.add(new W5TestUnit(W5TestModes.TEST_MODE_AUTO, "自动项测试", 6, 1));
//            }

            if (type == TYPE_TEST) {
                results.add(new W5TestUnit(W5TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }
            if (type != TYPE_TEST_PARTIALLY && type != TYPE_CHECK) {
                results.add(new W5TestUnit(W5TestModes.TEST_MODE_PRINT, "打印标签", -1, type == TYPE_TEST ? 2 : 1));
            }

            if (type == TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                if (PERMISSION_ERASE) {
                    results.add(new W5TestUnit(W5TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new W5TestUnit(W5TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }

    /**
     * 生成Sn，根据Tester的类型
     * @param tester 测试者信息
     * @return sn
     */
    public static String generateSNForTester(Tester tester, int w5Type) {
        if(tester == null || !tester.checkValid()) {
            throw  new RuntimeException("W5 Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        return DeviceCommonUtils.generateSN(CommonUtils.getDateStringByOffset(0),
                w5Type == W5_TYPE_MINI ? DEVICE_TYPE_CODE_W5C : DEVICE_TYPE_CODE_W5, tester.getStation(), serializableNumber);
    }

    /**
     * 初始化SN的参数
     * @param sn sn
     */
    public static void initSnSerializableNumber(String sn) {
        if(CommonUtils.isEmpty(sn) || sn.length() != 14) {
            clearSnSerializableNumber();
        } else {
            String day = CommonUtils.getDateStringByOffset(0).substring(2);
            String snDay = sn.substring(2, 8);
            if(day.equals(snDay)) {
                int number = Integer.valueOf(sn.substring(sn.length() - 4)) + 1;
                CommonUtils.addSysMap(SHARED_SERIALIZABLE_DAY, day);
                CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SERIALIZABLE_NUMBER, number);
            } else {
                clearSnSerializableNumber();
            }
        }
    }

    /**
     * 获取新的SN的末尾四位序列号
     * @param day 日期
     * @return String
     */
    private static String getNextSnSerializableNumber(String day) {
        String lastDay = CommonUtils.getSysMap(SHARED_SERIALIZABLE_DAY);
        int start = 0;
        if(lastDay.equals(day)) {
            start = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), SHARED_SERIALIZABLE_NUMBER, 0);
        }

        if(start > 9999) {
            return null;
        }

        CommonUtils.addSysMap(SHARED_SERIALIZABLE_DAY, day);
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SERIALIZABLE_NUMBER, start + 1);

        return String.format("%04d", start);
    }

    /**
     * 清除序列号相关参数
     */
    public static void clearSnSerializableNumber() {
        CommonUtils.addSysMap(SHARED_SERIALIZABLE_DAY, "");
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SERIALIZABLE_NUMBER, 0);

    }

    /**
     * 存储测试完成的设备信息
     * @param device W5
     */
    public static void storeSucceedDeviceInfo(Device device, String ageingResult) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("store W5 failed, " + (device == null ? "W5 is null !" : device.toString()));
        }

        PetkitLog.d("store W5 info: " + device.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreDeviceInfoFilePath(), device.generateMainJson(ageingResult) + ",", true);
    }

    /**
     * 获取存储SN的文件，内部实现文件内容的条件限制，文件名自增
     * @return
     */
    private static String getStoreDeviceInfoFilePath() {
        String fileName = CommonUtils.getSysMap(SHARED_SN_FILE_NAME);
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), SHARED_SN_FILE_NUMBER, 0);

        if (fileName != null &&             //文件不存在，或者文件不是今天产生的，都需要重新生成文件
                (!fileName.startsWith(getFileName()) || !(new File(getW5StoryDir() + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getW5StoryDir();
            if(!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            File outFile = new File(dir, getFileName() + ".txt");
            int i = 1;
            while (outFile.exists()) {
                outFile = new File(dir, getFileName() + "-" + (i++) + ".log");
            }
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogcatStorageHelper.addLog("file create failed !");
                LogcatStorageHelper.addLog(e.toString());
                return "";
//                throw  new RuntimeException("file create failed !");
            }
            LogcatStorageHelper.addLog("file name: " + outFile.getName() + ", sn number: " + 1);
            CommonUtils.addSysMap(SHARED_SN_FILE_NAME, outFile.getName());
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SN_FILE_NUMBER, 1);
            return outFile.getAbsolutePath();
        } else {
            fileSnNumber++;
            LogcatStorageHelper.addLog("file name: " + fileName + ", sn number: " + fileSnNumber);
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SN_FILE_NUMBER, fileSnNumber);
            return getW5StoryDir() + fileName;
        }
    }

    /**
     * 校验MAC是否存在重复
     * @param mac mac
     * @return bool
     */
    public static boolean checkMacIsDuplicate(String mac) {
        String fileName = CommonUtils.getSysMap(SHARED_SN_FILE_NAME);
        if(!CommonUtils.isEmpty(fileName)) {
            String content = FileUtils.readFileToString(new File(getW5StoryDir() + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * 检查本地是否有SN缓存
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = getW5StoryDir();
        if(new File(dir).exists()) {
            String filename = getFileName();
            String[] files = new File(dir).list();
            if (files != null && files.length > 0) {
                for (String item : files) {
                    if(!item.startsWith(filename)
                            && !FILE_MAINTAIN_INFO_NAME.equals(item)
                            && !FILE_CHECK_INFO_NAME.equals(item)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 存储重复的信息
     * @param devicesError error
     */
    public static void storeDuplicatedInfo(DevicesError devicesError) {
        if(devicesError == null || ((devicesError.getMac() == null || devicesError.getMac().size() == 0)
                        && (devicesError.getSn() == null || devicesError.getSn().size() == 0))) {
            CommonUtils.addSysMap(SHARED_W5_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_W5_ERROR_INFO, new Gson().toJson(devicesError));
        }
    }

    /**
     * 获取重复的错误信息
     * @return DevicesError
     */
    public static DevicesError getDevicesErrorMsg() {
        String msg = CommonUtils.getSysMap(SHARED_W5_ERROR_INFO);
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, DevicesError.class);
    }


    public static void storeMainTainInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }
        String dir = getW5StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = getW5StoryDir() + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateJson();
        PetkitLog.d("store W5 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }

        String dir = getW5StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = getW5StoryDir() + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateCheckJson();
        PetkitLog.d("store W5 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

    public static String getW5StoryDir() {
        return CommonUtils.getAppCacheDirPath() + W5_STORE_DIR;
    }

    public static void stopBle (Context context) {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
