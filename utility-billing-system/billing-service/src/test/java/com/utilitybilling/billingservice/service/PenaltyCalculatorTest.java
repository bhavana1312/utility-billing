package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.OverduePenaltySlab;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyCalculatorTest {

    @Test
    void calculatePenalty_withinSlab() {
        OverduePenaltySlab slab=new OverduePenaltySlab();
        slab.setFromDay(1);
        slab.setToDay(5);
        slab.setPenaltyPercentage(10);

        BigDecimal penalty=PenaltyCalculator.calculatePenalty(
                BigDecimal.valueOf(200),3,List.of(slab));

        assertEquals(BigDecimal.valueOf(20.00).setScale(2),penalty);
    }

    @Test
    void calculatePenalty_outsideSlab() {
        OverduePenaltySlab slab=new OverduePenaltySlab();
        slab.setFromDay(1);
        slab.setToDay(2);
        slab.setPenaltyPercentage(10);

        BigDecimal penalty=PenaltyCalculator.calculatePenalty(
                BigDecimal.valueOf(200),10,List.of(slab));

        assertEquals(BigDecimal.ZERO,penalty);
    }
}
