package com.example.redis_playground.seat.scheduler;

import com.example.redis_playground.seat.processor.SeatQueueProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
@Profile({"queue", "local"})
@RequiredArgsConstructor
public class SeatQueueScheduler {

    private static final String ACTIVE_SEAT_QUEUE_KEY = "queue:seat:active";
    private static final String LOCK_KEY_PREFIX = "lock:seat:";
    private static final long SCHEDULER_INTERVAL_MS = 3000;
    private static final long LOCK_TTL_MS = 2500;

    private final StringRedisTemplate redisTemplate;
    private final SeatQueueProcessor seatQueueProcessor;

    @Scheduled(fixedDelay = SCHEDULER_INTERVAL_MS)
    public void process() {
        Set<String> seatIds = redisTemplate.opsForSet().members(ACTIVE_SEAT_QUEUE_KEY);
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        for (String seatIdStr : seatIds) {
            String lockKey = LOCK_KEY_PREFIX + seatIdStr;
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofMillis(LOCK_TTL_MS));

            if (Boolean.TRUE.equals(locked)) {
                try {
                    Long seatId = Long.parseLong(seatIdStr);
                    seatQueueProcessor.processTick(seatId);
                } catch (Exception e) {
                    log.error("[SEAT_QUEUE_FAIL] seatId={} error={}", seatIdStr, e.getMessage(), e);
                }
            } else {
                log.trace("Could not acquire lock for seatId={}, another process is likely handling it.", seatIdStr);
            }
        }
    }
}
