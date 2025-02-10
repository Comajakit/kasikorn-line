package com.kasikorn.line.core.mobile.banking.service;

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
import com.kasikorn.line.core.mobile.banking.service.account.AccountServiceImpl;
import com.kasikorn.line.core.mobile.banking.service.helper.GeneralUtil;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    @Mock
    AccountsRepository accountsRepository;
    @Mock
    AccountFlagsRepository accountFlagsRepository;
    @Mock
    GeneralUtil generalUtil;
    @InjectMocks
    AccountServiceImpl accountService;
    private List<AccountsEntity> accountsEntities;

    @BeforeEach
    public void setup() {
        accountsEntities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        AccountsEntity account1 = new AccountsEntity();
        account1.setAccountId("u001-l001");
        account1.setUserId("user001");
        account1.setAccountName("House Loan");
        account1.setColor("#AAAAAA");
        account1.setIsMainAccount(false);
        account1.setAccountStatus(AccountStatusEnum.ACTIVE);
        account1.setType(AccountTypeEnum.LOAN);
        account1.setCurrency(CurrencyEnum.USD);
        account1.setAccountBalance(BigDecimal.valueOf(0.00));
        account1.setAccountNumber("568-2-21484-9");
        account1.setIssuer("BankD");
        account1.setDueDate(LocalDateTime.parse("2027-01-01 00:00:00", formatter));
        account1.setPaymentAccml(BigDecimal.valueOf(5000.00));
        account1.setInterestAccml(BigDecimal.valueOf(1500.00));
        account1.setRemaining(BigDecimal.valueOf(45000.00));
        account1.setLoanAmount(BigDecimal.valueOf(50000.00));
        account1.setInterestRate(BigDecimal.valueOf(5.50));
        account1.setLoanTerm(24);
        account1.setLoanStartDate(LocalDateTime.parse("2025-01-01 00:00:00", formatter));
        account1.setCreatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        account1.setUpdatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        accountsEntities.add(account1);

        AccountsEntity account2 = new AccountsEntity();
        account2.setAccountId("u001-s001");
        account2.setUserId("user001");
        account2.setAccountName("Saving");
        account2.setColor("#FF0000");
        account2.setIsMainAccount(true);
        account2.setAccountStatus(AccountStatusEnum.ACTIVE);
        account2.setType(AccountTypeEnum.SAVING);
        account2.setCurrency(CurrencyEnum.THB);
        account2.setAccountBalance(BigDecimal.valueOf(20000.00));
        account2.setAccountNumber("568-2-45295-8");
        account2.setIssuer("BankA");
        account2.setCreatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        account2.setUpdatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        accountsEntities.add(account2);

        AccountsEntity account3 = new AccountsEntity();
        account3.setAccountId("u001-s002");
        account3.setUserId("user001");
        account3.setAccountName("Second Saving");
        account3.setColor("#FF9900");
        account3.setIsMainAccount(false);
        account3.setAccountStatus(AccountStatusEnum.ACTIVE);
        account3.setType(AccountTypeEnum.SAVING);
        account3.setCurrency(CurrencyEnum.THB);
        account3.setAccountBalance(BigDecimal.valueOf(15000.00));
        account3.setAccountNumber("568-2-77363-5");
        account3.setIssuer("BankA");
        account3.setCreatedAt(LocalDateTime.parse("2025-02-07 02:56:46", formatter));
        account3.setUpdatedAt(LocalDateTime.parse("2025-02-07 02:56:46", formatter));
        accountsEntities.add(account3);

        AccountsEntity account4 = new AccountsEntity();
        account4.setAccountId("u001-sg001");
        account4.setUserId("user001");
        account4.setAccountName("To the sea");
        account4.setColor("#00FF00");
        account4.setIsMainAccount(false);
        account4.setProgress(40);
        account4.setAccountStatus(AccountStatusEnum.ACTIVE);
        account4.setType(AccountTypeEnum.SAVING_GOAL);
        account4.setCurrency(CurrencyEnum.THB);
        account4.setAccountBalance(BigDecimal.valueOf(40000.00));
        account4.setSavingGoal(BigDecimal.valueOf(100000.00));
        account4.setAccountNumber("568-2-82678-9");
        account4.setIssuer("BankB");
        account4.setCreatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        account4.setUpdatedAt(LocalDateTime.parse("2025-02-07 02:56:45", formatter));
        accountsEntities.add(account4);

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getPrincipal()).thenReturn("user001");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAccountFetch_Success() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));

        when(accountsRepository.findTopByUserId("user001",pageable)).thenReturn(accountsEntities);
        for (AccountsEntity account : accountsEntities) {
            when(accountFlagsRepository.findByAccountId(account.getAccountId())).thenReturn(new ArrayList<>());
        }
        FetchAccountRequest request = new FetchAccountRequest();
        FetchAccountResponse response = accountService.fetch(request);
        assertNotNull(response);
        assertNotNull(response.getAccounts());
        assertEquals(accountsEntities.size(), response.getAccounts().size());
        accountsEntities.forEach(account -> verify(generalUtil).loanInterestUpdate(account));
        AccountDTO firstAccount = response.getAccounts().getFirst();
        assertEquals("u001-s002", firstAccount.getAccountId());
    }

    @Test
    public void testCreateAccount_Saving() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.SAVING.name());
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setAccountName("Test Saving Account");
        request.setColor("#FF0000");
        request.setCurrency("USD");
        request.setIsMain(true);
        when(accountsRepository.save(any(AccountsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAccountResponse response = accountService.createAccount(request);
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("Test Saving Account", response.getAccountName());
        assertEquals("user001", response.getUserId());
        assertEquals("#FF0000", response.getColor());
        assertTrue(response.getIsMainAccount());
        assertEquals(AccountTypeEnum.SAVING, response.getType());
        assertEquals(CurrencyEnum.USD, response.getCurrency());
        assertEquals(new BigDecimal("1000.00"), response.getAccountBalance());
        assertNull(response.getSavingGoal());
    }

    @Test
    public void testCreateAccount_Loan() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.LOAN.name());
        request.setAccountName("Test Loan Account");
        request.setColor("#0000FF");
        request.setCurrency("USD");
        request.setLoanTerm(12);
        request.setLoanAmount(new BigDecimal("5000.00"));
        when(accountsRepository.save(any(AccountsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAccountResponse response = accountService.createAccount(request);
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("Test Loan Account", response.getAccountName());
        assertEquals("user001", response.getUserId());
        assertEquals("#0000FF", response.getColor());
        assertFalse(response.getIsMainAccount());
        assertEquals(AccountTypeEnum.LOAN, response.getType());
        assertEquals(CurrencyEnum.USD, response.getCurrency());
        assertNull(response.getAccountBalance());
        assertNull(response.getSavingGoal());
        assertEquals(new BigDecimal("5000.00"), response.getLoanAmount());
        assertEquals(new BigDecimal("10"), response.getInterestRate());
        assertEquals(12, response.getLoanTerm().intValue());
        assertNotNull(response.getLoanStartDate());
        assertNotNull(response.getDueDate());
    }

    @Test
    public void testCreateAccount_SavingGoal() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.SAVING_GOAL.name());
        request.setAccountName("Test Saving Goal Account");
        request.setColor("#00FF00");
        request.setCurrency("USD");
        request.setInitialBalance(new BigDecimal("2000.00"));
        request.setSavingGoal(new BigDecimal("10000.00"));
        when(accountsRepository.save(any(AccountsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAccountResponse response = accountService.createAccount(request);
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("Test Saving Goal Account", response.getAccountName());
        assertEquals("user001", response.getUserId());
        assertEquals("#00FF00", response.getColor());
        assertFalse(response.getIsMainAccount());
        assertEquals(AccountTypeEnum.SAVING_GOAL, response.getType());
        assertEquals(CurrencyEnum.USD, response.getCurrency());
        assertEquals(new BigDecimal("2000.00"), response.getAccountBalance());
        assertEquals(new BigDecimal("10000.00"), response.getSavingGoal());
        assertEquals(20, response.getProgress().intValue());
        assertNull(response.getLoanAmount());
        assertNull(response.getInterestRate());
        assertNull(response.getLoanTerm());
        assertNull(response.getLoanStartDate());
        assertNull(response.getDueDate());
    }

    @Test
    public void testCreateFlag_Success() {
        CreateFlagRequest request = new CreateFlagRequest();
        request.setAccountId("acc123");
        request.setFlagValue("test-flag");
        AccountsEntity dummyAccount = AccountsEntity.builder()
                .accountId("acc123")
                .userId("user001")
                .build();
        when(accountsRepository.findByAccountId("acc123"))
                .thenReturn(Optional.of(dummyAccount));
        when(accountFlagsRepository.save(any(AccountFlagsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateFlagResponse response = accountService.createFlag(request);
        assertNotNull(response);
        assertEquals("acc123", response.getAccountId());
        assertEquals("test-flag", response.getFlagValue());
    }

    @Test
    public void testCreateAccount_SavingGoalValidationFail() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.SAVING_GOAL.name());
        request.setAccountName("Invalid Saving Goal Account");
        request.setColor("#00FF00");
        request.setCurrency("USD");
        request.setInitialBalance(new BigDecimal("2000.00"));
        request.setSavingGoal(BigDecimal.ZERO);
        assertThrows(ValidationException.class, () -> accountService.createAccount(request));
    }

    @Test
    public void testCreateAccount_LoanValidationFail_LoanAmount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.LOAN.name());
        request.setAccountName("Invalid Loan Account");
        request.setColor("#0000FF");
        request.setCurrency("USD");
        request.setLoanTerm(12);
        request.setLoanAmount(null);
        assertThrows(ValidationException.class, () -> accountService.createAccount(request));
    }

    @Test
    public void testCreateAccount_LoanValidationFail_LoanTerm() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.LOAN.name());
        request.setAccountName("Invalid Loan Account");
        request.setColor("#0000FF");
        request.setCurrency("USD");
        request.setLoanTerm(0);
        request.setLoanAmount(new BigDecimal("5000.00"));
        assertThrows(ValidationException.class, () -> accountService.createAccount(request));
    }

    @Test
    public void testCreateFlag_InvalidUser() {
        CreateFlagRequest request = new CreateFlagRequest();
        request.setAccountId("accInvalidUser");
        request.setFlagValue("test-flag");
        AccountsEntity dummyAccount = AccountsEntity.builder()
                .accountId("accInvalidUser")
                .userId("otherUser")
                .type(AccountTypeEnum.SAVING)
                .build();
        when(accountsRepository.findByAccountId("accInvalidUser")).thenReturn(Optional.of(dummyAccount));
        assertThrows(ValidationException.class, () -> accountService.createFlag(request));
    }

    @Test
    public void testCreateFlag_InvalidFlagValue() {
        CreateFlagRequest request = new CreateFlagRequest();
        request.setAccountId("accInvalidFlag");
        request.setFlagValue("Disbursement");
        AccountsEntity dummyAccount = AccountsEntity.builder()
                .accountId("accInvalidFlag")
                .userId("user001")
                .type(AccountTypeEnum.SAVING)
                .build();
        when(accountsRepository.findByAccountId("accInvalidFlag")).thenReturn(Optional.of(dummyAccount));
        assertThrows(ValidationException.class, () -> accountService.createFlag(request));
    }

    @Test
    public void testCreateFlag_AccountNotExists() {
        CreateFlagRequest request = new CreateFlagRequest();
        request.setAccountId("nonExistingAcc");
        request.setFlagValue("test-flag");
        when(accountsRepository.findByAccountId("nonExistingAcc")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> accountService.createFlag(request));
    }

    @Test
    public void testGenerateUniqueAccountNumber_Failure() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setType(AccountTypeEnum.SAVING.name());
        request.setAccountName("Test Duplicate Account Number");
        request.setColor("#FFFFFF");
        request.setCurrency("USD");
        request.setInitialBalance(new BigDecimal("100.00"));
        request.setIsMain(true);
        when(accountsRepository.existsByAccountNumber(anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> accountService.createAccount(request));
    }
}
