package com.chitu.bigdata.sdp.service.validate.parser;

import com.chitu.bigdata.sdp.service.validate.domain.SqlSegment;

/**
 * SetSqlParser
 *
 * @author wenmo
 * @since 2021/10/21 18:41
 **/
public class SetSqlParser extends BaseSingleSqlParser {

    public SetSqlParser(String originalSql) {
        super(originalSql);
    }

    @Override
    protected void initializeSegments() {
        //SET(\s+(\S+)\s*=(.*))?
        segments.add(new SqlSegment("(set)\\s+(.+)(\\s*=)", "[.]"));
        segments.add(new SqlSegment("(=)\\s*(.*)( ENDOFSQL)", ","));
    }
}
