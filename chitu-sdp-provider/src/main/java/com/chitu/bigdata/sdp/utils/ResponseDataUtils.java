package com.chitu.bigdata.sdp.utils;

import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.AppCode;
import com.chitu.cloud.model.ResponseCode;
import com.chitu.cloud.model.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseDataUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseDataUtils.class);

    public static <T> ResponseData<T> newResponseData(final Throwable e) {
        ResponseData<T> responseData = new ResponseData();
        return setThrowable(responseData, e);
    }

    public static <T extends ResponseData<E>, E> T setThrowable(final T responseData, final Throwable e) {
        ApplicationException ae = ApplicationExceptionUtils.getApplicationException(e);
        if (ae != null) {
            AppCode appCode = ae.getAppCode();
            responseData.setCode(appCode.getCode());
            responseData.setMsg(appCode.getMessage());
            return responseData;
        } else {
            LOGGER.error(e.getMessage(), e);
            ResponseCode code = ResponseCode.UNKOWN_EXCEPTION;
            responseData.setCode(code.getCode());
            responseData.setMsg((String) StringUtils.defaultIfEmpty(e.getMessage(), code.getMessage()));
            return responseData;
        }
    }

    public static <T> ResponseData<T> newResponseData(final T data) {
        ResponseData<T> responseData = new ResponseData();
        responseData.setData(data);
        responseData.ok();
        return responseData;
    }

    public static <T> T getData(ResponseData<T> responseData) {
        if (responseData == null) {
            throw new ApplicationException(ResponseCode.UNKOWN_EXCEPTION);
        } else if (responseData.isSuccess()) {
            return responseData.getData();
        } else {
            throw new ApplicationException(responseData.getCode(), responseData.getMsg());
        }
    }

    private ResponseDataUtils() {
    }
}
