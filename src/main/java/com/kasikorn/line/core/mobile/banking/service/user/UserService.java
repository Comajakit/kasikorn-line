package com.kasikorn.line.core.mobile.banking.service.user;

import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinRequest;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinResponse;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserRequest;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserResponse;

public interface UserService {
    CreateUserResponse createUser(CreateUserRequest request);
    CreatePinResponse createPin(CreatePinRequest request);
}
