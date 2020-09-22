package com.petkit.matetool.ui.aq;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;

import java.io.File;
import java.io.IOException;

public class AQCalculateActivity extends BaseActivity {

    private static final double PI = 3.1415926;

    int year, month, date;
    double glat, glong;

    private TextView mResultView;
    private EditText glatEdit, glongEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_aq_calculate);
    }

    @Override
    protected void setupViews() {

        mResultView = (TextView) findViewById(R.id.result);

        glatEdit = (EditText) findViewById(R.id.latitude);
        glongEdit = (EditText) findViewById(R.id.longitude);

        findViewById(R.id.calculate).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.calculate:
                DatePicker datePicker = (DatePicker) findViewById(R.id.date_picker);
                year = datePicker.getYear();
                month = datePicker.getMonth()+1;
                date = datePicker.getDayOfMonth();

                glat = Double.valueOf(String.valueOf(glatEdit.getEditableText()));
                glong = Double.valueOf(String.valueOf(glongEdit.getEditableText()));

                calculate();
                break;
        }
    }


//    using namespace std;


    int days_of_month_1[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int days_of_month_2[] = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    double h = -0.833;
    //定义全局变量


    boolean leap_year(int year) {
        if (((year % 400 == 0) || (year % 100 != 0) && (year % 4 == 0))) return true;
        else return false;
    }

    //判断是否为闰年dao:若为闰年，返回1；若非闰年，返回0


    int days(int year, int month, int date) {
        int i, a = 0;
        for (i = 2000; i < year; i++) {
            if (leap_year(i)) a = a + 366;
            else a = a + 365;
        }

        if (leap_year(year)) {
            for (i = 0; i < month - 1; i++) {
                a = a + days_of_month_2[i];
            }
        } else {
            for (i = 0; i < month - 1; i++) {
                a = a + days_of_month_1[i];
            }
        }
        a = a + date;
        return a;
    }

    //求从格林威治时间公元2000年1月1日到计算日天数days
    private double t_century(int days, double UTo) {
        return ((double) days + UTo / 360) / 36525;
    }

    //求格林威治时间公元2000年1月1日到计算日的世纪数t
    private double L_sun(double t_century) {
        return (280.460 + 36000.770 * t_century);
    }

    //求太阳的平黄径
    double G_sun(double t_century) {
        return (357.528 + 35999.050 * t_century);
    }

    //求太阳的平近点角
    double ecliptic_longitude(double L_sun, double G_sun) {
        return (L_sun + 1.915 * Math.sin(G_sun * PI / 180) + 0.02 * Math.sin(2 * G_sun * PI / 180));
    }

    //求黄道经度
    double earth_tilt(double t_century) {
        return (23.4393 - 0.0130 * t_century);
    }

    //求地球倾角
    double sun_deviation(double earth_tilt, double ecliptic_longitude) {
        return (180 / PI * Math.asin(Math.sin(PI / 180 * earth_tilt) * Math.sin(PI / 180 * ecliptic_longitude)));
    }

    //求太阳偏差
    double GHA(double UTo, double G_sun, double ecliptic_longitude) {
        return (UTo - 180 - 1.915 * Math.sin(G_sun * PI / 180) - 0.02 * Math.sin(2 * G_sun * PI / 180) + 2.466 * Math.sin(2 * ecliptic_longitude * PI / 180) - 0.053 * Math.sin(4 * ecliptic_longitude * PI / 180));
    }

    //求格林威治时间的太阳时间角GHA
    double e(double h, double glat, double sun_deviation) {
        return 180 / PI * Math.acos((Math.sin(h * PI / 180) - Math.sin(glat * PI / 180) * Math.sin(sun_deviation * PI / 180)) / (Math.cos(glat * PI / 180) * Math.cos(sun_deviation * PI / 180)));
    }

    //求修正值e
    double UT_rise(double UTo, double GHA, double glong, double e) {
        return (UTo - (GHA + glong + e));
    }

    //求日出时间
    double UT_set(double UTo, double GHA, double glong, double e) {
        return (UTo - (GHA + glong - e));
    }

    //求日落时间
    double result_rise(double UT, double UTo, double glong, double glat, int year, int month, int date) {
        double d;
        if (UT >= UTo) d = UT - UTo;
        else d = UTo - UT;
        if (d >= 0.1) {
            UTo = UT;
            UT = UT_rise(UTo, GHA(UTo, G_sun(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))), glong, e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo))))));
            result_rise(UT, UTo, glong, glat, year, month, date);
        }
        return UT;
    }

    //判断并返回结果（日出）
    double result_set(double UT, double UTo, double glong, double glat, int year, int month, int date) {
        double d;
        if (UT >= UTo) d = UT - UTo;
        else d = UTo - UT;
        if (d >= 0.1) {
            UTo = UT;
            UT = UT_set(UTo, GHA(UTo, G_sun(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))), glong, e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo))))));
            result_set(UT, UTo, glong, glat, year, month, date);
        }
        return UT;
    }

    //求时区
