package com.chitu.bigdata.sdp.utils;

import com.xiaoleilu.hutool.util.StrUtil;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

public class KafkaConnectUtil {
    public static KafkaConsumer<String, String> initConsumer(String groupId, String servers){
        Map<String, Object> consumerConfig = new HashMap<>();
        // 设置默认值
        consumerConfig.put("enable.auto.commit", false); // 关闭自动commit
        consumerConfig.put("auto.commit.interval.ms", "1000");
        consumerConfig.put("session.timeout.ms", "30000");
        consumerConfig.put("fetch.min.bytes", "1048576");
        consumerConfig.put("fetch.max.wait.ms", "1000");
        consumerConfig.put("max.partition.fetch.bytes", "8388608");
        consumerConfig.put("max.poll.records", "3");
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        consumerConfig.put("auto.offset.reset", "earliest");
        consumerConfig.put("group.id", groupId);
        consumerConfig.put("client.id", StrUtil.subAfter(groupId,"-",false));
        consumerConfig.put("bootstrap.servers", servers);
        return  new KafkaConsumer<String, String>(consumerConfig);
    }

    public static Producer<String, String> getProducer(String server){
        Properties props = new Properties();
        //设置kafka集群的地址
        props.put("bootstrap.servers", server);
        //ack模式，all是最慢但最安全的
        props.put("acks", "-1");
        //失败重试次数
        props.put("retries", 0);
        props.put("batch.size", 1);
        //消息在缓冲区保留的时间，超过设置的值就会被提交到服务端
        props.put("linger.ms", 10000);
        //整个Producer用到总内存的大小，如果缓冲区满了会提交数据到服务端
        //buffer.memory要大于batch.size，否则会报申请内存不足的错误
        props.put("buffer.memory", 20480);
        //序列化器
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
        return producer;
    }

    public  static AdminClient getClient(String bootStrap){
        Properties props = new Properties();
        props.put("bootstrap.servers", bootStrap);
        props.put("retries", 1);
        props.put("request.timeout.ms",5000);
        AdminClient adminClient = AdminClient.create(props);
        return adminClient;

    }
    public static void resetToEarliest(KafkaConsumer<String, String> kafkaConsumer) {
        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(100);

            assignment = kafkaConsumer.assignment();
        }
        Map<TopicPartition, Long> beginOffsets = kafkaConsumer.beginningOffsets(assignment);
        for (TopicPartition tp : assignment) {
            Long offset = beginOffsets.get(tp);
            System.out.println("分区 " + tp + " 从 " + offset + " 开始消费");
            kafkaConsumer.seek(tp, offset);
        }
    }
}
