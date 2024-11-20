package ru.t1.java.demo.kafka;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
abstract class BaseKafkaProducer<T> implements KafkaProducer<T> {

    private final String topic;
    protected final KafkaTemplate<String, Object> kafka;

    protected BaseKafkaProducer(String topic, KafkaTemplate<String, Object> kafka) {
        this.topic = topic;
        this.kafka = kafka;
    }

    @Override
    public void sendMessage(@NonNull T message) {
        log.info("to sendMessage: topic=[{}], message=[{}]", topic, message);

        try {
            ProducerRecord record = getRecord(topic, message);
            CompletableFuture<SendResult<String, T>> future =  kafka.send(record);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Message not sent: exceptionMsg=[{}]", ex.getMessage(), ex);
                } else {
                    String key = result.getProducerRecord().key();
                    long offset = result.getRecordMetadata().offset();
                    log.info("Message sent successfully: key=[{}], offset=[{}]", key, offset);
                }
            });
        } catch (Exception ex) {
            log.error("Unable to send message: exceptionMsg=[{}]", ex.getMessage(), ex);
        }

        log.info("from sendMessage");
    }

    @NonNull
    private ProducerRecord<String, T> getRecord(@NonNull String topic,
                                                @NonNull T message) {
        return new ProducerRecord<>(topic, generateKey(), message);
    }

    @NonNull
    private String generateKey() {
        return UUID.randomUUID().toString();
    }
}
