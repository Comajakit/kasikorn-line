package com.kasikorn.line.core.mobile.banking.service;

import com.kasikorn.line.core.mobile.banking.dto.TransactionDTO;
import com.kasikorn.line.core.mobile.banking.entity.AccountsEntity;
import com.kasikorn.line.core.mobile.banking.entity.TransactionsEntity;
import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnResponse;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentRequest;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentResponse;
import com.kasikorn.line.core.mobile.banking.repository.AccountsRepository;
import com.kasikorn.line.core.mobile.banking.repository.TransactionsRepository;
import com.kasikorn.line.core.mobile.banking.service.helper.GeneralUtil;
import com.kasikorn.line.core.mobile.banking.service.transaction.TransactionServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock
    private TransactionsRepository transactionsRepository;
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private GeneralUtil generalUtil;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    public void setUp() {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
        when(auth.getPrincipal()).thenReturn("user001");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFetchRecent() {
        TransactionsEntity txn1 = new TransactionsEntity();
        txn1.setTransactionId("txn1");
        txn1.setName("Txn One");
        txn1.setUserId("user001");
        txn1.setImage("img1");
        txn1.setIsBank(true);
        LocalDateTime createdAt1 = LocalDateTime.now().minusDays(1);
        txn1.setCreatedAt(createdAt1);

        TransactionsEntity txn2 = new TransactionsEntity();
        txn2.setTransactionId("txn2");
        txn2.setName("Txn Two");
        txn2.setUserId("user001");
        txn2.setImage("img2");
        txn2.setIsBank(false);
        LocalDateTime createdAt2 = LocalDateTime.now();
        txn2.setCreatedAt(createdAt2);

        List<TransactionsEntity> txnList = Arrays.asList(txn1, txn2);
        when(transactionsRepository.findTopByUserId(eq("user001"), any(Pageable.class))).thenReturn(txnList);

        FetchRecentTxnRequest request = new FetchRecentTxnRequest();
        request.setLimit(10);
        request.setPage(0);
        FetchRecentTxnResponse response = transactionService.fetchRecent(request);
        assertNotNull(response);
        assertNotNull(response.getTxnList());
        assertEquals(2, response.getTxnList().size());

        TransactionDTO dto1 = response.getTxnList().getFirst();
        assertEquals("txn1", dto1.getTransactionId());
        assertEquals("Txn One", dto1.getName());
        assertEquals("img1", dto1.getImage());
        assertTrue(dto1.getIsBank());
    }

    @Test
    public void testPayment_InvalidTransactionType() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("acc001");
        request.setTransactionType("invalid");
        request.setAmount(new BigDecimal("100"));
        request.setName("Payment Test");

        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.payment(request));
        assertEquals("Invalid transaction type: invalid", ex.getMessage());
    }

    @Test
    public void testPayment_AccountNotFound() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accNotFound");
        request.setTransactionType("DEPOSIT");
        request.setAmount(new BigDecimal("50"));
        request.setName("Payment Test");

        when(accountsRepository.findById("accNotFound")).thenReturn(Optional.empty());
        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.payment(request));
        assertEquals("Account not found: accNotFound", ex.getMessage());
    }

    @Test
    public void testPayment_SavingDeposit() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accSaving");
        request.setTransactionType("DEPOSIT");
        request.setAmount(new BigDecimal("50"));
        request.setName("Deposit Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accSaving");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        account.setAccountBalance(new BigDecimal("100"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accSaving")).thenReturn(Optional.of(account));
        when(accountsRepository.save(any(AccountsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionsRepository.save(any(TransactionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = transactionService.payment(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("150"), account.getAccountBalance());
    }

    @Test
    public void testPayment_SavingWithdraw_Sufficient() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accSaving");
        request.setTransactionType("WITHDRAW");
        request.setAmount(new BigDecimal("50"));
        request.setName("Withdraw Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accSaving");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        account.setAccountBalance(new BigDecimal("100"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accSaving")).thenReturn(Optional.of(account));
        when(accountsRepository.save(any(AccountsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionsRepository.save(any(TransactionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = transactionService.payment(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("50"), account.getAccountBalance());
    }

    @Test
    public void testPayment_SavingWithdraw_Insufficient() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accSaving");
        request.setTransactionType("WITHDRAW");
        request.setAmount(new BigDecimal("150"));
        request.setName("Withdraw Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accSaving");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING);
        account.setAccountBalance(new BigDecimal("100"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accSaving")).thenReturn(Optional.of(account));

        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.payment(request));
        assertEquals("Insufficient balance for this transaction", ex.getMessage());
    }

    @Test
    public void testPayment_SavingGoalDeposit() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accGoal");
        request.setTransactionType("DEPOSIT");
        request.setAmount(new BigDecimal("100"));
        request.setName("Deposit Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accGoal");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING_GOAL);
        account.setAccountBalance(new BigDecimal("200"));
        account.setSavingGoal(new BigDecimal("1000"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accGoal")).thenReturn(Optional.of(account));
        when(accountsRepository.save(any(AccountsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionsRepository.save(any(TransactionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = transactionService.payment(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("300"), account.getAccountBalance());
        assertEquals(30, account.getProgress().intValue());
    }

    @Test
    public void testPayment_SavingGoalWithdraw_Sufficient() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accGoal");
        request.setTransactionType("WITHDRAW");
        request.setAmount(new BigDecimal("100"));
        request.setName("Withdraw Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accGoal");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING_GOAL);
        account.setAccountBalance(new BigDecimal("200"));
        account.setSavingGoal(new BigDecimal("1000"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accGoal")).thenReturn(Optional.of(account));
        when(accountsRepository.save(any(AccountsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionsRepository.save(any(TransactionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = transactionService.payment(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("100"), account.getAccountBalance());
        assertEquals(10, account.getProgress().intValue());
    }

    @Test
    public void testPayment_SavingGoalWithdraw_Insufficient() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accGoal");
        request.setTransactionType("WITHDRAW");
        request.setAmount(new BigDecimal("250"));
        request.setName("Withdraw Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accGoal");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.SAVING_GOAL);
        account.setAccountBalance(new BigDecimal("200"));
        account.setSavingGoal(new BigDecimal("1000"));
        account.setIssuer("SomeIssuer");

        when(accountsRepository.findById("accGoal")).thenReturn(Optional.of(account));

        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.payment(request));
        assertEquals("Insufficient balance for this transaction", ex.getMessage());
    }

    @Test
    public void testPayment_Loan() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accLoan");
        request.setTransactionType("PAYMENT");
        request.setAmount(new BigDecimal("40"));
        request.setName("Loan Payment");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accLoan");
        account.setUserId("user001");
        account.setType(AccountTypeEnum.LOAN);
        account.setIssuer("SomeIssuer");
        account.setInterestAccml(new BigDecimal("30"));
        account.setRemaining(new BigDecimal("200"));
        account.setPaymentAccml(BigDecimal.ZERO);

        when(accountsRepository.findById("accLoan")).thenReturn(Optional.of(account));
        when(accountsRepository.save(any(AccountsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionsRepository.save(any(TransactionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = transactionService.payment(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("0"), account.getInterestAccml());
        assertEquals(new BigDecimal("190"), account.getRemaining());
        assertEquals(new BigDecimal("40"), account.getPaymentAccml());
        verify(generalUtil).loanInterestUpdate(account);
    }

    @Test
    public void testPayment_UnknownAccountType() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountId("accUnknown");
        request.setTransactionType("DEPOSIT");
        request.setAmount(new BigDecimal("50"));
        request.setName("Payment Test");

        AccountsEntity account = new AccountsEntity();
        account.setAccountId("accUnknown");
        account.setUserId("user001");
        // Create a dummy AccountTypeEnum using a mock to simulate an unknown type.
        AccountTypeEnum unknownType = mock(AccountTypeEnum.class);
        when(unknownType.name()).thenReturn("UNKNOWN");
        account.setType(unknownType);

        when(accountsRepository.findById("accUnknown")).thenReturn(Optional.of(account));

        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.payment(request));
        assertEquals("Unknown account type", ex.getMessage());
    }
}
