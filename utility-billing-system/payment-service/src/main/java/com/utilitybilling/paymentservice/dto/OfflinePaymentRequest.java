package com.utilitybilling.paymentservice.dto;

import com.utilitybilling.paymentservice.model.PaymentMode;
import lombok.Data;

@Data
public class OfflinePaymentRequest{
    private String billId;
    private PaymentMode mode;
}
