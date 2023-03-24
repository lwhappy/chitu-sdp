package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableRelation;
import com.chitu.bigdata.sdp.api.vo.DatasourceMetatableInfo;
import com.chitu.bigdata.sdp.mapper.SdpMetaTableRelationMapper;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 599718
 * @date 2022-06-16 15:38
 */
@Service
@Slf4j
public class SdpMetaTableRelationService extends GenericService<SdpMetaTableRelation, Long> {
    public SdpMetaTableRelationService(@Autowired SdpMetaTableRelationMapper relationMapper) {
        super(relationMapper);
    }
    public SdpMetaTableRelationMapper getMapper() {
        return (SdpMetaTableRelationMapper) super.genericMapper;
    }

    public Object metaTableListByDataSource(SdpDataSourceBO datasourceBo) {
        try {
            PageHelper.startPage(datasourceBo.getPage(), datasourceBo.getPageSize());
            Page<DatasourceMetatableInfo> page=(Page) this.getMapper().metaTableListByDataSource(datasourceBo);
            Pagination pagination = Pagination.getInstance(datasourceBo.getPage(), datasourceBo.getPageSize());
            pagination.setRows(page);
            pagination.setRowTotal((int) page.getTotal());
            pagination.setPageTotal(page.getPages());
            return pagination;
        } catch (Exception e) {
            log.error("metaTableListByDataSource select error=",e);
            return null;
        }

    }
}
