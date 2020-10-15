package com.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {
    //----------------format parse---------------
    private static final Logger LOGGER= LoggerFactory.getLogger(DateUtil.class);

    private static final String DATE_FORMAT="yyyy-MM-dd";
    private static final String DATETIME_FORMAT="yyyy-MM-dd HH:mm:ss";

    private static final ThreadLocal<Map<String, DateFormat>> dateFormatThreadLocal=new ThreadLocal<>();
    private static DateFormat getDateFormat(String pattern){
        if (pattern==null||pattern.length()==0) {
            throw new IllegalArgumentException("patten cannot be empty.");
        }
        Map<String,DateFormat> dateFormatMap=dateFormatThreadLocal.get();
        if (dateFormatMap != null&&dateFormatMap.containsKey(pattern)) {
            return dateFormatMap.get(pattern);
        }
        synchronized (dateFormatThreadLocal){
            if (dateFormatMap==null) {
                dateFormatMap=new HashMap<>();
            }
            dateFormatMap.put(pattern,new SimpleDateFormat(pattern));
            dateFormatThreadLocal.set(dateFormatMap);
        }
        return dateFormatMap.get(pattern);
    }

    /**
     * format datetime. like "yyyy-MM-dd"
     * @param date
     * @return
     */
    public static String formatDate(Date date){
        return format(date,DATE_FORMAT);
    }

    /**
     * format date. like "yyyy-MM-dd HH:mm:ss"
     * @param date
     * @return
     */
    public static String formatDateTime(Date date){
        return format(date,DATETIME_FORMAT);
    }

    /**
     * format date
     * @param date
     * @param dateFormat
     * @return
     */
    private static String format(Date date, String dateFormat) {
        return getDateFormat(dateFormat).format(date);
    }

    /**
     * parse date string like "yyyy-MM-dd HH:mm:ss"
     * @param dateString
     * @return
     */
    public static Date parseDate(String dateString){
        return parse(dateString,DATE_FORMAT);
    }

    /**
     * parse dateTime like "yyyy-MM-dd HH:mm:ss"
     * @param dateString
     * @return
     */
    public static Date parseDateTime(String dateString){
        return parse(dateString,DATETIME_FORMAT);
    }

    /**
     * parse date
     * @param dateString
     * @param pattern
     * @return
     */
    private static Date parse(String dateString, String pattern) {
        try {
            Date date=getDateFormat(pattern).parse(dateString);
            return date;
        } catch (ParseException e) {
            LOGGER.error("parse date error, dateString = {} , pattern ={}; errorMsg ={}",dateString,pattern,e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
    // ---------------------- add date ----------------------
    public static Date addDays(final Date date,final int amount){
        return add(date, Calendar.DAY_OF_MONTH,amount);
    }
    private static Date add(final Date date, final int dayOfMonth,final int amount) {
        if (date == null) {
            return null;
        }
        final Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(dayOfMonth,amount);
        return calendar.getTime();
    }

    public static void main(String[] args) {

        String s = DateUtil.formatDate(DateUtil.addDays(new Date(), 1));
        String format = DateUtil.format(DateUtil.addDays(new Date(), 1), DATETIME_FORMAT);
        System.out.println(format);
        Date date = DateUtil.parseDateTime(format);
        Date date1 = DateUtil.parseDate(s);
        System.out.println("date1  "+date1);
        System.out.println("date  "+date);
        System.out.println(s);
        System.out.println(format);
    }
}
