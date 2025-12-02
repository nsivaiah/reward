package com.offer.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.offer.dto.RewardSummary;
import com.offer.dto.RewardTransactionPoints;
import com.offer.exception.DataValidationException;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;

@Service
public class RewardServiceImpl implements RewardService {

	private static final Logger log = LoggerFactory.getLogger(RewardService.class);

	private final TransactionRepository transactionRepository;

	public RewardServiceImpl(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	/*
	 * Below logic is to fetch the reward points
	 */
	public RewardSummary getCustomerRewards(String customerId, LocalDate startDate, LocalDate endDate) {

	    if (customerId == null || customerId.isBlank()) {
	        throw new DataValidationException("Customer ID cannot be null or empty");
	    }

	    if (startDate == null) {
	        throw new DataValidationException("Start date cannot be null");
	    }

	    if (endDate == null) {
	        throw new DataValidationException("End date cannot be null");
	    }

	    if (startDate.isAfter(endDate)) {
	        throw new DataValidationException("Start date cannot be after end date");
	    }

	    if (startDate.isAfter(LocalDate.now()) || endDate.isAfter(LocalDate.now())) {
	        throw new DataValidationException("Dates cannot be in the future");
	    }

		Optional<Customer> customer = transactionRepository.getCustomerById(customerId);
		if (customer.isEmpty()) {
			throw new DataValidationException("Customer not found");
		}
		
        log.debug("Customer found: {}", customer.get());

		List<Transaction> transactions = transactionRepository.getTransactionsForCustomerId(customerId, startDate,
				endDate);

		List<RewardTransactionPoints> enriched = new ArrayList<>();
		Map<String, Long> monthlyPointsInYear = new HashMap<>();
		long total = 0;
		

		for (Transaction transaction : transactions) {
            log.debug("Processing transaction: {}", transaction);
            
            if (transaction.getAmount() < 0) {
                throw new DataValidationException("Transaction amount cannot be negative");
            }

			long points = calculatePoints(transaction.getAmount());
            log.debug("Calculated points for transaction {} = {}", transaction.getId(), points);

            RewardTransactionPoints rewardTransactionPoints = new RewardTransactionPoints();
			rewardTransactionPoints.setTransactionId(transaction.getId());
			rewardTransactionPoints.setDate(transaction.getDate());
			rewardTransactionPoints.setAmount(transaction.getAmount());
			rewardTransactionPoints.setPoints(points);
			enriched.add(rewardTransactionPoints);

			String monthAndYearKey = transaction.getDate().getMonth().toString() + "-" + transaction.getDate().getYear();
			monthlyPointsInYear.merge(monthAndYearKey,points,Long::sum);
			
			total = total + points;
		}
		
        log.info("Total reward points for customer {} = {}", customerId, total);

		return new RewardSummary(customer.get().getId(), customer.get().getName(), customer.get().getEmail(),
				monthlyPointsInYear, total, enriched);
	}

	private long calculatePoints(double amount) {
		long points = 0;
		long dollars = Math.round(amount);
		if (dollars > 100) {
			points = points + (dollars - 100) * 2;
			dollars = 100;
		}
		if (dollars > 50) {
			points = points + (dollars - 50) * 1;
		}
        log.debug("Points calculated for amount {} = {}", amount, points);

		return points;
	}

}
