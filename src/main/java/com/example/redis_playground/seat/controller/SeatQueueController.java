package com.example.redis_playground.seat.controller;

import com.example.redis_playground.seat.dto.SeatHoldStatusResponse;
import com.example.redis_playground.seat.dto.SeatQueueStatusResponse;
import com.example.redis_playground.seat.service.SeatHoldStatusService;
import com.example.redis_playground.seat.service.SeatQueueService;
import com.example.redis_playground.seat.service.SeatQueueStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"queue", "local"})
@RequiredArgsConstructor
@RequestMapping("/seats")
public class SeatQueueController {

    public static final String USER_ID_HEADER = "X-USER-ID";

    private final SeatQueueService seatQueueService;
    private final SeatHoldStatusService seatHoldStatusService;
    private final SeatQueueStatusService seatQueueStatusService;


    @PostMapping("/{seatId}/queue")
    public ResponseEntity<Long> joinQueue(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        final Long order = seatQueueService.joinQueue(seatId, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{seatId}/hold/status")
    public ResponseEntity<SeatHoldStatusResponse> pollingStatus(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        final SeatHoldStatusResponse holdStatusResponse = seatHoldStatusService.getStatus(seatId, userId);
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
