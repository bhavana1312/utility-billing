package com.utilitybilling.tariffservice.controller;

import com.utilitybilling.tariffservice.dto.CreateTariffRequest;
import com.utilitybilling.tariffservice.dto.TariffResponse;
import com.utilitybilling.tariffservice.dto.UpdateTariffRequest;
import com.utilitybilling.tariffservice.model.UtilityType;
import com.utilitybilling.tariffservice.service.TariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/utilities/tariffs")
@RequiredArgsConstructor
public class TariffController{

    private final TariffService service;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateTariffRequest r){
        service.create(r);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{utilityType}")
    public ResponseEntity<TariffResponse> getActive(@PathVariable("utilityType")  UtilityType utilityType){
        return ResponseEntity.ok(service.getActive(utilityType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable("id") String id){
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateTariffRequest r){
        service.update(id,r);
        return ResponseEntity.noContent().build();
    }
}
