package com.utilitybilling.billingservice.controller;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController{

    private final BillingService service;

    @PostMapping("/generate")
    public ResponseEntity<BillResponse> generate(
            @Valid @RequestBody GenerateBillRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.generate(request));
    }
}
