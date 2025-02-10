package com.kasikorn.line.core.mobile.banking.service.transaction;

import com.kasikorn.line.core.mobile.banking.dto.TransactionDTO;
import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import com.kasikorn.line.core.mobile.banking.entity.TransactionsEntity;
import com.kasikorn.line.core.mobile.banking.enums.BankNameEnum;
import com.kasikorn.line.core.mobile.banking.enums.TransactionTypeEnum;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnResponse;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentRequest;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentResponse;
import com.kasikorn.line.core.mobile.banking.repository.AccountsRepository;
import com.kasikorn.line.core.mobile.banking.repository.TransactionsRepository;
import com.kasikorn.line.core.mobile.banking.service.helper.GeneralUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kasikorn.line.core.mobile.banking.constant.Constant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionsRepository transactionsRepository;
    private final AccountsRepository accountsRepository;
    private final GeneralUtil generalUtil;

    @Override
    public FetchRecentTxnResponse fetchRecent(FetchRecentTxnRequest request) {
        log.info("##### Transaction Service: Fetch recent transaction #####");
        log.info("**** Incoming Request: {} ****", request);
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("*** Fetching txn for userId: {} ***", userId);

        int limit = request.getLimit() != null ? request.getLimit() : 10;
        int page = request.getPage() != null ? request.getPage() : 0;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("*** Query Transaction: findTopByUserId ***");
        List<TransactionsEntity> transactionsEntities = transactionsRepository.findTopByUserId(userId, pageable);
        log.info("*** Query Result: {} ***", transactionsEntities);

        List<TransactionDTO> transactionDTOS = transactionsEntities.stream()
                .map(entity -> TransactionDTO.builder()
                        .transactionId(entity.getTransactionId())
                        .name(entity.getName())
                        .userId(entity.getUserId())
                        .image(entity.getImage())
                        .isBank(entity.getIsBank())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .toList();
        FetchRecentTxnResponse response = FetchRecentTxnResponse.builder().txnList(transactionDTOS).build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse payment(PaymentRequest request) {
        log.info("##### Transaction Service: payment #####");
        log.info("**** Incoming Request: {} ****", request);
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TransactionTypeEnum transactionType;
        try {
            transactionType = TransactionTypeEnum.valueOf(request.getTransactionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid transaction type: " + request.getTransactionType());
        }

        log.info("*** Query Account: findById ***");
        Optional<AccountsEntity> accountOptional = accountsRepository.findById(request.getAccountId());
        log.info("*** Query Result: {} ***", accountOptional);
        if (accountOptional.isEmpty()) {
            throw new ValidationException("Account not found: " + request.getAccountId());
        }
        AccountsEntity account = accountOptional.get();
        BigDecimal transactionAmount = request.getAmount();

        switch (account.getType().name()) {
            case SAVING:
                log.info("*** Case: Saving ***");
                if (!isValidForSaving(transactionType)) {
                    throw new ValidationException("Transaction type not allowed for saving accounts");
                }
                processSavingTransaction(account, transactionAmount, transactionType);
                break;

            case SAVING_GOAL:
                log.info("*** Case: Saving Goal ***");
                if (!isValidForSavingGoal(transactionType)) {
                    throw new ValidationException("Transaction type not allowed for saving goal accounts");
                }
                processSavingGoalTransaction(account, transactionAmount, transactionType);
                break;

            case LOAN:
                log.info("*** Case: Loan ***");
                if (!isValidForLoan(transactionType)) {
                    throw new ValidationException("Transaction type not allowed for loan accounts");
                }
                processLoanPayment(account, transactionAmount);
                break;

            default:
                throw new ValidationException("Unknown account type");
        }

        accountsRepository.save(account);
        log.info("*** Processing Completed ***");
        boolean isBank = isBankTransaction(account.getIssuer());

        log.info("*** Creating Transaction ***");
        TransactionsEntity transaction = TransactionsEntity.builder()
                .transactionId(generateTransactionId())
                .accountId(request.getAccountId())
                .userId(userId)
                .transactionType(transactionType)
                .amount(transactionAmount)
                .image("sample://image")
                .name(request.getName())
                .isBank(isBank)
                .build();
        TransactionsEntity savedTxn = transactionsRepository.save(transaction);
        log.info("*** Transaction Created ***");
        PaymentResponse response = PaymentResponse.builder()
                .txnId(savedTxn.getTransactionId())
                .build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    private boolean isValidForSaving(TransactionTypeEnum type) {
        return type == TransactionTypeEnum.DEPOSIT ||
                type == TransactionTypeEnum.WITHDRAW ||
                type == TransactionTypeEnum.QR_PAYMENT ||
                type == TransactionTypeEnum.PAYMENT;
    }

    private boolean isValidForSavingGoal(TransactionTypeEnum type) {
        return type == TransactionTypeEnum.DEPOSIT || type == TransactionTypeEnum.WITHDRAW;
    }

    private boolean isValidForLoan(TransactionTypeEnum type) {
        return type == TransactionTypeEnum.PAYMENT;
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    private boolean isBankTransaction(String issuer) {
        return Arrays.stream(BankNameEnum.values())
                .anyMatch(bank -> bank.name().equalsIgnoreCase(issuer));
    }

    private void processSavingTransaction(AccountsEntity account, BigDecimal transactionAmount, TransactionTypeEnum transactionType) {
        log.info("*** Processing: {} ***", transactionType.name());
        if (transactionType == TransactionTypeEnum.DEPOSIT) {
            account.setAccountBalance(account.getAccountBalance().add(transactionAmount));
        } else {
            if (account.getAccountBalance().compareTo(transactionAmount) < 0) {
                throw new ValidationException("Insufficient balance for this transaction");
            }
            account.setAccountBalance(account.getAccountBalance().subtract(transactionAmount));
        }
    }

    private void processSavingGoalTransaction(AccountsEntity account, BigDecimal transactionAmount, TransactionTypeEnum transactionType) {
        log.info("*** Processing: {} ***", transactionType.name());
        if (transactionType == TransactionTypeEnum.DEPOSIT) {
            account.setAccountBalance(account.getAccountBalance().add(transactionAmount));
        } else {
            if (account.getAccountBalance().compareTo(transactionAmount) < 0) {
                throw new ValidationException("Insufficient balance for this transaction");
            }
            account.setAccountBalance(account.getAccountBalance().subtract(transactionAmount));
        }

        BigDecimal savingGoal = account.getSavingGoal();
        if (savingGoal != null && savingGoal.compareTo(BigDecimal.ZERO) > 0) {
            int progress = account.getAccountBalance()
                    .multiply(new BigDecimal("100"))
                    .divide(savingGoal, 2, RoundingMode.HALF_UP)
                    .intValue();

            account.setProgress(Math.min(progress, 100));
        }
    }

    private void processLoanPayment(AccountsEntity account, BigDecimal paymentAmount) {
        log.info("*** Processing: Payment ***");
        generalUtil.loanInterestUpdate(account);

        BigDecimal interestAccml = account.getInterestAccml() != null ? account.getInterestAccml() : BigDecimal.ZERO;
        BigDecimal remainingLoan = account.getRemaining() != null ? account.getRemaining() : BigDecimal.ZERO;

        BigDecimal amountToDeductFromInterest = paymentAmount.min(interestAccml);
        BigDecimal amountLeftForRemaining = paymentAmount.subtract(amountToDeductFromInterest);

        account.setInterestAccml(interestAccml.subtract(amountToDeductFromInterest));
        account.setRemaining(remainingLoan.subtract(amountLeftForRemaining));
        account.setPaymentAccml(account.getPaymentAccml().add(paymentAmount));
        account.setUpdatedAt(LocalDateTime.now());
    }
}