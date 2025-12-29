package com.utilitybilling.paymentservice.dto;

import lombok.Data;

@Data
public class InitiatePaymentRequest{
    private String billId;
}
