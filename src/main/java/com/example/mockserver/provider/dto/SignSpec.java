package com.example.mockserver.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SignSpec(
        String vendorNumber,
        UserSpec user,
        String certificateSerialNumber,
        UUID certId,
        List<Document> documents) {
}
