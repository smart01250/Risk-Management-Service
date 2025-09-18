package com.assessment.riskmanagement.repository;

import com.assessment.riskmanagement.entity.RiskEvent;
import com.assessment.riskmanagement.entity.RiskEventType;
import com.assessment.riskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskEventRepository extends JpaRepository<RiskEvent, UUID> {
    
    List<RiskEvent> findByUserOrderByCreatedAtDesc(User user);
    
    List<RiskEvent> findByEventTypeOrderByCreatedAtDesc(RiskEventType eventType);
    
    @Query("SELECT re FROM RiskEvent re ORDER BY re.createdAt DESC")
    List<RiskEvent> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT re FROM RiskEvent re WHERE re.user = :user AND re.tradingDisabledUntil IS NOT NULL AND re.tradingDisabledUntil > :currentTime ORDER BY re.createdAt DESC")
    Optional<RiskEvent> findLatestTradingDisabledEvent(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
}
