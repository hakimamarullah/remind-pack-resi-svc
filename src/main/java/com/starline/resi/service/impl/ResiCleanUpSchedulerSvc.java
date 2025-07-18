package com.starline.resi.service.impl;

import com.starline.resi.repository.ResiRepository;
import com.starline.resi.service.ResiCleanUpScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResiCleanUpSchedulerSvc implements ResiCleanUpScheduler {

    private final ResiRepository resiRepository;

    @Value("${resi.max.days.active:10}")
    private int maxDaysActive;

    @Transactional
    @Modifying
    @Scheduled(cron = "${cron.resi-cleanup:59 59 22 * * *}", zone = "Asia/Jakarta")
    @Override
    public void cleanUpOldResi() {
        log.info("[✓] Starting to delete Resi entries older than {} days", maxDaysActive);
        LocalDateTime cutOffDate = LocalDateTime.now().minusDays(maxDaysActive);
        int deleted = resiRepository.deleteAllByCreatedDateBefore(cutOffDate);
        log.info("[✓] Deleted {} Resi entries older than {}", deleted, cutOffDate);
    }

    @Transactional
    @Modifying
    @Scheduled(cron = "${cron.awb-subscription-expired-cleanup:59 59 23 * * *}", zone = "Asia/Jakarta")
    @Override
    public void cleanUpResiWithExpiredSubscription() {
        LocalDateTime cutOffDate = LocalDateTime.now();
        log.info("[✓] Starting to delete AWB with expired subscription. Run on {}", cutOffDate);
        int deleted = resiRepository.deleteAllBySubscriptionExpiryDateBefore(cutOffDate);
        log.info("[✓] Deleted {} AWB entries with subscription expired date before {}", deleted, cutOffDate);
    }
}
