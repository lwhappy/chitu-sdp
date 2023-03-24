package com.chitu.bigdata.sdp.api.enums;


import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EnvironmentEnum {
    PROD("prod", "生产"),
    GRAY("gray", "灰度"),
    UAT("uat", "UAT"),
    STG("stg", "STG"),
    DEV("dev", "开发");

    private String code;
    private String desc;

    EnvironmentEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    public static List<String> ENVIRONMENT_LIST = new ArrayList<>(3);

    static {
        Arrays.stream(EnvironmentEnum.values()).forEach(environmentEnum -> ENVIRONMENT_LIST.add(environmentEnum.code));
    }

    public static EnvironmentEnum getEnvEnum(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        EnvironmentEnum[] environmentEnums = EnvironmentEnum.values();
        for (EnvironmentEnum environmentEnum : environmentEnums) {
            if (environmentEnum.code.equals(code.toLowerCase())) {
                return environmentEnum;
            }
        }
        return null;
    }
}
