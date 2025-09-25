package com.assessment.riskmanagement.controller;

import com.assessment.riskmanagement.dto.TradingSignalRequest;
import com.assessment.riskmanagement.entity.Order;
import com.assessment.riskmanagement.entity.User;
import com.assessment.riskmanagement.repository.OrderRepository;
import com.assessment.riskmanagement.service.OrderService;
import com.assessment.riskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "Trading order processing endpoints")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/webhook")
    @Operation(
        summary = "Process trading signal",
        description = "Receive and process trading signals from TradingView or other sources. Supports inverse and pyramid trading strategies with automatic risk management."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Trading signal processed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Successful Order Execution",
                        value = """
                        {
                            "status": "success",
                            "message": "Order executed successfully",
                            "order_id": "ORD_789456123",
                            "action": "BUY",
                            "symbol": "XBTUSD",
                            "quantity": 1000,
                            "strategy": "inverse",
                            "client_id": "CLIENT_ABC123XYZ",
                            "timestamp": "2025-09-18T14:30:00Z"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Risk Threshold Exceeded",
                        value = """
                        {
                            "status": "rejected",
                            "message": "Order rejected: Daily risk threshold exceeded",
                            "risk_percentage": 3.2,
                            "risk_limit": 2.5,
                            "client_id": "CLIENT_ABC123XYZ",
                            "timestamp": "2025-09-18T14:30:00Z"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Trading Disabled",
                        value = """
                        {
                            "status": "rejected",
                            "message": "Trading is currently disabled for this user",
                            "client_id": "CLIENT_ABC123XYZ",
                            "timestamp": "2025-09-18T14:30:00Z"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid trading signal or validation error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                        "status": "error",
                        "message": "Invalid trading signal: Symbol is required"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "User Not Found",
                    value = """
                    {
                        "status": "error",
                        "message": "User not found with client ID: CLIENT_INVALID"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> processTradingSignal(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Trading signal from TradingView or other sources",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Buy Signal (Inverse Strategy)",
                            value = """
                            {
                                "client_id": "CLIENT_ABC123XYZ",
                                "symbol": "XBTUSD",
                                "action": "BUY",
                                "quantity": 1000,
                                "strategy": "inverse"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Sell Signal (Pyramid Strategy)",
                            value = """
                            {
                                "client_id": "CLIENT_ABC123XYZ",
                                "symbol": "ETHUSD",
                                "action": "SELL",
                                "quantity": 5000,
                                "strategy": "pyramid"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Close Position Signal",
                            value = """
                            {
                                "client_id": "CLIENT_ABC123XYZ",
                                "symbol": "XBTUSD",
                                "action": "CLOSE",
                                "quantity": 1
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody TradingSignalRequest signal) {
        try {
            Map<String, Object> result = orderService.processTradingSignal(signal);

            HttpStatus status = "success".equals(result.get("status")) ?
                    HttpStatus.OK : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/user/{clientId}")
    @Operation(summary = "Get user orders", description = "Retrieve all orders for a specific user")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            List<Order> orders = orderService.getUserOrders(clientId);
            
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "orders", orders,
                    "count", orders.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "order", order
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/close-all/{clientId}")
    @Operation(
        summary = "Close all orders for user",
        description = "Cancel all open orders for a specific user. This is typically used for risk management when thresholds are exceeded."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders closed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Orders Closed",
                    value = """
                    {
                        "status": "success",
                        "message": "All orders closed successfully",
                        "closed_orders": ["order-id-1", "order-id-2"],
                        "count": 2,
                        "client_id": "CLIENT_ABC123XYZ"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "User Not Found",
                    value = """
                    {
                        "status": "error",
                        "message": "User not found with client ID: CLIENT_INVALID"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> closeAllUserOrders(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            // Get user first to validate existence
            User user = userService.getUserEntityByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("User not found with client ID: " + clientId));

            List<String> closedOrders = orderService.closeAllOrdersForUser(user);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", closedOrders.isEmpty() ? "No open orders to close" : "All orders closed successfully",
                    "closed_orders", closedOrders,
                    "count", closedOrders.size(),
                    "client_id", clientId
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @GetMapping("/open/{clientId}")
    @Operation(
        summary = "Get open orders for user",
        description = "Retrieve all currently open orders for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Open orders retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Open Orders",
                    value = """
                    {
                        "status": "success",
                        "open_orders": [
                            {
                                "id": "order-id-1",
                                "symbol": "XBTUSD",
                                "side": "BUY",
                                "quantity": 1000,
                                "strategy": "inverse",
                                "status": "OPEN"
                            }
                        ],
                        "count": 1,
                        "client_id": "CLIENT_ABC123XYZ"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getOpenOrders(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            User user = userService.getUserEntityByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("User not found with client ID: " + clientId));

            List<Order> openOrders = orderRepository.findOpenOrdersByUser(user);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "open_orders", openOrders,
                    "count", openOrders.size(),
                    "client_id", clientId
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @GetMapping("/stats/{clientId}")
    @Operation(
        summary = "Get order statistics for user",
        description = "Get comprehensive order statistics for a user including counts by status, total volume, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Order Statistics",
                    value = """
                    {
                        "status": "success",
                        "client_id": "CLIENT_ABC123XYZ",
                        "statistics": {
                            "total_orders": 25,
                            "open_orders": 3,
                            "closed_orders": 20,
                            "cancelled_orders": 2,
                            "failed_orders": 0,
                            "total_volume": 50000.00,
                            "symbols_traded": ["XBTUSD", "ETHUSD"],
                            "strategies_used": ["inverse", "pyramid"]
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            User user = userService.getUserEntityByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("User not found with client ID: " + clientId));

            List<Order> allOrders = orderService.getUserOrders(clientId);

            // Calculate statistics
            long totalOrders = allOrders.size();
            long openOrders = allOrders.stream().filter(o -> o.getStatus().name().equals("OPEN")).count();
            long closedOrders = allOrders.stream().filter(o -> o.getStatus().name().equals("CLOSED")).count();
            long cancelledOrders = allOrders.stream().filter(o -> o.getStatus().name().equals("CANCELLED")).count();
            long failedOrders = allOrders.stream().filter(o -> o.getStatus().name().equals("FAILED")).count();

            double totalVolume = allOrders.stream()
                    .mapToDouble(o -> o.getQuantity().doubleValue())
                    .sum();

            List<String> symbolsTraded = allOrders.stream()
                    .map(Order::getSymbol)
                    .distinct()
                    .sorted()
                    .toList();

            List<String> strategiesUsed = allOrders.stream()
                    .map(Order::getStrategy)
                    .distinct()
                    .sorted()
                    .toList();

            Map<String, Object> statistics = Map.of(
                    "total_orders", totalOrders,
                    "open_orders", openOrders,
                    "closed_orders", closedOrders,
                    "cancelled_orders", cancelledOrders,
                    "failed_orders", failedOrders,
                    "total_volume", totalVolume,
                    "symbols_traded", symbolsTraded,
                    "strategies_used", strategiesUsed
            );

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "client_id", clientId,
                    "statistics", statistics
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}
