package com.assessment.riskmanagement.dto.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenOrdersResponse {
    
    private String result;
    private String error;
    
    @JsonProperty("openOrders")
    private List<KrakenOrder> openOrders;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KrakenOrder {
        private String orderId;
        private String symbol;
        private String side;
        private BigDecimal size;
        private String orderType;
        private String status;
        
        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getSide() { return side; }
        public void setSide(String side) { this.side = side; }
        
        public BigDecimal getSize() { return size; }
        public void setSize(BigDecimal size) { this.size = size; }
        
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Getters and Setters
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<KrakenOrder> getOpenOrders() { return openOrders; }
    public void setOpenOrders(List<KrakenOrder> openOrders) { this.openOrders = openOrders; }
}
