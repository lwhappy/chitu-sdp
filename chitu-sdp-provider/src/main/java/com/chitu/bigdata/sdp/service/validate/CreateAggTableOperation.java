package com.chitu.bigdata.sdp.service.validate;


import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import org.apache.flink.table.api.Table;

import java.util.List;

/**
 * CreateAggTableOperation
 *
 * @author wenmo
 * @since 2021/6/13 19:24
 */
public class CreateAggTableOperation extends AbstractOperation implements Operation{

    private String KEY_WORD = "CREATE AGGTABLE";

    public CreateAggTableOperation() {
    }

    public CreateAggTableOperation(String statement) {
        super(statement);
    }

    @Override
    public String getHandle() {
        return KEY_WORD;
    }

    @Override
    public Operation create(String statement) {
        return new CreateAggTableOperation(statement);
    }

    @Override
    public void build(CustomTableEnvironmentImpl stEnvironment) {
        AggTable aggTable = AggTable.build(statement);
        Table source = stEnvironment.sqlQuery("select * from "+ aggTable.getTable());
        List<String> wheres = aggTable.getWheres();
        if(wheres!=null&&wheres.size()>0) {
            for (String s : wheres) {
                source = source.filter(s);
            }
        }
        Table sink = source.groupBy(aggTable.getGroupBy())
                .flatAggregate(aggTable.getAggBy())
                .select(aggTable.getColumns());
        stEnvironment.registerTable(aggTable.getName(), sink);
    }
}
