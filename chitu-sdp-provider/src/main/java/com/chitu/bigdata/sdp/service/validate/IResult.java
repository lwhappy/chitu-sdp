package com.chitu.bigdata.sdp.service.validate;

import java.time.LocalDateTime;

/**
 * IResult
 *
 * @author wenmo
 * @since 2021/5/25 16:22
 **/
public interface IResult {

    void setStartTime(LocalDateTime startTime);

    String getJobId();
}
