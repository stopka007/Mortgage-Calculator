package com.example.hypocalculator.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MortgageResponse {
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayment;
    private BigDecimal totalInterest;
    private BigDecimal principalAmount;
    private BigDecimal downPayment;
    private Integer intervalPayment;
    private BigDecimal totalCost;
    private BigDecimal rpsn;
    private List<PaymentSchedule> paymentSchedule;
    
    @Data
    public static class PaymentSchedule {
        private Integer paymentNumber;
        private BigDecimal payment;
        private BigDecimal principal;
        private BigDecimal interest;
        private BigDecimal remainingBalance;
    }
} 