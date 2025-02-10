package com.kasikorn.line.core.mobile.banking.controller;

import com.kasikorn.line.core.mobile.banking.model.common.ApiResponse;
import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountResponse;
import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardResponse;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagRequest;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagResponse;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinRequest;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinResponse;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserRequest;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnResponse;
import com.kasikorn.line.core.mobile.banking.model.getbanner.FetchBannerResponse;
import com.kasikorn.line.core.mobile.banking.model.login.LoginRequest;
import com.kasikorn.line.core.mobile.banking.model.login.LoginResponse;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentRequest;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentResponse;
import com.kasikorn.line.core.mobile.banking.service.account.AccountService;
import com.kasikorn.line.core.mobile.banking.service.auth.AuthService;
import com.kasikorn.line.core.mobile.banking.service.banner.BannerService;
import com.kasikorn.line.core.mobile.banking.service.debitcard.DebitCardService;
import com.kasikorn.line.core.mobile.banking.service.transaction.TransactionService;
import com.kasikorn.line.core.mobile.banking.service.user.UserService;
import com.kasikorn.line.core.mobile.banking.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mb")
@RequiredArgsConstructor
@Slf4j
public class MobileBankingController {
    private final UserService userService;
    private final AuthService authService;
    private final AccountService accountService;
    private final BannerService bannerService;
    private final TransactionService transactionService;
    private final DebitCardService debitCardService;

    @PostMapping("/user/create")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.createUser(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/user/pin/create")
    public ResponseEntity<ApiResponse> createPin(
            @Valid @RequestBody CreatePinRequest request) {
        CreatePinResponse response = userService.createPin(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/account/fetch")
    public ResponseEntity<ApiResponse> fetchAccount(@Valid @RequestBody FetchAccountRequest request) {
        FetchAccountResponse response = accountService.fetch(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/account/flag/create")
    public ResponseEntity<ApiResponse> createAccountFlag(@Valid @RequestBody CreateFlagRequest request) {
        CreateFlagResponse response = accountService.createFlag(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/account/create")
    public ResponseEntity<ApiResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        CreateAccountResponse response = accountService.createAccount(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/txn/fetch")
    public ResponseEntity<ApiResponse> fetchRecentTxn(@Valid @RequestBody FetchRecentTxnRequest request) {
        FetchRecentTxnResponse response = transactionService.fetchRecent(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse> payment(@Valid @RequestBody PaymentRequest userId) {
        PaymentResponse response = transactionService.payment(userId);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/debit/fetch")
    public ResponseEntity<ApiResponse> fetchDebitCard(@Valid @RequestBody FetchDebitCardRequest request) {
        FetchDebitCardResponse response = debitCardService.fetchDebitCard(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/debit/create")
    public ResponseEntity<ApiResponse> createDebitCard(@Valid @RequestBody CreateDebitCardRequest request) {
        CreateDebitCardResponse response = debitCardService.createDebit(request);
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/banner")
    public ResponseEntity<ApiResponse> getBanner() {
        FetchBannerResponse response = bannerService.getBannerByUserId();
        ApiResponse apiResponse = ResponseUtil.responseSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }


}
