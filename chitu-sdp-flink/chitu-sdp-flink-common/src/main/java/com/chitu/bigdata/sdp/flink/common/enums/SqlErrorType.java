

package com.chitu.bigdata.sdp.flink.common.enums;

import java.io.Serializable;

public enum SqlErrorType implements Serializable {
    /**
     * 基本检验失败(如为null等)
     */
    VERIFY_FAILED(1),
    /**
     * 语法错误
     */
    SYNTAX_ERROR(2),
    /**
     * 不支持的方言
     */
    UNSUPPORTED_DIALECT(3),
    /**
     * 不支持的sql命令
     */
    UNSUPPORTED_SQL(4),
    /**
     * 非";"结尾
     */
    ENDS_WITH(5);

    private final int value;

    SqlErrorType(int value) {
        this.value = value;
    }

    public static SqlErrorType of(Integer value) {
        for (SqlErrorType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
