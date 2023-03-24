package com.chitu.bigdata.sdp.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenyun
 * @create 2021-12-13 11:01
 */
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static final String CHARSET_UTF_8 = "utf-8";


    public static final String CONTENT_TYPE_TEXT_HTML = "text/xml";


    public static final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";


    public static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";


    /**
     * 连接管理器
     */
    private static PoolingHttpClientConnectionManager pool;

    /**
     * 请求配置
     */
    private static RequestConfig requestConfig;

    static {

        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register(
                    "http", PlainConnectionSocketFactory.getSocketFactory()).register(
                    "https", sslsf).build();
            // 初始化连接管理器
            pool = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            // 将最大连接数增加到200，实际项目最好从配置文件中读取这个值
            pool.setMaxTotal(200);
            // 设置最大路由
            pool.setDefaultMaxPerRoute(2);
            // 根据默认超时限制初始化requestConfig
            int socketTimeout = 10000;
            int connectTimeout = 10000;
            int connectionRequestTimeout = 10000;
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).build();

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            logger.error("static method went error, error message is: {} ", e.getMessage(), e);
        }


        // 设置请求超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(50000)
                .setConnectionRequestTimeout(50000).build();
    }

    public static CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(pool)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();

        return httpClient;
    }

    /**
     * 发送Post请求
     *
     * @param httpPost
     * @return
     */
    private static String sendHttpPost(HttpPost httpPost) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    /**
     * 发送Get请求
     *
     * @param httpGet
     * @return
     */
    private static String sendHttpGet(HttpGet httpGet) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     */
    public static String sendHttpPost(String httpUrl) {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 get请求
     *
     * @param httpUrl
     */
    public static String sendHttpGet(String httpUrl) {
        // 创建get请求
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet);
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param params  参数(格式:key1=value1&key2=value2)
     */
    public static String sendHttpPost(String httpUrl, String params) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (params != null && params.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(params, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_FORM_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 post请求
     *
     * @param maps 参数
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps) {
        String param = convertStringParamter(maps);
        return sendHttpPost(httpUrl, param);
    }


    /**
     * 发送 post请求 发送json数据
     *
     * @param httpUrl    地址
     * @param paramsJson 参数(格式 json)
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson) {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        try {
            // 设置参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }


    public static String sendHttpPostJson(String httpUrl, String paramsJson, Map<String, String> headerMaps) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        if (headerMaps != null) {
//            for (String key : headerMaps.keySet()) {
//                httpPost.setHeader(key, headerMaps.get(key));
//            }
            Set<Map.Entry<String, String>> entrySet = headerMaps.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                httpPost.setHeader(key, value);
            }
        }
        try {
            // 设置参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }


    /**
     * 发送 post请求 发送xml数据
     *
     * @param httpUrl   地址
     * @param paramsXml 参数(格式 Xml)
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (paramsXml != null && paramsXml.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsXml, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_TEXT_HTML);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }


    /**
     * 将map集合的键值对转化成：key1=value1&key2=value2 的形式
     *
     * @param parameterMap 需要转化的键值对集合
     * @return 字符串
     */
    public static String convertStringParamter(Map<String, String> parameterMap) {
        StringBuffer parameterBuffer = new StringBuffer();
        if (parameterMap != null) {
            Set<Map.Entry<String, String>> entrySet = parameterMap.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String value = entry.getValue()==null?"":entry.getValue();
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }





    private static String tryGetIndexName(String message) {
        String index = null;
        try {
            if(message != null) {
                int startIndex = message.indexOf("Unknown index [");
                int endIndex = -1;
                if(startIndex != -1) {
                    startIndex += "Unknown index[".length() + 1;
                    endIndex = message.indexOf("]", startIndex);
                }
                if(endIndex != -1) {
                    index = message.substring(startIndex, endIndex);
                }
            }
        } catch (Throwable ignore) {}

        return index;
    }

    /**
     * 发送http请求，请求方式为PUT,数据是表单形式
     * @param httpUrl
     * @param maps
     * @return
     */
    public static String sendHttpPut(String httpUrl,Map<String, String> maps) {
        String param = convertStringParamter(maps);
        // 创建HttpPut
        HttpPut httpPut = new HttpPut(httpUrl);
        try {
            // 设置参数
            if (param != null && param.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(param, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_FORM_URL);
                httpPut.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPutRequest(httpPut);
    }

    /**
     * 发送Put请求
     *
     * @param httpPut
     * @return
     */
    private static String sendHttpPutRequest(HttpPut httpPut) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpPut.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPut);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode() || HttpStatus.SC_CREATED == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }
}