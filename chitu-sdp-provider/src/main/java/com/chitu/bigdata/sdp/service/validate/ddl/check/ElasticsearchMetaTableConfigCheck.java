package com.chitu.bigdata.sdp.service.validate.ddl.check;

import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.service.validate.ddl.AbstractMetaTableConfigCheck;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zouchangzhen
 * @date 2022/4/1
 */
@Component
@Slf4j
public class ElasticsearchMetaTableConfigCheck extends AbstractMetaTableConfigCheck {

    @Override
    public SqlExplainResult check(SdpMetaTableConfig sdpMetaTableConfig, SdpDataSource sdpDataSource) {
        try {
            List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(sdpMetaTableConfig.getFlinkDdl());
              //checkCreateTableName(sqlNodes,"index",sdpMetaTableConfig.getMetaTableName(),"表单中的表名和ddl属性中的index名称不一致");
            if(CollectionUtils.isEmpty(sqlNodes)){
                return null;
            }
            SqlExplainResult sqlExplainResult = null;

            for (SqlNode sqlNode1 : sqlNodes) {
                if(!(sqlNode1 instanceof SqlCreateTable)){
                    continue;
                }
                //表
                SqlCreateTable sqlCreateTable = (SqlCreateTable)sqlNode1;
                //属性
                SqlNodeList propertyList = sqlCreateTable.getPropertyList();
                List<SqlNode> sqlNodeList = propertyList.getList();

                Map<String, SqlTableOption> keyOptMap = Optional.ofNullable(sqlNodeList).orElse(new ArrayList<>()).stream().filter(f -> Objects.nonNull(f) && f instanceof SqlTableOption)
                        .map(m -> (SqlTableOption) m)
                        .collect(Collectors.toMap(SqlTableOption::getKeyString, m -> m, (k1, k2) -> k2));

                SqlTableOption optTableName = keyOptMap.get("index");
                if(Objects.isNull(optTableName)){
                    sqlExplainResult = new SqlExplainResult();
                    sqlExplainResult.setError("[index]选项不存在");
                    break;
                }
                if (!optTableName.getValueString().startsWith(sdpMetaTableConfig.getMetaTableName())){
                    //索引的只是匹配前缀就可以，因为有写配置的是分索引
                    sqlExplainResult = new SqlExplainResult();
                    //"输入框中的topic名称和ddl中的不一致，请确认"
                    sqlExplainResult.setError("表单中的表名和ddl中的index名称不匹配[匹配前缀]");
                    break;
                }
            }

            return sqlExplainResult;
        } catch (Exception e) {
           log.error("解析sql出错",e);
        }
        return null;
    }
}
