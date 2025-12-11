package com.offer.service;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.offer.dto.RewardSummary;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;

class BaseRewardServiceTest {

    private Validator validator;
    private ExecutableValidator executableValidator;
    private BaseRewardService service;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        executableValidator = validator.forExecutables();

        // Anonymous implementation for testing
        service = new BaseRewardService() {
            @Override
            public RewardSummary getCustomerRewards(String customerId, LocalDate startDate, LocalDate endDate) {
                return null; // logic not needed for validation tests
            }
        };
    }

    @Test
    void testCustomerIdNotBlankOrNull() throws Exception {
        Method method = service.getClass().getMethod(
                "getCustomerRewards", String.class, LocalDate.class, LocalDate.class);

        // Test blank customerId
        Object[] paramsBlank = { "   ", LocalDate.now(), LocalDate.now() };
        Set<ConstraintViolation<BaseRewardService>> violationsBlank =
                executableValidator.validateParameters(service, method, paramsBlank);

        assertFalse(violationsBlank.isEmpty());
        assertTrue(violationsBlank.stream()
                .anyMatch(v -> v.getMessage().contains("Customer ID cannot be null or empty")));

        // Test null customerId
        Object[] paramsNull = { null, LocalDate.now(), LocalDate.now() };
        Set<ConstraintViolation<BaseRewardService>> violationsNull =
                executableValidator.validateParameters(service, method, paramsNull);

        assertFalse(violationsNull.isEmpty());
        assertTrue(violationsNull.stream()
                .anyMatch(v -> v.getMessage().contains("Customer ID cannot be null or empty")));
    }

    @Test
    void testStartDateNotNull() throws Exception {
        Method method = service.getClass().getMethod(
                "getCustomerRewards", String.class, LocalDate.class, LocalDate.class);

        Object[] params = { "C1", null, LocalDate.now() };
        Set<ConstraintViolation<BaseRewardService>> violations =
                executableValidator.validateParameters(service, method, params);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Start date cannot be null")));
    }

    @Test
    void testEndDateNotNull() throws Exception {
        Method method = service.getClass().getMethod(
                "getCustomerRewards", String.class, LocalDate.class, LocalDate.class);

        Object[] params = { "C1", LocalDate.now(), null };
        Set<ConstraintViolation<BaseRewardService>> violations =
                executableValidator.validateParameters(service, method, params);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("End date cannot be null")));
    }

    @Test
    void testValidParameters_NoViolations() throws Exception {
        Method method = service.getClass().getMethod(
                "getCustomerRewards", String.class, LocalDate.class, LocalDate.class);

        Object[] params = { "C1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1) };
        Set<ConstraintViolation<BaseRewardService>> violations =
                executableValidator.validateParameters(service, method, params);

        assertTrue(violations.isEmpty());
    }
}
