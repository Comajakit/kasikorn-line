package com.kasikorn.line.core.mobile.banking.model.createdebitcard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateDebitCardResponse {
    private String status;
    private String userId;
    private String accountId;
    private String name;
    private String issuer;
    private String number;
    private String color;
    private String borderColor;
}
