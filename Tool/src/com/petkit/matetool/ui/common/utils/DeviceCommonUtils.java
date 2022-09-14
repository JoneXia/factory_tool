package com.petkit.matetool.ui.common.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.AQ1S.AQ1STestMainActivity;
import com.petkit.matetool.ui.AQH1.AQH1TestMainActivity;
import com.petkit.matetool.ui.AQR.AQRTestMainActivity;
import com.petkit.matetool.ui.D3.utils.D3Utils;
import com.petkit.matetool.ui.D4.utils.D4Utils;
import com.petkit.matetool.ui.D4S.D4STestMainActivity;
import com.petkit.matetool.ui.HG.HGTestMainActivity;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.K3.K3TestMainActivity;
import com.petkit.matetool.ui.P3.P3TestMainActivity;
import com.petkit.matetool.ui.R2.R2TestMainActivity;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.W5New.W5NTestMainActivity;
import com.petkit.matetool.ui.common.BLEErrorListActivity;
import com.petkit.matetool.ui.common.BLEStartActivity;
import com.petkit.matetool.ui.common.WifiErrorListActivity;
import com.petkit.matetool.ui.common.WifiStartActivity;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.ui.t3.utils.T3Utils;
import com.petkit.matetool.ui.t4.T4TestMainActivity;
import com.petkit.matetool.utils.Globals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.petkit.android.utils.LogcatStorageHelper.getFileName;

/**
 *
 *
 *
 *  Created by Jone on 29/7/21.
 */
public class DeviceCommonUtils {

    public static final String STORE_DIR = ".%s/";
    public static final String EXTRA_TESTER = "EXTRA_TESTER";
    public static final String EXTRA_DEVICE_TYPE   = "EXTRA_DEVICE_TYPE";
    public static final String EXTRA_DEVICE   = "EXTRA_DEVICE";
    public static final String EXTRA_ERROR_DEVICE   = "EXTRA_ERROR_DEVICE";
    public static final String EXTRA_TEST_TYPE = "TestType";
    public static final String EXTRA_DEVICE_SN = "EXTRA_DEVICE_SN";

    private static final String SHARED_SERIALIZABLE_DAY     = "%s_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "%s_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "%s_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "%s_SnFileNumber";
    private static final String SHARED_SN_ERROR_INFO = "%s_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "%s_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "%s_check_info.txt";


    //maintain/check/product
    public static final String URL_TYPE_MAINTAIN   = "maintain";
    public static final String URL_TYPE_CHECK      = "check";
    public static final String URL_TYPE_PRODUCT    = "product";
    public static final String URL_TYPE_LAST_SN    = "lastSNd";

    private static final int MAX_SN_NUMBER_SESSION = 200;

    private static HashMap<Integer, DeviceConfigInfo> mDeviceConfigs;

