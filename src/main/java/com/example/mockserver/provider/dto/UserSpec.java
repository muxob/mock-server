package com.example.mockserver.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSpec(String identificationNumber, String country, UUID automatedSignCertID) {
}
