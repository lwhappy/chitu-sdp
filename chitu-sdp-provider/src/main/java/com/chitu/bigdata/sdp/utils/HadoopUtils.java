

package com.chitu.bigdata.sdp.utils;

import com.chitu.bigdata.sdp.api.flink.App;
import com.chitu.bigdata.sdp.api.flink.AppInfo;
import com.chitu.bigdata.sdp.api.flink.AppInfos;
import com.chitu.bigdata.sdp.flink.common.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 类功能描述
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/22 15:41
 */
@Slf4j
public class HadoopUtils {

    //通过yarn rest api获取任务信息
    public static AppInfo httpYarnAppInfo(String appId, String appName, String hadoopConfDir) throws Exception {
        String format = "%s/ws/v1/cluster/apps/%s";
        if (StringUtils.isNotBlank(appId)) {
            try {
                String url = String.format(format, com.chitu.bigdata.sdp.flink.common.util.HadoopUtils.getRMWebAppURL(true, hadoopConfDir), appId);
                return httpGetDoResult(url, AppInfo.class);
            } catch (Exception e) {
                log.warn(e.getMessage());
                String url = String.format(format, com.chitu.bigdata.sdp.flink.common.util.HadoopUtils.getRMWebAppURL(true, hadoopConfDir), appId);
                return httpGetDoResult(url, AppInfo.class);
            }
        } else {
            AppInfo result = null;
            //当appId为空，此时任务在yarn上为accepted状态
            String url = String.format(format, com.chitu.bigdata.sdp.flink.common.util.HadoopUtils.getRMWebAppURL(true, hadoopConfDir), "");
            if (StringUtils.isNotBlank(appName)) {
                AppInfos appInfos = httpGetDoResult(url, AppInfos.class);
                List<App> list1 = appInfos.getApps().getApp().stream().filter(x -> x.getName().equals(appName)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(list1)) {
                    App app = list1.stream().max((a, b) -> {
                        if (a.getStartedTime() > b.getStartedTime()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }).orElse(null);
                    if (app != null) {
                        result = new AppInfo();
                        result.setApp(app);
                    }
                }
            }
            return result;
        }
    }

    public static <T> T httpGetDoResult(String url, Class<T> clazz) throws Exception {
        String result = HttpClientUtils.httpGetRequest(url);
        if (result != null) {
            return JsonUtils.read(result, clazz);
        }
        return null;
    }

}

