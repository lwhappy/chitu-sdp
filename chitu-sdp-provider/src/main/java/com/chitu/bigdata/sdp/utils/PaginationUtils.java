package com.chitu.bigdata.sdp.utils;

import com.chitu.cloud.model.GenericBO;
import com.chitu.cloud.model.Pagination;

public class PaginationUtils {
    public static <T> Pagination<T> getInstance4BO(GenericBO<T> genericBO) {
        Pagination<T> pagination = new Pagination(genericBO.getPage(), genericBO.getPageSize(), true);
        pagination.setCriteria(genericBO);
        return pagination;
    }

    private PaginationUtils() {
    }
}
