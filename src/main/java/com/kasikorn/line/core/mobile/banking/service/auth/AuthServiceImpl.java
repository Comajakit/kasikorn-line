package com.kasikorn.line.core.mobile.banking.service.auth;

import com.kasikorn.line.core.mobile.banking.entity.UserEntity;
import com.kasikorn.line.core.mobile.banking.model.login.LoginRequest;
import com.kasikorn.line.core.mobile.banking.model.login.LoginResponse;
import com.kasikorn.line.core.mobile.banking.repository.UserRepository;
import com.kasikorn.line.core.mobile.banking.service.helper.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("##### Auth Service: login #####");
        log.info("*** Query User: findById ***");
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        log.info("*** Query Result: {} ***", user);
        if ((request.getPassword() == null || request.getPassword().trim().isEmpty()) &&
                (request.getPin() == null || request.getPin().trim().isEmpty())) {
            throw new RuntimeException("Either password or PIN must be provided");
        }
        boolean authenticated = false;

        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getUserId());
        String token = authUtil.generateToken(claims);
        LoginResponse response = LoginResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .token(token)
                .bannerId(user.getBannerId())
                .greeting(user.getGreeting())
                .message("Login successful")
                .build();

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            authenticated = passwordEncoder.matches(request.getPassword(), user.getHashedPassword());
            if(authenticated){
                log.info("**** Outgoing Response: {} ****", response);
                return response;
            }
        }
        if (request.getPin() != null && !request.getPin().trim().isEmpty()) {
            if (user.getHashedPin() == null) {
                throw new RuntimeException("PIN is not set for this user");
            }
            authenticated = passwordEncoder.matches(request.getPin(), user.getHashedPin());
        }

        if (!authenticated) {
            throw new RuntimeException("Invalid credentials provided");
        }
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }
}
