package com.shopease.service;

import com.shopease.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job to clean up expired guest carts.
 * 
 * <p>Guest carts expire after 30 days of inactivity.
 * This job runs daily at 2 AM to remove expired carts.</p>
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GuestCartCleanupJob {

    private final CartRepository cartRepository;

    private static final int GUEST_CART_EXPIRY_DAYS = 30;

    /**
     * Runs daily at 2 AM to delete expired guest carts.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredGuestCarts() {
        log.info("Starting guest cart cleanup job");
        
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(GUEST_CART_EXPIRY_DAYS);
        int deleted = cartRepository.deleteExpiredGuestCarts(expiryDate);
        
        log.info("Deleted {} expired guest carts", deleted);
    }
}
