package com.chitu.bigdata.sdp.service.validate.domain;

import com.chitu.bigdata.sdp.api.vo.ExplainExt;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 解释结果
 *
 * @author  wenmo
 * @since  2021/6/7 22:06
 **/
@Data
@EqualsAndHashCode
public class SqlExplainResult {
    private Integer index;
    private Integer line;
    private String type = "error";
    private String sql;
    private String parse;
    private String explain;
    private String error;
    private boolean parseTrue;
    private boolean explainTrue;

    /**
     * 返回弹框数据
     */
    private ExplainExt explainExt;

    private LocalDateTime explainTime = LocalDateTime.now();

    @Override
    public String toString() {
        return "SqlExplainRecord{" +
                "index=" + index +
                ", line='" + line + '\'' +
                ", type='" + type + '\'' +
                ", sql='" + sql + '\'' +
                ", parse='" + parse + '\'' +
                ", explain='" + explain + '\'' +
                ", error='" + error + '\'' +
                ", parseTrue=" + parseTrue +
                ", explainTrue=" + explainTrue +
                ", explainTime=" + explainTime +
                '}';
    }
}
