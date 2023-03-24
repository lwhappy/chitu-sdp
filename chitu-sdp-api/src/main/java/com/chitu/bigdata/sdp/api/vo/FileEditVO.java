package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/11/3
 */
@Data
public class FileEditVO {
    /**
     * 是否允许编辑
     */
    Boolean allowEdit;
    /**
     * 是否是存量作业（历史作业）
     */
    Boolean historyFile;
}
