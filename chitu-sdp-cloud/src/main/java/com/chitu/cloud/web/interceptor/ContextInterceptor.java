package com.chitu.cloud.web.interceptor;

import com.chitu.cloud.model.UserInfo;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import com.chitu.cloud.web.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author liheng
 * @since 1.0
 */
public class ContextInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(ContextInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean flag = true;

        Context context = new Context();
        context.setUser(initUserInfo(request));

        // 所有请求头原值保存
        ContextUtils.set(context);
        return flag;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        ContextUtils.unset();
    }

    private UserInfo initUserInfo(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(request.getHeader(UserInfo.getHeaderName(UserInfo.KEY_ID)));
        userInfo.setUsername(decode(request.getHeader(UserInfo.getHeaderName(UserInfo.KEY_USERNAME))));
        userInfo.setUserNumber(decode(request.getHeader(Constants.X_USERNUMBER)));
        userInfo.setUserNameEn(decode(request.getHeader(Constants.X_USERNAME)));
        userInfo.setNickname(userInfo.getUsername());
        return userInfo;
    }



    private String  decode(String str){
        if(StringUtils.isNotRealEmpty(str)){
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("strToCharset",e);
            }
        }
        return "";
    }


}