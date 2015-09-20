package com.websocket.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeConversion {
	
	public static Long getTime(){
		Calendar calendar = Calendar.getInstance();
	    TimeZone fromTimeZone = calendar.getTimeZone();
	    TimeZone toTimeZone = TimeZone.getTimeZone("IST");

	    calendar.setTimeZone(fromTimeZone);
	    calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
	    if (fromTimeZone.inDaylightTime(calendar.getTime())) {
	        calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
	    }

	    calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
	    if (toTimeZone.inDaylightTime(calendar.getTime())) {
	        calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
	    }
	    return calendar.getTimeInMillis();
	}

	public static String getCurrentDateAndTime(){
		Date date = new Date(getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		return sdf.format(date);
	}
	public static String getSimpleDate(Long timemillis){
		Date date = new Date(timemillis);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date);
	}
	public static String getSimpleTime(Long timemillis){
		Date date = new Date(timemillis);
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		return sdf.format(date);
	}
	public static String getCurrentDate(){
		Date date = new Date(getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date);
	}
	
	public static String getCurrentTime(){
		Date date = new Date(getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		return sdf.format(date);
	}
	
}
