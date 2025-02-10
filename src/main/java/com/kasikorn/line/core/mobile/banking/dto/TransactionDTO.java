package com.kasikorn.line.core.mobile.banking.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String transactionId;
    private String userId;
    private String name;
    private String image;
    private Boolean isBank;
    private LocalDateTime createdAt;
}
