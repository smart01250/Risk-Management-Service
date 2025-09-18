package com.assessment.riskmanagement.dto.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenBalanceResponse extends KrakenAccountResponse {
    // Inherits from KrakenAccountResponse as balance info is in accounts
}
