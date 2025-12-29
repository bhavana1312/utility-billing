package com.utilitybilling.billingservice.controller;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.model.BillStatus;
import com.utilitybilling.billingservice.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController{

    private final BillingService billingService;
    private final BillingQueryService queryService;

    @PostMapping("/generate")
    public ResponseEntity<BillResponse> generate(
            @Valid @RequestBody GenerateBillRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generate(request));
    }

    @GetMapping("/{consumerId}")
    public List<BillResponse> consumerBills(
            @PathVariable("consumerId") String consumerId){
        return queryService.consumerBills(consumerId);
    }

    @GetMapping
    public List<BillResponse> all(
            @RequestParam(required=false) BillStatus status){
        return queryService.all(status);
    }  

}
