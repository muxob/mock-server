package com.example.mockserver.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpSignSpec(String vendorNumber, String transactionID, List<String> hash) {
}
