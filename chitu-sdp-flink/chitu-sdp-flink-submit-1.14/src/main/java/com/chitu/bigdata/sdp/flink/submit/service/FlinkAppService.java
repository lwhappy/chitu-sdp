package com.chitu.bigdata.sdp.flink.submit.service;

import com.chitu.bigdata.sdp.api.flink.Application;

public interface FlinkAppService {

    SubmitResponse start(Application application);

    String stop(Application application);

    String triggerSavepoint(Application application);

}
