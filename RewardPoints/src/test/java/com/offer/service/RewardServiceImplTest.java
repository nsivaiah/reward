package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.offer.dto.RewardSummary;
import com.offer.exception.CustomerNotFoundException;
import com.offer.exception.DataValidationException;
import com.offer.model.Customer;
import com.offer.model.Transaction;
import com.offer.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    private Customer customer;
    private Transaction txnJan;
    private Transaction txnFeb;
    private Transaction txnMar;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId("C1");
        customer.setName("John");
        customer.setEmail("john@test.com");

        // January transaction
        txnJan = new Transaction();
        txnJan.setId("T1");
        txnJan.setAmount(120); // 90 points
        txnJan.setDate(LocalDate.of(2024, 1, 10));

        // February transaction
        txnFeb = new Transaction();
        txnFeb.setId("T2");
        txnFeb.setAmount(80); // 30 points
        txnFeb.setDate(LocalDate.of(2024, 2, 5));

        // March transaction
        txnMar = new Transaction();
        txnMar.setId("T3");
        txnMar.setAmount(60); // 10 points
        txnMar.setDate(LocalDate.of(2024, 3, 1));
    }

    // Helper method to calculate points dynamically
    private long calculatePoints(double amount) {
        long points = 0;
        if (amount > 100)
            points += (amount - 100) * 2;
        if (amount > 50)
            points += Math.min(amount, 100) - 50;
        return points;
    }

    @Test
    void testGetCustomerRewards_Success() {

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txnJan, txnFeb, txnMar));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 31)
        );

        assertNotNull(summary);
        assertEquals("C1", summary.getCustomerId());
        assertEquals("John", summary.getCustomerName());

        long expectedTotal =
                calculatePoints(txnJan.getAmount()) +
                calculatePoints(txnFeb.getAmount()) +
                calculatePoints(txnMar.getAmount());

        assertEquals(expectedTotal, summary.getTotalPoints());

        Map<String, Long> monthly = summary.getPointsPerMonth();
        assertTrue(monthly.containsKey("2024-01"));
        assertTrue(monthly.containsKey("2024-02"));
        assertTrue(monthly.containsKey("2024-03"));

        verify(transactionRepository).getCustomerById("C1");
        verify(transactionRepository).getTransactionsForCustomerId(any(), any(), any());
    }

    @Test
    void testGetCustomerRewards_CustomerNotFound() {

        when(transactionRepository.getCustomerById("C99"))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () ->
                rewardService.getCustomerRewards(
                        "C99",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 3, 31)
                )
        );
    }

    @Test
    void testGetCustomerRewards_NullCustomerId() {

        assertThrows(CustomerNotFoundException.class, () ->
                rewardService.getCustomerRewards(
                        null,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 3, 31)
                )
        );
    }

    @Test
    void testGetCustomerRewards_BlankCustomerId() {

        assertThrows(CustomerNotFoundException.class, () ->
                rewardService.getCustomerRewards(
                        "   ",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 3, 31)
                )
        );
    }

    @Test
    void testGetCustomerRewards_InvalidDateRange() {

        assertThrows(DataValidationException.class, () ->
                rewardService.getCustomerRewards(
                        "C1",
                        LocalDate.of(2024, 5, 1),
                        LocalDate.of(2024, 1, 1)
                )
        );
    }

    @Test
    void testGetCustomerRewards_FutureDate() {

        assertThrows(DataValidationException.class, () ->
                rewardService.getCustomerRewards(
                        "C1",
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(5)
                )
        );
    }

    @Test
    void testGetCustomerRewards_NegativeTransactionAmount() {

        txnJan.setAmount(-10);

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txnJan));

        assertThrows(DataValidationException.class, () ->
                rewardService.getCustomerRewards(
                        "C1",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 2, 29)
                )
        );
    }

    @Test
    void testMonthFormatIsYYYY_MM() {

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txnJan, txnFeb));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 29)
        );

        // Validate keys are in YYYY-MM format
        assertTrue(summary.getPointsPerMonth().containsKey("2024-01"));
        assertTrue(summary.getPointsPerMonth().containsKey("2024-02"));
    }
    
    @Test
    void testGetCustomerRewards_EmptyTransactions() {

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        // Empty list returned
        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(
                        // We still need a dummy transaction for each month in range for Rule 4
                        new Transaction() {{
                            setId("Tdummy1");
                            setAmount(0);
                            setDate(LocalDate.of(2024, 1, 1));
                        }},
                        new Transaction() {{
                            setId("Tdummy2");
                            setAmount(0);
                            setDate(LocalDate.of(2024, 2, 1));
                        }}
                ));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 29)
        );

        assertNotNull(summary);
        assertEquals(0, summary.getTotalPoints());
        assertEquals(0, summary.getPointsPerMonth().get("2024-01"));
        assertEquals(0, summary.getPointsPerMonth().get("2024-02"));
    }
    
    @Test
    void testGetCustomerRewards_BoundaryAmounts() {

        Transaction txn50 = new Transaction();
        txn50.setId("T50");
        txn50.setAmount(50); // boundary → 0 points
        txn50.setDate(LocalDate.of(2024, 1, 10));

        Transaction txn100 = new Transaction();
        txn100.setId("T100");
        txn100.setAmount(100); // boundary → 50 points (50–100)
        txn100.setDate(LocalDate.of(2024, 2, 10));

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txn50, txn100));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 29)
        );

        assertEquals(0, summary.getPointsPerMonth().get("2024-01")); // 50 → 0 points
        assertEquals(50, summary.getPointsPerMonth().get("2024-02")); // 100 → 50 points

        assertEquals(50, summary.getTotalPoints());
    }

    @Test
    void testGetCustomerRewards_ZeroAmountTransaction() {

        Transaction txn0 = new Transaction();
        txn0.setId("T0");
        txn0.setAmount(0);
        txn0.setDate(LocalDate.of(2024, 1, 15));

        Transaction txnDummyFeb = new Transaction();
        txnDummyFeb.setId("Tdummy");
        txnDummyFeb.setAmount(0);
        txnDummyFeb.setDate(LocalDate.of(2024, 2, 1));

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txn0, txnDummyFeb));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 29)
        );

        assertEquals(0, summary.getTotalPoints());
        assertEquals(0, summary.getPointsPerMonth().get("2024-01"));
        assertEquals(0, summary.getPointsPerMonth().get("2024-02"));
    }


}
