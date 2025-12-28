package com.utilitybilling.consumerservice.controller;

import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.service.ConsumerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consumer-requests")
@RequiredArgsConstructor
public class ConsumerRequestController{

    private final ConsumerRequestService service;

    @PostMapping
    public ResponseEntity<ConsumerRequestResponse> submit(@Valid @RequestBody CreateConsumerRequest r){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(r));
    }

    @GetMapping
    public ResponseEntity<List<ConsumerRequest>> getAll(@RequestParam(required=false) String status){
        return ResponseEntity.ok(service.getAll(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumerRequest> get(@PathVariable String id){
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable String id,@Valid @RequestBody RejectRequest r){
        service.reject(id,r.getReason());
        return ResponseEntity.noContent().build();
    }
}
