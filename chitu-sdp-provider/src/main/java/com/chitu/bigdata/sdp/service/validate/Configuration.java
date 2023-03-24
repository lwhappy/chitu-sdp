package com.chitu.bigdata.sdp.service.validate;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/2/15 15:47
 */
public class Configuration {
        private String name;
        private String label;
        private SystemConfiguration.ValueType type;
        private Object defaultValue;
        private Object value;
        private String note;

        public Configuration(String name, String label, SystemConfiguration.ValueType type, Object defaultValue, String note) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.note = note;
    }

        public String getName() {
        return name;
    }

        public void setName(String name) {
        this.name = name;
    }

        public String getLabel() {
        return label;
    }

        public void setLabel(String label) {
        this.label = label;
    }

        public SystemConfiguration.ValueType getType() {
        return type;
    }

        public void setType(SystemConfiguration.ValueType type) {
        this.type = type;
    }

        public Object getDefaultValue() {
        return defaultValue;
    }

        public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

        public Object getValue() {
        return value;
    }

        public void setValue(Object value) {
        this.value = value;
    }

        public String getNote() {
        return note;
    }

        public void setNote(String note) {
        this.note = note;
    }

}
