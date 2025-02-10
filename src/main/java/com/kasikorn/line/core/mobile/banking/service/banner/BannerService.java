package com.kasikorn.line.core.mobile.banking.service.banner;

import com.kasikorn.line.core.mobile.banking.model.getbanner.FetchBannerResponse;

public interface BannerService {
    FetchBannerResponse getBannerByUserId();
    String initBanner(String userId);
}
