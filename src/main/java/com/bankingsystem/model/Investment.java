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
@Table(name = "investments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "investment_number", unique = true, nullable = false)
    private String investmentNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false)
    private InvestmentType investmentType;
    
    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "current_value")
    private BigDecimal currentValue;
    
    @Column(name = "profit_loss")
    private BigDecimal profitLoss;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvestmentStatus status = InvestmentStatus.ACTIVE;
    
    @Column(name = "instrument_name")
    private String instrumentName;
    
    @Column(name = "instrument_code")
    private String instrumentCode;
    
    @Column(name = "risk_level")
    private String riskLevel;
    
    public enum InvestmentType {
        MUTUAL_FUND,
        STOCK,
        FIXED_DEPOSIT,
        BOND,
        RETIREMENT_PLAN,
        GOLD
    }
    
    public enum InvestmentStatus {
        ACTIVE,
        MATURED,
        REDEEMED,
        CANCELLED
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
