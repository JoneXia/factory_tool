package com.petkit.matetool.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.utils.K2Utils;
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

    private static HashMap<Integer, Tester> mTesterTempList = new HashMap<>();

    public static void initTesterTempList() {
        getCurrentTesterForType(Globals.FEEDER);
        getCurrentTesterForType(Globals.COZY);
        getCurrentTesterForType(Globals.FEEDER_MINI);
        getCurrentTesterForType(Globals.T3);
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
            }
            if(!TextUtils.isEmpty(testerString)) {
                tester = new Gson().fromJson(testerString, Tester.class);
                mTesterTempList.put(type, tester);
            }
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
                }
                iterator.remove();
            }
        }
    }


}
