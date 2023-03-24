package com.chitu.bigdata.sdp.service.validate.ddl.check;

import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.constant.KuduConstant;
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
public class KuduMetaTableConfigCheck extends AbstractMetaTableConfigCheck {

    @Override
    public SqlExplainResult check(SdpMetaTableConfig sdpMetaTableConfig, SdpDataSource sdpDataSource) {
        try {
            List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(sdpMetaTableConfig.getFlinkDdl());
            String tableName = KuduConstant.getKuduTableName(sdpDataSource.getDatabaseName(),sdpMetaTableConfig.getMetaTableName());
            return  checkCreateTableName(sqlNodes,"kudu.table",tableName,"表单中的表名和ddl属性中的kudu.table名称不一致");
        } catch (Exception e) {
           log.error("解析sql出错",e);
        }
        return null;
    }
}
