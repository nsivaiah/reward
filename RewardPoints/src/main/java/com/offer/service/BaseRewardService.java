package com.offer.service;

import java.time.LocalDate;

import com.offer.dto.RewardSummary;

public interface BaseRewardService {
	
    RewardSummary getCustomerRewards(String customerId, LocalDate startDate, LocalDate endDate);

}
