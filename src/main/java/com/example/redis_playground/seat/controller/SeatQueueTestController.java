package com.example.redis_playground.seat.controller;

import com.example.redis_playground.seat.processor.SeatQueueProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class SeatQueueTestController {

    private final SeatQueueProcessor processor;

    @PostMapping("/process")
    public List<String> process(
            @RequestParam(defaultValue = "5") int batch
    ) {
        return processor.processBatch(1L, batch);
    }
}
