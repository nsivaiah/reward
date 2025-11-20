package com.offer.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.offer.dto.RewardSummary;
import com.offer.service.RewardService;

@WebMvcTest(controllers = RewardController.class)
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    @Test
    void shouldReturnBadRequest_whenMissingQueryParams() throws Exception {
        // missing startDate and endDate -> should be 400
        mockMvc.perform(get("/rewards/{customerId}", "anyId")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenDateFormatInvalid() throws Exception {
        // invalid date format for startDate
        mockMvc.perform(get("/rewards/{customerId}", "cust99")
                .param("startDate", "2025-13-99") // invalid
                .param("endDate", "2025-03-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkAndCallService_withValidInputs() throws Exception {
        // arrange
        var customerId = "cust123";
        var start = "2025-01-01";
        var end = "2025-03-31";

        // Return a Mockito mock for the DTO (Jackson will serialize it as an empty JSON object "{}")
        RewardSummary mockSummary = org.mockito.Mockito.mock(RewardSummary.class);
        when(rewardService.getCustomerRewards(eq(customerId),
                                             eq(LocalDate.parse(start)),
                                             eq(LocalDate.parse(end))))
            .thenReturn(mockSummary);

        // act / assert
        mockMvc.perform(get("/rewards/{customerId}", customerId)
                .param("startDate", start)
                .param("endDate", end)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ensure some JSON was produced (works even if mock serializes to "{}")
                .andExpect(jsonPath("$").exists());

        // verify service was called with parsed LocalDate arguments
        verify(rewardService).getCustomerRewards(eq(customerId),
                                                eq(LocalDate.parse(start)),
                                                eq(LocalDate.parse(end)));
    }
}
