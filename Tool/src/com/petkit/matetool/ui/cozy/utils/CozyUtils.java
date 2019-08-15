package com.petkit.matetool.ui.cozy.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.cozy.mode.Cozy;
import com.petkit.matetool.ui.cozy.mode.CozyTestUnit;
import com.petkit.matetool.ui.cozy.mode.CozysError;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;

/**
 * Created by Jone on 17/12/1.
 */

public class CozyUtils {

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_COZY_TESTER   = "EXTRA_COZY_TESTER";
    public static final String EXTRA_COZY   = "EXTRA_COZY";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_COZY_TESTER = "SHARED_COZY_TESTER";

    public enum CozyTestModes {
        TEST_MODE_KEY,
        TEST_MODE_IR,
        TEST_MODE_FAN,
        TEST_MODE_LIGHT,
        TEST_MODE_COOL,
        TEST_MODE_HOT,
        TEST_MODE_VOLTAGE,
        TEST_MODE_TEMP,
        TEST_MODE_SN,
        TEST_MODE_PRINT,
        TEST_MODE_MAC,
        TEST_MODE_TEST,
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
    public static String getRequestForKeyAndPayload(int key, Object payload) {
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
    public static ArrayList<CozyTestUnit> generateCozyTestUnitsForType(int type) {
        ArrayList<CozyTestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_MAC, "设置重复", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {
            if (type != TYPE_TEST_PARTIALLY) {
                results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_AGEINGRESULT, "老化结果", 98, 1));
            }

            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_KEY, "按键测试", 0, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_LIGHT, "外设测试", 1, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_IR, "红外测试", 6, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_FAN, "风扇测试", 7, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_TEMP, "温湿度测试", 10, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_COOL, "制冷测试", 11, 1));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_HOT, "制热测试", 11, 2));
            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_VOLTAGE, "电流电压", 12, 1));
//            results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_TEST, "压力测试", 13, 1));
            if (type != TYPE_TEST_PARTIALLY) {
                if (type == TYPE_TEST) {
                    results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_SN, "写入SN", 98, 2));
//                    results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                }
                results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
            }

            if (type == TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
//                results.add(new CozyTestUnit(CozyTestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
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
            throw  new RuntimeException("Cozy Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append("C")
                .append(tester.getStation())
                .append(serializableNumber);

        if (stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

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
                CommonUtils.addSysMap("Cozy_SerializableDay", day);
                CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "Cozy_SerializableNumber", number);
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
        String lastDay = CommonUtils.getSysMap("Cozy_SerializableDay");
        int start = 0;
        if(lastDay.equals(day)) {
            start = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), "Cozy_SerializableNumber", 0);
        }

        if(start > 9999) {
            return null;
        }

        CommonUtils.addSysMap("Cozy_SerializableDay", day);
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "Cozy_SerializableNumber", start + 1);

        return String.format("%04d", start);
    }

    /**
     * 清除序列号相关参数
     */
    public static void clearSnSerializableNumber() {
        CommonUtils.addSysMap("Cozy_SerializableDay", "");
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "Cozy_SerializableNumber", 0);

    }

    /**
     * 存储测试完成的设备信息
     * @param cozy 猫窝
     */
    public static void storeSucceedCozyInfo(Cozy cozy, String ageingResult) {
        if(cozy == null || !cozy.checkValid()) {
            throw  new RuntimeException("store cozy failed, " + (cozy == null ? "cozy is null !" : cozy.toString()));
        }

        PetkitLog.d("store cozy info: " + cozy.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreCozyInfoFilePath(), cozy.generateMainJson(ageingResult) + ",", true);
    }

    /**
     * 获取存储SN的文件，内部实现文件内容的条件限制，文件名自增
     * @return
     */
    private static String getStoreCozyInfoFilePath() {
        String fileName = CommonUtils.getSysMap("Cozy_SnFileName");
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), "Cozy_SnFileNumber", 0);
        String dir = CommonUtils.getAppCacheDirPath() + ".cozy/";

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
            CommonUtils.addSysMap("Cozy_SnFileName", outFile.getName());
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "Cozy_SnFileNumber", 1);
            return outFile.getAbsolutePath();
        } else {
            fileSnNumber++;
            PetkitLog.d("file name: " + fileName + ", sn number: " + fileSnNumber);
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), "Cozy_SnFileNumber", fileSnNumber);
            return CommonUtils.getAppCacheDirPath() + ".cozy/" + fileName;
        }
    }

    /**
     * 校验MAC是否存在重复
     * @param mac mac
     * @return bool
     */
    public static boolean checkMacIsDuplicate(String mac) {
        String fileName = CommonUtils.getSysMap("Cozy_SnFileName");
        if(!CommonUtils.isEmpty(fileName)) {
            String content = FileUtils.readFileToString(new File(CommonUtils.getAppCacheDirPath() + ".cozy/" + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * 检查本地是否有SN缓存
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = CommonUtils.getAppCacheDirPath() + ".cozy/";
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
     * @param CozysError error
     */
    public static void storeDuplicatedInfo(CozysError CozysError) {
        if(CozysError == null || ((CozysError.getMac() == null || CozysError.getMac().size() == 0)
                && (CozysError.getSn() == null || CozysError.getSn().size() == 0))) {
            CommonUtils.addSysMap("Cozy_Error", "");
        } else {
            CommonUtils.addSysMap("Cozy_Error", new Gson().toJson(CozysError));
        }
    }

    /**
     * 获取重复的错误信息
     * @return CozysError
     */
    public static CozysError getCozysErrorMsg() {
        String msg = CommonUtils.getSysMap("Cozy_Error");
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, CozysError.class);
    }


    public static final String FILE_MAINTAIN_INFO_NAME     = "cozy_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "cozy_check_info.txt";


    public static void storeMainTainInfo(Cozy cozy) {
        if(cozy == null || !cozy.checkValid()) {
            return;
        }
        String dir = CommonUtils.getAppCacheDirPath() + ".cozy/";
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = CommonUtils.getAppCacheDirPath() + ".cozy/" + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(cozy.getMac())) {
            return;
        }
        String info = cozy.generateJson();
        PetkitLog.d("store cozy info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Cozy cozy) {
        if(cozy == null || !cozy.checkValid()) {
            return;
        }

        String dir = CommonUtils.getAppCacheDirPath() + ".cozy/";
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = CommonUtils.getAppCacheDirPath() + ".cozy/" + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(cozy.getMac())) {
            return;
        }
        String info = cozy.generateCheckJson();
        PetkitLog.d("store cozy info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }


}
