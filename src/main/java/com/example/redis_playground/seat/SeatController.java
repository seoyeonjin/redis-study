package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    public static final String USER_ID_HEADER = "X-USER-ID";
    private final SeatReserveService seatReserveService;
    private final SeatQueueService seatQueueService;

    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<Void> reserve(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        seatReserveService.reserve(seatId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{seatId}/queue")
    public ResponseEntity<Long> joinQueue(
            @PathVariable Long seatId,
            @RequestHeader("X-USER-ID") String userId
    ) {
        final Long order = seatQueueService.joinQueue(seatId, userId);
        return ResponseEntity.ok(order);
    }
}
