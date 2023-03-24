package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.convert.Convert;
import com.chitu.cloud.utils.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.List;

/**
 *  版本号自动递增工具
 * @author chenyun
 * @date 2021/10/19 14:19
 */
public class VersionUtil {
    public static final String INIT_VERSION = "1.0";
    public static final String INIT_VERSION_PLUS = "v1";
    public static final int NUM_MAX = 10;
    public static final String VERSION_PRE = "v";
    public static final String VERSION_FORMAT = "v%s";
    /**
     * 升级版本号,格式：V1
     *
     * @param version
     * @return
     */
    public static String increaseVersion(String version) {
        if (StringUtils.isRealEmpty(version)) {
            return INIT_VERSION_PLUS;
        }
        String versionNum = org.apache.commons.lang3.StringUtils.substringAfter(version, VERSION_PRE);
        Integer versionInt = Convert.toInt(versionNum);
        String newVersion = String.format(VERSION_FORMAT, ++versionInt);
        return newVersion;
    }

    public static String fallbackVersion(String version) {
        if(INIT_VERSION_PLUS.equals(version)){
            return INIT_VERSION_PLUS;
        }
        String versionNum = org.apache.commons.lang3.StringUtils.substringAfter(version, VERSION_PRE);
        Integer versionInt = Convert.toInt(versionNum);
        String newVersion = String.format(VERSION_FORMAT, --versionInt);
        return newVersion;
    }
    /**
     * 升级版本号,格式：1.0
     *
     * @param versionNum
     * @return
     */
    public static String upgradeVersion(String versionNum) {
        if (StringUtils.isRealEmpty(versionNum)) {
            return INIT_VERSION;
        }
        List<String> verStr = Splitter.on(".").splitToList(versionNum);
        Integer[] nums = new Integer[verStr.size()];
        for (int i = 0; i < verStr.size(); i++) {
            nums[i] = Convert.toInt(verStr.get(i));
        }
        // 递归递增
        upgradeVersionNum(nums,nums.length-1);
        return Joiner.on(".").join(nums);
    }

    private static void upgradeVersionNum(Integer[] nums,int index) {
        if (index == 0) {
            nums[0] = nums[0] + 1;
        } else {
            int value = nums[index] + 1;
            if (value < NUM_MAX) {
                nums[index] = value;
            } else {
                nums[index] = 0;
                upgradeVersionNum(nums, index - 1);
            }
        }
    }
}
