package com.kasikorn.line.core.mobile.banking.entity;

import com.kasikorn.line.core.mobile.banking.enums.AccountStatusEnum;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.enums.CurrencyEnum;
import jakarta.persistence.*;
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
@Table(name = "accounts")
public class AccountsEntity {

    @Id
    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "account_name", length = 50)
    private String accountName;

    @Column(name = "color", length = 10)
    private String color;

    @Column(name = "is_main_account")
    private Boolean isMainAccount;

    @Column(name = "progress")
    private Integer progress;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private AccountTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 10)
    private CurrencyEnum currency;

    @Column(name = "account_balance", precision = 15, scale = 2)
    private BigDecimal accountBalance;

    @Column(name = "saving_goal", precision = 15, scale = 2)
    private BigDecimal savingGoal;

    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "issuer", length = 100)
    private String issuer;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "payment_accml", precision = 15, scale = 2)
    private BigDecimal paymentAccml;

    @Column(name = "interest_accml", precision = 15, scale = 2)
    private BigDecimal interestAccml;

    @Column(name = "loan_amount", precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "remaining", precision = 15, scale = 2)
    private BigDecimal remaining;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "loan_term")
    private Integer loanTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatusEnum accountStatus;

    @Column(name = "loan_start_date")
    private LocalDateTime loanStartDate;

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
