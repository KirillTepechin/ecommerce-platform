package com.example.order.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;

import java.util.Map;

public class CustomerPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partitionCount = cluster.partitionsForTopic(topic).size();
        if (partitionCount == 0) {
            return 0;
        }

        if (keyBytes == null || key == null) {
            return 0;
        }

        return Utils.toPositive(Utils.murmur2(keyBytes)) % partitionCount;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
