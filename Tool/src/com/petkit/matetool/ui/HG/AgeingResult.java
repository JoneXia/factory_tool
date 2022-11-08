package com.petkit.matetool.ui.HG;

import com.petkit.android.utils.ByteUtil;

public class AgeingResult {

    private float targetTemp;
    private int ageingDuration;
    private float ntc1AverageTemp;
    private float ntc1CurTemp;
    private float ntc2AverageTemp;
    private float ntc2CurTemp;
    private float ntcPtcAverageTemp;
    private float ntcPtcCurTemp;
    private float ahtAverageTemp;
    private float ahtCurTemp;
    private int ahtAverageHumi;
    private int aht1CurHumi;
    private int powerAverageCurr;
    private int powerCurCurr;
    private int powerAverageSpeed;
    private int powerCurSpeed;
    private int errCode;
    private int errDetail;
    private int ageResult;  //老化结果：0-未老化； 1-老化时间小于15分钟； 2-老化失败； 3-老化成功；


    /**
     *
     * D[0-1]：目标温度，单位：0.1℃；
     * D[2-3]：老化时间，单位：S；
     * D[4-5]：NTC1平均温度，单位：0.1℃；
     * D[6-7]：NTC1实时温度；
     * D[8-9]：NTC2平均温度；
     * D[10-11]：NTC2实时温度；
     * D[12-13]：NTC-PTC平均温度；
     * D[14-15]：NTC-PTC实时温度；
     * D[16-17]：AHT平均温度；
     * D[18-19]：AHT实时温度；
     * D[20]：AHT平均湿度，单位：%rh；
     * D[21]：AHT实时湿度，单位：%rh；
     * D[22-23]：电机平均电流，单位：ma；
     * D[24-25]：电机实时电流，单位：ma；
     * D[26-27]：电机平均转速，单位：转/分
     * D[28-29]：电机实时转速，单位：转/分
     * D[30-31]故障码；(见运行信息)
     * D[32-33] 当前故障具体数据；
     * D[34] 老化结果；
     *
     */

    public AgeingResult(byte[] ageingData) {

        if (ageingData == null || ageingData.length < 37) {
            return;
        }

        targetTemp = ByteUtil.byteToInt(ageingData, 0, 2) / 10f;
        ageingDuration = ByteUtil.byteToInt(ageingData, 2, 2);
        ntc1AverageTemp = ByteUtil.byteToInt(ageingData, 4, 2) / 10f;
        ntc1CurTemp = ByteUtil.byteToInt(ageingData, 6, 2) / 10f;
        ntc2AverageTemp = ByteUtil.byteToInt(ageingData, 8, 2) / 10f;
        ntc2CurTemp = ByteUtil.byteToInt(ageingData, 10, 2) / 10f;
        ntcPtcAverageTemp = ByteUtil.byteToInt(ageingData, 12, 2) / 10f;
        ntcPtcCurTemp = ByteUtil.byteToInt(ageingData, 14, 2) / 10f;
        ahtAverageTemp = ByteUtil.byteToInt(ageingData, 16, 2) / 10f;
        ahtCurTemp = ByteUtil.byteToInt(ageingData, 18, 2) / 10f;
        ahtAverageHumi = ByteUtil.byteToInt(ageingData, 20, 2);
        aht1CurHumi = ByteUtil.byteToInt(ageingData, 22, 2);
        powerAverageCurr = ByteUtil.byteToInt(ageingData, 24, 2);
        powerCurCurr = ByteUtil.byteToInt(ageingData, 26, 2);
        powerAverageSpeed = ByteUtil.byteToInt(ageingData, 28, 2);
        powerCurSpeed = ByteUtil.byteToInt(ageingData, 30, 2);
        errCode = ByteUtil.byteToInt(ageingData, 32, 2);
        errDetail = ByteUtil.byteToInt(ageingData, 34, 2);
        ageResult = ByteUtil.byteToInt(ageingData, 36, 1);

    }

    @Override
    public String toString() {
        return "AgeingResult{" +
                "targetTemp=" + targetTemp +
                ", ageingDuration=" + ageingDuration +
                ", ntc1AverageTemp=" + ntc1AverageTemp +
                ", ntc1CurTemp=" + ntc1CurTemp +
                ", ntc2AverageTemp=" + ntc2AverageTemp +
                ", ntc2CurTemp=" + ntc2CurTemp +
                ", ntcPtcAverageTemp=" + ntcPtcAverageTemp +
                ", ntcPtcCurTemp=" + ntcPtcCurTemp +
                ", ahtAverageTemp=" + ahtAverageTemp +
                ", ahtCurTemp=" + ahtCurTemp +
                ", ahtAverageHumi=" + ahtAverageHumi +
                ", aht1CurHumi=" + aht1CurHumi +
                ", powerAverageCurr=" + powerAverageCurr +
                ", powerCurCurr=" + powerCurCurr +
                ", powerAverageSpeed=" + powerAverageSpeed +
                ", powerCurSpeed=" + powerCurSpeed +
                ", errCode=" + errCode +
                ", errDetail=" + errDetail +
                ", ageResult=" + ageResult +
                '}';
    }


    public String toDisplayString() {
        return "老化数据: " +
                "\n目标温度=" + targetTemp +
                "\n老化时间=" + convertAgeingDuration(ageingDuration) +
                "\nNTC1平均温度=" + ntc1AverageTemp +
                "\nNTC1当前温度=" + ntc1CurTemp +
                "\nNTC2平均温度=" + ntc2AverageTemp +
                "\nNTC2当前温度=" + ntc2CurTemp +
                "\nNTC-PTC平均温度=" + ntcPtcAverageTemp +
                "\nNTC-PTC当前温度=" + ntcPtcCurTemp +
                "\nAHT平均温度=" + ahtAverageTemp +
                "\nAHT当前温度=" + ahtCurTemp +
                "\nAHT平均湿度=" + ahtAverageHumi +
                "\nAHT当前湿度=" + aht1CurHumi +
                "\n电机平均电流=" + powerAverageCurr +
                "\n电机当前电流=" + powerCurCurr +
                "\n电机平均速度=" + powerAverageSpeed +
                "\n电机当前速度=" + powerCurSpeed +
                "\n故障码=" + errCode +
                "\n故障明细=" + errDetail +
                "\n老化结果=" + convertAgeingResult();
    }

    private String convertAgeingDuration(int duration) {
        StringBuilder stringBuilder = new StringBuilder();
        if (duration / 3600 > 0) {
            stringBuilder.append(duration / 3600 + "小时");
        }

        if ((duration % 3600) / 60 > 0) {
            stringBuilder.append((duration % 3600) / 60 + "分钟");
        }

        if ((duration % 60) > 0) {
            stringBuilder.append((duration % 60) + "秒");
        }

        return stringBuilder.toString();
    }


    /**
     * 老化结果：0-未老化； 1-老化时间小于15分钟； 2-老化失败； 3-老化成功；
     * @return
     */
    private String convertAgeingResult() {
        switch (ageResult) {
            case 0:
                return "未老化";
            case 1:
                return "老化时间小于15分钟";
            case 2:
                return "老化失败";
            case 3:
                return "老化成功";
        }

        return "未知";
    }

}
