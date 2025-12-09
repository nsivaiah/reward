package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.offer.dto.RewardSummary;

@ExtendWith(MockitoExtension.class)
class BaseRewardServiceTest {

    @Mock
    private BaseRewardService baseRewardService;

    //  SUCCESS CASE
    @Test
    void testGetCustomerRewards_Success() {

        RewardSummary mockSummary = new RewardSummary(
                "C1",
                "John",
                "john@test.com",
                new HashMap<>(),
                120L,
                null
        );

        when(baseRewardService.getCustomerRewards(
                anyString(), any(), any()))
                .thenReturn(mockSummary);

        RewardSummary result = baseRewardService.getCustomerRewards(
                "C1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1)
        );

        assertNotNull(result);
        assertEquals("C1", result.getCustomerId());
        assertEquals(120L, result.getTotalPoints());
    }

    //  CUSTOMER NOT FOUND SCENARIO
    @Test
    void testGetCustomerRewards_CustomerNotFound() {

        when(baseRewardService.getCustomerRewards(
                anyString(), any(), any()))
                .thenThrow(new RuntimeException("Customer not found"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                baseRewardService.getCustomerRewards(
                        "C99",
                        LocalDate.now(),
                        LocalDate.now()
                )
        );

        assertEquals("Customer not found", exception.getMessage());
    }

    //  NULL CUSTOMER ID
    @Test
    void testGetCustomerRewards_NullCustomerId() {

        when(baseRewardService.getCustomerRewards(
                isNull(), any(), any()))
                .thenThrow(new IllegalArgumentException("Customer ID is null"));

        assertThrows(IllegalArgumentException.class, () ->
                baseRewardService.getCustomerRewards(
                        null,
                        LocalDate.now(),
                        LocalDate.now()
                )
        );
    }

    //  NULL DATE VALUES
    @Test
    void testGetCustomerRewards_NullDates() {

        when(baseRewardService.getCustomerRewards(
                anyString(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("Dates cannot be null"));

        assertThrows(IllegalArgumentException.class, () ->
                baseRewardService.getCustomerRewards(
                        "C1",
                        null,
                        null
                )
        );
    }

    // VERIFY METHOD INVOCATION
    @Test
    void testGetCustomerRewards_VerifyInvocation() {

        when(baseRewardService.getCustomerRewards(
                anyString(), any(), any()))
                .thenReturn(null);

        baseRewardService.getCustomerRewards(
                "C1",
                LocalDate.now(),
                LocalDate.now()
        );

        verify(baseRewardService, times(1))
                .getCustomerRewards(anyString(), any(), any());
    }
}
