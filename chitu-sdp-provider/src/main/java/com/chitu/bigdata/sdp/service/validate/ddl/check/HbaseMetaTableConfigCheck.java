package com.chitu.bigdata.sdp.service.validate.ddl.check;

import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.service.validate.ddl.AbstractMetaTableConfigCheck;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/4/1
 */
@Component
@Slf4j
public class HbaseMetaTableConfigCheck extends AbstractMetaTableConfigCheck {

    @Override
    public SqlExplainResult check(SdpMetaTableConfig sdpMetaTableConfig, SdpDataSource sdpDataSource) {
        try {
            List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(sdpMetaTableConfig.getFlinkDdl());
            String tableName = String.format("%s:%s", sdpDataSource.getDatabaseName(), sdpMetaTableConfig.getMetaTableName());
            return  checkCreateTableName(sqlNodes,"table-name",tableName,"表单中的表名和ddl属性中的table-name名称不一致");
        } catch (Exception e) {
           log.error("解析sql出错",e);
        }
        return null;
    }
}
