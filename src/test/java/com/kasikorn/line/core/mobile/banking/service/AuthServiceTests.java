package com.kasikorn.line.core.mobile.banking.service;

import com.kasikorn.line.core.mobile.banking.entity.UserEntity;
import com.kasikorn.line.core.mobile.banking.model.login.LoginRequest;
import com.kasikorn.line.core.mobile.banking.model.login.LoginResponse;
import com.kasikorn.line.core.mobile.banking.repository.UserRepository;
import com.kasikorn.line.core.mobile.banking.service.auth.AuthServiceImpl;
import com.kasikorn.line.core.mobile.banking.service.helper.AuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthUtil authUtil;
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        // No SecurityContext needed as login does not use it.
    }

    @AfterEach
    public void tearDown() {
        // No SecurityContext to clear.
    }

    @Test
    public void testLoginWithPasswordSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user001");
        request.setPassword("password123");
        request.setPin("");
        UserEntity user = UserEntity.builder()
                .userId("user001")
                .name("John")
                .bannerId("banner001")
                .hashedPassword("hashedPassword")
                .hashedPin("hashedPin")
                .greeting("Have a nice day John")
                .build();
        when(userRepository.findById("user001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(authUtil.generateToken(any())).thenReturn("token123");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("user001", response.getUserId());
        assertEquals("John", response.getName());
        assertEquals("banner001", response.getBannerId());
        assertEquals("Have a nice day John", response.getGreeting());
        assertEquals("token123", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testLoginWithPinSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user002");
        request.setPassword("");
        request.setPin("1234");
        UserEntity user = UserEntity.builder()
                .userId("user002")
                .name("Alice")
                .bannerId("banner002")
                .hashedPassword("hashedPassword")
                .hashedPin("hashedPin")
                .greeting("Have a nice day Alice")
                .build();
        when(userRepository.findById("user002")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "hashedPin")).thenReturn(true);
        when(authUtil.generateToken(any())).thenReturn("token456");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("user002", response.getUserId());
        assertEquals("Alice", response.getName());
        assertEquals("banner002", response.getBannerId());
        assertEquals("Have a nice day Alice", response.getGreeting());
        assertEquals("token456", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testLoginWithBothPasswordAndPin_PasswordSucceeds() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user003");
        request.setPassword("pass123");
        request.setPin("1111");
        UserEntity user = UserEntity.builder()
                .userId("user003")
                .name("Bob")
                .bannerId("banner003")
                .hashedPassword("hashedPass")
                .hashedPin("hashedPin")
                .greeting("Have a nice day Bob")
                .build();
        when(userRepository.findById("user003")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashedPass")).thenReturn(true);
        when(authUtil.generateToken(any())).thenReturn("token789");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("user003", response.getUserId());
        assertEquals("Bob", response.getName());
        assertEquals("banner003", response.getBannerId());
        assertEquals("Have a nice day Bob", response.getGreeting());
        assertEquals("token789", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testLoginWithBothPasswordAndPin_PasswordFails_PinSucceeds() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user004");
        request.setPassword("wrongPassword");
        request.setPin("4321");
        UserEntity user = UserEntity.builder()
                .userId("user004")
                .name("Charlie")
                .bannerId("banner004")
                .hashedPassword("hashedPass")
                .hashedPin("hashedPin")
                .greeting("Have a nice day Charlie")
                .build();
        when(userRepository.findById("user004")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPass")).thenReturn(false);
        when(passwordEncoder.matches("4321", "hashedPin")).thenReturn(true);
        when(authUtil.generateToken(any())).thenReturn("token101");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("user004", response.getUserId());
        assertEquals("Charlie", response.getName());
        assertEquals("banner004", response.getBannerId());
        assertEquals("Have a nice day Charlie", response.getGreeting());
        assertEquals("token101", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testLoginFailsDueToEmptyCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user005");
        request.setPassword("   ");
        request.setPin(" ");
        when(userRepository.findById("user005")).thenReturn(Optional.of(UserEntity.builder().userId("user005").build()));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Either password or PIN must be provided", ex.getMessage());
    }

    @Test
    public void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUserId("nonexistent");
        request.setPassword("any");
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User not found with id: nonexistent", ex.getMessage());
    }

    @Test
    public void testLoginFailsWhenPinNotSet() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user006");
        request.setPassword("");
        request.setPin("1234");
        UserEntity user = UserEntity.builder()
                .userId("user006")
                .name("Dana")
                .bannerId("banner006")
                .hashedPassword("hashedPass")
                .hashedPin(null)
                .greeting("Have a nice day Dana")
                .build();
        when(userRepository.findById("user006")).thenReturn(Optional.of(user));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("PIN is not set for this user", ex.getMessage());
    }

    @Test
    public void testLoginFailsInvalidCredentials_Password() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user007");
        request.setPassword("invalid");
        request.setPin("");
        UserEntity user = UserEntity.builder()
                .userId("user007")
                .name("Eve")
                .bannerId("banner007")
                .hashedPassword("hashedPass")
                .hashedPin("hashedPin")
                .greeting("Have a nice day Eve")
                .build();
        when(userRepository.findById("user007")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("invalid", "hashedPass")).thenReturn(false);
        when(authUtil.generateToken(any())).thenReturn("token202");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid credentials provided", ex.getMessage());
    }

    @Test
    public void testLoginFailsInvalidCredentials_Pin() {
        LoginRequest request = new LoginRequest();
        request.setUserId("user008");
        request.setPassword("");
        request.setPin("wrongPin");
        UserEntity user = UserEntity.builder()
                .userId("user008")
                .name("Frank")
                .bannerId("banner008")
                .hashedPassword("hashedPass")
                .hashedPin("hashedPin")
                .greeting("Have a nice day Frank")
                .build();
        when(userRepository.findById("user008")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPin", "hashedPin")).thenReturn(false);
        when(authUtil.generateToken(any())).thenReturn("token303");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid credentials provided", ex.getMessage());
    }
}

