package com.petkit.matetool.ui.feederMini.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.feeder.mode.Feeder;
import com.petkit.matetool.ui.feeder.mode.FeedersError;
import com.petkit.matetool.ui.feederMini.mode.FeederMiniTestUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_D2;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class FeederMiniUtils {

    public static final String FEEDER_MINI_SESSION = "FEEDER_MINI_SESSION";

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_FEEDER_MINI_TESTER   = "EXTRA_FEEDER_MINI_TESTER";
    public static final String EXTRA_FEEDER   = "EXTRA_FEEDER";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_FEEDER_MINI_TESTER = "SHARED_FEEDER_MINI_TESTER";

    private static final String SHARED_SERIALIZABLE_DAY     = "FeederMini_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "FeederMini_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "FeederMini_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "FeederMini_SnFileNumber";
    private static final String SHARED_FEEDER_MINI_ERROR_INFO     = "FeederMini_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "feeder_mini_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "feeder_mini_check_info.txt";
    public static final String FEEDERMINI_STORE_DIR     = ".feederMini/";

    public static ArrayList<Feeder> mTempFeeders = new ArrayList<>();

    public enum FeederMiniTestModes {
        TEST_MODE_KEY,
        TEST_MODE_DOOR,
        TEST_MODE_MOTOR,
        TEST_MODE_LIGHT,
        TEST_MODE_DC,
        TEST_MODE_BAT,
        TEST_MODE_IR,
        TEST_MODE_SN,
        TEST_MODE_PRINT,
        TEST_MODE_MAC,
        TEST_MODE_AGEINGRESULT,
        TEST_MODE_RESET_ID,
        TEST_MODE_TIME,
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
    public static ArrayList<FeederMiniTestUnit> generateFeederMiniTestUnitsForType(int type) {
        ArrayList<FeederMiniTestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_SN, "写入SN", 12, 2));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type != TYPE_TEST_PARTIALLY) {
                results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_AGEINGRESULT, "老化结果", 97, 1));
            }
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_KEY, "按键测试", 0, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_LIGHT, "外设测试", 1, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_IR, "红外测试", 16, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_DOOR, "门马达", 5, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_MOTOR, "叶轮马达", 6, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_DC, "直流电压", 9, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_BAT, "电池电压", 10, 1));
            results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_TIME, "时钟测试", 15, 1));

            if (type == TYPE_TEST) {
                results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_SN, "写入SN", 12, 2));
            }

            if (type == TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_PRINT, "打印标签", -1, type == TYPE_TEST ? 2 : 1));
                if (PERMISSION_ERASE) {
                    results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new FeederMiniTestUnit(FeederMiniTestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
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
            throw  new RuntimeException("FeederMini Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        return DeviceCommonUtils.generateSN(CommonUtils.getDateStringByOffset(0), DEVICE_TYPE_CODE_D2, tester.getStation(), serializableNumber);
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
     * 生成的SN时，先存储临时数据，SN成功写入设备后从临时数据中删除
     *
     * @param feeder 喂食器
     */
    public static void storeTempFeederInfo(Feeder feeder) {
        for (Feeder temp : mTempFeeders) {
            if (temp.equals(feeder)) {
                return;
            }
        }

        mTempFeeders.add(feeder);
    }

    /**
     * 移除临时数据中的feeder
     * @param feeder feeder
     */
    public static void removeTempFeederInfo(Feeder feeder) {
        for (Feeder temp : mTempFeeders) {
            if (temp.equals(feeder)) {
                mTempFeeders.remove(temp);
                return;
            }
        }
    }

    /**
     * 检查临时数据中是否存在Feeder
     * @param feeder
     * @return
     */
    public static boolean isFeederInTemp(Feeder feeder) {
        for (Feeder temp : mTempFeeders) {
            if (temp.equals(feeder)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 存储测试完成的设备信息
     * @param feeder 喂食器
     */
    public static void storeSucceedFeederInfo(Feeder feeder, String ageingResult) {
        if(feeder == null || !feeder.checkValid()) {
            throw  new RuntimeException("store feederMini failed, " + (feeder == null ? "feederMini is null !" : feeder.toString()));
        }

        PetkitLog.d("store feederMini info: " + feeder.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreFeederInfoFilePath(), feeder.generateMainJson(ageingResult) + ",", true);
    }

    /**
     * 获取存储SN的文件，内部实现文件内容的条件限制，文件名自增
     * @return
     */
    private static String getStoreFeederInfoFilePath() {
        String fileName = CommonUtils.getSysMap(SHARED_SN_FILE_NAME);
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), SHARED_SN_FILE_NUMBER, 0);

        if (fileName != null &&             //文件不存在，或者文件不是今天产生的，都需要重新生成文件
                (!fileName.startsWith(getFileName()) || !(new File(getFeederMiniStoryDir() + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getFeederMiniStoryDir();
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
            return getFeederMiniStoryDir() + fileName;
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
            String content = FileUtils.readFileToString(new File(getFeederMiniStoryDir() + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * 检查本地是否有SN缓存
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = getFeederMiniStoryDir();
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
            CommonUtils.addSysMap(SHARED_FEEDER_MINI_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_FEEDER_MINI_ERROR_INFO, new Gson().toJson(feedersError));
        }
    }

    /**
     * 获取重复的错误信息
     * @return FeedersError
     */
    public static FeedersError getFeedersErrorMsg() {
        String msg = CommonUtils.getSysMap(SHARED_FEEDER_MINI_ERROR_INFO);
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, FeedersError.class);
    }


    public static void storeMainTainInfo(Feeder feeder) {
        if(feeder == null || !feeder.checkValid()) {
            return;
        }
        String dir = getFeederMiniStoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = getFeederMiniStoryDir() + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(feeder.getMac())) {
            return;
        }
        String info = feeder.generateJson();
        PetkitLog.d("store feederMini info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Feeder feeder) {
        if(feeder == null || !feeder.checkValid()) {
            return;
        }

        String dir = getFeederMiniStoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = getFeederMiniStoryDir() + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(feeder.getMac())) {
            return;
        }
        String info = feeder.generateCheckJson();
        PetkitLog.d("store feederMini info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

    public static String getFeederMiniStoryDir() {
        return CommonUtils.getAppCacheDirPath() + FEEDERMINI_STORE_DIR;
    }

}
