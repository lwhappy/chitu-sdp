package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.DBManageBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/10 21:33
 */
@Slf4j
public class MySelectProvider {

    public String executeSql(DBManageBO dbManageBO) throws Exception{
        String sql = dbManageBO.getSql();
        if(StringUtils.isEmpty(sql)){
            throw new Exception("请明确输入要执行的SQL语句");
        }
        log.info("执行sql==="+sql);
        if(sql.contains(";")){
            sql = sql.replace(";","");
        }
        if(sql.contains("select") || sql.contains("SELECT")){
            if(!sql.contains("limit")){
                sql += " limit 50";
            }
        }
        return sql;
    }
}
