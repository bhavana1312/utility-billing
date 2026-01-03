package com.utilitybilling.billingservice.controller;

import com.utilitybilling.billingservice.dto.OutstandingBalanceResponse;
import com.utilitybilling.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/internal")
@RequiredArgsConstructor
public class InternalBillingController {

    private final BillingService billingService;

    @GetMapping("/{billId}")
    public Object getBill(@PathVariable String billId) {
        return billingService.getById(billId);
    }

    @PutMapping("/{billId}/mark-paid")
    public ResponseEntity<Void> markPaid(@PathVariable String billId) {
        billingService.markPaid(billId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer/{consumerId}/outstanding")
    public OutstandingBalanceResponse outstanding(@PathVariable String consumerId) {
        return billingService.outstanding(consumerId);
    }
}
