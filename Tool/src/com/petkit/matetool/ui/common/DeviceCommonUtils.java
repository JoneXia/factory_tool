package com.petkit.matetool.ui.common;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.Device;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.AQR.AQRStartActivity;
import com.petkit.matetool.ui.AQR.AQRTestMainActivity;
import com.petkit.matetool.ui.K3.K3StartActivity;
import com.petkit.matetool.ui.K3.K3TestMainActivity;
import com.petkit.matetool.ui.P3.P3StartActivity;
import com.petkit.matetool.ui.P3.P3TestMainActivity;
import com.petkit.matetool.ui.t4.T4StartActivity;
import com.petkit.matetool.ui.t4.T4TestMainActivity;
import com.petkit.matetool.utils.Globals;

import java.io.File;
import java.io.IOException;

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

    private static final String SHARED_SERIALIZABLE_DAY     = "%s_SerializableDay";
    private static final String SHARED_SERIALIZABLE_NUMBER     = "%s_SerializableNumber";
    private static final String SHARED_SN_FILE_NAME     = "%s_SnFileName";
    private static final String SHARED_SN_FILE_NUMBER     = "%s_SnFileNumber";
    private static final String SHARED_W5_ERROR_INFO     = "%s_ERRORS";

    public static final String FILE_MAINTAIN_INFO_NAME     = "%s_maintain_info.txt";
    public static final String FILE_CHECK_INFO_NAME     = "%s_check_info.txt";


    //maintain/check/product
    public static final String URL_TYPE_MAINTAIN   = "maintain";
    public static final String URL_TYPE_CHECK      = "check";
    public static final String URL_TYPE_PRODUCT    = "product";
    public static final String URL_TYPE_LAST_SN    = "lastSNd";

    private static final int MAX_SN_NUMBER_SESSION = 200;


    /**
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceKeyByType(int deviceType) {
        String key;
        switch (deviceType) {
            case Globals.P3C:
            case Globals.P3D:
                key = "P3";
                break;
            case Globals.T4:
            case Globals.T4_p:
                key = "T4";
                break;
            case Globals.K3:
                key = "K3";
                break;
            case Globals.AQR:
                key = "AQR";
                break;
            default:
                throw  new RuntimeException("getDeviceKeyByType deviceType not support!");
        }

        return key;
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static Class getStartActivityByType(int deviceType) {
        switch (deviceType) {
            case Globals.P3C:
            case Globals.P3D:
                return P3StartActivity.class;
            case Globals.T4:
            case Globals.T4_p:
                return T4StartActivity.class;
            case Globals.K3:
                return K3StartActivity.class;
            case Globals.AQR:
                return AQRStartActivity.class;
            default:
                throw  new RuntimeException("getStartActivity deviceType not support!");
        }
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static Class getMainActivityByType(int deviceType) {
        switch (deviceType) {
            case Globals.P3C:
            case Globals.P3D:
                return P3TestMainActivity.class;
            case Globals.T4:
            case Globals.T4_p:
                return T4TestMainActivity.class;
            case Globals.K3:
                return K3TestMainActivity.class;
            case Globals.AQR:
                return AQRTestMainActivity.class;
            default:
                throw  new RuntimeException("getStartActivity deviceType not support!");
        }
    }

    /**
     *
     *
     * @param deviceType
     * @return
     */
    public static String getDeviceFlagByType(int deviceType) {
        switch (deviceType) {
            case Globals.P3C:
                return Globals.DEVICE_TYPE_CODE_P3C;
            case Globals.P3D:
                return Globals.DEVICE_TYPE_CODE_P3D;
            case Globals.T4:
            case Globals.T4_p:
                return Globals.DEVICE_TYPE_CODE_T4;
            case Globals.K3:
                return Globals.DEVICE_TYPE_CODE_K3;
            case Globals.AQR:
                return Globals.DEVICE_TYPE_CODE_AQR;
            default:
                throw  new RuntimeException("getDeviceFlagByType deviceType not support!");
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
                            && !item.contains(FILE_MAINTAIN_INFO_NAME)
                            && !item.contains(FILE_CHECK_INFO_NAME)) {
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
            CommonUtils.addSysMap(SHARED_W5_ERROR_INFO, "");
        } else {
            CommonUtils.addSysMap(SHARED_W5_ERROR_INFO, new Gson().toJson(devicesError));
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

        String fileName = dir + FILE_MAINTAIN_INFO_NAME;
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
        String fileName = dir + FILE_CHECK_INFO_NAME;
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
        String msg = CommonUtils.getSysMap(SHARED_W5_ERROR_INFO);
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
            throw  new RuntimeException("store W5 failed, " + (device == null ? "W5 is null !" : device.toString()));
        }

        PetkitLog.d("store W5 info: " + device.generateMainJson(ageingResult));
        FileUtils.writeStringToFile(getStoreDeviceInfoFilePath(deviceType), device.generateMainJson(ageingResult) + ",", true);
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
                outFile = new File(dir, getFileName() + "-" + (i++) + ".log");
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
            CommonUtils.addSysMap(CommonUtils.getSysMap(String.format(SHARED_SN_FILE_NAME, getDeviceKeyByType(deviceType))), outFile.getName());
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), String.format(SHARED_SN_FILE_NUMBER, getDeviceKeyByType(deviceType)), 1);
            return outFile.getAbsolutePath();
        } else {
            fileSnNumber++;
            LogcatStorageHelper.addLog("file name: " + fileName + ", sn number: " + fileSnNumber);
            CommonUtils.addSysIntMap(CommonUtils.getAppContext(), String.format(SHARED_SN_FILE_NUMBER, getDeviceKeyByType(deviceType)), fileSnNumber);
            return getStorageDirForDevice(deviceType) + fileName;
        }
    }

}
