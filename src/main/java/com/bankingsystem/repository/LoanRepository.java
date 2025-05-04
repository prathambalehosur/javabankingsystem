package com.bankingsystem.repository;

import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Loan.LoanStatus;
import com.bankingsystem.model.Loan.LoanType;
import com.bankingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    Optional<Loan> findByLoanNumber(String loanNumber);
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    
    List<Loan> findByLoanType(LoanType loanType);
    
    List<Loan> findByStatus(LoanStatus status);
    
    List<Loan> findByNextPaymentDateBefore(LocalDate date);
    
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate <= ?1 AND l.status = 'ACTIVE'")
    List<Loan> findLoansWithUpcomingPayments(LocalDate date);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = ?1 AND l.status IN ('ACTIVE', 'APPROVED')")
    Long countActiveLoans(User user);
}
