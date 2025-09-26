package com.assessment.riskmanagement.controller;

import com.assessment.riskmanagement.dto.RiskCheckResponse;
import com.assessment.riskmanagement.entity.RiskEvent;
import com.assessment.riskmanagement.service.RiskService;
import com.assessment.riskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/risk")
@Tag(name = "Risk Management", description = "Risk monitoring and management endpoints")
public class RiskController {

    @Autowired
    private RiskService riskService;

    @Autowired
    private UserService userService;

    @PostMapping("/check/{clientId}")
    @Operation(
        summary = "Check user risk",
        description = "Manually trigger risk check for a specific user. Returns current risk status and takes action if thresholds are exceeded."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Risk check completed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Risk Within Limits",
                        value = """
                        {
                            "status": "success",
                            "message": "Risk check completed",
                            "risk_status": "SAFE",
                            "current_balance": 48500.00,
                            "initial_balance": 50000.00,
                            "daily_loss": 1500.00,
                            "daily_loss_percentage": 3.0,
                            "risk_percentage_limit": 2.5,
                            "risk_absolute_limit": 1000.00,
                            "action_taken": "none",
                            "timestamp": "2025-09-18T14:30:00Z"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Risk Threshold Exceeded",
                        value = """
                        {
                            "status": "success",
                            "message": "Risk threshold exceeded - Trading disabled",
                            "risk_status": "EXCEEDED",
                            "current_balance": 47000.00,
                            "initial_balance": 50000.00,
                            "daily_loss": 3000.00,
                            "daily_loss_percentage": 6.0,
                            "risk_percentage_limit": 2.5,
                            "risk_absolute_limit": 1000.00,
                            "action_taken": "trading_disabled_positions_closed",
                            "positions_closed": 3,
                            "timestamp": "2025-09-18T14:30:00Z"
                        }
                        """
                    )
                }
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
                        "message": "User not found"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<RiskCheckResponse> checkUserRisk(
            @Parameter(
                description = "Client ID of the user to check risk for",
                example = "CLIENT_ABC123XYZ"
            )
            @PathVariable String clientId) {
        try {
            var user = userService.getUserEntityByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            RiskCheckResponse result = riskService.checkUserRisk(user);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            RiskCheckResponse errorResponse = new RiskCheckResponse("error", e.getMessage(), "ERROR");
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    @PostMapping("/check-all")
    @Operation(summary = "Check all users risk", description = "Manually trigger risk check for all active users")
    public ResponseEntity<Map<String, Object>> checkAllUsersRisk() {
        try {
            List<RiskCheckResponse> results = riskService.checkAllUsersRisk();

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Risk check completed for all users",
                    "results", results,
                    "users_checked", results.size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/reset-trading/{clientId}")
    @Operation(summary = "Reset user trading status", description = "Re-enable trading for a specific user (for testing purposes)")
    public ResponseEntity<Map<String, Object>> resetUserTradingStatus(
            @Parameter(description = "Client ID of the user", example = "CLIENT_ABC123XYZ")
            @PathVariable String clientId) {
        try {
            boolean success = riskService.resetUserTradingStatus(clientId);

            if (success) {
                Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Trading status reset for user " + clientId,
                        "client_id", clientId
                );
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = Map.of(
                        "status", "error",
                        "message", "User not found or failed to reset trading status"
                );
                return ResponseEntity.status(404).body(errorResponse);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/reset-trading")
    @Operation(summary = "Reset daily trading", description = "Re-enable trading for users whose daily restriction has expired")
    public ResponseEntity<Map<String, Object>> resetDailyTrading() {
        try {
            int resetCount = riskService.resetDailyTrading();
            
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Daily trading reset completed",
                    "users_reset", resetCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/events")
    @Operation(summary = "Get all risk events", description = "Retrieve all risk events across all users")
    public ResponseEntity<Map<String, Object>> getAllRiskEvents() {
        try {
            List<RiskEvent> events = riskService.getAllRiskEvents();
            
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "events", events,
                    "count", events.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/events/{clientId}")
    @Operation(summary = "Get user risk events", description = "Retrieve risk events for a specific user")
    public ResponseEntity<Map<String, Object>> getUserRiskEvents(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            List<RiskEvent> events = riskService.getUserRiskEvents(clientId);
            
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "events", events,
                    "count", events.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}
