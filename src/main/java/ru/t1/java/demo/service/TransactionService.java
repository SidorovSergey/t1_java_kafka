package ru.t1.java.demo.service;

import lombok.NonNull;
import ru.t1.java.demo.dto.TransactionAcceptDto;

public interface TransactionService {

    void handleTransactionAccept(@NonNull String key, @NonNull TransactionAcceptDto transactionAccept);

}
