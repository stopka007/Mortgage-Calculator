package com.example.hypocalculator.controllers;

import com.example.hypocalculator.model.MortgageRequest;
import com.example.hypocalculator.model.MortgageResponse;
import com.example.hypocalculator.service.MortgageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mortgage")
@CrossOrigin(origins = "*")
public class MortgageController {

    @Autowired
    private MortgageService mortgageService;

    @PostMapping("/calculate")
    public ResponseEntity<MortgageResponse> calculateMortgage(@Valid @RequestBody MortgageRequest request) {
        try {
            MortgageResponse response = mortgageService.calculateMortgage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mortgage calculator is running!");
    }
} 