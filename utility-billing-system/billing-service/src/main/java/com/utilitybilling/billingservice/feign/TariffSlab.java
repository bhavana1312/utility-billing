package com.utilitybilling.billingservice.feign;

import lombok.Data;

@Data
public class TariffSlab{
    private int fromUnit;
    private int toUnit;
    private double ratePerUnit;
}
