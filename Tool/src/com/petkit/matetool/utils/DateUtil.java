package com.petkit.matetool.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat") public class DateUtil {
	public static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static final String ISO8601DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String ISO8601DATE_WITH_MILLS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ISO8601DATE_WITH_ZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final String ISO8601DATE_WITH_ZONE_MILLS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_2 = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_3 = "yyyy.MM.dd";
    public static final String DATE_FORMAT_4 = "yyyy.MM.dd HH:mm:ss";
    public static final String DATE_FORMAT_5 = "MM-dd HH:mm";
    public static final String DATE_FORMAT_6 = "HH:mm";
    public static final String DATE_FORMAT_7 = "yyyyMMdd";
    public static final int ISO8601DATE_FORMAT_VALUE_LENGTH = ISO8601DATE_FORMAT
            .length() - 4;

    public static Date parseISO8601Date(String s) throws Exception {
        if (s == null || s.isEmpty()) {
            return null;
        }
        Date date = null;
        if (s.charAt(s.length() - 1) == 'Z') {
            String format = (s.length() == ISO8601DATE_FORMAT_VALUE_LENGTH) ? ISO8601DATE_FORMAT
                    : ISO8601DATE_WITH_MILLS_FORMAT;
            DateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(GMT_TIMEZONE);
            date = dateFormat.parse(s);
        } else if (s.length() == DATE_FORMAT.length()) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setTimeZone(GMT_TIMEZONE);
            date = dateFormat.parse(s);
        } else if (s.indexOf('.') > 0) {
            date = new SimpleDateFormat(ISO8601DATE_WITH_ZONE_MILLS_FORMAT)
                    .parse(s);
        } else {
            date = new SimpleDateFormat(ISO8601DATE_WITH_ZONE_FORMAT).parse(s);
        }
        return date;
    }

    public static String formatISO8601Date(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(ISO8601DATE_FORMAT);
        dateFormat.setTimeZone(GMT_TIMEZONE);
        return dateFormat.format(date);
    }

    public static String formatISO8601DateWithMills(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(
                ISO8601DATE_WITH_MILLS_FORMAT);
        dateFormat.setTimeZone(GMT_TIMEZONE);
        return dateFormat.format(date);
    }

    public static String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(GMT_TIMEZONE);
        return dateFormat.format(date);
    }

    /**
     * 格式：yyyy-MM-dd HH:mm
     *
     * @param dateString
     * @return
     */
    public static String getFormatDateFromString(String dateString) {
        try {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());//new SimpleDateFormat(DATE_FORMAT_2);
            return dateFormat.format(parseISO8601Date(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }
    
    /**
     * 格式：yyyy-MM-dd
     * @param dateString
     * @return
     */
    public static String getFormatDate2FromString(String dateString){
    	try {
			return formatDate(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
//    /**
//     * 格式：yyyy-MM-dd HH:mm
//     * @param dateString
//     * @return
//     */
//    public static String getFormatDateFromString(String dateString){
//    	try {
//    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);
//			return dateFormat.format(parseISO8601Date(dateString));
//		} catch (Exception e) {
//			return dateString;
//		}
//    }
    
    /**
     * 格式：yyyy.MM.dd
     * @param dateString
     * @return
     */
    public static String getFormatDate3FromString(String dateString){
    	try {
    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_3);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
    /**
     * 格式：yyyy.MM.dd HH:mm:ss
     * @param dateString
     * @return
     */
    public static String getFormatDate4FromString(String dateString){
    	try {
    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_4);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
    /**
     * 格式：MM-dd HH:mm
     * @param dateString
     * @return
     */
    public static String getFormatDate5FromString(String dateString){
    	try {
    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_5);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
    /**
     * 格式：HH:mm
     * @param dateString
     * @return
     */
    public static String getFormatDate6FromString(String dateString){
    	try {
    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_6);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
    
    /**
     * 格式：HH:mm
     * @param dateString
     * @return
     */
    public static String getFormatDate7FromString(String dateString){
    	try {
    		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_7);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
    }
    
    
}
