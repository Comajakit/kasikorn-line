package com.kasikorn.line.core.mobile.banking.service.helper;

import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.repository.AccountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralUtil {
    private final AccountsRepository accountsRepository;

    public void loanInterestUpdate(AccountsEntity entity) {
        if (entity.getType() != AccountTypeEnum.LOAN) {
            return;
        }

        LocalDate lastUpdateDate = entity.getUpdatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        if (today.equals(lastUpdateDate)) {
            return;
        }

        long daysDiff = ChronoUnit.DAYS.between(lastUpdateDate, today);
        if (daysDiff <= 0 || entity.getRemaining() == null || entity.getInterestRate() == null) {
            return;
        }

        BigDecimal currentInterest = entity.getInterestAccml() != null ? entity.getInterestAccml() : BigDecimal.ZERO;
        int denominator = lastUpdateDate.isLeapYear() ? 366 : 365;

        BigDecimal additionalInterest = entity.getRemaining()
                .multiply(entity.getInterestRate().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .multiply(new BigDecimal(daysDiff))
                .divide(new BigDecimal(denominator), 2, RoundingMode.HALF_UP);

        BigDecimal updatedInterestAccml = currentInterest.add(additionalInterest);
        entity.setInterestAccml(updatedInterestAccml);
        entity.setUpdatedAt(LocalDateTime.now());
        accountsRepository.save(entity);

        log.info("Updated interest_accml for account {}: added {} for {} days (denom={}), new value: {}",
                entity.getAccountId(), additionalInterest, daysDiff, denominator, updatedInterestAccml);
    }
}
