package com.kasikorn.line.core.mobile.banking.model.fetchaccount;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchAccountRequest {
    @Min(value = 1, message = "Limit must be at least 1")
    @Nullable
    private Integer limit;

    @Min(value = 0, message = "Page must be at least 0")
    @Nullable
    private Integer page;
}
