package com.chitu.cloud.utils;

/**
 * @author liheng
 * @since 1.0
 */
public class StringUtils {
    /**
     * 把applicationAbbr作为第一个Key，以"-"分隔
     *
     * @param keys
     * @return
     */
    public static String joinKeysByApplicationAbbr(String... keys) {
        String[] newKeys = new String[keys.length + 1];
        newKeys[0] = SpringUtils.applicationAbbr;
        System.arraycopy(keys, 0, newKeys, 1, keys.length);

        return String.join("-", newKeys);
    }

    /**
     * 判断字符串是否为空
     * 不去除首尾空格
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * 判断字符串是否为空
     * 去除首尾空格
     * @param s
     * @return
     */
    public static boolean isRealEmpty(String s) {
        boolean result = isEmpty(s);
        if (!result) {
            result = s.trim().length() == 0;
        }

        return result;
    }

    /**
     * 判断字符串是否为空
     * 去除首尾空格
     * @param s
     * @return
     */
    public static boolean isTrimedEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
    /**
     * 判断字符串是否非空
     * 不去除首尾空格
     * @param s
     * @return
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * 判断字符串是否非空
     * 去除首尾空格
     * @param s
     * @return
     */
    public static boolean isNotRealEmpty(String s) {
        return !isRealEmpty(s);
    }

    public static String defaultString(final String str, final String defaultStr) {
        return isTrimedEmpty(str) ? defaultStr : str.trim();
    }
}
