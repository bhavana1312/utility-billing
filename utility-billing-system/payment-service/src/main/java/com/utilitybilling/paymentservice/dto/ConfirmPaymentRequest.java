package com.utilitybilling.paymentservice.dto;

import lombok.Data;

@Data
public class ConfirmPaymentRequest{
    private String paymentId;
    private String otp;
}
