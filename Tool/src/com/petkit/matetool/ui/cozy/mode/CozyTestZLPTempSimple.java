package com.petkit.matetool.ui.cozy.mode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 制冷片测试的临时数据
 *
 * Created by Jone on 17/12/6.
 */
public class CozyTestZLPTempSimple {

    //测试模式，1：制冷；2：制热
    private int mMode;

    private boolean isTesting;

    private CozyTestDetail mCurTestDetail;

    private HashMap<Integer, CozyTestDetail> mTestDetailHashMap;

    public CozyTestZLPTempSimple() {
        mTestDetailHashMap = new HashMap<>();
    }

    class CozyTestDetail {
        //测试步骤
        private int mStep;
        //温度缓存
        private ArrayList<Integer> mTempList;
        //测试类型，先进行温度对比，再进行电压校验
        private int mTestType;
        //测试结果
        private boolean result;

        public CozyTestDetail(int type) {
            mTempList = new ArrayList<>();
            mTestType = type;
        }

        void addTemp(int temp) {
            if (mTestType != 0) {
                return;
            }

            if (mStep >= 0 && mStep < 4) {
                mTempList.add(temp);
                mStep++;
            }

            if (mStep == 4) {
                checkTempResult();
            }
        }

        public void addVolage(int mode, int vol1, int vol2) {
            if (mTestType != 1) {
                return;
            }

            if (mode == 1) {
                result = vol1 > 5000 && vol2 < 1000;
            } else if (mode == 2) {
                result = vol2 > 5000 && vol1 < 1000;
            }
        }

        private void checkTempResult() {
            if (mMode == 1) {
                result = mTempList.get(0) > mTempList.get(1) &&  mTempList.get(1) > mTempList.get(2)
                        &&  mTempList.get(2) > mTempList.get(3);
            } else if (mMode == 2) {
                result = mTempList.get(0) < mTempList.get(1) &&  mTempList.get(1) < mTempList.get(2)
                        &&  mTempList.get(2) < mTempList.get(3);
            }
        }

        public void reset() {
            mStep = 0;
            result = false;
            mTempList.clear();
        }
    }

    public boolean startCoolTest() {
        if (isTesting) {
            return false;
        }

        mMode = 1;
        start();
        return true;
    }

    public boolean startHotTest() {
        if (isTesting) {
            return false;
        }

        mMode = 2;
        start();
        return true;
    }

    private void start() {
        if (mTestDetailHashMap.containsKey(mMode)) {
            mCurTestDetail = mTestDetailHashMap.get(mMode);
            mCurTestDetail.reset();
        } else {
            if (mTestDetailHashMap.size() == 0) {
                mCurTestDetail = new CozyTestDetail(0);
                isTesting = true;
            } else {
                mCurTestDetail = new CozyTestDetail(1);
                isTesting = true;
            }
            mTestDetailHashMap.put(mMode, mCurTestDetail);
        }
    }

    public void addTemp(int temp) {
        if (mCurTestDetail == null) {
            return;
        }

        mCurTestDetail.addTemp(temp);
        isTesting = !(mCurTestDetail.mTestType == 0 && mCurTestDetail.mStep == 4);
    }

    public void addVol(int mode, int vol1, int vol2) {
        if (mCurTestDetail == null) {
            return;
        }

        if (mode != mMode) {
            return;
        }

        mCurTestDetail.addVolage(mMode, vol1, vol2);
        isTesting = !(mCurTestDetail.mTestType == 1);
    }

    public boolean isTesting() {
        return isTesting;
    }

    public boolean isSuccess() {
        if (mCurTestDetail == null) {
            return false;
        }
        return mCurTestDetail.result;
    }

    public boolean isTotalComplete() {
        if (!isTesting && mTestDetailHashMap != null && mTestDetailHashMap.size() == 2) {
            CozyTestDetail detail1 = mTestDetailHashMap.get(1);
            CozyTestDetail detail2 = mTestDetailHashMap.get(2);
            return detail1.result && detail2.result;
        }

        return false;
    }
}
