package com.kasikorn.line.core.mobile.banking.validator;

import com.kasikorn.line.core.mobile.banking.enums.TransactionTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, String> {

    @Override
    public boolean isValid(String transactionType, ConstraintValidatorContext context) {
        boolean isValid = transactionType != null && Arrays.stream(TransactionTypeEnum.values())
                .anyMatch(type -> type.name().equalsIgnoreCase(transactionType));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid transaction type")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
