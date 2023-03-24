package com.chitu.bigdata.sdp.service.validate.domain;


/**
 * RunTimeException
 *
 * @author wenmo
 * @since 2021/6/27
 **/
public class RunTimeException extends RuntimeException {

    public RunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunTimeException(String message) {
        super(message);
    }
}