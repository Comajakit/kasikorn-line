package com.kasikorn.line.core.mobile.banking.repository;

import com.kasikorn.line.core.mobile.banking.entity.DebitCardsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebitCardsRepository extends JpaRepository<DebitCardsEntity, String> {
    @Query("SELECT d FROM DebitCardsEntity d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    List<DebitCardsEntity> findTopByUserId(@Param("userId") String userId, Pageable pageable);

    boolean existsByAccountId(String accountId);
}