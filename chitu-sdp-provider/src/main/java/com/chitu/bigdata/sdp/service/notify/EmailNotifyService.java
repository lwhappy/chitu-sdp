package com.chitu.bigdata.sdp.service.notify;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RefreshScope
public class EmailNotifyService {

    @Value("${alert.email.emailHostName}")
    private String emailHostName;

    @Value("${alert.email.senderEmail}")
    private String senderEmail;

    @Value("${alert.email.senderName}")
    private String senderName;

    @Value("${alert.email.senderPassword}")
    private String senderPassword;

    public boolean sendMsg(List<String> employeeEmailList,String subject,String contentJsonStr){
        if(CollectionUtils.isEmpty(employeeEmailList)){
            return true;
        }
        List<String> emailCollect = employeeEmailList.stream().filter(obj -> StrUtil.isNotBlank(obj)).collect(Collectors.toList());
        String resMsgId = "";
        try {
            SimpleEmail email = new SimpleEmail();
            // 这里是SMTP发送服务器的名字：163的如下："smtp.163.com"
//            email.setHostName("smtp.163.com");
            email.setHostName(emailHostName);
//            email.setHostName("smtp.exmail.qq.com");
            // 字符编码集的设置
            email.setCharset("UTF-8");
            // 收件人的邮箱
            email.addTo(emailCollect.toArray(new String[emailCollect.size()]));
            // 发送人的邮箱以及发件人名称
//            email.setFrom("aabbcc1235789@163.com", "赤兔实时平台");
            email.setFrom(senderEmail, senderName);
            // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码
//            email.setAuthentication("aabbcc1235789@163.com", "UVHNBAWMLXIIBLOC");
            email.setAuthentication(senderEmail, senderPassword);
            // 要发送的邮件主题
            email.setSubject(subject);
            // 要发送的信息，可以使用text、HtmlEmail等
            email.setMsg(contentJsonStr);
            resMsgId = email.send();
        } catch (Exception e) {
            log.error("调用邮箱接口异常", e);
        }
        if(StrUtil.isBlank(resMsgId)){
            log.info("发送邮箱失败:{}",emailCollect);
            return false;
        }
        log.info("发送邮箱成功:{}",emailCollect);
        return true;
    }

}
