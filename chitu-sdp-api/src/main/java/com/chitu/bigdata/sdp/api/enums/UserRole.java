package com.chitu.bigdata.sdp.api.enums;
/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/12 19:28
 */
public interface UserRole {
    /**
     * 系统用户类型
     * 1：系统管理员
     * 0：普通用户
     */
    enum ManagerEnum{

         PLATFORM_ADMIN(1),
         COMMON_USER(0);

        ManagerEnum(Integer code) {
            this.code = code;
        }

        private Integer code;

        public Integer getCode() {
            return code;
        }
    }

    /**
     * 项目用户角色
     * 2：项目责任人
     * 1：项目管理者
     * 0：普通用户
     */
    enum ProjectUserEnum{

        RESPONSIBLE_PERSON(2),
        ORGANIZE_USER(1),
        GENERAL_USER(0);

        ProjectUserEnum(Integer code) {
            this.code = code;
        }

        private Integer code;

        public Integer getCode() {
            return code;
        }
    }
}
