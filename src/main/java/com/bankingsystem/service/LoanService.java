package com.bankingsystem.service;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Loan.LoanStatus;
import com.bankingsystem.model.Loan.LoanType;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.Transaction.TransactionStatus;
import com.bankingsystem.model.Transaction.TransactionType;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.LoanRepository;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final NotificationService notificationService;

    /**
     * Get all loans for a user
     */
    public List<Loan> getUserLoans(User user) {
        return loanRepository.findByUser(user);
    }

    /**
     * Get loan by loan number
     */
    public Loan getLoanByNumber(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    /**
     * Apply for a new loan
     */
    @Transactional
    public Loan applyForLoan(User user, LoanType loanType, BigDecimal amount, Integer termMonths, String purpose) {
        // Validate loan amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loan amount must be positive");
        }

        // Validate term months
        if (termMonths <= 0) {
            throw new RuntimeException("Loan term must be positive");
        }

        // Set interest rate based on loan type
        BigDecimal interestRate;
        switch (loanType) {
            case PERSONAL:
                interestRate = new BigDecimal("12.0");
                break;
            case HOME:
                interestRate = new BigDecimal("8.5");
                break;
            case CAR:
                interestRate = new BigDecimal("9.5");
                break;
            case EDUCATION:
                interestRate = new BigDecimal("7.0");
                break;
            case BUSINESS:
                interestRate = new BigDecimal("11.0");
                break;
            default:
                throw new RuntimeException("Invalid loan type");
        }

        // Generate loan number
        String loanNumber = generateLoanNumber();

        // Calculate EMI
        BigDecimal emi = calculateEMI(amount, interestRate, termMonths);

        // Generate credit score (simulation)
        int creditScore = generateCreditScore(user);

        // Create loan
        Loan loan = new Loan();
        loan.setLoanNumber(loanNumber);
        loan.setLoanType(loanType);
        loan.setPrincipalAmount(amount);
        loan.setInterestRate(interestRate);
        loan.setTermMonths(termMonths);
        loan.setEmiAmount(emi);
        loan.setUser(user);
        loan.setStartDate(null); // Will be set when approved
        loan.setEndDate(null); // Will be set when approved
        loan.setNextPaymentDate(null); // Will be set when approved
        loan.setStatus(LoanStatus.PENDING);
        loan.setCreditScore(creditScore);
        loan.setPurpose(purpose);

        Loan savedLoan = loanRepository.save(loan);

        // Send notification
        notificationService.sendTransactionNotification(
                user,
                null,
                "Loan application submitted for " + loanType + " loan of $" + amount
        );

        return savedLoan;
    }

    /**
     * Calculate EMI for a loan
     */
    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualInterestRate, Integer termMonths) {
        // Convert annual interest rate to monthly and decimal form
        BigDecimal monthlyInterestRate = annualInterestRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

        // Calculate EMI using formula: P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal powerTerm = onePlusR.pow(termMonths, MathContext.DECIMAL128);

        BigDecimal numerator = principal.multiply(monthlyInterestRate).multiply(powerTerm);
        BigDecimal denominator = powerTerm.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * Generate amortization schedule
     */
    public List<Object[]> generateAmortizationSchedule(BigDecimal principal, BigDecimal annualInterestRate, Integer termMonths) {
        List<Object[]> schedule = new ArrayList<>();
        BigDecimal monthlyInterestRate = annualInterestRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal emi = calculateEMI(principal, annualInterestRate, termMonths);
        BigDecimal remainingPrincipal = principal;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = emi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            
            // Handle last payment rounding issues
            if (month == termMonths) {
                principalPaid = remainingPrincipal;
                emi = principalPaid.add(interest);
            }
            
            remainingPrincipal = remainingPrincipal.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            
            // Create a row: [month, emi, principal, interest, balance]
            Object[] row = new Object[]{
                    month,
                    emi,
                    principalPaid,
                    interest,
                    remainingPrincipal
            };
            
            schedule.add(row);
        }

        return schedule;
    }

    /**
     * Calculate eligible loan amount
     */
    public BigDecimal calculateEligibleAmount(User user, BigDecimal monthlyIncome, BigDecimal existingEMI, LoanType loanType) {
        // Calculate debt-to-income ratio
        BigDecimal dti = existingEMI.divide(monthlyIncome, 2, RoundingMode.HALF_UP);
        
        // Maximum allowed DTI is 50%
        BigDecimal maxDti = new BigDecimal("0.5");
        
        if (dti.compareTo(maxDti) >= 0) {
            return BigDecimal.ZERO; // Already at or exceeding max DTI
        }
        
        // Available income for new loan
        BigDecimal availableIncome = monthlyIncome.multiply(maxDti).subtract(existingEMI);
        
        // Set interest rate based on loan type
        BigDecimal interestRate;
        Integer maxTermMonths;
        
        switch (loanType) {
            case PERSONAL:
                interestRate = new BigDecimal("12.0");
                maxTermMonths = 60; // 5 years
                break;
            case HOME:
                interestRate = new BigDecimal("8.5");
                maxTermMonths = 360; // 30 years
                break;
            case CAR:
                interestRate = new BigDecimal("9.5");
                maxTermMonths = 84; // 7 years
                break;
            case EDUCATION:
                interestRate = new BigDecimal("7.0");
                maxTermMonths = 120; // 10 years
                break;
            case BUSINESS:
                interestRate = new BigDecimal("11.0");
                maxTermMonths = 120; // 10 years
                break;
            default:
                throw new RuntimeException("Invalid loan type");
        }
        
        // Convert annual interest rate to monthly and decimal form
        BigDecimal monthlyInterestRate = interestRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        
        // Calculate eligible principal using EMI formula rearranged:
        // P = EMI * ((1+r)^n - 1) / (r * (1+r)^n)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal powerTerm = onePlusR.pow(maxTermMonths, MathContext.DECIMAL128);
        
        BigDecimal numerator = availableIncome.multiply(powerTerm.subtract(BigDecimal.ONE));
        BigDecimal denominator = monthlyInterestRate.multiply(powerTerm);
        
        BigDecimal eligibleAmount = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        
        // Apply credit score factor (simulation)
        int creditScore = generateCreditScore(user);
        BigDecimal creditFactor;
        
        if (creditScore >= 750) {
            creditFactor = new BigDecimal("1.0");
        } else if (creditScore >= 700) {
            creditFactor = new BigDecimal("0.9");
        } else if (creditScore >= 650) {
            creditFactor = new BigDecimal("0.8");
        } else if (creditScore >= 600) {
            creditFactor = new BigDecimal("0.7");
        } else {
            creditFactor = new BigDecimal("0.5");
        }
        
        return eligibleAmount.multiply(creditFactor).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get repayment schedule for a loan
     */
    public List<Object[]> getRepaymentSchedule(Loan loan) {
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Repayment schedule is only available for active loans");
        }
        
        List<Object[]> schedule = new ArrayList<>();
        BigDecimal monthlyInterestRate = loan.getInterestRate().divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal emi = loan.getEmiAmount();
        BigDecimal remainingPrincipal = loan.getPrincipalAmount();
        LocalDate paymentDate = loan.getStartDate();
        
        for (int month = 1; month <= loan.getTermMonths(); month++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = emi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            
            // Handle last payment rounding issues
            if (month == loan.getTermMonths()) {
                principalPaid = remainingPrincipal;
                emi = principalPaid.add(interest);
            }
            
            remainingPrincipal = remainingPrincipal.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            
            // Create a row: [month, date, emi, principal, interest, balance, paid]
            boolean paid = paymentDate.isBefore(LocalDate.now());
            Object[] row = new Object[]{
                    month,
                    paymentDate,
                    emi,
                    principalPaid,
                    interest,
                    remainingPrincipal,
                    paid
            };
            
            schedule.add(row);
            paymentDate = paymentDate.plusMonths(1);
        }
        
        return schedule;
    }

    /**
     * Pay EMI for a loan
     */
    @Transactional
    public void payEMI(String loanNumber, String accountNumber) {
        Loan loan = getLoanByNumber(loanNumber);
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Cannot pay EMI for a non-active loan");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Check if account belongs to the loan user
        if (!account.getUser().equals(loan.getUser())) {
            throw new RuntimeException("Account does not belong to the loan holder");
        }
        
        // Check if account has sufficient balance
        if (account.getBalance().compareTo(loan.getEmiAmount()) < 0) {
            throw new RuntimeException("Insufficient balance to pay EMI");
        }
        
        // Withdraw from account
        accountService.withdraw(accountNumber, loan.getEmiAmount());
        
        // Update loan next payment date
        loan.setNextPaymentDate(loan.getNextPaymentDate().plusMonths(1));
        loanRepository.save(loan);
        
        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setAmount(loan.getEmiAmount());
        transaction.setSourceAccount(account);
        transaction.setDescription("EMI payment for loan " + loanNumber);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReferenceNumber());
        
        transactionRepository.save(transaction);
        
        // Send notification
        notificationService.sendTransactionNotification(
                loan.getUser(),
                transaction,
                "EMI payment of $" + loan.getEmiAmount() + " for loan " + loanNumber
        );
    }

    /**
     * Approve a loan application (admin function)
     */
    @Transactional
    public void approveLoan(String loanNumber) {
        Loan loan = getLoanByNumber(loanNumber);
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Can only approve pending loans");
        }
        
        // Set loan dates
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(loan.getTermMonths());
        LocalDate nextPaymentDate = startDate.plusMonths(1);
        
        loan.setStartDate(startDate);
        loan.setEndDate(endDate);
        loan.setNextPaymentDate(nextPaymentDate);
        loan.setStatus(LoanStatus.APPROVED);
        
        loanRepository.save(loan);
        
        // Send notification
        notificationService.sendTransactionNotification(
                loan.getUser(),
                null,
                "Your loan application for $" + loan.getPrincipalAmount() + " has been approved"
        );
    }

    /**
     * Disburse an approved loan to an account (admin function)
     */
    @Transactional
    public void disburseLoan(String loanNumber, String accountNumber) {
        Loan loan = getLoanByNumber(loanNumber);
        
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Can only disburse approved loans");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Check if account belongs to the loan user
        if (!account.getUser().equals(loan.getUser())) {
            throw new RuntimeException("Account does not belong to the loan holder");
        }
        
        // Deposit to account
        accountService.deposit(accountNumber, loan.getPrincipalAmount());
        
        // Update loan status
        loan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loan);
        
        // Send notification
        notificationService.sendTransactionNotification(
                loan.getUser(),
                null,
                "Your loan of $" + loan.getPrincipalAmount() + " has been disbursed to account " + accountNumber
        );
    }

    /**
     * Reject a loan application (admin function)
     */
    @Transactional
    public void rejectLoan(String loanNumber, String reason) {
        Loan loan = getLoanByNumber(loanNumber);
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Can only reject pending loans");
        }
        
        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);
        
        // Send notification
        notificationService.sendTransactionNotification(
                loan.getUser(),
                null,
                "Your loan application for $" + loan.getPrincipalAmount() + " has been rejected. Reason: " + reason
        );
    }

    /**
     * Generate a credit score (simulation)
     */
    private int generateCreditScore(User user) {
        // In a real application, this would use actual credit data
        // For simulation, we'll generate a random score between 500 and 850
        Random random = new Random(user.getId()); // Use user ID as seed for consistency
        return 500 + random.nextInt(351);
    }

    /**
     * Generate a unique loan number
     */
    private String generateLoanNumber() {
        // In a real application, this would follow a specific format and validation
        return "LN" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * Generate a unique transaction number
     */
    private String generateTransactionNumber() {
        // Use UUID for uniqueness
        return "TXN" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Generate a reference number for a transaction
     */
    private String generateReferenceNumber() {
        // Use UUID for uniqueness
        return "REF" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
    }
}
