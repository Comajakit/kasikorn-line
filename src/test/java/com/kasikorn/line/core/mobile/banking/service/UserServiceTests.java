package com.kasikorn.line.core.mobile.banking.service;

import com.kasikorn.line.core.mobile.banking.entity.UserEntity;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinRequest;
import com.kasikorn.line.core.mobile.banking.model.createpin.CreatePinResponse;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserRequest;
import com.kasikorn.line.core.mobile.banking.model.createuser.CreateUserResponse;
import com.kasikorn.line.core.mobile.banking.repository.UserRepository;
import com.kasikorn.line.core.mobile.banking.service.banner.BannerService;
import com.kasikorn.line.core.mobile.banking.service.user.UserServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BannerService bannerService;
    @InjectMocks
    private UserServiceImpl userService;


    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateUserSuccess() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("user001");
        request.setName("John");
        request.setPassword("password123");
        when(userRepository.existsById("user001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(bannerService.initBanner("user001")).thenReturn("banner123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateUserResponse response = userService.createUser(request);
        assertNotNull(response);
        assertEquals("user001", response.getUserId());
        assertEquals("John", response.getName());
        assertEquals("banner123", response.getBannerId());
        assertEquals("Have a nice day John", response.getGreeting());
    }

    @Test
    public void testCreateUserValidationFail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("user001");
        request.setName("John");
        request.setPassword("password123");
        when(userRepository.existsById("user001")).thenReturn(true);
        assertThrows(ValidationException.class, () -> userService.createUser(request));
    }

    @Test
    public void testCreatePinSuccess() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        CreatePinRequest request = new CreatePinRequest();
        request.setPin("1234");
        when(auth.getPrincipal()).thenReturn("user001");
        UserEntity userEntity = UserEntity.builder().userId("user001").build();
        when(userRepository.findById("user001")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("1234")).thenReturn("hashedPin");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreatePinResponse response = userService.createPin(request);
        assertNotNull(response);
        assertEquals("user001", response.getUserId());
        assertEquals("hashedPin", userEntity.getHashedPin());
    }

    @Test
    public void testCreatePinUserNotFound() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        CreatePinRequest request = new CreatePinRequest();
        request.setPin("1234");
        when(auth.getPrincipal()).thenReturn("user001");
        when(userRepository.findById("user001")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createPin(request));
        assertEquals("User not found with id: user001", exception.getMessage());
    }
}
