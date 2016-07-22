package com.mangocity.etl.kettle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {


	public static final DateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static final DateFormat YYYYMMDD_HH = new SimpleDateFormat("yyyyMMdd_HH");
	
	public static final DateFormat YYYYMMDD_HH_MM = new SimpleDateFormat("yyyyMMdd_HH_mm");
	
	public static String getCurrentDateStr(DateFormat formate) {
		Date date = new Date();
		return formate.format(date);
	}
	
	public static String getCurrentDateStr() {
		return getCurrentDateStr(YYYYMMDDHHMMSS);
	}
	

	public static Date getDateByStr(String dateStr, DateFormat format) {
		try {
			return format.parse(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static Date getDateByStr(String dateStr) {
		return getDateByStr(dateStr, YYYYMMDDHHMMSS);
	}
	
	public static Date stringToDate(String dateStr, String formart) {
		if ((dateStr != null) && (!"".equals(dateStr))) {
			SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(formart);
			Date localDate = null;
			try {
				localDate = localSimpleDateFormat.parse(dateStr);
			} catch (ParseException localParseException) {
				localParseException.printStackTrace();
			}
			return localDate;
		}
		return null;
	}
}
