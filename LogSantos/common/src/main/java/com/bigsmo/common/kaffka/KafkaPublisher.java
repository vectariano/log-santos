package com.bigsmo.common.kaffka;

public interface KafkaPublisher<T> {
    void publish(T message);
}