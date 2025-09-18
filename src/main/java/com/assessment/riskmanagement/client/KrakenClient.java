package com.assessment.riskmanagement.client;

import com.assessment.riskmanagement.dto.kraken.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Component
public class KrakenClient {

    private static final Logger logger = LoggerFactory.getLogger(KrakenClient.class);

    @Value("${risk-management.kraken.base-url}")
    private String baseUrl;

    @Value("${risk-management.kraken.api-version}")
    private String apiVersion;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KrakenClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public KrakenAccountResponse getAccountInfo(String apiKey, String privateKey) {
        try {
            String endpoint = "accounts";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, "");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<KrakenAccountResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, KrakenAccountResponse.class);
            
            logger.info("Account info retrieved successfully for API key: {}", maskApiKey(apiKey));
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error getting account info: {}", e.getMessage());
            throw new RuntimeException("Failed to get account info", e);
        }
    }

    public KrakenBalanceResponse getBalances(String apiKey, String privateKey) {
        try {
            String endpoint = "accounts";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, "");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<KrakenBalanceResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, KrakenBalanceResponse.class);
            
            logger.info("Balances retrieved successfully");
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error getting balances: {}", e.getMessage());
            throw new RuntimeException("Failed to get balances", e);
        }
    }

    public KrakenOrdersResponse getOpenOrders(String apiKey, String privateKey) {
        try {
            String endpoint = "openorders";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, "");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<KrakenOrdersResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, KrakenOrdersResponse.class);
            
            KrakenOrdersResponse ordersResponse = response.getBody();
            int orderCount = ordersResponse != null && ordersResponse.getOpenOrders() != null ? 
                ordersResponse.getOpenOrders().size() : 0;
            logger.info("Retrieved {} open orders", orderCount);
            
            return ordersResponse;
            
        } catch (Exception e) {
            logger.error("Error getting open orders: {}", e.getMessage());
            throw new RuntimeException("Failed to get open orders", e);
        }
    }

    public KrakenOrderResponse placeOrder(String apiKey, String privateKey, KrakenOrderRequest orderRequest) {
        try {
            String endpoint = "sendorder";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("orderType", orderRequest.getOrderType());
            formData.add("symbol", orderRequest.getSymbol());
            formData.add("side", orderRequest.getSide());
            formData.add("size", orderRequest.getSize().toString());
            
            if (orderRequest.getStopPrice() != null) {
                formData.add("stopPrice", orderRequest.getStopPrice().toString());
            }
            
            String postData = createPostData(formData);
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, postData);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KrakenOrderResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, KrakenOrderResponse.class);
            
            logger.info("Order placed successfully: {}", orderRequest);
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage());
            throw new RuntimeException("Failed to place order", e);
        }
    }

    public KrakenCancelResponse cancelOrder(String apiKey, String privateKey, String orderId) {
        try {
            String endpoint = "cancelorder";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("order_id", orderId);
            
            String postData = createPostData(formData);
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, postData);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KrakenCancelResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, KrakenCancelResponse.class);
            
            logger.info("Order cancelled successfully: {}", orderId);
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to cancel order", e);
        }
    }

    public KrakenCancelResponse cancelAllOrders(String apiKey, String privateKey, String symbol) {
        try {
            String endpoint = "cancelallorders";
            String url = String.format("%s/derivatives/api/%s/%s", baseUrl, apiVersion, endpoint);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            if (symbol != null && !symbol.isEmpty()) {
                formData.add("symbol", symbol);
            }
            
            String postData = createPostData(formData);
            HttpHeaders headers = createAuthenticatedHeaders(apiKey, privateKey, endpoint, postData);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KrakenCancelResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, KrakenCancelResponse.class);
            
            logger.info("All orders cancelled successfully");
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error cancelling all orders: {}", e.getMessage());
            throw new RuntimeException("Failed to cancel all orders", e);
        }
    }

    private HttpHeaders createAuthenticatedHeaders(String apiKey, String privateKey, String endpoint, String postData) {
        try {
            String nonce = String.valueOf(System.currentTimeMillis() * 1000);
            String urlPath = "/derivatives/api/" + apiVersion + "/" + endpoint;
            
            String signature = generateSignature(urlPath, postData, nonce, privateKey);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("API-Key", apiKey);
            headers.set("API-Sign", signature);
            headers.set("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            
            return headers;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create authenticated headers", e);
        }
    }

    private String generateSignature(String urlPath, String postData, String nonce, String privateKey) throws Exception {
        String message = nonce + postData;
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(message.getBytes(StandardCharsets.UTF_8));
        
        byte[] pathBytes = urlPath.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[pathBytes.length + hash.length];
        System.arraycopy(pathBytes, 0, combined, 0, pathBytes.length);
        System.arraycopy(hash, 0, combined, pathBytes.length, hash.length);
        
        byte[] secretBytes = Base64.decodeBase64(privateKey);
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, "HmacSHA512");
        mac.init(secretKeySpec);
        
        byte[] signature = mac.doFinal(combined);
        return Base64.encodeBase64String(signature);
    }

    private String createPostData(MultiValueMap<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
            for (String value : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                  .append("=")
                  .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
