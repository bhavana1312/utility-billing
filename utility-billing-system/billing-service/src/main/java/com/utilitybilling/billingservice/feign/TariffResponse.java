package com.utilitybilling.billingservice.feign;

import lombok.Data;
import java.util.List;

@Data
public class TariffResponse{

    private String utilityType;
    private List<TariffSlab> slabs;
    private double fixedCharge;
    private double taxPercentage;
    private List<OverduePenaltySlab> overduePenaltySlabs;
}
