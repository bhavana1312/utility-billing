package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.OverduePenaltySlab;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyCalculatorTest {

    @Test
    void shouldCalculatePenaltyWithinSlab() {
        OverduePenaltySlab slab = new OverduePenaltySlab();
        slab.setFromDay(1);
        slab.setToDay(10);
        slab.setPenaltyPercentage(5);

        double penalty = PenaltyCalculator.calculatePenalty(
                1000, 5, List.of(slab));

        assertEquals(50.0, penalty);
    }

    @Test
    void shouldReturnZeroIfNoSlabMatches() {
        OverduePenaltySlab slab = new OverduePenaltySlab();
        slab.setFromDay(10);
        slab.setToDay(20);
        slab.setPenaltyPercentage(10);

        double penalty = PenaltyCalculator.calculatePenalty(
                1000, 3, List.of(slab));

        assertEquals(0.0, penalty);
    }
}
