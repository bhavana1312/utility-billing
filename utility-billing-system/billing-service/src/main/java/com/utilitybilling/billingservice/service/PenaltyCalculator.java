package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.OverduePenaltySlab;
import java.util.List;

public final class PenaltyCalculator{

    private PenaltyCalculator(){}

    public static double calculatePenalty(
            double baseAmount,
            int overdueDays,
            List<OverduePenaltySlab> slabs){

        for(OverduePenaltySlab slab:slabs){
            if(overdueDays>=slab.getFromDay()
                    && overdueDays<=slab.getToDay()){
                return baseAmount
                        *slab.getPenaltyPercentage()/100.0;
            }
        }
        return 0;
    }
}
