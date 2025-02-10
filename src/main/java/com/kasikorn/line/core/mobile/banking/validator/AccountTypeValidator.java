package com.kasikorn.line.core.mobile.banking.validator;

import com.kasikorn.line.core.mobile.banking.enums.AccountTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class AccountTypeValidator implements ConstraintValidator<ValidAccountType, String> {

    @Override
    public boolean isValid(String accountType, ConstraintValidatorContext context) {
        boolean isValid = accountType != null && Arrays.stream(AccountTypeEnum.values())
                .anyMatch(type -> type.name().equalsIgnoreCase(accountType));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid account type")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
