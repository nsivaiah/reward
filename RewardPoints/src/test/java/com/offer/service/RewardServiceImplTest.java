package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

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
    private Transaction txn1;
    private Transaction txn2;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId("C1");
        customer.setName("John");
        customer.setEmail("john@test.com");

        txn1 = new Transaction();
        txn1.setId("T1");
        txn1.setAmount(120);
        txn1.setDate(LocalDate.of(2024, 1, 10));

        txn2 = new Transaction();
        txn2.setId("T2");
        txn2.setAmount(80);
        txn2.setDate(LocalDate.of(2024, 2, 5));
    }

    @Test
    void testGetCustomerRewards_Success() {

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txn1, txn2));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1)
        );

        assertNotNull(summary);
        assertEquals("C1", summary.getCustomerId());
        assertEquals("John", summary.getCustomerName());

        //FIXED EXPECTED VALUE
        assertEquals(120, summary.getTotalPoints());

        Map<String, Long> monthly = summary.getPointsPerMonth();
        assertTrue(monthly.containsKey("2024-01"));
        assertTrue(monthly.containsKey("2024-02"));

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
                        LocalDate.of(2024, 3, 1)
                )
        );
    }

    // NULL CUSTOMER ID 
    @Test
    void testGetCustomerRewards_NullCustomerId() {

        assertThrows(CustomerNotFoundException.class, () ->
                rewardService.getCustomerRewards(
                        null,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 3, 1)
                )
        );
    }

    //  BLANK CUSTOMER ID 
    @Test
    void testGetCustomerRewards_BlankCustomerId() {

        assertThrows(CustomerNotFoundException.class, () ->
                rewardService.getCustomerRewards(
                        "   ",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 3, 1)
                )
        );
    }

    // START DATE AFTER END DATE
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

    // FUTURE DATE
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

    // NEGATIVE TRANSACTION AMOUNT
    @Test
    void testGetCustomerRewards_NegativeTransactionAmount() {

        txn1.setAmount(-10);

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txn1));

        assertThrows(DataValidationException.class, () ->
                rewardService.getCustomerRewards(
                        "C1",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 2, 1)
                )
        );
    }

    @Test
    void testMonthFormatIsYYYY_MM() {

        when(transactionRepository.getCustomerById("C1"))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.getTransactionsForCustomerId(
                anyString(), any(), any()))
                .thenReturn(List.of(txn1));

        RewardSummary summary = rewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1)
        );

        // FIX: Check monthly points map, NOT totalPoints
        assertTrue(summary.getPointsPerMonth().containsKey("2024-01"));
    }
}
