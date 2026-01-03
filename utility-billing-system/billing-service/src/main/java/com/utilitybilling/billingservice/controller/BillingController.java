package com.utilitybilling.billingservice.controller;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.model.BillStatus;
import com.utilitybilling.billingservice.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/generate")
    public ResponseEntity<BillResponse> generate(
            @Valid @RequestBody GenerateBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generate(request));
    }

    @GetMapping("/{consumerId}")
    public Page<BillResponse> consumerBills(
            @PathVariable String consumerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return billingService.consumerBills(consumerId, page, size);
    }

    @GetMapping
    public Page<BillResponse> all(
            @RequestParam(required = false) BillStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return billingService.all(status, page, size);
    }
}
