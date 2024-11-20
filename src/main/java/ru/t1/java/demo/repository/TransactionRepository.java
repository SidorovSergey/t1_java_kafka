package ru.t1.java.demo.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Transaction;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Modifying
    @Query(nativeQuery = true, value = "select t.* from t1_demo.t1$client c " +
            "left join t1_demo.t1$account a on c.id = a.client_id " +
            "left join t1_demo.t1$transaction t on a.id = t.account_id " +
            "where c.client_id = :#{#clientId} and a.account_id = :#{#accountId} " +
            "and :#{#from} <= t.timestamp and t.timestamp <= :#{#to}")
    List<Transaction> findTransactions(@Param("clientId") UUID clientId,
                                       @Param("accountId") UUID accountId,
                                       @Param("from") @NonNull Timestamp from,
                                       @Param("to") @NonNull Timestamp to);


}
