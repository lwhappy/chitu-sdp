package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/10 21:34
 */
@Data
public class DBManageVO {
    private Set<String> columnList;
    private List<List<Object>> rows;
    private int effectRow;
}
