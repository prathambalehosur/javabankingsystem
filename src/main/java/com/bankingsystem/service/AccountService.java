package com.bankingsystem.service;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Account.AccountType;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.Transaction.TransactionStatus;
import com.bankingsystem.model.Transaction.TransactionType;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Get all accounts for a user
     */
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }
    
    /**
     * Get all active accounts for a user
     */
    public List<Account> getUserActiveAccounts(User user) {
        return accountRepository.findByUserAndActive(user, true);
    }
    
    /**
     * Get account by account number
     */
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
    
    /**
     * Create a new account for a user
     */
    @Transactional
    public Account createAccount(User user, AccountType accountType, BigDecimal initialDeposit) {
        return createAccount(user, accountType, initialDeposit, null, false, null);
    }
    
    @Transactional
    public Account createAccount(User user, AccountType accountType, BigDecimal initialDeposit, 
                               String accountName, boolean isJointAccount, User secondaryUser) {
        // Generate account number
        String accountNumber = generateAccountNumber();
        
        // Set default values based on account type
        BigDecimal minimumBalance = BigDecimal.ZERO;
        BigDecimal interestRate = BigDecimal.ZERO;
        
        if (accountType == AccountType.SAVINGS) {
            minimumBalance = new BigDecimal("100.00");
            interestRate = new BigDecimal("0.025"); // 2.5% annual interest
        } else if (accountType == AccountType.CHECKING) {
            minimumBalance = new BigDecimal("50.00");
        }
        
        // Create new account
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType);
        account.setBalance(initialDeposit);
        account.setUser(user);
        account.setActive(true);
        account.setJointAccount(isJointAccount);
        
        // Set account name if provided
        if (accountName != null && !accountName.trim().isEmpty()) {
            account.setName(accountName.trim());
        }
        
        // Add secondary user as joint holder if provided
        if (isJointAccount && secondaryUser != null) {
            account.getJointHolders().add(secondaryUser);
        }
        
        Account savedAccount = accountRepository.save(account);
        
        // Record initial deposit transaction if needed
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionNumber(generateTransactionNumber());
            transaction.setTransactionType(TransactionType.DEPOSIT);
            transaction.setAmount(initialDeposit);
            transaction.setDescription("Initial deposit");
            transaction.setSourceAccount(null);
            transaction.setDestinationAccount(savedAccount);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus(TransactionStatus.COMPLETED);
            
            transactionRepository.save(transaction);
        }
        
        return savedAccount;
    }
    
    /**
     * Add a joint account holder to an existing account
     * @param accountNumber The account number to add the joint holder to
     * @param secondaryUser The user to add as a joint account holder
     */
    @Transactional
    public void addJointAccountHolder(String accountNumber, User secondaryUser) {
        Account account = getAccountByNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        
        // Check if the secondary user is already a joint holder
        if (account.getJointHolders().contains(secondaryUser)) {
            throw new RuntimeException("User is already a joint holder of this account");
        }
        
        // Add the secondary user as a joint holder
        account.getJointHolders().add(secondaryUser);
        accountRepository.save(account);
    }
    
    /**
     * Create a joint account for multiple users
     */
    @Transactional
    public Account createJointAccount(List<User> users, AccountType accountType, BigDecimal initialDeposit) {
        // Implementation for joint account
        // In a real application, this would involve a more complex model with a many-to-many relationship
        // For simplicity, we'll just create a regular account with the first user as the primary owner
        
        if (users.isEmpty()) {
            throw new RuntimeException("At least one user is required for a joint account");
        }
        
        User primaryUser = users.get(0);
        Account account = createAccount(primaryUser, accountType, initialDeposit);
        
        // In a real application, you would associate the other users with this account
        
        return account;
    }
    
    /**
     * Deposit money into an account
     */
    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }
        
        Account account = getAccountByNumber(accountNumber);
        
        if (!account.isActive()) {
            throw new RuntimeException("Cannot deposit to an inactive account");
        }
        
        // Update balance
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        // Record transaction
        Transaction transaction = recordTransaction(
                null,
                account,
                TransactionType.DEPOSIT,
                amount,
                "Deposit to account",
                TransactionStatus.COMPLETED
        );
        
        return transaction;
    }
    
    /**
     * Withdraw money from an account
     */
    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }
        
        Account account = getAccountByNumber(accountNumber);
        
        if (!account.isActive()) {
            throw new RuntimeException("Cannot withdraw from an inactive account");
        }
        
        // Check if withdrawal would go below minimum balance
        if (account.getBalance().subtract(amount).compareTo(account.getMinimumBalance()) < 0) {
            throw new RuntimeException("Withdrawal would go below minimum balance");
        }
        
        // Update balance
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        // Record transaction
        Transaction transaction = recordTransaction(
                account,
                null,
                TransactionType.WITHDRAWAL,
                amount,
                "Withdrawal from account",
                TransactionStatus.COMPLETED
        );
        
        return transaction;
    }
    
    /**
     * Transfer money between accounts
     */
    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
        
        Account fromAccount = getAccountByNumber(fromAccountNumber);
        Account toAccount = getAccountByNumber(toAccountNumber);
        
        if (!fromAccount.isActive() || !toAccount.isActive()) {
            throw new RuntimeException("Cannot transfer with inactive accounts");
        }
        
        // Check if transfer would go below minimum balance
        if (fromAccount.getBalance().subtract(amount).compareTo(fromAccount.getMinimumBalance()) < 0) {
            throw new RuntimeException("Transfer would go below minimum balance");
        }
        
        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Record transaction
        Transaction transaction = recordTransaction(
                fromAccount,
                toAccount,
                TransactionType.TRANSFER,
                amount,
                "Transfer between accounts",
                TransactionStatus.COMPLETED
        );
        
        return transaction;
    }
    
    /**
     * Freeze/unfreeze an account
     */
    @Transactional
    public void setAccountStatus(String accountNumber, boolean active) {
        Account account = getAccountByNumber(accountNumber);
        account.setActive(active);
        accountRepository.save(account);
    }
    
    /**
     * Apply interest to savings accounts (scheduled task)
     */
    @Transactional
    public void applyInterest() {
        List<Account> savingsAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getAccountType() == AccountType.SAVINGS && a.isActive())
                .toList();
        
        for (Account account : savingsAccounts) {
            // Calculate interest (simple interest for demonstration)
            BigDecimal interestRate = account.getInterestRate().divide(new BigDecimal("100"));
            BigDecimal interest = account.getBalance().multiply(interestRate).divide(new BigDecimal("12"));
            
            // Apply interest
            account.setBalance(account.getBalance().add(interest));
            accountRepository.save(account);
            
            // Record transaction
            Transaction transaction = recordTransaction(
                    null,
                    account,
                    TransactionType.INTEREST,
                    interest,
                    "Monthly interest",
                    TransactionStatus.COMPLETED
            );
        }
    }
    
    /**
     * Record a transaction
     */
    private Transaction recordTransaction(
            Account sourceAccount,
            Account destinationAccount,
            TransactionType type,
            BigDecimal amount,
            String description,
            TransactionStatus status
    ) {
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(status);
        transaction.setReferenceNumber(generateReferenceNumber());
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Generate a unique account number
     */
    private String generateAccountNumber() {
        // In a real application, this would follow a specific format and validation
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        // Generate a 10-digit account number
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
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
