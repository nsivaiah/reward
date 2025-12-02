package com.offer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.offer.dto.RewardSummary;
import com.offer.dto.RewardTransactionPoints;
import com.offer.exception.DataValidationException;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

	@Mock
	private TransactionRepository transactionRepository;
	private RewardServiceImpl rewardService;

	@BeforeEach
	void setUp() {
		transactionRepository = mock(TransactionRepository.class);
		rewardService = new RewardServiceImpl(transactionRepository);
	}

	@Test
	void testGetCustomerRewards_Success() {
		String customerId = "CUST1";
		Customer customer = new Customer();
		customer.setId(customerId);
		customer.setName("John Doe");
		customer.setEmail("john@example.com");

		when(transactionRepository.getCustomerById(customerId)).thenReturn(Optional.of(customer));

		Transaction t1 = new Transaction();
		t1.setId("1");
		t1.setAmount(120.0);
		t1.setDate(LocalDate.of(2025, 1, 15));

		Transaction t2 = new Transaction();
		t2.setId("2");
		t2.setAmount(80.0);
		t2.setDate(LocalDate.of(2025, 2, 10));

		List<Transaction> transactions = Arrays.asList(t1, t2);

		when(transactionRepository.getTransactionsForCustomerId(eq(customerId), any(), any())).thenReturn(transactions);

		RewardSummary summary = rewardService.getCustomerRewards(customerId, LocalDate.of(2025, 1, 1),
				LocalDate.of(2025, 2, 28));

		// Monthly points assertions
		Map<String, Long> pointsPerMonth = summary.getPointsPerMonth();
		assertEquals(90L, pointsPerMonth.get("JANUARY-2025"));
		assertEquals(30L, pointsPerMonth.get("FEBRUARY-2025"));

		// Total points
		assertEquals(120L, summary.getTotalPoints());

		// Transaction points
		List<RewardTransactionPoints> enriched = summary.getTransactions();
		assertEquals(2, enriched.size());
		assertEquals(90L, enriched.get(0).getPoints());
		assertEquals(30L, enriched.get(1).getPoints());
	}

	@Test
	void testGetCustomerRewards_InvalidCustomerId() {
		when(transactionRepository.getCustomerById("CUST2")).thenReturn(Optional.empty());
		DataValidationException ex = assertThrows(DataValidationException.class,
				() -> rewardService.getCustomerRewards("CUST2", LocalDate.now().minusDays(1), LocalDate.now()));
		assertEquals("Customer not found", ex.getMessage());
	}

	@Test
	void testGetCustomerRewards_InvalidDates() {
		DataValidationException ex = assertThrows(DataValidationException.class,
				() -> rewardService.getCustomerRewards("CUST1", LocalDate.now().plusDays(1), LocalDate.now()));
		assertEquals("Start date cannot be after end date", ex.getMessage());
	}

	@Test
	void testGetCustomerRewards_NegativeTransaction() {
		String customerId = "CUST1";
		Customer customer = new Customer();
		customer.setId(customerId);
		customer.setName("John Doe");
		customer.setEmail("john@example.com");

		when(transactionRepository.getCustomerById(customerId)).thenReturn(Optional.of(customer));

		Transaction t1 = new Transaction();
		t1.setId("1");
		t1.setAmount(-50.0);
		t1.setDate(LocalDate.of(2025, 1, 15));

		when(transactionRepository.getTransactionsForCustomerId(eq(customerId), any(), any())).thenReturn(List.of(t1));

		DataValidationException ex = assertThrows(DataValidationException.class, () -> rewardService
				.getCustomerRewards(customerId, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)));
		assertEquals("Transaction amount cannot be negative", ex.getMessage());
	}
	
	/*
     * CUSTOMER NOT FOUND
     */
    
    @Test
    void throws_whenCustomerNotFound() {
        String customerId = "missing";

        when(transactionRepository.getCustomerById(customerId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rewardService.getCustomerRewards(
                        customerId,
                        LocalDate.now().minusMonths(1),
                        LocalDate.now()
                ));

        assertTrue(ex.getMessage().toLowerCase().contains("customer not found"));
    }

    /*
     * NO TRANSACTIONS -> ZERO REWARDS
     */
    @Test
    void returnsZero_whenNoTransactions() {
        String customerId = "c1";

        Customer c = new Customer();
        c.setId(customerId);
        c.setName("Test User");
        c.setEmail("test@mail.com");

        when(transactionRepository.getCustomerById(customerId))
                .thenReturn(Optional.of(c));

        when(transactionRepository.getTransactionsForCustomerId(eq(customerId), any(), any()))
                .thenReturn(List.of()); // empty list

        RewardSummary summary = rewardService.getCustomerRewards(
                customerId,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        assertEquals(0L, summary.getTotalPoints());
        assertTrue(summary.getPointsPerMonth().isEmpty());
        assertTrue(summary.getTransactions().isEmpty());
    }

}
