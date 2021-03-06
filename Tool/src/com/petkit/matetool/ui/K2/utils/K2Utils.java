package com.petkit.matetool.ui.K2.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.mode.K2TestUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_K2;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class K2Utils {

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_K2_TESTER   = "EXTRA_K2_TESTER";
    public static final String EXTRA_K2   = "EXTRA_K2";
    public static final String EXTRA_ERROR_K2   = "EXTRA_ERROR_K2";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_K2_TESTER = "SHARED_K2_TESTER";

    private static final String SHARED_SERIALIZABLE_DAY     = "K2_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "K2_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "K2_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "K2_SnFileNumber";
    private static final String SHARED_K2_ERROR_INFO     = "K2_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "K2_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "K2_check_info.txt";
    public static final String K2_STORE_DIR     = ".K2/";

    public static final Integer[] DC_RANGE = new Integer[]{5500, 6500};

    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum K2TestModes {
        TEST_MODE_DC,   //??????
        TEST_MODE_AUTO,   //???????????????
        TEST_MODE_LED,  //?????????????????????
        TEST_MODE_LED_2,  //LED
        TEST_MODE_KEY,  //??????
        TEST_MODE_HOLZER, //????????????
        TEST_MODE_FAN, //??????
        TEST_MODE_TEMP, //?????????
        TEST_MODE_BT,   //??????
        TEST_MODE_TIME, //??????
        TEST_MODE_MAC,
        TEST_MODE_SN,   //???SN
        TEST_MODE_RESET_SN, //??????SN
        TEST_MODE_RESET_ID, //??????ID
        TEST_MODE_AGEINGRESULT,
        TEST_MODE_PRINT     //????????????
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
     * ??????Socket?????????????????????
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
     * ??????Socket????????????
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
    public static ArrayList<K2TestUnit> generateK2AutoTestUnits() {
        ArrayList<K2TestUnit> results = new ArrayList<>();

        results.add(new K2TestUnit(K2TestModes.TEST_MODE_DC, "????????????", 0, 1));
        results.add(new K2TestUnit(K2TestModes.TEST_MODE_TIME, "????????????", 11, 1));
        results.add(new K2TestUnit(K2TestModes.TEST_MODE_FAN, "????????????", 7, 1));
        results.add(new K2TestUnit(K2TestModes.TEST_MODE_BT, "????????????", 10, 1));

        return results;
    }


    /**
     * ?????????????????????????????????????????????
     * @param type ????????????
     * @return ?????????
     */
    public static ArrayList<K2TestUnit> generateK2TestUnitsForType(int type) {
        ArrayList<K2TestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_MAC, "????????????", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_SN, "??????SN", 12, 2));
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_PRINT, "????????????", -1, 1));
        } else {
            if (type != TYPE_TEST) {
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_AGEINGRESULT, "????????????", 97, 1));
            }

            if (type == TYPE_MAINTAIN) {
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_DC, "????????????", 0, 1));
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_TIME, "????????????", 11, 1));
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_FAN, "????????????", 7, 1));
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_BT, "????????????", 10, 1));
            } else {
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_AUTO, "???????????????", 10, 1));
            }

            results.add(new K2TestUnit(K2TestModes.TEST_MODE_LED, "LED???????????????", 1, 1));
