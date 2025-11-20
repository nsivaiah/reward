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
import com.offer.dto.TransactionWithPoints;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;

@Service
public class RewardService {
	
    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

	private final TransactionRepository transactionRepository;

	public RewardService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	/*
	 * Below logic is to fetch the reward points
	 */
	public RewardSummary getCustomerRewards(String customerId, LocalDate startDate, LocalDate endDate) {

		Optional<Customer> customer = transactionRepository.getCustomerById(customerId);
		if (customer.isEmpty()) {
			throw new RuntimeException("Customer Not Found");
		}
		
        log.debug("Customer found: {}", customer.get());

		List<Transaction> transactions = transactionRepository.getTransactionsForCustomerId(customerId, startDate,
				endDate);

		List<TransactionWithPoints> enriched = new ArrayList<>();
		Map<String, Long> monthlyPoints = new HashMap<>();
		String month = null;
		long total = 0;
		TransactionWithPoints transactionWithPoints = null;

		for (Transaction transaction : transactions) {
            log.debug("Processing transaction: {}", transaction);

			long points = calculatePoints(transaction.getAmount());
            log.debug("Calculated points for transaction {} = {}", transaction.getId(), points);

			transactionWithPoints = new TransactionWithPoints();
			transactionWithPoints.setTransactionId(transaction.getId());
			transactionWithPoints.setDate(transaction.getDate());
			transactionWithPoints.setAmount(transaction.getAmount());
			transactionWithPoints.setPoints(points);
			enriched.add(transactionWithPoints);

			month = transaction.getDate().getMonth().toString();
			if (!monthlyPoints.containsKey(month)) {
				monthlyPoints.put(month, points);
			} else {
				monthlyPoints.put(month, monthlyPoints.get(month) + points);
			}
			total = total + points;
		}
		
        log.info("Total reward points for customer {} = {}", customerId, total);

		return new RewardSummary(customer.get().getId(), customer.get().getName(), customer.get().getEmail(),
				monthlyPoints, total, enriched);
	}

	private long calculatePoints(double amount) {
		long points = 0;
		long dollars = (long) amount;
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
