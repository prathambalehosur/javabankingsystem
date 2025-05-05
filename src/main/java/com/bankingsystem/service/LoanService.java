package com.bankingsystem.service;

import com.bankingsystem.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoanService {

    /**
     * Calculate EMI for a loan
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal interestRate, int tenureInMonths) {
        // Convert annual interest rate to monthly
        BigDecimal monthlyRate = interestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        
        // Calculate EMI using formula: P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powValue = onePlusRate.pow(tenureInMonths);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(powValue);
        BigDecimal denominator = powValue.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total interest payable
     */
    public BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal emi, int tenureInMonths) {
        BigDecimal totalPayment = emi.multiply(new BigDecimal(tenureInMonths));
        return totalPayment.subtract(principal).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Check loan eligibility
     */
    public boolean checkEligibility(User user, BigDecimal loanAmount, BigDecimal income) {
        // Simple eligibility check: loan amount should not exceed 36 times monthly income
        return loanAmount.compareTo(income.multiply(new BigDecimal("36"))) <= 0;
    }
}
