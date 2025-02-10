package com.kasikorn.line.core.mobile.banking.service.transaction;


import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnRequest;
import com.kasikorn.line.core.mobile.banking.model.fetchtransaction.FetchRecentTxnResponse;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentRequest;
import com.kasikorn.line.core.mobile.banking.model.payment.PaymentResponse;

public interface TransactionService {
    FetchRecentTxnResponse fetchRecent(FetchRecentTxnRequest request);
    PaymentResponse payment(PaymentRequest request);
}
