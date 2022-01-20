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
import com.petkit.matetool.ui.K3.K3TestMainActivity;
import com.petkit.matetool.ui.P3.P3TestMainActivity;
import com.petkit.matetool.ui.R2.R2TestMainActivity;
import com.petkit.matetool.ui.W5New.W5NTestMainActivity;
import com.petkit.matetool.ui.common.BLEErrorListActivity;
import com.petkit.matetool.ui.common.BLEStartActivity;
import com.petkit.matetool.ui.common.WifiErrorListActivity;
import com.petkit.matetool.ui.common.WifiStartActivity;
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
                Globals.DEVICE_TYPE_CODE_P3C, P3TestMainActivity.class));
        mDeviceConfigs.put(Globals.P3D, new DeviceConfigInfo(true, "P3", "P3D", new String[]{"Petkit_P3D", "Petkit_P3C"},
                Globals.DEVICE_TYPE_CODE_P3D, P3TestMainActivity.class));
        mDeviceConfigs.put(Globals.T4, new DeviceConfigInfo(false, "T4", "T4", null,
                Globals.DEVICE_TYPE_CODE_T4, T4TestMainActivity.class));
        mDeviceConfigs.put(Globals.T4_p, new DeviceConfigInfo(false, "T4", "T4P", null,
                Globals.DEVICE_TYPE_CODE_T4, T4TestMainActivity.class));
        mDeviceConfigs.put(Globals.K3, new DeviceConfigInfo(true, "K3", "K3", new String[]{"Petkit_K3"},
                Globals.DEVICE_TYPE_CODE_K3, K3TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQR, new DeviceConfigInfo(true, "AQR", "AQR", new String[]{"Petkit_AQR"},
                Globals.DEVICE_TYPE_CODE_AQR, AQRTestMainActivity.class));
        mDeviceConfigs.put(Globals.AQ1S, new DeviceConfigInfo(true, "AQ", "AQ1S", new String[]{"Petkit_AQ", "Petkit_AQ1S"},
                Globals.DEVICE_TYPE_CODE_AQ1S, AQ1STestMainActivity.class));
        mDeviceConfigs.put(Globals.R2, new DeviceConfigInfo(true, "R2", "R2", new String[]{"Petkit_R2"},
                Globals.DEVICE_TYPE_CODE_R2, R2TestMainActivity.class));
        mDeviceConfigs.put(Globals.W5N, new DeviceConfigInfo(true, "W5", "W5N", new String[]{"Petkit_W5N", "Petkit_W4X"},
                Globals.DEVICE_TYPE_CODE_W5N, W5NTestMainActivity.class));
        mDeviceConfigs.put(Globals.W4X, new DeviceConfigInfo(true, "W5", "W4X", new String[]{"Petkit_W5N", "Petkit_W4X"},
                Globals.DEVICE_TYPE_CODE_W4X, W5NTestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_500, new DeviceConfigInfo(false, "AQH1", "AQH1_500", null,
                Globals.DEVICE_TYPE_CODE_AQH1_500, AQH1TestMainActivity.class));
        mDeviceConfigs.put(Globals.AQH1_1000, new DeviceConfigInfo(false, "AQH1", "AQH1_1000", null,
                Globals.DEVICE_TYPE_CODE_AQH1_1000, AQH1TestMainActivity.class));
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

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tester.getCode())
                .append(day)
                .append(getDeviceFlagByType(deviceType))
                .append(tester.getStation())
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

        PetkitLog.d("storeSucceedDeviceInfo info: " + device.generateMainJson(ageingResult));
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

        PetkitLog.d("storeSucceedDeviceInfo info: " + device.generateMainJson(ageingResult, withK3));
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
        if (sn == null || sn.length() != 14)
            return false;

        if (mDeviceConfigs.get(deviceType) != null) {
            return mDeviceConfigs.get(deviceType).getDeviceSNFlag().equalsIgnoreCase(sn.substring(8,9));
        } else {
            return true;
        }

    }

}
