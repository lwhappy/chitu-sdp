

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-11-8
 * </pre>
 */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRuleBO;
import com.chitu.bigdata.sdp.api.enums.AlertGenerateRuleType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRule;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.config.DefaultAlertRuleConfigProperties;
import com.chitu.bigdata.sdp.mapper.SdpJobAlertRuleMapper;
import com.chitu.bigdata.sdp.utils.Assert;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * job告警规则业务类
 * </pre>
 */
@Service
@Slf4j
public class SdpJobAlertRuleService extends GenericService<SdpJobAlertRule, Long> {

    public SdpJobAlertRuleService(@Autowired SdpJobAlertRuleMapper sdpJobAlertRuleMapper) {
        super(sdpJobAlertRuleMapper);
    }

    public SdpJobAlertRuleMapper getMapper() {
        return (SdpJobAlertRuleMapper) super.genericMapper;
    }


    @Autowired
    private DefaultAlertRuleConfigProperties defaultAlertRuleConfigProperties;
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;


    public ResponseData queryList(SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        PageHelper.startPage(sdpJobAlertRuleBO.getPage(), sdpJobAlertRuleBO.getPageSize());
        List<SdpJobAlertRule> jobAlertRuleList = this.getMapper().queryList(sdpJobAlertRuleBO);
        PageInfo<SdpJobAlertRule> pageInfo = new PageInfo<>(jobAlertRuleList);
        ResponseData responseData = new ResponseData<>();
        responseData.setData(pageInfo).ok();
        return responseData;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseData addRule(SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        List<SdpJobAlertRule> jobAlertRuleList = this.selectAll(new SdpJobAlertRule(sdpJobAlertRuleBO.getJobId(), sdpJobAlertRuleBO.getRuleName()));
        Assert.isEmpty(jobAlertRuleList, ResponseCode.RULENAME_ALREADY_EXISTS);
        SdpJobAlertRule sdpJobAlertRule = new SdpJobAlertRule();
        BeanUtils.copyProperties(sdpJobAlertRuleBO, sdpJobAlertRule);
        sdpJobAlertRule.setRuleContent(JSONObject.toJSONString(sdpJobAlertRuleBO.getRuleContent()));
        this.insert(sdpJobAlertRule);
        ResponseData resp = new ResponseData<>();
        resp.ok();
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseData updateRule(SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        ResponseData resp = new ResponseData<>();
        SdpJobAlertRule sdpJobAlertRule = new SdpJobAlertRule();
        BeanUtils.copyProperties(sdpJobAlertRuleBO, sdpJobAlertRule);
        sdpJobAlertRule.setRuleContent(JSONObject.toJSONString(sdpJobAlertRuleBO.getRuleContent()));
        this.updateSelective(sdpJobAlertRule);
        resp.ok();
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseData delRule(SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        ResponseData resp = new ResponseData<>();
        SdpJobAlertRule sdpJobAlertRule = this.get(sdpJobAlertRuleBO.getId());
        sdpJobAlertRule.setEnabledFlag(0L);
        this.update(sdpJobAlertRule);
        resp.ok();
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDefaultRule(SdpJob job) {
        List<Map<String, String>> rules = defaultAlertRuleConfigProperties.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        //通知用户默认job创建人
        if (StrUtil.isEmpty(job.getCreatedBy())) {
            return;
        }
        SdpUser sdpUser = userService.get(Long.valueOf(job.getCreatedBy()));
        // 这里查不到可能性很低，为空就不创建告警规则
        if (sdpUser == null) {
            log.warn("job: {}, 创建人: {}, 在用户表中不存在，不添加告警规则！", job.getJobName(), job.getCreatedBy());
            return;
        }
        assembleAlertRules(job, rules, sdpUser);
    }

    private void assembleAlertRules(SdpJob job, List<Map<String, String>> rules, SdpUser sdpUser) {
        //通知用户默认job创建人
        List<Map> notifyUserList = new ArrayList<>();
        Map<String, String> map = new HashMap<>(3);
        map.put("employee_number", sdpUser.getEmployeeNumber());
        map.put("employee_name", sdpUser.getUserName());
        notifyUserList.add(map);

        List<SdpJobAlertRule> sdpJobAlertRules = new ArrayList<>();
        rules.forEach(item -> {
            SdpJobAlertRule sdpJobAlertRule = new SdpJobAlertRule();
            sdpJobAlertRule.setJobId(job.getId());
            sdpJobAlertRule.setRuleGenerateType(AlertGenerateRuleType.SYSTEM_AUTOMATIC.toString());
            sdpJobAlertRule.setNotifyUsers(JSONObject.toJSONString(notifyUserList));

            Set<Map.Entry<String,String>> entries = item.entrySet();
            Iterator<Map.Entry<String,String>> iterator = entries.iterator();
            RuleContent ruleContent = new RuleContent();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                if (key.equals("ruleName")){
                    sdpJobAlertRule.setRuleName(value);
                }else if(key.equals("ruleDesc")){
                    sdpJobAlertRule.setRuleDesc(value);
                }else if(key.equals("indexName")){
                    sdpJobAlertRule.setIndexName(value);
                }else if(key.equals("effectiveTime")){
                    sdpJobAlertRule.setEffectiveTime(value);
                }else if(key.equals("alertRate")){
                    sdpJobAlertRule.setAlertRate(Integer.valueOf(value));
                }else if(key.equals("effectiveState")){
                    sdpJobAlertRule.setEffectiveState(value);
                }else if(key.endsWith("P"+ job.getPriority())){
                    sdpJobAlertRule.setNotifiType(value.equals("")?null:value);
                }else if(key.equals("timeDiff")){
                    String timeDiff = value;
                    if (StrUtil.isNotEmpty(timeDiff)) {
                        ruleContent.setTimeDiff(Long.valueOf(timeDiff));
                    }
                }else if(key.equals("operator")){
                    ruleContent.setOperator(value);
                }else if(key.equals("threshold")){
                    ruleContent.setThreshold(value);
                }
            }
            sdpJobAlertRule.setRuleContent(JSONObject.toJSONString(ruleContent));
            sdpJobAlertRules.add(sdpJobAlertRule);
        });
        if (!CollectionUtils.isEmpty(sdpJobAlertRules)) {
            this.insertBatch(sdpJobAlertRules);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDefaultRule(SdpJob job, List<Map<String, String>> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        //通知用户默认job创建人
        if (StrUtil.isEmpty(job.getCreatedBy())) {
            return;
        }
        SdpUser sdpUser = userService.get(Long.valueOf(job.getCreatedBy()));
        // 这里查不到可能性很低，为空就不创建告警规则
        if (sdpUser == null) {
            log.warn("job: {}, 创建人: {}, 在用户表中不存在，不添加告警规则！", job.getJobName(), job.getCreatedBy());
            return;
        }
        assembleAlertRules(job, rules, sdpUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseData syncJobAlertRule() {
        // 查询所有job
        List<SdpJob> sdpJobs = jobService.selectAll(new SdpJob());
        sdpJobs.forEach(job -> {
            // 判断是否存在告警规则
            List<SdpJobAlertRule> sdpJobAlertRules = this.selectAll(new SdpJobAlertRule(job.getId()));
            List<Map<String, String>> defaultRules = defaultAlertRuleConfigProperties.getRules();
            // 同步所有默认规则
            if (CollectionUtil.isEmpty(sdpJobAlertRules)) {
                this.addDefaultRule(job);
            } else if (sdpJobAlertRules.size() < defaultRules.size()) {
                // 同步部分不存在的默认规则
                for (Map<String,String> defaultRule : defaultRules) {
                    List<String> indexNameList = sdpJobAlertRules.stream().map(SdpJobAlertRule::getIndexName).collect(Collectors.toList());
                    if (!indexNameList.contains(defaultRule.get("indexName"))) {
                        addDefaultRule(job, Arrays.asList(defaultRule));
                    }
                }
            }
        });
        ResponseData responseData = new ResponseData<>();
        responseData.setData("1").ok();
        return responseData;
    }
}