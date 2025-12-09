package com.offer.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.offer.dto.RewardSummary;
import com.offer.dto.RewardTransactionPoints;
import com.offer.exception.CustomerNotFoundException;
import com.offer.exception.DataValidationException;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RewardServiceImpl implements RewardService {

	private static final Logger log = LoggerFactory.getLogger(RewardService.class);

	private final TransactionRepository transactionRepository;

	public RewardServiceImpl(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	/*
	 * Below logic is to fetch the reward points
	 */
	public RewardSummary getCustomerRewards(
            @NotBlank(message = "Customer ID cannot be null or empty") String customerId,
            @NotNull(message = "Start date cannot be null") LocalDate startDate,
            @NotNull(message = "End date cannot be null") LocalDate endDate) {

		/*
		 * Below method is used to validate input parameters
		 */
		validateInputParameters(customerId, startDate, endDate);

		Customer customer = transactionRepository.getCustomerById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found : " + customerId));

		List<Transaction> transactions = transactionRepository.getTransactionsForCustomerId(customerId, startDate,
				endDate);

		List<RewardTransactionPoints> enriched = new ArrayList<>();
		Map<String, Long> monthlyPointsInYear = new HashMap<>();
		long total = 0;

		for (Transaction transaction : transactions) {

			if (transaction.getAmount() < 0) {
				throw new DataValidationException("Transaction amount cannot be negative");
			}

			long points = calculatePoints(transaction.getAmount());

			RewardTransactionPoints rewardTransactionPoints = new RewardTransactionPoints();
			rewardTransactionPoints.setTransactionId(transaction.getId());
			rewardTransactionPoints.setDate(transaction.getDate());
			rewardTransactionPoints.setAmount(transaction.getAmount());
			rewardTransactionPoints.setPoints(points);
			enriched.add(rewardTransactionPoints);

			String monthAndYearKey = java.time.YearMonth.from(transaction.getDate()).toString();
			monthlyPointsInYear.merge(monthAndYearKey, points, Long::sum);

			total += points;
		}

		log.info(
			    "Reward summary generated | customerId={} | customerName={} | from={} | to={} | totalPoints={} | monthlyBreakup={}",
			    customerId,
			    customer.getName(),
			    startDate,
			    endDate,
			    total,
			    monthlyPointsInYear
			);

		return new RewardSummary(customer.getId(), customer.getName(), customer.getEmail(),
				monthlyPointsInYear, total, enriched);
	}

	/*
	 * Below method is used to validate input parameters
	 */
	private void validateInputParameters(String customerId, LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new DataValidationException("Start date cannot be after end date");
		}

		if (startDate.isAfter(LocalDate.now()) || endDate.isAfter(LocalDate.now())) {
			throw new DataValidationException("Dates cannot be in the future");
		}
	}

	private long calculatePoints(double amount) {
		double points = 0;
		if (amount > 100) {
			points += (amount - 100) * 2;
			amount = 100;
		}
		if (amount > 50) {
			points += (amount - 50) * 1;
		}
		
		long finalPoints = (long) points;
		return finalPoints;
	}

}
