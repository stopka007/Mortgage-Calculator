package com.example.hypocalculator.service;

import com.example.hypocalculator.model.MortgageRequest;
import com.example.hypocalculator.model.MortgageResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class MortgageService {

    public MortgageResponse calculateMortgage(MortgageRequest request) {
        BigDecimal principal = request.getLoanAmount().subtract(request.getDownPayment());
        int interval = request.getIntervalPayment(); // 1=monthly, 6=semi-annual, 12=annual

        int paymentsPerYear = 12 / interval;
        int totalPayments = request.getLoanTermYears() * paymentsPerYear;

        // Interest rate per period
        BigDecimal periodInterestRate = request.getAnnualInterestRate()
            .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(paymentsPerYear), 8, RoundingMode.HALF_UP);

        // Calculate payment per period
        BigDecimal paymentPerPeriod = calculatePaymentPerPeriod(principal, periodInterestRate, totalPayments);

        // Calculate payment schedule
        List<MortgageResponse.PaymentSchedule> schedule = calculatePaymentSchedule(
            principal, periodInterestRate, totalPayments, paymentPerPeriod);

        // Calculate totals
        BigDecimal totalPayment = paymentPerPeriod.multiply(BigDecimal.valueOf(totalPayments));
        BigDecimal totalInterest = totalPayment.subtract(principal);
        BigDecimal totalCost = totalPayment.add(request.getDownPayment());

        // RPSN calculation
        BigDecimal rpsn = totalCost
            .divide(principal, 8, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(request.getLoanTermYears()), 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        MortgageResponse response = new MortgageResponse();
        response.setMonthlyPayment(paymentPerPeriod.setScale(2, RoundingMode.HALF_UP));
        response.setTotalPayment(totalPayment.setScale(2, RoundingMode.HALF_UP));
        response.setTotalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP));
        response.setPrincipalAmount(principal.setScale(2, RoundingMode.HALF_UP));
        response.setDownPayment(request.getDownPayment().setScale(2, RoundingMode.HALF_UP));
        response.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP));
        response.setRpsn(rpsn.setScale(2, RoundingMode.HALF_UP));
        response.setPaymentSchedule(schedule);
        response.setIntervalPayment(interval);

        return response;
    }

    private BigDecimal calculatePaymentPerPeriod(BigDecimal principal, BigDecimal periodInterestRate, int totalPayments) {
        if (periodInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRate = BigDecimal.ONE.add(periodInterestRate);
        BigDecimal rateToPower = onePlusRate.pow(totalPayments);
        BigDecimal numerator = principal.multiply(periodInterestRate).multiply(rateToPower);
        BigDecimal denominator = rateToPower.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private List<MortgageResponse.PaymentSchedule> calculatePaymentSchedule(
            BigDecimal principal, BigDecimal periodInterestRate, int totalPayments, BigDecimal paymentPerPeriod) {
        
        List<MortgageResponse.PaymentSchedule> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;

        for (int paymentNumber = 1; paymentNumber <= totalPayments; paymentNumber++) {
            BigDecimal interest = remainingBalance.multiply(periodInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = paymentPerPeriod.subtract(interest);
            
            // For the last payment, adjust to ensure remaining balance becomes zero
            if (paymentNumber == totalPayments) {
                principalPayment = remainingBalance;
            }
            
            remainingBalance = remainingBalance.subtract(principalPayment);

            MortgageResponse.PaymentSchedule payment = new MortgageResponse.PaymentSchedule();
            payment.setPaymentNumber(paymentNumber);
            payment.setPayment(paymentPerPeriod.setScale(2, RoundingMode.HALF_UP));
            payment.setPrincipal(principalPayment.setScale(2, RoundingMode.HALF_UP));
            payment.setInterest(interest.setScale(2, RoundingMode.HALF_UP));
            payment.setRemainingBalance(remainingBalance.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            
            schedule.add(payment);
        }

        return schedule;
    }
} 