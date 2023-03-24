package com.chitu.bigdata.sdp.service.validate;

/**
 * AbstractOperation
 *
 * @author wenmo
 * @since 2021/6/14 18:18
 */
public class AbstractOperation {

    protected String statement;

    public AbstractOperation() {
    }

    public AbstractOperation(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public boolean noExecute(){
        return true;
    }
}
