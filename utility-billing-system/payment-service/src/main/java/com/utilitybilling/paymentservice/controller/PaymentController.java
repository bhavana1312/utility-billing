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
    
    @GetMapping("/history/{consumerId}")
    public Object history(@PathVariable String consumerId){
        return service.history(consumerId);
    }

    @GetMapping("/invoices/{consumerId}")
    public Object invoices(@PathVariable String consumerId){
        return service.invoices(consumerId);
    }
    
    @GetMapping("/outstanding/{consumerId}")
    public Object outstanding(@PathVariable String consumerId){
        return service.outstanding(consumerId);
    }
}
