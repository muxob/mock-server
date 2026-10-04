package com.example.mockserver.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InitSpec(String vendorNumber, OtpUserSpec user, List<DocumentDescription> documents) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OtpUserSpec(
            String documentNumber,
            String identificationNumber,
            String country,
            String firstName,
            String lastName,
            String phone) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocumentDescription(String description) {
    }
}
