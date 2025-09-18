package com.assessment.riskmanagement.repository;

import com.assessment.riskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByClientId(String clientId);
    
    boolean existsByClientId(String clientId);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.tradingEnabled = false")
    List<User> findUsersWithTradingDisabled();
}
