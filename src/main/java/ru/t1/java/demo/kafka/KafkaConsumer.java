package ru.t1.java.demo.kafka;

import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

public interface KafkaConsumer<T> {

    String TRANSACTION_ACCEPT_NAME = "transaction_accept";

    void listen(Acknowledgment ack, String key, List<T> messages);
}
