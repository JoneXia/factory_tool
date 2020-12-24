package com.petkit.matetool.ui.feeder.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feeder.mode.FeederTestUnit;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.feeder.mode.FeedersError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_D1;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class FeederUtils {

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_FEEDER_TESTER   = "EXTRA_FEEDER_TESTER";
    public static final String EXTRA_FEEDER   = "EXTRA_FEEDER";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_FEEDER_TESTER = "SHARED_FEEDER_TESTER";

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
        TEST_MODE_PRINT,
        TEST_MODE_MAC,
        TEST_MODE_AGEINGRESULT,
        TEST_MODE_RESET_ID,
        TEST_MODE_RESET_SN
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
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<FeederTestUnit> generateFeederTestUnitsForType(int type) {
        ArrayList<FeederTestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_SN, "写入SN", 12, 2));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type != TYPE_TEST_PARTIALLY) {
                results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_AGEINGRESULT, "老化结果", 97, 1));
            }
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_KEY, "按键测试", 0, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_LIGHT, "外设测试", 1, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_DOOR, "门马达", 5, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_MOTOR, "叶轮马达", 6, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_LID, "粮盖测试", 14, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_DC, "直流电压", 9, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BAT, "电池电压", 10, 1));
            results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BAT, "时钟测试", 15, 1));
            if (type != TYPE_CHECK) {
                results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BALANCE, "秤校准", 7, 1));
            } else {
                results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_BALANCE, "秤读取", 7, 3));
            }
            if (type != TYPE_TEST_PARTIALLY) {
                if (type == TYPE_TEST) {
                    results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_SN, "写入SN", 12, 2));
                }
                results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_PRINT, "打印标签", -1, type == TYPE_TEST ? 2 : 1));
            }

            if (type == TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                if (PERMISSION_ERASE) {
                    results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new FeederTestUnit(FeederTestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
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
    public static String generateSNForTester(Tester tester) {
        if(tester == null || !tester.checkValid()) {
            throw  new RuntimeException("Feeder Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append(DEVICE_TYPE_CODE_D1)
                .append(tester.getStation())
                .append(serializableNumber);

        if(stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

//        return "0A170516P10022";
        return stringBuilder.toString();
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
                CommonUtils.addSysMap("SerializableDay", day);
                CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "SerializableNumber", number);
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
        String lastDay = CommonUtils.getSysMap("SerializableDay");
        int start = 0;
        if(lastDay.equals(day)) {
            start = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), "SerializableNumber", 0);
        }

        if(start > 9999) {
            return null;
        }

        CommonUtils.addSysMap("SerializableDay", day);
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "SerializableNumber", start + 1);

        return String.format("%04d", start);
    }

    /**
     * 清除序列号相关参数
     */
    public static void clearSnSerializableNumber() {
        CommonUtils.addSysMap("SerializableDay", "");
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "SerializableNumber", 0);

    }

    /**
     * 存储测试完成的设备信息
     * @param feeder 喂食器
     */
    public static void storeSucceedFeederInfo(Feeder feeder, String ageingResult) {
        if(feeder == null || !feeder.checkValid()) {
            throw  new RuntimeException("store feeder failed, " + (feeder == null ? "feeder is null !" : feeder.toString()));
        }

        PetkitLog.d("store feeder info: " + feeder.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreFeederInfoFilePath(), feeder.generateMainJson(ageingResult) + ",", true);
    }

    /**
     * 获取存储SN的文件，内部实现文件内容的条件限制，文件名自增
     * @return
     */
    private static String getStoreFeederInfoFilePath() {
        String fileName = CommonUtils.getSysMap("SnFileName");
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), "SnFileNumber", 0);

        String dir = CommonUtils.getAppCacheDirPath() + ".sn/";
        if (fileName != null &&             //文件不存在，或者文件不是今天产生的，都需要重新生成文件
                (!fileName.startsWith(getFileName()) || !new File(dir + fileName).exists())) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
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
                throw  new RuntimeException("file create failed !");
            }
            PetkitLog.d("file name: " + outFile.getName() + ", sn number: " + 1);
            CommonUtils.addSysMap("SnFileName", outFile.getName());
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "SnFileNumber", 1);
            return outFile.getAbsolutePath();
        } else {
            fileSnNumber++;
            PetkitLog.d("file name: " + fileName + ", sn number: " + fileSnNumber);
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "SnFileNumber", fileSnNumber);
            return CommonUtils.getAppCacheDirPath() + ".sn/" + fileName;
        }
    }

    /**
     * 校验MAC是否存在重复
     * @param mac mac
     * @return bool
     */
    public static boolean checkMacIsDuplicate(String mac) {
        String fileName = CommonUtils.getSysMap("SnFileName");
        if(!CommonUtils.isEmpty(fileName)) {
            String content = FileUtils.readFileToString(new File(CommonUtils.getAppCacheDirPath() + ".sn/" + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * 检查本地是否有SN缓存
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = CommonUtils.getAppCacheDirPath() + ".sn/";
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
     * @param feedersError error
     */
    public static void storeDuplicatedInfo(FeedersError feedersError) {
        if(feedersError == null || ((feedersError.getMac() == null || feedersError.getMac().size() == 0)
                        && (feedersError.getSn() == null || feedersError.getSn().size() == 0))) {
            CommonUtils.addSysMap("FeedersError", "");
        } else {
            CommonUtils.addSysMap("FeedersError", new Gson().toJson(feedersError));
        }
    }

    /**
     * 获取重复的错误信息
     * @return FeedersError
     */
    public static FeedersError getFeedersErrorMsg() {
        String msg = CommonUtils.getSysMap("FeedersError");
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, FeedersError.class);
    }


    public static final String FILE_MAINTAIN_INFO_NAME     = "feeder_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "feeder_check_info.txt";


    public static void storeMainTainInfo(Feeder feeder) {
        if(feeder == null || !feeder.checkValid()) {
            return;
        }
        String dir = CommonUtils.getAppCacheDirPath() + ".sn/";
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = CommonUtils.getAppCacheDirPath() + ".sn/" + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(feeder.getMac())) {
            return;
        }
        String info = feeder.generateJson();
        PetkitLog.d("store feeder info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Feeder feeder) {
        if(feeder == null || !feeder.checkValid()) {
            return;
        }

        String dir = CommonUtils.getAppCacheDirPath() + ".sn/";
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = CommonUtils.getAppCacheDirPath() + ".sn/" + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(feeder.getMac())) {
            return;
        }
        String info = feeder.generateCheckJson();
        PetkitLog.d("store feeder info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

}
