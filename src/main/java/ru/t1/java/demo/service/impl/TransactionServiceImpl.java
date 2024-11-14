package ru.t1.java.demo.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dao.TxDao;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.dto.TransactionStatus;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.SECOND;
import static ru.t1.java.demo.dto.TransactionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${t1-demo.transaction.limit}")
    private int limit;
    @Value("${t1-demo.transaction.period}")
    private Duration period;

    private final TxDao txDao;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final KafkaProducer<TransactionResultDto> transactionResultProducer;

    @Override
    public void handleTransactionAccept(@NonNull String key, @NonNull TransactionAcceptDto transactionAcceptDto) {
        log.info("to handleTransactionAccept: key=[{}], transactionAcceptDto=[{}]", key, transactionAcceptDto);

        try {
            TransactionStatus status = getTransactionStatus(transactionAcceptDto);
            transactionResultProducer.sendMessage(transactionMapper.toTransactionResultDto(status, transactionAcceptDto));
        } catch (Exception ex) {
            log.error("Fail handle transaction accept: key=[{}], transactionAcceptDto=[{}]", key, transactionAcceptDto, ex);
        }

        log.info("from handleTransactionAccept: key=[{}]", key);
    }

    @NonNull
    private TransactionStatus getTransactionStatus(@NonNull TransactionAcceptDto transAcceptDto) {
        if (isBlocked(transAcceptDto)) {
            return BLOCKED;
        }
        return transAcceptDto.getAccountBalance().compareTo(BigDecimal.ZERO) < 0 ? REJECTED : ACCEPTED;
    }

    private boolean isBlocked(@NonNull TransactionAcceptDto transAcceptDto) {
        Timestamp to = new Timestamp(System.currentTimeMillis());
        Timestamp from = getFromTimestamp(to);

        return txDao.requiresNew(() -> {
            List<Transaction> transactions = transactionRepository.findTransactions(transAcceptDto.getClientId(),transAcceptDto.getAccountId(), from, to);

            if (transactions.size() > limit) {
                transactions.forEach(transaction -> transaction.setTransactionStatus(BLOCKED));
                return true;
            }
            return false;
        });
    }

    @NonNull
    private Timestamp getFromTimestamp(@NonNull Timestamp to) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        cal.add(SECOND, -1 * ((int) period.getSeconds()));
        return new Timestamp(cal.getTime().getTime());
    }

}
