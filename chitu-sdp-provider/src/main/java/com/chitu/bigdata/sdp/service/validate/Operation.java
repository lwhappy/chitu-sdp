package com.chitu.bigdata.sdp.service.validate;


import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;

/**
 * Operation
 *
 * @author wenmo
 * @since 2021/6/13 19:24
 */
public interface Operation {

    String getHandle();

    Operation create(String statement);

    void build(CustomTableEnvironmentImpl stEnvironment);

    boolean noExecute();
}
