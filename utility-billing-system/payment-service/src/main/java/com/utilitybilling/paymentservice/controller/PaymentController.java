package com.utilitybilling.paymentservice.controller;

import com.utilitybilling.paymentservice.dto.*;
import com.utilitybilling.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController{

    private final PaymentService service;

    @PostMapping("/initiate")
    public Object initiate(@RequestBody InitiatePaymentRequest r){
        return service.initiate(r);
    }

    @PostMapping("/confirm")
    public Object confirm(@RequestBody ConfirmPaymentRequest r){
        return service.confirm(r);
    }

    @PostMapping("/offline")
    public void offline(@RequestBody OfflinePaymentRequest r){
        service.offlinePay(r);
    }
}