//            results.add(new K2TestUnit(K2TestModes.TEST_MODE_LED_2, "LED?????????", 3, 1));
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_KEY, "????????????", 2, 2));
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_HOLZER, "??????????????????", 9, 1));
            results.add(new K2TestUnit(K2TestModes.TEST_MODE_TEMP, "???????????????", 8, 1));

            if (type != TYPE_TEST_PARTIALLY) {
                if (type == TYPE_TEST) {
                    results.add(new K2TestUnit(K2TestModes.TEST_MODE_SN, "??????SN", 12, 1));
                }
                if (type == TYPE_MAINTAIN) {
                }
                results.add(new K2TestUnit(K2TestModes.TEST_MODE_PRINT, "????????????", -1, type == TYPE_TEST ? 2 : 1));
            }

            if (type == TYPE_MAINTAIN) {        //??????ID??????????????????????????????
                if (PERMISSION_ERASE) {
                    results.add(new K2TestUnit(K2TestModes.TEST_MODE_RESET_SN, "??????SN", 97, 1));
                    results.add(new K2TestUnit(K2TestModes.TEST_MODE_RESET_ID, "??????ID", 98, 1));
                }
            }
        }
        return results;
    }

    /**
     * ??????Sn?????????Tester?????????
     * @param tester ???????????????
     * @return sn
     */
    public static String generateSNForTester(Tester tester) {
        if(tester == null || !tester.checkValid()) {
            throw  new RuntimeException("K2 Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append(DEVICE_TYPE_CODE_K2)
                .append(tester.getStation())
                .append(serializableNumber);

        if(stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

        return stringBuilder.toString().toUpperCase();
    }

    /**
     * ?????????SN?????????
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
     * ????????????SN????????????????????????
     * @param day ??????
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
     * ???????????????????????????
     */
    public static void clearSnSerializableNumber() {
        CommonUtils.addSysMap(SHARED_SERIALIZABLE_DAY, "");
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), SHARED_SERIALIZABLE_NUMBER, 0);

    }

    /**
     * ?????????????????????????????????
     * @param device K2
     */
    public static void storeSucceedDeviceInfo(Device device, String ageingResult) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("store K2 failed, " + (device == null ? "K2 is null !" : device.toString()));
        }

        PetkitLog.d("store K2 info: " + device.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreDeviceInfoFilePath(), device.generateMainJson(ageingResult) + ",", true);
    }

    /**
     * ????????????SN?????????????????????????????????????????????????????????????????????
     * @return
     */
    private static String getStoreDeviceInfoFilePath() {
        String fileName = CommonUtils.getSysMap(SHARED_SN_FILE_NAME);
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), SHARED_SN_FILE_NUMBER, 0);

        if (fileName != null &&             //?????????????????????????????????????????????????????????????????????????????????
                (!fileName.startsWith(getFileName()) || !(new File(getK2StoryDir() + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getK2StoryDir();
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
            return getK2StoryDir() + fileName;
        }
    }

    /**
     * ??????MAC??????????????????
     * @param mac mac
     * @return bool
     */
    public static boolean checkMacIsDuplicate(String mac) {
        String fileName = CommonUtils.getSysMap(SHARED_SN_FILE_NAME);
        if(!CommonUtils.isEmpty(fileName)) {
            String content = FileUtils.readFileToString(new File(getK2StoryDir() + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * ?????????????????????SN??????
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = getK2StoryDir();
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
     * ?????????????????????
     * @param devicesError error
     */
    public static void storeDuplicatedInfo(DevicesError devicesError) {
        if(devicesError == null || ((devicesError.getMac() == null || devicesError.getMac().size() == 0)
                        && (devicesError.getSn() == null || devicesError.getSn().size() == 0))) {
            CommonUtils.addSysMap(SHARED_K2_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_K2_ERROR_INFO, new Gson().toJson(devicesError));
        }
    }

    /**
     * ???????????????????????????
     * @return DevicesError
     */
    public static DevicesError getDevicesErrorMsg() {
        String msg = CommonUtils.getSysMap(SHARED_K2_ERROR_INFO);
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, DevicesError.class);
    }


    public static void storeMainTainInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }
        String dir = getK2StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = getK2StoryDir() + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateJson();
        PetkitLog.d("store K2 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }

        String dir = getK2StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = getK2StoryDir() + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateCheckJson();
        PetkitLog.d("store K2 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

    public static String getK2StoryDir() {
        return CommonUtils.getAppCacheDirPath() + K2_STORE_DIR;
    }

}
