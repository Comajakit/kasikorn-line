package com.kasikorn.line.core.mobile.banking.service;

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
import com.kasikorn.line.core.mobile.banking.service.debitcard.DebitCardServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kasikorn.line.core.mobile.banking.constant.Constant.ACTIVE;
import static com.kasikorn.line.core.mobile.banking.constant.Constant.TEST_LAB;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DebitCardServiceTests {

    @Mock
    private DebitCardsRepository debitCardsRepository;
    @Mock
    private AccountsRepository accountsRepository;
    @InjectMocks
    private DebitCardServiceImpl debitCardService;

    @BeforeEach
    public void setUp() {
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext context = Mockito.mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
        when(auth.getPrincipal()).thenReturn("user001");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFetchDebitCard_WithValidData() {
        DebitCardsEntity card1 = new DebitCardsEntity();
        card1.setCardId("card1");
        card1.setStatus("ACTIVE");
        card1.setName("Card One");
        card1.setIssuer("IssuerA");
        card1.setColor("Red");
        card1.setBorderColor("Black");
        card1.setNumber("1234 5678 9012 3456");
        LocalDateTime createdAt1 = LocalDateTime.of(2025, 2, 7, 10, 0);
        card1.setCreatedAt(createdAt1);
        DebitCardsEntity card2 = new DebitCardsEntity();
        card2.setCardId("card2");
        card2.setStatus("ACTIVE");
        card2.setName("Card Two");
        card2.setIssuer("IssuerB");
        card2.setColor("Blue");
        card2.setBorderColor("White");
        card2.setNumber("9876 5432 1098 7654");
        LocalDateTime createdAt2 = LocalDateTime.of(2025, 2, 7, 12, 0);
        card2.setCreatedAt(createdAt2);
        List<DebitCardsEntity> cards = Arrays.asList(card1, card2);
        FetchDebitCardRequest request = new FetchDebitCardRequest();
        request.setLimit(10);
        request.setPage(0);
        when(debitCardsRepository.findTopByUserId(eq("user001"), any(Pageable.class))).thenReturn(cards);
        FetchDebitCardResponse response = debitCardService.fetchDebitCard(request);
        assertNotNull(response);
        assertNotNull(response.getDebits());
        assertEquals(2, response.getDebits().size());
        DebitCardDTO dto1 = response.getDebits().getFirst();
        assertEquals("card1", dto1.getCardId());
        assertEquals("1234 56xx xxxx 3456", dto1.getNumber());
        DebitCardDTO dto2 = response.getDebits().get(1);
        assertEquals("card2", dto2.getCardId());
        assertEquals("9876 54xx xxxx 7654", dto2.getNumber());
    }

    @Test
    public void testFetchDebitCard_WithInvalidCardNumberFormat() {
        DebitCardsEntity card = new DebitCardsEntity();
        card.setCardId("cardInvalid");
        card.setStatus("ACTIVE");
        card.setName("Invalid Card");
        card.setIssuer("IssuerC");
        card.setColor("Green");
        card.setBorderColor("Yellow");
        card.setNumber("1234567890123456");
        card.setCreatedAt(LocalDateTime.now());
        FetchDebitCardRequest request = new FetchDebitCardRequest();
        request.setLimit(10);
        request.setPage(0);
        when(debitCardsRepository.findTopByUserId(eq("user001"), any(Pageable.class))).thenReturn(Collections.singletonList(card));
        FetchDebitCardResponse response = debitCardService.fetchDebitCard(request);
        assertNotNull(response);
        assertEquals(1, response.getDebits().size());
        DebitCardDTO dto = response.getDebits().getFirst();
        assertEquals("1234567890123456", dto.getNumber());
    }

    @Test
    public void testCreateDebitCard_Success() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("acc001");
        request.setName("My Debit Card");
        request.setColor("Purple");
        request.setBorderColor("Orange");
        AccountsEntity account = new AccountsEntity();
        account.setAccountId("acc001");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        when(accountsRepository.findByAccountId("acc001")).thenReturn(Optional.of(account));
        when(debitCardsRepository.existsByAccountId("acc001")).thenReturn(false);
        when(accountsRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(debitCardsRepository.save(any(DebitCardsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateDebitCardResponse response = debitCardService.createDebit(request);
        assertNotNull(response);
        assertEquals("My Debit Card", response.getName());
        assertEquals("user001", response.getUserId());
        assertEquals("acc001", response.getAccountId());
        assertEquals("Purple", response.getColor());
        assertEquals("Orange", response.getBorderColor());
        assertEquals(TEST_LAB, response.getIssuer());
        assertEquals(ACTIVE, response.getStatus());
        assertNotNull(response.getNumber());
        assertTrue(response.getNumber().matches("\\d{4} \\d{4} \\d{4} \\d{4}"));
    }

    @Test
    public void testCreateDebitCard_Fail_AccountNotExists() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("nonexistent");
        request.setName("Card");
        request.setColor("Color");
        request.setBorderColor("Border");
        when(accountsRepository.findByAccountId("nonexistent")).thenReturn(Optional.empty());
        ValidationException ex = assertThrows(ValidationException.class, () -> debitCardService.createDebit(request));
        assertEquals("Account not exists", ex.getMessage());
    }

    @Test
    public void testCreateDebitCard_Fail_InvalidUser() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("acc002");
        request.setName("Card");
        request.setColor("Color");
        request.setBorderColor("Border");
        AccountsEntity account = new AccountsEntity();
        account.setAccountId("acc002");
        account.setUserId("otherUser");
        account.setType(AccountTypeEnum.SAVING);
        when(accountsRepository.findByAccountId("acc002")).thenReturn(Optional.of(account));
        ValidationException ex = assertThrows(ValidationException.class, () -> debitCardService.createDebit(request));
        assertEquals("Invalid userId", ex.getMessage());
    }

    @Test
    public void testCreateDebitCard_Fail_InvalidAccountType() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("acc003");
        request.setName("Card");
        request.setColor("Color");
        request.setBorderColor("Border");
        AccountsEntity account = new AccountsEntity();
        account.setAccountId("acc003");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.LOAN);
        when(accountsRepository.findByAccountId("acc003")).thenReturn(Optional.of(account));
        ValidationException ex = assertThrows(ValidationException.class, () -> debitCardService.createDebit(request));
        assertEquals("Invalid account type for debit card", ex.getMessage());
    }

    @Test
    public void testCreateDebitCard_Fail_DebitCardAlreadyExists() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("acc004");
        request.setName("Card");
        request.setColor("Color");
        request.setBorderColor("Border");
        AccountsEntity account = new AccountsEntity();
        account.setAccountId("acc004");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        when(accountsRepository.findByAccountId("acc004")).thenReturn(Optional.of(account));
        when(debitCardsRepository.existsByAccountId("acc004")).thenReturn(true);
        ValidationException ex = assertThrows(ValidationException.class, () -> debitCardService.createDebit(request));
        assertEquals("Debit card with this account already existed", ex.getMessage());
    }

    @Test
    public void testCreateDebitCard_Fail_UniqueDebitCardNumber() {
        CreateDebitCardRequest request = new CreateDebitCardRequest();
        request.setAccountId("acc005");
        request.setName("Card");
        request.setColor("Color");
        request.setBorderColor("Border");
        AccountsEntity account = new AccountsEntity();
        account.setAccountId("acc005");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        when(accountsRepository.findByAccountId("acc005")).thenReturn(Optional.of(account));
        when(debitCardsRepository.existsByAccountId("acc005")).thenReturn(false);
        when(accountsRepository.existsByAccountNumber(anyString())).thenReturn(true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> debitCardService.createDebit(request));
        assertEquals("Failed to generate a unique debit card number after 5 attempts", ex.getMessage());
    }
}
