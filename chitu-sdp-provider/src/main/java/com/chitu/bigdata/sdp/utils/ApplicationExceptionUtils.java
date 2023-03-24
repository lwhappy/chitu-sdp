package com.chitu.bigdata.sdp.utils;

import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ApplicationExceptionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionUtils.class);

    public static ApplicationException getApplicationException(final Throwable e) {
        Set<Throwable> set = new HashSet();

        for(Throwable t = e; t != null; t = t.getCause()) {
            if (set.contains(t)) {
                return null;
            }

            set.add(t);
            if (t instanceof ApplicationException) {
                return (ApplicationException)t;
            }
        }

        return null;
    }

    public static ApplicationException newUnknownException(final Throwable exception) {
        ApplicationException ae = getApplicationException(exception);
        if (ae != null) {
            return ae;
        } else {
            LOGGER.error(exception.getMessage(), exception);
            return newUnknownException(exception.getMessage());
        }
    }

    public static ApplicationException newUnknownException(final String message) {
        ResponseCode code = ResponseCode.UNKOWN_EXCEPTION;
        return new ApplicationException(code.getCode(), (String) StringUtils.defaultIfEmpty(message, code.getMessage()));
    }

    private ApplicationExceptionUtils() {
    }
}
