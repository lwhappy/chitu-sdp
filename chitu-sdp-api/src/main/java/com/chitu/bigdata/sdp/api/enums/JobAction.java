package com.chitu.bigdata.sdp.api.enums;

import java.util.HashMap;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/8 19:56
 */
public enum JobAction {
    START("启动"),
    STOP("停止"),
    PAUSE("暂停"),
    RECOVER("恢复"),
    ONLINE("上线"),
    ADD_SAVEPOINT("添加保存点")
    ;

    private String type;

    JobAction(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public static HashMap<String,String> getJobActionMap(){
        HashMap<String, String> map = new HashMap<>();
        for (JobAction value : JobAction.values()) {
            map.put(value.name(),value.type);
        }
        return map;
    }
}
