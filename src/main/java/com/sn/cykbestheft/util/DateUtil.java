package com.sn.cykbestheft.util;

import java.text.SimpleDateFormat;
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
     * 时间转换为时间戳毫秒
     *
     * @param date
     * @return
     */
    public static Long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 当前时间 往前推多少天
     *
     * @param strCurrentTime
     * @param interval
     * @return
     */
    public static String intervalTime(String strCurrentTime, int interval) {
        Date currentTime = DateUtil.strToDate(strCurrentTime, "yyyy-MM-dd HH:mm:ss");
        long oneDay = 24 * 60 * 60 * 1000;
        long lgCurrentTime = currentTime.getTime();
        long lgStartTime = lgCurrentTime - (interval * oneDay);
        Date startTime = new Date(lgStartTime);
        return DateUtil.dateToStr(startTime, "yyyy-MM-dd HH:mm:ss");
    }
}
