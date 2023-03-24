package com.chitu.cloud.service;

import com.chitu.cloud.model.Pagination;

import java.util.List;

/**
 * Created by henry on 2017/1/31.
 */
public interface PaginationCallback<T> {
    public List<T> execute(Pagination<T> pagination);
}
