package com.example.redis_playground.seat.controller;

import com.example.redis_playground.seat.service.SeatReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"reserve", "local"})
@RequiredArgsConstructor
@RequestMapping("/seats")
public class SeatReserveController {

    public static final String USER_ID_HEADER = "X-USER-ID";
    private final SeatReserveService seatReserveService;

    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<Void> reserve(
            @PathVariable Long seatId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        seatReserveService.reserve(seatId, userId);
        return ResponseEntity.ok().build();
    }
}
