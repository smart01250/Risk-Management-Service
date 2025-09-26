package com.assessment.riskmanagement.service;

import com.assessment.riskmanagement.client.KrakenClient;
import com.assessment.riskmanagement.dto.TradingSignalRequest;
import com.assessment.riskmanagement.dto.kraken.KrakenOrderRequest;
import com.assessment.riskmanagement.dto.kraken.KrakenOrderResponse;
import com.assessment.riskmanagement.entity.Order;
import com.assessment.riskmanagement.entity.OrderSide;
import com.assessment.riskmanagement.entity.OrderStatus;
import com.assessment.riskmanagement.entity.User;
import com.assessment.riskmanagement.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private KrakenClient krakenClient;

    @Value("${risk-management.kraken.demo-mode:false}")
    private boolean demoMode;

    public Map<String, Object> processTradingSignal(TradingSignalRequest signal) {
        try {
            // Get user
            User user = userService.getUserEntityByClientId(signal.getClientId())
                    .orElseThrow(() -> new RuntimeException("User with client_id " + signal.getClientId() + " not found"));

            if (!user.getTradingEnabled()) {
                throw new RuntimeException("Trading is disabled for user " + signal.getClientId());
            }

            // Get existing orders for this strategy and symbol
            List<Order> existingOrders = orderRepository.findByUserAndStrategyAndSymbolAndStatus(
                    user, signal.getStrategy(), signal.getSymbol(), OrderStatus.OPEN
            );

            // Determine order side
            OrderSide orderSide = "buy".equalsIgnoreCase(signal.getAction()) ? OrderSide.BUY : OrderSide.SELL;

            // Check pyramid logic
            if (!signal.getPyramid() && !existingOrders.isEmpty()) {
                // Check if there are same-side orders
                List<Order> sameSideOrders = existingOrders.stream()
                        .filter(o -> o.getSide() == orderSide)
                        .toList();
                
                if (!sameSideOrders.isEmpty()) {
                    logger.warn("Pyramid disabled: rejecting {} order for strategy {}", orderSide, signal.getStrategy());
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "rejected");
                    result.put("reason", "Pyramid disabled - same side order already exists");
                    result.put("existing_orders", sameSideOrders.size());
                    return result;
                }
            }

            // Handle inverse logic
            if (signal.getInverse() && !existingOrders.isEmpty()) {
                // Close existing positions first
                for (Order order : existingOrders) {
                    try {
                        if (order.getKrakenOrderId() != null) {
                            krakenClient.cancelOrder(user.getKrakenApiKey(), user.getKrakenPrivateKey(), order.getKrakenOrderId());
                        }
                        order.setStatus(OrderStatus.CLOSED);
                        orderRepository.save(order);
                        logger.info("Closed order {} for inverse logic", order.getId());
                    } catch (Exception e) {
                        logger.error("Error closing order {}: {}", order.getId(), e.getMessage());
                    }
                }
            }

            // Create new order
            Order newOrder = new Order(user, signal.getSymbol(), signal.getStrategy(), orderSide, signal.getOrderQty());
            newOrder.setStopLossPercentage(signal.getStopLossPercent());
            newOrder.setMaxRiskPerDayPercentage(signal.getMaxRiskPerDayPercent());
            newOrder.setInverse(signal.getInverse());
            newOrder.setPyramid(signal.getPyramid());
            newOrder.setStatus(OrderStatus.PENDING);

            newOrder = orderRepository.save(newOrder);

            // Place order with Kraken (or simulate in demo mode)
            try {
                if (demoMode) {
                    // Demo mode - simulate successful order placement
                    logger.info("DEMO MODE: Simulating order placement for {}", signal);
                    newOrder.setStatus(OrderStatus.OPEN);
                    newOrder.setKrakenOrderId("DEMO_ORDER_" + System.currentTimeMillis());
                    newOrder.setExecutedAt(LocalDateTime.now());
                    logger.info("DEMO MODE: Order simulated successfully with ID: {}", newOrder.getKrakenOrderId());
                } else {
                    // Production mode - place real order with Kraken
                    KrakenOrderRequest krakenRequest = new KrakenOrderRequest(
                            signal.getSymbol(),
                            signal.getAction().toLowerCase(),
                            signal.getOrderQty()
                    );

                    if (signal.getStopLossPercent() != null) {
                        krakenRequest.setStopPrice(signal.getStopLossPercent());
                    }

                    KrakenOrderResponse krakenResponse = krakenClient.placeOrder(
                            user.getKrakenApiKey(),
                            user.getKrakenPrivateKey(),
                            krakenRequest
                    );

                    // Update order with Kraken response
                    if ("success".equals(krakenResponse.getResult())) {
                        newOrder.setStatus(OrderStatus.OPEN);
                        if (krakenResponse.getSendStatus() != null) {
                            newOrder.setKrakenOrderId(krakenResponse.getSendStatus().getOrderId());
                        }
                        newOrder.setExecutedAt(LocalDateTime.now());
                    } else {
                        newOrder.setStatus(OrderStatus.FAILED);
                        newOrder.setErrorMessage(krakenResponse.getError());
                    }
                }

            } catch (Exception e) {
                logger.error("Error placing order with Kraken: {}", e.getMessage());
                newOrder.setStatus(OrderStatus.FAILED);
                newOrder.setErrorMessage(demoMode ? "Demo mode error: " + e.getMessage() : e.getMessage());
            }

            newOrder = orderRepository.save(newOrder);

            logger.info("Order processed: {} - Status: {}", newOrder.getId(), newOrder.getStatus());

            Map<String, Object> result = new HashMap<>();
            result.put("status", newOrder.getStatus() == OrderStatus.OPEN ? "success" : "failed");
            result.put("order_id", newOrder.getId().toString());
            result.put("kraken_order_id", newOrder.getKrakenOrderId());
            result.put("message", newOrder.getStatus() == OrderStatus.FAILED ? 
                    newOrder.getErrorMessage() : "Order placed successfully");

            return result;

        } catch (Exception e) {
            logger.error("Error processing trading signal: {}", e.getMessage());
            throw new RuntimeException("Error processing trading signal: " + e.getMessage());
        }
    }

    public List<String> closeAllOrdersForUser(User user) {
        List<String> closedOrders = new java.util.ArrayList<>();

        try {
            // Get all open orders for user
            List<Order> openOrders = orderRepository.findOpenOrdersByUser(user);

            if (openOrders.isEmpty()) {
                return closedOrders;
            }

            // Cancel all orders
            for (Order order : openOrders) {
                try {
                    if (order.getKrakenOrderId() != null) {
                        krakenClient.cancelOrder(user.getKrakenApiKey(), user.getKrakenPrivateKey(), order.getKrakenOrderId());
                    }

                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    closedOrders.add(order.getId().toString());
                    logger.info("Cancelled order {} for user {}", order.getId(), user.getClientId());

                } catch (Exception e) {
                    logger.error("Error cancelling order {}: {}", order.getId(), e.getMessage());
                    order.setErrorMessage(e.getMessage());
                    orderRepository.save(order);
                }
            }

        } catch (Exception e) {
            logger.error("Error closing orders for user {}: {}", user.getClientId(), e.getMessage());
            throw new RuntimeException("Error closing orders for user", e);
        }

        return closedOrders;
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(String clientId) {
        User user = userService.getUserEntityByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(String orderId) {
        return orderRepository.findById(java.util.UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
