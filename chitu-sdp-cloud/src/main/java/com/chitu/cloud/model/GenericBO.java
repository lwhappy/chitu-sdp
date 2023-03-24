package com.chitu.cloud.model;

import java.util.List;

/**
 * 业务传输对象
 * @author liheng
 * @since 1.0
 */
public class GenericBO<T> {
    /**
     * 新增
     */
    private List<T> addList;
    /**
     * 修改
     */
    private List<T>  updateList;
    /**
     * 启用
     */
    private List<T>  enableList;
    /**
     *失效
     */
    private List<T>   disableList;
    /**
     * 删除数据
     */
    private List<T>   deleteList;



    /**
     * 通用导出功能使用
     * 菜单ID
     */
    private String menuId;

    /**
     * 通用查询编码
     */
    private String genericSearchCode;

    /**
     * 通用导出功能使用
     * 查询编码
     */
    private String searchCode;

    private T vo;

    private String id;

    private String[] ids;

    private int page;  //页码

    private int pageSize = 10; //每页记录数

    private String elasticsearchFlag = "N"; // 索引标识

    private String countFlag = "N"; // 查询数量标识

    /**
     * 排序对象
     */
    private List<OrderByClause> orderByClauses;


    /**
     * 包含规则
     */
    private String[] includes;

    /**
     * 排除规则
     */
    private String[] excludes;

    public T getVo() {
        return vo;
    }

    public void setVo(T vo) {
        this.vo = vo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public List<T> getAddList() {
        return addList;
    }

    public void setAddList(List<T> addList) {
        this.addList = addList;
    }

    public List<T> getUpdateList() {
        return updateList;
    }

    public void setUpdateList(List<T> updateList) {
        this.updateList = updateList;
    }

    public List<T> getEnableList() {
        return enableList;
    }

    public void setEnableList(List<T> enableList) {
        this.enableList = enableList;
    }

    public List<T> getDisableList() {
        return disableList;
    }

    public void setDisableList(List<T> disableList) {
        this.disableList = disableList;
    }

    public List<T> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<T> deleteList) {
        this.deleteList = deleteList;
    }



    public String getElasticsearchFlag() {
        return elasticsearchFlag;
    }

    public void setElasticsearchFlag(String elasticsearchFlag) {
        this.elasticsearchFlag = elasticsearchFlag;
    }

    public String getCountFlag() {
        return countFlag;
    }

    public void setCountFlag(String countFlag) {
        this.countFlag = countFlag;
    }

    public String getMenuId() {
        return menuId;
    }

    public GenericBO<T> setMenuId(String menuId) {
        this.menuId = menuId;
        return this;
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public String getSearchCode() {
        return searchCode;
    }

    public GenericBO<T> setSearchCode(String searchCode) {
        this.searchCode = searchCode;
        return this;
    }

    public List<OrderByClause> getOrderByClauses() {
        return orderByClauses;
    }

    public void setOrderByClauses(List<OrderByClause> orderByClauses) {
        this.orderByClauses = orderByClauses;
    }

    public String getGenericSearchCode() {
        return genericSearchCode;
    }

    public GenericBO<T> setGenericSearchCode(String genericSearchCode) {
        this.genericSearchCode = genericSearchCode;
        return this;
    }
}