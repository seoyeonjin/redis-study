package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatHoldStatusService {

    private final StringRedisTemplate redisTemplate;

    public HoldStatusResponse getStatus(final Long seatId, final String userId) {

        final String holdKey = "seat:hold:" + seatId + ":" + userId;

        final Boolean exists = redisTemplate.hasKey(holdKey);

        if (Boolean.TRUE.equals(exists)) {
            final Long ttl = redisTemplate.getExpire(holdKey);
            return new HoldStatusResponse("HOLDING", ttl);
        }

        return new HoldStatusResponse("WAITING", null);
    }
}
