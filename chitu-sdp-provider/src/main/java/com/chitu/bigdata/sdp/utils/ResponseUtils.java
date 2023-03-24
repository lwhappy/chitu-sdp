
package com.chitu.bigdata.sdp.utils;

import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.AppCode;
import com.chitu.cloud.model.ResponseData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Response封装类
 *
 * @author: chenyun
 * @date: 2022/6/27
 * @version 1.0
 */
public class ResponseUtils {

    private static final String INTERFACE_RETURN_MSG_OK = "ok";
    private static final int SUCCESS_CODE = 0;

    /**
     * 构造 ResponseData 对象
     *
     * @return
     */
    private static ResponseData buildResponseData() {
        ResponseData resp = new ResponseData<>();
        resp.ok();
        return resp;
    }

    /**
     * 构造 ResponseData 对象，并设置data值
     *
     * @param data
     * @param <R>
     * @return
     */
    private static <R> ResponseData<R> buildResponseData(R data) {
        ResponseData<R> resp = new ResponseData<>();
        resp.setData(data).ok();
        return resp;
    }

    /**
     * 通过ApplicationException 构造相应结果
     * @param ex 业务异常
     * @param <T> 返回类型
     * @return
     */
    public static <T> ResponseData<T> buildResponseData(ApplicationException ex){
        AppCode appCode = ex.getAppCode();
        ResponseData<T> responseData = new ResponseData<>();
        responseData.setCode(appCode.getCode());
        responseData.setMsg(appCode.getMessage());
        return responseData;
    }

    /**
     * 是否失败(判断方式：返回对象为空或者code=0)
     * @param r ResponseData对象
     * @param <T>
     * @return
     */
    public static <T> boolean isFailure(ResponseData<T> r) {
        return r == null || r.getCode() != 0;
    }

    /**
     * 是否成功 ! 是否失败(判断方式：返回对象为空或者code=0)
     * @param r
     * @param <T>
     * @return
     */
    public static <T> boolean isSuccess(ResponseData<T> r) {
        return !isFailure(r);
    }

    /**
     * 是否成功（判断返回对象不为空且code=0,data如果是对象不为空，如果是集合、数组、map不为空算是成功）
     * @param r
     * @param <T>
     * @return
     */
    public static <T> boolean isSuccessWithValidData(ResponseData<T> r) {
        return !isFailureWithValidData(r);
    }

    @SuppressWarnings("rawtypes")
    public static <T> boolean isFailureWithValidData(ResponseData<T> r) {
        boolean basicValid = r == null || r.getCode() != 0 || r.getData() == null;
        if(!basicValid) {
            //说明data一定不为空
            T data = r.getData();
            if(data.getClass().isArray()) {
                //如果是数组
                return ArrayUtils.isEmpty((Object[])data);
            }else if(data instanceof Collection) {
                //如果是集合
                return CollectionUtils.isEmpty((Collection) data);
            }else if(data instanceof Map) {
                return MapUtils.isEmpty((Map) data);
            }
        }
        return basicValid;
    }

    public static <T> T getData(ResponseData<T> responseData) {
        if(isSuccessWithValidData(responseData)) {
            return responseData.getData();
        }
        return null;
    }

    public static <T> ResponseData<T> success(T t) {
        ResponseData<T> result = success();
        result.setData(t);
        return result;
    }

    public static <T> ResponseData<T> success(T t,String msg) {
        ResponseData<T> result = success(t);
        result.setMsg(msg);
        return result;
    }

    public static <T> ResponseData<T> success(T t,int code,String msg) {
        ResponseData<T> result = success(t);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> ResponseData<T> success() {
        ResponseData<T> result = new ResponseData<T>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(INTERFACE_RETURN_MSG_OK);
        result.ok();
        return result;
    }

    public static <T> ResponseData<T> failure(AppCode appCode) {
        ResponseData<T> resp = new ResponseData<>();
        resp.setCode(appCode.getCode());
        resp.setMsg(appCode.getMessage());
        return resp;
    }

    public static <T> ResponseData<T> buildResponseData(T t,int code,String msg) {
        ResponseData<T> result = success(t);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
