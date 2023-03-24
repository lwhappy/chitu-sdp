package com.chitu.bigdata.sdp.api.flink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class AppInfoList {

    private AppInfos apps;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppInfos{
        private List<Apps> app;
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static  class Apps{
            private String name;
            private String state;
        }
    }
}
