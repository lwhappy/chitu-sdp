package com.chitu.cloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.GenericModel;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.model.ResponseCode;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Service基础类，封装单表的insert/update/delete操作
 *
 * @author liheng
 * @since 1.0
 */
public abstract class GenericService<T, PK> extends ServiceImpl<GenericMapper<T, PK>,T> {
    protected static final Logger logger = LoggerFactory.getLogger(GenericService.class);
    protected GenericMapper<T, PK> genericMapper;

    public GenericService(GenericMapper<T, PK> genericMapper) {
        this.genericMapper = genericMapper;
    }

    /**
     * 插入数据
     *
     * 如果主键是基于DB的方式，数据插入成功后，主键值会自动填充到输入对象中
     *
     * @param data 数据
     * @return 返回操作记录数
     */
    @Transactional
    public int insert(T data) {
        int result = 0;
        try {
            setDefault(data, true);
            result = genericMapper.insert(data);
        } catch (Exception e) {
            logger.error(ResponseCode.INSERT_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.INSERT_EXCEPTION);
        }

        return result;
    }

    /**
     * 插入数据，忽略值为null的字段
     * @param data 数据
     * @return 返回操作记录数
     */
    @Transactional
    public int insertSelective(T data) {
        int result = 0;
        try {
            setDefault(data, true);
            result = genericMapper.insert(data);
        } catch (Exception e) {
            logger.error(ResponseCode.INSERT_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.INSERT_EXCEPTION);
        }

        return result;
    }

    /**
     * 批量插入数据
     * @param datas 数据
     * @return 返回操作记录数
     */
    @Transactional
    public int insertBatch(List<T> datas) {
        int result = 0;
        try {
            if (datas != null) {
                for (T data : datas) {
                    setDefault(data, true);
                    int k = genericMapper.insert(data);
                    result = result + k;
                }
            }
        } catch (Exception e) {
            logger.error(ResponseCode.INSERT_BATCH_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.INSERT_BATCH_EXCEPTION);
        }

        return result;
    }

    /**
     * 更新数据
     * 主键为更新条件，其他为数据
     * @param datas 数据
     * @return 更新结果行数
     */
    @Transactional
    public int update(T... datas) {
        int result = 0;
        if (datas != null) {
            try {
                for (T data : datas) {
                    setDefault(data, false);
                    if (updateById(data)) {
                        result++;
                    }
                }
            } catch (Exception e) {
                logger.error(ResponseCode.UPDATE_EXCEPTION.getMessage(), e);
                throw new ApplicationException(ResponseCode.UPDATE_EXCEPTION);
            }
        }

        return result;
    }

    /**
     * 更新数据，忽略空字段
     * 主键为更新条件，其他非null字段为数据
     * @param datas 数据
     * @return 第一个对象的update语句更新的行数
     */
    @Transactional
    public int updateSelective(T... datas) {
        return update(datas);
    }

    /**
     * !!! 注意, 该方法只能用于非分库分表数据源, 并且jdbc连接字符串要增加allowMultiQueries=true参数 !!!
     * 批量更新数据，忽略空字段
     * 主键为更新条件，其他非null字段为数据
     * @param datas 数据
     * @return 更新结果行数
     */
    @Transactional
    public int batchUpdateSelective(List<T> datas) {
        int result = 0;
        for (T data : datas) {
            result += update(data);
        }
        return result;
    }





    /**
     * 通过主键删除记录
     * @param ids  主键
     * @return    删除行数
     */
    @Transactional
    public int delete(PK... ids) {
        int result = 0;
        try {
            ArrayList<PK> pks = Lists.newArrayList(ids);
            result = genericMapper.deleteBatchIds(pks);
        } catch (Exception e) {
            logger.error(ResponseCode.DELETE_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.DELETE_EXCEPTION);
        }

        return result;
    }



    @Transactional
    public  int disable(Class<T> clazz,PK... ids) {
        int result = 0;
        try {
            T t = clazz.newInstance();
            Field enabledFlag = FieldUtils.getField(clazz, "enabledFlag", true);
            FieldUtils.writeField(enabledFlag,t,0L,true);

            QueryWrapper<T> updateWrapper = new QueryWrapper<>();
            updateWrapper.in("id",ids);

            result= genericMapper.update(t,updateWrapper);
        } catch (Exception e) {
            logger.error(ResponseCode.DELETE_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.DELETE_EXCEPTION);
        }

        return result;
    }

