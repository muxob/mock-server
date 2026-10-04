package com.example.mockserver.provider.dto;

import java.util.List;

public record FollowUpGroupDto(String threadID, List<TransactionDto> transactions) {
}
