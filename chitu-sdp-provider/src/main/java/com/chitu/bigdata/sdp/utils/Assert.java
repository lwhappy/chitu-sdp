package com.chitu.bigdata.sdp.utils;

import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.cloud.exception.ApplicationException;
import com.xiaoleilu.hutool.util.CollectionUtil;

import java.util.Collection;

/**
 * @author sutao
 * @create 2022-03-11 11:43
 */
public class Assert {


    /**
     * 断言参数不为空
     * 如果为空，则抛出异常
     *
     * @param collection
     * @param responseCode
     */
    public static void notEmpty(Collection<?> collection, ResponseCode responseCode) {
        if (CollectionUtil.isEmpty(collection)) {
            throw new ApplicationException(responseCode);
        }
    }

    /**
     * 断言参数为空
     * 如果不为空，则抛出异常
     *
     * @param collection
     * @param responseCode
     */
    public static void isEmpty(Collection<?> collection, ResponseCode responseCode) {
        if (CollectionUtil.isNotEmpty(collection)) {
            throw new ApplicationException(responseCode);
        }
    }


    /**
     * 断言表达式为真
     * 如果不为真，则抛出异常
     *
     * @param expression 是否成功
     */
    public static void isTrue(boolean expression, ResponseCode responseCode) {
        if (!expression) {
            throw new ApplicationException(responseCode);
        }
    }

    public static void isTrue(boolean expression, ResponseCode responseCode, Object... msgArgs) {
        if (!expression) {
            throw new ApplicationException(responseCode, msgArgs);
        }
    }


    /**
     * 断言对象不为空
     * obj 为空则抛异常
     *
     * @param obj
     * @param responseCode
     */
    public static void notNull(Object obj, ResponseCode responseCode) {
        if (obj == null) {
            throw new ApplicationException(responseCode);
        }
    }

    /**
     * 断言对象不为空
     * obj 为空则抛异常
     *
     * @param obj
     * @param responseCode
     */
    public static void notNull(Object obj, ResponseCode responseCode, Object... msgArgs) {
        if (obj == null) {
            throw new ApplicationException(responseCode, msgArgs);
        }
    }

}
