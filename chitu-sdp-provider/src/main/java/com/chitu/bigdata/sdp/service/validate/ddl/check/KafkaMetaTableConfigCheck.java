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
import org.apache.flink.sql.parser.ddl.SqlTableColumn;
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
public class KafkaMetaTableConfigCheck extends AbstractMetaTableConfigCheck {

    @Override
    public SqlExplainResult check(SdpMetaTableConfig sdpMetaTableConfig, SdpDataSource sdpDataSource) {
        try {
            List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(sdpMetaTableConfig.getFlinkDdl());
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



                SqlTableOption optTableName = keyOptMap.get("topic");
                SqlTableOption optTableName4Pattern = keyOptMap.get("topic-pattern");

                //1.topic名称校验
                if(Objects.isNull(optTableName) && Objects.isNull(optTableName4Pattern)){
                    sqlExplainResult = new SqlExplainResult();
                    sqlExplainResult.setError("[topic|topic-pattern]必须存在一个");
                    break;
                }

                if(Objects.nonNull(optTableName) && Objects.nonNull(optTableName4Pattern)){
                    sqlExplainResult = new SqlExplainResult();
                    sqlExplainResult.setError("[topic|topic-pattern]不能同时存在");
                    break;
                }

                //目前只校验单topic属性，不支持多个topic
                if (Objects.nonNull(optTableName) && !sdpMetaTableConfig.getMetaTableName().equals(optTableName.getValueString())){
                    sqlExplainResult = new SqlExplainResult();
                    //"输入框中的topic名称和ddl中的不一致，请确认"
                    sqlExplainResult.setError("表单中的topic和ddl中的topic名称不一致");
                    break;
                }

                //2.如果是canal-json的需要校验一下是否存在Array类型 'format' = 'canal-json',
                SqlTableOption format = keyOptMap.get("format");
                if(Objects.nonNull(format) && "canal-json".equalsIgnoreCase(format.getValueString())){
                    SqlNodeList columnList = sqlCreateTable.getColumnList();
                    for (SqlNode sqlNode : columnList) {
                        if(Objects.nonNull(sqlNode) && sqlNode instanceof SqlTableColumn.SqlRegularColumn){
                            SqlTableColumn.SqlRegularColumn sqlTableColumn = (SqlTableColumn.SqlRegularColumn) sqlNode;
                            String typeStr = Optional.ofNullable(sqlTableColumn).map(m -> sqlTableColumn.getType()).map(m1 -> m1.toString()).orElse("");
                            if(typeStr.trim().toLowerCase(Locale.ROOT).startsWith("array")){
                                sqlExplainResult = new SqlExplainResult();
                                sqlExplainResult.setError("当format格式为canal-json时，字段类型无法使用array，如果要使用array可以将format改为json");
                                break;
                            }
                        }
                    }
                }
            }
            return  sqlExplainResult;
        } catch (Exception e) {
           log.error("解析sql出错",e);
        }
        return null;
    }
}
