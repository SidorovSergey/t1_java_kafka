package ru.t1.java.demo.util;

import lombok.NonNull;
import org.mapstruct.Mapper;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.dto.TransactionStatus;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class TransactionMapper {

    @NonNull
    public TransactionResultDto toTransactionResultDto(@NonNull TransactionStatus status,
                                                       @NonNull TransactionAcceptDto transactionAcceptDto) {
        return new TransactionResultDto()
                .setStatus(status)
                .setAccountId(transactionAcceptDto.getAccountId())
                .setTransactionId(transactionAcceptDto.getTransactionId());
    }

}
