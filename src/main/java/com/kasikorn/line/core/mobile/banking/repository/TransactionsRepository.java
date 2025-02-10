package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.TransactionsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<TransactionsEntity, String> {
    List<TransactionsEntity> findByUserId(String userId);

    @Query("SELECT t FROM TransactionsEntity t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<TransactionsEntity> findTopByUserId(@Param("userId") String userId, Pageable pageable);
}