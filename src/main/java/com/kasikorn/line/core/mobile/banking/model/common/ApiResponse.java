package com.kasikorn.line.core.mobile.banking.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private ResponseStatus responseStatus;
    private Object data;
}
