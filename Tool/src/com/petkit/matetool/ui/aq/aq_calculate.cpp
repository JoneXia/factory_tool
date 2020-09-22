//
// Created by Jone Xia on 2020/8/24.
//
// 1. 根据经度、纬度、时区计算日出日落时间
// 2. 根据本地时间、日出日落时间计算的冷白、暖白占空比
//

#define PI 3.1415926

 #include<math.h>

 #include<iostream>

 using namespace std;


 int days_of_month_1[]={31,28,31,30,31,30,31,31,30,31,30,31};

 int days_of_month_2[]={31,29,31,30,31,30,31,31,30,31,30,31};

 long double h=-0.833;


//日出日路时间，基于0点的秒数
typedef struct rise_down_sec_t {
    int rise_seconds;
    int down_seconds;
 } rise_down_sec_t;


//日出日路时间，基于0点的秒数
typedef struct rise_down_sec_t {
    int white_light_pwm;
    int warn_light_pwm;
    int total_pwm;
 } light_pwm_t;


int leap_year(int year){
     if(((year%400==0) || (year%100!zhi=0) && (year%4==0))) return 1;
     else return 0;
 }


 //判断是否为闰年dao:若为闰年，返回1；若非闰年，返回0
 int days(int year, int month, int day){
     int i,a=0;
     for(i=2000;i<year;i++){
         if(leap_year(i))
            a=a+366;
         else
            a=a+365;
     }

     if(leap_year(year)){
         for(i=0;i<month-1;i++){
             a=a+days_of_month_2[i];
         }
     } else {
         for(i=0;i<month-1;i++){
             a=a+days_of_month_1[i];
         }
     }
     a=a+day;
     return a;
 }

 //求从格林威治时间公元2000年1月1日到计算日天数days
 long double t_century(int days, long double UTo){
     return ((long double)days+UTo/360)/36525;
 }

 //求格林威治时间公元2000年1月1日到计算日的世纪数t
 long double L_sun(long double t_century){
     return (280.460+36000.770*t_century);
 }

 //求太阳的平黄径
long double G_sun(long double t_century){

     return (357.528+35999.050*t_century);

 }

 //求太阳的平近点角
long double ecliptic_longitude(long double L_sun,long double G_sun){
     return (L_sun+1.915*sin(G_sun*PI/180)+0.02*sin(2*G_sun*PI/180));
 }

 //求黄道经度
long double earth_tilt(long double t_century){
     return (23.4393-0.0130*t_century);
 }

 //求地球倾角
long double sun_deviation(long double earth_tilt, long double ecliptic_longitude){
     return (180/PI*asin(sin(PI/180*earth_tilt)*sin(PI/180*ecliptic_longitude)));
 }

 //求格林威治时间的太阳时间角GHA
long double GHA(long double UTo, long double G_sun, long double ecliptic_longitude){
     return (UTo-180-1.915*sin(G_sun*PI/180)-0.02*sin(2*G_sun*PI/180)+2.466*sin(2*ecliptic_longitude*PI/180)-0.053*sin(4*ecliptic_longitude*PI/180));
 }

 //求太阳偏差
 long double e(long double h, long double glat, long double sun_deviation){
     return 180/PI*acos((sin(h*PI/180)-sin(glat*PI/180)*sin(sun_deviation*PI/180))/(cos(glat*PI/180)*cos(sun_deviation*PI/180)));
 }

 //求日出时间
 long double UT_rise(long double UTo, long double GHA, long double glong, long double e){
     return (UTo-(GHA+glong+e));
 }

 //求日落时间
long double UT_down(long double UTo, long double GHA, long double glong, long double e){
     return (UTo-(GHA+glong-e));
 }

 //求日出时间
long double result_rise(long double UT, long double UTo, long double glong, long double glat, int year, int month, int day){
     long double d;
     if(UT>=UTo)
        d=UT-UTo;
     else
        d=UTo-UT;

     if(d>=0.1) {
         UTo=UT;
         UT=UT_rise(UTo,GHA(UTo,G_sun(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo)))),glong,e(h,glat,sun_deviation(earth_tilt(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo))))));
         result_rise(UT,UTo,glong,glat,year,month,day);
     }

     return UT;
 }

 //求日落时间
long double result_down(long double UT, long double UTo, long double glong, long double glat, int year, int month, int day){
     long double d;
     if(UT>=UTo)
        d=UT-UTo;
     else
        d=UTo-UT;

     if(d>=0.1){
         UTo=UT;
         UT=UT_down(UTo,GHA(UTo,G_sun(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo)))),glong,e(h,glat,sun_deviation(earth_tilt(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo))))));
         result_down(UT,UTo,glong,glat,year,month,day);
     }

     return UT;

 }


