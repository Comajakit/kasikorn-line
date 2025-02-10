package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<AccountsEntity, String> {
    @Query("SELECT a FROM AccountsEntity a WHERE a.userId = :userId ORDER BY a.updatedAt DESC")
    List<AccountsEntity> findTopByUserId(@Param("userId") String userId, Pageable pageable);
    List<AccountsEntity> findByUserId(String userId);
    Optional<AccountsEntity> findByAccountId(String accountId);
    boolean existsByAccountNumber(String accountNumber);
}