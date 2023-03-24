package com.chitu.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * DAO基础类，封装单表的insert/update/delete操作
 * @author liheng
 * @since 1.0
 */
public interface GenericMapper<T, PK> extends BaseMapper<T> {


}