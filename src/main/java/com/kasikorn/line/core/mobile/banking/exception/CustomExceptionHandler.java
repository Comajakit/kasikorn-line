package com.kasikorn.line.core.mobile.banking.exception;

import com.kasikorn.line.core.mobile.banking.model.common.ApiResponse;
import com.kasikorn.line.core.mobile.banking.model.common.ResponseStatus;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleRequestValidationException(MethodArgumentNotValidException ex) {
        ApiResponse apiResponse = new ApiResponse();
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setMessage(ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
        responseStatus.setStatus("999");
        apiResponse.setResponseStatus(responseStatus);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex) {
        ApiResponse apiResponse = new ApiResponse();
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setMessage(ex.getMessage());
        responseStatus.setStatus("999");
        apiResponse.setResponseStatus(responseStatus);

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}