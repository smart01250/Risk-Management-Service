package com.assessment.riskmanagement.dto.kraken;

import java.math.BigDecimal;

public class KrakenOrderRequest {
    
    private String orderType = "mkt"; // market order by default
    private String symbol;
    private String side;
    private BigDecimal size;
    private BigDecimal stopPrice;

    public KrakenOrderRequest() {}

    public KrakenOrderRequest(String symbol, String side, BigDecimal size) {
        this.symbol = symbol;
        this.side = side;
        this.size = size;
    }

    // Getters and Setters
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public BigDecimal getSize() { return size; }
    public void setSize(BigDecimal size) { this.size = size; }

    public BigDecimal getStopPrice() { return stopPrice; }
    public void setStopPrice(BigDecimal stopPrice) { this.stopPrice = stopPrice; }

    @Override
    public String toString() {
        return "KrakenOrderRequest{" +
                "orderType='" + orderType + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", size=" + size +
                ", stopPrice=" + stopPrice +
                '}';
    }
}
