package com.kasikorn.line.core.mobile.banking.service.user;


import com.kasikorn.line.core.mobile.banking.entity.UserEntity;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinRequest;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinResponse;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserRequest;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserResponse;
import com.kasikorn.line.core.mobile.banking.repository.UserRepository;
import com.kasikorn.line.core.mobile.banking.service.banner.BannerService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BannerService bannerService;

    @Override
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        log.info("##### User Service: Create User #####");
        log.info("**** Incoming Request: {} ****", request);
        String userId = request.getUserId();
        validateCreateUserRequest(request);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String bannerId = bannerService.initBanner(userId);
        String greeting = "Have a nice day " + request.getName();
        UserEntity user = UserEntity.builder()
                .userId(userId)
                .name(request.getName())
                .bannerId(bannerId)
                .hashedPassword(hashedPassword)
                .greeting(greeting)
                .build();
        UserEntity savedUser = userRepository.save(user);
        log.info("*** User Created ***");
        CreateUserResponse response = CreateUserResponse.builder()
                .userId(savedUser.getUserId())
                .name(savedUser.getName())
                .bannerId(savedUser.getBannerId())
                .greeting(savedUser.getGreeting())
                .build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    @Override
    public CreatePinResponse createPin(CreatePinRequest request) {
        log.info("##### User Service: Create User PIN #####");
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        String hashedPin = passwordEncoder.encode(request.getPin());
        user.setHashedPin(hashedPin);
        userRepository.save(user);
        log.info("*** PIN Created ***");
        CreatePinResponse response = CreatePinResponse.builder()
                .userId(user.getUserId())
                .build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        boolean isExisted = userRepository.existsById(request.getUserId());
        if (isExisted) {
            throw new ValidationException("User already existed");
        }
    }
}
