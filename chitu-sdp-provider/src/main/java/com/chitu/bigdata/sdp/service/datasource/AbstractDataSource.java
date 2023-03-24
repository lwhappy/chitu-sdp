package com.chitu.bigdata.sdp.service.datasource;

import cn.hutool.core.util.ReUtil;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.domain.SourceMeta;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.utils.StrUtils;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlTableOption;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author sutao
 * @create 2021-12-09 17:16
 */
public abstract class AbstractDataSource<T> {

    /**
     * 获取连接
     * @param connectInfo
     * @return
     * @throws Exception
     */
    public abstract T getConnection(ConnectInfo connectInfo) throws Exception;

    /**
     * 关闭连接
     * @param t
     */
    public abstract void closeConnection(T t);

    /**
     * 表名是否存在
     * @param connectInfo
     * @param tableName
     * @return
     * @throws Exception
     */
    public abstract boolean tableExists(ConnectInfo connectInfo, String tableName) throws Exception;

    /**
     * 获取所有表名
     * @param connectInfo
     * @return
     * @throws Exception
     */
    public abstract List<String> getTables(ConnectInfo connectInfo) throws Exception;

    /**
     * 获取表字段信息
     * @param connectInfo
     * @param tableName
     * @return
     * @throws Exception
     */
    public abstract List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) throws Exception;

    /**
     * 生成flink sql模板ddl
     * @param flinkTableGenerate
     * @return
     */
    public abstract String generateDdl(FlinkTableGenerate flinkTableGenerate);

    /**
     * 替换flink sql连接参数加密key值
     * @param options
     * @param sdpDataSource
     * @return
     */
    public abstract Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource);


    /**
     * 对数据源表进行初始化操作，包括清空数据
     * @param meta
     * @return
     */
    public abstract void initTable(SourceMeta meta);


    /**
     * 转环境 修改选项参数
     */
    public abstract String modifyOption4ChangeEnv(String ddl,SdpDataSource sdpDataSource) throws Exception;


    /**
     * 删除选项，返回下标位置
     *
     * @param options
     * @param optionKey
     * @return
     */
    public static int removeOption(List<SqlNode> options, String optionKey) {
        int location = -1;
        if (CollectionUtils.isEmpty(options)) {
            return location;
        }
        int index = -1;
        final Iterator<SqlNode> each = options.iterator();
        while (each.hasNext()) {
            ++index;
            SqlTableOption next = (SqlTableOption) each.next();
            if (Objects.nonNull(next) && next.getKeyString().equals(optionKey)) {
                each.remove();
                location = index;
            }
        }
        return location;
    }

    /**
     * 按照下标写回去
     *
     * @param options
     * @param location
     * @param sqlTableOption
     * @return
     */
    public static  List<SqlNode> insertOption(List<SqlNode> options, int location, SqlTableOption sqlTableOption) {
        if (CollectionUtils.isEmpty(options)) {
            return options;
        }
        if (location >= 0) {
            options.add(location, sqlTableOption);
        }
        return options;
    }

    /**
     * 避免去掉字段的注释,options中注释就没有办法保留了
     * @param ddl
     * @param options
     * @return
     */
    public static StringBuilder flinkSqlBuilder(String ddl, List<SqlNode> options) {
        String keyword = ReUtil.getGroup1(".*(\\s*WITH\\s*\\().*", ddl);
        int index = ddl.lastIndexOf(keyword);
        String prefix = ddl.substring(0, index + keyword.length());
        StringBuilder modifySql = new StringBuilder(prefix);

        modifySql.append(StrUtils.LF);
        String kv = "'%s' = '%s'";
        for (int j = 0, xlen = options.size(); j < xlen; j++) {
            SqlTableOption sqlTableOption = (SqlTableOption) options.get(j);
            if (Objects.isNull(sqlTableOption)) {
                continue;
            }
            if (j == xlen - 1) {
                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.LF);
            } else {
                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.DLF);
            }
        }
        modifySql.append(");");
        return modifySql;
    }

}
