package com.chitu.bigdata.sdp.interceptor;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Properties;

public class EnvInterceptorInit implements InitializingBean {

    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;

    private Environment environment;

    public EnvInterceptorInit(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        if(sqlSessionFactoryList != null && !sqlSessionFactoryList.isEmpty()) {
            MybatisInterceptor interceptor = new MybatisInterceptor(environment);
            Properties properties = new Properties();
            interceptor.setProperties(properties);
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                Configuration configuration = sqlSessionFactory.getConfiguration();
                configuration.addInterceptor(interceptor);
            }
        }
    }
}
