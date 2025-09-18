package com.assessment.riskmanagement.controller;

import com.assessment.riskmanagement.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring", description = "System monitoring and health check endpoints")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @GetMapping("/status")
    @Operation(
        summary = "Get monitoring status",
        description = "Get the current status of the risk monitoring service including active users, last check time, and system health"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Monitoring status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Monitoring Status",
                    value = """
                    {
                        "status": "success",
                        "monitoring_enabled": true,
                        "active_users": 15,
                        "last_check_time": "2025-09-18T14:45:30Z",
                        "next_check_time": "2025-09-18T14:46:00Z",
                        "check_interval_seconds": 30,
                        "total_risk_events_today": 3,
                        "users_with_trading_disabled": 2,
                        "system_uptime": "2 hours 15 minutes",
                        "kraken_api_status": "connected",
                        "database_status": "healthy"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
        Map<String, Object> status = monitoringService.getMonitoringStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/enable")
    @Operation(summary = "Enable monitoring", description = "Enable the risk monitoring service")
    public ResponseEntity<Map<String, Object>> enableMonitoring() {
        monitoringService.enableMonitoring();
        
        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Risk monitoring enabled"
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable monitoring", description = "Disable the risk monitoring service")
    public ResponseEntity<Map<String, Object>> disableMonitoring() {
        monitoringService.disableMonitoring();
        
        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Risk monitoring disabled"
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Simple health check endpoint to verify the service is running and all components are healthy"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Health Check Response",
                    value = """
                    {
                        "status": "healthy",
                        "service": "Risk Management Service",
                        "version": "1.0.0",
                        "monitoring_enabled": true,
                        "timestamp": "2025-09-18T14:30:00Z",
                        "uptime": "2 hours 15 minutes"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "healthy",
                "service", "Risk Management Service",
                "version", "1.0.0",
                "monitoring_enabled", monitoringService.isMonitoringEnabled(),
                "timestamp", java.time.Instant.now().toString()
        );

        return ResponseEntity.ok(health);
    }
}
