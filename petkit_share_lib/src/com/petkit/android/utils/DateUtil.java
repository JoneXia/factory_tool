package com.petkit.android.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
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
	public static final String DATE_FORMAT_8 = "MM-dd";
	public static final String DATE_FORMAT_9 = "yyyy/M/d";
	public static final String DATE_FORMAT_10 = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_11 = "yyyy年MM月dd日";
	public static final int ISO8601DATE_FORMAT_VALUE_LENGTH = ISO8601DATE_FORMAT.length() - 4;

	public static Date parseISO8601Date(String s) throws Exception {
		if (s == null || s.isEmpty()) {
			return null;
		}
		Date date = null;
		if (s.charAt(s.length() - 1) == 'Z') {
			String format = (s.length() == ISO8601DATE_FORMAT_VALUE_LENGTH) ? ISO8601DATE_FORMAT : ISO8601DATE_WITH_MILLS_FORMAT;
			DateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(GMT_TIMEZONE);
			date = dateFormat.parse(s);
		} else if (s.length() == DATE_FORMAT.length()) {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			dateFormat.setTimeZone(GMT_TIMEZONE);
			date = dateFormat.parse(s);
		} else if (s.indexOf('.') > 0) {
			date = new SimpleDateFormat(ISO8601DATE_WITH_ZONE_MILLS_FORMAT).parse(s);
		} else if (s.length() == 8) {
			date = new SimpleDateFormat(DATE_FORMAT_7).parse(s);
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
		DateFormat dateFormat = new SimpleDateFormat(ISO8601DATE_WITH_MILLS_FORMAT);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat.format(date);
	}

	public static String formatDate(Date date) {
		// DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,
		// Locale.getDefault());
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat.format(date);
	}

	/**
	 * 格式：yyyy-MM-dd
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate2FromString(String dateString) {
		try {
			return formatDate(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：yyyy-MM-dd HH:mm
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDateFromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：yyyy.MM.dd
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate3FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_3);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：yyyy.MM.dd HH:mm:ss
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate4FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_4);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：MM-dd HH:mm
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate5FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_5);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：HH:mm
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate6FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_6);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：HH:mm
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate7FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_7);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：MM-dd
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate8FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("MM-dd");
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}

	/**
	 * 格式：yyyy/M/d
	 * 
	 * @param dateString
	 * @return
	 */
	public static String getFormatDate9FromString(String dateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_9);
			return dateFormat.format(parseISO8601Date(dateString));
		} catch (Exception e) {
			return dateString;
		}
	}
	
	
	/**
	 * getOffsetDays
	 * 
	 * @param dateString
	 * @return days
	 */
	public static int getOffsetDaysToTodayFromString(String dateString) {
		try {

			Date date = parseISO8601Date(dateString);
			Date curDate = new Date();
			String currentdateStr = date2Str(curDate, DateUtil.DATE_FORMAT);
			Date str2Date = str2Date(currentdateStr, DateUtil.DATE_FORMAT);
			String dateStr = date2Str(date, DateUtil.DATE_FORMAT);

			if (currentdateStr.equals(dateStr)) {
				return 0;
			}

			if (date.getTime() > str2Date.getTime()) {
				return (int) ((date.getTime() - str2Date.getTime()) / (1000 * 3600 * 24));
			} else {
				return (int) ((date.getTime() - str2Date.getTime()) / (1000 * 3600 * 24) - 1);
			}

		} catch (Exception e) {
		}

		return 0;
	}
	
	/**
	 * Date To String
	 * @param date
	 * @param format
	 * @return
	 */
	public static String date2Str(Date date, String format) {
		if (null == date) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * String To Date
	 * @param str
	 * @param format
	 * @return Date
	 */
	public static Date str2Date(String str, String format) {
		if (null == str) {
			return null;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * String To Calendar
	 * @param dateTime
	 * @param format
	 * @return Calendar
	 */
	public static Calendar convertToCanlendar(String dateTime, String format) {
		Calendar calendar = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date date = sdf.parse(dateTime);
			calendar = Calendar.getInstance();
			calendar.setTime(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return calendar;
	}
	/**
	 * 转换日期格式
	 * @param dateString
	 * @param oldFormatStr
	 * @param newFormatStr
	 * @return
	 */
	public static String getConvertDateString(String dateString, String oldFormatStr, String newFormatStr) {
		String dateTime = "";
		try {
			DateFormat format = new SimpleDateFormat(oldFormatStr);
			Date date = format.parse(dateString);
			SimpleDateFormat sdf = new SimpleDateFormat(newFormatStr);
			dateTime = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateTime;
	}
	
	/**
	 * long2str
	 * @param longdate
	 * @param format
	 * @return
	 */
	public static String long2str(long longdate, String format) {
		Date date = new Date(longdate);
		return date2Str(date, format);
	}
	
	
	/**
	 * 日期的毫秒值
	 * @param str
	 * @param format
	 * @return
	 */
	public static long getMillisecondByDateTime(String str, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		long millionSeconds = 0;
		try {
			millionSeconds = sdf.parse(str).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return millionSeconds;
	}
	
	/**
	 * 两个日期相差的天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int daysOfTwo(Date startDate, Date endDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int sday = calendar.get(Calendar.DAY_OF_YEAR);

		calendar.setTime(endDate);
		int eday = calendar.get(Calendar.DAY_OF_YEAR);

		return eday - sday;
	}
}
