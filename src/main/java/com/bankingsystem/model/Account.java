package com.bankingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    @NotNull
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    
    @Column(name = "minimum_balance")
    private BigDecimal minimumBalance;
    
    @OneToMany(mappedBy = "sourceAccount", fetch = FetchType.LAZY)
    private List<Transaction> sourceTransactions;
    
    @OneToMany(mappedBy = "destinationAccount", fetch = FetchType.LAZY)
    private List<Transaction> destinationTransactions;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "account_joint_holders",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> jointHolders = new HashSet<>();
    
    @Column(name = "is_joint_account")
    private boolean jointAccount = false;
    
    @Column(name = "account_name")
    private String name;
    
    public List<Transaction> getTransactions() {
        List<Transaction> allTransactions = new ArrayList<>();
        if (sourceTransactions != null) {
            allTransactions.addAll(sourceTransactions);
        }
        if (destinationTransactions != null) {
            allTransactions.addAll(destinationTransactions);
        }
        return allTransactions;
    }
    
    public enum AccountType {
        SAVINGS,
        CHECKING,
        FIXED_DEPOSIT,
        LOAN
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
    
    // Helper method to check if a user is a joint holder
    public boolean isJointHolder(User user) {
        return jointHolders.contains(user);
    }
}
