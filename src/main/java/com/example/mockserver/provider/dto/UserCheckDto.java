package com.example.mockserver.provider.dto;

public record UserCheckDto(
        boolean isRegistered,
        boolean isIdentified,
        boolean isRejected,
        boolean rejectReason,
        boolean isSupervised,
        boolean isReadyToSign,
        boolean hasConfirmedPhone,
        boolean hasConfirmedEmail) {
}
