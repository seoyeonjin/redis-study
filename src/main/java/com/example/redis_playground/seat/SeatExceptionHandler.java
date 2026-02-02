package com.example.redis_playground.seat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SeatExceptionHandler {

    @ExceptionHandler(SeatHoldException.class)
    public ResponseEntity<String> handle(SeatHoldException e) {
        return switch(e.getMessage()) {
            case "HOLD_NOT_FOUND" -> ResponseEntity.status(HttpStatus.CONFLICT).body("HOLD_NOT_FOUND");
            case "NOT_HOLDER" -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("NOT_HOLDER");
            case "ALREADY_RESERVED" -> ResponseEntity.status(HttpStatus.CONFLICT).body("ALREADY_RESERVED");
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD_REQUEST");
        };
    }
}
