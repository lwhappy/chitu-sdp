package com.chitu.bigdata.sdp.api.enums;
/**
 * @author 587694
 * @description: TODO
 * @date 2021/12/14 19:28
 */
public interface DataSourceOption {

    enum JdbcOption {
        /**
         * jdbc地址
         */
        JDBC_URL("jdbc-url"),
        /**
         * 地址
         */
        URL("url"),
        /**
         * 用户名
         */
        USER_NAME("username"),
        /**
         * 密码
         */
        PWD("password"),
        /**
         * 表名
         */
        TABLE_NAME("table-name"),
        /**
         * stream load地址
         */
        STREAM_LOAD_URL("load-url"),
        /**
         * 其它的
         */
        OTHER("空");
        private String option;

        JdbcOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static JdbcOption ofOption(String option){
            for (JdbcOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

    enum EsOption {
        /**
         * 集群host
         */
        HOSTS("hosts"),
        /**
         * 用户名
         */
        USER_NAME("username"),
        /**
         * 密码
         */
        PWD("password"),
        /**
         * 索引
         */
        INDEX("index"),
        /**
         * 文档类型
         */
        DOCUMENT_TYPE("document-type"),
        /**
         * 其它的
         */
        OTHER("空");
        private String option;

        EsOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static EsOption ofOption(String option){
            for (EsOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

    enum KafkaOption {
        /**
         * kafka集群
         */
        BOOTSTRAP("properties.bootstrap.servers"),
        /**
         * topic
         */
        TOPIC("topic"),
        /**
         * 认证信息
         */
        SASL_CONFIG("properties.sasl.jaas.config"),

        MECHANISM_CONFIG("properties.sasl.mechanism"),
        /**
         * 其它的
         */
        OTHER("空");
        private String option;

    KafkaOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static KafkaOption ofOption(String option){

            for (KafkaOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

    enum HbaseOption {
        /**
         * zookeeper集群
         */
        QUORUM("zookeeper.quorum"),
        /**
         * 其它的
         */
        OTHER("空");
        private String option;

        HbaseOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static HbaseOption ofOption(String option){
            for (HbaseOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

    enum KuduOption {
        /**
         * 集群
         */
        MASTERS("kudu.masters"),
        /**
         * 其它的
         */
        OTHER("空");
        private String option;

        KuduOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static KuduOption ofOption(String option){
            for (KuduOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

    enum TiDBOption {
        /**
         * jdbc地址
         */
        JDBC_URL("tidb.database.url"),
        /**
         * 用户名称
         */
        USER_NAME("tidb.username"),
        /**
         * 密码
         */
        PWD("tidb.password"),
        /**
         * 其它的
         */
        OTHER("空");

        private String option;

        TiDBOption(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
        public static TiDBOption ofOption(String option){
            for (TiDBOption value : values()) {
                if (value.option.equals(option)){
                    return value;
                }
            }
            return OTHER;
        }
    }

}
