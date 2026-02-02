package com.example.redis_playground.seat;

public record SeatQueueStatusResponse(
        String status,

        // WAITING 전용
        Long ahead,                 // 내 앞에 남은 사람 수
        Long total,                 // 전체 대기 인원
        Long estimatedWaitSeconds,  // 예상 대기 시간 (ETA)

        // HOLDING 전용
        Long ttlSeconds
) {

    public static SeatQueueStatusResponse waiting(
            Long ahead,
            Long total,
            Long estimatedWaitSeconds
    ) {
        return new SeatQueueStatusResponse(
                "WAITING",
                ahead,
                total,
                estimatedWaitSeconds,
                null
        );
    }

    public static SeatQueueStatusResponse holding(Long ttlSeconds) {
        return new SeatQueueStatusResponse(
                "HOLDING",
                null,
                null,
                null,
                ttlSeconds
        );
    }
}
