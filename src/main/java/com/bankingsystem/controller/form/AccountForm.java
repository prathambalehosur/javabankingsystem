package com.bankingsystem.controller.form;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AccountForm {
    
    @NotBlank(message = "Account name is required")
    private String name;
    
    @NotNull(message = "Account type is required")
    private String type;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial deposit must be greater than 0")
    private BigDecimal initialDeposit;
    
    private String term;
    private boolean jointAccount;
    private String secondaryOwner;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getTerm() {
        return term;
    }


    public void setTerm(String term) {
        this.term = term;
    }


    public boolean isJointAccount() {
        return jointAccount;
    }

    public void setJointAccount(boolean jointAccount) {
        this.jointAccount = jointAccount;
    }

    public String getSecondaryOwner() {
        return secondaryOwner;
    }

    public void setSecondaryOwner(String secondaryOwner) {
        this.secondaryOwner = secondaryOwner;
    }
}
