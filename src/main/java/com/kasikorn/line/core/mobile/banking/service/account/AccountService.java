package com.kasikorn.line.core.mobile.banking.service.account;

import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.createaccount.CreateAccountResponse;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagRequest;
import com.kasikorn.line.core.mobile.banking.model.createflag.CreateFlagResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchaccount.FetchAccountResponse;


public interface AccountService {
    FetchAccountResponse fetch(FetchAccountRequest request);
    CreateAccountResponse createAccount(CreateAccountRequest request);
    CreateFlagResponse createFlag(CreateFlagRequest request);
}
