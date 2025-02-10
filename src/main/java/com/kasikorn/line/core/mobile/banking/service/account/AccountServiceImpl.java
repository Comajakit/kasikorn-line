package com.kasikorn.line.core.mobile.banking.service.account;

import com.kasikorn.line.core.mobile.banking.dto.AccountDTO;
import com.kasikorn.line.core.mobile.banking.entity.AccountFlagsEntity;
import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import com.kasikorn.line.core.mobile.banking.enums.AccountStatusEnum;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.enums.CurrencyEnum;
import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountResponse;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagRequest;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountResponse;
import com.kasikorn.line.core.mobile.banking.repository.AccountFlagsRepository;
import com.kasikorn.line.core.mobile.banking.repository.AccountsRepository;
import com.kasikorn.line.core.mobile.banking.service.helper.GeneralUtil;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.kasikorn.line.core.mobile.banking.constant.Constant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountsRepository accountsRepository;
    private final AccountFlagsRepository accountFlagsRepository;
    private final GeneralUtil generalUtil;
    private final Random random = new Random();

    @Override
    public FetchAccountResponse fetch(FetchAccountRequest request) {
        log.info("##### Account Service: Fetch #####");
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching accounts for userId: {}", userId);

        int limit = request.getLimit() != null ? request.getLimit() : 10;
        int page = request.getPage() != null ? request.getPage() : 0;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));

        List<AccountsEntity> accountsEntities = accountsRepository.findTopByUserId(userId, pageable);
        accountsEntities.forEach(generalUtil::loanInterestUpdate);
        accountsEntities.sort(Comparator.comparing(AccountsEntity::getUpdatedAt).reversed());
        List<AccountDTO> accountDTOs = accountsEntities.stream().map(entity -> {
            List<String> flagValues = accountFlagsRepository.findByAccountId(entity.getAccountId())
                    .stream()
                    .map(AccountFlagsEntity::getFlagValue)
                    .collect(Collectors.toList());

            return AccountDTO.builder()
                    .accountId(entity.getAccountId())
                    .accountName(entity.getAccountName())
                    .userId(entity.getUserId())
                    .color(entity.getColor())
                    .isMainAccount(entity.getIsMainAccount())
                    .progress(entity.getProgress())
                    .type(entity.getType())
                    .currency(entity.getCurrency())
                    .accountBalance(entity.getAccountBalance())
                    .savingGoal(entity.getSavingGoal())
                    .accountNumber(entity.getAccountNumber())
                    .issuer(entity.getIssuer())
                    .dueDate(entity.getDueDate())
                    .paymentAccml(entity.getPaymentAccml())
                    .interestAccml(entity.getInterestAccml())
                    .loanAmount(entity.getLoanAmount())
                    .remaining(entity.getRemaining())
                    .interestRate(entity.getInterestRate())
                    .loanTerm(entity.getLoanTerm())
                    .loanStartDate(entity.getLoanStartDate())
                    .accountStatus(entity.getAccountStatus())
                    .flags(flagValues)
                    .build();
        }).collect(Collectors.toList());

        return FetchAccountResponse.builder().accounts(accountDTOs).build();
    }

    @Override
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        log.info("##### Account Service: Create account #####");
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating account for userId: {}", userId);
        String accountId = UUID.randomUUID().toString();
        String accountNumber = generateUniqueAccountNumber();
        Integer progress = null;
        BigDecimal savingGoal = null;
        BigDecimal accountBalance = request.getType().equalsIgnoreCase(AccountTypeEnum.SAVING.name())
                ? request.getInitialBalance()
                : null;
        if (request.getType().equalsIgnoreCase(AccountTypeEnum.SAVING_GOAL.name())) {
            savingGoalAccountValidation(request);
            savingGoal = request.getSavingGoal();
            accountBalance = request.getInitialBalance() != null
                    ? request.getInitialBalance()
                    : BigDecimal.ZERO;
            BigDecimal ratio = accountBalance.divide(savingGoal, 4, RoundingMode.HALF_UP);
            BigDecimal progressDecimal = ratio.multiply(new BigDecimal("100"));
            progress = progressDecimal.compareTo(new BigDecimal("100")) > 0 ? 100 : progressDecimal.intValue();
        }

        LocalDateTime loanStartDate = null;
        LocalDateTime dueDate = null;
        BigDecimal interestRate = null;
        BigDecimal loanAmount = null;
        Integer loanTerm = null;
        BigDecimal paymentAccml = null;
        BigDecimal interestAccml = null;
        if (request.getType().equalsIgnoreCase(AccountTypeEnum.LOAN.name())) {
            paymentAccml = BigDecimal.ZERO;
            interestAccml = BigDecimal.ZERO;
            loanAccountValidation(request);
            loanStartDate = LocalDateTime.now();
            dueDate = loanStartDate.plusMonths(request.getLoanTerm());
            interestRate = new BigDecimal("10");
            loanAmount = request.getLoanAmount();
            loanTerm = request.getLoanTerm();
        }

        AccountStatusEnum accountStatus = AccountStatusEnum.ACTIVE;
        boolean isMain = request.getType().equalsIgnoreCase(AccountTypeEnum.SAVING.name())
                && Boolean.TRUE.equals(request.getIsMain());
        AccountsEntity entity = AccountsEntity.builder()
                .accountId(accountId)
                .accountName(request.getAccountName())
                .userId(userId)
                .color(request.getColor())
                .isMainAccount(isMain)
                .progress(progress)
                .type(AccountTypeEnum.valueOf(request.getType().toUpperCase()))
                .currency(CurrencyEnum.valueOf(request.getCurrency().toUpperCase()))
                .accountBalance(accountBalance)
                .savingGoal(savingGoal)
                .accountNumber(accountNumber)
                .issuer(TEST_LAB)
                .dueDate(dueDate)
                .paymentAccml(paymentAccml)
                .interestAccml(interestAccml)
                .remaining(loanAmount)
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .loanTerm(loanTerm)
                .loanStartDate(loanStartDate)
                .accountStatus(accountStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AccountsEntity savedEntity = accountsRepository.save(entity);

        return CreateAccountResponse.builder()
                .accountId(savedEntity.getAccountId())
                .accountName(savedEntity.getAccountName())
                .userId(savedEntity.getUserId())
                .color(savedEntity.getColor())
                .isMainAccount(savedEntity.getIsMainAccount())
                .progress(savedEntity.getProgress())
                .type(savedEntity.getType())
                .currency(savedEntity.getCurrency())
                .accountBalance(savedEntity.getAccountBalance())
                .savingGoal(savedEntity.getSavingGoal())
                .accountNumber(savedEntity.getAccountNumber())
                .issuer(savedEntity.getIssuer())
                .dueDate(savedEntity.getDueDate())
                .loanAmount(savedEntity.getLoanAmount())
                .interestRate(savedEntity.getInterestRate())
                .loanTerm(savedEntity.getLoanTerm())
                .loanStartDate(savedEntity.getLoanStartDate())
                .accountStatus(savedEntity.getAccountStatus())
                .build();

    }

    @Override
    @Transactional
    public CreateFlagResponse createFlag(CreateFlagRequest request) {
        log.info("##### Account Service: Create flag #####");
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating flag for accountId: {}", request.getAccountId());
        Optional<AccountsEntity> accounts = accountsRepository.findByAccountId(request.getAccountId());
        AccountsEntity accountsEntity;
        if (accounts.isEmpty()) {
            throw new ValidationException("Account not exists");
        } else {
            accountsEntity = accounts.get();
        }
        accountFlagValidation(accountsEntity, request, userId);
        AccountFlagsEntity accountFlagsEntity = new AccountFlagsEntity();
        accountFlagsEntity.setAccountId(request.getAccountId());
        accountFlagsEntity.setFlagType(SYSTEM);
        accountFlagsEntity.setFlagValue(request.getFlagValue());
        accountFlagsEntity.setUserId(userId);
        AccountFlagsEntity newAccountFlag = accountFlagsRepository.save(accountFlagsEntity);

        CreateFlagResponse response = new CreateFlagResponse();
        response.setAccountId(newAccountFlag.getAccountId());
        response.setFlagValue(newAccountFlag.getFlagValue());
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    private String generateUniqueAccountNumber() {
        final int maxAttempts = 3;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int part1 = random.nextInt(1000);
            int part2 = random.nextInt(10);
            int part3 = random.nextInt(100000);
            int part4 = random.nextInt(10);
            String accountNumber = String.format("%03d-%d-%05d-%d", part1, part2, part3, part4);
            if (!accountsRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
            log.warn("Duplicate account number generated: {}. Retrying...", accountNumber);
        }
        throw new RuntimeException("Failed to generate a unique account number after 3 attempts");
    }

    private void savingGoalAccountValidation(CreateAccountRequest request) {
        if (request.getSavingGoal() == null || request.getSavingGoal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Saving goal account validation fail");
        }

    }

    private void accountFlagValidation(AccountsEntity entity, CreateFlagRequest request, String userId) {
        if (!entity.getUserId().equals(userId)) {
            throw new ValidationException("Invalid userId");
        }
        boolean isLoan = AccountTypeEnum.LOAN.equals(entity.getType());
        String flagValue = request.getFlagValue();
        if (!isLoan && (DISBURSEMENT.equalsIgnoreCase(flagValue) || OVERDUE.equalsIgnoreCase(flagValue))) {
            throw new ValidationException("Invalid flag value");
        }
    }

    private void loanAccountValidation(CreateAccountRequest request) {
        if (request.getLoanAmount() == null || request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Loan account validation fail");
        }
        if (request.getLoanTerm() == null || request.getLoanTerm().compareTo(0) <= 0) {
            throw new ValidationException("Loan account validation fail");
        }

    }
}