/**
  p_seconds 输出日出日落时间

  year  年
  mouth  月
  day  日
  glat  纬度
  glong  经度
  zone  时区，注意0时区传0，东西时区用正负数
**/
int calculateRiseAndDown(rise_down_sec_t *p_seconds, int year, int month, int day, long double glat, long double glong, int zone){

     long double UTo=180.0;

     long double rise,down;

     rise = result_rise(UT_rise(UTo,GHA(UTo,G_sun(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),
                G_sun(t_century(days(year,month,day),UTo)))),glong,e(h,glat,sun_deviation(earth_tilt(t_century(days(year,month,day),UTo)),
                ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo)))))),UTo,glong,glat,year,month,day);
     down = result_down(UT_down(UTo,GHA(UTo,G_sun(t_century(days(year,month,day),UTo)),ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),
                G_sun(t_century(days(year,month,day),UTo)))),glong,e(h,glat,sun_deviation(earth_tilt(t_century(days(year,month,day),UTo)),
                ecliptic_longitude(L_sun(t_century(days(year,month,day),UTo)),G_sun(t_century(days(year,month,day),UTo)))))),UTo,glong,glat,year,month,day);

     p_sec->rise_seconds = (int)(rise/15+zone) * 3600 + (int)(60*(rise/15+zone-(int)(rise/15+zone))) * 60;
     p_sec->down_seconds = (int)(down/15+zone) * 3600 + (int)(60*(down/15+zone-(int)(down/15+zone))) * 60;

     return 0;
 }


/**
    p_pwns 输出冷白、暖白PWN占空比，总的占空比

    p_seconds 输入日出日落时间
    curSec 输入当前基于今天0点的秒数
*/
 int calculatePMW(light_pwm_t &p_pwms, rise_down_sec_t* p_seconds, int curSec) {

     if (curSec > p_seconds->down_seconds || curSec < p_seconds->rise_seconds) {
        if (curSec > p_seconds->down_seconds && curSec - p_seconds->down_seconds < 3 * 60 * 60) {
            p_pwms->total_pwm = 32 * (3 * 60 * 60 - curSec + p_seconds->down_seconds) / (3 * 60 * 60);
            p_pwms->white_light_pwm = 0;
            p_pwms->warn_light_pwm = p_pwms->total_pwm;
        } else {
            p_pwms->total_pwm = 0;
            p_pwms->white_light_pwm = 0;
            p_pwms->warn_light_pwm = 0;
        }
    } else if (curSec - p_seconds->rise_seconds < 30 * 60) {
        p_pwms->total_pwm = 35 + 9 * (curSec - p_seconds->rise_seconds) / 1800;
        p_pwms->white_light_pwm = 0;
        p_pwms->warn_light_pwm = p_pwms->total_pwm;
    } else if (curSec - p_seconds->rise_seconds < 90 * 60) {
        p_pwms->total_pwm = 44 + 29 * (curSec - p_seconds->rise_seconds - 1800) / 3600;
        p_pwms->white_light_pwm = (p_pwms->total_pwm - 44) * 2;
        p_pwms->warn_light_pwm = 88 - p_pwms->total_pwm;
    } else if (curSec - p_seconds->rise_seconds < 120 * 60) {
        p_pwms->total_pwm = 73 + 7 * (curSec - p_seconds->rise_seconds - 5400) / 1800;
        p_pwms->white_light_pwm = p_pwms->total_pwm;
        p_pwms->warn_light_pwm = 0;
    } else if (p_seconds->down_seconds - curSec < 120 * 60) {
        p_pwms->total_pwm = 78 - 36 * (7200 - p_seconds->down_seconds + curSec) / 5400;
        p_pwms->warn_light_pwm = p_pwms->total_pwm > 42 ? 78 - p_pwms->total_pwm : p_pwms->total_pwm;
        p_pwms->white_light_pwm = p_pwms->total_pwm - p_pwms->warn_light_pwm;
    } else if (p_seconds->down_seconds - curSec < 30 * 60) {
        p_pwms->total_pwm = 42 - 7 * (1800 - p_seconds->down_seconds + curSec) / 1800;
        p_pwms->white_light_pwm = 0;
        p_pwms->warn_light_pwm = p_pwms->total_pwm;
    } else {
        int midSeconds = (p_seconds->down_seconds + p_seconds->rise_seconds) / 2;
        if (midSeconds > curSec) {
            p_pwms->total_pwm = 80 + 20 * (curSec - p_seconds->rise_seconds - 7200) / (midSeconds - p_seconds->rise_seconds - 7200);
            p_pwms->white_light_pwm = p_pwms->total_pwm;
            p_pwms->warn_light_pwm = 0;
        } else {
            p_pwms->total_pwm = 100 - 22 * (curSec - midSeconds) / (p_seconds->down_seconds - midSeconds - 7200);
            p_pwms->white_light_pwm = p_pwms->total_pwm;
            p_pwms->warn_light_pwm = 0;
        }
    }

    //TODO: 读取运行参数，用来计算最终占空比
    //追光冷白百分比，默认100，需要从运行参数中读取；
    int white_percent = 100;
    //追光暖白百分比，默认100，需要从运行参数中读取；
    int warn_percent = 100;

    p_pwms->white_light_pwm = p_pwms->white_light_pwm * white_percent / 100;
    p_pwms->warn_light_pwm = p_pwms->warn_light_pwm * warn_percent / 100;

    return 0;
 }



