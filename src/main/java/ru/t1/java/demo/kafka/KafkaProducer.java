package ru.t1.java.demo.kafka;

public interface KafkaProducer<T> {
    String TRANSACTION_RESULT_NAME = "transaction_result";

    void sendMessage(T message);
}
