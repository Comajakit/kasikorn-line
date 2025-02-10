package com.kasikorn.line.core.mobile.banking.entity;


import com.kasikorn.line.core.mobile.banking.enums.AccountStatusEnum;
import com.kasikorn.line.core.mobile.banking.enums.TransactionTypeEnum;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class TransactionsEntity {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_bank")
    private Boolean isBank;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionTypeEnum transactionType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
