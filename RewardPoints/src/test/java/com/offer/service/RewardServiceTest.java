package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;
import com.offer.dto.RewardSummary;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(transactionRepository);
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

        assertTrue(ex.getMessage().contains("Customer Not Found"));
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

    /*
     * ONE TRANSACTION ABOVE $100 -> 90 POINTS
     */
    @Test
    void calculatesPoints_forSingleTransactionOver100() {
        String customerId = "c2";

        Customer c = new Customer();
        c.setId(customerId);
        c.setName("Bob");
        c.setEmail("bob@example.com");

        when(transactionRepository.getCustomerById(customerId))
                .thenReturn(Optional.of(c));

        // Real Transaction object
        Transaction tx = new Transaction(
                customerId,
                120.50,  // cast -> 120 , reward = 40*2 + 50 = 90
                LocalDate.of(2025, Month.MARCH, 10)
        );

        when(transactionRepository.getTransactionsForCustomerId(eq(customerId), any(), any()))
                .thenReturn(List.of(tx));

        RewardSummary summary = rewardService.getCustomerRewards(
                customerId,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 31)
        );

        assertEquals(90L, summary.getTotalPoints());
        assertEquals(1, summary.getPointsPerMonth().size());
        assertEquals(90L, summary.getPointsPerMonth().get("MARCH"));
        assertEquals(1, summary.getTransactions().size());
        assertEquals(90L, summary.getTransactions().get(0).getPoints());
    }
}
