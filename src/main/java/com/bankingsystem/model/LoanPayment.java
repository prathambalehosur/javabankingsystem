package com.bankingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoanPayment {
    private int paymentNumber;
    private BigDecimal payment;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal remainingBalance;
}
