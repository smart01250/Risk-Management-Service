package com.assessment.riskmanagement.dto.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenOrderResponse {
    
    private String result;
    private String error;
    
    @JsonProperty("sendStatus")
    private SendStatus sendStatus;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SendStatus {
        @JsonProperty("order_id")
        private String orderId;
        private String status;
        
        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Getters and Setters
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public SendStatus getSendStatus() { return sendStatus; }
    public void setSendStatus(SendStatus sendStatus) { this.sendStatus = sendStatus; }
}
