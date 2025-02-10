package com.kasikorn.line.core.mobile.banking.service.banner;

import com.kasikorn.line.core.mobile.banking.dto.BannerDTO;
import com.kasikorn.line.core.mobile.banking.entity.BannersEntity;
import com.kasikorn.line.core.mobile.banking.model.getbanner.FetchBannerResponse;
import com.kasikorn.line.core.mobile.banking.repository.BannersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannersRepository bannerRepository;

    @Override
    public FetchBannerResponse getBannerByUserId() {
        log.info("##### Banner Service: Get user banner #####");
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching banner for userId: {}", userId);

        log.info("*** Query Banner: findByUserId ***");
        List<BannerDTO> bannerDTOS = bannerRepository.findByUserId(userId)
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(BannersEntity::getUpdatedAt).reversed())
                .map(entity -> BannerDTO.builder()
                        .title(entity.getTitle())
                        .description(entity.getDescription())
                        .build())
                .collect(Collectors.toList());
        FetchBannerResponse response = FetchBannerResponse.builder()
                .banners(bannerDTOS)
                .build();
        log.info("**** Outgoing Response: {} ****", response);
        return response;
    }

    @Override
    @Transactional
    public String initBanner(String userId) {
        log.info("**** Insert initial banner for userId: {} ****",userId);
        String bannerId = UUID.randomUUID().toString();
        BannersEntity bannersEntity = new BannersEntity();
        bannersEntity.setBannerId(bannerId);
        bannersEntity.setUserId(userId);
        bannersEntity.setTitle("Want some money?");
        bannersEntity.setDescription("You can start applying");
        bannersEntity.setImage("https://dummyimage.com/54x54/999/fff");
        bannerRepository.save(bannersEntity);

        return bannerId;

    }
}