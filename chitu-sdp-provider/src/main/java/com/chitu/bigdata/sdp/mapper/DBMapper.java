package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.DBManageBO;
import com.chitu.bigdata.sdp.service.MySelectProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/10 21:38
 */
@Mapper
public interface DBMapper {

    @SelectProvider(type = MySelectProvider.class,method = "executeSql")
    List<Map> execute(DBManageBO dbManagerBO) throws Exception;

}
