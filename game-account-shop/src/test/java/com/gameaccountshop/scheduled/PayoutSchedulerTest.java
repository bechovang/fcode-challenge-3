package com.gameaccountshop.scheduled;

import com.gameaccountshop.service.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutSchedulerTest {

    @Mock
    private PayoutService payoutService;

    @InjectMocks
    private PayoutScheduler payoutScheduler;

    @Test
    void testMonthlyPayoutCreation_OnFirstOfMonth() {
        // Given
        doNothing().when(payoutService).createMonthlyPayouts();

        // When
        payoutScheduler.createMonthlyPayouts();

        // Then
        verify(payoutService, times(1)).createMonthlyPayouts();
    }

    @Test
    void testMonthlyPayoutCreation_handlesException() {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(payoutService).createMonthlyPayouts();

        // When - should not throw
        payoutScheduler.createMonthlyPayouts();

        // Then - service was still called
        verify(payoutService, times(1)).createMonthlyPayouts();
    }
}
