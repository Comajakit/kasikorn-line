package com.kasikorn.line.core.mobile.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardDTO {
    private String cardId;
    private String status;
    private String name;
    private String issuer;
    private String number;
    private String color;
    private String borderColor;
    private LocalDateTime createdAt;
}
