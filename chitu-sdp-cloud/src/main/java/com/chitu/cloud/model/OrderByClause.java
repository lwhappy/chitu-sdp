package com.chitu.cloud.model;

import java.io.Serializable;

/**
 * 排序对象
 */
public class OrderByClause implements Serializable{
    /**
     * 排序字段
     */
    private String field;
    /**
     * 0 代表升序
     * 否则代表降序
     */
    private int orderByMode = 0;

    public static final String REGEX = "[\\w|-|_|.]*";

    public String getField() {
        return field;
    }

    public OrderByClause setField(String field) {
        if(!field.matches(REGEX)){
            throw new RuntimeException("非法操作");
        }
        this.field = field;
        return this;
    }

    public int getOrderByMode() {
        return orderByMode;
    }

    public OrderByClause setOrderByMode(int orderByMode) {
        this.orderByMode = orderByMode;
        return this;
    }
}
