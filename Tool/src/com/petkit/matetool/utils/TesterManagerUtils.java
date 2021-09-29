package com.petkit.matetool.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D3.utils.D3Utils;
import com.petkit.matetool.ui.D4.utils.D4Utils;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.cozy.utils.CozyUtils;
import com.petkit.matetool.ui.feeder.utils.FeederUtils;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.ui.t3.utils.T3Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jone on 2018/8/9.
 */

public class TesterManagerUtils {

    public static final String SHARED_TESTER = "SHARED_%s_TESTER";

    private static HashMap<Integer, Tester> mTesterTempList = new HashMap<>();

    public static void initTesterTempList() {
        getCurrentTesterForType(Globals.FEEDER);
        getCurrentTesterForType(Globals.COZY);
        getCurrentTesterForType(Globals.FEEDER_MINI);
        getCurrentTesterForType(Globals.T3);
        getCurrentTesterForType(Globals.K2);
        getCurrentTesterForType(Globals.D3);
        getCurrentTesterForType(Globals.D4);
//        getCurrentTesterForType(Globals.W5);

        for (int i = Globals.W5; i < Globals.MAX; i++) {
            getCurrentTesterForType(i);
        }
    }


    public static Tester getCurrentTesterForType(int type) {
        Tester tester = mTesterTempList.get(type);

        if (tester == null) {
            String testerString = null;
            switch (type) {
                case Globals.FEEDER:
                    testerString = CommonUtils.getSysMap(FeederUtils.SHARED_FEEDER_TESTER);
                    break;
                case Globals.COZY:
                    testerString = CommonUtils.getSysMap(CozyUtils.SHARED_COZY_TESTER);
                    break;
                case Globals.FEEDER_MINI:
                    testerString = CommonUtils.getSysMap(FeederMiniUtils.SHARED_FEEDER_MINI_TESTER);
                    break;
                case Globals.T3:
                    testerString = CommonUtils.getSysMap(T3Utils.SHARED_T3_TESTER);
                    break;
                case Globals.K2:
                    testerString = CommonUtils.getSysMap(K2Utils.SHARED_K2_TESTER);
                    break;
                case Globals.D3:
                    testerString = CommonUtils.getSysMap(D3Utils.SHARED_D3_TESTER);
                    break;
                case Globals.D4:
                    testerString = CommonUtils.getSysMap(D4Utils.SHARED_D4_TESTER);
                    break;
                default:
                    if (type >= Globals.W5) {       //旧设备不支持这个方式来获取
                        testerString = CommonUtils.getSysMap(getShareTesterKey(type));
                    }
                    break;
            }
            if(!TextUtils.isEmpty(testerString)) {
                tester = new Gson().fromJson(testerString, Tester.class);
                mTesterTempList.put(type, tester);
            }

//            if (type == Globals.W4X || type == Globals.W5N) {
//                tester = new Tester();
//                tester.setCode("00");
//                tester.setName("写死的账号");
//                tester.setStation("1");
//            }
        }

        return tester;
    }


    public static void addTesterForType(int type, Tester tester) {
        String testerString = new Gson().toJson(tester);

        switch (type) {
            case Globals.FEEDER:
                CommonUtils.addSysMap(FeederUtils.SHARED_FEEDER_TESTER, testerString);
                break;
            case Globals.COZY:
                CommonUtils.addSysMap(CozyUtils.SHARED_COZY_TESTER, testerString);
                break;
            case Globals.FEEDER_MINI:
                CommonUtils.addSysMap(FeederMiniUtils.SHARED_FEEDER_MINI_TESTER, testerString);
                break;
            case Globals.T3:
                CommonUtils.addSysMap(T3Utils.SHARED_T3_TESTER, testerString);
                break;
            case Globals.K2:
                CommonUtils.addSysMap(K2Utils.SHARED_K2_TESTER, testerString);
                break;
            case Globals.D3:
                CommonUtils.addSysMap(D3Utils.SHARED_D3_TESTER, testerString);
                break;
            case Globals.D4:
                CommonUtils.addSysMap(D4Utils.SHARED_D4_TESTER, testerString);
                break;
//            case Globals.W5:
//                CommonUtils.addSysMap(W5Utils.SHARED_W5_TESTER, testerString);
//                break;
            default:
                CommonUtils.addSysMap(getShareTesterKey(type), testerString);
                break;
        }

        for(Integer key : mTesterTempList.keySet()) {
            if (mTesterTempList.get(key).getName().equals(tester.getName())) {
                mTesterTempList.put(key, tester);
            }
        }

        mTesterTempList.put(type, tester);
    }

    public static void removeTesterForType(int type) {
        Tester tester = mTesterTempList.get(type);

        if (tester == null) {
            return;
        }

        Set<Map.Entry<Integer, Tester>> set= mTesterTempList.entrySet();
        Iterator<Map.Entry<Integer, Tester>> iterator= set.iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, Tester> entry = iterator.next();

            Tester tempTester = entry.getValue();
            if (tempTester.getName().equals(tester.getName())) {
                switch (entry.getKey()) {
                    case Globals.FEEDER:
                        CommonUtils.addSysMap(FeederUtils.SHARED_FEEDER_TESTER, "");
                        break;
                    case Globals.COZY:
                        CommonUtils.addSysMap(CozyUtils.SHARED_COZY_TESTER, "");
                        break;
                    case Globals.FEEDER_MINI:
                        CommonUtils.addSysMap(FeederMiniUtils.SHARED_FEEDER_MINI_TESTER, "");
                        break;
                    case Globals.T3:
                        CommonUtils.addSysMap(T3Utils.SHARED_T3_TESTER, "");
                        break;
                    case Globals.K2:
                        CommonUtils.addSysMap(K2Utils.SHARED_K2_TESTER, "");
                        break;
                    case Globals.D3:
                        CommonUtils.addSysMap(D3Utils.SHARED_D3_TESTER, "");
                        break;
                    case Globals.D4:
                        CommonUtils.addSysMap(D4Utils.SHARED_D4_TESTER, "");
                        break;
//                    case Globals.W5:
//                        CommonUtils.addSysMap(W5Utils.SHARED_W5_TESTER, "");
//                        break;
                    default:
                        CommonUtils.addSysMap(getShareTesterKey(entry.getKey()), "");
                        break;
                }
                iterator.remove();
            }
        }
    }

    private static String getShareTesterKey(int deviceType) {
        return String.format(SHARED_TESTER, DeviceCommonUtils.getDeviceTesterKeyByType(deviceType));
    }


    /**
     * 判断测试账号是否已在本机登录，如已登录则限制不能重复登录
     * @param username
     * @return
     */
    public static boolean checkTesterIsLogined(String username) {
        for(Integer key : mTesterTempList.keySet()) {
            if (mTesterTempList.get(key).getName().equals(username)) {
                return true;
            }
        }
        return false;
    }


}
