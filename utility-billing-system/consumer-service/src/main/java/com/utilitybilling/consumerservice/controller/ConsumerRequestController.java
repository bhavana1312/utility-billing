package com.utilitybilling.consumerservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utilitybilling.consumerservice.dto.ConsumerRequestResponse;
import com.utilitybilling.consumerservice.dto.CreateConsumerRequest;
import com.utilitybilling.consumerservice.dto.RejectRequest;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.service.ConsumerRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<Page<ConsumerRequest>> getAll(
    		@RequestParam(name="status",required=false) String status,
    		@RequestParam(name="page",defaultValue="0") int page,
    		@RequestParam(name="size",defaultValue="10") int size){
    	return ResponseEntity.ok(service.getAll(status,page,size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumerRequest> get(@PathVariable("id") String id){
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable("id") String id,@Valid @RequestBody RejectRequest r){
        service.reject(id,r.getReason());
        return ResponseEntity.noContent().build();
    }
}
