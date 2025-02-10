package com.kasikorn.line.core.mobile.banking.service.auth;

import com.kasikorn.line.core.mobile.banking.model.login.LoginRequest;
import com.kasikorn.line.core.mobile.banking.model.login.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

}
