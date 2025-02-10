package com.kasikorn.line.core.mobile.banking.model.getbanner;

import com.kasikorn.line.core.mobile.banking.dto.BannerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchBannerResponse {
    private List<BannerDTO> banners;
}
