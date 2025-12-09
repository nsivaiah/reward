package com.offer.service;

import java.time.LocalDate;

import com.offer.dto.RewardSummary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface BaseRewardService {
	
    RewardSummary getCustomerRewards(
    		@NotBlank(message = "Customer ID cannot be null or empty") String customerId,
            @NotNull(message = "Start date cannot be null") LocalDate startDate,
            @NotNull(message = "End date cannot be null") LocalDate endDate);

}
