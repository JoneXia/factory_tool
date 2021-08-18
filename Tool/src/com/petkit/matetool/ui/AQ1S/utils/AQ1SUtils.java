package com.petkit.matetool.ui.AQ1S.utils;

import com.petkit.matetool.ui.AQ1S.mode.AQ1STestUnit;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class AQ1SUtils {


    public enum AQ1STestModes {
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AUTO,
        TEST_MODE_MAC,
        TEST_MODE_PRINT     //打印标签
    }


    /**
     *
     TEST_MODE_DC,   //电压
     TEST_MODE_LED,
     TEST_MODE_PUMP,
     */
    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<AQ1STestUnit> generateAQ1STestUnitsForType(int type) {
        ArrayList<AQ1STestUnit> results = new ArrayList<>();

        if(type == Globals.TYPE_DUPLICATE_MAC) {
            results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_MAC, "MAC重复", 97, 1));
        } else if(type == Globals.TYPE_DUPLICATE_SN){
            results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_SN, "写入SN", 98, 2));
            results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_PRINT, "打印标签", -1, 1));
        } else {

            if (type == Globals.TYPE_TEST) {
                results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_SN, "写入SN", 98, 2));
            }
            if (type != Globals.TYPE_TEST_PARTIALLY && type != Globals.TYPE_CHECK) {
                results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_PRINT, "打印标签", -1, type == Globals.TYPE_TEST ? 2 : 1));
            }

            if (type == Globals.TYPE_MAINTAIN) {        //擦除ID选项先关闭，暂不开放
                if (PERMISSION_ERASE) {
                    results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1));
                    results.add(new AQ1STestUnit(AQ1STestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1));
                }
            }
        }
        return results;
    }


}
