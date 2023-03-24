package com.chitu.bigdata.sdp.service.validate.ddl;

import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 元表配置校验
 *
 * @author zouchangzhen
 * @date 2022/4/1
 */
public abstract class AbstractMetaTableConfigCheck {

    /**
     * 校验： 不同的数据源，可以有不一样的校验规则
     * @param sdpMetaTableConfig
     * @param sdpDataSource
     * @return
     */
    public abstract SqlExplainResult check(SdpMetaTableConfig sdpMetaTableConfig, SdpDataSource sdpDataSource);

    /**
     * 检查输入框的表名和ddl中的表名是否是一致
     * @param sqlNodes ddl解析出来的对象
     * @param sqlTableOptionKey 属性key
     * @param metaTableName 输入框的表名
     * @param errMsg 错误提示信息
     * @return
     */
    public static SqlExplainResult checkCreateTableName(List<SqlNode> sqlNodes,String sqlTableOptionKey,String metaTableName,String errMsg){
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

            SqlTableOption optTableName = keyOptMap.get(sqlTableOptionKey);
            if(Objects.isNull(optTableName)){
                sqlExplainResult = new SqlExplainResult();
                sqlExplainResult.setError("["+sqlTableOptionKey+"]选项不存在");
                break;
            }
            if (!metaTableName.equals(optTableName.getValueString())){
                sqlExplainResult = new SqlExplainResult();
                //"输入框中的topic名称和ddl中的不一致，请确认"
                sqlExplainResult.setError(errMsg);
                break;
            }
        }
        return  sqlExplainResult;
    }
}
