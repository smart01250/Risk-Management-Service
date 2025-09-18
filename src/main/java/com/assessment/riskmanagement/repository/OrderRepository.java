package com.assessment.riskmanagement.repository;

import com.assessment.riskmanagement.entity.Order;
import com.assessment.riskmanagement.entity.OrderStatus;
import com.assessment.riskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.strategy = :strategy AND o.symbol = :symbol AND o.status = :status")
    List<Order> findByUserAndStrategyAndSymbolAndStatus(
        @Param("user") User user, 
        @Param("strategy") String strategy, 
        @Param("symbol") String symbol, 
        @Param("status") OrderStatus status
    );
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = 'OPEN'")
    List<Order> findOpenOrdersByUser(@Param("user") User user);
}
