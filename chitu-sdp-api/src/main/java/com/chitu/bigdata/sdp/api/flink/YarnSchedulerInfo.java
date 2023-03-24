package com.chitu.bigdata.sdp.api.flink;

import com.chitu.bigdata.sdp.api.flink.yarn.ResourceMemAndCpu;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class YarnSchedulerInfo {
    private Scheduler scheduler;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
   public static class Scheduler{
       private SchedulerInfo schedulerInfo;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
       public static class SchedulerInfo{
           private Queues queues;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
           public static class Queues{
               private ArrayList<QuenueInfo> queue;
                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
               public static class QuenueInfo{
                   private String queueName;

                    /**
                     * JM和TM已使用资源
                     */
                   private ResourceMemAndCpu resourcesUsed;
                    /**
                     * 队列最大可以使用的资源
                     */
                   private ResourceMemAndCpu maxEffectiveCapacity;
                    /**
                     * JM已使用资源
                     */
                   private ResourceMemAndCpu usedAMResource;
                    /**
                     * JM最大限制资源
                     */
                    @JsonProperty("AMResourceLimit")
                   private ResourceMemAndCpu amResourceLimit;
                    /**
                     * JM最大限制资源[每个用户]
                     */
                    @JsonProperty("userAMResourceLimit")
                   private ResourceMemAndCpu userAmResourceLimit;
                }

           }
       }
   }
}
