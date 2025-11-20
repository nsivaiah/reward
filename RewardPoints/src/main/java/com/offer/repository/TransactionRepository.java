package com.offer.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.offer.model.Customer;
import com.offer.model.Transaction;

import jakarta.annotation.PostConstruct;

@Repository
public class TransactionRepository {

	private final Map<String, Customer> customers = new HashMap<>();
	private List<Transaction> transactions = new ArrayList<>();

	@PostConstruct
	public void init() {

		customers.put("1", new Customer("1", "Neelima Sivaiahgari", "neelima@gmail.com"));
		customers.put("2", new Customer("2", "Sekhar", "sekhar@gmail.com"));

		transactions.add(new Transaction("1", 120.0, LocalDate.of(2025, 1, 10)));
		transactions.add(new Transaction("1", 75.0, LocalDate.of(2025, 2, 5)));
		transactions.add(new Transaction("1", 200.0, LocalDate.of(2025, 3, 12)));

		transactions.add(new Transaction("2", 60.0, LocalDate.of(2025, 2, 3)));
		transactions.add(new Transaction("2", 130.0, LocalDate.of(2025, 3, 18)));

	}

	public Optional<Customer> getCustomerById(String customerId) {
		return Optional.ofNullable(customers.get(customerId));
	}

	public List<Transaction> getTransactionsForCustomerId(String customerId, LocalDate startDate, LocalDate endDate) {
		return transactions.stream().filter(t -> t.getCustomerId().equals(customerId))
				.filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
				.sorted(Comparator.comparing(Transaction::getDate)).collect(Collectors.toList());
	}
}
