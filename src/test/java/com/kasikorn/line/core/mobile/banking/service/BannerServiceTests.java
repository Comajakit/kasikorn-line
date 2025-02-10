package com.kasikorn.line.core.mobile.banking.service;

import com.kasikorn.line.core.mobile.banking.entity.BannersEntity;
import com.kasikorn.line.core.mobile.banking.model.getbanner.FetchBannerResponse;
import com.kasikorn.line.core.mobile.banking.repository.BannersRepository;
import com.kasikorn.line.core.mobile.banking.service.banner.BannerServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BannerServiceTests {

    @Mock
    private BannersRepository bannerRepository;

    @InjectMocks
    private BannerServiceImpl bannerService;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetBannerByUserId_WithBanners() {
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getPrincipal()).thenReturn("user001");
        BannersEntity banner1 = new BannersEntity();
        banner1.setBannerId("banner1");
        banner1.setUserId("user001");
        banner1.setTitle("Title1");
        banner1.setDescription("Desc1");
        banner1.setUpdatedAt(LocalDateTime.of(2025, 2, 7, 12, 0));

        BannersEntity banner2 = new BannersEntity();
        banner2.setBannerId("banner2");
        banner2.setUserId("user001");
        banner2.setTitle("Title2");
        banner2.setDescription("Desc2");
        banner2.setUpdatedAt(LocalDateTime.of(2025, 2, 7, 13, 0));

        List<BannersEntity> banners = Arrays.asList(banner1, banner2);
        when(bannerRepository.findByUserId("user001")).thenReturn(Optional.of(banners));

        FetchBannerResponse response = bannerService.getBannerByUserId();
        assertNotNull(response);
        assertNotNull(response.getBanners());
        assertEquals(2, response.getBanners().size());
        assertEquals("Title2", response.getBanners().get(0).getTitle());
        assertEquals("Desc2", response.getBanners().get(0).getDescription());
        assertEquals("Title1", response.getBanners().get(1).getTitle());
        assertEquals("Desc1", response.getBanners().get(1).getDescription());
    }

    @Test
    public void testGetBannerByUserId_NoBanners() {
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getPrincipal()).thenReturn("user001");
        when(bannerRepository.findByUserId("user001")).thenReturn(Optional.empty());
        FetchBannerResponse response = bannerService.getBannerByUserId();
        assertNotNull(response);
        assertNotNull(response.getBanners());
        assertTrue(response.getBanners().isEmpty());
    }

    @Test
    public void testInitBanner() {
        when(bannerRepository.save(any(BannersEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        String bannerId = bannerService.initBanner("user001");
        assertNotNull(bannerId);
        verify(bannerRepository, times(1)).save(argThat(entity ->
                entity.getBannerId().equals(bannerId) &&
                        entity.getUserId().equals("user001") &&
                        "Want some money?".equals(entity.getTitle()) &&
                        "You can start applying".equals(entity.getDescription()) &&
                        "https://dummyimage.com/54x54/999/fff".equals(entity.getImage())
        ));
    }
}