//    void output(double rise, double set, double glong) {
//        mResultView.append(year + "-" + month + "-" + date + " .\n");
//        if ((int) (60 * (rise / 15 + Zone(glong) - (int) (rise / 15 + Zone(glong)))) < 10) {
//            PetkitLog.d("The time at which the sun rises is " + (int) (rise / 15 + Zone(glong)) + ":0" +
//                    (int) (60 * (rise / 15 + Zone(glong) - (int) (rise / 15 + Zone(glong)))) + " .\n");
//            mResultView.append("日出时间：" + (int) (rise / 15 + Zone(glong)) + ":0" +
//                    (int) (60 * (rise / 15 + Zone(glong) - (int) (rise / 15 + Zone(glong)))) + " .\n");
//        }
//        else {
//            PetkitLog.d("The time at which the sun rises is " + (int) (rise / 15 + Zone(glong)) + ":" +
//                    (int) (60 * (rise / 15 + Zone(glong) - (int) (rise / 15 + Zone(glong)))) + " .\n");
//            mResultView.append("日出时间：" +  (int) (rise / 15 + Zone(glong)) + ":" +
//                    (int) (60 * (rise / 15 + Zone(glong) - (int) (rise / 15 + Zone(glong)))) + " .\n");
//        }
//        if ((int) (60 * (set / 15 + Zone(glong) - (int) (set / 15 + Zone(glong)))) < 10) {
//            PetkitLog.d("The time at which the sun sets is " + (int) (set / 15 + Zone(glong)) + ": " +
//                    (int) (60 * (set / 15 + Zone(glong) - (int) (set / 15 + Zone(glong)))) + " .\n");
//            mResultView.append("日落时间：" + (int) (set / 15 + Zone(glong)) + ": " +
//                    (int) (60 * (set / 15 + Zone(glong) - (int) (set / 15 + Zone(glong)))) + " .\n");
//        }
//        else {
//            PetkitLog.d("The time at which the sun sets is " + (int) (set / 15 + Zone(glong)) + ":" +
//                    (int) (60 * (set / 15 + Zone(glong) - (int) (set / 15 + Zone(glong)))) + " .\n");
//            mResultView.append("日落时间：" + (int) (set / 15 + Zone(glong)) + ":" +
//                    (int) (60 * (set / 15 + Zone(glong) - (int) (set / 15 + Zone(glong)))) + " .\n");
//        }
//    }

    //打印结果


    private void calculate() {

        double UTo = 180.0;
//        glat = c[0] + c[1] / 60 + c[2] / 3600;
//        glong = c[0] + c[1] / 60 + c[2] / 3600;
        if (year == 0 || month == 0 || date == 0) {
            showShortToast("请先设置日期");
            return;
        }

        if (glat == 0 || glong == 0) {
            showShortToast("请先选择经纬度");
            return;
        }

        double rise, set;
        rise = result_rise(UT_rise(UTo, GHA(UTo, G_sun(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))), glong, e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))))), UTo, glong, glat, year, month, date);
        set = result_set(UT_set(UTo, GHA(UTo, G_sun(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))), glong, e(h, glat, sun_deviation(earth_tilt(t_century(days(year, month, date), UTo)), ecliptic_longitude(L_sun(t_century(days(year, month, date), UTo)), G_sun(t_century(days(year, month, date), UTo)))))), UTo, glong, glat, year, month, date);

        int riseSeconds = (int)(rise/15+8) * 3600 + (int)(60*(rise/15+8-(int)(rise/15+8))) * 60;
        int setSeconds = (int)(set/15+8) * 3600 + (int)(60*(set/15+8-(int)(set/15+8))) * 60;

        rise_down_sec_t p_seconds = new rise_down_sec_t();
        p_seconds.down_seconds = setSeconds;
        p_seconds.rise_seconds = riseSeconds;

        String file = CommonUtils.getAppDirPath() + "aq_" + System.currentTimeMillis() + ".csv";
        try {
            new File(file).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(year + "-" + month + "-" + date + ": ");
        stringBuffer.append("日出时间：" + riseSeconds/3600 + ":" + (riseSeconds%3600)/60);
        stringBuffer.append("日落时间：" + setSeconds/3600 + ":" + (setSeconds%3600)/60+ "\n");
        stringBuffer.append("time, total_pwm, white_pwm, warn_pwn\n");

        FileUtils.writeStringToFile(file, stringBuffer.toString(),true);
        light_pwm_t pwms = new light_pwm_t();
        for (int i = 0; i <= 24*6; i++) {
            calculatePMW(pwms, p_seconds, i*10*60);

            savePMWs(pwms, formatTime(i*10), file);
        }

    }

    private void savePMWs(light_pwm_t pwms, String time, String file) {

        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append("time: ").append(time).append(", white_pwm: ").append(pwms.white_light_pwm).append(", warn_pwm:").append(pwms.warn_light_pwm)
//                .append("\n");
        stringBuffer.append(time).append(", ").append(pwms.total_pwm).append(", ").append(pwms.white_light_pwm).append(", ").append(pwms.warn_light_pwm)
                .append("\n");

        FileUtils.writeStringToFile(file, stringBuffer.toString(),true);
    }

    private String formatTime(int time) {
        return String.format("%02d:%02d", time/60, time%60);
    }


    //日出日路时间，基于0点的秒数
    public class light_pwm_t {
        public int white_light_pwm;
        public int warn_light_pwm;
        public int total_pwm;
    }

    //日出日路时间，基于0点的秒数
    public class rise_down_sec_t {
        int rise_seconds;
        int down_seconds;
    }



    int calculatePMW(light_pwm_t p_pwms, rise_down_sec_t p_seconds, int curSec) {

        if (curSec > p_seconds.down_seconds || curSec < p_seconds.rise_seconds) {
            if (curSec > p_seconds.down_seconds && curSec - p_seconds.down_seconds < 3 * 60 * 60) {
                p_pwms.total_pwm = 32 * (3 * 60 * 60 - curSec + p_seconds.down_seconds) / (3 * 60 * 60);
                p_pwms.white_light_pwm = 0;
                p_pwms.warn_light_pwm = p_pwms.total_pwm;
            } else {
                p_pwms.total_pwm = 0;
                p_pwms.white_light_pwm = 0;
                p_pwms.warn_light_pwm = 0;
            }
        } else if (curSec - p_seconds.rise_seconds < 30 * 60) {
            p_pwms.total_pwm = 35 + 9 * (curSec - p_seconds.rise_seconds) / 1800;
            p_pwms.white_light_pwm = 0;
            p_pwms.warn_light_pwm = p_pwms.total_pwm;
        } else if (curSec - p_seconds.rise_seconds < 90 * 60) {
            p_pwms.total_pwm = 44 + 29 * (curSec - p_seconds.rise_seconds - 1800) / 3600;
            p_pwms.white_light_pwm = (p_pwms.total_pwm - 44) * 2;
            p_pwms.warn_light_pwm = 88 - p_pwms.total_pwm;
        } else if (curSec - p_seconds.rise_seconds < 120 * 60) {
            p_pwms.total_pwm = 73 + 7 * (curSec - p_seconds.rise_seconds - 5400) / 1800;
            p_pwms.white_light_pwm = p_pwms.total_pwm;
            p_pwms.warn_light_pwm = 0;
        } else if (p_seconds.down_seconds - curSec < 120 * 60) {
            p_pwms.total_pwm = 78 - 36 * (7200 - p_seconds.down_seconds + curSec) / 5400;
            p_pwms.warn_light_pwm = p_pwms.total_pwm > 42 ? 78 - p_pwms.total_pwm : p_pwms.total_pwm;
            p_pwms.white_light_pwm = p_pwms.total_pwm - p_pwms.warn_light_pwm;
        } else if (p_seconds.down_seconds - curSec < 30 * 60) {
            p_pwms.total_pwm = 42 - 7 * (1800 - p_seconds.down_seconds + curSec) / 1800;
            p_pwms.white_light_pwm = 0;
            p_pwms.warn_light_pwm = p_pwms.total_pwm;
        } else {
            int midSeconds = (p_seconds.down_seconds + p_seconds.rise_seconds) / 2;
            if (midSeconds > curSec) {
                p_pwms.total_pwm = 80 + 20 * (curSec - p_seconds.rise_seconds - 7200) / (midSeconds - p_seconds.rise_seconds - 7200);
                p_pwms.white_light_pwm = p_pwms.total_pwm;
                p_pwms.warn_light_pwm = 0;
            } else {
                p_pwms.total_pwm = 100 - 22 * (curSec - midSeconds) / (p_seconds.down_seconds - midSeconds - 7200);
                p_pwms.white_light_pwm = p_pwms.total_pwm;
                p_pwms.warn_light_pwm = 0;
            }
        }


        return 0;
    }
}
