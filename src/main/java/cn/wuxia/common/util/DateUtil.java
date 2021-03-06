/**
 * <h3>Class description</h3> <h4>Date processing class</h4> <h4>Special Notes</h4>
 *
 * @version 0.1
 * @author songlin.li 2008-8-11
 * @support by
 * @version 0.2
 * @author songlin.li
 */
package cn.wuxia.common.util;

import cn.wuxia.common.exception.AppWebException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nutz.castor.Castors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtil extends DateUtils {
    protected static transient final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    private static final int[] dayArray = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public enum DateFormatter {
        /**
         * yyyyMMddHHmmssSSS
         */
        FORMAT_YYYYMMDDHHMMSSSSS("yyyyMMddHHmmssSSS"),
        /**
         * yyyyMMddHHmmss
         */
        FORMAT_YYYYMMDDHHMMSS("yyyyMMddHHmmss"),

        /**
         * yyyyMMddHHmm
         */
        FORMAT_YYYYMMDDHHMM("yyyyMMddHHmm"),

        /**
         * yyyyMMdd
         */
        FORMAT_YYYYMMDD("yyyMMdd"),

        /**
         * yyyy-MM
         */
        FORMAT_YYYY_MM("yyyy-MM"),

        /**
         * yyyy
         */
        FORMAT_YYYY("yyyy"),
        /**
         * MMdd
         */
        FORMAT_MMDD("MMdd"),
        /**
         * HHmm
         */
        FORMAT_HHMM("HHmm"),
        /**
         * HHmmss
         */
        FORMAT_HHMMSS("HHmmss"),
        /**
         * yyyy年MM月dd日 HH:mm
         */
        FORMAT_YYYY_MM_DD_CN_HH_MM("yyyy年MM月dd日 HH:mm"),

        /**
         * yyyy年MM月dd日HH时mm分
         */
        FORMAT_YYYY_MM_DD_HH_MM_CN("yyyy年MM月dd日HH时mm分"),

        /**
         * yyyy-MM-dd HH:mm
         */
        FORMAT_YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),

        /**
         * yyyy-MM-dd
         */

        FORMAT_YYYY_MM_DD("yyyy-MM-dd"),

        /**
         * yyyy-MM-dd HH:mm:ss
         */

        FORMAT_YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),

        /**
         * yyyy-MM-dd EEE
         */

        FORMAT_YYYY_MM_DD_EEE("yyyy-MM-dd EEE"),

        /**
         * dd/MM/yyyy
         */
        FORMAT_DD_MM_YYYY("dd/MM/yyyy"),

        /**
         * dd/MM/yyyy HH:mm:ss
         */
        FORMAT_DD_MM_YYYY_HH_MM_SS("dd/MM/yyyy HH:mm:ss"),

        /**
         * dd MMM yyyy HH:mm:ss
         */
        FORMAT_DD_MMM_YYYY_HH_MM_SS("dd MMM yyyy HH:mm:ss"),

        /**
         * dd MMM yyyy
         */
        FORMAT_DD_MMM_YYYY("dd MMM yyyy"),

        MONDAY_CN("周一"),

        TUESDAY_CN("周二"),

        WEDNESDAY_CN("周三"),

        THURSDAY_CN("周四"),

        FRIDAY_CN("周五"),

        SATURDAY_CN("周六"),

        SUNDAY_CN("周日");

        private String dateformat;

        private DateFormatter(String dateformat) {
            this.dateformat = dateformat;
        }

        public String getFormat() {
            return this.dateformat;
        }
    }

    public enum TypeEnum {
        YEAR, HOUR, DAY, MINUTE, SECOND, MILLISECOND;

        TypeEnum() {
        }
    }

    /**
     * million Seconds Of Day
     */
    public static long millionSecondsOfDay = 86400000;

    /**
     * @param date -java.util.Date
     * @return
     * @description : convert java.util.Date to java.sql.Date
     */
    public static java.sql.Date utilDateToSQLDate(java.util.Date date) {
        if (date == null)
            return null;
        Calendar cl = Calendar.getInstance();

        cl.setTime(date);
        java.sql.Date jd = new java.sql.Date(cl.getTimeInMillis());
        return jd;
    }

    /**
     * @param date -java.sql.Date
     * @return
     * @description :convert java.sql.Date to java.util.Date
     */
    public static java.util.Date sqlDateToUtilDate(java.sql.Date date) {
        if (date == null)
            return null;
        Calendar cl = Calendar.getInstance();

        cl.setTime(date);
        java.util.Date jd = new java.util.Date(cl.getTimeInMillis());
        return jd;
    }

    /**
     * @param timestamp -java.sql.Timestamp
     * @return
     * @description : convert java.sql.Timestamp to java.util.Date
     */
    public static java.util.Date timestampToUtilDate(java.sql.Timestamp timestamp) {
        return Castors.me().castTo(timestamp, java.util.Date.class);
    }

    /**
     * convert java.util.Date to  java.sql.Timestamp
     *
     * @param date -java.util.Date
     * @return
     * @author songlin
     */
    public static Timestamp utilDateToTimestamp(Date date) {
        return Castors.me().castTo(date, Timestamp.class);
    }

    /**
     * @param date
     * @return
     * @description : Support Date Format:YYYY-MM-DD, YYYY:M:D, YYYY/M/DD
     * @author songlin.li
     */
    @Deprecated
    public static Date stringToDate(String date) {
        if (date == null || "".equalsIgnoreCase(date))
            return null;

        Calendar cd = Calendar.getInstance();
        StringTokenizer token = new StringTokenizer(date, "-/ :.");
        if (token.hasMoreTokens()) {
            cd.set(Calendar.YEAR, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.YEAR, 1970);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MONTH, Integer.parseInt(token.nextToken()) - 1);
        } else {
            cd.set(Calendar.MONTH, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.DAY_OF_MONTH, 1);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MINUTE, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.MINUTE, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.SECOND, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.SECOND, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MILLISECOND, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.MILLISECOND, 0);
        }

        return cd.getTime();
    }

    /**
     * @param date
     * @return
     * @description : get First Date Of Month
     */
    public static Date getFirstDateOfMonth(String date) {
        return getFirstDateOfMonth(stringToDate(date));
    }

    public static Date getFirstDateOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 当前月份1号0点0分0秒
     *
     * @return
     */
    public static Date getFirstDateOfMonth() {
        return getFirstDateOfMonth(newInstanceDateBegin());
    }

    /**
     * @param date
     * @return
     * @description : get Last Date Of Month
     */
    public static Date getLastDateOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = 0;
        if (month < 1 || month > 12) {
            return null;
        }

        if (month == 2) {
            if (isLeapYear(year)) {
                day = 29;
            } else {
                day = dayArray[month - 1];
            }
        } else {
            day = dayArray[month - 1];
        }

        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public static Date getLastDateOfMonth(String date) {
        return getLastDateOfMonth(stringToDate(date));
    }

    /**
     * 当前月份23点59分59秒
     *
     * @return
     */
    public static Date getLastDateOfMonth() {
        return getLastDateOfMonth(newInstanceDateEnd());
    }

    public static int getYear(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDay(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getHour(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * @param year
     * @return
     * @description : is LeapYear
     */
    public static boolean isLeapYear(int year) {

        if ((year % 400) == 0)
            return true;
        else if ((year % 4) == 0) {
            if ((year % 100) == 0)
                return false;
            else
                return true;
        } else
            return false;
    }

    /**
     * @param type  DateUtil.DAY,DateUtil.HOUR...
     * @param value
     * @return
     * @description : Access to current time before time
     * @author songlin.li 2007-07-03
     */
    public static Date getDateTimeBeforeNow(TypeEnum type, int value) {
        Calendar cd = Calendar.getInstance();
        long nextTime = cd.getTimeInMillis();

        switch (type) {
            case DAY:
                nextTime = nextTime - 24 * 60 * 60 * 1000 * value;
                break;
            case HOUR:
                nextTime = nextTime - 60 * 60 * 1000 * value;
                break;
            case MINUTE:
                nextTime = nextTime - 60 * 1000 * value;
                break;
            case SECOND:
                nextTime = nextTime - 1000 * value;
                break;
            default:
                break;
        }
        return new Date(nextTime);

    }

    /**
     * @param date
     * @param type
     * @param value
     * @return
     * @description : Access to current time after time
     */
    public static Date getDateTimeAfter(Date date, TypeEnum type, long value) {
        // Calendar cd = Calendar.getInstance();
        long nextTime = date.getTime();

        switch (type) {
            case DAY:
                nextTime = nextTime + 24 * 60 * 60 * 1000 * value;
                break;
            case HOUR:
                nextTime = nextTime + 60 * 60 * 1000 * value;
                break;
            case MINUTE:
                nextTime = nextTime + 60 * 1000 * value;
                break;
            case SECOND:
                nextTime = nextTime + 1000 * value;
                break;
            case MILLISECOND:
                nextTime = nextTime + value;
                break;
            default:
                break;
        }
        return new Date(nextTime);

    }

    /*
     * Get the Next Date Write by Jeffy pan 2004-10-21 Date Format:YYYY-MM-DD
     * YYYY:M:D YYYY/M/DD
     */
    @Deprecated
    public static Date getNextDate(String date) {

        Calendar cd = Calendar.getInstance();
        StringTokenizer token = new StringTokenizer(date, "-/ :.");
        if (token.hasMoreTokens()) {
            cd.set(Calendar.YEAR, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.YEAR, 1970);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MONTH, Integer.parseInt(token.nextToken()) - 1);
        } else {
            cd.set(Calendar.MONTH, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.DAY_OF_MONTH, 1);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MINUTE, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.MINUTE, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.SECOND, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.SECOND, 0);
        }
        if (token.hasMoreTokens()) {
            cd.set(Calendar.MILLISECOND, Integer.parseInt(token.nextToken()));
        } else {
            cd.set(Calendar.MILLISECOND, 0);
        }

        long nextTime = cd.getTimeInMillis() + 24 * 60 * 60 * 1000;
        // SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return new Date(nextTime);
    }

    /**
     * @param date
     * @return
     * @description :isToday
     */
    public static boolean isToday(Date date) {

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        Calendar day = Calendar.getInstance();
        day.setTime(date);

        if (today.get(Calendar.YEAR) == day.get(Calendar.YEAR) && today.get(Calendar.MONTH) == day.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH))
            return true;
        else
            return false;
    }

    /**
     * @param sDate
     * @param format FORMAT_1 TO FORMAT_7
     * @return
     * @description : default format is yyyy-MM-dd
     * @author songlin.li
     */
    public static Date stringToDate(String sDate, DateFormatter format) {
        return parse(sDate, format == null ? DateFormatter.FORMAT_YYYY_MM_DD : format);
    }

    /**
     * @param sDate
     * @param format FORMAT_1 TO FORMAT_7
     * @return
     * @description : default format is yyyy-MM-dd
     * @author songlin.li
     */
    public static Date stringToDate(String sDate, String format) {
        return parse(sDate, format == null ? DateFormatter.FORMAT_YYYY_MM_DD.dateformat : format);
    }

    /**
     * @param dDate
     * @param format FORMAT_1 TO FORMAT_7
     * @return
     * @description : default format is yyyy-MM-dd
     * @author songlin.li
     */
    public static String dateToString(Date dDate, DateFormatter format) {
        return format(dDate, format == null ? DateFormatter.FORMAT_YYYY_MM_DD : format);
    }

    /**
     * @param dDate
     * @param format FORMAT_1 TO FORMAT_7
     * @return
     * @description : default format is yyyy-MM-dd
     * @author songlin.li
     */
    public static String dateToString(Date dDate, String format) {
        return format(dDate, StringUtil.isBlank(format) ? DateFormatter.FORMAT_YYYY_MM_DD.dateformat : format);
    }

    /**
     * @param date
     * @return
     * @description : default format yyyy-MM-dd HH:mm:ss
     * @author songlin.li
     */
    public static String defaultFormatTimeStamp(Date date) {
        return dateToString(date, DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * @param date1
     * @param date2
     * @return
     * @description : Get the number of days between two date
     * 注意，这里是非自然天数
     * @author songlin.li
     */
    public static int getDay(Date date1, Date date2) {
        Long d2 = date2.getTime();
        Long d1 = date1.getTime();

        Double days = 0D;
        if (date1.before(date2)) {
            days = Math.ceil((d2 - d1) / millionSecondsOfDay);
        } else {
            days = Math.ceil((d1 - d2) / millionSecondsOfDay);
        }
        return days.intValue();
    }

    /**
     * @param date Date
     * @return int return 1-7
     * @description : According to the date it's Thursday
     * @author songlin.li
     */
    public static int getWeekOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return (calendar.get(Calendar.DAY_OF_WEEK) - 1) == 0 ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * @param date
     * @param formater
     * @return
     * @description : formater "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy"....
     * @author songlin.li
     */
    public static Date parse(String date, DateFormatter formater) {
        if (StringUtil.isBlank(date))
            return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formater.getFormat(), Locale.ENGLISH);
            sdf.setLenient(false);
            return sdf.parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param date
     * @param formater
     * @return
     * @description : formater "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy"....
     * @author songlin.li
     */
    public static Date parse(String date, String formater) {
        if (StringUtil.isBlank(date))
            return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formater, Locale.ENGLISH);
            sdf.setLenient(false);
            return sdf.parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param date
     * @param formater
     * @return
     * @description : format "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy"....
     * @author songlin.li
     */
    public static String format(Date date, DateFormatter formater) {
        if (date == null)
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat(formater.getFormat(), Locale.ENGLISH);
        return formatter.format(date).trim();
    }

    /**
     * @param date
     * @param formater
     * @return
     * @description : format "yyyy-MM-dd HH:mm:ss","dd/MM/yyyy"....
     * @author songlin.li
     */
    public static String format(Date date, String formater) {
        if (date == null)
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat(formater, Locale.ENGLISH);
        return formatter.format(date).trim();
    }

    public static String getWeekDay(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        int weekDay = c.get(Calendar.DAY_OF_WEEK);
        switch (weekDay) {
            case Calendar.MONDAY:
                return DateFormatter.MONDAY_CN.dateformat;
            case Calendar.TUESDAY:
                return DateFormatter.TUESDAY_CN.dateformat;
            case Calendar.WEDNESDAY:
                return DateFormatter.WEDNESDAY_CN.dateformat;
            case Calendar.THURSDAY:
                return DateFormatter.THURSDAY_CN.dateformat;
            case Calendar.FRIDAY:
                return DateFormatter.FRIDAY_CN.dateformat;
            case Calendar.SATURDAY:
                return DateFormatter.SATURDAY_CN.dateformat;
            case Calendar.SUNDAY:
                return DateFormatter.SUNDAY_CN.dateformat;
            default:
                return "";
        }
    }

    public static int getWeekDay(String weekDate) {
        weekDate = StringUtils.trim(weekDate);
        if (StringUtils.equals(DateFormatter.MONDAY_CN.dateformat, weekDate)) {
            return Calendar.MONDAY;
        } else if (StringUtils.equals(DateFormatter.TUESDAY_CN.dateformat, weekDate)) {
            return Calendar.TUESDAY;
        } else if (StringUtils.equals(DateFormatter.WEDNESDAY_CN.dateformat, weekDate)) {
            return Calendar.WEDNESDAY;
        } else if (StringUtils.equals(DateFormatter.THURSDAY_CN.dateformat, weekDate)) {
            return Calendar.THURSDAY;
        } else if (StringUtils.equals(DateFormatter.FRIDAY_CN.dateformat, weekDate)) {
            return Calendar.FRIDAY;
        } else if (StringUtils.equals(DateFormatter.SATURDAY_CN.dateformat, weekDate)) {
            return Calendar.SATURDAY;
        } else if (StringUtils.equals(DateFormatter.SUNDAY_CN.dateformat, weekDate)) {
            return Calendar.SUNDAY;
        }
        return Calendar.MONDAY;
    }

    /**
     * new Instance Date, the date begin.<br>
     * eg. Tue Feb 25 00:00:00 CST 2014
     *
     * @return
     * @author songlin
     */
    public static Date newInstanceDateBegin() {
        Calendar date = Calendar.getInstance();
        String source = format(date.getTime(), DateFormatter.FORMAT_YYYY_MM_DD);
        return parse(source, DateFormatter.FORMAT_YYYY_MM_DD);
    }

    /**
     * new Instance Date,the date end.<br>
     * eg. Tue Feb 25 23:59:59 CST 2014
     *
     * @return
     * @author songlin
     */
    public static Date newInstanceDateEnd() {
        Calendar date = Calendar.getInstance();
        String source = format(date.getTime(), DateFormatter.FORMAT_YYYY_MM_DD);
        return parse(source + " 23:59:59", DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * new Instance Timestamp,the current datetime.<br>
     * eg. Tue Feb 25 21:29:50 CST 2014
     *
     * @return
     * @author songlin
     */
    public static Timestamp newInstanceDate() {
        Calendar date = Calendar.getInstance();
        return new Timestamp(date.getTimeInMillis());
    }

    /**
     * trim date time 00:00:00
     *
     * @param date
     * @return
     * @author songlin
     */
    public static Date trimDateTime(Date date) {
        String source = format(date, DateFormatter.FORMAT_YYYY_MM_DD);
        return parse(source, DateFormatter.FORMAT_YYYY_MM_DD);
    }

    /**
     * add date time 23:59:59
     *
     * @param date
     * @return
     * @author songlin
     */
    public static Date addDateTime(Date date) {
        String source = format(date, DateFormatter.FORMAT_YYYY_MM_DD);
        return parse(source + " 23:59:59", DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前系统时间所在周的第一日
     *
     * @return 当前系统时间所在周的第一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date firstDayOfWeek() {
        Calendar c = Calendar.getInstance();
        int weekday = c.get(Calendar.DAY_OF_WEEK) - 1;
        c.add(Calendar.DAY_OF_MONTH, -weekday);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * FIXME
     * 获取当前系统时间所在周的最后一日
     * @return 当前系统时间所在周的最后一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date lastDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 6);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在月的第一日
     *
     * @return 当前系统时间所在月的第一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date firstDayOfMonth() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在月的最后一日
     *
     * @return 当前系统时间所在月的最后一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date lastDayOfMonth() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    /**
     * 获取指定月份的第一日
     *
     * @param amount 月份变量值，通过对当前系统月份的加减得到指定的月份
     * @return
     * @author CaRson.Yan
     */
    public static Date firstDayOfSpecifiedMonth(Integer amount) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, amount);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 获取指定月份的最后一日
     *
     * @param amount 月份变量值，通过对当前系统月份的加减得到指定的月份
     * @return
     * @author CaRson.Yan
     */
    public static Date lastDayOfSpecifiedMonth(Integer amount) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, amount);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在季度的第一日
     *
     * @return 当前系统时间所在季度的第一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date firstDayOfQuarter() {
        Calendar c = Calendar.getInstance();
        int month = getQuarterInMonth(c.get(Calendar.MONTH) + 1, true);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在季度的最后一日
     *
     * @return 当前系统时间所在季度的最后一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date lastDayOfQuarter() {
        Calendar c = Calendar.getInstance();
        int month = getQuarterInMonth(c.get(Calendar.MONTH) + 1, false);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, 0);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在季度的首月
     *
     * @return 当前系统时间所在季度首月
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    private static int getQuarterInMonth(int month, boolean isQuarterStart) {
        int months[] = {1, 4, 7, 10};
        if (!isQuarterStart) {
            months = new int[]{3, 6, 9, 12};
        }
        if (month >= 1 && month <= 3) {
            return months[0];
        } else if (month >= 4 && month <= 6) {
            return months[1];
        } else if (month >= 7 && month <= 9) {
            return months[2];
        } else {
            return months[3];
        }
    }

    /**
     * 获取当前系统时间所在年的第一日
     *
     * @return 当前系统时间所在年的第一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date firstDayOfYear() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 获取当前系统时间所在年的最后一日
     *
     * @return 当前系统时间所在年的最后一日
     * @author Wind.Zhao
     * @date 2014/08/19
     */
    public static Date lastDayOfYear() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.getActualMaximum(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }


    /**
     * 返回day_of_week 文字形式
     *
     * @param week  代表周几
     * @param style 风格 0-星期日 1-周日
     * @return
     * @author guwen
     */
    public static String getWeekStr(int week, int style) {
        String styleStr = "周";
        if (style == 0) {
            styleStr = "星期";
        }
        if (style == 1) {
            styleStr = "周";
        }
        String weekStr = "日";
        if (week == 0) {
            weekStr = "日";
        }
        if (week == 1) {
            weekStr = "一";
        }
        if (week == 2) {
            weekStr = "二";
        }
        if (week == 3) {
            weekStr = "三";
        }
        if (week == 4) {
            weekStr = "四";
        }
        if (week == 5) {
            weekStr = "五";
        }
        if (week == 6) {
            weekStr = "六";
        }
        return styleStr + weekStr;
    }

    /**
     * 获取两个时间间相差的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differDays(Date date1, Date date2) {
        //将转换的两个时间对象转换成Calendard对象
        Calendar can1 = Calendar.getInstance();
        can1.setTime(date1);
        Calendar can2 = Calendar.getInstance();
        can2.setTime(date2);
        //拿出两个年份
        int year1 = can1.get(Calendar.YEAR);
        int year2 = can2.get(Calendar.YEAR);
        //天数
        int days = 0;
        Calendar can = null;
        //如果can1 < can2
        //减去小的时间在这一年已经过了的天数
        //加上大的时间已过的天数
        if (can1.before(can2)) {
            days -= can1.get(Calendar.DAY_OF_YEAR);
            days += can2.get(Calendar.DAY_OF_YEAR);
            can = can1;
        } else {
            days -= can2.get(Calendar.DAY_OF_YEAR);
            days += can1.get(Calendar.DAY_OF_YEAR);
            can = can2;
        }
        for (int i = 0; i < Math.abs(year2 - year1); i++) {
            //获取小的时间当前年的总天数
            days += can.getActualMaximum(Calendar.DAY_OF_YEAR);
            //再计算下一年。
            can.add(Calendar.YEAR, 1);
        }
        System.out.println("天数差(自然天)：" + days);
        return days;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param start 时间参数 1 格式：1990-01-01 12:00:00
     * @param end   时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：xx天xx小时xx分xx秒
     */
    public static String getDistanceTime(Date start, Date end) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long time1 = start.getTime();
        long time2 = end.getTime();
        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (hour == 0 && day == 0) {
            return min + "分" + sec + "秒";
        } else if (day == 0) {
            return hour + "小时" + min + "分";
        } else if (day <= 30) {
            return day + "天" + hour + "小时";
        } else {
            return day + "天";
        }
    }


    public static boolean isSameHour(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameHour(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameHour(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6) && cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameDay(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameWeek(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameWeek(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameMonth(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameYear(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameYear(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static void main(String[] args) {
        System.out.println(newInstanceDateBegin());
        System.out.println(DateUtil.addDays(DateUtil.addSeconds(newInstanceDateEnd(), 1), -1));
        Date d = newInstanceDate();
        System.out.println(dateToString(d, DateFormatter.FORMAT_YYYY_MM_DD));

        System.out.println("=============================================");

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        System.out.println(dateToString(c.getTime(), DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        System.out.println(DateUtil.firstDayOfMonth());


        Date begindate = DateUtil.stringToDate("2018-05-16", "yyyy-MM-dd");
        Date enddate = DateUtil.stringToDate("2018-5-18", "yyyy-MM-dd");
        if (begindate.after(enddate)) {
            throw new AppWebException("截止日期应在开始日期之后。");
        }
        while (begindate.compareTo(enddate) <= 0) {
            System.out.println(begindate);
            begindate = DateUtil.addDays(begindate, 1);
        }
        System.out.println(newInstanceDateBegin().before(DateUtil.addMinutes(newInstanceDateBegin(), 1)));
        System.out.println(DateUtil.addDays(DateUtil.addSeconds(newInstanceDateEnd(), 1), -1).after(newInstanceDateBegin()));


        System.out.println(DateUtil.getHour(begindate));


        System.out.println(getWeekDay(new Date()));
        System.out.println(getWeekOfDate(DateUtil.addDays(new Date(), -1)));
    }
}
