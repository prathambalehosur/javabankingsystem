package com.bankingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_number", unique = true, nullable = false)
    private String loanNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;
    
    @NotNull
    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;
    
    @NotNull
    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;
    
    @NotNull
    @Column(name = "term_months", nullable = false)
    private Integer termMonths;
    
    @Column(name = "emi_amount")
    private BigDecimal emiAmount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status = LoanStatus.PENDING;
    
    @Column(name = "credit_score")
    private Integer creditScore;
    
    @Column(name = "collateral_details")
    private String collateralDetails;
    
    @Column(name = "purpose")
    private String purpose;
    
    public enum LoanType {
        PERSONAL,
        HOME,
        CAR,
        EDUCATION,
        BUSINESS
    }
    
    public enum LoanStatus {
        PENDING,
        APPROVED,
        ACTIVE,
        REJECTED,
        CLOSED,
        DEFAULTED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
