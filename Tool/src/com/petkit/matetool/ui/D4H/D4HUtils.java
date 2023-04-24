package com.petkit.matetool.ui.D4H;

import com.petkit.matetool.model.Device;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

import static com.petkit.matetool.utils.Globals.PERMISSION_ERASE;
import static com.petkit.matetool.utils.Globals.TYPE_AFTERMARKET;
import static com.petkit.matetool.utils.Globals.TYPE_DUPLICATE_MAC;
import static com.petkit.matetool.utils.Globals.TYPE_DUPLICATE_SN;
import static com.petkit.matetool.utils.Globals.TYPE_MAINTAIN;
import static com.petkit.matetool.utils.Globals.TYPE_TEST;
import static com.petkit.matetool.utils.Globals.TYPE_TEST_BOARD;
import static com.petkit.matetool.utils.Globals.TYPE_TEST_PARTIALLY;

/**
 *
 * Created by Jone on 17/3/21.
 */
public class D4HUtils {


    public static ArrayList<Device> mTempDevices = new ArrayList<>();

    public enum D4HTestModes {
        TEST_MODE_TIME, //时钟
        TEST_MODE_DC,   //电压
        TEST_MODE_BT,   //蓝牙
        TEST_MODE_LED,  //指示灯和蜂鸣器
        TEST_MODE_KEY,  //按键
        TEST_MODE_IR,   //红外
        TEST_MODE_MOTOR,    //电机
        TEST_MODE_BAT, //电池
        TEST_MODE_COM, //串口
        TEST_MODE_VIDEO,   //视频
        TEST_MODE_IR_cut,   //滤片
        TEST_MODE_IR_light,   //红外补光
        TEST_MODE_SPEAK,   //speak
        TEST_MODE_MIC,   //mic
        TEST_MODE_MAC,  //MAC重复
        TEST_MODE_SN,   //写SN
        TEST_MODE_RESET_SN, //重置SN
        TEST_MODE_RESET_ID, //清除ID
        TEST_MODE_AGEINGRESULT, //老化数据
        TEST_MODE_AUTO,
        TEST_MODE_PRINT     //打印标签
    }

    /**
     *
     * @return ArrayList
     */
    public static ArrayList<D4HTestUnit> generateD4AutoTestUnits() {
        ArrayList<D4HTestUnit> results = new ArrayList<>();

        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_DC, "电压测试", 0, 1, false));
        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_COM, "串口测试", 8, 1, false));
        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_TIME, "时钟测试", 7, 1, false));
        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_BT, "蓝牙测试", 6, 1, false));

        return results;
    }

    /**
     * 获取不同的测试模式对应的测试项
     * @param type 测试类型
     * @return 测试项
     */
    public static ArrayList<D4HTestUnit> generateTestUnitsForType(int type) {
        ArrayList<D4HTestUnit> results = new ArrayList<>();

        if(type == TYPE_DUPLICATE_MAC) {
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_MAC, "设置重复", 99, 1, false));
        } else if(type == TYPE_DUPLICATE_SN){
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_RESET_SN, "重写SN", 98, 2, false));
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_PRINT, "打印标签", -1, 1, false));
        } else {

            if (type != TYPE_TEST_BOARD) {
                if (type == TYPE_MAINTAIN || type == TYPE_AFTERMARKET) {
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_DC, "电压测试", 0, 1, false));
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_COM, "串口测试", 8, 1, false));
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_TIME, "时钟测试", 7, 1, false));
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_BT, "蓝牙测试", 6, 1, false));
                } else {
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_AUTO, "自动项测试", 6, 1, false));
                }
                if (type != TYPE_TEST_PARTIALLY) {
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_AGEINGRESULT, "老化结果", 97, 1, false));
                }

                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_LED, "指示灯、蜂鸣器测试", 1, 1, false));
                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_KEY, "按键测试", 2, 1, false));
                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_BAT, "电池", 5, 1, false));
                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_IR, "红外测试", 3, 1, false));
                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_MOTOR, "电机测试", 4, 1, false));
            } else {
//                results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_COM, "串口测试", 8, 1, false));
            }

            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_VIDEO, "视频测试", 9, 1, true));
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_IR_cut, "IRcut测试", 10, 1, true));
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_IR_light, "红外补光测试", 11, 1, true));
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_SPEAK, "Speak测试", 12, 1, true));
            results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_MIC, "Mic测试", 13, 1, true));

            if (type != TYPE_TEST_BOARD) {
                if (type == Globals.TYPE_TEST || type == Globals.TYPE_AFTERMARKET) {
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_SN, "写入SN", 98, 2, false));
                }

                if (type == TYPE_MAINTAIN || type == TYPE_AFTERMARKET) {        //擦除ID选项先关闭，暂不开放
                    results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_PRINT, "打印标签", -1, type == TYPE_TEST ? 2 : 1, false));
                    if (PERMISSION_ERASE) {
                        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_RESET_SN, "重写SN", 97, 1, false));
                        results.add(new D4HTestUnit(D4HTestModes.TEST_MODE_RESET_ID, "擦除ID", 98, 1, false));
                    }
                }
            }
        }
        return results;
    }

}
