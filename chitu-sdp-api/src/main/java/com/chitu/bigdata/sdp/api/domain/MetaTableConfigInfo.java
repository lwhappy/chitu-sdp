

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-12-10
 * </pre>
 */

package com.chitu.bigdata.sdp.api.domain;

import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_meta_table_config
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MetaTableConfigInfo extends SdpMetaTableConfig {

   private String dataSourceType;

   private String dataBaseName;
}