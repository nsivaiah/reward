package com.offer.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.offer.dto.RewardSummary;
import com.offer.service.RewardService;

@RestController
@RequestMapping("/rewards")
public class RewardController {
	
    private static final Logger log = LoggerFactory.getLogger(RewardController.class);

	private final RewardService rewardService;

	public RewardController(RewardService rewardService) {
		this.rewardService = rewardService;
	}

	/*
	 * Below API is used fetch total reward points as well as reward points per each month
	 */
	@GetMapping("/{customerId}")
	public RewardSummary getRewards(@PathVariable String customerId, 
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
	    
		log.info("Received request for rewards: customerId={}, startDate={}, endDate={}",
	            customerId, startDate, endDate);

		return rewardService.getCustomerRewards(customerId, startDate, endDate);
	}
	
}
