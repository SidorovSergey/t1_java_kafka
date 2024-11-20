package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;
import java.util.Objects;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionAcceptConsumer implements KafkaConsumer<TransactionAcceptDto> {

    private final TransactionService transactionService;

    @Override
    @KafkaListener(id = "${t1-demo.kafka.consumers.transaction_accept.group-id}",
            topics = "${t1-demo.kafka.consumers.transaction_accept.topic}",
            containerFactory = "transactionAcceptContainerFactory")
    public void listen(Acknowledgment ack,
                       @Header(RECEIVED_KEY) String key,
                       @Payload List<TransactionAcceptDto> messages) {
        log.debug("Messages received: key=[{}], messages=[{}]", key, messages);

        try {
            messages.stream()
                    .filter(Objects::nonNull)
                    .forEach(transactionAccept -> transactionService.handleTransactionAccept(key, transactionAccept));
        } catch (Exception ex) {
            log.error("Processing failed for transaction consumer: key=[{}], messages=[{}]", key, messages, ex);
        } finally {
            ack.acknowledge();
        }
    }

}
