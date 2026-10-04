package com.example.mockserver.provider.dto;

public record CertificateDto(String serialNumber, String certificate, int type, int coverage, long dateValidTo) {
}
