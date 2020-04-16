package com.sn.cykbestheft.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author songning
 * @create 2019/7/24 13:31
 */
public class DateUtil {

    /**
     * java.util.Date => String
     *
     * @param date      要格式的java.util.Date对象
     * @param strFormat 输出的String字符串格式的限定（如："yyyy-MM-dd HH:mm:ss"）
     * @return 表示日期的字符串
     */
    public static String dateToStr(Date date, String strFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strFormat);
        String str = simpleDateFormat.format(date);
        return str;
    }

    /**
     * String => java.util.Date
     *
     * @param str        表示日期的字符串
     * @param dateFormat 传入字符串的日期表示格式（如："yyyy-MM-dd HH:mm:ss"）
     * @return java.util.Date类型日期对象（如果转换失败则返回null）
     */
    public static Date strToDate(String str, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = simpleDateFormat.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * java.sql.Timestamp => String
     *
     * @param timestamp 要格式的java.sql.Timestamp对象
     * @param strFormat 输出的String字符串格式的限定（如："yyyy-MM-dd HH:mm:ss"）
     * @return 表示日期的字符串
     */
    public static String dateToStr(java.sql.Timestamp timestamp, String strFormat) {
        DateFormat dateFormat = new SimpleDateFormat(strFormat);
        String str = dateFormat.format(timestamp);
        return str;
    }

    /**
     * String =>java.sql.Timestamp
     *
     * @param strDate    表示日期的字符串
     * @param dateFormat 传入字符串的日期表示格式（如："yyyy-MM-dd HH:mm:ss"）
     * @return java.sql.Timestamp类型日期对象（如果转换失败则返回null）
     */
    public static java.sql.Timestamp strToSqlDate(String strDate, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = simpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
        return sqlDate;
    }

    /**
     * java.util.Date => java.sql.Timestamp
     *
     * @param date 要转化的java.util.Date对象
     * @return 转化后的java.sql.Timestamp对象
     */
    public static java.sql.Timestamp dateToTime(Date date) {
        String strDate = dateToStr(date, "yyyy-MM-dd HH:mm:ss SSS");
        return strToSqlDate(strDate, "yyyy-MM-dd HH:mm:ss SSS");
    }

    /**
     * java.sql.Timestamp => java.util.Date
     * 二者是父子关系，可以直接赋值，自动转换
     *
     * @param timestamp
     * @return
     */
    public static Date timeToDate(java.sql.Timestamp timestamp) {
        return timestamp;
    }

    /**
     * 获取当前时间的前几个小时
     *
     * @param hour
     * @return
     */
    public static Date getBeforeByCurrentTime(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return calendar.getTime();
    }

    /**
     * 时间转换为时间戳毫秒
     * @param date
     * @return
     */
    public static Long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 当前时间 往前推多少天
     * @param strCurrentTime
     * @param interval
     * @return
     */
    public static String intervalTime(String strCurrentTime, int interval) {
        Date currentTime = DateUtil.strToDate(strCurrentTime, "yyyy-MM-dd HH:mm:ss");
        Long oneDay = (long) (24 * 60 * 60 * 1000);
        Long lgCurrentTime = currentTime.getTime();
        Long lgStartTime = lgCurrentTime - (interval * oneDay);
        Date startTime = new Date(lgStartTime);
        String strStartTime = DateUtil.dateToStr(startTime, "yyyy-MM-dd HH:mm:ss");
        return strStartTime;
    }

    public static void main(String[] args) {
       DateUtil.intervalTime("2019-12-15 11:16:37", 1923 - 1922 - 1);
    }
}
