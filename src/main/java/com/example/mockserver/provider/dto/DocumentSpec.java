package com.example.mockserver.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentSpec(String vendorNumber, String transactionID) {
}