    /**
     * 通过主键使记录有效（相当于恢复逻辑删除）
     * @param ids  主键
     * @return    更新结果行数
     */
    @Transactional
    public int enable(Class<T> clazz,PK... ids) {
        int result = 0;
        try {
            T t = clazz.newInstance();
            Field enabledFlag = FieldUtils.getField(clazz, "enabledFlag", true);
            FieldUtils.writeField(enabledFlag,t,1L,true);

            QueryWrapper<T> updateWrapper = new QueryWrapper<>();
            updateWrapper.in("id",ids);

            result= genericMapper.update(t,updateWrapper);
        } catch (Exception e) {
            logger.error(ResponseCode.UPDATE_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.UPDATE_EXCEPTION);
        }

        return result;
    }



    /**
     * 通过主键获取数据
     * @param id  主键
     * @return    一行数据
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public T get(PK id) {
        T result = null;
        try {
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",id);
            result = genericMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            logger.error(ResponseCode.SELECT_ONE_EXCEPTION.getMessage(), e);
        }

        return result;
    }



    /**
     * 通过主键获取数据
     * @param ids  主键
     * @return List 如果无数据时，返回是长度为0的List对象
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<T> getByIds(PK... ids) {
        List<T> result = null;
        try {
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id",ids);
            result = genericMapper.selectList(queryWrapper);
        } catch (Exception e) {
            logger.error(ResponseCode.SELECT_ONE_EXCEPTION.getMessage(), e);
        }
        if (result == null) {
            result = new ArrayList<T>();
        }
        return result;
    }

    /**
     * 通过Model获取数据
     * @param data  Model数据，非null字段都做为条件查询
     * @return List 如果无数据时，返回是长度为0的List对象
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<T> selectAll(T data) {
        List<T> result = null;
        try {
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.setEntity(data);
            result = genericMapper.selectList(queryWrapper);
        } catch (Exception e) {
            logger.error(ResponseCode.SELECT_EXCEPTION.getMessage(), e);
        }

        if (result == null) {
            result = new ArrayList<T>();
        }
        return result;
    }

    protected void executePagination(PaginationCallback<T> callback, Pagination<T> pagination) {
        try {
            if(null != pagination) {
                PageHelper.startPage(pagination.getPage(), pagination.getPageSize(), pagination.isCount());
                List<T> pageResult = callback.execute(pagination);

                if (pagination.isCount()) {
                    Page page = (Page) pageResult;

                    pagination.setRowTotal((int) page.getTotal());
                    pagination.setPageTotal(page.getPages());
                }

                List<T> result = new ArrayList<T>();
                if (pageResult != null && pageResult.size() > 0) {
                    result.addAll(pageResult);
                }

                pagination.setRows(result);

            }
        } catch (Exception e) {
            logger.error(ResponseCode.SELECT_PAGINATION_EXCEPTION.getMessage(), e);
            throw new ApplicationException(ResponseCode.SELECT_PAGINATION_EXCEPTION);

        }
        finally {
            PageHelper.clearPage();
        }
    }



    /**
     * 设置添加公用参数
     *
     * @param data
     */
    private void setDefault(T data, boolean isNew) {
        if (data instanceof GenericModel) {
            GenericModel model = (GenericModel) data;
            Context context = ContextUtils.get();
            if (isNew) {
                model.setCreationDate(new Timestamp(System.currentTimeMillis()));
                if (context != null) {
                    if (!StringUtils.isRealEmpty(context.getUserId())) {
                        model.setCreatedBy(context.getUserId());
                    }else{
                        model.setCreatedBy("System");
                    }

                }
            }
            model.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            if (context != null) {
                if (!StringUtils.isRealEmpty(context.getUserId())) {
                    model.setUpdatedBy(context.getUserId());
                } else{
                    model.setUpdatedBy("System");
                }
            }
            if (model.getEnabledFlag() == null) {
                model.setEnabledFlag(1L);
            }
        }
    }

}