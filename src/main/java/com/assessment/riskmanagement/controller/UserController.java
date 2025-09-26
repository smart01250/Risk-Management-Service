package com.assessment.riskmanagement.controller;

import com.assessment.riskmanagement.dto.UserRegistrationRequest;
import com.assessment.riskmanagement.dto.UserResponse;
import com.assessment.riskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User registration and management endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Register a new user with Kraken API credentials and risk settings. Returns a unique client ID for the user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "status": "success",
                        "message": "User registered successfully",
                        "client_id": "CLIENT_ABC123XYZ",
                        "user": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "clientId": "CLIENT_ABC123XYZ",
                            "dailyRiskPercentage": 2.5,
                            "dailyRiskAbsolute": 1000.00,
                            "initialBalance": 50000.00,
                            "tradingEnabled": true,
                            "isActive": true,
                            "createdAt": "2025-09-18T10:30:00Z",
                            "lastRiskCheck": null
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or validation error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                        "status": "error",
                        "message": "Kraken API key is required and must be valid"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User already exists with these credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    value = """
                    {
                        "status": "error",
                        "message": "User with these Kraken credentials already exists"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details with Kraken API credentials",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Registration Request",
                        value = """
                        {
                            "krakenApiKey": "/tjRZL5FBJ/IPhYv4CBrox7mPfPgiQ8v9ZT+z5EY9e28Hck4y9sYjOvP",
                            "krakenPrivateKey": "T1qIe4efEWBjletsVTldzux9f4sH/yDpTp3vvL3XAaZhZVdTVBGPPn14MZebBxkPP1V5RNcmIdK2DYGk+N+MPPh9",
                            "dailyRiskPercentage": 2.5,
                            "dailyRiskAbsolute": 1000.00
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse user = userService.registerUser(request);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "User registered successfully",
                    "client_id", user.getClientId(),
                    "user", user
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all registered users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{clientId}")
    @Operation(
        summary = "Get user by client ID",
        description = "Retrieve a specific user by their client ID with current risk settings and status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "status": "success",
                        "user": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "clientId": "CLIENT_ABC123XYZ",
                            "dailyRiskPercentage": 2.5,
                            "dailyRiskAbsolute": 1000.00,
                            "initialBalance": 50000.00,
                            "tradingEnabled": true,
                            "isActive": true,
                            "createdAt": "2025-09-18T10:30:00Z",
                            "lastRiskCheck": "2025-09-18T14:45:00Z"
                        }
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
                    name = "Not Found Error",
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
    public ResponseEntity<Map<String, Object>> getUserByClientId(
            @Parameter(
                description = "Client ID of the user (e.g., CLIENT_ABC123XYZ)",
                example = "CLIENT_ABC123XYZ"
            )
            @PathVariable String clientId) {
        try {
            UserResponse user = userService.getUserByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "user", user
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

    @PutMapping("/{clientId}")
    @Operation(summary = "Update user", description = "Update user risk settings")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId,
            @Valid @RequestBody UserRegistrationRequest updateRequest) {
        try {
            UserResponse user = userService.updateUser(clientId, updateRequest);
            
            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "User updated successfully",
                    "user", user
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

    @PutMapping("/{clientId}/trading/{enabled}")
    @Operation(
        summary = "Enable/Disable trading for user",
        description = "Enable or disable trading for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Trading status updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "status": "success",
                        "message": "Trading enabled for user CLIENT_ABC123XYZ",
                        "user": {
                            "clientId": "CLIENT_ABC123XYZ",
                            "tradingEnabled": true,
                            "isActive": true
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> setTradingEnabled(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId,
            @Parameter(description = "true to enable trading, false to disable") @PathVariable boolean enabled) {
        try {
            UserResponse user = userService.setTradingEnabled(clientId, enabled);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", (enabled ? "Trading enabled" : "Trading disabled") + " for user " + clientId,
                    "user", user
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

    @DeleteMapping("/{clientId}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "Client ID of the user") @PathVariable String clientId) {
        try {
            userService.deleteUser(clientId);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "User deleted successfully"
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
}