    public static void initDeviceConfig() {
        mDeviceConfigs = new HashMap<>();

        mDeviceConfigs.put(Globals.P3C, new DeviceConfigInfo(true, "P3", "P3", new String[]{"Petkit_P3C"},
                Globals.DEVICE_TYPE_CODE_NEW_P3C, P3TestMainActivity.class));
        mDeviceConfigs.put(Globals.P3D, new DeviceConfigInfo(true, "P3", "P3D", new String[]{"Petkit_P3D", "Petkit_P3C"},
                Globals.DEVICE_TYPE_CODE_NEW_P3D, P3TestMainActivity.class));
        mDeviceConfigs.put(Globals.T4, new DeviceConfigInfo(false, "T4", "T4", null,
                Globals.DEVICE_TYPE_CODE_NEW_T4, T4TestMainActivity.class));
        mDeviceConfigs.put(Globals.T4_p, new DeviceConfigInfo(false, "T4", "T4P", null,
                Globals.DEVICE_TYPE_CODE_NEW_T4_P, T4TestMainActivity.class));
        mDeviceConfigs.put(Globals.K3, new DeviceConfigInfo(true, "K3", "K3", new String[]{"Petkit_K3"},
                Globals.DEVICE_TYPE_CODE_NEW_K3, K3TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQR, new DeviceConfigInfo(true, "AQR", "AQR", new String[]{"Petkit_AQR"},
                Globals.DEVICE_TYPE_CODE_NEW_AQR, AQRTestMainActivity.class));
        mDeviceConfigs.put(Globals.AQ1S, new DeviceConfigInfo(true, "AQ", "AQ1S", new String[]{"Petkit_AQ", "Petkit_AQ1S"},
                Globals.DEVICE_TYPE_CODE_NEW_AQ1S, AQ1STestMainActivity.class));
        mDeviceConfigs.put(Globals.R2, new DeviceConfigInfo(true, "R2", "R2", new String[]{"Petkit_R2"},
                Globals.DEVICE_TYPE_CODE_NEW_R2, R2TestMainActivity.class));
        mDeviceConfigs.put(Globals.W5N, new DeviceConfigInfo(true, "W5", "W5N", new String[]{"Petkit_W5N", "Petkit_W4X"},
                Globals.DEVICE_TYPE_CODE_NEW_W5N, W5NTestMainActivity.class));
        mDeviceConfigs.put(Globals.W4X, new DeviceConfigInfo(true, "W5", "W4X", new String[]{"Petkit_W5N", "Petkit_W4X"},
                Globals.DEVICE_TYPE_CODE_NEW_W4X, W5NTestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_500, new DeviceConfigInfo(false, "AQH1", "AQH1_500", null,
                Globals.DEVICE_TYPE_CODE_NEW_AQH1_500, AQH1TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_1000, new DeviceConfigInfo(false, "AQH1", "AQH1_1000", null,
                Globals.DEVICE_TYPE_CODE_NEW_AQH1_1000, AQH1TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_500_A, new DeviceConfigInfo(false, "AQH1", "AQH1_500", null,
                Globals.DEVICE_TYPE_CODE_NEW_AQH1_500, AQH1TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_1000_A, new DeviceConfigInfo(false, "AQH1", "AQH1_1000", null,
                Globals.DEVICE_TYPE_CODE_NEW_AQH1_1000, AQH1TestMainActivity.class));
        mDeviceConfigs.put(Globals.CTW2, new DeviceConfigInfo(true, "W5", "CTW2", new String[]{"Petkit_W5N", "Petkit_W4X", "Petkit_CTW2"},
                Globals.DEVICE_TYPE_CODE_NEW_CTW2, W5NTestMainActivity.class));
//        mDeviceConfigs.put(Globals.D3_1, new DeviceConfigInfo(true, "D3_1", "D3_1", null,
//                Globals.DEVICE_TYPE_CODE_NEW_D3_1, D3TestMainActivity.class));
//        mDeviceConfigs.put(Globals.D4_1, new DeviceConfigInfo(true, "D4_1", "D4_1", null,
//                Globals.DEVICE_TYPE_CODE_NEW_D4_1, D4TestMainActivity.class));
        mDeviceConfigs.put(Globals.D4S, new DeviceConfigInfo(false, "D4S", "D4S", null,
                Globals.DEVICE_TYPE_CODE_NEW_D4S, D4STestMainActivity.class));
        mDeviceConfigs.put(Globals.HG, new DeviceConfigInfo(true, "HG", "HG", new String[]{"Petkit_HG"},
                Globals.DEVICE_TYPE_CODE_NEW_HG, HGTestMainActivity.class));
        mDeviceConfigs.put(Globals.HG_110V, new DeviceConfigInfo(true, "HG", "HG_110V", new String[]{"Petkit_HG"},
                Globals.DEVICE_TYPE_CODE_NEW_HG_100V, HGTestMainActivity.class));
    }

    /**
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceKeyByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            return mDeviceConfigs.get(deviceType).getDeviceKey();
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }


    /**
     *
     * 部分产品软件功能一致，分多个SKU，不共用测试者，可用此方法来区分
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceTesterKeyByType(int deviceType) {
        String key;
        switch (deviceType) {
            /**
             * 旧版本不区分W5/W5C，使用了"W5"作为key存储了Tester信息
             * 因为W5C先生产，为了兼容旧版本，新版本使用W5作为W5C的key，W5使用W5L作为key
             */
            case Globals.W5C:
                key = "W5";
                break;
            case Globals.W5:
                key = "W5L";
                break;
            default:
                if (mDeviceConfigs.get(deviceType) != null) {
                    key = mDeviceConfigs.get(deviceType).getDeviceTesterKey();
                } else {
                    throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
                }
        }

        return key;
    }


    /**
     *
     * @param deviceType
     * @return
     */
    public static String[] getDeviceNameByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            return mDeviceConfigs.get(deviceType).getDeviceNames();
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }

    /**
     *
     * @param deviceName
     * @param deviceType
     * @return
     */
    public static boolean checkDeviceNameByType(String deviceName, int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            if (mDeviceConfigs.get(deviceType).getDeviceNames() != null) {
                for (String name : mDeviceConfigs.get(deviceType).getDeviceNames()) {
                    if (name.equalsIgnoreCase(deviceName)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static Class getStartActivityByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            if (mDeviceConfigs.get(deviceType).isBleDevice()) {
                return BLEStartActivity.class;
            } else {
                return WifiStartActivity.class;
            }
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static Class getErrorActivityByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            if (mDeviceConfigs.get(deviceType).isBleDevice()) {
                return BLEErrorListActivity.class;
            } else {
                return WifiErrorListActivity.class;
            }
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }


    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static Class getMainActivityByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            return mDeviceConfigs.get(deviceType).getDeviceMainActiviy();
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support! " + deviceType);
        }
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceFlagByType(int deviceType) {
        if (mDeviceConfigs.get(deviceType) != null) {
            return mDeviceConfigs.get(deviceType).getDeviceSNFlag();
        } else {
            throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }
    }

    public static String getStorageDirForDevice(int deviceType) {
        return CommonUtils.getAppCacheDirPath() + String.format(STORE_DIR, getDeviceKeyByType(deviceType).toUpperCase());
    }


    /**
     *
     * @param filename
     * @return
     */
    public static String geturlTypeByFilename(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return URL_TYPE_PRODUCT;
        }

        if (filename.contains(URL_TYPE_MAINTAIN)) {
            return URL_TYPE_MAINTAIN;
        } else if (filename.contains(URL_TYPE_CHECK)) {
            return URL_TYPE_CHECK;
        } else {
            return URL_TYPE_PRODUCT;
        }
    }

    /**
     * 检查本地是否有SN缓存
     * @param deviceType
     * @return bool
     */
    public static boolean checkHasSnCache(int deviceType) {
        String dir = getStorageDirForDevice(deviceType);
        if(new File(dir).exists()) {
            String filename = getFileName();
            String[] files = new File(dir).list();
            if (files != null && files.length > 0) {
                for (String item : files) {
                    if(!item.startsWith(filename)
                            && !item.contains(String.format(FILE_MAINTAIN_INFO_NAME, getDeviceKeyByType(deviceType)))
                            && !item.contains(String.format(FILE_CHECK_INFO_NAME, getDeviceKeyByType(deviceType)))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     *
     * @param deviceType
     * @param urlType : maintain/check/product
     * @return
     */
    public static String getUrlByTypeForDevice(int deviceType, String urlType) {
        String url;
        String key = getDeviceKeyByType(deviceType).toLowerCase();
        switch (urlType) {
            case URL_TYPE_MAINTAIN:
                url = String.format("/api/%s/maintain/repair", key);
                break;
            case URL_TYPE_CHECK:
                url = String.format("/api/%s/maintain/inspect", key);
                break;
            case URL_TYPE_PRODUCT:
                url = String.format("/api/%s/batch", key);
                break;
            case URL_TYPE_LAST_SN:
                url = String.format("/api/%s/latest", key);
                break;
            default:
                throw  new RuntimeException("urlType not support!");
        }

        return url;
    }


    /**
     * 存储重复的信息
     * @param devicesError error
     */
    public static void storeDuplicatedInfo(int deviceType, DevicesError devicesError) {
        if(devicesError == null || ((devicesError.getMac() == null || devicesError.getMac().size() == 0)
                && (devicesError.getSn() == null || devicesError.getSn().size() == 0))) {
            CommonUtils.addSysMap(String.format(SHARED_SN_ERROR_INFO, getDeviceKeyByType(deviceType)), "");
        } else {
            CommonUtils.addSysMap(String.format(SHARED_SN_ERROR_INFO, getDeviceKeyByType(deviceType)), new Gson().toJson(devicesError));
        }
    }

    public static void storeMainTainInfo(int deviceType, Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }
        String dir = getStorageDirForDevice(deviceType);
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }

        String fileName = dir + String.format(FILE_MAINTAIN_INFO_NAME, getDeviceKeyByType(deviceType));
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateJson();
        PetkitLog.d("store P3 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);

    }

    public static void storeCheckInfo(int deviceType, Device device) {
        if(device == null || !device.checkValid()) {
            return;
        }

        String dir = getStorageDirForDevice(deviceType);
        if(!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        String fileName = dir + String.format(FILE_CHECK_INFO_NAME, getDeviceKeyByType(deviceType));
        String content = FileUtils.readFileToString(new File(fileName));
        if(content != null && content.contains(device.getMac())) {
            return;
        }
        String info = device.generateCheckJson();
        PetkitLog.d("store P3 info: " + info);
        FileUtils.writeStringToFile(fileName, info + ",", true);
    }


    /**
     * 获取重复的错误信息
     * @return DevicesError
     */
    public static DevicesError getDevicesErrorMsg(int deviceType) {
        String msg = CommonUtils.getSysMap(String.format(SHARED_SN_ERROR_INFO, getDeviceKeyByType(deviceType)));

//        if (deviceType == Globals.T4_p) {
//            DevicesError mDevicesError = new DevicesError();
//            mDevicesError.setSn(new ArrayList<Device>());
//            mDevicesError.getSn().add(new Device("943cc651ca8c", "03211028L10000", ""));
//            return mDevicesError;
//        }
//
//        if (deviceType == Globals.P3C) {
//            DevicesError mDevicesError = new DevicesError();
//            mDevicesError.setMac(new ArrayList<Device>());
//            mDevicesError.getMac().add(new Device("a4c1382bcf52", "00210805K40001", ""));
//            return mDevicesError;
//        }

        if(CommonUtils.isEmpty(msg)) {
            return null;
        }

        return new Gson().fromJson(msg, DevicesError.class);
    }

    /**
     * 生成Sn，根据Tester的类型
     * @param tester 测试者信息
     * @return sn
     */
    public static String generateSNForTester(int deviceType, Tester tester) {
        if(tester == null || !tester.checkValid()) {
            throw  new RuntimeException("Tester is invalid!");
        }

        String day = CommonUtils.getDateStringByOffset(0).substring(2);
        String serializableNumber = getNextSnSerializableNumber(deviceType, day);
        if(serializableNumber == null) {
            return null;
        }

        switch (deviceType) {
            /**
             * 这些设备使用工站代码来生成SN
             */
            case Globals.W5:
            case Globals.W5C:
            case Globals.W5N:
//                return generateSN(tester.getCode() + day, getDeviceFlagByType(deviceType), tester.getStation(), serializableNumber);
            case Globals.K3:
            case Globals.P3C:
            case Globals.P3D:
            case Globals.T4:
            case Globals.T4_p:
            case Globals.W4X:
            case Globals.R2:
            default:
//                throw  new RuntimeException("generate SN is forbidden!");
                return generateSN("0" + tester.getStation() + day, getDeviceFlagByType(deviceType), serializableNumber);
//                return generateSN(CommonUtils.getDateStringByOffset(0), getDeviceFlagByType(deviceType), serializableNumber);
        }
    }

    public  static String generateSN(String day, String flag, String serializableNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(day)
                .append(flag)
                .append(serializableNumber);

        if(stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

        return stringBuilder.toString().toUpperCase();
    }

    public  static String generateSN(String day, String flag, String opStation, String serializableNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(day)
                .append(flag)
                .append(opStation)
                .append(serializableNumber);

        if(stringBuilder.toString().length() != 14) {
            throw  new RuntimeException("generate SN failed!");
        }

        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 获取新的SN的末尾四位序列号
     * @param day 日期
     * @return String
     */
    private static String getNextSnSerializableNumber(int deviceType, String day) {
        String sysDayKey = String.format(SHARED_SERIALIZABLE_DAY, getDeviceKeyByType(deviceType));
        String sysNumberKey = String.format(SHARED_SERIALIZABLE_NUMBER, getDeviceKeyByType(deviceType));

        String lastDay = CommonUtils.getSysMap(sysDayKey);
        int start = 0;
        if(lastDay.equals(day)) {
            start = CommonUtils.getSysIntMap(CommonUtils.getAppContext(), sysNumberKey, 0);
        }

        if(start > 9999) {
            return null;
        }

        CommonUtils.addSysMap(sysDayKey, day);
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(), sysNumberKey, start + 1);

        return String.format("%04d", start);
    }

    /**
     * 初始化SN的参数
     * @param sn sn
     */
    public static void initSnSerializableNumber(int deviceType, String sn) {
        if(CommonUtils.isEmpty(sn) || sn.length() != 14) {
            clearSnSerializableNumber(deviceType);
        } else {
            String day = CommonUtils.getDateStringByOffset(0).substring(2);
            String snDay = sn.substring(2, 8);
            if(day.equals(snDay)) {
                int number = Integer.valueOf(sn.substring(sn.length() - 4)) + 1;

                CommonUtils.addSysMap(String.format(SHARED_SERIALIZABLE_DAY, getDeviceKeyByType(deviceType)), day);
                CommonUtils.addSysIntMap(CommonUtils.getAppContext(),
                        String.format(SHARED_SERIALIZABLE_NUMBER, getDeviceKeyByType(deviceType)), number);
            } else {
                clearSnSerializableNumber(deviceType);
            }
        }
    }


    /**
     * 清除序列号相关参数
     */
    public static void clearSnSerializableNumber(int deviceType) {
        CommonUtils.addSysMap(String.format(SHARED_SERIALIZABLE_DAY, getDeviceKeyByType(deviceType)), "");
        CommonUtils.addSysIntMap(CommonUtils.getAppContext(),
                String.format(SHARED_SERIALIZABLE_NUMBER, getDeviceKeyByType(deviceType)), 0);

    }


    /**
     * 存储测试完成的设备信息
     * @param deviceType
     * @param device
     * @param ageingResult
     */
    public static void storeSucceedDeviceInfo(int deviceType, Device device, String ageingResult) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("storeSucceedDeviceInfo failed, " + (device == null ? "device is null !" : device.toString()));
        }

        LogcatStorageHelper.addLog("storeSucceedDeviceInfo info: " + device.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreDeviceInfoFilePath(deviceType), device.generateMainJson(ageingResult) + ",", true);
    }

    /**
     *
     * @param deviceType
     * @param device
     * @param ageingResult
     * @param withK3
     */
    public static void storeSucceedDeviceInfo(int deviceType, Device device, String ageingResult, int withK3) {
        if(device == null || !device.checkValid()) {
            throw  new RuntimeException("storeSucceedDeviceInfo failed, " + (device == null ? "device is null !" : device.toString()));
        }
//        if(deviceType != Globals.T4_p) {
//            throw  new RuntimeException("storeSucceedDeviceInfo failed, " + "device type must be device with K3");
//        }

        LogcatStorageHelper.addLog("storeSucceedDeviceInfo info: " + device.generateMainJson(ageingResult, withK3));
        FileUtils.writeStringToFile(getStoreDeviceInfoFilePath(deviceType), device.generateMainJson(ageingResult, withK3) + ",", true);
    }

    /**
     * 获取存储SN的文件，内部实现文件内容的条件限制，文件名自增
     * @return
     */
    private static String getStoreDeviceInfoFilePath(int deviceType) {
        String fileName = CommonUtils.getSysMap(String.format(SHARED_SN_FILE_NAME, getDeviceKeyByType(deviceType)));
        int fileSnNumber = CommonUtils.getSysIntMap(CommonUtils.getAppContext(),
                String.format(SHARED_SN_FILE_NUMBER, getDeviceKeyByType(deviceType)), 0);

        if (fileName != null &&             //文件不存在，或者文件不是今天产生的，都需要重新生成文件
                (!fileName.startsWith(getFileName()) || !(new File(getStorageDirForDevice(deviceType) + fileName).exists()))) {
            fileName = null;
        }

        if(fileSnNumber >= MAX_SN_NUMBER_SESSION || CommonUtils.isEmpty(fileName)) {
            String dir = getStorageDirForDevice(deviceType);
            if(!new File(dir).exists()) {
                new File(dir).mkdirs();
            }
            File outFile = new File(dir, getFileName() + ".txt");
            int i = 1;
            while (outFile.exists()) {
                outFile = new File(dir, getFileName() + "-" + (i++) + ".txt");
            }
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogcatStorageHelper.addLog("file create failed !");
                LogcatStorageHelper.addLog(e.toString());
                return "";
            }
            LogcatStorageHelper.addLog("file name: " + outFile.getName() + ", sn number: " + 1);
            CommonUtils.addSysMap(CommonUtils.getAppContext(), String.format(SHARED_SN_FILE_NAME, getDeviceKeyByType(deviceType)), outFile.getName());
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), String.format(SHARED_SN_FILE_NUMBER, getDeviceKeyByType(deviceType)), 1);
            return outFile.getAbsolutePath();
        } else {
            fileSnNumber++;
            LogcatStorageHelper.addLog("file name: " + fileName + ", sn number: " + fileSnNumber);
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), String.format(SHARED_SN_FILE_NUMBER, getDeviceKeyByType(deviceType)), fileSnNumber);
            return getStorageDirForDevice(deviceType) + fileName;
        }
    }

    /**
     * 校验MAC是否存在重复
     * @param mac mac
     * @return bool
     */
    public static boolean checkMacIsDuplicate(int deviceType, String mac) {
        String fileName = CommonUtils.getSysMap(String.format(SHARED_SN_FILE_NAME, getDeviceKeyByType(deviceType)));
        if(!CommonUtils.isEmpty(fileName)) {
            String content = FileUtils.readFileToString(new File(getStorageDirForDevice(deviceType) + fileName));
            return content != null && content.contains(mac);
        }

        return false;
    }

    public static void stopBle (Context context) {
        Intent intent = new Intent(BLEConsts.BROADCAST_ACTION);
        intent.putExtra(BLEConsts.EXTRA_ACTION, BLEConsts.ACTION_ABORT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static boolean checkSN (String sn, int deviceType) {
        if (sn == null || sn.length() < 14)
            return false;

        if (mDeviceConfigs.get(deviceType) != null) {
            return sn.contains(mDeviceConfigs.get(deviceType).getDeviceSNFlag());
        } else {
            switch (deviceType) {
                case Globals.W5C:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_W5C);
                case Globals.COZY:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_Z1S);
                case Globals.D3:
                case Globals.D3_1:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_D3);
                case Globals.D4:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_D4);
                case Globals.D4_1:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_D4_1);
                case Globals.FEEDER:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_D1);
                case Globals.FEEDER_MINI:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_D2);
                case Globals.K2:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_K2);
                case Globals.T3:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_T3);
                case Globals.T4:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_T4);
                case Globals.T4_p:
                    return sn.contains(Globals.DEVICE_TYPE_CODE_NEW_T4_P);
            }
            return true;
        }

    }

    /**
     * 检查本地SN是否重复
     *
     * @param deviceType
     * @param sn
     * @return
     */
    public static boolean checkSNIsDuplicate(int deviceType, String sn) {

        String filePath = null;
        if (mDeviceConfigs.get(deviceType) != null) {
            String fileName = CommonUtils.getSysMap(String.format(SHARED_SN_FILE_NAME, getDeviceKeyByType(deviceType)));

            if(!CommonUtils.isEmpty(fileName)) {
                filePath = getStorageDirForDevice(deviceType) + fileName;
            }
        } else {
            switch (deviceType) {
                case Globals.W5C:
                    filePath = W5Utils.getStoreDeviceInfoFilePath();
                    break;
                case Globals.D3:
                case Globals.D3_1:
                    filePath = D3Utils.getStoreDeviceInfoFilePath();
                    break;
                case Globals.D4:
                case Globals.D4_1:
                    filePath = D4Utils.getStoreDeviceInfoFilePath();
                    break;
                case Globals.FEEDER_MINI:
                    filePath = FeederMiniUtils.getStoreFeederInfoFilePath();
                    break;
                case Globals.K2:
                    filePath = K2Utils.getStoreDeviceInfoFilePath();
                    break;
                case Globals.T3:
                    filePath = T3Utils.getStoreDeviceInfoFilePath();
                    break;
            }
        }

        if (!TextUtils.isEmpty(filePath)) {
            String content = FileUtils.readFileToString(new File(filePath));
            return content != null && content.contains(sn);
        }

        return false;
    }


    /**
     * 计算饮水机系列的水循环次数和电量
     *
     * @param valueType 能源种类：1 水循环次数；2 电量
     * @param deviceType Wx系列类型：W4X/W5C/W5/CTW2
     * @param time 运行时间：单位秒
     */
    private float canculateWxEnergyForType(int valueType, String deviceType, int time) {
        if (valueType == 1) {
            float flow = "W5C".equals(deviceType) ? 1.3f : 1.5f;
            float capacity = "W5C".equals(deviceType) ? 1f : "W4X".equals(deviceType) ? 1.8f : 2f;
            return flow * time / 60f / capacity;
        } else {
            float watt = "W5C".equals(deviceType) ? 0.182f : 0.75f;
            return watt * time / 3600f / 1000f;
        }
    }

    /**
     * 计算饮水机滤芯剩余使用天数
     *
     * @param leftPercent 当前剩余比例
     * @param workDuration 工作时间
     * @param sleepDuration 休眠时间
     * @return
     */
    public static int canculateWxFilterLeftDays(float leftPercent, int workDuration, int sleepDuration) {
        return (int) Math.ceil(30f * leftPercent / (workDuration / (workDuration + sleepDuration)));
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

}
