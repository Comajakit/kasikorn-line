package com.kasikorn.line.core.mobile.banking.service.debitcard;

import com.kasikorn.line.core.mobile.banking.dto.DebitCardDTO;
import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import com.kasikorn.line.core.mobile.banking.entity.DebitCardsEntity;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardResponse;
import com.kasikorn.line.core.mobile.banking.repository.AccountsRepository;
import com.kasikorn.line.core.mobile.banking.repository.DebitCardsRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static com.kasikorn.line.core.mobile.banking.constant.Constant.ACTIVE;
import static com.kasikorn.line.core.mobile.banking.constant.Constant.TEST_LAB;

@Service
@Slf4j
@RequiredArgsConstructor
public class DebitCardServiceImpl implements DebitCardService {
    private final DebitCardsRepository debitCardsRepository;
    private final AccountsRepository accountsRepository;
    private static final Random random = new Random();

    @Override
    public FetchDebitCardResponse fetchDebitCard(FetchDebitCardRequest request) {
        log.info("##### Debit Card Service: Fetch debit card #####");
        log.info("**** Incoming Request: {} ****", request);
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching debitcards for userId: {}", userId);
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        int page = request.getPage() != null ? request.getPage() : 0;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("*** Query Debit Card: findTopByUserId ***");
        List<DebitCardsEntity> debitCardsEntities = debitCardsRepository.findTopByUserId(userId, pageable);
        log.info("*** Query Completed ***");
        List<DebitCardDTO> debitCardDTOS = debitCardsEntities.stream()
                .map(entity -> DebitCardDTO.builder()
                        .cardId(entity.getCardId())
                        .status(entity.getStatus())
                        .name(entity.getName())
                        .issuer(entity.getIssuer())
                        .color(entity.getColor())
                        .borderColor(entity.getBorderColor())
                        .number(maskCardNumber(entity.getNumber())) // Apply masking
                        .createdAt(entity.getCreatedAt())
                        .build())
                .toList();
        FetchDebitCardResponse response = FetchDebitCardResponse.builder().debits(debitCardDTOS).build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    @Override
    @Transactional
    public CreateDebitCardResponse createDebit(CreateDebitCardRequest request) {
        log.info("##### Debit Card Service: Create debit card #####");
        log.info("**** Incoming Request: {} ****", request);
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating debit card for userId: {}", userId);
        validateDebitCardCreation(request.getAccountId(), userId);

        DebitCardsEntity debitCardsEntity = new DebitCardsEntity();
        debitCardsEntity.setAccountId(request.getAccountId());
        debitCardsEntity.setIssuer(TEST_LAB);
        debitCardsEntity.setColor(request.getColor());
        debitCardsEntity.setBorderColor(request.getBorderColor());
        debitCardsEntity.setUserId(userId);
        debitCardsEntity.setCardId(UUID.randomUUID().toString());
        debitCardsEntity.setName(request.getName());
        debitCardsEntity.setStatus(ACTIVE);
        debitCardsEntity.setNumber(generateUniqueDebitCardNumber());

        DebitCardsEntity savedDebit = debitCardsRepository.save(debitCardsEntity);

        CreateDebitCardResponse response = new CreateDebitCardResponse();
        response.setName(savedDebit.getName());
        response.setNumber(savedDebit.getNumber());
        response.setIssuer(savedDebit.getIssuer());
        response.setColor(savedDebit.getColor());
        response.setStatus(savedDebit.getStatus());
        response.setBorderColor(savedDebit.getBorderColor());
        response.setUserId(userId);
        response.setAccountId(savedDebit.getAccountId());
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    private String generateUniqueDebitCardNumber() {
        final int maxAttempts = 5;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                int digit = random.nextInt(10);
                sb.append(digit);
                if ((i + 1) % 4 == 0 && i != 15) {
                    sb.append(" ");
                }
            }
            String cardNumber = sb.toString();
            if (!accountsRepository.existsByAccountNumber(cardNumber)) {
                return cardNumber;
            }
            log.warn("Duplicate debit card number generated: {}. Retrying...", cardNumber);
        }
        throw new RuntimeException("Failed to generate a unique debit card number after 5 attempts");
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.contains(" ")) {
            log.warn("Invalid card number format: {}", cardNumber);
            return cardNumber;
        }

        String[] parts = cardNumber.split(" ");

        String maskedSecond = parts[1].substring(0, Math.min(2, parts[1].length())) + "x".repeat(Math.max(0, parts[1].length() - 2));
        String maskedThird = "x".repeat(parts[2].length());

        return String.format("%s %s %s %s", parts[0], maskedSecond, maskedThird, parts[3]);
    }

    private void validateDebitCardCreation(String accountId, String userId) {
        Optional<AccountsEntity> accountOpt = accountsRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            throw new ValidationException("Account not exists");
        }
        AccountsEntity accountEntity = accountOpt.get();
        if (!accountEntity.getUserId().equals(userId)) {
            throw new ValidationException("Invalid userId");
        }
        if (!AccountTypeEnum.SAVING.equals(accountEntity.getType())) {
            throw new ValidationException("Invalid account type for debit card");
        }
        if (debitCardsRepository.existsByAccountId(accountId)) {
            throw new ValidationException("Debit card with this account already existed");
        }
    }
}
