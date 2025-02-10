package com.kasikorn.line.core.mobile.banking.service.debitcard;

import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.createdebitcard.CreateDebitCardResponse;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchdebit.FetchDebitCardResponse;

public interface DebitCardService {
    FetchDebitCardResponse fetchDebitCard(FetchDebitCardRequest request);
    CreateDebitCardResponse createDebit(CreateDebitCardRequest request);
}
