package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.AccountFlagsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountFlagsRepository extends JpaRepository<AccountFlagsEntity, Integer> {
    List<AccountFlagsEntity> findByAccountId(String accountId);
}