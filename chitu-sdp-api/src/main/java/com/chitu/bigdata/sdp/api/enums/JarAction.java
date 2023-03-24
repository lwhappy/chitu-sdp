package com.chitu.bigdata.sdp.api.enums;

import java.util.HashMap;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/8 19:56
 */
public enum JarAction {
    ADD("新增JAR"),
    UPDATE("上传新版本"),
    EDIT("编辑历史JAR");

    private String type;

    JarAction(String type) {
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
        for (JarAction value : JarAction.values()) {
            map.put(value.name(),value.type);
        }
        return map;
    }
}
