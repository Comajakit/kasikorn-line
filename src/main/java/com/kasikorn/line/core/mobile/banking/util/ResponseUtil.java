package com.kasikorn.line.core.mobile.banking.util;

import com.kasikorn.line.core.mobile.banking.model.common.ApiResponse;
import com.kasikorn.line.core.mobile.banking.model.common.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseUtil {

    public static ApiResponse responseSuccess(Object data){
        ApiResponse apiResponse = new ApiResponse();
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatus("000");
        responseStatus.setMessage("Success");
        apiResponse.setResponseStatus(responseStatus);
        apiResponse.setData(data);
        return apiResponse;
    }
}
