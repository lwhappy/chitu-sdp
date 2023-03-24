package com.chitu.bigdata.sdp.utils;

import com.xiaoleilu.hutool.date.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author sutao
 * @create 2021-11-16 20:44
 */
public class DateUtils {

    public final static  String YYYYMMDD = "yyyyMMdd";
    public final static  String YYYYMMDDHHMM = "yyyyMMddHHmm";
    public final static  String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTimeStr   当前时间
     * @param startTimeStr 开始时间
     * @param endTimeStr   结束时间
     * @return
     */
    public static boolean isEffectiveDate(String nowTimeStr, String startTimeStr, String endTimeStr) {

        try {
            String format = "HH:mm";
            Date nowTime = new SimpleDateFormat(format).parse(nowTimeStr);
            Date startTime = new SimpleDateFormat(format).parse(startTimeStr);
            Date endTime = new SimpleDateFormat(format).parse(endTimeStr);

            if (nowTime.getTime() == startTime.getTime()
                    || nowTime.getTime() == endTime.getTime()) {
                return true;
            }

            Calendar date = Calendar.getInstance();
            date.setTime(nowTime);

            Calendar begin = Calendar.getInstance();
            begin.setTime(startTime);

            Calendar end = Calendar.getInstance();
            end.setTime(endTime);

            if (date.after(begin) && date.before(end)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取当前系统时间的小时分钟 HH:mm
     * @return
     */
    public static String getCurrentDateHM() {
        return DateUtil.formatDateTime(new Date()).substring(11, 16);
    }

    /**
     * 转化为 几天几时几分几秒
     * @param totalTime 单位秒
     * @return
     */
    public static String dhms(Long totalTime) {
        if(Objects.isNull(totalTime) || totalTime < 0 ){
            return null;
        }

        long day = totalTime / (60 * 60 * 24);
        long hour = (totalTime % (60 * 60 * 24)) / (60 * 60);
        long minute = (totalTime % (60 * 60)) / (60);
        long second = (totalTime % (60));
        return String.format("%s天%s时%s分%s秒",day,hour,minute,second);
    }




}
