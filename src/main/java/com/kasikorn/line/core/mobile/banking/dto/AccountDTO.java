package com.kasikorn.line.core.mobile.banking.dto;

import com.kasikorn.line.core.mobile.banking.enums.AccountStatusEnum;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String accountId;
    private String accountName;
    private String userId;
    private String color;
    private Boolean isMainAccount;
    private Integer progress;
    private AccountTypeEnum type;
    private CurrencyEnum currency;
    private BigDecimal accountBalance;
    private BigDecimal savingGoal;
    private String accountNumber;
    private String issuer;
    private LocalDateTime dueDate;
    private BigDecimal paymentAccml;
    private BigDecimal interestAccml;
    private BigDecimal loanAmount;
    private BigDecimal remaining;
    private BigDecimal interestRate;
    private Integer loanTerm;
    private LocalDateTime loanStartDate;
    private AccountStatusEnum accountStatus;

    private List<String> flags;
}
