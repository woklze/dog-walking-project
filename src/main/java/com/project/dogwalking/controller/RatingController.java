package com.project.dogwalking.controller;

import com.project.dogwalking.dto.RatingCreateDto;
import com.project.dogwalking.dto.RatingResponseDto;
import com.project.dogwalking.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponseDto> createRating(@Valid @RequestBody RatingCreateDto createDto) {
        RatingResponseDto rating = ratingService.createRating(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }
}