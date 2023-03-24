package com.chitu.bigdata.sdp.aspect;

import com.chitu.cloud.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/1 21:45
 * 全局异常处理，拦截入参校验注解返回的消息，进行有效精简消息内容
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseData MyExceptionHandle(MethodArgumentNotValidException exception){
        exception.printStackTrace();
        BindingResult result = exception.getBindingResult();
        StringBuilder errorMsg = new StringBuilder();
        if (result.hasErrors()) {
            List<FieldError> fieldErrors = result.getFieldErrors();
            fieldErrors.forEach(error -> {
                log.info("field" + error.getField() + ", msg:" + error.getDefaultMessage());
                errorMsg.append(error.getDefaultMessage()).append("!");
            });
        }
        exception.printStackTrace();
        return new ResponseData(-1,errorMsg.toString() );
    }
}
