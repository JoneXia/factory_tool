package com.petkit.matetool.ui.D4.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D4.mode.D4TestUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_D4;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class D4Utils {

    public static final String D4_SESSION = "D4_SESSION";

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_D4_TESTER   = "EXTRA_D4_TESTER";
    public static final String EXTRA_D4   = "EXTRA_D4";
    public static final String EXTRA_ERROR_D4   = "EXTRA_ERROR_D4";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_D4_TESTER = "SHARED_D4_TESTER";

    private static final String SHARED_SERIALIZABLE_DAY     = "D4_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "D4_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "D4_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "D4_SnFileNumber";
    private static final String SHARED_D4_ERROR_INFO     = "D4_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "D4_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "D4_check_info.txt";
    public static final String D4_STORE_DIR     = ".D4/";

    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum D4TestModes {
        TEST_MODE_TIME, //时钟
        TEST_MODE_DC,   //电压
        TEST_MODE_BT,   //蓝牙
        TEST_MODE_LED,  //指示灯和蜂鸣器
        TEST_MODE_KEY,  //按键
        TEST_MODE_IR,   //红外
        TEST_MODE_MOTOR,    //电机
        TEST_MODE_BAT, //电池
        TEST_MODE_MAC,
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AGEINGRESULT, //老化数据
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
    public static ArrayList<D4TestUnit> generateD4AutoTestUnits() {
        ArrayList<D4TestUnit> results = new ArrayList<>();

        results.add(new D4TestUnit(D4TestModes.TEST_MODE_DC, "电压测试", 0, 1));
        results.add(new D4TestUnit(D4TestModes.TEST_MODE_TIME, "时钟测试", 7, 1));
        results.add(new D4TestUnit(D4TestModes.TEST_MODE_BT, "蓝牙测试", 6, 1));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<D4TestUnit> generateD4TestUnitsForType(int type) {
        ArrayList<D4TestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type == TYPE_MAINTAIN) {
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_DC, "电压测试", 0, 1));
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_TIME, "时钟测试", 7, 1));
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_BT, "蓝牙测试", 6, 1));
            } else {
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_AUTO, "自动项测试", 6, 1));
            }
            if (type != TYPE_TEST_PARTIALLY) {
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_AGEINGRESULT, "老化结果", 97, 1));
            }

            results.add(new D4TestUnit(D4TestModes.TEST_MODE_LED, "指示灯、蜂鸣器测试", 1, 1));
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_KEY, "按键测试", 2, 1));
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_IR, "红外测试", 3, 1));
//            results.add(new D4TestUnit(D4TestModes.TEST_MODE_HOLZER, "粮桶霍尔", 8, 1));
//            results.add(new D4TestUnit(D4TestModes.TEST_MODE_TEMP, "环境温度", 9, 1));
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_MOTOR, "马达测试", 4, 1));
            results.add(new D4TestUnit(D4TestModes.TEST_MODE_BAT, "电池", 5, 1));

            if (type != TYPE_TEST_PARTIALLY) {
                if (type == TYPE_TEST) {
                    results.add(new D4TestUnit(D4TestModes.TEST_MODE_SN, "写入SN", 98, 2));
                }
            }
            if (type != TYPE_TEST_PARTIALLY && type != TYPE_CHECK) {
                results.add(new D4TestUnit(D4TestModes.TEST_MODE_PRINT, "打印标签", -1, type == TYPE_TEST ? 2 : 1));
            }

            if (type == TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
//                results.add(new D4TestUnit(D4TestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
//                results.add(new D4TestUnit(D4TestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
            }
        }
        return results;
    }

    /**
     * 生成Sn，根据Tester的类型
     * @param tester 测试者信息
     * @return sn
     */
    public static String generateSNForTester(Tester tester) {
        if(tester == null || !tester.checkValid()) {
            throw  new RuntimeException("D4 Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append(DEVICE_TYPE_CODE_D4)
                .append(tester.getStation())
                .append(serializableNumber);

        if(stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

        return stringBuilder.toString().toUpperCase();
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
     * @param device D4
     */
    public static void storeSucceedDeviceInfo(Device device, String ageingResult) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("store D4 failed, " + (device == null ? "D4 is null !" : device.toString()));
        }

        PetkitLog.d("store D4 info: " + device.generateMainJson(ageingResult));
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
                (!fileName.startsWith(getFileName()) || !(new File(getD4StoryDir() + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getD4StoryDir();
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
            return getD4StoryDir() + fileName;
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
            String content = FileUtils.readFileToString(new File(getD4StoryDir() + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * 检查本地是否有SN缓存
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = getD4StoryDir();
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
            CommonUtils.addSysMap(SHARED_D4_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_D4_ERROR_INFO, new Gson().toJson(devicesError));
        }
    }

    /**
     * 获取重复的错误信息
     * @return DevicesError
     */
    public static DevicesError getDevicesErrorMsg() {
        String msg = CommonUtils.getSysMap(SHARED_D4_ERROR_INFO);
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, DevicesError.class);
    }


    public static void storeMainTainInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }
        String dir = getD4StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = getD4StoryDir() + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateJson();
        PetkitLog.d("store D4 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }

        String dir = getD4StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = getD4StoryDir() + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateCheckJson();
        PetkitLog.d("store D4 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

    public static String getD4StoryDir() {
        return CommonUtils.getAppCacheDirPath() + D4_STORE_DIR;
    }

}
