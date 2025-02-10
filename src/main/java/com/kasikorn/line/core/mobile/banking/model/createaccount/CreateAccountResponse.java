package com.kasikorn.line.core.mobile.banking.model.createaccount;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kasikorn.line.core.mobile.banking.enums.AccountStatusEnum;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateAccountResponse {
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
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer loanTerm;
    private LocalDateTime loanStartDate;
    private AccountStatusEnum accountStatus;
}
