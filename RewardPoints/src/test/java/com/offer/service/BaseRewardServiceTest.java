package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import com.offer.dto.RewardSummary;
import com.offer.exception.DataValidationException;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

class BaseRewardServiceTest {

	private TransactionRepository transactionRepository;
	private BaseRewardService baseRewardService;

	@BeforeEach
	void setUp() {
		transactionRepository = mock(TransactionRepository.class);
		baseRewardService = new RewardServiceImpl(transactionRepository); // use impl
	}

	@Test
	void testGetCustomerRewards_Success() {
		String customerId = "CUST1";
		Customer customer = new Customer();
		customer.setId(customerId);
		customer.setName("Alice");
		customer.setEmail("alice@example.com");

		when(transactionRepository.getCustomerById(customerId)).thenReturn(Optional.of(customer));

		Transaction t1 = new Transaction();
		t1.setId("1");
		t1.setAmount(120.0);
		t1.setDate(LocalDate.of(2025, 1, 10));

		when(transactionRepository.getTransactionsForCustomerId(eq(customerId), any(), any())).thenReturn(List.of(t1));

		RewardSummary summary = baseRewardService.getCustomerRewards(customerId, LocalDate.of(2025, 1, 1),
				LocalDate.of(2025, 1, 31));

		assertEquals(1, summary.getTransactions().size());
		assertEquals(90L, summary.getTransactions().get(0).getPoints());
		assertEquals(90L, summary.getPointsPerMonth().get("JANUARY-2025"));
		assertEquals(90L, summary.getTotalPoints());
	}

	@Test
	void testGetCustomerRewards_CustomerNotFound() {
		when(transactionRepository.getCustomerById("CUST2")).thenReturn(Optional.empty());
		DataValidationException ex = assertThrows(DataValidationException.class,
				() -> baseRewardService.getCustomerRewards("CUST2", LocalDate.now().minusDays(1), LocalDate.now()));
		assertEquals("Customer not found", ex.getMessage());
	}

	@Test
	void testGetCustomerRewards_InvalidDates() {
		DataValidationException ex = assertThrows(DataValidationException.class,
				() -> baseRewardService.getCustomerRewards("CUST1", LocalDate.now().plusDays(1), LocalDate.now()));
		assertEquals("Start date cannot be after end date", ex.getMessage());
	}

}
