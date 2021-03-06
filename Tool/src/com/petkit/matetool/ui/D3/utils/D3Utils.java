package com.petkit.matetool.ui.D3.utils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D3.mode.D3TestUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;
import static com.petkit.matetool.utils.Globals.DEVICE_TYPE_CODE_D3;
import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class D3Utils {

    public static final String D3_SESSION = "D3_SESSION";

    public static final int TYPE_TEST_PARTIALLY         = 1;
    public static final int TYPE_TEST                   = 2;
    public static final int TYPE_MAINTAIN               = 3;
    public static final int TYPE_CHECK                  = 4;
    public static final int TYPE_DUPLICATE_MAC          = 5;
    public static final int TYPE_DUPLICATE_SN           = 6;

    public static final String EXTRA_D3_TESTER   = "EXTRA_D3_TESTER";
    public static final String EXTRA_D3   = "EXTRA_D3";
    public static final String EXTRA_ERROR_D3   = "EXTRA_ERROR_D3";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    public static final String SHARED_D3_TESTER = "SHARED_D3_TESTER";

    private static final String SHARED_SERIALIZABLE_DAY     = "D3_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "D3_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "D3_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "D3_SnFileNumber";
    private static final String SHARED_D3_ERROR_INFO     = "D3_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "D3_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "D3_check_info.txt";
    public static final String D3_STORE_DIR     = ".D3/";

    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum D3TestModes {
        TEST_MODE_TIME, //??????
        TEST_MODE_DC,   //??????
        TEST_MODE_BT,   //??????
        TEST_MODE_LED,  //??????????????????
        TEST_MODE_KEY,  //??????
        TEST_MODE_IR,   //??????
        TEST_MODE_HOLZER, //????????????
        TEST_MODE_TEMP, //??????
        TEST_MODE_MOTOR,    //??????
        TEST_MODE_BALANCE_SET,  //??????????????????
        TEST_MODE_BALANCE,  //?????????
        TEST_MODE_PROXIMITY, //???????????????
        TEST_MODE_BAT, //??????
        TEST_MODE_BAT_SHIP, //??????????????????
        TEST_MODE_MAC,
        TEST_MODE_SN,   //???SN
        TEST_MODE_RESET_SN, //??????SN
        TEST_MODE_RESET_ID, //??????ID
        TEST_MODE_AGEINGRESULT, //????????????
        TEST_MODE_AUTO,
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
    public static ArrayList<D3TestUnit> generateD3AutoTestUnits() {
        ArrayList<D3TestUnit> results = new ArrayList<>();

        results.add(new D3TestUnit(D3TestModes.TEST_MODE_DC, "????????????", 0, 1));
        results.add(new D3TestUnit(D3TestModes.TEST_MODE_TIME, "????????????", 11, 1));
        results.add(new D3TestUnit(D3TestModes.TEST_MODE_BT, "????????????", 10, 1));

        return results;
    }

    /**
     * ?????????????????????????????????????????????
     * @param type ????????????
     * @return ?????????
     */
    public static ArrayList<D3TestUnit> generateD3TestUnitsForType(int type) {
        ArrayList<D3TestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_MAC, "????????????", 99, 1));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_SN, "??????SN", 98, 2));
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_PRINT, "????????????", -1, 1));
        } else {
            if (type == TYPE_MAINTAIN) {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_DC, "????????????", 0, 1));
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_TIME, "????????????", 11, 1));
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_BT, "????????????", 10, 1));
            } else {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_AUTO, "???????????????", 10, 1));
            }
            if (type != TYPE_TEST_PARTIALLY) {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_AGEINGRESULT, "????????????", 97, 1));
            }

            results.add(new D3TestUnit(D3TestModes.TEST_MODE_LED, "?????????????????????????????????", 1, 1));
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_KEY, "????????????", 2, 1));
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_IR, "?????????????????????", 3, 1));
//            results.add(new D3TestUnit(D3TestModes.TEST_MODE_HOLZER, "????????????", 8, 1));
//            results.add(new D3TestUnit(D3TestModes.TEST_MODE_TEMP, "????????????", 9, 1));
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_MOTOR, "????????????", 4, 1));

            if (type != TYPE_CHECK && type != TYPE_TEST) {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_BALANCE_SET, "?????????", 5, 1));
            } else {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_BALANCE, "?????????", 5, 3));
            }

            results.add(new D3TestUnit(D3TestModes.TEST_MODE_PROXIMITY, "???????????????", 6, 1));
            results.add(new D3TestUnit(D3TestModes.TEST_MODE_BAT, "??????", 7, 1));

            if (type != TYPE_TEST_PARTIALLY) {
                if (type == TYPE_TEST) {
                    results.add(new D3TestUnit(D3TestModes.TEST_MODE_SN, "??????SN", 98, 2));
                }
            }
            if (type != TYPE_TEST_PARTIALLY && type != TYPE_CHECK) {
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_PRINT, "????????????", -1, type == TYPE_TEST ? 2 : 1));
            }

            if (type == TYPE_MAINTAIN) {        //??????ID??????????????????????????????
                results.add(new D3TestUnit(D3TestModes.TEST_MODE_BAT_SHIP, "??????????????????", 12, 1));
                if (PERMISSION_ERASE) {
                    results.add(new D3TestUnit(D3TestModes.TEST_MODE_RESET_SN, "??????SN", 97, 1));
                    results.add(new D3TestUnit(D3TestModes.TEST_MODE_RESET_ID, "??????ID", 98, 1));
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
            throw  new RuntimeException("D3 Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(day);
        if(serializableNumber == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append(DEVICE_TYPE_CODE_D3)
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
     * @param device D3
     */
    public static void storeSucceedDeviceInfo(Device device, String ageingResult) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("store D3 failed, " + (device == null ? "D3 is null !" : device.toString()));
        }

        PetkitLog.d("store D3 info: " + device.generateMainJson(ageingResult));
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
                (!fileName.startsWith(getFileName()) || !(new File(getD3StoryDir() + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getD3StoryDir();
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
            return getD3StoryDir() + fileName;
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
            String content = FileUtils.readFileToString(new File(getD3StoryDir() + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    /**
     * ?????????????????????SN??????
     * @return bool
     */
    public static boolean checkHasSnCache() {
        String dir = getD3StoryDir();
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
            CommonUtils.addSysMap(SHARED_D3_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_D3_ERROR_INFO, new Gson().toJson(devicesError));
        }
    }

    /**
     * ???????????????????????????
     * @return DevicesError
     */
    public static DevicesError getDevicesErrorMsg() {
        String msg = CommonUtils.getSysMap(SHARED_D3_ERROR_INFO);
        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, DevicesError.class);
    }


    public static void storeMainTainInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }
        String dir = getD3StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = getD3StoryDir() + FILE_MAINTAIN_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateJson();
        PetkitLog.d("store D3 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }

        String dir = getD3StoryDir();
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = getD3StoryDir() + FILE_CHECK_INFO_NAME;
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateCheckJson();
        PetkitLog.d("store D3 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }

    public static String getD3StoryDir() {
        return CommonUtils.getAppCacheDirPath() + D3_STORE_DIR;
    }

}
