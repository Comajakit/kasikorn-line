package com.kasikorn.line.core.mobile.banking.model.createaccount;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kasikorn.line.core.mobile.banking.validator.ValidAccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateAccountRequest {
    @NotBlank(message = "Color is required")
    private String color;

    private Boolean isMain;

    @NotBlank(message = "Account name is required")
    private String accountName;

    @ValidAccountType
    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Currency is required")
    private String currency;

    private BigDecimal initialBalance;
    private BigDecimal savingGoal;
    private BigDecimal loanAmount;
    private Integer loanTerm;
}
