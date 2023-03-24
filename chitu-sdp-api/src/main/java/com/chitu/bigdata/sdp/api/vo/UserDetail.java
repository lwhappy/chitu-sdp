package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author chenyun
 * @create 2021-10-12 11:01
 */
@Data
public class UserDetail {
    private String id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 在职状态，数据字典：hr_employee_in_office
     */
    private Integer inOffice;
    /**
     * 在职情况，数据字典：hr_employee_office_status
     */
    private Integer officeStatus;
    /**
     * 工作职务id
     */
    private Long dutyId;
    /**
     * 工作职务
     */
    private String duty;
    /**
     * 信息类别，数据字典：hr_employee_infomation_type
     */
    private Integer infomationType;
    /**
     * 用工单位，数据字典：hr_employing_unit
     */
    private String employingUnit;
    /**
     * 所属区域 10-华南 20-华北 30-华东 40-京津冀
     */
    private String employeeArea;
    /**
     * 所属部门id
     */
    private String departmentId;
    /**
     * 所属部门
     */
    private String department;
    /**
     * 上级部门id
     */
    private String higherDepartmentId;
    /**
     * 上级部门
     */
    private String higherDepartment;
    /**
     * 上级领导id
     */
    private String leaderId;
    /**
     * 上级领导
     */
    private String leader;
    /**
     * 员工工号
     */
    private String employeeNumber;
    /**
     * 所在城市
     */
    private String locatioinCity;
    /**
     * 私人手机
     */
    private String privateMobile;
    /**
     * 公司手机
     */
    private String companyMobile;
    /**
     * 短号
     */
    private String shortPhone;
    private String qq;
    /**
     * 职级编码
     */
    private String rankCode;
    private String rank;
    /**
     * 性别，数据字典：hr_common_sex
     */
    private String sex;
    private String email;
    /**
     * 座机号
     */
    private String phone;
    private String age;
    /**
     * 工龄
     */
    private String seniority;

    /**
     * 短号区域
     */
    private String shortPhoneArea;
    /**
     * 头像地址
     */
    private String photoUrl;
}
