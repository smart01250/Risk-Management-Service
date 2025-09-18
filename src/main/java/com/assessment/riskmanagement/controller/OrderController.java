package com.assessment.riskmanagement.controller;

import com.assessment.riskmanagement.dto.TradingSignalRequest;
import com.assessment.riskmanagement.entity.Order;
import com.assessment.riskmanagement.service.OrderService;
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
                            "price": 45000.50,
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
                                "price": 45000.50,
                                "strategy": "inverse",
                                "timestamp": "2025-09-18T14:30:00Z"
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
                                "price": 2800.75,
                                "strategy": "pyramid",
                                "timestamp": "2025-09-18T14:30:00Z"
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
                                "timestamp": "2025-09-18T14:30:00Z"
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
}
