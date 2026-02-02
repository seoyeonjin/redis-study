package com.example.redis_playground.seat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final SeatHoldStatusService seatHoldStatusService;
    private final SeatQueueStatusService seatQueueStatusService;

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
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        final Long order = seatQueueService.joinQueue(seatId, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{seatId}/hold/status")
    public ResponseEntity<HoldStatusResponse> pollingStatus(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        final HoldStatusResponse holdStatusResponse = seatHoldStatusService.getStatus(seatId, userId);
        return ResponseEntity.ok(holdStatusResponse);
    }

    @GetMapping("/{seatId}/queue/status")
    public ResponseEntity<SeatQueueStatusResponse> getStatus(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        final SeatQueueStatusResponse seatQueueStatusResponse = seatQueueStatusService.getStatus(seatId,userId);
        return ResponseEntity.ok(seatQueueStatusResponse);
    }
}
