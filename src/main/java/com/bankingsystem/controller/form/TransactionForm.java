package com.bankingsystem.controller.form;

import com.bankingsystem.model.Transaction.TransactionType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionForm {
    private TransactionType type;
    
    @NotBlank(message = "Source account is required")
    private String fromAccountNumber;
    
    @NotBlank(message = "Destination account is required")
    private String toAccountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String description;
}
