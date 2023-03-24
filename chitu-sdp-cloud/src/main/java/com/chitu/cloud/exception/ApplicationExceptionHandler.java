package com.chitu.cloud.exception;

import com.alibaba.fastjson.JSON;
import com.chitu.cloud.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Map;

/**
 * Created by liheng on 2017/7/22.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseData<String> baseErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        ResponseData<String> result;

        try {
            logErrorInfo(req, e);
        } catch (Throwable t) {
            logger.error("", t);
        }

        if (ApplicationException.class.isAssignableFrom(e.getClass())) {
            ApplicationException applicationException = (ApplicationException) e;
            result = new ResponseData<>(applicationException.getAppCode());
            result.setMsg(applicationException.getMessage());
        } else {
            result = new ResponseData<>();
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 打印错误信息
     * @param req
     * @param e
     */
    private void logErrorInfo(HttpServletRequest req, Exception e) {
        String params = "";
        String body = "";

        try {

            Map<String, String[]> parameterMap = req.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                params = JSON.toJSONString(parameterMap);
            }

            try (BufferedReader reader = req.getReader()) {
                StringBuilder bodyContent = new StringBuilder();
                String str = null;
                while ((str = reader.readLine()) != null) {
                    bodyContent.append(str);
                }
                body = bodyContent.toString();
            }
        } catch (Exception ignore) {}

        String paramMessage = String.format("Exception Handler：Host %s invokes url %s ERROR: %s params: %s body: %s",
                req.getRemoteHost(), req.getRequestURL(), e.getMessage(), params, body);
        String errorMessage = "process is error!";

        logger.error(paramMessage);
        logger.error(errorMessage, e);

    }

}