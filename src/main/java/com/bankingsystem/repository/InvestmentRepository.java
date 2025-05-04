package com.bankingsystem.repository;

import com.bankingsystem.model.Investment;
import com.bankingsystem.model.Investment.InvestmentStatus;
import com.bankingsystem.model.Investment.InvestmentType;
import com.bankingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    
    Optional<Investment> findByInvestmentNumber(String investmentNumber);
    
    List<Investment> findByUser(User user);
    
    List<Investment> findByUserAndStatus(User user, InvestmentStatus status);
    
    List<Investment> findByInvestmentType(InvestmentType investmentType);
    
    List<Investment> findByStatus(InvestmentStatus status);
    
    List<Investment> findByMaturityDateBefore(LocalDate date);
    
    @Query("SELECT i FROM Investment i WHERE i.maturityDate <= ?1 AND i.status = 'ACTIVE'")
    List<Investment> findInvestmentsWithUpcomingMaturity(LocalDate date);
    
    @Query("SELECT SUM(i.currentValue) FROM Investment i WHERE i.user = ?1 AND i.status = 'ACTIVE'")
    Double getTotalInvestmentValue(User user);
    
    @Query("SELECT i.investmentType, SUM(i.currentValue) FROM Investment i WHERE i.user = ?1 AND i.status = 'ACTIVE' GROUP BY i.investmentType")
    List<Object[]> getInvestmentBreakdown(User user);
}
