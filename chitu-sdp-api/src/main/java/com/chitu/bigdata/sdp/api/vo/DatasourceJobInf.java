package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.model.SdpJob;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据源管理 列表 点作业数弹出作业列表
 * @author 599718
 * @date 2022-06-14 17:36
 */
@Data
public class DatasourceJobInf extends SdpJob implements Serializable {
    private  String projectName;
}
