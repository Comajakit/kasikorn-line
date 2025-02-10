package com.kasikorn.line.core.mobile.banking.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "debit_cards")
public class DebitCardsEntity {

    @Id
    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "issuer", length = 100)
    private String issuer;

    @Column(name = "number", length = 25)
    private String number;

    @Column(name = "color", length = 10)
    private String color;

    @Column(name = "border_color", length = 10)
    private String borderColor;

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